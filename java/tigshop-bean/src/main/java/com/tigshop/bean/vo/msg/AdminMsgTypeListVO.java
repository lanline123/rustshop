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

import java.util.List;

/**
 * 管理员消息VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "管理员消息参数")
public class AdminMsgTypeListVO {
    @Schema(description = "分类id")
    private Integer catId;

    @Schema(description = "分类名")
    private String catName;

    @Schema(description = "未读数量")
    private Integer unreadCount;

    @Schema(description = "子分类")
    private List<AdminMsgTypeListChildVO> child;

    @Data
    public static class AdminMsgTypeListChildVO {
        @Schema(description = "类型id")
        private Integer msgType;

        @Schema(description = "分类名")
        private String name;

        @Schema(description = "未读数量")
        private Integer unreadCount;
    }

}