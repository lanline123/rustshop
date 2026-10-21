// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 电子卡券组VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "电子卡券组参数")
public class ECardGroupVO {
    @Schema(description = "卡券分组id")
    private Integer groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "0:不使用,1:使用")
    private Integer isUse;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "更新时间")
    private String upTime;
}