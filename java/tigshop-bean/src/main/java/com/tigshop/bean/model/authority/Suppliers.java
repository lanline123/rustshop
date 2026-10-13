// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商管理model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("suppliers")
@Schema(description = "供应商管理")
public class Suppliers {
    @TableId(value = "suppliers_id", type = IdType.AUTO)
    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @TableField(value = "suppliers_name")
    @Schema(description = "供应商名称")
    private String suppliersName;

    @TableField(value = "suppliers_desc")
    @Schema(description = "供应商描述")
    private String suppliersDesc;

    @TableField(value = "is_show")
    @Schema(description = "是否展示")
    private Integer isShow;

    @TableField(value = "is_check")
    @Schema(description = "是否启用")
    private Integer isCheck;

    @TableField(value = "country")
    @Schema(description = "省份")
    private Integer country;

    @TableField(value = "province")
    @Schema(description = "市")
    private Integer province;

    @TableField(value = "city")
    @Schema(description = "市")
    private Integer city;

    @TableField(value = "district")
    @Schema(description = "区")
    private Integer district;

    @TableField(value = "contact_name")
    @Schema(description = "联系人")
    private String contactName;

    @TableField(value = "contact_phone")
    @Schema(description = "联系电话")
    private String contactPhone;

    @TableField(value = "contact_address")
    @Schema(description = "联系地址")
    private String contactAddress;

    @TableField(value = "shop_id")
    @Schema(description = "店铺id")
    private Integer shopId;
}