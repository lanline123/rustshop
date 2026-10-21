package com.tigshop.bean.query.salesman;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分销员排行列表查询条件
 *
 * @author kidd
 * @since 2025/6/21
 */
@Schema(description = "分销员排行列表查询条件")
@EqualsAndHashCode(callSuper = true)
@Data
public class SalesmanRankingPageQuery extends BasePage {

    @Schema(description = "日期类型")
    private Integer dateType;

    @Schema(description = "开始时间")
    private String startEndTime;

    @Schema(description = "查询开始时间", hidden = true)
    private Long searchStartTime;

    @Schema(description = "查询结束时间", hidden = true)
    private Long searchEndTime;

    public void handleSearchTime() {
        List<String> times = StrUtil.split(startEndTime, StrUtil.COMMA);

        // 获取时间
        long[] startEndDate = new long[2];
        if (times.size() == 2) {
            String startTime = times.getFirst().isEmpty() ? times.getLast() : times.getFirst();
            String endTime = times.getLast().isEmpty() ? times.getFirst() : times.getLast();
            startEndDate = TigUtils.getDateRange(this.dateType, startTime, endTime);
        }
        if (times.size() == 1) {
            startEndDate = TigUtils.getDateRange(this.dateType, times.getFirst(), times.getFirst());
        }
        this.searchStartTime = startEndDate[0];
        this.searchEndTime = startEndDate[1];
    }

}
