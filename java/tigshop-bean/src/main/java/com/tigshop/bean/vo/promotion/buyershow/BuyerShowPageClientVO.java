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
@Schema(description = "买家秀分页信息-客户端")
public class BuyerShowPageClientVO {

    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "图片信息")
    private List<BuyerShowGallery> imgList;

    @Schema(description = "视频信息")
    private List<BuyerShowVideo> videoList;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "买家头像")
    private String buyerPhoto;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "当前用户是否点赞（0：否，1：是）")
    private Integer isLike;

    @Schema(description = "是否视频类（0：否，1：是）。图片与视频按 sortOrder 混排后第一条是视频则为是，平局视频优先。"
            + "前端据此决定点击进视频流还是图文详情，并显示视频角标")
    private Integer isVideo;
}
