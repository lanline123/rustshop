package com.tigshop.bean.query.panel;

import cn.hutool.core.date.DateUtil;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Tigshop
 * @create 2025年03月03日 16:09
 */
@Data
@Schema(description = "用户")
public class UserTrendsQuery {

    @NotBlank(message = "请选择日期")
    @Schema(description = "开始时间")
    private String startEndTime;

    @NotNull(message = "数据类型不能为空")
    @Schema(description = "数据类型")
    private Integer dateType;

    @Schema(description = "是否导出")
    private Integer isExport;

    @Schema(description = "店铺ID")
    private Integer shopId;

    public String[] transStartEndTimeStr() {
        return  TigUtils.getRangeDate(this.getDateType(), this.startEndTime, this.startEndTime);
    }

    public Long[] transStartEndTime() {
        String[] startEndTime = TigUtils.getRangeDate(this.getDateType(), this.startEndTime, this.startEndTime);
        return new Long[]{DateUtil.parse(startEndTime[0]).getTime() / 1000, DateUtil.parse(startEndTime[1]).getTime() / 1000};
    }
}
