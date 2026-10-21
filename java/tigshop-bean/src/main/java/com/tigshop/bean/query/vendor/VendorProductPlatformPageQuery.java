package com.tigshop.bean.query.vendor;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台端供应商商品列表查询条件
 *
 * @author kidd
 * @since 2025/7/10 09:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class  VendorProductPlatformPageQuery extends BasePage {

    @Schema(description = "商品品牌ID")
    private Integer productBrandId;

    @Schema(description = "商品类目ID")
    private Integer categoryId;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    @Schema(description = "是否回收")
    private Integer isRecycle;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "开始创建时间")
    private String searchStartTime;

    @Schema(description = "结束创建时间")
    private String searchEndTime;

    @Schema(description = "开始创建时间", hidden = true)
    private Long startCreateTime;

    @Schema(description = "结束创建时间", hidden = true)
    private Long endCreateTime;

    public void handleSearchTime() {
        if (StrUtil.isNotBlank(this.searchStartTime)) {
            this.startCreateTime = TigUtils.toTimestampYmd(this.searchStartTime);
        }

        if (StrUtil.isNotBlank(this.searchEndTime)) {
            this.endCreateTime = TigUtils.toTimestampYmd(this.searchEndTime);
        }
    }
}
