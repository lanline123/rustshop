package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditOrderListDTO;
import com.tigshop.bean.vo.finance.CreditOrderVO;
import com.tigshop.service.finance.CreditOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账期订单管理控制器
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@RestController
@RequestMapping("/adminapi/credit/order")
@Tag(name = "账期订单管理", description = "账期订单管理功能")
@Validated
@PreAuthorize("@pms.hasPermission('termOrderManage')")
public class CreditOrderController {

    @Resource
    private CreditOrderService creditOrderService;

    @GetMapping("/list")
    @Operation(summary = "账期订单列表")
    @PreAuthorize("@pms.hasPermission('termOrderManage')")
    public Page<CreditOrderVO> list(CreditOrderListDTO listDTO) {
        return creditOrderService.list(listDTO);
    }

}
