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
 * 打印机列表查询条件
 *
 * @author Tigshop团队
 * @create 2025/1/15
 */
@Setter
@Getter
@Schema(description = "打印机列表查询条件")
public class PrintPageQuery extends BasePage {

    @Schema(description = "打印机名称")
    private String printName;

    @Schema(description = "打印机SN")
    private String printSn;

    @Schema(description = "状态 1开启 2关闭")
    private Integer status;

    @Schema(description = "第三方平台 1飞鹅云")
    private Integer platform;

    @Schema(description = "按收银台设备码筛选")
    private String deviceCode;
}
