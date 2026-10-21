package com.tigshop.bean.vo.user;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺收藏VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会员等级配置VO")
public class UserRankConfigVO {
    @Schema(description = "会员等级配置ID")
    private Integer id;

    @Schema(description = "配置编码")
    private String code;

    @Schema(description = "类型")
    private Integer rankType;

    @Schema(description = "等级配置")
    private JSONObject data;
}
