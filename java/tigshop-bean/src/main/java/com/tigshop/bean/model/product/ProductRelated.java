package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 关联商品信息表
 *
 * @author Jayce
 * @create 2024年11月20日 14:50
 */
@Data
@Schema(description = "关联商品信息表")
@TableName(value = "product_related")
public class ProductRelated implements Serializable {
    @Schema(description = "商品ID")
    @TableId(value = "product_id", type = IdType.NONE)
    private Integer productId;

    @Schema(description = "被关联的商品的id")
    private Integer relatedProductId;
}
