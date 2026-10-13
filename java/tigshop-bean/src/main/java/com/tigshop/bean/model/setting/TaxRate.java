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
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 税率管理
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tax_rate")
@Schema(description = "税率管理")
public class TaxRate implements Serializable {

    @TableId(value = "tax_rate_id", type = IdType.AUTO)
    @Schema(description = "税率ID")
    private Integer taxRateId;

    @TableField(value = "region_id")
    @Schema(description = "国家和地区ID")
    private Integer regionId;

    @TableField(value = "region_name")
    @Schema(description = "国家和地区名称")
    private String regionName;

    @TableField(value = "rate")
    @Schema(description = "税率(百分比)")
    private BigDecimal rate;

    @TableField(value = "is_active")
    @Schema(description = "是否生效:0-否,1-是")
    private Integer isActive;

    @TableField(value = "remark")
    @Schema(description = "备注")
    private String remark;

    @TableField(value = "create_time")
    @Schema(description = "创建时间(秒级时间戳)")
    private Long createTime;

    @TableField(value = "update_time")
    @Schema(description = "更新时间(秒级时间戳)")
    private Long updateTime;
}
