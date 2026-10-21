package com.tigshop.bean.param.product.comment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.order.ShowPic;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 商品评价 / 晒单参数DTO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "商品评价 / 晒单参数")
public class CommentEvaluateParam {

    @Schema(description = "产品id")
    private Integer productId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单item_id")
    private Integer orderItemId;

    @Schema(description = "评价星级")
    private Integer commentRank;

    @Schema(description = "评价标签")
    private List<String> commentTag;

    @Schema(description = "评价内容")
    @Size(min = 10, max = 500, message = "评价内容长度必须在10到500个字符之间")
    private String content;

    @Schema(description = "晒单图片")
    private List<ShowPic> showPics;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "是否匿名评价；0-否，1-是，不传视为 0")
    private Integer isAnonymous;

    public Comment createComment(User user) {

        String commentTag = ObjectUtil.isEmpty(this.getCommentTag()) ? "[]" : JSONUtil.toJsonStr(this.getCommentTag());
        String showPics = ObjectUtil.isEmpty(this.getShowPics()) ? "[]" : JSONUtil.toJsonStr(this.getShowPics());
        Integer isShowed = CollUtil.isNotEmpty(this.showPics) ? Constants.YES : Constants.NO;

        return Comment.builder()
                .productId(this.productId)
                .orderId(this.orderId)
                .orderItemId(this.orderItemId)
                .commentRank(this.commentRank)
                .commentTag(commentTag)
                .content(this.content)
                .showPics(showPics)
                .shopId(this.shopId)
                .userId(this.userId)
                .addTime(StringUtils.getCurrentTime())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .isAnonymous(ObjectUtil.defaultIfNull(this.isAnonymous, Constants.NO))
                .isShowed(isShowed)
                .build();
    }
}
