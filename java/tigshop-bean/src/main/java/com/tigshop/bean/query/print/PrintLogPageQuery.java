// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.print;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 打印日志分页查询参数
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Setter
@Getter
@Schema(description = "打印日志分页查询参数")
public class PrintLogPageQuery extends BasePage {

    @Schema(description = "平台 1=飞鹅云 2=商米云")
    private Integer platform;

    @Schema(description = "状态 0=待推送 1=推送成功 2=打印成功 3=推送失败 4=打印机离线")
    private Integer status;

    @Schema(description = "触发方式 1=自动 2=手动补打")
    private Integer triggerType;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "打印机SN（模糊）")
    private String printSn;

    @Schema(description = "开始时间（Unix 秒）")
    private Integer startTime;

    @Schema(description = "结束时间（Unix 秒）")
    private Integer endTime;
}
