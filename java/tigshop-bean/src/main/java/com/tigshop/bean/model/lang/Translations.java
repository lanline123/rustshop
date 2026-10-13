// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.lang;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 翻译
 *
 * @author Tigshop团队
 * @create 2024年12月19日 16:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "翻译")
@TableName("translations")
public class Translations implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "待翻译内容")
    private String translationName;

    @Schema(description = "翻译key")
    private String translationKey;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "店铺id")
    private Integer shopId;

    public Translations(Integer id, Integer dataType, String translationName) {
        this.id = id;
        this.dataType = dataType;
        this.translationName = translationName;
    }

}
