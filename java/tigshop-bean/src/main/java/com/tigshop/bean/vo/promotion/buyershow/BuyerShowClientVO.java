package com.tigshop.bean.vo.promotion.buyershow;

import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.BuyerShowGallery;
import com.tigshop.bean.model.promotion.BuyerShowVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀详情信息-客户端")
public class BuyerShowClientVO {

    @Schema(description = "自增ID，买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "买家头像")
    private String buyerPhoto;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "图片信息")
    private List<BuyerShowGallery> imgList;

    @Schema(description = "视频信息")
    private List<BuyerShowVideo> videoList;

    @Schema(description = "关联商品信息")
    private List<Product> productList;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "发布时间")
    private String addTime;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "当前用户是否点赞（0：否，1：是）")
    private Integer isLike;

}
