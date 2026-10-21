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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品样式关联表")
@TableName("product_style_item")
public class ProductStyleItem implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Integer itemId;

    @Schema(description = "样式ID")
    private Integer styleId;

    @Schema(description = "关联类型 1=商品 2=分组 3=品牌")
    private Integer itemType;

    @Schema(description = "关联目标ID（商品ID/分组ID/品牌ID）")
    private Integer refId;

    @Schema(description = "创建时间")
    private Long addTime;
}
