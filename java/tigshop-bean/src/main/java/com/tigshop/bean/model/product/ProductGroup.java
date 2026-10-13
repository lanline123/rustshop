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
 * 商品分组model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("product_group")
@Schema(description = "商品分组")
public class ProductGroup implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer productGroupId;

    /**
     * 分组名称
     */
    private String productGroupName;

    /**
     * 分组别名
     */
    private String productGroupSn;

    /**
     * 分组描述
     */
    private String productGroupDescription;

    /**
     * 商品IDjson集合
     */
    private String productIds;

    /**
     * 添加时间
     */
    private Long addTime;

    /**
     * 店铺id
     */
    private Integer shopId;

}