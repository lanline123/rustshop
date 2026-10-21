// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 税率管理VO
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
@Data
@Schema(description = "税率管理VO")
public class TaxRateVO {

    @Schema(description = "税率ID")
    private Integer taxRateId;

    @Schema(description = "国家和地区ID")
    private Integer regionId;

    @Schema(description = "国家和地区名称")
    private String regionName;

    @Schema(description = "税率(百分比)")
    private BigDecimal rate;

    @Schema(description = "是否生效:0-否,1-是")
    private Integer isActive;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private Long createTime;

    @Schema(description = "更新时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private Long updateTime;
}
