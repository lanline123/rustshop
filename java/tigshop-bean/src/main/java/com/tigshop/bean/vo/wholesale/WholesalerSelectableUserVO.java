package com.tigshop.bean.vo.wholesale;

import com.tigshop.bean.dto.user.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 「可选为批发商的会员」列表项 VO。
 *
 * <p>用途：添加批发商时的会员选择列表。继承 {@link UserDTO} 复用会员全字段
 *（含 {@code mobile} 的 {@code @Sensitive} 仅 demo 站脱敏），并追加批发商身份标识：
 * <ul>
 *   <li>{@code isWholesaler}：该会员是否已是有效批发商（前端可据此置灰禁选或自行过滤剔除）；</li>
 *   <li>{@code gradeId}/{@code gradeName}：已是批发商时回填其当前批发等级，否则为 null。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "可选为批发商的会员列表项")
public class WholesalerSelectableUserVO extends UserDTO {

    @Schema(description = "是否已是批发商：true=已是 false=未是")
    private Boolean isWholesaler;

    @Schema(description = "已是批发商时的批发等级ID（否则 null）")
    private Integer gradeId;

    @Schema(description = "已是批发商时的批发等级名称（否则 null）")
    private String gradeName;
}
