package com.tigshop.bean.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服搜索用户相关VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "客服搜索用户相关参数")
public class ImSearchUserVO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "最后客服ID")
    private Integer lastServantId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "最后更新时间")
    private String lastUpdateTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "会话状态：0待接入；1会话中；2已结束")
    private Integer status;

    @Schema(description = "会话备注")
    private String remark;

    @Schema(description = "会话总结")
    private String summary;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "用户信息")
    private UserVO user;

    @Data
    @Schema(description = "用户信息")
    public static class UserVO {
        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;
    }

}
