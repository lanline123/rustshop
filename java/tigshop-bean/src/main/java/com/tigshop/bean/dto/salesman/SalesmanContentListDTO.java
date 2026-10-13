// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.salesman;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分销内容管理列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "分销内容管理列表参数")
public class SalesmanContentListDTO extends BasePage {
    // 新增字段
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "来源")
    private String from;
}
