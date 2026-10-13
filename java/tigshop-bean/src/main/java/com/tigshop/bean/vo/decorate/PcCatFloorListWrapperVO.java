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


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/8 19:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "获取阶梯菜单")
public class PcCatFloorListWrapperVO {

    @Schema(description = "首页分类栏参数列表")
    private List<PcCatFloorListVO> catFloor;

    @Schema(description = "ICO自定义CSS")
    private String icoDefinedCss;
}
