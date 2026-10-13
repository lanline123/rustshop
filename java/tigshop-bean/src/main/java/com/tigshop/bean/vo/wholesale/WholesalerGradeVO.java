package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商等级 VO
 *
 * <p>CR-20260602-134800（CR-VOS-3）：曾约定不返回默认等级标记字段。
 * 后续按需求新增 {@link #isDefault}（= 批发设置 wholesaleDefaultGradeId 指定的等级），
 * 供等级列表直接区分默认等级，无需再单独调 /setting 接口。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商等级项")
public class WholesalerGradeVO {

    @Schema(description = "等级ID")
    private Integer gradeId;

    @Schema(description = "等级名称")
    private String gradeName;

    @Schema(description = "状态：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "排序值，升序")
    private Integer sortOrder;

    @Schema(description = "已绑定客户数（withUserCount=true 时返回）")
    private Integer userCount;

    @Schema(description = "是否默认等级（=批发设置 wholesaleDefaultGradeId 指定的等级）")
    private Boolean isDefault;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "添加时间 yyyy-MM-dd HH:mm:ss")
    private String addTime;
}
