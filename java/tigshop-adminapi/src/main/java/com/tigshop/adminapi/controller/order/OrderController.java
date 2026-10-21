package com.tigshop.adminapi.controller.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.dto.order.deliver.DeliverInfo;
import com.tigshop.bean.dto.order.deliver.DeliverOrderParam;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.param.order.AftersalesApplyAdminParam;
import com.tigshop.bean.param.order.ExportItemSaveParam;
import com.tigshop.bean.param.order.OrderAdminNoteParam;
import com.tigshop.bean.param.order.OrderChangeStatusParam;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.vo.order.OrderDeliverImportResultVO;
import com.tigshop.bean.vo.order.OrderPageConfigVO;
import com.tigshop.bean.vo.order.OrderStatusCountVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.order.OrderSpitLogService;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 订单管理控制器
 *
 * @author kidd
 * @create 2025/7/7
 */
@Tag(name = "订单管理", description = "订单管理功能")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/order/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderSpitLogService orderSplitLogService;
    private final ProductService productService;


    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<OrderVO> list(OrderListPageQuery pageQuery) {
        return orderService.list(pageQuery);
    }

    @GetMapping("/statusCount")
    @Operation(summary = "统计各订单状态的数量")
    public OrderStatusCountVO statusCount(OrderListPageQuery pageQuery) {
        return orderService.getStatusCount(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public OrderVO detail(@RequestParam Integer id) {
        return orderService.detail(id);
    }

    @PostMapping("/setAdminNote")
    @Operation(summary = "设置管理员备注")
    @PreAuthorize("@pms.hasPermission('setAdminNoteManage')")
    public void setAdminNote(@RequestBody OrderAdminNoteParam param) {
        orderService.setAdminNote(param);
    }

    @GetMapping("/parentDetail")
    @Operation(summary = "获取父订单详情")
    public Object parentOrderDetail(@RequestParam("id") Integer id) {
        return orderSplitLogService.getParentDataByOrderId(id);
    }

    @PostMapping("/setConfirm")
    @Operation(summary = "设置订单确认")
    @PreAuthorize("@pms.hasPermission('setConfirmManage')")
    public void setConfirm(@RequestBody OrderIdDTO orderIddto) {
        orderService.setOrderConfirm(orderIddto.getId());
    }

    @PostMapping("/splitStoreOrder")
    @Operation(summary = "订单拆分")
    @PreAuthorize("@pms.hasPermission('splitStoreOrderManage')")
    public void splitStoreOrder(@RequestBody OrderIdDTO orderIddto) {
        orderService.splitStoreOrder(orderIddto.getId());
    }

    @PostMapping("/setPaid")
    @Operation(summary = "设置已付款")
    @PreAuthorize("@pms.hasPermission('setPaidManage')")
    public boolean setPaid(@RequestBody OrderIdDTO orderIddto) {
        orderService.setOrderPaid(orderIddto.getId(), orderIddto.getDescription());
        Integer id = orderIddto.getId();
        // true 表示已经拆单
        // false 表示未拆单
        return orderService.count(new QueryWrapper<Order>().eq("order_id", id)) <= 0;
    }

    @PostMapping("cancelOrder")
    @Operation(summary = "取消订单")
    @PreAuthorize("@pms.hasPermission('cancelOrderManage')")
    public void cancelOrder(@RequestBody OrderIdDTO orderIddto) {
        orderService.cancelOrder(orderIddto.getId(),null,orderIddto.getDescription());
    }

    @PostMapping("/delOrder")
    @Operation(summary = "删除订单")
    @PreAuthorize("@pms.hasPermission('delOrderManage')")
    public void delOrder(@RequestBody OrderIdDTO orderIddto) {
        orderService.delOrder(orderIddto.getId());
    }

    @PostMapping("/modifyMoney")
    @Operation(summary = "修改订单金额")
    @PreAuthorize("@pms.hasPermission('orderModifyMoneyManage')")
    public void modifyMoney(@RequestBody OrderModifyMoneyDTO feeData) {
        orderService.modifyOrderMoney(feeData);
    }

    @PostMapping("/modifyConsignee")
    @Operation(summary = "修改收货人信息")
    @PreAuthorize("@pms.hasPermission('orderModifyConsigneeManage')")
    public void modifyConsignee(@RequestBody OrderModifyConsigneeDTO modifyConsigneeDTO) {
        orderService.modifyOrderConsignee(modifyConsigneeDTO);
    }

    @PostMapping("/confirmReceipt")
    @Operation(summary = "确认收货")
    @PreAuthorize("@pms.hasPermission('orderConfirmReceiptManage')")
    public void confirmReceipt(@RequestBody OrderIdDTO orderIddto) {
        orderService.confirmReceipt(orderIddto.getId());
    }


    @PostMapping("/modifyShipping")
    @Operation(summary = "修改配送信息")
    @PreAuthorize("@pms.hasPermission('orderModifyShippingManage')")
    public void modifyShipping(@RequestBody OrderModifyShippingDTO modifyShippingDTO) {
        orderService.modifyOrderShipping(modifyShippingDTO);
    }

    @PostMapping("/modifyProduct")
    @Operation(summary = "修改商品信息")
    @PreAuthorize("@pms.hasPermission('modifyProductManage')")
    public void modifyProduct(@RequestBody OrderModifyProductDTO modifyProductDTO) {
        orderService.modifyOrderProduct(modifyProductDTO);
    }

    @PostMapping("/getAddProductInfo")
    @Operation(summary = "添加商品时获取商品信息")
    public List<Product> getAddProductInfo(@RequestBody Map<String, List<Integer>> params) {
        return productService.getProductByIds(params.get("ids"));
    }

    @Operation(summary = "发货")
    @PreAuthorize("@pms.hasPermission('orderDeliverManage')")
    @PostMapping("/deliver")
    public void deliver(@RequestBody @Validated DeliverOrderParam param) {
        orderService.deliverOrder(param);
    }

    @GetMapping("/orderPrint")
    @Operation(summary = "打印订单")
    public OrderVO orderPrint(@RequestParam Integer id) {
        return orderService.getOrderPrintInfo(id);
    }

    @GetMapping("/orderPrintBill")
    @Operation(summary = "打印电子面单")
    public LogisticsApiLog orderPrintWaybill(@RequestParam Integer id) {
        return orderService.getOrderPrintWaybillInfo(id);
    }

    @GetMapping("/severalDetail")
    @Operation(summary = "批量详情")
    public List<OrderVO> severalDetail(@RequestParam String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        if (CollUtil.isNotEmpty(list)) {
            return orderService.getSeveralDetail(list, null);
        } else {
            throw new GlobalException("#ids 错误");
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('splitStoreOrderManage')")
    public void batch(@RequestBody com.tigshop.bean.dto.order.OrderBatchDTO dto) {
        if ("deliver".equals(dto.getType())) {
            try {
                for (Integer id : dto.getIds()) {
                    orderService.batchOperation(id, dto.getType(), dto.getData());
                }
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            }
        } else {
            throw new GlobalException("#type 错误");
        }
    }

    @GetMapping("/printSeveral")
    @Operation(summary = "批量打印")
    public List<OrderVO> printSeveral(@RequestParam List<Integer> ids) {
        return orderService.printSeveral(ids);
    }

    @GetMapping("/getExportItemList")
    @Operation(summary = "订单导出标签列表")
    public Map<String, String> getExportItemList() {
        return orderService.getExportItemList();
    }

    @GetMapping("/exportItemInfo")
    @Operation(summary = "标签详情")
    public Map<String, String> exportItemInfo() {
        return orderService.exportItemInfo();
    }

    @PostMapping("/saveExportItem")
    @Operation(summary = "保存导出标签信息")
    public void saveExportItem(@RequestBody @Validated ExportItemSaveParam param) {
        orderService.saveExportItem(param);
    }

    @GetMapping("/orderExport")
    @Operation(summary = "订单导出")
    public void exportItemInfo(HttpServletResponse response, @Validated OrderListPageQuery pageQuery) {
        orderService.orderExport(pageQuery, response);
    }

    @GetMapping("/shippingInfo")
    @Operation(summary = "物流信息")
    public ShippingResult shippingInfo(@RequestParam Integer id) {
        Integer userId = getCurrentUserId();
        return orderService.getShippingInfo(id,userId);
    }

    @Operation(summary = "获取订单列表配置")
    @GetMapping("/getOrderPageConfig")
    public OrderPageConfigVO getOrderPageConfig() {
        return orderService.getOrderPageConfig();
    }

    @Operation(summary = "修改订单状态")
    @PostMapping("/changeOrderStatus")
    public void changeOrderStatus(@RequestBody @Validated OrderChangeStatusParam param) {
        orderService.changeOrderStatus(param);
    }


    @Operation(summary = "催发货")
    @PostMapping("/remindDeliver")
    @PreAuthorize("@pms.hasPermission('remindDeliver')")
    public void remindDeliver(@RequestParam Integer id) {
        orderService.remindDeliver(id);
    }

    @Operation(summary = "后台取消订单")
    @PostMapping("/cancelOrderByAdmin")
    public void cancelOrderByAdmin(@RequestBody @Validated AftersalesApplyAdminParam param) {
        orderService.cancelOrderByAdmin(param);
    }

    @Operation(summary = "批量发货Excel导入")
    @PostMapping("/batchDeliverImport")
    @PreAuthorize("@pms.hasPermission('orderDeliverManage')")
    public OrderDeliverImportResultVO batchDeliverImport(@RequestParam("file") MultipartFile file) {
        return orderService.batchDeliverImport(file);
    }

    @Operation(summary = "获取批量发货导入失败的数据（导出错误Excel）")
    @PostMapping("/getBatchDeliverImportErrorData")
    @PreAuthorize("@pms.hasPermission('orderDeliverManage')")
    public void getBatchDeliverImportErrorData(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        orderService.getBatchDeliverImportErrorData(file, response);
    }

    @Operation(summary = "下载批量发货模板")
    @GetMapping("/downloadDeliverTemplate")
    public void downloadDeliverTemplate(@RequestParam List<Integer> ids, HttpServletResponse response) {
        orderService.downloadDeliverTemplate(ids, response);
    }

}
