// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Component;

/**
 * 微信公众号「客服消息」能力探测器。
 * <p>
 * 用途：在应用启动 / 后台菜单（自动回复）配置页访问时，探测当前公众号账号是否具备
 * 客服消息接口权限（订阅号 / 未认证服务号通常无此能力），从而允许前端按需降级：
 * <ul>
 *   <li>有能力 —— 正常展示「48 小时内主动下发客服消息」相关配置项</li>
 *   <li>无能力 —— 前端弹出降级提示，隐藏 / 禁用相关配置</li>
 * </ul>
 *
 * <p>探测策略：
 * <ul>
 *   <li>启动时通过 {@link PostConstruct} 同步执行一次
 *       {@code wxMpService.getKefuService().kfList()}，捕获
 *       {@link WxErrorException}（常见错误码 48001/45015/40001 等）。</li>
 *   <li>结果缓存 <b>5 分钟</b>；{@link #isEnabled()} 在缓存过期时会同步触发一次
 *       {@link #refresh()}，前端调用频次较低，单次多 1 次微信 IO 可接受。</li>
 *   <li>失败结果同样缓存，避免雪崩；网络等非鉴权异常保留上次 {@code enabled} 值。</li>
 *   <li>对外只暴露 {@link #isEnabled()} / {@link #refresh()} 两个方法。</li>
 * </ul>
 *
 * <p>注入策略说明：{@code WxMpService} 配置缺失时
 * {@code WxMpConfig#wxService()} 会抛 {@link IllegalStateException}，
 * 若以 {@code required=true} 注入将导致本 bean 级联创建失败。
 * 因此改用 {@code @Autowired(required = false)}，当公众号未配置时
 * {@link #wxMpService} 为 {@code null}，本类自身仍可正常暴露
 * {@code isEnabled() == false}，供前端展示降级提示。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Component
@Slf4j
public class WxKefuCapabilityProbe {

    /** 能力探测结果缓存 TTL：5 分钟。 */
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    /**
     * 当前是否具备客服消息能力。
     * <p>乐观默认 {@code true}，避免启动瞬间前端误显示降级提示条；
     * {@link #initProbe()} 会立刻执行一次探测并以真实结果覆盖。
     */
    private volatile boolean enabled = true;

    /** 上次探测完成时间戳（ms），0 表示尚未探测。 */
    private volatile long lastCheckedAt = 0L;

    /**
     * 公众号服务统一入口；当公众号配置缺失时 {@link WxMpServiceHolder#get()} 返回 {@code null}。
     */
    @Resource
    private WxMpServiceHolder wxMpServiceHolder;

    /**
     * 启动自检：容器初始化完毕后立即探测一次，失败降级为 {@code enabled=false} 并记日志，
     * 不抛异常阻断启动流程。
     */
    @PostConstruct
    public void initProbe() {
        try {
            refresh();
        } catch (Exception e) {
            log.warn("客服消息能力启动探测异常（不影响启动）", e);
        }
    }

    /**
     * 当前公众号是否具备客服消息接口能力。
     * <p>若缓存过期（超过 {@value #CACHE_TTL_MS} ms）则同步触发一次 {@link #refresh()}
     * 以更新结果，随后返回最新 {@link #enabled} 值。
     */
    public boolean isEnabled() {
        if (System.currentTimeMillis() - lastCheckedAt > CACHE_TTL_MS) {
            refresh();
        }
        return enabled;
    }

    /**
     * 手动 / 定时刷新能力探测结果。
     * <p>探测逻辑：
     * <ul>
     *   <li>{@code wxMpService == null}（公众号未配置）→ {@code enabled=false}</li>
     *   <li>{@code kfList()} 调用成功 → {@code enabled=true}</li>
     *   <li>捕获 {@link WxErrorException}，errcode 为
     *       {@code 48001/45015/40001} 等鉴权类 → {@code enabled=false}；
     *       其余（如网络抖动）保留上次 {@code enabled} 值</li>
     *   <li>其他任意异常 → 视为未开通，{@code enabled=false}</li>
     * </ul>
     * <p>{@code synchronized} 避免并发重复探测；无论成功失败，{@link #lastCheckedAt}
     * 都会更新，防止失败瞬间被反复打到微信侧。
     */
    public synchronized void refresh() {
        WxMpService wxMpService = wxMpServiceHolder.get();
        if (wxMpService == null) {
            enabled = false;
            lastCheckedAt = System.currentTimeMillis();
            log.warn("客服消息能力探测：公众号未配置，视为未开通");
            return;
        }
        try {
            wxMpService.getKefuService().kfList();
            enabled = true;
            log.info("客服消息能力探测：已开通");
        } catch (WxErrorException e) {
            int code = e.getError() == null ? -1 : e.getError().getErrorCode();
            String msg = e.getError() == null ? e.getMessage() : e.getError().getErrorMsg();
            if (code == 48001 || code == 45015 || code == 40001) {
                enabled = false;
                log.warn("客服消息能力探测：未开通 errcode={} errmsg={}", code, msg);
            } else {
                log.warn("客服消息能力探测：WxError errcode={} errmsg={}，保留上次状态 enabled={}",
                        code, msg, enabled);
            }
        } catch (Exception e) {
            enabled = false;
            log.warn("客服消息能力探测：异常 {}，视为未开通", e.getMessage());
        } finally {
            lastCheckedAt = System.currentTimeMillis();
        }
    }
}
