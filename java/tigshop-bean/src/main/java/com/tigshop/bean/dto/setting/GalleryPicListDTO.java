// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 相册图片列表入参
 *
 * @author Jayce
 * @create 2024年11月13日 15:55
 */
@Schema(description = "相册图片列表参数")
@Getter
@Setter
public class GalleryPicListDTO extends BasePage {
    @Schema(description = "相册id")
    private Integer galleryId;

    @Schema(description = "格式筛选，如jpg、png、webp")
    private String format;

    @Schema(description = "来源筛选：local/upload=本地上传, external=外部直链, 空=全部")
    private String source;

}
