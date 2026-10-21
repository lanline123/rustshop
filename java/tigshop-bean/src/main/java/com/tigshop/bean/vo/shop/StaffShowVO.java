// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.shop;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/21 16:04
 */
@Data
@Schema(description = "员工展示信息")
public class StaffShowVO {

    @Schema(description = "已有员工数")
    private Integer usingUser;
    @Schema(description = "停用员工数")
    private Integer stopUsingUser;
    @Schema(description = "取店铺对应的管理员")
    private Integer residue;
    @Schema(description = "最近员工操作")
    private List<AdminLogVO> adminLog;

    @Data
    public static class AdminLogVO {
        @Schema(description = "日志ID")
        private Integer logId;
        @Schema(description = "日志时间")
        private String logTime;
        @Schema(description = "操作人ID")
        private Integer userId;
        @Schema(description = "日志详情")
        private String logInfo;
        @Schema(description = "ip")
        private String ipAddress;
        @Schema(description = "操作人名称")
        private String username;
    }
}
