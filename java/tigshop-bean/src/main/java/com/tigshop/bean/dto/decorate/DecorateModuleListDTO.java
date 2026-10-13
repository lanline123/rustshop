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

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 页面管理列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "装修模块")
public class DecorateModuleListDTO extends BasePage {
    @Schema(description ="active")
    private Boolean active;

    @Schema(description ="类型")
    private String type;

    @Schema(description ="是否显示")
    private Boolean isShow;

    @Schema(description ="名称")
    private String label;

    @Schema(description ="模块注册键，与 type 组成后台编辑器的 type@url")
    private String url;

    @Schema(description ="index")
    private Long moduleIndex;

    @Schema(description ="模块")
    private Object module;

}
