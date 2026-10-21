// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品描述图片（供后台配置图片本地化：原图 URL -> 各语言本地化图 URL，dataType=7）
 *
 * @author Tigshop团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品描述图片")
public class ProductDescImageVO {

    @Schema(description = "类型：pic=描述图片 video=视频封面")
    private String type;

    @Schema(description = "原图URL（作为 image_i18n 本地化映射的 key）")
    private String url;
}
