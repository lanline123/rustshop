// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.config;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 上传相册图片返回参数
 *
 * @author Jayce
 * @create 2024年11月14日 13:43
 */
@Schema(description = "上传相册图片返回参数")
@Getter
@Setter
public class GalleryPicUploadVO {
    @Schema(description = "图片缩略图")
    private String picThumb;

    @Schema(description = "图片地址")
    private String picUrl;

    @Schema(description = "图片名称")
    private String picName;

    @Schema(description = "图片id")
    private Integer picId;

    @Schema(description = "图片宽度")
    private Integer picWidth;

    @Schema(description = "图片高度")
    private Integer picHeight;

    @Schema(description = "图片大小（字节）")
    private Long picSize;

    @Schema(description = "所属分组ID")
    private Integer galleryId;

    @Schema(description = "文件格式")
    private String fileType;

    public GalleryPicUploadVO(String picUrl, String picName, Integer picId, String picThumb){
        this.picUrl = picUrl;
        this.picName = picName;
        this.picId = picId;
        this.picThumb = picThumb;
    }
}
