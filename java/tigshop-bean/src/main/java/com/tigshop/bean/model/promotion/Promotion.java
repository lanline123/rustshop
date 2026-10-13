package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 营销管理model
 *
 * @author kidd
 * @create 2025/7/1
 */
@Schema(description = "营销管理")
@Data
@TableName("promotion")
public class Promotion {

    @TableId(value = "promotion_id", type = IdType.AUTO)
    @Schema(description = "活动ID")
    private Integer promotionId;

    @Schema(description = "活动名称")
    @JsonTranslate(dataType = 10)
    private String promotionName;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "类型1秒杀2优惠券3满减4折扣5满赠6限时折扣7抽奖活动")
    private Integer type;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "活动对应的ID")
    private Integer relationId;

    @Schema(description = "活动范围:0,全部商品;3,指定商品.4，不含以下商品")
    @TableField(value = "`range`")
    private Integer range;

    @Schema(description = "[json]活动范围相关信息")
    private String rangeData;

    @Schema(description = "允许的SKU")
    private String skuIds;

    @Schema(description = "是否启用")
    private Integer isAvailable;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表（JSON格式，如[1,2,3]）")
    private String memberMarks;

    @Schema(description = "会员标签二期人群圈选(MemberAudienceDTO 序列化JSON)，非空则优先于 memberRange/memberMarks")
    private String memberScope;
}