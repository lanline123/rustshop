// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 相册图片表
 *
 * @author Jayce
 * @create 2024年11月12日 11:13
 */
@Data
@Schema(description = "相册表")
@TableName("gallery_pic")
public class GalleryPic implements Serializable {
    @TableId(type = IdType.AUTO, value = "pic_id")
    @Schema(description = "主键")
    private Integer picId;

    @Schema(description = "店铺id")
    @TableField(value = "shop_id")
    private Integer shopId;

    @Schema(description = "相册id")
    @TableField(value = "gallery_id")
    private Integer galleryId;

    @Schema(description = "操作人员id")
    @TableField(value = "pic_ower_id")
    private Integer picOwerId;

    @Schema(description = "图片地址")
    @TableField(value = "pic_url")
    private String picUrl;

    @Schema(description = "图片名称")
    @TableField(value = "pic_name")
    private String picName;

    @Schema(description = "缩略图")
    @TableField(value = "pic_thumb")
    private String picThumb;

    @Schema(description = "添加时间")
    @TableField(value = "add_time")
    private String addTime;

    @Schema(description = "供应商id")
    private Integer vendorId;


    @Schema(description = "是否隐藏")
    private Integer hidden;

    @Schema(description = "图片宽度")
    @TableField(value = "pic_width")
    private Integer picWidth;

    @Schema(description = "图片高度")
    @TableField(value = "pic_height")
    private Integer picHeight;

    @Schema(description = "图片大小（字节）")
    @TableField(value = "pic_size")
    private Long picSize;

    @Schema(description = "文件格式")
    @TableField(value = "file_type")
    private String fileType;

    @Schema(description = "排序值")
    @TableField(value = "pic_sort")
    private Integer picSort;

    @Schema(description = "来源：upload=本地上传, external=外部直链")
    @TableField(value = "source")
    private String source;

}
