package com.tigshop.bean.query.salesman;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销员分组
 *
 * @author kidd
 * @since 2025/6/21 09:36
 */
@Schema(description = "分销员分组列表查询条件")
@Data
public class SalesmanGroupPageQuery extends BasePage {

    @Schema(description = "组名称")
    private String groupName;

}
