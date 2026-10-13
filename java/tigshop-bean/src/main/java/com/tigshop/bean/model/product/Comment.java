// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评价管理model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment")
@Schema(description = "评价管理")
public class Comment implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户评论的自增ID")
    private Integer commentId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "是否匿名评价；0-否，1-是")
    private Integer isAnonymous;

    @Schema(description = "会员头像")
    private String avatar;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单商品 itemId")
    private Integer orderItemId;

    @Schema(description = "星级，5代表5星")
    private Integer commentRank;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论时间")
    private Long addTime;

    @Schema(description = "状态：0=待审核，1=审核通过，2=无效")
    private Integer status;

    @Schema(description = "父评论ID（如果是回复）")
    private Integer parentId;

    @Schema(description = "评价有用次数")
    private Integer usefull;

    @Schema(description = "评价无用次数")
    private Integer useless;

    @Schema(description = "评论标签（JSON 数组）")
    private String commentTag;

    @Schema(description = "晒单图片（JSON 数组）[{pic_thumb, pic_url}]")
    private String showPics;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "是否已晒单")
    private Integer isShowed;

    @Schema(description = "是否系统默认评价")
    private Integer isDefault;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "所属店铺ID")
    private Integer shopId;
}