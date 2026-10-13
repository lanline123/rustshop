// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.decorate.DecorateConstants.*;

/**
 * 页面管理创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "页面管理参数")
public class DecorateCreateDTO {
    @NotBlank(message = DECORATE_TITLE_NOT_NULL)
    @Size(max = 50, message = DECORATE_TITLE_OVER_LENGTH)
    @Schema(description ="模板名称")
    private String decorateTitle;

    @Schema(description ="已发布的页面模块数据")
    private String data;

    @Schema(description ="草稿页面模块数据")
    private String draftData;

    @Schema(description ="页面类型 1:h5 2:PC 3:自定义页面")
    private Integer decorateType;

    @Schema(description ="页面类型 1:商城 2:店铺 3:门店 4:自定义 7:导航")
    private Integer pageType;

    @Schema(description ="是否是首页")
    private Integer isHome;

    @Schema(description ="所属店铺")
    private Integer shopId;

    @Schema(description ="状态 1:发布 0:未发布")
    private Integer status;

    @Schema(description ="更新时间")
    private Integer updateTime;

    @Schema(description = "排序")
    private Integer sortOrder;
}
