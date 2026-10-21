package com.tigshop.api.controller.user;

import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceSaveParam;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceEditParam;
import com.tigshop.bean.vo.finance.OrderInvoiceDetailVO;
import com.tigshop.service.finance.OrderInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户订单发票
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/orderInvoice"))
@Tag(name = "前台用户发票")
@Validated
public class OrderInvoiceClientController {
    @Resource
    OrderInvoiceService orderInvoiceService;

    @GetMapping("/detail")
    @Operation(summary = "发票详情")
    public OrderInvoiceDetailVO detail(Integer id) {
        return orderInvoiceService.clientDetail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "发票申请")
    public void create(@RequestBody @Valid OrderInvoiceSaveParam createDTO) {
        orderInvoiceService.clientCreate(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新发票申请数据")
    public void update(@RequestBody OrderInvoiceEditParam updateDTO) {
        orderInvoiceService.clientUpdate(updateDTO);
    }
}
