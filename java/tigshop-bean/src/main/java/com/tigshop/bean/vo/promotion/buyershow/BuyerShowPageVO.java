package com.tigshop.bean.vo.promotion.buyershow;

import com.tigshop.bean.model.promotion.BuyerShowGallery;
import com.tigshop.bean.model.promotion.BuyerShowVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀分页信息")
public class BuyerShowPageVO {

    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "图片信息")
    private List<BuyerShowGallery> imgList;

    @Schema(description = "视频信息")
    private List<BuyerShowVideo> videoList;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "分组信息")
    private List<String> groupList;

    @Schema(description = "商品数")
    private Integer productCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "发布人")
    private String buyerName;

    @Schema(description = "状态（0：待审核，1：通过，2：拒绝）")
    private Integer status;

    @Schema(description = "是否公开（0：隐藏，1：公开）")
    private Integer isShow;

    @Schema(description = "当前状态（待审核，隐藏，公开）")
    private String statusText;

    @Schema(description = "排序")
    private Integer sortOrder;
}
