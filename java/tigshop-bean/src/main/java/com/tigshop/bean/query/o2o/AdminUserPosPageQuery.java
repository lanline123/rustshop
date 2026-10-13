// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.o2o;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 收银台员工查询参数
 *
 * @author Tigshop团队
 * @create 2024年12月04日 17:17
 */
@Schema(description = "收银台员工查询参数")
@Setter
@Getter
public class AdminUserPosPageQuery extends BasePage {
    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private Integer shopId;

    /**
     * 是否使用
     */
    @Schema(description = "是否使用")
    private Integer isUsing;

    @Schema(description = "开始创建时间 yyyy-MM-dd")
    private String searchStartTime;

    @Schema(description = "结束创建时间 yyyy-MM-dd")
    private String searchEndTime;

    @Schema(description = "开始创建时间", hidden = true)
    private Long startCreateTime;

    @Schema(description = "结束创建时间", hidden = true)
    private Long endCreateTime;

}