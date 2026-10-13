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

import static com.tigshop.common.constant.decorate.PcNavigationConstants.*;

/**
 * 首页分类栏创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "首页分类栏参数")
public class PcNavigationCreateDTO {
    @Schema(description ="首页分类栏名称")
    @NotBlank(message = TITLE_NOT_NULL)
    @Size(max = 50, message = TITLE_OVER_LENGTH)
    private String title;

    @Schema(description ="是否显示")
    private Integer isShow;

    @Schema(description ="页面显示顺序，数字越大越靠后")
    private Integer sortOrder;

    @Schema(description ="导航链接是否在新窗口打开，1为是，其他为否")
    private Integer isBlank;

    @Schema(description ="链接数据，JSON格式{name,label,link,app_link,id,sn……}")
    private Object link;

    @Schema(description ="导航类型：1为导航条，2为顶部站点地图导航，3为底部快捷导航")
    private Integer type;

    @Schema(description ="父导航ID（需具体模板支持）")
    private Integer parentId;

    @Schema(description ="图标")
    private String icon;
}
