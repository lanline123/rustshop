// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.print;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 打印模板项
 *
 * @author Tigshop团队
 * @since 2025/7/22
 */
@Data
public class PrintTemplateItem {

    @Schema(description = "模板项标题")
    private String title;

    @Schema(description = "模板项内容")
    private List<PrintTemplateOption> options;
}