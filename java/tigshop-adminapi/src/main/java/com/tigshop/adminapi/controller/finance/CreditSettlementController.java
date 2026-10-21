package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditSettlementCancelDTO;
import com.tigshop.bean.dto.finance.CreditSettlementListDTO;
import com.tigshop.bean.dto.finance.CreditSettlementSubmitDTO;
import com.tigshop.bean.dto.finance.CreditSettlementUserListDTO;
import com.tigshop.bean.dto.finance.CreditOrderListDTO;
import com.tigshop.bean.enums.finance.CreditOrderSettlementStatusEnum;
import com.tigshop.bean.vo.finance.CreditOrderVO;
import com.tigshop.bean.vo.finance.CreditSettlementUserVO;
import com.tigshop.bean.vo.finance.CreditSettlementVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.CreditOrderService;
import com.tigshop.service.finance.CreditSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账期结算管理控制器
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@RestController
@RequestMapping("/adminapi/credit/settlement")
@Tag(name = "账期结算管理", description = "账期结算管理功能")
@Validated
@PreAuthorize("@pms.hasPermission('termPayManage')")
public class CreditSettlementController {

    @Resource
    private CreditSettlementService creditSettlementService;

    @Resource
    private CreditOrderService creditOrderService;

    @GetMapping("/userList")
    @Operation(summary = "结算会员列表")
    @PreAuthorize("@pms.hasPermission('termPayManage')")
    public Page<CreditSettlementUserVO> userList(CreditSettlementUserListDTO listDTO) {
        return creditSettlementService.getUserList(listDTO);
    }

    @GetMapping("/pendingOrders")
    @Operation(summary = "待结算订单列表")
    @PreAuthorize("@pms.hasPermission('termPayManage')")
    public Page<CreditOrderVO> pendingOrders(CreditOrderListDTO listDTO) {
        // 返回待结算 + 退款中订单（支持订单号、下单时间范围筛选）
        listDTO.setSettlementStatusList(List.of(
                CreditOrderSettlementStatusEnum.UNSETTLED.getCode(),
                CreditOrderSettlementStatusEnum.REFUNDING.getCode()
        ));
        return creditOrderService.list(listDTO);
    }

    @PostMapping("/submit")
    @Operation(summary = "确认结算")
    @PreAuthorize("@pms.hasPermission('termPayModifyManage')")
    public void submit(@Valid @RequestBody CreditSettlementSubmitDTO submitDTO) {
        creditSettlementService.submit(submitDTO);
    }

    @GetMapping("/history")
    @Operation(summary = "历史结算列表")
    @PreAuthorize("@pms.hasPermission('termPayManage')")
    public Page<CreditSettlementVO> history(CreditSettlementListDTO listDTO) {
        return creditSettlementService.history(listDTO);
    }

    @GetMapping("/historyOrders")
    @Operation(summary = "历史结算订单明细")
    @PreAuthorize("@pms.hasPermission('termPayManage')")
    public Page<CreditOrderVO> historyOrders(CreditOrderListDTO listDTO) {
        if (listDTO.getSettlementId() == null) {
            throw new GlobalException("结算记录ID不能为空");
        }
        // 根据结算记录ID查询订单明细（撤销后仍可查看）
        return creditOrderService.list(listDTO);
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤销结算")
    @PreAuthorize("@pms.hasPermission('termPayModifyManage')")
    public void cancel(@Valid @RequestBody CreditSettlementCancelDTO cancelDTO) {
        creditSettlementService.cancel(cancelDTO);
    }
}
