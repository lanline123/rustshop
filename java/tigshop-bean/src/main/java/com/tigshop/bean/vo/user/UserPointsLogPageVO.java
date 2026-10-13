package com.tigshop.bean.vo.user;

import com.tigshop.bean.dto.user.UserPointsLogDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 积分列表
 *
 * @author kidd
 * @since 2025/5/8 15:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsLogPageVO {

    @Schema(description = "用户积分")
    private Integer userPoints;

    @Schema(description = "用户积分记录列表")
    private List<UserPointsLogDTO> records;

    @Schema(description = "用户积分列表记录总数")
    private Long total;

}
