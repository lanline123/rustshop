// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 评价管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价管理参数")
public class CommentVO {

    // *** Comment ***

    @Schema(description = "评价管理ID")
    private Integer commentId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "是否匿名评价；0-否，1-是")
    private Integer isAnonymous;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单项ID")
    private Integer orderItemId;

    @Schema(description = "评论等级")
    private Integer commentRank;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "父评论ID")
    private Integer parentId;

    @Schema(description = "有用数量")
    private Integer usefull;

    @Schema(description = "无用数量")
    private Integer useless;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "是否展示")
    private Integer isShowed;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "店铺ID")
    private Integer shopId;

    // *** Product ***

    @Schema(description = "产品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "产品编号")
    private String productSn;

    @Schema(description = "图片缩略图URL")
    private String picThumb;

    // *** Reply ***

    @Schema(description = "子评论列表")
    private List<ReplyVO> replies;

    // *** Other ***

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "评论标签")
    private List<String> commentTag;

    @Schema(description = "显示图片列表")
    private List<ShowPic> showPics;

    // *** UNKNOWN ***

    @Schema(description = "客服名称")
    private String kefuName;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "商品规格值（JSON）")
    private List<ProductSkuDTO.SkuData> skuData;


    @Data
    public static class SkuData {

        @Schema(description = "规格值名称")
        private String name;

        @Schema(description = "规格值名称")
        private String value;

        public SkuData(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "回复内容")
    public static class ReplyVO {

        @Schema(description = "回复ID")
        private Integer commentId;

        @Schema(description = "回复用户ID")
        private Integer userId;

        @Schema(description = "回复用户名")
        private String username;

        @Schema(description = "是否匿名；0-否，1-是。商家回复恒为 0")
        private Integer isAnonymous;

        @Schema(description = "回复内容")
        private String content;

        @Schema(description = "回复父id")
        private Integer parentId;

        @Schema(description = "回复时间")
        private String addTime;

        public ReplyVO(Comment comment) {
            this.commentId = comment.getCommentId();
            this.userId = comment.getUserId();
            this.username = comment.getUsername();
            this.isAnonymous = comment.getIsAnonymous();
            this.content = comment.getContent();
            this.parentId = comment.getParentId();
            this.addTime = TigUtils.handelTime(comment.getAddTime());
        }
    }

    @Data
    @Schema(description = "显示图片")
    public static class ShowPic {

        @Schema(description = "图片ID")
        private Integer picId;

        @Schema(description = "图片缩略图URL")
        private String picThumb;

        @Schema(description = "图片完整URL")
        private String picUrl;

        @Schema(description = "图片名称")
        private String picName;

        @Schema(description = "storage_url")
        private String storageUrl;

        @Schema(description = "errcode")
        private Integer errcode;

        @Schema(description = "message")
        private String message;
    }

    public CommentVO(Comment comment, Product product, List<Comment> replyComments) {

        List<String> commentTag = Collections.emptyList();
        if (JSONUtil.isTypeJSON(comment.getCommentTag())) {
            commentTag = JSONUtil.toList(comment.getCommentTag(), String.class);
        }

        List<CommentVO.ShowPic> showPics = getCommentShowPicsByJson(comment.getShowPics());

        this.commentId = comment.getCommentId();
        this.userId = comment.getUserId();
        this.username = comment.getUsername();
        this.isAnonymous = comment.getIsAnonymous();
        this.avatar = comment.getAvatar();
        this.productId = comment.getProductId();
        this.orderId = comment.getOrderId();
        this.orderItemId = comment.getOrderItemId();
        this.commentRank = comment.getCommentRank();
        this.content = comment.getContent();
        this.status = comment.getStatus();
        this.parentId = comment.getParentId();
        this.usefull = comment.getUsefull();
        this.useless = comment.getUseless();
        this.isRecommend = comment.getIsRecommend();
        this.isTop = comment.getIsTop();
        this.isShowed = comment.getIsShowed();
        this.isDefault = comment.getIsDefault();
        this.sortOrder = comment.getSortOrder();
        this.shopId = comment.getShopId();

        if (product != null) {
            this.productName = product.getProductName();
            this.productSn = product.getProductSn();
            this.picThumb = product.getPicThumb();
        }

        if (replyComments != null && !replyComments.isEmpty()) {
            this.replies = replyComments.stream()
                    .map(ReplyVO::new)
                    .toList();
        } else {
            this.replies = Collections.emptyList();
        }

        this.addTime = TigUtils.handelTime(comment.getAddTime());
        this.commentTag = commentTag;
        this.showPics = showPics;
    }

    private List<CommentVO.ShowPic> getCommentShowPicsByJson(String showPics) {
        if (showPics != null && !StringUtils.isEmpty(showPics)
                && !"[]".equals(showPics)) {
            try {
                if (showPics.trim().startsWith("[")) {
                    return JSONUtil.toList(showPics, CommentVO.ShowPic.class);
                } else if (showPics.trim().startsWith("{")) {
                    CommentVO.ShowPic pic = JSONUtil.toBean(showPics, CommentVO.ShowPic.class);
                    return Collections.singletonList(pic);
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}