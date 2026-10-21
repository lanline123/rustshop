// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分销内容管理创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "分销内容管理参数")
public class SalesmanContentSaveParam {

    @Schema(description = "分销内容管理ID")
    private Integer id;

    @Schema(description = "分销内容管理标题")
    private String title;

    @Schema(description = "分销内容管理描述")
    private String describe;

    @NotBlank(message = "分销内容管理内容不能为空")
    @Schema(description = "分销内容管理内容")
    private String content;

    @Schema(description = "分销内容管理图片")
    private String img;

    @Schema(description = "分销内容管理开始时间")
    private String startTime;

    @Schema(description = "分销内容管理结束时间")
    private String endTime;

    @Schema(description = "是否置顶")
    private Integer isTop;
}
