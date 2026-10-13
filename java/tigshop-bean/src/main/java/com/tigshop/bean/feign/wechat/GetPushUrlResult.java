// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获取推流地址响应
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "获取推流地址响应")
public class GetPushUrlResult {

    @Schema(description = "返回码")
    private Long errcode;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "推流地址")
    private String pushUrl;

    @Schema(description = "开播码")
    private String liveCode;
}
