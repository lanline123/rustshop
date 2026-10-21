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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.setting.GalleryConstants.GALLERY_NAME_IS_NULL;
import static com.tigshop.common.constant.setting.GalleryConstants.GALLERY_NAME_OVER_LENGTH;

/**
 * 相册数据对象
 *
 * @author Jayce
 * @create 2024年11月12日 15:14
 */
@Data
@Schema(description = "相册数据对象")
public class GalleryDTO {
    @Schema(description = "主键")
    private Integer galleryId;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "管理员id")
    private Integer galleryAdminId;

    @Schema(description = "相册名称")
    @NotNull(message = GALLERY_NAME_IS_NULL)
    @Size(max = 100, message = GALLERY_NAME_OVER_LENGTH)
    private String galleryName;

    @Schema(description = "相册排序")
    private Integer gallerySort;

    @Schema(description = "相册缩略图")
    private String galleryThumb;

    @Schema(description = "店铺id")
    private Integer shopId;
}
