// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.decorate;

import com.tigshop.bean.dto.decorate.DecorateModuleListDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 页面管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "页面模块信息")
public class DecorateModuleListVO {
    @Schema(description = "模块页面ID")
    private Integer decorateId;

    @Schema(description ="模块列表")
    private List<DecorateModuleListDTO> moduleList;

    @Schema(description ="页面模块")
    private Map<String, Object> pageModule;

    @Schema(description ="绑定的页面ID")
    private Integer bindDecorateId;

    @Schema(description ="是否首页")
    private Integer isHome;
}