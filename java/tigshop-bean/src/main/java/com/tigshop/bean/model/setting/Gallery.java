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
 * 相册表
 *
 * @author Jayce
 * @create 2024年11月12日 11:04
 */
@Data
@Schema(description = "相册表")
@TableName("gallery")
public class Gallery implements Serializable {
    @TableId(type = IdType.AUTO, value = "gallery_id")
    @Schema(description = "主键")
    private Integer galleryId;

    @TableField(value = "parent_id")
    @Schema(description = "父级id")
    private Integer parentId;

    @TableField(value = "gallery_admin_id")
    @Schema(description = "管理员id")
    private Integer galleryAdminId;

    @TableField(value = "gallery_name")
    @Schema(description = "相册名称")
    private String galleryName;

    @TableField(value = "gallery_sort")
    @Schema(description = "相册排序")
    private Integer gallerySort;

    @TableField(value = "gallery_thumb")
    @Schema(description = "相册缩略图")
    private String galleryThumb;

    @TableField(value = "shop_id")
    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "供应商id")
    private Integer vendorId;
}
