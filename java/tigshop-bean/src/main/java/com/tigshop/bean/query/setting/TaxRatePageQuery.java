// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.setting;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 税率管理分页查询
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "税率管理分页查询")
public class TaxRatePageQuery extends BasePage {

    @Schema(description = "国家和地区名称(模糊搜索)")
    private String regionName;

    @Schema(description = "是否生效:0-否,1-是")
    private Integer isActive;

    @Schema(description = "开始时间(yyyy-MM-dd)")
    private String startTime;

    @Schema(description = "结束时间(yyyy-MM-dd)")
    private String endTime;
}
