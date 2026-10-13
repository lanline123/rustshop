// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.lang;

import com.tigshop.bean.model.lang.TranslationsData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 翻译
 *
 * @author Tigshop团队
 * @create 2025年01月02日 14:16
 */
@Data
@Schema(description = "翻译")
public class TranslationsCreateParam {

    @Schema(description = "数据id")
    private Integer dataId;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "翻译词汇")
    private String translationName;

    @Schema(description = "翻译内容")
    private List<TranslationsData> items;

    @Schema(description = "译文来源：0-未知/人工，1-AI翻译，2-AI智能消除（仅商品图片 dataType=7 场景生效；item 未带时取此默认）")
    private Integer translationSource;
}
