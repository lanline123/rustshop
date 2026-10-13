// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.salesman.SalesmanOrderPageQuery;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.query.salesman.CustomerTransactionPageQuery;
import com.tigshop.bean.vo.salesman.CustomerTransactionOrderVO;
import com.tigshop.bean.vo.salesman.SalesmanOrderVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 分销订单服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SalesmanOrderService extends BaseService<SalesmanOrder> {

    /**
     * 列表
     */
    Page<SalesmanOrderVO> list(SalesmanOrderPageQuery pageQuery, HttpServletResponse response);


    /**
     * 新增分销订单
     *
     * @param orderItemList 订单项列表
     * @param salesmanOrder 分销订单
     */
    void addSalesmanOrder(List<OrderItem> orderItemList, Map<String, Integer> salesmanOrder) ;

    /**
     * 微商模式新增分销订单：佣金按「买家的上级链」归属，与推广链接无关。
     * <p>直接上级得一级佣金（rate），上级的上级得二级佣金（downSalesmanRate，
     * 仅 saleType 开启二级分销时），费率均按直接上级等级查表。</p>
     *
     * @param orderItemList 订单项列表
     * @param buyerUserId   买家用户ID
     */
    void addSalesmanOrderForWechatBiz(List<OrderItem> orderItemList, Integer buyerUserId);

    /**
     * 新增订单项
     *
     * @param item      订单项
     * @param salesmanId 分销商id
     */
    void addNewOrderItem(OrderItem item, Integer salesmanId, boolean isParent);

    /**
     * 成交客户列表
     * @param customerTransactionPageQuery 查询参数
     * @return Page
     */
    Page<CustomerTransactionOrderVO> listCustomerTransaction(CustomerTransactionPageQuery customerTransactionPageQuery);

    /**
     * 拆分分销订单（用于订单拆分场景，如部分发货、店铺拆单）
     * <p>
     * 当订单拆分时，同一个原 item_id 可能会被拆成多个新 item_id（如部分发货场景：3件发2件留1件）。
     * 此方法会按比例将原分销订单的佣金拆分到各个新订单中，确保总佣金不变。
     * </p>
     *
     * @param originalOrderVO      原订单信息
     * @param oldItemIds           原订单项ID列表
     * @param newOrderItemsMap     新订单ID -> 新订单项列表的映射
     */
    void splitSalesmanOrders(OrderVO originalOrderVO, List<Integer> oldItemIds, Map<Integer, List<OrderItem>> newOrderItemsMap);

    /**
     * 查询订单的分销佣金总额（排除已失效订单）
     *
     * @param orderId 订单ID
     * @return 佣金总额，无数据返回 BigDecimal.ZERO
     */
    BigDecimal getTotalCommissionByOrderId(Integer orderId);

    /**
     * 人工结算：将选中的分销订单记录标记为已结算。
     * <p>用于"线下人工结算"模式：管理员在后台手动结算。仅对"待结算(status=0)"记录生效，
     * 按记录所属订单去重后复用 {@link com.tigshop.service.order.OrderService#settlement}，传 settlementType=1，
     * 资金流转与自动结算完全一致（标记已结算 + 累加推广业绩额 + 发放佣金到佣金账户 + 触发升级判断）。</p>
     * <p>注意：结算后佣金即进入分销员佣金账户，此后订单退款不再回收（退款仅冲正"待结算"记录），
     * 与"自动立即结算"的亏损风险一致，需管理员在确认不再退款后操作。</p>
     *
     * @param salesmanOrderIds 分销订单记录ID列表
     */
    void manualSettle(List<Integer> salesmanOrderIds);

}
