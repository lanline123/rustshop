// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import com.tigshop.bean.model.product.ProductPresale;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品预售信息VO（用于返回给前端，包含实时计算的定金金额）
 *
 * @author Tigshop团队
 * @create 2025年11月21日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品预售信息VO")
public class ProductPresaleVO {

    @Schema(description = "预售ID")
    private Integer presaleId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "销售模式：1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "销售模式文本")
    private String saleModeText;

    @Schema(description = "定金收取方式：1-按百分比，2-按固定金额")
    private Integer depositType;



    @Schema(description = "定金比例：1-99")
    private Integer depositRatio;

    @Schema(description = "是否向下取整：0-否，1-是")
    private Integer isRoundDown;

    @Schema(description = "固定定金金额")
    private BigDecimal depositFixedAmount;



    @Schema(description = "实时计算的定金金额（仅定金预售返回）")
    private BigDecimal depositAmount;


    @Schema(description = "尾款金额（定金预售）")
    private BigDecimal finalPaymentAmount;

    @Schema(description = "尾款支付时间类型：1-相对时间，2-固定时间")
    private Integer finalPaymentType;

    @Schema(description = "支付定金后N天开始支付尾款")
    private Integer finalPaymentRelativeDays;

    @Schema(description = "尾款支付期限-天")
    private Integer finalPaymentDurationDays;

    @Schema(description = "尾款支付期限-小时")
    private Integer finalPaymentDurationHours;

    @Schema(description = "尾款支付开始时间")
    private String finalPaymentStartTime;

    @Schema(description = "尾款支付结束时间")
    private String finalPaymentEndTime;

    @Schema(description = "尾款支付时间展示文案")
    private String finalPaymentStartTimeText;

    @Schema(description = "发货时间类型：1-相对时间，2-指定日期")
    private Integer deliveryType;

    @Schema(description = "支付全款后N天发货")
    private Integer deliveryRelativeDays;

    @Schema(description = "支付全款后N小时发货")
    private Integer deliveryRelativeHours;

    @Schema(description = "发货日期")
    private String deliveryDate;

    @Schema(description = "发货时间展示文案")
    private String deliveryTimeText;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    private ProductPresale presale;
}

