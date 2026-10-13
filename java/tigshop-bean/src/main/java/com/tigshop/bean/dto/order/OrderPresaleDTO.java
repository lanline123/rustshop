// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单预售配置DTO
 *
 * @author Tigshop团队
 * @create 2024年11月24日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单预售配置DTO")
public class OrderPresaleDTO implements Serializable {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "销售模式：1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "定金金额（定金预售）")
    private BigDecimal depositAmount;

    @Schema(description = "尾款金额（定金预售）")
    private BigDecimal finalPaymentAmount;

    @Schema(description = "发货天数（相对时间）")
    private Integer deliveryDays;

    @Schema(description = "发货日期（指定日期）")
    private Long deliveryDate;

    @Schema(description = "尾款相对天数（用于计算尾款开始支付时间）")
    private Integer finalPaymentRelativeDays;
}

