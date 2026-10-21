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

package com.tigshop.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.dto.order.deliver.BatchDeliverData;
import com.tigshop.bean.dto.order.deliver.DeliverOrderParam;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.param.order.AftersalesApplyAdminParam;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.order.ExportItemSaveParam;
import com.tigshop.bean.param.order.OrderAdminNoteParam;
import com.tigshop.bean.param.order.OrderChangeStatusParam;
import org.springframework.web.multipart.MultipartFile;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.order.*;
import com.tigshop.bean.vo.order.OrderDeliverImportResultVO;
import com.tigshop.bean.vo.panel.SalesProductOrderItemVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderService extends BaseService<Order> {

    /**
     * 列表
     */
    Page<OrderVO> list(OrderListPageQuery pageQuery);


    /**
     *
     * @param pageQuery
     * @return
     */
    Page<OrderVO> list(OrderListPageQuery pageQuery,Integer size);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderVO detail(Integer id);

    /**
     * 前台订单详情
     */
    OrderVO detail(Integer id, Integer userId);

    /**
     * 订单数量
     *
     * @return OrderNumVO
     */
    OrderNumVO orderNum();

    /**
     * @param order
     * @param newItems
     * @param isSpiltStore
     * @param shopId
     * @return
     */
    Order createSplitOrder(OrderVO order, List<OrderItemVO> newItems, boolean isSpiltStore, Integer shopId, boolean isSingleShopSingleVendor);

    /**
     * 订单进度条
     *
     * @param orderVO 订单
     * @return OrderStepVO
     */
    OrderStepVO stepStatus(OrderVO orderVO);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderUpdateDTO updateDTO);

    List<OrderItemVO> getOrderItemList(List<Integer> orderIds);

    /**
     * 根据order数据生成可操作项
     *
     * @param orderVO 订单
     * @return OrderActionVO
     */
    OrderActionVO availableActions(OrderVO orderVO);

    /**
     * 设置管理员备注
     */
    void setAdminNote(@RequestBody OrderAdminNoteParam param);

    /**
     * 查询不同订单的状态数量
     *
     * @param userId 用户id
     * @param type   不同类型
     * @param status 不同状态
     * @return Long
     */
    Long getOrderStatusCount(int userId, String type, int status);

    boolean setOrderConfirm(Integer id);

    boolean splitStoreOrder(Integer id);

    boolean splitOrderAtSubmit(Integer id);

    Order setOrderPaid(Integer id);

    Order setOrderPaid(Integer id, String logDescription);


    /**
     * 前台取消订单
     *
     * @param id
     * @param userId
     * @return
     */
    boolean cancelOrder(Integer id, Integer userId, String description);

    boolean delOrder(Integer id);

    /**
     * 前台删除订单
     *
     * @param id
     * @param userId
     * @return
     */
    boolean delOrder(Integer id, Integer userId);

    boolean modifyOrderMoney(OrderModifyMoneyDTO feeData);

    boolean modifyOrderConsignee(OrderModifyConsigneeDTO modifyConsigneeDTO);

    boolean confirmReceipt(Integer orderId);

    /**
     * 前台确认收货
     *
     * @param id
     * @param userId
     * @return
     */
    boolean confirmReceipt(Integer id, Integer userId);

    /**
     * 次卡首次实际核销或过期时，完成主订单并按主订单金额触发结算。
     *
     * @param orderId 次卡主订单 ID
     * @return 本次是否成功取得完成触发权
     */
    boolean completeCycleCardParentOrder(Integer orderId);

    void settlement(Integer orderId, Integer settlementType);

    void vendorSettlement(Integer orderId, Integer vendorId, Integer shopId);

    boolean modifyOrderShipping(OrderModifyShippingDTO modifyShippingDTO);

    /**
     * 物流信息
     *
     * @param id
     * @param userId
     * @return
     */
    ShippingResult getShippingInfo(Integer id, Integer userId);

    void batchOperation(Integer id, String type, BatchDeliverData data);

    List<OrderVO> printSeveral(List<Integer> ids);

    void deliverOrder(OrderVO orderVO,DeliverOrderParam param);

    void deliverOrder(DeliverOrderParam param);

    /**
     * 支付提交后在独立事务中尝试自动发货虚拟类订单。
     *
     * @param orderId 真实订单 ID
     */
    void autoDeliverVirtualOrder(Integer orderId);

    OrderVO getOrderPrintInfo(Integer id);

    LogisticsApiLog getOrderPrintWaybillInfo(Integer id);

    List<OrderVO> getSeveralDetail(List<Integer> ids, Integer suppliersId);

    boolean modifyOrderProduct(OrderModifyProductDTO modifyProductDTO);

    BigDecimal getOrderUnSettlementAmount(Integer shopId);

    BigDecimal getOrderUnSettlementAmountByVendor(Integer vendorId);

    void batchSaveStatementOutputParam(Order order);

    BigDecimal getTodayOrderSettlementAmountByVendor(Integer vendorId, Long todayStart, Long todayEnd);

    Map<String, String> getExportItemList();

    /**
     * 保存导出标签信息
     */
    void saveExportItem(ExportItemSaveParam param);

    Map<String, String> exportItemInfo();

    /**
     * 订单导出
     */
    void orderExport(OrderListPageQuery pageQuery, HttpServletResponse response);

    Order updateOrderMoney(Order order, Boolean calculatePaymentAmount);

    void addLog(Integer orderId, String desc);

    /**
     * 售后列表
     */
    Page<AfterSalesClientVO> afterSalesList(AftersalesListPageQuery listDTO);

    /**
     * 售后详情
     */
    List<AfterSalesDetailVO> applyData(Integer itemId, Integer orderId);

    /**
     * 获取新订单数量
     */
    Long getNewOrderCount(String adminType, Integer shopId, Integer vendorId, Long startTime);

    /**
     * 获取已付款订单数量（payStatus = 已支付，按 payTime >= startTime 过滤）
     * <p>
     * 注：本计数用于语音播报的增量判断，必须单调递增，故不叠加 shippingStatus 过滤，
     * 否则发货后计数回落会导致后续新付款单漏播。
     *
     * @param adminType 管理员类型（区分供应商和店铺维度）
     * @param shopId    店铺 ID
     * @param vendorId  供应商 ID
     * @param startTime 起始时间（毫秒级 epoch）
     * @return 数量
     */
    Long getPaidOrderCount(String adminType, Integer shopId, Integer vendorId, Long startTime);

    /**
     * 获取已取消订单数量（orderStatus = 已取消，按 addTime >= startTime 过滤）
     * <p>
     * 注：orders 表无 cancelTime 字段，本方法按订单生成时间近似统计，
     * 不计入 startTime 之前下单但近期才被取消的订单。
     *
     * @param adminType 管理员类型（区分供应商和店铺维度）
     * @param shopId    店铺 ID
     * @param vendorId  供应商 ID
     * @param startTime 起始时间（毫秒级 epoch）
     * @return 数量
     */
    Long getCancelOrderCount(String adminType, Integer shopId, Integer vendorId, Long startTime);

    /**
     * 获取订单余额
     */
    BigDecimal getOrderBalance(String startTime, String endTime, String type);

    /**
     * 销售商品明细
     */
    Page<SalesProductOrderItemVO> getSalesProductDetail(SalesProductDetailPageQuery pageQuery);

    /**
     * 获取订单列表配置
     */
    OrderPageConfigVO getOrderPageConfig();

    /**
     * 统计各订单状态的订单数量（复用列表筛选条件，忽略 orderStatus 本身）
     */
    OrderStatusCountVO getStatusCount(OrderListPageQuery pageQuery);

    /**
     * 修改订单状态
     */
    void changeOrderStatus(OrderChangeStatusParam param);

    /**
     * 催发货
     */
    void remindDeliver(Integer id);

    /**
     * 管理员订单取消
     */
    void cancelOrderByAdmin(AftersalesApplyAdminParam param);

    /**
     * 批量发货Excel导入
     *
     * @param file Excel文件
     * @return 导入结果
     */
    OrderDeliverImportResultVO batchDeliverImport(MultipartFile file);

    /**
     * 获取批量发货导入失败的数据（导出错误Excel）
     * <p>
     * 注意：该接口仅做校验并导出失败行，不会执行发货。
     *
     * @param file     Excel文件
     * @param response 响应对象（返回Excel文件）
     */
    void getBatchDeliverImportErrorData(MultipartFile file, HttpServletResponse response);

    /**
     * 下载批量发货模板
     *
     * @param orderIds 订单ID列表
     * @param response 响应对象
     */
    void downloadDeliverTemplate(List<Integer> orderIds, HttpServletResponse response);

    /**
     * 为同城配送订单和预约上门订单创建派送订单（用于定时任务等无请求上下文的场景）
     *
     * @param order         子订单实体
     * @param parentOrderVO 父订单VO（用于获取地址等信息）
     */
    void createDeliveryOrderIfNeeded(Order order, OrderVO parentOrderVO);


    void createDeliveryOrderIfNeeded(OrderVO orderVO);

    /**
     * 订单完成后延迟赠送积分（由延迟队列消费者调用）
     * 仅当订单仍为已完成状态时执行赠送，若已退款/取消则不再赠送
     *
     * @param orderId 订单ID
     */
    void doOrderCompleteSendPoint(Integer orderId);

    /**
     * 预览订单可赠送的积分数量（不落库，仅计算）。
     * 与支付成功后实际赠送使用同一公式：getOrderSendPoint(orderMoney) × 会员等级 rankPoint，向上取整。
     *
     * @param orderMoney 计算基数（一般传订单总金额 totalAmount）
     * @param userId     用户ID（用于取会员等级 rankPoint）
     * @return 预计赠送积分数量，异常或无积分时返回 0
     */
    int previewSendPoints(BigDecimal orderMoney, Integer userId);
}
