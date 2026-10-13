// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.product.comment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 评价管理创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "评价管理参数")
public class CommentSaveParam {

    // *** Comment ***

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @NotNull(message = "产品ID不能为空")
    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "评论等级")
    private Integer commentRank;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    // *** Other ***

    @Schema(description = "评论标签")
    private List<String> commentTag;

    @Schema(description = "显示图片列表")
    private List<ShowPic> showPics;

   // *** UNKNOWN ***

    @Schema(description = "关联的商品ID列表")
    private List<Long> productIds;

    @Data
    public static class ShowPic {
        @Schema(description = "图片ID")
        private Long picId;

        @Schema(description = "图片缩略图URL")
        private String picThumb;

        @Schema(description = "图片完整URL")
        private String picUrl;

        @Schema(description = "图片名称")
        private String picName;
    }

    public Comment createComment() {

        long addTime = StrUtil.isNotBlank(this.addTime) ? DateUtil.parse(this.addTime).getTime() / 1000 : StringUtils.getCurrentTime();
        String commentTag = CollUtil.isNotEmpty(this.commentTag) ? JSON.toJSONString(this.commentTag) : null;
        String showPics = CollUtil.isNotEmpty(this.showPics) ? JSON.toJSONString(this.showPics) : "[]";

        return Comment.builder()
                .username(this.username)
                .avatar(this.avatar)
                .productId(this.productId)
                .commentRank(this.commentRank)
                .content(this.content)
                .addTime(addTime)
                .isRecommend(this.isRecommend)
                .isTop(this.isTop)
                .sortOrder(this.sortOrder)
                .commentTag(commentTag)
                .showPics(showPics)
                .build();
    }
}
