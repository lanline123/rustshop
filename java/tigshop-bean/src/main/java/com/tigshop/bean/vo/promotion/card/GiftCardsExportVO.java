package com.tigshop.bean.vo.promotion.card;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop项目组
 * @create 2025年10月30日 15:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class GiftCardsExportVO {
    // 卡号、密码 是否售出 是否兑换 创建时间
    @ExcelProperty("卡号")
    @Schema(description = "卡号")
    private String cardNumber;

    @ExcelProperty("密码")
    @Schema(description = "密码")
    private String cardPassword;

    @ExcelProperty("卡唯一识别码")
    private String giftCardSn;

    @ExcelProperty("是否售出")
    @Schema(description = "是否售出")
    private String isSold;

    @ExcelProperty("是否兑换")
    @Schema(description = "是否兑换")
    private String isExchange;

    @ExcelProperty("创建时间")
    @Schema(description = "创建时间")
    private String addTime;
}