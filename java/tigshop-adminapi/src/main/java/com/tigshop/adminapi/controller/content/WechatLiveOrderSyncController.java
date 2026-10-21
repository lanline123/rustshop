package com.tigshop.adminapi.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.content.WechatLiveSyncLog;
import com.tigshop.bean.query.content.WechatLiveSyncLogPageQuery;
import com.tigshop.service.content.WechatLiveOrderSyncService;
import com.tigshop.service.content.WechatLiveSyncLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 微信直播订单同步控制器
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@RestController
@RequestMapping("/adminapi/content/wechatLiveOrderSync")
@Tag(name = "微信直播订单同步", description = "微信直播订单同步管理接口")
@PreAuthorize("@pms.hasPermission('wechatLiveManage')")
public class WechatLiveOrderSyncController {

    @Resource
    private WechatLiveSyncLogService wechatLiveSyncLogService;

    @Resource
    private WechatLiveOrderSyncService wechatLiveOrderSyncService;

    /**
     * 同步日志列表
     * <p>请求参数格式：Query（URL参数）</p>
     * @param pageQuery 分页查询条件
     * @return 分页日志列表
     */
    @GetMapping("/logs")
    @Operation(summary = "同步日志列表", description = "请求参数格式：Query（URL参数）")
    public Page<WechatLiveSyncLog> logs(WechatLiveSyncLogPageQuery pageQuery) {
        return wechatLiveSyncLogService.page(pageQuery);
    }

    /**
     * 重试同步
     * <p>请求参数格式：Path（路径参数）</p>
     * @param logId 日志ID
     */
    @PostMapping("/retry/{logId}")
    @Operation(summary = "重试同步", description = "请求参数格式：Path（路径参数）")
    public void retry(@PathVariable Long logId) {
        wechatLiveOrderSyncService.retrySyncLog(logId);
    }

    /**
     * 触发对账
     * <p>请求参数格式：无参数</p>
     */
    @PostMapping("/reconcile")
    @Operation(summary = "触发对账", description = "请求参数格式：无参数")
    public void reconcile() {
        wechatLiveOrderSyncService.reconcileLiveOrders();
    }
}
