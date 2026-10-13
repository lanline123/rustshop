// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信直播推流信息VO
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "微信直播推流信息VO")
public class WechatLivePushUrlVO {

    @Schema(description = "直播间ID")
    private Integer roomId;

    @Schema(description = "推流地址")
    private String pushUrl;

    @Schema(description = "开播码")
    private String liveCode;

    @Schema(description = "直播间名称")
    private String roomName;
}
