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

/**
 * 相册信息更新对象
 *
 * @author Jayce
 * @create 2024年11月14日 10:50
 */
@Getter
@Setter
@Schema(description = "相册信息更新对象")
public class GalleryUpdateDTO extends GalleryDTO{
    @Schema(description = "编辑使用id")
    private Integer id;
}
