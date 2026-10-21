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

import com.tigshop.bean.model.lang.TranslationsData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 翻译返回类
 *
 * @author Tigshop团队
 * @create 2025年01月02日 10:20
 */
@Data
@Schema(description = "翻译返回类")
public class TranslationsVO {
    @Schema(description = "数据id")
    private Integer id;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "待翻译内容")
    private String translationName;

    @Schema(description = "翻译key")
    private String translationKey;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "翻译内容")
    List<TranslationsData> tdata;
}
