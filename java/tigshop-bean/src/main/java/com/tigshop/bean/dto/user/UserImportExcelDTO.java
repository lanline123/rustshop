package com.tigshop.bean.dto.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员导入Excel DTO
 *
 * @author Tigshop团队
 * @create 2025年12月01日
 */
@Data
@Schema(name = "会员导入Excel DTO")
public class UserImportExcelDTO {

    @Schema(description = "用户名")
    @ExcelProperty(value = "用户名*")
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

    @Schema(description = "密码")
    @ExcelProperty(value = "密码")
    @ColumnWidth(20)
    private String password;

    @Schema(description = "生日")
    @ExcelProperty(value = "生日(格式:2025-01-01)")
    @ColumnWidth(25)
    private String birthday;

    @Schema(description = "会员等级ID")
    @ExcelProperty(value = "会员等级ID")
    @ColumnWidth(20)
    private String rankId;

    @Schema(description = "余额")
    @ExcelProperty(value = "余额(增量)")
    @ColumnWidth(15)
    private String balance;

    @Schema(description = "积分")
    @ExcelProperty(value = "积分(增量)")
    @ColumnWidth(15)
    private String points;

    @Schema(description = "成长积分")
    @ExcelProperty(value = "成长积分(增量)")
    @ColumnWidth(15)
    private String growthPoints;

    @Schema(description = "备注")
    @ExcelProperty(value = "备注")
    @ColumnWidth(30)
    private String remark;
}

