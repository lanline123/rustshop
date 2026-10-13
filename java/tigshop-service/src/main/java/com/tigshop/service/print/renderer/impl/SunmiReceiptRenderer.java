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
import com.tigshop.common.utils.FeieyunApiUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 商米云小票渲染器
 * <p>
 * 输出格式：UTF-8 文本转十六进制 + 混合 ESC/POS 控制字节。
 * 参考：<a href="https://docs.sunmi.com/zh-CN/cdixeghjk491/xffzeghjk557">7、云打印ESC/POS指令集</a>。
 * <p>
 * <b>设计取舍</b>：MVP 阶段采用与 {@link FeieyunReceiptRenderer} 对等的模板遍历结构
 * （基于 {@code PrintTemplate} 的相同项目清单），但输出协议不同。这样行为与飞鹅云在
 * 内容组织上保持一致（REQ-006：共用模板），降低商家在两平台切换时的认知成本。
 * <p>
 * <b>元素支持度</b>：
 * <ul>
 *     <li>文本 / 分割线 / 对齐 / 加粗 / 切纸：完整支持</li>
 *     <li>条码（Code128）：完整支持（GS k 49 指令）</li>
 *     <li>二维码 / Logo 图：MVP 不实现，降级为纯文本"订单编号"（见 TODO）</li>
 * </ul>
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SunmiReceiptRenderer implements ReceiptRenderer {

    // ===== ESC/POS 控制字节 =====

    /** ESC @ 初始化打印机 */
    private static final byte[] INIT = {0x1B, 0x40};
    /** ESC a 0 左对齐 */
    private static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    /** ESC a 1 居中 */
    private static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    /** ESC E 1 加粗开 */
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    /** ESC E 0 加粗关 */
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    /** LF 换行 */
    private static final byte LF = 0x0A;
    /** GS V 0 全切 */
    private static final byte[] CUT_FULL = {0x1D, 0x56, 0x00};

    private final OrderService orderService;
    private final ConfigService configService;
    private final ShopService shopService;
    private final PaylogMapper paylogMapper;
    private final TigshopProperties tigshopProperties;

    @Override
    public Integer getPlatformCode() {
        return PrintPlatformEnum.SUNMI.getCode();
    }

    @Override
    public String render(Integer orderId, PrintTemplate template, PrintPaperSizeEnum paperSize) {
        PaperLayout layout = new PaperLayout(paperSize == null ? PrintPaperSizeEnum.SIZE_58 : paperSize);
        SunmiContentBuilder sb = new SunmiContentBuilder();
        sb.write(INIT);
        sb.alignLeft();

        Order order = orderService.getById(orderId);
        OrderVO orderVO = orderService.detail(orderId, order.getUserId());

        for (PrintTemplateItem item : template) {
            String title = item.getTitle();
            List<PrintTemplateOption> options = item.getOptions();
            switch (title) {
                case "小票头部" -> generateReceiptHeader(sb, orderVO, options, layout);
                case "配送信息" -> generateDeliveryInfo(sb, orderVO, options, layout);
                case "买家备注" -> generateBuyerNote(sb, orderVO, options, layout);
                case "商品信" -> generateProductInfo(sb, orderVO, options, layout);
                case "运费信息" -> generateShippingInfo(sb, orderVO, options, layout);
                case "优惠信息" -> generateDiscountInfo(sb, orderVO, options, layout);
                case "支付信息" -> generatePaymentInfo(sb, orderVO, options, layout);
                case "客户信息" -> generateCustomerInfo(sb, orderVO, options, layout);
                case "其他订单信息" -> generateOtherOrderInfo(sb, orderVO, options, layout);
                case "二维码" -> generateCodeInfo(sb, orderVO, options, layout);
                case "底部公告" -> generateFooter(sb, orderVO, options, layout);
                default -> log.warn("商米渲染器遇到未知模板项：{}", title);
            }
        }

        sb.lf();
        sb.lf();
        sb.write(CUT_FULL);
        return sb.toHex();
    }

    // ===== Paper 布局（58mm/80mm）封装 =====

    /**
     * 纸张布局封装（与 {@code FeieyunReceiptRenderer.PaperLayout} 同构）。
     * <p>
     * 58mm 时 divider / header / 价格行维持历史 32 字符宽度；80mm 时按 48 字符宽度重新排版。
     * 原因：ESC/POS 字体 1 在 58mm 打印机上物理可印 32 字符/行，在 80mm 打印机上物理可印 48 字符/行。
     */
    private static final class PaperLayout {
        private final PrintPaperSizeEnum paper;
        private final String divider;

        PaperLayout(PrintPaperSizeEnum paper) {
            this.paper = paper;
            this.divider = "-".repeat(paper.getCharsPerLine());
        }

        boolean is58() {
            return paper == PrintPaperSizeEnum.SIZE_58;
        }

        String divider() {
            return divider;
        }

        /** 商品表头：与飞鹅保持一致 */
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
         * 三列商品价格行：价格左对齐 / 数量居中 / 小计右对齐。
         * <p>
         * 使用 display-width 感知方法（汉字/全角符号按 2 宽计），避免 ￥（U+FFE5）
         * 被 {@code String.format} 误算为 1 宽导致 80mm 溢出换行。
         */
        String priceLine(String price, int quantity, String subtotal) {
            int col = paper.getCharsPerLine() / 3;
            return FeieyunApiUtils.padRightConsideringChinese(price, col)
                    + FeieyunApiUtils.centerConsideringChinese(String.valueOf(quantity), col)
                    + FeieyunApiUtils.padLeftConsideringChinese(subtotal, col);
        }

        /** 运费行：左对齐标签 + 右对齐数字 */
        String kv(String left, String right) {
            return paper == PrintPaperSizeEnum.SIZE_58
                    ? FeieyunApiUtils.formatLine(left, right)
                    : FeieyunApiUtils.formatLine(left, right, paper.getCharsPerLine());
        }

        /**
         * 单行键值对：仅 80mm 做 label 左 / value 右撑满；58mm 保持原
         * {@code "标签：" + value} 直接拼接。
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

    // ===== 模板块渲染（与 FeieyunReceiptRenderer 模板项选项保持一致，但输出 ESC/POS） =====

    private void generateReceiptHeader(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String shopName = configService.getConfigVal(SettingsEnum.SHOP_NAME);
        if (PrintTemplateOption.isChoose(options, "商家名称")) {
            Shop shop = shopService.getById(orderVO.getShopId());
            if (shop != null) {
                orderVO.setShopName(shop.getShopTitle());
            }
            sb.alignCenter().textLine(orderVO.getShopName() != null ? orderVO.getShopName() : shopName).alignLeft();
        }
        if (PrintTemplateOption.isChoose(options, "门店名称")) {
            sb.alignCenter().bold(orderVO.getShopName() != null ? orderVO.getShopName() : shopName).lf().alignLeft();
        }
    }

    private void generateDeliveryInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (isPosOrder(orderVO)) {
            return;
        }

        if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES)
                && orderVO.getOrderType() != null && OrderType.isReservationOrder(orderVO.getOrderType())) {
            generateReservationInfo(sb, orderVO, options, layout);
            return;
        }
        if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && orderVO.getIsPickup()) {
            if (PrintTemplateOption.isChoose(options, "配送方式")) {
                sb.textLine(layout.kvLine("配送方式：", "到店自提"));
                sb.textLine(layout.kvLine("自提时间：", StrUtil.isNotBlank(orderVO.getExpectPickupTime()) ? orderVO.getExpectPickupTime() : ""));
            }
        } else if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && OrderDeliveryTypeEnum.SAME_CITY.getCode().equals(orderVO.getShippingTypeId())) {
            if (PrintTemplateOption.isChoose(options, "配送方式")) {
                sb.textLine(layout.kvLine("配送方式：", "同城配送"));
            }
        } else {
            if (PrintTemplateOption.isChoose(options, "配送方式")) {
                sb.textLine(layout.kvLine("配送方式：", orderVO.getShippingTypeName() != null ? orderVO.getShippingTypeName() : ""));
            }
        }

        if (PrintTemplateOption.isChoose(options, "商配&同城-收货信息")) {
            String consignee;
            String mobile;
            String address;
            String label;
            if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && orderVO.getIsPickup()) {
                consignee = orderVO.getUserPickupInfo().getConsignee();
                mobile = orderVO.getUserPickupInfo().getMobile();
                address = orderVO.getPickupDetail().getAddress();
                label = "自提信息：";
            } else if (Objects.equals(tigshopProperties.getIsO2o(), Constants.YES) && OrderDeliveryTypeEnum.SAME_CITY.getCode().equals(orderVO.getShippingTypeId())) {
                consignee = orderVO.getConsignee();
                mobile = orderVO.getMobile();
                address = orderVO.getUserAddress();
                label = "配送信息：";
            } else {
                consignee = orderVO.getConsignee();
                mobile = orderVO.getMobile();
                address = orderVO.getUserAddress();
                label = "收货信息：";
            }
            if (PrintTemplateOption.isChoose(options, "手机号模糊")) {
                mobile = StringUtils.maskMiddleHalf(mobile);
            }
            sb.textLine(label + consignee + "，" + mobile + "，" + address);
        }
        if (PrintTemplateOption.isChooseAny(options, "配送方式", "同城配送-收货信息")) {
            sb.textLine(layout.divider());
        }
    }

    /** 预约订单没有普通自提点，按订单类型读取预约信息，避免依赖 pickupDetail。 */
    private void generateReservationInfo(SunmiContentBuilder sb, OrderVO orderVO,
                                         List<PrintTemplateOption> options, PaperLayout layout) {
        boolean atStore = Objects.equals(orderVO.getOrderType(), OrderType.RESERVATION_STORE.getCode());
        if (PrintTemplateOption.isChoose(options, "配送方式")) {
            sb.textLine(layout.kvLine("配送方式：", atStore ? "预约到店" : "预约上门"));
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
            sb.textLine(layout.kvLine("预约时间：", StrUtil.blankToDefault(time, "-")));
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
            sb.textLine("预约信息：" + StrUtil.blankToDefault(consignee, "-") + "，"
                    + StrUtil.blankToDefault(mobile, "-") + "，" + StrUtil.blankToDefault(address, "-"));
        }
        if (PrintTemplateOption.isChooseAny(options, "配送方式", "商配&同城-收货信息")) {
            sb.textLine(layout.divider());
        }
    }

    private void generateBuyerNote(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "买家备注")) {
            String note = ObjectUtil.isEmpty(orderVO.getBuyerNote()) ? "无" : orderVO.getBuyerNote();
            sb.bold("买家备注：" + note).lf();
            sb.textLine(layout.divider());
        }
    }

    private void generateProductInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        if (PrintTemplateOption.isChooseAny(options, "商品基础信息", "规格编码")) {
            sb.textLine("商品");
            sb.textLine(layout.productHeader());
            sb.textLine(layout.divider());
        }
        if (orderVO.getItems() != null && !orderVO.getItems().isEmpty()) {
            for (OrderItemVO item : orderVO.getItems()) {
                if (PrintTemplateOption.isChoose(options, "商品基础信息")) {
                    StringBuilder line = new StringBuilder(item.getProductName());
                    if (ObjectUtil.isNotEmpty(item.getSkuValue())) {
                        line.append("（").append(item.getSkuValue()).append("）");
                    }
                    sb.textLine(line.toString());
                }
                if (PrintTemplateOption.isChoose(options, "规格编码")) {
                    sb.textLine("商品编码：" + item.getProductSn());
                }
                if (PrintTemplateOption.isChoose(options, "商品基础信息")) {
                    BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    sb.textLine(layout.priceLine(configVal + item.getPrice().toPlainString(),
                            item.getQuantity(),
                            configVal + subtotal.toPlainString()));
                }
            }
        }
        int sum = orderVO.getItems().stream().mapToInt(OrderItemVO::getQuantity).sum();
        if (PrintTemplateOption.isChooseAny(options, "商品基础信息", "规格编码")) {
            sb.textLine(layout.divider());
            // 58mm 保留历史 6 空格写法（绝对不动）；80mm 按纸宽 48 列动态算 gap，
            // 让 "合计：xxx" 严格右对齐到行末。
            String left = "共" + sum + "件";
            String right = "合计：" + configVal + orderVO.getProductAmount().toPlainString();
            int gap;
            if (layout.is58()) {
                gap = 6;
            } else {
                gap = 48 - FeieyunApiUtils.getDisplayWidth(left) - FeieyunApiUtils.getDisplayWidth(right);
                if (gap < 1) {
                    gap = 1; // 极端长价格兜底，至少留 1 空格分隔
                }
            }
            // bold 仅包文字段，中间空格走普通模式：避免 bold 双重打印导致字宽轻微外扩
            // 在 80mm 边界（48 列）触发折行+行高增加。
            sb.bold(left).spaces(gap).bold(right).lf();
            sb.textLine(layout.divider());
        }
    }

    private boolean isPosOrder(OrderVO orderVO) {
        return "pos".equals(orderVO.getOrderSource());
    }

    private void generateShippingInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (!isPosOrder(orderVO)) {
            String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
            if (PrintTemplateOption.isChoose(options, "运费")) {
                // 58mm 保留原字面量（2 中文 + 10 空格 = 14 宽）；80mm 用 kv 两列对齐
                if (layout.is58()) {
                    sb.textLine("运费          " + configVal + orderVO.getShippingFee().toPlainString());
                } else {
                    sb.textLine(layout.kv("运费", configVal + orderVO.getShippingFee().toPlainString()));
                }
                sb.textLine(layout.divider());
            }
        }
    }

    private void generateDiscountInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        // 58mm 直接走历史字面量（绝对不变）；80mm 走 layout.kv 右对齐撑满
        if (layout.is58()) {
            if (PrintTemplateOption.isChoose(options, "优惠明细")) {
                sb.textLine("优惠  优惠券：-" + configVal + orderVO.getCouponAmount().toPlainString());
                sb.textLine("      积分抵扣：-" + configVal + orderVO.getPointsAmount().toPlainString());
                sb.textLine("      活动优惠：-" + configVal + orderVO.getDiscountAmount().toPlainString());
            }
            BigDecimal total58 = orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount());
            if (PrintTemplateOption.isChoose(options, "优惠总计") && !PrintTemplateOption.isChoose(options, "优惠明细")) {
                String label = isPosOrder(orderVO) ? "优惠总计：-" : "总计：-";
                sb.text("优惠  ").bold(label + configVal + total58.toPlainString()).lf();
            }
            if (PrintTemplateOption.isChoose(options, "优惠总计") && PrintTemplateOption.isChoose(options, "优惠明细")) {
                String label = isPosOrder(orderVO) ? "优惠总计：-" : "总计：-";
                sb.text("      ").bold(label + configVal + total58.toPlainString()).lf();
            }
            if (PrintTemplateOption.isChooseAny(options, "优惠明细", "优惠总计")) {
                sb.textLine(layout.divider());
            }
            return;
        }
        // 80mm 分支：与飞鹅同构，用 layout.kv（左标签 + 右值撑满 48 列）
        if (PrintTemplateOption.isChoose(options, "优惠明细")) {
            sb.textLine(layout.kv("优惠", "优惠券：-" + configVal + orderVO.getCouponAmount().toPlainString()));
            sb.textLine(layout.kv("", "积分抵扣：-" + configVal + orderVO.getPointsAmount().toPlainString()));
            sb.textLine(layout.kv("", "活动优惠：-" + configVal + orderVO.getDiscountAmount().toPlainString()));
        }
        BigDecimal total = orderVO.getCouponAmount().add(orderVO.getPointsAmount()).add(orderVO.getDiscountAmount());
        // 总计行 80mm 风格：text(左标签 + 中间空格) + bold(值)，整行右对齐撑满 48 列
        if (PrintTemplateOption.isChoose(options, "优惠总计") && !PrintTemplateOption.isChoose(options, "优惠明细")) {
            String label = isPosOrder(orderVO) ? "优惠总计：-" : "总计：-";
            String right = label + configVal + total.toPlainString();
            int gap = 48 - FeieyunApiUtils.getDisplayWidth("优惠") - FeieyunApiUtils.getDisplayWidth(right);
            // bold 仅包文字段，中间空格走普通模式
            sb.bold("优惠").spaces(Math.max(0, gap)).bold(right).lf();
        }
        if (PrintTemplateOption.isChoose(options, "优惠总计") && PrintTemplateOption.isChoose(options, "优惠明细")) {
            String label = isPosOrder(orderVO) ? "优惠总计：-" : "总计：-";
            String right = label + configVal + total.toPlainString();
            int gap = 48 - FeieyunApiUtils.getDisplayWidth(right);
            sb.spaces(Math.max(0, gap)).bold(right).lf();
        }
        if (PrintTemplateOption.isChooseAny(options, "优惠明细", "优惠总计")) {
            sb.textLine(layout.divider());
        }
    }

    private void generatePaymentInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        String configVal = configService.getConfigVal(SettingsEnum.DOLLAR_SIGN).replace("¥", "￥");
        Paylog paylog = paylogMapper.selectOne(Wrappers.lambdaQuery(Paylog.class)
                .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                .eq(Paylog::getOrderId, orderVO.getOrderId())
                // 商品订单：orderType=0，避免撞充值单号
                .eq(Paylog::getOrderType, 0)
                .orderBy(true, false, Paylog::getPaylogId)
                .last(" limit 1")
                .select(Paylog::getPaySn, Paylog::getPayCode, Paylog::getTransactionId));
        if (PrintTemplateOption.isChoose(options, "支付方式")) {
            if (paylog != null) {
                sb.textLine(layout.kvLine("支付方式：", PayMethodType.getTypeName(paylog.getPayCode())));
            } else {
                if (orderVO.getPayTypeId() == PaymentType.OFFLINE.getCode()) {
                    sb.textLine(layout.kvLine("支付方式：", "线下支付"));
                }
                if (orderVO.getPayTypeId() == PaymentType.COD.getCode()) {
                    sb.textLine(layout.kvLine("支付方式：", "货到付款"));
                }
                if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    sb.textLine(layout.kvLine("支付方式：", "余额支付"));
                }
            }
        }
        if (PrintTemplateOption.isChoose(options, "实收金额")) {
            sb.textLine(layout.kvLine("实收金额：", configVal + orderVO.getTotalAmount()));
        }
        if (PrintTemplateOption.isChoose(options, "第三方支付单号")) {
            sb.textLine(layout.kvLine("第三方支付单号：", paylog != null ? paylog.getTransactionId() : "无"));
        }
        if (PrintTemplateOption.isChooseAny(options, "支付方式", "实收金额", "第三方支付单号")) {
            sb.textLine(layout.divider());
        }
    }

    private void generateCustomerInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "客户信息")) {
            sb.textLine(layout.kvLine("客户姓名：", StringUtils.maskMiddleHalf(orderVO.getUser().getUsername())));
            sb.textLine(layout.kvLine("手机号：", StringUtils.maskMiddleHalf(orderVO.getUser().getMobile())));
            sb.textLine(layout.divider());
        }
    }

    private void generateOtherOrderInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "下单时间")) {
            sb.textLine(layout.kvLine("下单时间：", orderVO.getAddTime()));
        }
        if (PrintTemplateOption.isChoose(options, "支付时间")) {
            sb.textLine(layout.kvLine("支付时间：", ObjectUtil.isEmpty(orderVO.getPayTime()) ? "-" : orderVO.getPayTime()));
        }
        if (PrintTemplateOption.isChoose(options, "渠道类型")) {
            sb.textLine(layout.kvLine("渠道类型：", orderVO.getOrderSource()));
        }
        if (PrintTemplateOption.isChooseAny(options, "下单时间", "支付时间", "渠道类型")) {
            sb.textLine(layout.divider());
        }
    }

    private void generateCodeInfo(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "订单二维码")) {
            sb.textLine("订单编号：" + orderVO.getOrderSn());
            // Code128 条码：居中显示订单号
            sb.alignCenter();
            sb.barcode128(orderVO.getOrderSn());
            sb.alignLeft();
            sb.lf();
        } else {
            sb.lf();
            sb.lf();
        }
    }

    private void generateFooter(SunmiContentBuilder sb, OrderVO orderVO, List<PrintTemplateOption> options, PaperLayout layout) {
        if (PrintTemplateOption.isChoose(options, "底部公告")) {
            sb.alignCenter().textLine(options.getFirst().getValue()).alignLeft();
        }
    }

    // ===== ESC/POS hex 拼装辅助 =====

    /**
     * 将文本 + 控制字节累积为 byte[]，最终输出为 hex 字符串
     */
    static final class SunmiContentBuilder {
        private final java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();

        SunmiContentBuilder write(byte[] bytes) {
            try {
                buf.write(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        SunmiContentBuilder text(String s) {
            if (s == null || s.isEmpty()) {
                return this;
            }
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            try {
                buf.write(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        SunmiContentBuilder textLine(String s) {
            return text(s).lf();
        }

        SunmiContentBuilder bold(String s) {
            return write(BOLD_ON).text(s).write(BOLD_OFF);
        }

        SunmiContentBuilder spaces(int n) {
            return text(" ".repeat(Math.max(0, n)));
        }

        SunmiContentBuilder lf() {
            buf.write(LF);
            return this;
        }

        SunmiContentBuilder alignLeft() {
            return write(ALIGN_LEFT);
        }

        SunmiContentBuilder alignCenter() {
            return write(ALIGN_CENTER);
        }

        /**
         * 打印 Code128 条码。
         * 指令格式：{@code GS k 49 n d1..dn}，n 为数据长度。
         * 配套：GS H（HRI 下方显示）、GS h（高度）、GS w（模块宽度）
         */
        SunmiContentBuilder barcode128(String data) {
            if (data == null || data.isEmpty()) {
                return this;
            }
            byte[] payload = data.getBytes(StandardCharsets.US_ASCII);
            // GS H 02 : HRI 打印在条码下方
            write(new byte[]{0x1D, 0x48, 0x02});
            // GS h 80 : 条码高度 80 点
            write(new byte[]{0x1D, 0x68, (byte) 80});
            // GS w 02 : 模块宽度 2
            write(new byte[]{0x1D, 0x77, 0x02});
            // GS k 49 n d1..dn
            write(new byte[]{0x1D, 0x6B, 0x49, (byte) payload.length});
            write(payload);
            lf();
            return this;
        }

        String toHex() {
            byte[] bytes = buf.toByteArray();
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b & 0xFF));
            }
            return hex.toString();
        }
    }
}
