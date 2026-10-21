// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.product.CommentConstants.COMMENT_ID_NOT_NULL;

/**
 * 评价管理更新参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "评价管理参数")
public class CommentUpdateDTO {

    @Schema(description = "评价管理ID")
    @NotNull(message = COMMENT_ID_NOT_NULL)
    private Integer commentId;

    @Schema(description = "评论等级")
    private Integer commentRank;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "是否展示")
    private Integer isShowed;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "有用数量")
    private Integer usefull;

    @Schema(description = "无用数量")
    private Integer useless;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单项ID")
    private Integer orderItemId;

    @Schema(description = "父评论ID")
    private Integer parentId;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "关联的商品ID列表")
    private List<Integer> productIds;

    @Schema(description = "评论标签")
    private List<String> commentTag;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "评论内容")
    @NotNull(message = "评论内容不能为空")
    @Size(max = 500, message = "评论名称最多500个字符")
    private String content;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "客服名称")
    private String kefuName;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "显示图片列表")
    private List<ShowPic> showPics;

    @Schema(description = "回复内容")
    private Reply reply;

    @Schema(description = "店铺ID")
    private Integer shopId;

    // 内部类 ShowPic
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
    }

    @Data
    @Schema(description = "回复")
    public static class Reply {
        @Schema(description = "评论ID")
        private Integer commentId;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "评论内容")
        private String content;

        @Schema(description = "父评论ID")
        private Integer parentId;

        @Schema(description = "添加时间")
        private String addTime;
    }
}
