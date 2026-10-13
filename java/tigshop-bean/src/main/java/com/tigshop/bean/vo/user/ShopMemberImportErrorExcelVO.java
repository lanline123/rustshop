package com.tigshop.bean.vo.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺会员导入失败Excel VO。
 *
 * <p>字段与 {@link com.tigshop.bean.dto.user.ShopMemberImportExcelDTO} 对齐（只到生日），末列附错误信息。
 */
@Data
@Schema(name = "店铺会员导入失败ExcelVO")
public class ShopMemberImportErrorExcelVO {

    @Schema(description = "用户名")
    @ExcelProperty(value = "用户名")
    @ColumnWidth(20)
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    @ExcelProperty(value = "手机号*")
    @ColumnWidth(20)
    private String mobile;

    @Schema(description = "邮箱")
    @ExcelProperty(value = "邮箱")
    @ColumnWidth(25)
    private String email;

    @Schema(description = "昵称")
    @ExcelProperty(value = "昵称")
    @ColumnWidth(20)
    private String nickname;

    @Schema(description = "生日")
    @ExcelProperty(value = "生日(格式:2025-01-01)")
    @ColumnWidth(25)
    private String birthday;

    @Schema(description = "错误信息")
    @ExcelProperty(value = "错误信息")
    @ColumnWidth(40)
    private String errorMsg;
}
