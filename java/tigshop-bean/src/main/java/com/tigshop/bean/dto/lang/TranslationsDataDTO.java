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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 翻译数据
 *
 * @author Tigshop团队
 * @create 2025年01月02日 14:56
 */
@Data
@Schema(description = "翻译数据")
public class TranslationsDataDTO {
    private Integer id;

    @Schema(description = "数据id")
    private Integer dataId;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "语言id")
    private Integer localeId;

    @Schema(description = "翻译key")
    private String translationKey;

    @Schema(description = "待翻译内容")
    private String translationName;

    @Schema(description = "翻译值")
    private String translationValue;

    @Schema(description = "译文来源：0-未知/人工，1-AI翻译，2-AI智能消除（仅商品图片 dataType=7 场景透出）")
    private Integer translationSource;

    @Schema(description = "翻译语言")
    private LocalesDTO locales;
}
