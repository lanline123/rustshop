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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 优惠券分页结果：在标准分页（records/total）基础上附带各状态数量。
 * 继承 Page&lt;CouponVO&gt; 以保持原有分页字段兼容（records/total/size/current/pages）。
 *
 * @author Tigshop团队
 */
@Schema(description = "优惠券分页结果（含各状态数量）")
public class CouponPageVO extends Page<CouponVO> {

    @Schema(description = "各状态数量（与 status 入参无关，按当前店铺全量统计）")
    private CouponStatusCountVO statusCount;

    public CouponStatusCountVO getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(CouponStatusCountVO statusCount) {
        this.statusCount = statusCount;
    }
}
