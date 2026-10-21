// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品相册
 *
 * @author Tigshop团队
 * @create 2024年11月27日 14:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品相册")
@TableName("product_gallery")
public class ProductGallery implements Serializable {

    @Schema(description = "商品图片ID")
    @TableId(value = "pic_id", type = IdType.AUTO)
    private Integer picId;

    @TableField(value = "product_id")
    @Schema(description = "商品ID")
    private Integer productId;

    @TableField(value = "shop_id")
    @Schema(description = "门店ID，0=平台")
    private Long shopId;

    @TableField(value = "pic_url")
    @Schema(description = "商品图片")
    private String picUrl;

    @TableField(value = "pic_desc")
    @Schema(description = "商品图片描述")
    private String picDesc;

    @TableField(value = "pic_thumb")
    @Schema(description = "商品图片缩略图")
    private String picThumb;

    @TableField(value = "pic_original")
    @Schema(description = "商品图片原图")
    private String picOriginal;

    @TableField(value = "pic_large")
    @Schema(description = "商品图片大图")
    private String picLarge;

    @TableField(value = "sort_order")
    @Schema(description = "商品图片排序")
    private Integer sortOrder;



}
