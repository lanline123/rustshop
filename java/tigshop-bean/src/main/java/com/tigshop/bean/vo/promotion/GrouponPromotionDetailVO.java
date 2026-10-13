package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 拼团详情
 *
 * @author kidd
 * @since 2025/8/28 15:03
 */
@Data
public class GrouponPromotionDetailVO {

    @Schema(description = "拼团 ID")
    private Long grouponPromotionId;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "开始时间")
    private String startTime;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "成团有效时间")
    private Long expirationTime;

    @Schema(description = "h5活动图片")
    @JsonTranslate(dataType = 7)
    private String grouponImage;

    @Schema(description = "pc活动图片")
    @JsonTranslate(dataType = 7)
    private String pcGrouponImage;

    @Schema(description = "分享图")
    @JsonTranslate(dataType = 7)
    private String shareImage;

    @Schema(description = "活动说明")
    private String grouponExplain;

    @Schema(description = "适用组织")
    private List<Shop> shops;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，无法无损表达 sendUserTarget 时为空）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

    @Schema(description = "当前用户是否可参与（前台；未登录或后台返回 true）")
    private Boolean canJoin;

    @Schema(description = "不可参与时的受限提示文案，可空")
    private String restrictTip;

}
