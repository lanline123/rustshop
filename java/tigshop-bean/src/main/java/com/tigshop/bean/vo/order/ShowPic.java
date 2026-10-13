// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.order;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/4/21 9:58
 */
@Data
@Schema(description = "订单商品图片")
public class ShowPic {
    @Schema(description = "图片id")
    private Integer picId;
    @Schema(description = "图片名称")
    private String picName;
    @Schema(description = "缩略图")
    private String picThumb;
    @Schema(description = "图片地址")
    private String picUrl;
}
