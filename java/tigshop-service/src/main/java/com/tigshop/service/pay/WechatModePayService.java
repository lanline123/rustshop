// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.pay;

import com.tigshop.bean.bo.pay.WechatPayContext;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;

import java.util.Map;

/**
 * 微信按下单形态支付入口。
 * <p>合单支付与服务商支付是微信独有概念，其余支付渠道没有对应语义，故单独成接口而不并入
 * {@link PayService}，避免通用支付抽象被微信细节污染，也避免其余渠道实现类被迫补空实现。
 *
 * @author Tigshop团队
 */
public interface WechatModePayService {

    /**
     * 按已判定的下单形态创建微信支付单。
     *
     * @param orderVO    订单信息，{@code paySn} 必须已由父单 paylog 写入
     * @param openid     支付者在平台 appId 下的 openid
     * @param clientType 客户端类型
     * @param context    形态上下文，为 null 或 DIRECT 时等价于 {@link PayService#pay}
     * @return 支付参数
     */
    PayCreateVO.PayInfo payByMode(OrderVO orderVO, String openid, String clientType, WechatPayContext context);

    /**
     * 合单支付回调：逐子单回写交易号后，按合单商户单号触发父单支付成功链路。
     *
     * @param map 微信回调报文
     * @return 微信要求的应答体
     */
    String combineNotify(Map<String, Object> map);

    /**
     * 普通服务商支付回调：整笔只有一个平台交易号，父单与其全部子单写同一个交易号。
     *
     * @param map 微信回调报文
     * @return 微信要求的应答体
     */
    String partnerNotify(Map<String, Object> map);

    /**
     * 普通服务商退款回调。
     *
     * @param map 微信回调报文
     * @return 微信要求的应答体
     */
    String partnerRefundNotify(Map<String, Object> map);
}
