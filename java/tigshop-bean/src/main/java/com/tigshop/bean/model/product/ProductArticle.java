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
 * 商品文章
 *
 * @author kidd
 * @create 2025/7/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品文章")
@TableName("product_article")
public class ProductArticle implements Serializable {

    @TableId(type = IdType.NONE)
    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "文章ID")
    private Integer articleId;

}
