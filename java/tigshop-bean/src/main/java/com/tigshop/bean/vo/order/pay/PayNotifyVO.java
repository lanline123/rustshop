// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 支付页面VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "支付页面参数")
public class PayNotifyVO {

    @Schema(description = "code")
    private String code;

    @Schema(description = "message")
    private String message;

    public PayNotifyVO(String code, String message) {
        this.code = code;
        this.message = message;
    }

}