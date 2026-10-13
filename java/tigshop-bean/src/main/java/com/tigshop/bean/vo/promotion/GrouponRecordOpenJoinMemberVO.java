package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动开团成员分页列表
 *
 * @author kidd
 * @since 2025/8/29 09:51
 */
@Data
public class GrouponRecordOpenJoinMemberVO {

    @Schema(description = "参与类型；1-开团，2-参团")
    private Integer joinType;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "创建时间")
    private Long addTime;

    // *** Other ***

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "id")
    private Integer userId;

    public GrouponRecordOpenJoinMemberVO(GrouponPromotionRecord record, User user, String shopTitle) {
        this.joinType = record.getJoinType();
        this.shopId = record.getShopId();
        this.addTime = record.getAddTime();
        this.shopTitle = shopTitle;

        if (user != null) {
            this.username = user.getUsername();
            this.avatar = user.getAvatar();
            this.userId = user.getUserId();
        } else {
            this.username = record.getUsername();
            this.avatar = record.getAvatar();
            this.userId = record.getUserId().intValue();
        }
    }

}
