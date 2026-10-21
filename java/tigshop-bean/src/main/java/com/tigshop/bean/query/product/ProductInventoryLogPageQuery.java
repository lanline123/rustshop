package com.tigshop.bean.query.product;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存日志列表
 *
 * @author kidd
 * @since 2025/6/30
 */
@Setter
@Getter
@Schema(description = "库存日志列表参数")
public class ProductInventoryLogPageQuery extends BasePage {

    @Schema(description = "类型")
    private Integer type;

    private Integer shopId;

    public void init() {
        if (StrUtil.isBlank(this.getSortField()) && StrUtil.isBlank(this.getSortOrder())) {
            this.setSortField("product_inventory_log.add_time");
            this.setSortOrder("desc");
        }
    }

}
