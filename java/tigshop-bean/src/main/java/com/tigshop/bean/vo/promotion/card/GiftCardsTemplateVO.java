package com.tigshop.bean.vo.promotion.card;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop项目组
 * @create 2025年10月30日 15:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class GiftCardsTemplateVO {
    @ExcelProperty("卡号")
    @Schema(description = "卡号")
    private String cardNumber;

    @ExcelProperty("密码")
    @Schema(description = "密码")
    private String cardPassword;
}