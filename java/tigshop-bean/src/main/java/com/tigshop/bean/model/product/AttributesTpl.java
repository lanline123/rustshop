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
import lombok.Data;

import java.io.Serializable;

/**
 * 商品属性模板表
 *
 * @author Jayce
 * @create 2024年11月04日 16:29
 */
@Data
@Schema(description = "商品属性模板表")
@TableName("product_attributes_tpl")
public class AttributesTpl implements Serializable {

    @TableId(value = "tpl_id", type = IdType.AUTO)
    @Schema(description = "商品属性模板表ID")
    private Integer tplId;

    @TableField(value = "tpl_name")
    @Schema(description = "模板名称")
    private String tplName;

    @TableField(value = "tpl_data")
    @Schema(description = "模板数据")
    private String tplData;

    @TableField(value = "shop_id")
    @Schema(description = "店铺ID")
    private Integer shopId;

    @TableField(value = "category_id")
    @Schema(description = "关联商品类目ID")
    private Integer categoryId;
}
