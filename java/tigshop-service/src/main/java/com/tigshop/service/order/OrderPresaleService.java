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

import com.tigshop.bean.dto.order.OrderPresaleDTO;
import com.tigshop.bean.model.order.OrderPresale;
import com.tigshop.bean.vo.order.OrderPresaleVO;
import com.tigshop.service.common.BaseService;

/**
 * 订单预售服务接口
 *
 * @author Tigshop团队
 * @create 2024年11月24日
 */
public interface OrderPresaleService extends BaseService<OrderPresale> {

    /**
     * 创建预售订单信息
     * @param orderId 订单ID
     * @param orderSn 订单号
     * @param presaleDTO 预售配置
     * @return 是否成功
     */
    boolean createPresaleOrder(Integer orderId, String orderSn, OrderPresaleDTO presaleDTO);

    /**
     * 根据订单ID查询预售信息
     * @param orderId 订单ID
     * @return 预售信息
     */
    OrderPresale getByOrderId(Integer orderId);

    /**
     * 根据订单ID查询预售信息VO
     * @param orderId 订单ID
     * @return 预售信息VO
     */
    OrderPresaleVO getPresaleVOByOrderId(Integer orderId);

    /**
     * 支付定金成功后更新预售信息
     * @param orderId 订单ID
     * @param finalPaymentRelativeDays 尾款相对天数
     * @return 是否成功
     */
    boolean updateDepositPaid(Integer orderId, Integer finalPaymentRelativeDays);

    /**
     * 支付尾款成功后更新预售信息
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean updateFinalPaymentPaid(Integer orderId);

    /**
     * 检查是否可以支付尾款
     * @param orderId 订单ID
     * @return 是否可以支付
     */
    boolean canPayFinalPayment(Integer orderId);

    /**
     * 计算尾款开始支付时间
     * @param depositPaidTime 定金支付时间
     * @param finalPaymentRelativeDays 尾款相对天数
     * @return 尾款开始支付时间
     */
    Long calculateFinalPaymentStartTime(Long depositPaidTime, Integer finalPaymentRelativeDays);
}

