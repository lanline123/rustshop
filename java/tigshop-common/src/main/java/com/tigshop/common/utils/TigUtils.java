// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 获取Tig通用方法
 *
 * @author Jayce
 * @create 2024年10月28日 17:46:56
 */
public class TigUtils {


    public final static String YYYY_MM_DD_FORMAT = "yyyy-MM-dd";

    /**
     * 获取增长率
     *
     * @param prev    上次时间
     * @param current 本次时间
     * @return BigDecimal
     */
    public static BigDecimal getGrowthRate(BigDecimal prev, BigDecimal current) {
        if (Objects.isNull(prev) || Objects.isNull(current)) {
            return BigDecimal.ZERO;
        }
        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }
        return current.subtract(prev)
                      .divide(prev, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取时间 x 轴
     *
     * @param dateType  时间类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return string[]
     */
    public static String[] getRangeDateX(Integer dateType, String startTime, String endTime) {
        List<String> horizontalAxis = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (dateType) {
            case 1 -> {
                // 年 -> 显示月份（从1月到 endDate 的月份）
                int currentYear = LocalDate.now()
                                           .getYear();
                int currentMonth = 12;
                if (currentYear == Integer.parseInt(endTime)) {
                    LocalDate end = LocalDate.now();
                    currentMonth = end.getMonthValue();
                }
                for (int i = 1; i <= currentMonth; i++) {
                    horizontalAxis.add(String.format("%02d", i));
                }
            }
            case 2 -> {
                // 月 -> 显示该月的所有日期（1 到 N天）
                YearMonth inputMonth = YearMonth.parse(startTime);
                YearMonth currentMonth = YearMonth.now();
                // 返回传入月份有多少天
                int daysInMonth = inputMonth.lengthOfMonth();
                if (inputMonth.equals(currentMonth)) {
                    LocalDate date = LocalDate.parse(startTime, dateFormatter);
                    daysInMonth = date.lengthOfMonth();
                }
                for (int i = 1; i <= daysInMonth; i++) {
                    horizontalAxis.add(String.valueOf(i));
                }
            }
            case 3 -> {
                // 日 -> 显示 0 到 23 小时
                for (int i = 0; i < 24; i++) {
                    horizontalAxis.add(String.valueOf(i));
                }
            }
            default -> {
                // 自定义 -> 从 startDate 到 endDate 每一天
                LocalDate current = LocalDate.parse(startTime, dateFormatter);
                LocalDate end = LocalDate.parse(endTime, dateFormatter);
                while (!current.isAfter(end)) {
                    horizontalAxis.add(current.format(dateFormatter));
                    current = current.plusDays(1);
                }
            }
        }
        return horizontalAxis.toArray(new String[0]);
    }

    /**
     * 获取时间范围
     *
     * @param dateType  时间类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return string[]
     */
    public static String[] getRangeDate(Integer dateType, String startTime, String endTime) {
        String startDate;
        String endDate;
        if (startTime.matches("\\d{4}-\\d{2}")) {
            startTime = startTime + "-01";
        } else if (startTime.matches("\\d{4}")) {
            startTime = startTime + "-01-01";
        }
        DateTime date = new DateTime(startTime);
        Year year = Year.of(date.year());
        String yearMonth = date.toString("yyyy-MM");
        switch (dateType) {
            case 1 -> {
                // 年  -- 显示月份
                startDate = year + "-01-01";
                endDate = year + "-12-31";
            }
            case 2 -> {
                // 月 -- 显示日期
                startDate = yearMonth + "-01";
                endDate = yearMonth + "-" + DateUtil.lengthOfMonth(date.month() + 1, date.isLeapYear());
            }
            // 日 -- 显示24小时
            case 3 -> {
                startDate = startTime + " 00:00:00";
                endDate = endTime + " 23:59:59";
            }
            default -> {
                startDate = startTime;
                endDate = endTime;
            }
        }
        return new String[]{startDate, endDate};
    }

    /**
     * BufferedImage 转 Base64
     *
     * @param image BufferedImage
     * @return Base64
     */
    public static String bufferedImageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            byte[] imageBytes = bos.toByteArray();

            return Base64.getEncoder()
                         .encodeToString(imageBytes);
        } catch (IOException e) {
            throw new GlobalException("图片转换失败");
        }
    }

    /**
     * 时间戳转时间格式
     *
     * @param time 时间戳
     * @return String
     */
    public static String handelTime(Long time) {
        if (time == null || time == 0) {
            return "";
        }
        long timestampInSeconds = time;
        Date date = new Date(timestampInSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String handelTime(Long time, String format) {
        if (time == null || time == 0) {
            return "";
        }
        long timestampInSeconds = time;
        Date date = new Date(timestampInSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 将前端日期字符串解析为 LocalDate，兼容多种格式
     * 支持：yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / yyyy/MM/dd / yyyy.MM.dd / ISO 格式 / 时间戳
     *
     * @param dateStr 日期字符串
     * @return LocalDate，解析失败返回 null
     */
    public static LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String value = dateStr.trim();

        if (value.matches("^\\d+$")) {
            try {
                long num = Long.parseLong(value);
                long seconds = num > 9999999999L ? num / 1000 : num;
                return Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception ignored) {
            }
        }

        String datePart = value.length() >= 10 ? value.substring(0, 10) : value;
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ISO_LOCAL_DATE
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(datePart, fmt);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 将前端日期字符串解析为 LocalDateTime，兼容多种格式
     * 支持：yyyy-MM-dd HH:mm:ss / yyyy-MM-dd / 时间戳
     *
     * @param dateStr 日期时间字符串
     * @return LocalDateTime，解析失败返回 null
     */
    public static LocalDateTime parseLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String value = dateStr.trim();

        if (value.matches("^\\d+$")) {
            try {
                long num = Long.parseLong(value);
                long seconds = num > 9999999999L ? num / 1000 : num;
                return Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception ignored) {
            }
        }

        DateTimeFormatter[] dtFormatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        for (DateTimeFormatter fmt : dtFormatters) {
            try {
                return LocalDateTime.parse(value, fmt);
            } catch (Exception ignored) {
            }
        }

        LocalDate date = parseLocalDate(value);
        if (date != null) {
            return date.atStartOfDay();
        }
        return null;
    }

    /**
     * 时间戳转时间格式
     *
     * @param dateStr 时间戳
     * @return String
     */
    public static long toTimestampYmd(String dateStr) {
        return DateUtil.parse(dateStr, "yyyy-MM-dd")
                       .getTime() / 1000;
    }

    public static long toTimestampYmdHms(String dateStr) {
        return DateUtil.parse(dateStr, "yyyy-MM-dd HH:mm:ss")
                       .getTime() / 1000;
    }

    public static long toTimestampYmdHm(String dateStr) {
        return DateUtil.parse(dateStr, "yyyy-MM-dd HH:mm")
                .getTime() / 1000;
    }

    /**
     * 将浮点转为2位小数
     */
    public static BigDecimal toDecimalConvert(BigDecimal decimal) {
        if (decimal == null) {
            return BigDecimal.ZERO;
        }
        return decimal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 根据传入的type 1 返回最近三个月的开始和结束时间戳, 2返回最近六个月的开始和结束时间戳, 3返回最近一年的开始和结束时间戳, 4返回最近一天的开始和结束时间戳
     */
    public static Long[] getStartAndEndTime(Integer type) {
        Long[] startEndTime = new Long[2];
        long startTime = StringUtils.getCurrentTime();
        long endTime = switch (type) {
            case 1 -> startTime - 30L * 24 * 60 * 60 * 1000;
            case 2 -> startTime - 60L * 24 * 60 * 60 * 1000;
            case 3 -> startTime - 365L * 24 * 60 * 60 * 1000;
            case 4 -> startTime - 24L * 60 * 60 * 1000;
            default -> 0L;
        };
        startEndTime[0] = startTime;
        startEndTime[1] = endTime;
        return startEndTime;
    }

    /**
     * 从header头中解析token并判断是否是前后台，用于im服务
     */
    public static Boolean getClientTypeByToken() {
        // 获取当前请求
        final String authorization = HeaderUtils.getHeaderValue("Authorization");
        // 去掉 Bearer
        if (authorization == null) {
            throw new GlobalException("token异常");
        }
        final String token = authorization.replaceAll(Constants.TOKEN_PREFIX, "");
        JWT jwt = JWTUtil.parseToken(token);
        JWTPayload payload = jwt.getPayload();
        String isAdmin = payload.getClaim("isAdmin")
                                .toString();
        return "true".equals(isAdmin);
    }

    /**
     * 获取jar所在目录
     */
    public static String getJarDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取指定 dateType (0-自定义[yyyy-MM-dd], 1-年[yyyy], 2-月[yyyy-MM], 3-日[yyyy-MM-dd]) 对应日期的最小和最大时间。
     *
     * @param dateType     类型，0 = 自定义(天), 1 = 年, 2 = 月, 3 = 日
     * @param startDataStr 起始日期字符串
     * @param endDataStr   结束日期字符串
     * @return long[]{startTime, endTime}
     */
    public static long[] getDateRange(int dateType, String startDataStr, String endDataStr) {
        switch (dateType) {
            case 0:
                // 自定义
                LocalDate customStartDate = LocalDate.parse(startDataStr);
                LocalDate customEndDate = LocalDate.parse(endDataStr);
                return new long[]{customStartDate.atStartOfDay(ZoneId.systemDefault())
                                                 .toInstant().getEpochSecond(), customEndDate.atTime(LocalTime.MAX)
                                                                                             .atZone(ZoneId.systemDefault())
                                                                                             .toInstant().getEpochSecond()};
            case 1:
                // 年
                LocalDate yearStartData = LocalDate.parse(startDataStr + "-01-01");
                LocalDate yearEndData = LocalDate.parse(endDataStr + "-12-31");
                return new long[]{yearStartData.with(TemporalAdjusters.firstDayOfYear())
                                               .atStartOfDay(ZoneId.systemDefault())
                                               .toInstant().getEpochSecond(), yearEndData.with(TemporalAdjusters.lastDayOfYear())
                                                                                         .atTime(LocalTime.MAX)
                                                                                         .atZone(ZoneId.systemDefault())
                                                                                         .toInstant().getEpochSecond()};
            case 2:
                // 月
                YearMonth monthStartData = YearMonth.parse(startDataStr);
                YearMonth monthEndData = YearMonth.parse(endDataStr);
                return new long[]{monthStartData.atDay(1)
                                                .atStartOfDay(ZoneId.systemDefault())
                                                .toInstant().getEpochSecond(), monthEndData.atEndOfMonth()
                                                                                           .atTime(LocalTime.MAX)
                                                                                           .atZone(ZoneId.systemDefault())
                                                                                           .toInstant().getEpochSecond()};
            case 3:
                // 日
                LocalDate dayStartData = LocalDate.parse(startDataStr);
                LocalDate dayEndData = LocalDate.parse(endDataStr);
                return new long[]{dayStartData.atStartOfDay(ZoneId.systemDefault())
                                              .toInstant().getEpochSecond(), dayEndData.atTime(LocalTime.MAX)
                                                                                       .atZone(ZoneId.systemDefault())
                                                                                       .toInstant().getEpochSecond()};
            default:
                throw new IllegalArgumentException("不支持的 dateType: " + dateType);
        }
    }

    /**
     * 获取指定月份的日期列表
     *
     * @param yearMonthStr 月份字符串，格式为"yyyy-MM"
     * @return List<String>
     */
    public static List<String> getMonthDays(String yearMonthStr) {
        // 格式化器
        DateTimeFormatter ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 解析为 YearMonth
        YearMonth ym = YearMonth.parse(yearMonthStr, ymFormatter);
        YearMonth currentMonth = YearMonth.now();
        int lastDay = ym.lengthOfMonth();
        if (ym.equals(currentMonth)) {
            lastDay = LocalDate.now().getDayOfMonth();
        }
        List<String> days = new ArrayList<>();
        for (int day = 1; day <= lastDay; day++) {
            LocalDate date = ym.atDay(day);
            days.add(date.format(dayFormatter));
        }
        return days;
    }

    /**
     * 获取指定年份的月份
     *
     * @param year 年份
     * @return List<Integer>
     */
    public static List<String> getMonthByYear(String year) {
        List<String> months = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.now();
        int currentYear = currentYearMonth.getYear();
        int lastMonth = 12;

        if (year.equals(String.valueOf(currentYear))) {
            lastMonth = currentYearMonth.getMonthValue();
        }

        for (int month = 1; month <= lastMonth; month++) {
            months.add(year + "-" + month);
        }
        return months;
    }

}
