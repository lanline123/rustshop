package com.tigshop.service.msg.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tigshop.bean.vo.msg.AdminMsgCountVO;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.im.ImMessageService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msg.SseService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static com.tigshop.common.utils.HeaderUtils.*;

/**
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    /**
     * 消息播报推送协议版本号。
     * v2：补齐 paidOrderCount/aftersaleCount/cancelOrderCount。
     * v3：paidOrderCount/aftersaleCount 改为单调累计口径，并新增单调字段 customerMsgCount
     *     专供客服消息播报增量判断（imMsgCount 仍为可回落的未读数，仅供 UI 角标）。
     * v4：新增 newOrderMsgCount/paidOrderMsgCount/aftersaleMsgCount/cancelOrderMsgCount，
     *     来源改为 admin_msg 表（与消息中心同源、同过滤口径），供语音播报对齐消息中心；
     *     旧的 orderCount/paidOrderCount/aftersaleCount/cancelOrderCount 仍保留（兼容/红点角标）。
     */
    private static final int MSG_COUNT_PROTOCOL_VERSION = 4;

    /** 语音播报对齐消息中心的 4 类 admin_msg 类型：11 新订单 / 12 已付款 / 31 订单取消 / 32 售后申请 */
    private static final java.util.List<Integer> BROADCAST_MSG_TYPES = java.util.Arrays.asList(11, 12, 31, 32);

    private final AdminMsgService adminMsgService;
    private final OrderService orderService;
    private final ImMessageService imMessageService;
    private final AftersalesService aftersalesService;

    /**
     * 消息计数 SSE 推送间隔（秒）。通过 application.yml 的 tigshop.sse.msg-count.interval-seconds 配置，
     * 默认 5 秒；如需回滚到旧的 30 秒行为，仅修改配置 + 重启即可，无需改代码。
     */
    @Value("${tigshop.sse.msg-count.interval-seconds:5}")
    private long msgCountIntervalSeconds;

    private static final class AccountKey {
        private final Integer adminId;
        private final Integer shopId;
        private final Integer vendorId;

        private AccountKey(Integer adminId, Integer shopId, Integer vendorId) {
            this.adminId = adminId;
            this.shopId = shopId;
            this.vendorId = vendorId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AccountKey that = (AccountKey) o;
            return java.util.Objects.equals(adminId, that.adminId)
                    && java.util.Objects.equals(shopId, that.shopId)
                    && java.util.Objects.equals(vendorId, that.vendorId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(adminId, shopId, vendorId);
        }
    }

    private final Map<AccountKey, ConcurrentHashMap<String, SseEmitter>> clients = new ConcurrentHashMap<>();

    // 任务线程池，支持自动清理
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(
            5,
            new ThreadFactoryBuilder().setNameFormat("sse-push-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final Map<String, RequestAttributes> requestContextMap = new ConcurrentHashMap<>();
    private final Map<String, AccountKey> connectionKeyMap = new ConcurrentHashMap<>();

    private final Map<AccountKey, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public SseEmitter createConnection(Integer adminId) {
        // 超时设置为 30 分钟（可按需调节）
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        Integer shopId = getShopId();
        Integer vendorId = getVendorId();
        AccountKey key = new AccountKey(adminId, shopId, vendorId);
        String connectionId = registerClient(key, emitter);
        if (attrs != null) {
            requestContextMap.put(connectionId, attrs);
        }

        emitter.onCompletion(() -> cleanupConnection(adminId, connectionId));
        emitter.onTimeout(() -> cleanupConnection(adminId, connectionId));
        emitter.onError(e -> cleanupConnection(adminId, connectionId));

        return emitter;
    }

    public void startPush(AccountKey key, Callable<AdminMsgCountVO> task, long intervalSec) {
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            ConcurrentHashMap<String, SseEmitter> clientMap = clients.get(key);
            if (clientMap == null || clientMap.isEmpty()) {
                stopPush(key);
                return;
            }

            try {
                broadcast(key, task);
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }, 0, intervalSec, TimeUnit.SECONDS);

        // 原子替换并取消上一个周期任务：避免并发 startPush（前端频繁重连）时，
        // 旧的「先 stopPush 再 put」存在竞态窗口导致被覆盖的周期任务未被取消而泄漏，
        // 同一 key 多个不同 startTime 的任务并行推送，使前端计数在多个值之间反复横跳。
        ScheduledFuture<?> previous = tasks.put(key, future);
        if (previous != null) {
            previous.cancel(true);
        }
    }

    public SseEmitter getClient(Integer adminId) {
        for (Map.Entry<AccountKey, ConcurrentHashMap<String, SseEmitter>> entry : clients.entrySet()) {
            if (entry.getKey().adminId != null && entry.getKey().adminId.equals(adminId)) {
                ConcurrentHashMap<String, SseEmitter> map = entry.getValue();
                if (map != null && !map.isEmpty()) {
                    return map.values().iterator().next();
                }
            }
        }
        return null;
    }

    /**
     * 仅清理 map 数据，不触碰 emitter 生命周期。
     * 用于 onCompletion / onTimeout / onError 回调和 broadcast 失败场景，
     * 此时 emitter 已由 Spring 接管，再调 complete() 会触发 AsyncRequestNotUsableException。
     */
    private void cleanupConnection(Integer adminId, String connectionId) {
        AccountKey key = connectionKeyMap.get(connectionId);
        if (key == null || (key.adminId != null && !key.adminId.equals(adminId))) {
            return;
        }
        ConcurrentHashMap<String, SseEmitter> map = clients.get(key);
        if (map != null) {
            map.remove(connectionId);
            requestContextMap.remove(connectionId);
            connectionKeyMap.remove(connectionId);
            if (map.isEmpty()) {
                clients.remove(key);
                stopPush(key);
            }
        }
    }

    /**
     * 主动移除某个管理员的单个连接并关闭 emitter。
     * 仅用于业务层主动断开（非回调场景）。
     */
    public void removeClient(Integer adminId, String connectionId) {
        AccountKey key = connectionKeyMap.get(connectionId);
        if (key == null || (key.adminId != null && !key.adminId.equals(adminId))) {
            return;
        }
        ConcurrentHashMap<String, SseEmitter> map = clients.get(key);
        if (map != null) {
            SseEmitter emitter = map.remove(connectionId);
            if (emitter != null) {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
            requestContextMap.remove(connectionId);
            connectionKeyMap.remove(connectionId);
            if (map.isEmpty()) {
                clients.remove(key);
                stopPush(key);
            }
        }
    }

    // 移除某个管理员的所有连接（谨慎调用）
    public void removeClient(Integer adminId) {
        for (AccountKey key : new java.util.HashSet<>(clients.keySet())) {
            if (key.adminId != null && key.adminId.equals(adminId)) {
                stopPush(key);
                ConcurrentHashMap<String, SseEmitter> map = clients.remove(key);
                if (map != null) {
                    for (Map.Entry<String, SseEmitter> entry : map.entrySet()) {
                        String connectionId = entry.getKey();
                        SseEmitter emitter = entry.getValue();
                        try {
                            emitter.complete();
                        } catch (Exception ignored) {
                        }
                        requestContextMap.remove(connectionId);
                        connectionKeyMap.remove(connectionId);
                    }
                }
            }
        }
    }

    public void stopPush(AccountKey key) {
        ScheduledFuture<?> future = tasks.remove(key);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void stopPush(Integer adminId) {
        for (AccountKey key : new java.util.HashSet<>(tasks.keySet())) {
            if (key.adminId != null && key.adminId.equals(adminId)) {
                stopPush(key);
            }
        }
    }

    @Override
    public SseEmitter msgCount(Long startTime) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        SseEmitter emitter = this.createConnection(adminId);

        Integer currentShopId = getShopId();
        Integer currentVendorId = getVendorId();
        AccountKey key = new AccountKey(adminId, currentShopId, currentVendorId);
        this.startPush(key, () -> {
            Integer sId = getShopId();
            String aType = getAdminType();
            Integer vId = getVendorId();
            // 语音播报 4 类计数改读 admin_msg（与消息中心同源、同过滤口径），实现播报与消息中心事件级一致
            Map<Integer, Long> broadcastMsgCount =
                    adminMsgService.getNewMsgCountByType(aType, sId, vId, startTime, BROADCAST_MSG_TYPES);
            return AdminMsgCountVO.builder()
                    .imMsgCount(imMessageService.getUnreadImMessageCount(sId, adminId))
                    .customerMsgCount(imMessageService.getReceivedImMessageCount(sId, adminId, startTime))
                    .orderCount(orderService.getNewOrderCount(aType, sId, vId, startTime))
                    .unreadMsgCount(adminMsgService.getUnreadAdminMsgCount(aType, sId, vId))
                    .paidOrderCount(orderService.getPaidOrderCount(aType, sId, vId, startTime))
                    .aftersaleCount(aftersalesService.getNewAftersaleCount(aType, sId, vId, startTime))
                    .cancelOrderCount(orderService.getCancelOrderCount(aType, sId, vId, startTime))
                    .newOrderMsgCount(broadcastMsgCount.getOrDefault(11, 0L))
                    .paidOrderMsgCount(broadcastMsgCount.getOrDefault(12, 0L))
                    .cancelOrderMsgCount(broadcastMsgCount.getOrDefault(31, 0L))
                    .aftersaleMsgCount(broadcastMsgCount.getOrDefault(32, 0L))
                    .ts(System.currentTimeMillis() / 1000)
                    .version(MSG_COUNT_PROTOCOL_VERSION)
                    .adminId(adminId)
                    .build();
        }, msgCountIntervalSeconds);

        return emitter;
    }

    private void broadcast(AccountKey key, Callable<AdminMsgCountVO> task) {
        ConcurrentHashMap<String, SseEmitter> map = clients.get(key);
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, SseEmitter> entry : map.entrySet()) {
            String connectionId = entry.getKey();
            SseEmitter client = entry.getValue();
            try {
                RequestContextHolder.setRequestAttributes(requestContextMap.get(connectionId));
                AdminMsgCountVO data = task.call();
                client.send(SseEmitter.event()
                        .name("msg")
                        .data(AjaxResult.success(data)));
                client.send(SseEmitter.event().comment("keep-alive"));
                client.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                cleanupConnection(key.adminId, connectionId);
            }
        }
    }

    private void broadcastByShop(Integer adminId, Integer shopId, Callable<AdminMsgCountVO> task) {
        for (Map.Entry<AccountKey, ConcurrentHashMap<String, SseEmitter>> entryKey : clients.entrySet()) {
            AccountKey key = entryKey.getKey();
            if (key.adminId != null && key.adminId.equals(adminId) && key.shopId != null && key.shopId.equals(shopId)) {
                ConcurrentHashMap<String, SseEmitter> map = entryKey.getValue();
                if (map == null || map.isEmpty()) {
                    continue;
                }
                for (Map.Entry<String, SseEmitter> entry : map.entrySet()) {
                    String connectionId = entry.getKey();
                    SseEmitter client = entry.getValue();
                    try {
                        RequestContextHolder.setRequestAttributes(requestContextMap.get(connectionId));
                        AdminMsgCountVO data = task.call();
                        client.send(SseEmitter.event()
                                .name("msg")
                                .data(AjaxResult.success(data)));
                        client.send(SseEmitter.event().comment("keep-alive"));
                        client.send(SseEmitter.event().comment("heartbeat"));
                    } catch (Exception e) {
                        cleanupConnection(adminId, connectionId);
                    }
                }
            }
        }
    }

    private String registerClient(AccountKey key, SseEmitter emitter) {
        ConcurrentHashMap<String, SseEmitter> map = clients.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        String connectionId = UUID.randomUUID().toString();
        map.put(connectionId, emitter);
        connectionKeyMap.put(connectionId, key);
        return connectionId;
    }
}
