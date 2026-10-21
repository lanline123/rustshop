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
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 相册图片返回参数,嵌套在分类对象
 * @author Jayce
 * @create 2024年11月13日 16:11
 */
@Schema(description = "相册图片返回参数")
@Getter
@Setter
public class GalleryPicResDTO extends GalleryDTO{
    @Schema(description = "图片列表")
    private List<GalleryPicDTO> galleryPics;
}
