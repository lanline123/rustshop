package com.tigshop.bean.dto.o2o.stockcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "日期排除配置")
public class DateExclusionDTO {
    
    /**
     * 按周排除的星期列表
     * 1=周一，2=周二，3=周三，4=周四，5=周五，6=周六，7=周日
     */
    @Schema(description = "按周排除的星期列表，1=周一，2=周二，3=周三，4=周四，5=周五，6=周六，7=周日")
    private List<Integer> excludeWeekdays;
    
    /**
     * 按月排除的日期列表
     * 1-31表示每月的第几号
     */
    @Schema(description = "按月排除的日期列表，1-31表示每月的第几号")
    private List<Integer> excludeMonthDays;
    
    /**
     * 排除的具体日期列表
     * 格式：yyyy-MM-dd
     */
    @Schema(description = "排除的具体日期列表，格式：yyyy-MM-dd")
    private List<String> excludeSpecificDates;
}