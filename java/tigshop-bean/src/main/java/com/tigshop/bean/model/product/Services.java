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
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品服务类
 *
 * @author Jayce
 * @create 2024年11月06日 10:21
 */
@Data
@Schema(description = "商品服务参数")
@TableName("product_services")
public class Services implements Serializable {
    @TableId(value = "product_service_id", type = IdType.AUTO)
    @Schema(description = "服务id")
    private Integer productServiceId;

    @TableField(value = "product_service_name")
    @Schema(description = "服务名称")
    @JsonTranslate
    private String productServiceName;

    @TableField(value = "product_service_desc")
    @Schema(description = "服务描述")
    @JsonTranslate
    private String productServiceDesc;

    @TableField(value = "ico_img")
    @Schema(description = "服务图标")
    private String icoImg;

    @TableField(value = "sort_order")
    @Schema(description = "排序")
    private Integer sortOrder;

    @TableField(value = "default_on")
    @Schema(description = "默认开启")
    private Integer defaultOn;

    @TableField(value = "shop_id")
    @Schema(description = "店铺id")
    private Integer shopId;
}
