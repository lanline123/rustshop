// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.lang;

import com.tigshop.bean.dto.lang.TranslationsDataDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 翻译详情
 *
 * @author Tigshop团队
 * @create 2025年01月02日 14:49
 */
@Data
@Schema(description = "翻译详情")
public class TranslationsDetailVO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "翻译key")
    private String translationKey;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "翻译词汇")
    private String translationName;

    @Schema(description = "翻译内容")
    private List<TranslationsDataDTO> items;
}
