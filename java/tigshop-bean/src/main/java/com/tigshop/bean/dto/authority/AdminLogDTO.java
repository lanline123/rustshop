// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.authority;

import com.tigshop.bean.model.log.RequestLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员日志
 *
 * @author Jayce
 * @create 2024年10月28日 11:14
 */
@Data
@Schema(name = "管理员日志")
public class AdminLogDTO {
    @Schema(description = "日志ID")
    private Integer logId;

    @Schema(description = "日志时间")
    private String logTime;

    @Schema(description = "管理员ID")
    private Integer userId;

    @Schema(description = "日志详情")
    private String logInfo;

    @Schema(description = "ip")
    private String ipAddress;

    @Schema(description = "管理员名称")
    private String username;

    @Schema(description = "请求日志")
    private RequestLog requestLog;

}
