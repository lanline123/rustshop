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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 同步快递鸟物流公司结果
 */
@Data
@Schema(description = "同步快递鸟物流公司结果")
public class SyncLogisticsCompanyResultVO {

    @Schema(description = "远端快递公司数量（国内+国际合并后）")
    private Integer remoteTotal;

    @Schema(description = "新增数量")
    private Integer inserted;

    @Schema(description = "更新数量（通常仅更新名称）")
    private Integer updated;

    @Schema(description = "冲突数量")
    private Integer conflicted;
}
