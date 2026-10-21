package com.tigshop.bean.vo.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动图片VO
 *
 * @author kidd
 * @since 2025/8/29 14:02
 */
@Data
public class GrouponPromotionImageVO {

    @Schema(description = "拼团 ID")
    @TableId(type = IdType.AUTO)
    private Long grouponPromotionId;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "h5活动图片")
    private String grouponImage;

    @Schema(description = "pc活动图片")
    private String pcGrouponImage;

    @Schema(description = "分享图")
    private String shareImage;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;

    public Long getGrouponPromotionId() {
        return grouponPromotionId;
    }

    public GrouponPromotionImageVO setGrouponPromotionId(Long grouponPromotionId) {
        this.grouponPromotionId = grouponPromotionId;
        return this;
    }

    public String getName() {
        return name;
    }

    public GrouponPromotionImageVO setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getGroupType() {
        return groupType;
    }

    public GrouponPromotionImageVO setGroupType(Integer groupType) {
        this.groupType = groupType;
        return this;
    }

    public String getGrouponImage() {
        return grouponImage;
    }

    public GrouponPromotionImageVO setGrouponImage(String grouponImage) {
        this.grouponImage = grouponImage;
        return this;
    }

    public String getPcGrouponImage() {
        return pcGrouponImage;
    }

    public GrouponPromotionImageVO setPcGrouponImage(String pcGrouponImage) {
        this.pcGrouponImage = pcGrouponImage;
        return this;
    }

    public String getShareImage() {
        return shareImage;
    }

    public GrouponPromotionImageVO setShareImage(String shareImage) {
        this.shareImage = shareImage;
        return this;
    }

    public Integer getShopId() {
        return shopId;
    }

    public GrouponPromotionImageVO setShopId(Integer shopId) {
        this.shopId = shopId;
        return this;
    }

    public Long getAddTime() {
        return addTime;
    }

    public GrouponPromotionImageVO setAddTime(Long addTime) {
        this.addTime = addTime;
        return this;
    }
}
