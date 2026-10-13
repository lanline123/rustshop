package com.tigshop.bean.bo.pay;

import com.tigshop.bean.enums.pay.WechatPayMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 微信支付下单形态上下文
 * <p>由 {@code WechatPayModeResolver} 在支付创建时一次性算出，随后驱动下单、paylog 建行、
 * 回调解析、退款与查单。{@code subOrders} 恒为非 null 列表，DIRECT / PARTNER 形态下为空列表。
 *
 * @author Tigshop团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayContext {

    /**
     * 下单形态
     */
    private WechatPayMode mode;

    /**
     * 合单商户单号，等于父单 paySn；仅 COMBINE 形态有值
     */
    private String combineOutTradeNo;

    /**
     * 单店服务商支付的二级商户号；仅 PARTNER 形态有值
     */
    private String subMchId;

    /**
     * 单店服务商支付的二级商户绑定 appId；仅 PARTNER 形态可能有值，允许为空
     */
    private String subAppId;

    /**
     * 子单明细；仅 COMBINE 形态非空，其余形态为空列表
     */
    private List<SubOrderCtx> subOrders = new ArrayList<>();

    /**
     * 整单是否存在分账对象，决定 PARTNER 形态是否下发 settle_info。
     * <p>平台自营（shopId=0）虽可进件为二级商户参与收款，却没有分账对象——
     * {@code ProfitSharingOrderServiceImpl} 对 shopId&lt;=0 直接跳过建单。此时若下发分账标识，
     * 资金会冻结在微信侧等待一笔永不发起的分账，拖慢自营资金到账。
     * <p>COMBINE 形态按子单各自的标识逐单下发，不看本字段。
     */
    private boolean profitSharing;

    public boolean isCombine() {
        return WechatPayMode.COMBINE == mode;
    }

    public boolean isPartner() {
        return WechatPayMode.PARTNER == mode;
    }

    public boolean isDirect() {
        return mode == null || WechatPayMode.DIRECT == mode;
    }

    public boolean hasSubOrders() {
        return subOrders != null && !subOrders.isEmpty();
    }

    public List<SubOrderCtx> getSubOrders() {
        return subOrders == null ? Collections.emptyList() : subOrders;
    }

    /**
     * 构造直连形态上下文（未开分账 / 未进件 / 无店铺归属的统一降级出口）
     */
    public static WechatPayContext direct() {
        WechatPayContext context = new WechatPayContext();
        context.setMode(WechatPayMode.DIRECT);
        context.setSubOrders(new ArrayList<>());
        return context;
    }

    /**
     * 构造单店服务商形态上下文，按店铺单口径带分账标识
     */
    public static WechatPayContext partner(String subMchId, String subAppId) {
        return partner(subMchId, subAppId, true);
    }

    /**
     * 构造单店服务商形态上下文
     *
     * @param profitSharing 是否存在分账对象，平台自营须传 false
     */
    public static WechatPayContext partner(String subMchId, String subAppId, boolean profitSharing) {
        WechatPayContext context = new WechatPayContext();
        context.setMode(WechatPayMode.PARTNER);
        context.setSubMchId(subMchId);
        context.setSubAppId(subAppId);
        context.setProfitSharing(profitSharing);
        context.setSubOrders(new ArrayList<>());
        return context;
    }

    /**
     * 构造合单形态上下文
     */
    public static WechatPayContext combine(String combineOutTradeNo, List<SubOrderCtx> subOrders) {
        WechatPayContext context = new WechatPayContext();
        context.setMode(WechatPayMode.COMBINE);
        context.setCombineOutTradeNo(combineOutTradeNo);
        context.setSubOrders(subOrders == null ? new ArrayList<>() : subOrders);
        return context;
    }

    /**
     * 合单子单明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubOrderCtx {

        /**
         * 子单 id
         */
        private Integer orderId;

        /**
         * 子单单号
         */
        private String orderSn;

        /**
         * 子单独立支付单号，作为微信合单请求的 {@code subOrders[i].outTradeNo}
         */
        private String paySn;

        /**
         * 子单所属店铺 id
         */
        private Integer shopId;

        /**
         * 子单所属店铺的二级商户号
         */
        private String subMchId;

        /**
         * 子单所属店铺的二级商户绑定 appId，允许为空
         */
        private String subAppId;

        /**
         * 子单待付金额，合计必须严格等于父单待付金额
         */
        private BigDecimal amount;

        /**
         * 该子单是否存在分账对象，决定合单请求里这一子单是否下发 settle_info；平台自营子单为 false
         */
        private boolean profitSharing;
    }
}
