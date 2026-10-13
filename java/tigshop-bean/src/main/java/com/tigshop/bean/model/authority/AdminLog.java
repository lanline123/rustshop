// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员日志
 *
 * @author Jayce
 * @create 2024年10月25日 15:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员日志")
@TableName("admin_log")
public class AdminLog {
    @Schema(description = "日志ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    @Schema(description = "日志时间")
    @TableField(value = "log_time")
    private Long logTime;

    @Schema(description = "管理员ID")
    @TableField(value = "user_id")
    private Integer userId;

    @Schema(description = "操作作用域类型")
    @TableField(value = "scope_type")
    private String scopeType;

    @Schema(description = "操作作用域ID")
    @TableField(value = "scope_id")
    private Integer scopeId;

    @Schema(description = "管理员日志内容")
    @TableField(value = "log_info")
    private String logInfo;

    @Schema(description = "管理员登录地址")
    @TableField(value = "ip_address")
    private String ipAddress;

    @Schema(description = "请求日志id")
    @TableField(value = "request_log_id")
    private Integer requestLogId;
}
