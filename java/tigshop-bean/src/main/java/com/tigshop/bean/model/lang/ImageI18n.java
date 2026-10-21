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

/**
 * 海外站图片本地化映射
 * <p>
 * 把图片 URL 的本地化映射从被滥用的 translations_data（按 URL 字符串当翻译 key）中拆出，
 * 单表存储原图 URL -> 本地化图 URL，配合缓存与批量查消除商品详情/列表的图片翻译 N+1。
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "海外站图片本地化映射")
@TableName("image_i18n")
public class ImageI18n {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "语言id")
    private Integer localeId;

    @Schema(description = "数据类型：7=商品图片 8=规格图片")
    private Integer dataType;

    @Schema(description = "原图URL的MD5")
    private String originalKey;

    @Schema(description = "原图URL")
    private String originalUrl;

    @Schema(description = "本地化后的图URL")
    private String localizedUrl;

    @Schema(description = "创建时间")
    private Integer addTime;
}
