package com.tigshop.bean.dto.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺会员导入Excel DTO。
 *
 * <p>与平台会员导入模板({@link UserImportExcelDTO})区分：店铺导入只需到「生日」为止的字段，
 * 不含会员等级/余额/积分/成长积分/备注。手机号必填，用户名留空自动生成。
 */
@Data
@Schema(name = "店铺会员导入Excel DTO")
public class ShopMemberImportExcelDTO {

    @Schema(description = "用户名")
    @ExcelProperty(value = "用户名")
    @ColumnWidth(20)
    private String username;

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
}
