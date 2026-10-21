package com.tigshop.bean.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 电子卡券组导出VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "电子卡券组导出VO")
public class ECardExportVO {
    @ExcelProperty(value = "卡券名称", index = 0)
    @Schema(description = "卡券名称")
    private String cardNumber;

    @ExcelProperty(value = "卡号密码", index = 1)
    @Schema(description = "卡号密码")
    private String cardPwd;
}
