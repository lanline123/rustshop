// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.renderer.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.order.PayStatusEnum;
import com.tigshop.bean.enums.order.OrderType;
import com.tigshop.bean.enums.order.OrderDeliveryTypeEnum;
import com.tigshop.bean.enums.order.PayMethodType;
import com.tigshop.bean.enums.order.PaymentType;
import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.print.PrintTemplate;
import com.tigshop.bean.param.print.PrintTemplateItem;
import com.tigshop.bean.param.print.PrintTemplateOption;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.FeieyunApiUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.print.renderer.ReceiptRenderer;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 飞鹅云小票渲染器
 * <p>
 * <b>strict_compat 守护</b>：本类的所有 {@code generate*} 私有方法来自原
 * {@code RabbitMqConsumer} 中对应私有方法的物理迁移，**逻辑零修改**。
 * 仅做位置迁移（类名包名变化）和依赖注入替换（从原 {@code RabbitMqConsumer} 的 @RequiredArgsConstructor 字段到本类的同类型字段）。
 * 任何逻辑修改都必须走 /change-request。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeieyunReceiptRenderer implements ReceiptRenderer {

    private final OrderService orderService;
    private final ConfigService configService;
    private final ShopService shopService;
    private final PaylogMapper paylogMapper;
    private final TigshopProperties tigshopProperties;

    @Override
    public Integer getPlatformCode() {
        return PrintPlatformEnum.FEIEYUN.getCode();
    }

    @Override
    public String render(Integer orderId, PrintTemplate template, PrintPaperSizeEnum paperSize) {
        PaperLayout layout = new PaperLayout(paperSize == null ? PrintPaperSizeEnum.SIZE_58 : paperSize);
        return generatePrintContent(orderId, template, layout);
    }

    // ===== 以下方法从 RabbitMqConsumer 原 private 方法物理迁移，逻辑零修改（2026-04-22 加 PaperLayout 参数） =====

    /**
     * 根据订单信息和打印模板生成打印内容。
     * <p>
     * <b>58mm strict_compat</b>：当 {@code layout.paper == SIZE_58} 时，分割线、列宽、表头均走
     * 原 {@link FeieyunApiUtils} 老方法字面值，输出与本次变更前**字节级一致**。
     */
    private String generatePrintContent(Integer orderId, PrintTemplate template, PaperLayout layout) {
        StringBuilder content = new StringBuilder();

        Order order = orderService.getById(orderId);
        OrderVO orderVO = orderService.detail(orderId, order.getUserId());

        // 遍历打印模板，根据配置生成内容
        for (PrintTemplateItem item : template) {
            String title = item.getTitle();
            List<PrintTemplateOption> options = item.getOptions();

            // 根据模板项标题生成对应内容
            switch (title) {
                case "小票头部" -> generateReceiptHeader(content, orderVO, options, layout);
                case "配送信息" -> generateDeliveryInfo(content, orderVO, options, layout);
                case "买家备注" -> generateBuyerNote(content, orderVO, options, layout);
                case "商品信息" -> generateProductInfo(content, orderVO, options, layout);
                case "运费信息" -> generateShippingInfo(content, orderVO, options, layout);
                case "优惠信息" -> generateDiscountInfo(content, orderVO, options, layout);
                case "支付信息" -> generatePaymentInfo(content, orderVO, options, layout);
                case "客户信息" -> generateCustomerInfo(content, orderVO, options, layout);
                case "其他订单信息" -> generateOtherOrderInfo(content, orderVO, options, layout);
                case "二维码" -> generateCodeInfo(content, orderVO, options, layout);
                case "底部公告" -> generateFooter(content, orderVO, options, layout);
            }
        }

        return content.toString();
    }

    // ===== Paper 布局（58mm/80mm）封装 =====

    /**
     * 纸张布局封装。
     * <p>
     * 对外暴露"分割线 / 三列商品行 / 两列键值行 / 多行左右行 / 商品表头"五类输出，每个方法内部按
     * 纸张尺寸选择老逻辑（{@code SIZE_58}：直接调 {@link FeieyunApiUtils} 历史老方法，字节级零变更）
     * 或新逻辑（{@code SIZE_80}：按 48 字符 / 44 multipart 宽度调用新重载）。
     */
    private static final class PaperLayout {
        private final PrintPaperSizeEnum paper;
        /** 分割线字符串：58mm=32 个 '-'，80mm=48 个 '-' */
        private final String divider;

        PaperLayout(PrintPaperSizeEnum paper) {
            this.paper = paper;
            this.divider = "-".repeat(paper.getCharsPerLine());
        }

        String divider() {
            return divider;
        }

        String formatLine(String price, int quantity, String subtotal) {
            return paper == PrintPaperSizeEnum.SIZE_58
                    ? FeieyunApiUtils.formatLine(price, quantity, subtotal)
                    : FeieyunApiUtils.formatLine(price, quantity, subtotal, paper.getCharsPerLine());
        }

        String formatLine(String left, String right) {
            return paper == PrintPaperSizeEnum.SIZE_58
                    ? FeieyunApiUtils.formatLine(left, right)
                    : FeieyunApiUtils.formatLine(left, right, paper.getCharsPerLine());
        }

        String formatMultpartLine(String left, String right) {
            return paper == PrintPaperSizeEnum.SIZE_58
                    ? FeieyunApiUtils.formatMultpartLine(left, right)
                    : FeieyunApiUtils.formatMultpartLine(left, right, paper.getMultipartRightWidth());
        }

        /** 商品表头：58mm 保留原字面量；80mm 按 16 列/段动态拼接 */
        String productHeader() {
            if (paper == PrintPaperSizeEnum.SIZE_58) {
                return "单价          数量          小计";
            }
            int col = paper.getCharsPerLine() / 3;
            return FeieyunApiUtils.padRightConsideringChinese("单价", col)
                    + FeieyunApiUtils.centerConsideringChinese("数量", col)
                    + FeieyunApiUtils.padLeftConsideringChinese("小计", col);
        }

        /**
         * 单行键值对：仅 80mm 做 label 左 / value 右撑满；58mm 保持历史
         * {@code append("标签：").append(value)} 行为，与升级前字节级一致。
         */
        String kvLine(String label, String value) {
            String safeValue = value == null ? "" : value;
            if (paper == PrintPaperSizeEnum.SIZE_58) {
                return label + safeValue;
            }
            int labelWidth = FeieyunApiUtils.getDisplayWidth(label);
            int valueWidth = FeieyunApiUtils.getDisplayWidth(safeValue);
            int gap = paper.getCharsPerLine() - labelWidth - valueWidth;
            if (gap <= 0) {
                return label + safeValue;
            }
            return label + " ".repeat(gap) + safeValue;
        }
    }

    /**
     * 生成小票头部
     */
    private void generateReceiptHeader(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String shopName = configService.getConfigVal(SettingsEnum.SHOP_NAME);
        if (PrintTemplateOption.isChoose(options, "商家名称")) {
            Shop shop = shopService.getById(orderVO.getShopId());
            if (shop != null) {
                orderVO.setShopName(shop.getShopTitle());
            }
            content.append("<C>").append(orderVO.getShopName() != null ? orderVO.getShopName() : shopName).append("</C><BR>");
        }
        if (PrintTemplateOption.isChoose(options, "门店名称")) {
            content.append("<CB>").append(orderVO.getShopName() != null ? orderVO.getShopName() : shopName).append("</CB><BR>");
        }
    }

    /**
     * 生成配送信息
     */
    private void generateDeliveryInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if(isPosOrder(orderVO)){
            return;
        }

        if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES)
                && orderVO.getOrderType() != null && OrderType.isReservationOrder(orderVO.getOrderType())) {
            generateReservationInfo(content, orderVO, options, layout);
            return;
        }
        if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && orderVO.getIsPickup()) {
            if (PrintTemplateOption.isChoose(options, "配送方式")) {
                content.append(layout.kvLine("配送方式：", "到店自提")).append("<BR>");
                content.append(layout.kvLine("自提时间：", StrUtil.isNotBlank(orderVO.getExpectPickupTime()) ? orderVO.getExpectPickupTime() : "")).append("<BR>");
            }
        } else if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && OrderDeliveryTypeEnum.SAME_CITY.getCode().equals(orderVO.getShippingTypeId())) {
            if (PrintTemplateOption.isChoose(options, "配送方式")) {
                content.append(layout.kvLine("配送方式：", "同城配送")).append("<BR>");
            }
        } else {
            if (PrintTemplateOption.isChoose(options, "配送方式")) {
                content.append(layout.kvLine("配送方式：", orderVO.getShippingTypeName() != null ? orderVO.getShippingTypeName() : "")).append("<BR>");
            }
        }

        if (PrintTemplateOption.isChoose(options, "商配&同城-收货信息")) {

            if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && orderVO.getIsPickup()) {
                if (PrintTemplateOption.isChoose(options, "手机号模糊")) {
                    content.append(layout.formatMultpartLine("自提信息：", orderVO.getUserPickupInfo().getConsignee() + "，" + StringUtils.maskMiddleHalf(orderVO.getUserPickupInfo().getMobile()) + "，" + orderVO.getPickupDetail().getAddress())).append("<BR>");
                } else {
                    content.append(layout.formatMultpartLine("自提信息：", orderVO.getUserPickupInfo().getConsignee() + "，" + orderVO.getUserPickupInfo().getMobile() + "，" + orderVO.getPickupDetail().getAddress())).append("<BR>");
                }
            } else if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && OrderDeliveryTypeEnum.SAME_CITY.getCode().equals(orderVO.getShippingTypeId())) {
                if (PrintTemplateOption.isChoose(options, "手机号模糊")) {
                    content.append(layout.formatMultpartLine("配送信息：", orderVO.getConsignee() + "，" + StringUtils.maskMiddleHalf(orderVO.getMobile()) + "，" + orderVO.getUserAddress())).append("<BR>");
                } else {
                    content.append(layout.formatMultpartLine("配送信息：", orderVO.getConsignee() + "，" + orderVO.getMobile() + "，" + orderVO.getUserAddress())).append("<BR>");
                }
            } else {
                if (PrintTemplateOption.isChoose(options, "手机号模糊")) {
                    content.append(layout.formatMultpartLine("收货信息：", orderVO.getConsignee() + "，" + StringUtils.maskMiddleHalf(orderVO.getMobile()) + "，" + orderVO.getUserAddress())).append("<BR>");
                } else {
                    content.append(layout.formatMultpartLine("收货信息：", orderVO.getConsignee() + "，" + orderVO.getMobile() + "，" + orderVO.getUserAddress())).append("<BR>");
                }
            }


        }
        if (PrintTemplateOption.isChooseAny(options, "配送方式", "同城配送-收货信息")) {
            content.append(layout.divider()).append("<BR>");
        }
    }

    /** 预约订单没有普通自提点，按订单类型读取预约信息，避免依赖 pickupDetail。 */
    private void generateReservationInfo(StringBuilder content, OrderVO orderVO,
                                         List<PrintTemplateOption> options, PaperLayout layout) {
        boolean atStore = Objects.equals(orderVO.getOrderType(), OrderType.RESERVATION_STORE.getCode());
        if (PrintTemplateOption.isChoose(options, "配送方式")) {
            content.append(layout.kvLine("配送方式：", atStore ? "预约到店" : "预约上门")).append("<BR>");
            var reservation = orderVO.getOrderReservation();
            String time = "";
            if (reservation != null) {
                String slot = StrUtil.blankToDefault(reservation.getTimeSlotDescription(), "");
                if (StrUtil.isBlank(slot)) {
                    slot = StrUtil.blankToDefault(reservation.getStartTime(), "")
                            + (StrUtil.isNotBlank(reservation.getEndTime()) ? "-" + reservation.getEndTime() : "");
                }
                time = (StrUtil.blankToDefault(reservation.getReservationDate(), "") + " " + slot).trim();
            }
            content.append(layout.kvLine("预约时间：", StrUtil.blankToDefault(time, "-"))).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "商配&同城-收货信息")) {
            var contact = orderVO.getUserPickupInfo();
            String consignee = atStore ? (contact == null ? "" : contact.getConsignee()) : orderVO.getConsignee();
            String mobile = atStore ? (contact == null ? "" : contact.getMobile()) : orderVO.getMobile();
            mobile = StrUtil.blankToDefault(mobile, "");
            if (PrintTemplateOption.isChoose(options, "手机号模糊")) {
                mobile = StringUtils.maskMiddleHalf(mobile);
            }
            String address = orderVO.getUserAddress();
            if (atStore) {
                Shop shop = orderVO.getShopId() == null ? null : shopService.getById(orderVO.getShopId());
                address = shop == null ? orderVO.getShopName()
                        : (StrUtil.blankToDefault(shop.getShopTitle(), "") + " "
                        + StrUtil.blankToDefault(shop.getShopDetailedAddress(), "")).trim();
            }
            content.append(layout.formatMultpartLine("预约信息：", StrUtil.blankToDefault(consignee, "-") + "，"
                    + StrUtil.blankToDefault(mobile, "-") + "，" + StrUtil.blankToDefault(address, "-"))).append("<BR>");
        }
        if (PrintTemplateOption.isChooseAny(options, "配送方式", "商配&同城-收货信息")) {
            content.append(layout.divider()).append("<BR>");
        }
    }

    /** 生成买家备注。 */
    private void generateBuyerNote(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "买家备注")) {
            content.append("<BOLD>").append(layout.formatMultpartLine("买家备注：", ObjectUtil.isEmpty(orderVO.getBuyerNote()) ? "无" : orderVO.getBuyerNote())).append("</BOLD>").append("<BR>");
            content.append(layout.divider()).append("<BR>");
        }
    }

    /**
     * 生成商品信息
     */
    private void generateProductInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        if (PrintTemplateOption.isChooseAny(options, "商品基础信息", "规格编码")) {
            content.append("商品<BR>");
            content.append(layout.productHeader()).append("<BR>");
            content.append(layout.divider()).append("<BR>");
        }
        if (orderVO.getItems() != null && !orderVO.getItems().isEmpty()) {
            for (OrderItemVO item : orderVO.getItems()) {
                if (PrintTemplateOption.isChoose(options, "商品基础信息")) {
                    content.append(item.getProductName());
                    if (ObjectUtil.isNotEmpty(item.getSkuValue())) {
                        content.append("（").append(item.getSkuValue()).append("）");
                    }
                    content.append("<BR>");
                }
                if (PrintTemplateOption.isChoose(options, "规格编码")) {
                    content.append("商品编码：" + item.getProductSn()).append("<BR>");
                }
                if (PrintTemplateOption.isChoose(options, "商品基础信息")) {
                    BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    content.append(layout.formatLine(configVal + item.getPrice().toPlainString(), item.getQuantity(), configVal + subtotal.toPlainString())).append("<BR>");
                }
            }
        }
        int sum = orderVO.getItems().stream().mapToInt(OrderItemVO::getQuantity).sum();
        if (PrintTemplateOption.isChooseAny(options, "商品基础信息", "规格编码")) {
            content.append(layout.divider()).append("<BR>");
            content.append(layout.formatLine("共" + sum + "件", "<BOLD>合计：" + configVal + orderVO.getProductAmount().toPlainString())).append("</BOLD><BR>");
            content.append(layout.divider()).append("<BR>");
        }
    }

    /**
     * 是否是pos订单
     * @param orderVO
     * @return
     */
    private boolean isPosOrder(OrderVO orderVO){
        return  "pos".equals(orderVO.getOrderSource() );
    }

    /**
     * 生成运费信息
     */
    private void generateShippingInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if(!isPosOrder(orderVO)){
            String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
            if (PrintTemplateOption.isChoose(options, "运费")) {
                content.append(layout.formatLine("运费", configVal + orderVO.getShippingFee().toPlainString())).append("<BR>");
                content.append(layout.divider()).append("<BR>");
            }
        }
    }

    /**
     * 生成优惠信息
     */
    private void generateDiscountInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        if (PrintTemplateOption.isChoose(options, "优惠明细")) {
            content.append(layout.formatLine("优惠", "优惠券：-" + configVal + orderVO.getCouponAmount().toPlainString())).append("<BR>");
            content.append(layout.formatLine("", "积分抵扣：-" + configVal + orderVO.getPointsAmount().toPlainString())).append("<BR>");
            content.append(layout.formatLine("", "活动优惠：-" + configVal + orderVO.getDiscountAmount().toPlainString())).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "优惠总计") && !PrintTemplateOption.isChoose(options, "优惠明细")) {
            if(isPosOrder(orderVO)){
                content.append(layout.formatLine("优惠", "<BOLD>优惠总计：-" + configVal + orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount()).toPlainString())).append("</BOLD><BR>");
            }else{
                content.append(layout.formatLine("优惠", "<BOLD>总计：-" + configVal + orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount()).toPlainString())).append("</BOLD><BR>");
            }
        }
        if (PrintTemplateOption.isChoose(options, "优惠总计") && PrintTemplateOption.isChoose(options, "优惠明细")) {
            if(isPosOrder(orderVO)){
                content.append(layout.formatLine("", "<BOLD>优惠总计：-" + configVal + orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount()).toPlainString())).append("</BOLD><BR>");
            }else{
                content.append(layout.formatLine("", "<BOLD>总计：-" + configVal + orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount()).toPlainString())).append("</BOLD><BR>");
            }
        }
        if (PrintTemplateOption.isChooseAny(options, "优惠明细", "优惠总计")) {
            content.append(layout.divider()).append("<BR>");
        }
    }

    /**
     * 生成支付信息
     */
    private void generatePaymentInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        //预售订单会有2个支付记录,所以这里每次都是拿最新的
        Paylog paylog = paylogMapper.selectOne(Wrappers.lambdaQuery(Paylog.class)
                .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                .eq(Paylog::getOrderId, orderVO.getOrderId())
                // 商品订单：orderType=0，避免撞充值单号
                .eq(Paylog::getOrderType, 0)
                //从大到小 (自增)
                .orderBy(true,false,Paylog::getPaylogId)
                .last(" limit 1")
                .select(Paylog::getPaySn, Paylog::getPayCode, Paylog::getTransactionId)
        );
        if (PrintTemplateOption.isChoose(options, "支付方式")) {
            if (paylog != null) {
                content.append(layout.kvLine("支付方式：", PayMethodType.getTypeName(paylog.getPayCode()))).append("<BR>");
            } else {
                if (orderVO.getPayTypeId() == PaymentType.OFFLINE.getCode()) {
                    content.append(layout.kvLine("支付方式：", "线下支付")).append("<BR>");
                }
                if (orderVO.getPayTypeId() == PaymentType.COD.getCode()) {
                    content.append(layout.kvLine("支付方式：", "货到付款")).append("<BR>");
                }
                if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    content.append(layout.kvLine("支付方式：", "余额支付")).append("<BR>");
                }
            }
        }
        if (PrintTemplateOption.isChoose(options, "实收金额")) {
            content.append(layout.kvLine("实收金额：", configVal + orderVO.getTotalAmount())).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "第三方支付单号")) {
            if (paylog != null) {
                content.append(layout.kvLine("第三方支付单号：", paylog.getTransactionId())).append("<BR>");
            } else {
                content.append(layout.kvLine("第三方支付单号：", "无")).append("<BR>");
            }
        }
        if (PrintTemplateOption.isChooseAny(options, "支付方式", "实收金额", "第三方支付单号")) {
            content.append(layout.divider()).append("<BR>");
        }
    }

    /**
     * 生成客户信息
     */
    private void generateCustomerInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "客户信息")) {
            content.append(layout.kvLine("客户姓名：", StringUtils.maskMiddleHalf(orderVO.getUser().getUsername()))).append("<BR>");
            content.append(layout.kvLine("手机号：", StringUtils.maskMiddleHalf(orderVO.getUser().getMobile()))).append("<BR>");
            content.append(layout.divider()).append("<BR>");
        }
    }

    /**
     * 生成其他订单信息
     */
    private void generateOtherOrderInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "下单时间")) {
            content.append(layout.kvLine("下单时间：", orderVO.getAddTime())).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "支付时间")) {
            content.append(layout.kvLine("支付时间：", ObjectUtil.isEmpty(orderVO.getPayTime()) ? "-" : orderVO.getPayTime())).append("<BR>");
        }
        if (PrintTemplateOption.isChoose(options, "渠道类型")) {
            content.append(layout.kvLine("渠道类型：", orderVO.getOrderSource())).append("<BR>");
        }
        if (PrintTemplateOption.isChooseAny(options, "下单时间", "支付时间", "渠道类型")) {
            content.append(layout.divider()).append("<BR>");
        }
    }

    /**
     * 订单二维码信息
     */
    private void generateCodeInfo(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "订单二维码")) {
            content.append("订单编号：").append(orderVO.getOrderSn()).append("<BR>");
            content.append("<C>").append("<BC128_C>").append(orderVO.getOrderSn()).append("</BC128_C>").append("</C>").append("<BR>");
        } else {
            content.append("<BR>");
            content.append("<BR>");
        }
    }

    /**
     * 生成底部信息
     */
    private void generateFooter(StringBuilder content, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "底部公告")) {
            content.append("<C>").append(options.getFirst().getValue()).append("</C><BR>");
        }
        content.append("<BR>");
        content.append("<BR>");
        content.append("<CUT>");
    }
}
