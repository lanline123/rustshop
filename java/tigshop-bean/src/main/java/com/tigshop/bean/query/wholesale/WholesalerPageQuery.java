package com.tigshop.bean.query.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商分页查询入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商分页查询入参")
public class WholesalerPageQuery {

    @Schema(description = "页码，默认1", example = "1")
    private Integer page;

    @Schema(description = "每页条数，默认20，1-100", example = "20")
    private Integer size;

    @Schema(description = "联合模糊：username + mobile + email + nickname（UI 提示：用户名/手机号/邮箱）")
    private String keyword;

    @Schema(description = "批发等级筛选")
    private Integer gradeId;

    @Schema(description = "注册渠道筛选（user.fromTag：1=公众号 2=小程序 3=H5 4=PC 5=APP）")
    private Integer fromTag;

    @Schema(description = "批发商启停：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "批发商来源：1=注册自动 2=后台导入 3=手动添加")
    private Integer source;

    @Schema(description = "添加时间起 yyyy-MM-dd HH:mm:ss")
    private String addTimeStart;

    @Schema(description = "添加时间止 yyyy-MM-dd HH:mm:ss")
    private String addTimeEnd;
}
