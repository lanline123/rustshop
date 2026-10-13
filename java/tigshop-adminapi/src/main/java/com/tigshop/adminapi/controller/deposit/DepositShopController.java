package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.deposit.DepositShopLogQueryParam;
import com.tigshop.bean.vo.deposit.DepositShopFrozenDetailVO;
import com.tigshop.bean.vo.deposit.DepositShopLogVO;
import com.tigshop.bean.vo.deposit.DepositShopOverviewVO;
import com.tigshop.bean.vo.deposit.DepositShopProtocolVO;
import com.tigshop.bean.vo.deposit.DepositShopWarningVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.deposit.DepositShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺端保证金聚合控制器（财务中心 &gt; 保证金）。
 *
 * <p>身份取自店铺端鉴权头：{@link HeaderUtils#getShopId()}（X-Admin-Type=shop + X-Shop-Id）。
 * 所有接口仅返回当前 shopId 的数据，不接收外部传入的 shopId，避免越权查询他店保证金。
 * 本控制器仅做展示性聚合，不承接充值/退还申请提交（由 DepositRecharge/DepositRefund 代理实现）。</p>
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/shop")
@Tag(name = "店铺端保证金", description = "店铺端保证金总览/流水/冻结明细/预警/协议")
public class DepositShopController {

    @Resource
    private DepositShopService depositShopService;

    @GetMapping("/overview")
    @Operation(summary = "保证金总览")
    public DepositShopOverviewVO overview() {
        return depositShopService.getOverview(currentShopId());
    }

    @GetMapping("/logList")
    @Operation(summary = "店铺端交易流水")
    public Page<DepositShopLogVO> logList(DepositShopLogQueryParam param) {
        return depositShopService.logList(currentShopId(), param);
    }

    @GetMapping("/frozenDetailList")
    @Operation(summary = "冻结明细(平台扣款记录)")
    public Page<DepositShopFrozenDetailVO> frozenDetailList(DepositShopLogQueryParam param) {
        return depositShopService.frozenDetailList(currentShopId(), param);
    }

    @GetMapping("/warning")
    @Operation(summary = "首页保证金预警横幅")
    public DepositShopWarningVO warning() {
        return depositShopService.getWarning(currentShopId());
    }

    @GetMapping("/protocol")
    @Operation(summary = "保证金协议")
    public DepositShopProtocolVO protocol() {
        return depositShopService.getProtocol();
    }

    /**
     * 解析当前店铺id，未登录店铺端或缺少店铺标识时抛出系统错误。
     */
    private Integer currentShopId() {
        Integer shopId = HeaderUtils.getShopId();
        if (shopId == null || shopId <= 0) {
            throw new GlobalException("无法获取当前店铺信息", HttpStatus.UNAUTHORIZED);
        }
        return shopId;
    }
}
