// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分销内容管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "分销内容管理参数")
public class SalesmanContentVO {
    @Schema(description = "分销内容管理ID")
    private Integer id;

    @Schema(description = "分销内容管理标题")
    private String title;

    @Schema(description = "分销内容管理描述")
    private String describe;

    @Schema(description = "分销内容管理内容")
    private String content;

    @Schema(description = "分销内容管理图片")
    private String img;

    @Schema(description = "分销内容管理开始时间")
    private String startTime;

    @Schema(description = "分销内容管理结束时间")
    private String endTime;

    @Schema(description = "分销内容管理排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "是否可用")
    private Integer isAvailable;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "图片列表")
    private List<String> pics;
}