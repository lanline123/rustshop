// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceSaveParam;
import com.tigshop.bean.query.finance.orderinvoice.OrderInvoicePageQuery;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceEditParam;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.service.finance.OrderInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 发票申请控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/orderInvoice")
@Tag(name = "发票申请", description = "发票申请功能")
public class OrderInvoiceController {
    @Resource
    private OrderInvoiceService orderInvoiceService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<OrderInvoiceVO> list(OrderInvoicePageQuery query) {
        return orderInvoiceService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public OrderInvoiceVO detail(@RequestParam Integer id) {
        return orderInvoiceService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('orderInvoiceUpdateManage')")
    public void create(@RequestBody @Validated OrderInvoiceSaveParam param) {
        orderInvoiceService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('orderInvoiceUpdateManage')")
    public void update(@RequestBody @Validated OrderInvoiceEditParam param) {
        orderInvoiceService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('orderInvoiceDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        orderInvoiceService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('orderInvoiceUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        orderInvoiceService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('orderInvoiceBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        orderInvoiceService.batch(batchDTO);
    }
}
