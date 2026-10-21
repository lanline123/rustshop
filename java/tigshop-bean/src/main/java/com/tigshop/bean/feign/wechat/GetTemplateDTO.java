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
 * 获取模板数据
 *
 * @author Tigshop团队
 * @create 2024年12月27日 14:42
 */
@Data
@Schema(description = "获取模板数据")
public class GetTemplateDTO {
    @Schema(description = "添加至账号下的模板 id，发送小程序订阅消息时所需")
    private String priTmplId;

    @Schema(description = "模板标题")
    private String title;

    @Schema(description = "模板内容")
    private String content;

    @Schema(description = "示例")
    private String example;

    @Schema(description = "模板类型")
    private String type;

    @Schema(description = "关键词列表")
    private KeywordEnumValueList keywordEnumValueList;

    @Data
    public static class KeywordEnumValueList{
        private String keywordCode;
        private List<String> enumValueList;
    }
}
