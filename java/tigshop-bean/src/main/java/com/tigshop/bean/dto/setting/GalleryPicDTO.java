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
import lombok.Data;

/**
 * 相册图片参数对象
 *
 * @author Jayce
 * @create 2024年11月13日 13:50
 */
@Data
@Schema(description = "相册图片参数对象")
public class GalleryPicDTO {
    @Schema(description = "主键")
    private Integer picId;

    @Schema(description = "店铺id")
    private String shopId;

    @Schema(description = "相册id")
    private Integer galleryId;

    @Schema(description = "操作人员id")
    private Integer picOwerId;

    @Schema(description = "图片地址")
    private String picUrl;

    @Schema(description = "图片名称")
    private String picName;

    @Schema(description = "缩略图")
    private String picThumb;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "文件格式，如jpg、png、webp")
    private String fileType;

    @Schema(description = "图片宽度")
    private Integer picWidth;

    @Schema(description = "图片高度")
    private Integer picHeight;

    @Schema(description = "图片大小（字节）")
    private Long picSize;

}
