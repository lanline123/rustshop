// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.msg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页消息摘要VO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "首页消息摘要")
public class HomeMsgSummaryVO {

    @Schema(description = "未读消息总数（跨所有分类）")
    private Integer unreadCount;

    @Schema(description = "最新一条消息摘要；无任何消息时为 null")
    private LatestMessage latestMessage;

    /**
     * 最新一条消息摘要
     */
    @Data
    @Schema(description = "最新一条消息摘要")
    public static class LatestMessage {
        @Schema(description = "消息ID")
        private Integer msgId;

        @Schema(description = "消息类型编号（二级分类编号）")
        private Integer msgType;

        @Schema(description = "二级分类名，与 msgTypeArr 的 child.name 一致")
        private String typeName;

        @Schema(description = "消息展示文本")
        private String title;

        @Schema(description = "发送时间，格式 yyyy-MM-dd HH:mm:ss")
        private String sendTime;
    }
}
