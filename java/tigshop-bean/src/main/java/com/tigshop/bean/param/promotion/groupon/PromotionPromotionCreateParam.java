package com.tigshop.bean.param.promotion.groupon;

import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.model.promotion.GrouponPromotionShop;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.List;

import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * 拼团活动创建参数
 *
 * @author kidd
 * @since 2025/8/28 14:27
 */
@Data
public class PromotionPromotionCreateParam {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称")
    private String name;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private String startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private String endTime;

    @NotNull(message = "拼团类型不能为空")
    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @NotNull(message = "成团有效时间不能为空")
    @Schema(description = "成团有效时间")
    private Long expirationTime;

    @Schema(description = "h5活动图片")
    private String grouponImage;

    @Schema(description = "pc活动图片")
    private String pcGrouponImage;

    @Schema(description = "分享图")
    private String shareImage;

    @Schema(description = "活动说明")
    private String grouponExplain;

    @Schema(description = "分配组织")
    private List<Integer> shopIds;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，仅供旧调用方兼容；新调用方请用 sendUserTarget）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集，优先于 memberAudience）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

    public GrouponPromotion buildGrouponPromotion() {

        Integer shopId = getShopId();

        return GrouponPromotion.builder()
                .name(name)
                .startTime(TigUtils.toTimestampYmdHms(startTime))
                .endTime(TigUtils.toTimestampYmdHms(endTime))
                .groupType(groupType)
                .expirationTime(expirationTime)
                .grouponImage(grouponImage)
                .pcGrouponImage(pcGrouponImage)
                .shareImage(shareImage)
                .grouponExplain(grouponExplain)
                .shopId(shopId)
                .addTime(StringUtils.getCurrentTime())
                .build();
    }

    public GrouponPromotionShop buildGrouponPromotionShop(Integer grouponPromotionId) {
        return GrouponPromotionShop.builder()
                .shopIds(JsonUtil.toJson(new HashSet<>(this.shopIds)))
                .grouponPromotionId(grouponPromotionId)
                .build();
    }
}
