// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页快捷功能配置VO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "首页快捷功能配置")
public class ShortcutMenuVO {

    @Schema(description = "已固定到首页的功能 key 有序数组")
    private List<String> list = new ArrayList<>();
}
