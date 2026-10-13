// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "客服参数")
public class ImServantVO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "客服ID")
    private Integer servantId;

    @Schema(description ="最后更新时间")
    private String lastUpdateTime;

    @Schema(description ="添加时间")
    private String addTime;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="店铺id")
    private Integer shopId;

    @Schema(description ="会话数量")
    private Long conversationNum;

    @Schema(description ="用户信息")
    private UserVO user;

    @Data
    @Schema(description = "用户信息")
    public static class UserVO {
        @Schema(description = "用户ID")
        private Integer adminId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "头像")
        private String avatar;
    }
}