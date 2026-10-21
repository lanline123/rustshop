// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.lang;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 翻译内容列表查询参数
 *
 * @author Tigshop团队
 * @create 2025年01月02日 10:12
 */
@Setter
@Getter
@Schema(description = "翻译内容列表查询参数")
public class TranslationsListDTO extends BasePage {
    @Schema(description = "翻译内容名称")
    private String translationName;

    @Schema(description = "语言地区代码，如 'en_US'")
    private String localeCode;

    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "语言地区")
    private String localeIds;
}
