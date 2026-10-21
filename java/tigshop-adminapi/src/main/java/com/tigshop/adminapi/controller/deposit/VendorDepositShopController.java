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
import com.tigshop.service.deposit.VendorDepositShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商端保证金聚合控制器（财务中心 &gt; 保证金）。
 *
 * <p>与店铺端 {@link DepositShopController} 平行，但持有者维度为供应商：身份取自供应商端鉴权头
 * {@link HeaderUtils#getVendorId()}（X-Admin-Type=vendor + X-Vendor-Id）。所有接口仅返回当前
 * vendorId 的数据，不接收外部传入的 vendorId，避免越权查询他供应商保证金。本控制器仅做展示性
 * 聚合，不承接充值/退还申请提交（由 DepositRecharge/DepositRefund 代理实现）。</p>
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/vendor")
@Tag(name = "供应商端保证金", description = "供应商端保证金总览/流水/冻结明细/预警/协议")
public class VendorDepositShopController {

    @Resource
    private VendorDepositShopService vendorDepositShopService;

    @GetMapping("/overview")
    @Operation(summary = "保证金总览")
    public DepositShopOverviewVO overview() {
        return vendorDepositShopService.getOverview(currentVendorId());
    }

    @GetMapping("/logList")
    @Operation(summary = "供应商端交易流水")
    public Page<DepositShopLogVO> logList(DepositShopLogQueryParam param) {
        return vendorDepositShopService.logList(currentVendorId(), param);
    }

    @GetMapping("/frozenDetailList")
    @Operation(summary = "冻结明细(平台扣款记录)")
    public Page<DepositShopFrozenDetailVO> frozenDetailList(DepositShopLogQueryParam param) {
        return vendorDepositShopService.frozenDetailList(currentVendorId(), param);
    }

    @GetMapping("/warning")
    @Operation(summary = "首页保证金预警横幅")
    public DepositShopWarningVO warning() {
        return vendorDepositShopService.getWarning(currentVendorId());
    }

    @GetMapping("/protocol")
    @Operation(summary = "保证金协议")
    public DepositShopProtocolVO protocol() {
        return vendorDepositShopService.getProtocol();
    }

    /**
     * 解析当前供应商id，未登录供应商端或缺少供应商标识时抛出系统错误。
     */
    private Integer currentVendorId() {
        Integer vendorId = HeaderUtils.getVendorId();
        if (vendorId == null || vendorId <= 0) {
            throw new GlobalException("无法获取当前供应商信息", HttpStatus.UNAUTHORIZED);
        }
        return vendorId;
    }
}
