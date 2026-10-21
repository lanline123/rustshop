// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品询价
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "商品询价")
public class ProductPriceInquiryDTO {
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "商品id")
    private Integer productId;
}
