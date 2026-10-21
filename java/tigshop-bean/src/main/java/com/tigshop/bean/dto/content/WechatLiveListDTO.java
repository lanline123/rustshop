// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.content;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信直播列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "微信直播列表参数")
public class WechatLiveListDTO extends BasePage {
    @Schema(description = "直播标题")
    private String wechatLiveTitle;

    @Schema(description = "直播状态")
    private Integer liveStatus;

    @Schema(description = "创建开始时间，格式：yyyy-MM-dd")
    private String startTime;

    @Schema(description = "创建结束时间，格式：yyyy-MM-dd")
    private String endTime;
}
