package com.tigshop.service.product.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.o2o.stockcalendar.DateExclusionDTO;
import com.tigshop.bean.dto.o2o.stockcalendar.ShopSelectionDTO;
import com.tigshop.bean.enums.product.ShopSelectionTypeEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存日历工具类
 * 用于处理门店选择和日期过滤逻辑
 *
 * @author Tigshop项目组
 * @create 2025年01月01日
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StockCalendarUtil {

    private final ShopService shopService;
    private final ProductService productService;
    
    /**
     * 周期购商品默认可预约天数（90天）
     */
    private static final int CYCLE_ORDER_DEFAULT_DAYS = 90;


    /**
     * 根据日期排除配置过滤日期列表
     *
     * @param dateList      原始日期列表
     * @param dateExclusion 日期排除配置
     * @return 过滤后的日期列表
     */
    public List<String> getFilteredDates(List<String> dateList, DateExclusionDTO dateExclusion) {
        if (dateList == null || dateList.isEmpty()) {
            return new ArrayList<>();
        }

        if (dateExclusion == null) {
            // 没有排除配置，返回原始列表
            return new ArrayList<>(dateList);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> filteredDates = new ArrayList<>();

        for (String dateStr : dateList) {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            boolean shouldExclude = false;

            // 检查是否需要按周排除
            if (dateExclusion.getExcludeWeekdays() != null && !dateExclusion.getExcludeWeekdays().isEmpty()) {
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                int weekdayValue = dayOfWeek.getValue(); // 1=Monday, 7=Sunday
                if (dateExclusion.getExcludeWeekdays().contains(weekdayValue)) {
                    shouldExclude = true;
                    log.debug("日期{}因周{}被排除", dateStr, weekdayValue);
                }
            }

            // 检查是否需要按月排除
            if (!shouldExclude && dateExclusion.getExcludeMonthDays() != null && !dateExclusion.getExcludeMonthDays().isEmpty()) {
                int dayOfMonth = date.getDayOfMonth();
                if (dateExclusion.getExcludeMonthDays().contains(dayOfMonth)) {
                    shouldExclude = true;
                    log.debug("日期{}因月{}号被排除", dateStr, dayOfMonth);
                }
            }

            // 检查是否需要按具体日期排除
            if (!shouldExclude && dateExclusion.getExcludeSpecificDates() != null && !dateExclusion.getExcludeSpecificDates().isEmpty()) {
                if (dateExclusion.getExcludeSpecificDates().contains(dateStr)) {
                    shouldExclude = true;
                    log.debug("日期{}被具体排除", dateStr);
                }
            }

            if (!shouldExclude) {
                filteredDates.add(dateStr);
            }
        }

        log.info("日期过滤完成，原始{}个日期，过滤后{}个日期", dateList.size(), filteredDates.size());
        return filteredDates;
    }

    /**
     * 获取日期范围内的所有日期
     *
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate   结束日期 (yyyy-MM-dd)
     * @return 日期列表
     */
    public List<String> getDateRange(String startDate, String endDate) {
        List<String> dateList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            dateList.add(current.format(formatter));
            current = current.plusDays(1);
        }

        return dateList;
    }

    /**
     * 获取商品分配所有门店ID列表
     *
     * @return 所有门店ID列表
     */

    /**
     * 处理时间范围，支持两种格式：
     * 1. yyyy-MM：查询某个月的（如果是当月则从今天开始到本月结束）
     * 2. yyyy-MM-dd 到 yyyy-MM-dd：查询某个时间范围的
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 处理后的时间范围数组 [actualStartDate, actualEndDate]
     */
    public String[] processDateRange(String startDate, String endDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 检查startDate的格式
        if (startDate.matches("\\d{4}-\\d{2}")) {
            // yyyy-MM格式，查询某个月的
            YearMonth queryMonth = YearMonth.parse(startDate, monthFormatter);
            YearMonth currentMonth = YearMonth.now();
            LocalDate today = LocalDate.now();

            String actualStartDate;
            String actualEndDate;

            if (queryMonth.equals(currentMonth)) {
                // 当月：从今天开始到本月结束
                actualStartDate = today.format(dateFormatter);
                actualEndDate = queryMonth.atEndOfMonth().format(dateFormatter);
                log.info("查询当月库存：从今天({})开始到本月结束({})", actualStartDate, actualEndDate);
            } else {
                // 非当月：从月初到月末
                actualStartDate = queryMonth.atDay(1).format(dateFormatter);
                actualEndDate = queryMonth.atEndOfMonth().format(dateFormatter);
                log.info("查询{}月库存：从月初({})到月末({})", startDate, actualStartDate, actualEndDate);
            }

            return new String[]{actualStartDate, actualEndDate};
        } else if (startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // yyyy-MM-dd格式，查询时间范围
            if (endDate == null || endDate.trim().isEmpty()) {
                throw new GlobalException("当开始日期为yyyy-MM-dd格式时，结束日期不能为空");
            }

            if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new GlobalException("结束日期格式不正确，应为yyyy-MM-dd");
            }

            log.info("查询时间范围库存：从{}到{}", startDate, endDate);
            return new String[]{startDate, endDate};
        } else {
            throw new GlobalException("开始日期格式不正确，应为yyyy-MM或yyyy-MM-dd");
        }
    }

    /**
     * 根据商品和门店的预约模板计算客户端可预约的时间范围
     *
     * @param productId 商品ID
     * @param shopId    门店ID
     * @return 时间范围数组 [开始日期, 结束日期]
     */


}