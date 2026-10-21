// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 微信公众号获取模板列表
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@Data
@Schema(description = "微信公众号获取模板列表")
public class GetAllTemplateResult {
    @Schema(description = "模板列表")
    private List<TemplateInfo> templateList;

    @Data
    public static class TemplateInfo {
        @Schema(description = "模板ID")
        private String templateId;

        @Schema(description = "模板标题")
        private String title;

        @Schema(description = "模板所属行业的一级行业")
        private String primaryIndustry;

        @Schema(description = "模板所属行业的二级行业")
        private String deputyIndustry;

        @Schema(description = "模板内容")
        private String content;

        @Schema(description = "模板示例")
        private String example;
    }
}
