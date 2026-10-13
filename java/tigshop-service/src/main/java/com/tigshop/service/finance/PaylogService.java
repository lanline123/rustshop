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

package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.bo.pay.WechatPayContext;
import com.tigshop.bean.dto.finance.PaylogCreateDTO;
import com.tigshop.bean.dto.finance.PaylogUpdateDTO;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.query.finance.PaylogListPageQuery;
import com.tigshop.bean.vo.finance.PaylogVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 交易日志服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PaylogService extends BaseService<Paylog> {
    /**
     * 列表
     */
    Page<PaylogVO> list(PaylogListPageQuery pageQuery);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    PaylogVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(PaylogCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(PaylogUpdateDTO updateDTO);

    Paylog creatPayLog(String originPaySn, OrderVO detail, String type, Integer orderType);

    Paylog creatPayLog(String originPaySn, OrderVO detail, String type, Integer orderType, BigDecimal payAmount);

    /**
     * 创建或复用支付日志，并写入本次支付尝试的分账渠道快照。
     *
     * @param profitSharingChannel WECHAT/HUIFU/ALIPAY；空表示本次请求未冻结分账资金
     */
    Paylog creatPayLog(String originPaySn, OrderVO detail, String type, Integer orderType,
                       BigDecimal payAmount, String profitSharingChannel);

    /**
     * 为待支付日志不可变地绑定分账渠道快照。
     */
    void bindProfitSharingChannel(String paySn, String channel);

    /**
     * 取该业务单据最新一条支付日志。
     * <p>Paylog 被商品订单、充值单与付费会员订单共用，靠 {@code orderType} 区分（0=商品订单/order 表，1=充值单/user_recharge_order 表，2=付费会员/premium_member_order 表），
     * 两张表主键各自独立，orderId 会撞号；必须同时限定 orderType，否则会串到另一业务类型的支付记录。</p>
     *
     * @param orderId   业务单据 ID（商品订单 ID 或充值单 ID，取决于 orderType）
     * @param orderType 0=商品订单，1=充值单，2=付费会员订单
     */
    Paylog getLastByOrderId(Integer orderId, Integer orderType);

    /**
     * 获取订单最新一条成功、正金额且带合法分账快照的商品支付证据。
     */
    Paylog getPaidProfitSharingPaylog(Integer orderId);

    /**
     * 为提交时已拆单的订单补建子单 paylog 行，并把生成的 paySn 回写到上下文。
     * <p>分账按子单 orderId 发起并从子单 paylog 取 transactionId，所以每个有效子单都需要独立一行。
     * 仅 COMBINE 与 PARTNER 形态生效，子行必须原子绑定 WECHAT 快照；DIRECT 形态不建行，
     * 保持既有落库结果不变。任一子行创建、复用或绑定失败时整批回滚并阻止网关外呼。
     *
     * @param context 微信支付形态上下文，方法会回写 {@code subOrders[i].paySn} 与 {@code combineOutTradeNo}
     * @param parent  父单详情，已完成父单 paylog 创建
     * @param payCode 支付方式编码，与父单 paylog 一致
     */
    void createSplitPaylogs(WechatPayContext context, OrderVO parent, String payCode);

    /**
     * COMBINE 形态回调：按回调回传的「子单商户单号 → 交易号」为父单下全部有效子单写号。
     * <p>已有交易号的行整行跳过，重复投递不会覆盖首次落库结果。
     *
     * @param parentPaySn          合单商户单号（父单 paySn）
     * @param payCode              支付方式编码
     * @param transactionIdByPaySn 回调回传的子单商户单号到交易号映射
     * @return 仍未拿到交易号的有效子单数；&gt;0 表示这些子单无法发起分账，调用方须告警
     */
    int fillCombineChildTransactionIds(String parentPaySn, String payCode, Map<String, String> transactionIdByPaySn);

    /**
     * PARTNER 形态回调：整笔支付只进了一个二级商户，父单下全部有效子单写同一个交易号。
     *
     * @param parentPaySn   父单商户单号
     * @param payCode       支付方式编码
     * @param transactionId 微信交易号
     * @return 仍未拿到交易号的有效子单数
     */
    int fillPartnerChildTransactionIds(String parentPaySn, String payCode, String transactionId);
}
