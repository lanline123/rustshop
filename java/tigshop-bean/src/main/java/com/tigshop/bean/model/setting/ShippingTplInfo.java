// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 运费模板内容model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shipping_tpl_info")
@Schema(description = "运费模板内容")
public class ShippingTplInfo {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "运费模板内容ID")
    private Long id;

    @Schema(description = "配送类型ID")
    private Long shippingTypeId;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "是否包邮")
    private Integer isFree;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "地区信息，含id和name")
    private String regionData;

    @Schema(description = "起重或起件")
    private BigDecimal startNumber;

    @Schema(description = "起价")
    private BigDecimal startPrice;

    @Schema(description = "每增重或增价")
    private BigDecimal addNumber;

    @Schema(description = "增价")
    private BigDecimal addPrice;

    @Schema(description = "满多少包邮件")
    private BigDecimal freePrice;

    @Schema(description = "按重还是按件，1:按件，2：按重")
    private Integer pricingType;

}
