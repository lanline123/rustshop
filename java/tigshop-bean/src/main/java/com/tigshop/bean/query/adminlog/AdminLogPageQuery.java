package com.tigshop.bean.query.adminlog;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员日志列表查询参数
 *
 * @author kidd
 * @since 2025/3/31 17:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理员日志列表查询参数")
public class AdminLogPageQuery extends BasePage {

    @Schema(description = "关键字；日志信息/IP地址")
    private String keyword;


}
