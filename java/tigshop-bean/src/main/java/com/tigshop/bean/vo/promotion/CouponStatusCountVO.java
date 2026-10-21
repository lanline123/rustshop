// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券各状态数量（与列表 status 入参无关，按当前店铺全量统计）
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "优惠券各状态数量")
public class CouponStatusCountVO {

    @Schema(description = "进行中数量")
    private Integer processing = 0;

    @Schema(description = "待生效数量")
    private Integer pending = 0;

    @Schema(description = "已失效数量")
    private Integer expired = 0;
}
