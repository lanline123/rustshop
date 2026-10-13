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


import com.tigshop.bean.model.product.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/2 16:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品评论统计")
public class ProductCommentStatisticVO {

    @Schema(description = "评论总数")
    private Integer total;

    @Schema(description = "好评数")
    private Long goodCount;

    @Schema(description = "好评率")
    private BigDecimal goodPercent;

    @Schema(description = "中评数")
    private Long moderateCount;

    @Schema(description = "中评率")
    private BigDecimal moderatePercent;

    @Schema(description = "差评数")
    private Long badCount;

    @Schema(description = "差评率")
    private BigDecimal badPercent;

    @Schema(description = "晒单数")
    private Long showCount;

    @Schema(description = "平均评分")
    private Long averageRank;

    public ProductCommentStatisticVO(List<Comment> comments) {
        this.total = comments.size();

        this.goodCount = comments.stream().filter(comment -> comment.getCommentRank() >= 4).count();
        this.moderateCount = comments.stream().filter(comment -> comment.getCommentRank() == 3).count();
        this.badCount = comments.stream().filter(comment -> comment.getCommentRank() <= 2).count();
        this.showCount = comments.stream().filter(comment -> comment.getIsShowed() == 1).count();

        this.goodPercent = new BigDecimal(0);
        this.moderatePercent = new BigDecimal(0);
        this.badPercent = new BigDecimal(0);

        if (total != 0) {
            BigDecimal percent = new BigDecimal(total);
            this.goodPercent = new BigDecimal(this.goodCount).multiply(new BigDecimal(100)).divide(percent, 0, RoundingMode.DOWN);
            this.moderatePercent = new BigDecimal(this.moderateCount).multiply(new BigDecimal(100)).divide(percent, 0, RoundingMode.DOWN);
            this.badPercent = new BigDecimal(this.badCount).multiply(new BigDecimal(100)).divide(percent, 0, RoundingMode.DOWN);

            comments.stream()
                    .mapToLong(Comment::getCommentRank)
                    .average()
                    .ifPresent(averageRank -> this.averageRank = Math.round(averageRank));
        }



    }
}
