package com.tigshop.bean.vo.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 买家秀视频流项-客户端
 *
 * @author wzh
 */
@Data
@Schema(description = "买家秀视频流项-客户端")
public class BuyerShowVideoFeedItemClientVO {

    @Schema(description = "买家秀ID")
    private Integer id;

    @Schema(description = "视频信息")
    private Video video;

    @Schema(description = "该买家秀自带图片集，按 sortOrder 排序；无附图时为空数组")
    private List<Image> images;

    @Schema(description = "作者信息")
    private Author author;

    @Schema(description = "关联商品（可为空数组）")
    private List<Product> products;

    @Schema(description = "文案")
    private String content;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "是否已点赞（0否 1是）")
    private Integer isLike;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "是否已收藏（0否 1是）")
    private Integer isFavorite;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "发布时间")
    private String createTime;

    /**
     * 视频信息
     */
    @Data
    @Schema(description = "视频信息")
    public static class Video {

        @Schema(description = "视频播放地址")
        private String url;

        @Schema(description = "封面图")
        private String cover;

        @Schema(description = "宽（暂无采集，返回 null）")
        private Integer width;

        @Schema(description = "高（暂无采集，返回 null）")
        private Integer height;

        @Schema(description = "时长（秒，暂无采集，返回 null）")
        private Integer duration;
    }

    /**
     * 买家秀图片
     */
    @Data
    @Schema(description = "买家秀图片")
    public static class Image {

        @Schema(description = "图片地址")
        private String url;

        @Schema(description = "原图宽（暂无采集，返回 null）")
        private Integer width;

        @Schema(description = "原图高（暂无采集，返回 null）")
        private Integer height;
    }

    /**
     * 作者信息
     */
    @Data
    @Schema(description = "作者信息")
    public static class Author {

        @Schema(description = "作者会员ID")
        private Integer id;

        @Schema(description = "昵称")
        private String name;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "是否已关注（0否 1是）")
        private Integer isFollow;
    }

    /**
     * 关联商品
     */
    @Data
    @Schema(description = "关联商品")
    public static class Product {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "skuId（商品主表无该维度，返回 null）")
        private Integer skuId;

        @Schema(description = "商品名")
        private String name;

        @Schema(description = "价格（两位小数字符串）")
        private String price;

        @Schema(description = "商品图")
        private String picUrl;

        @Schema(description = "库存")
        private Integer stock;
    }
}
