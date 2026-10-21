package com.tigshop.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.TokenizerEngine;
import cn.hutool.extra.tokenizer.TokenizerUtil;
import cn.hutool.extra.tokenizer.Word;
import com.tigshop.common.exception.GlobalException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串工具类
 *
 * @author Tigshop团队
 * @create 2024年12月17日 10:06
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     * 空字符串
     */
    private static final String NULL_STR = "";

    /**
     * 下划线
     */
    private static final char SEPARATOR = '_';

    /**
     * 星号
     */
    private static final char ASTERISK = '*';

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str) {
        return str == null || NULL_STR.equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 替换指定字符串的指定区间内字符为"*"
     *
     * @param str          字符串
     * @param startInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @return 替换后的字符串
     */
    public static String hide(CharSequence str, int startInclude, int endExclude) {
        if (isEmpty(str)) {
            return NULL_STR;
        }
        final int strLength = str.length();
        if (startInclude > strLength) {
            return NULL_STR;
        }
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            // 如果起始位置大于结束位置，不替换
            return NULL_STR;
        }
        final char[] chars = new char[strLength];
        for (int i = 0; i < strLength; i++) {
            if (i >= startInclude && i < endExclude) {
                chars[i] = ASTERISK;
            } else {
                chars[i] = str.charAt(i);
            }
        }
        return new String(chars);
    }

    /**
     * 将字符串中间二分之一部分替换为 *
     * @param str 原始字符串
     * @return 处理后的字符串
     */
    public static String maskMiddleHalf(String str) {
        if (str == null) {
            return null;
        }

        if (StrUtil.isBlank(str) || str.length() < 2) {
            return StrUtil.repeat('*', str.length());
        }

        int length = str.length();
        int half = length / 2;

        int start = (length - half) / 2;
        int end = start + half;

        return StrUtil.replace(str, start, end, StrUtil.repeat('*', end - start));
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @param end   结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return NULL_STR;
        }

        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return NULL_STR;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 将字符串转成数组
     *
     * @param str 字符串
     * @return list集合
     */
    public static String[] str2Array(String str) {
        // 去掉方括号并分割字符串
        str = str.trim();
        if (str.startsWith("[") && str.endsWith("]")) {
            // 去掉开头和结尾的方括号和双引号
            str = str.substring(1, str.length() - 1);
        }
        str = str.replace("\"", "");
        // 使用逗号分割字符串，支持逗号后有空格的情况
        return str.split(",\\s*");
    }

    /**
     * 将字符串转成字符串集合
     */
    public static List<String> str2List(String arr) {
        if (StrUtil.isEmpty(arr)) {
            return new ArrayList<>();
        }
        // 去掉方括号
        arr = arr.replace("[", "").replace("]", "");
        if (StrUtil.isEmpty(arr)) {
            return new ArrayList<>();
        }
        // 按逗号分割
        String[] stringArray = arr.split(",");
        // 转换为 List<Integer>
        return Arrays.stream(stringArray)
                .map(String::trim)
                .toList();
    }

    /**
     * 字符串转int数组
     *
     * @param arr 数组字符串
     * @return List<Integer>
     */
    public static List<Integer> str2IntList(String arr) {
        if (StrUtil.isEmpty(arr)) {
            return new ArrayList<>();
        }
        // 去掉方括号
        arr = arr.replace("[", "").replace("]", "");
        if (StrUtil.isEmpty(arr.replace(" ", ""))) {
            return new ArrayList<>();
        }
        // 按逗号分割
        String[] stringArray = arr.split(",");
        // 转换为 List<Integer>
        return Arrays.stream(stringArray)
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

    }

    /**
     * 随机文件名生成方法
     *
     * @return String
     */
    public static String randomFileName() {
        return getCurrentTime() + getFileName();
    }


    public static String getFileName() {
        String pattern = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLOMNOPQRSTUVWXYZ";
        StringBuilder str = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < 18; i++) {
            int index = random.nextInt(pattern.length());
            str.append(pattern.charAt(index));
        }
        return str.toString();
    }

    /**
     * 清洗上传文件的原始文件名，得到安全的基础名（不含扩展名）。
     * 去除路径、路径穿越片段与非法字符；结果为空时回退随机名。
     * 用于「保留原素材名」上传设置。
     *
     * @param originalName 原始文件名
     * @return 安全的文件基础名（不含扩展名）
     */
    public static String sanitizeFileBaseName(String originalName) {
        if (isBlank(originalName)) {
            return getFileName();
        }
        // 只取文件名部分，去除可能携带的路径
        String name = originalName.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        // 去掉扩展名
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        // 过滤路径穿越与非法/控制字符
        name = name.replace("..", "_")
                .replaceAll("[\\\\/:*?\"<>|\\x00-\\x1F]", "_")
                .trim();
        // 折叠连续空白为单个下划线
        name = name.replaceAll("\\s+", "_");
        // 限制长度，避免存储路径过长
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        if (isBlank(name)) {
            return getFileName();
        }
        return name;
    }

    /**
     * 生成用于避免同名覆盖的短后缀（时间戳 + 6 位随机字符）。
     *
     * @return 短后缀
     */
    public static String shortUniqueSuffix() {
        String pattern = "1234567890abcdefghijklmnopqrstuvwxyz";
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(pattern.charAt(random.nextInt(pattern.length())));
        }
        return getCurrentTime() + str.toString();
    }

    /**
     * 获取当前时间戳(秒级)
     *
     * @return Long
     */
    public static Long getCurrentTime() {
        // 获取当前时间戳
        return System.currentTimeMillis() / 1000;
    }

    public static Long getTodayBeginTime() {
        // 获取当前时间戳
        return DateUtil.beginOfDay(new Date()).getTime() / 1000;
    }

    public static Long getTodayEndTime() {
        // 获取当前时间戳
        return DateUtil.endOfDay(new Date()).getTime() / 1000;
    }

    /**
     * 日期转时间戳
     *
     * @param dateTimeString 日期时间字符串
     * @return Long
     */
    public static Long dateToTimestampExample(String dateTimeString) {
        // 如果没有时间部分，自动补上
        if (!dateTimeString.contains(":")) {
            dateTimeString = dateTimeString.trim() + " 00:00:00";
        } else if (dateTimeString.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
            // 只有到分钟，补秒
            dateTimeString = dateTimeString.trim() + ":00";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;
    }

    /**
     * 搜索文本分词
     *
     * @param text 文本
     * @return String
     */
    public static String cutForSearch(String text) {
        //自动根据用户引入的分词库的jar来自动选择使用的引擎
        TokenizerEngine engine = TokenizerUtil.createEngine();

        //解析文本
        Result result = engine.parse(text);
        // 将 Iterator 转换为 Stream 并拼接为字符串
        return Stream.of(result)
                .flatMap(r -> CollUtil.newArrayList((Iterator<Word>) r).stream())
                .filter(word -> !StrUtil.isBlank(word.getText()))
                .map(word -> StrUtil.trim(word.getText()))
                .collect(Collectors.joining(" "));
    }

    /**
     * 获取当前时间年月
     *
     * @return String
     */
    public static String getCurrentYearMonth() {
        // 获取当前日期
        LocalDate now = LocalDate.now();

        // 使用DateTimeFormatter指定格式为YYYYMM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        // 格式化当前日期
        return now.format(formatter);
    }

    /**
     * 移除字符串前缀
     *
     * @param str    原始字符串
     * @param prefix 标识位置
     * @return String
     */
    public static String removePrefix(String str, String prefix) {
        // 找到 "prefix" 的起始位置
        int index = str.indexOf(prefix);

        // 使用 substring 方法获取从 "prefix" 开始的部分
        return str.substring(index);
    }

    /**
     * 获取环比时间区间
     *
     * @param startEndTime 日期范围
     * @param type         类型
     * @return 环比时间区间
     */
    public static String[] getPrevDate(String[] startEndTime, int type) {
        if (startEndTime == null || startEndTime.length != 2) {
            return new String[]{};
        }

        String startDateStr = startEndTime[0];
        String endDateStr = startEndTime[1];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        LocalDate prevStartDate;
        LocalDate prevEndDate;

        switch (type) {
            case 1:
                // 年
                prevStartDate = startDate.minusYears(1);
                prevEndDate = endDate.minusYears(1);
                break;
            case 2:
                // 月
                prevStartDate = startDate.minusMonths(1);
                prevEndDate = endDate.minusMonths(1);
                break;
            case 3:
                // 日
                prevStartDate = startDate.minusDays(1);
                prevEndDate = endDate.minusDays(1);
                break;
            case 4:
                // 具体时间区间
                long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                prevStartDate = startDate.minusDays(daysBetween);
                prevEndDate = endDate.minusDays(daysBetween);
                break;
            default:
                return new String[]{};
        }

        return new String[]{prevStartDate.toString(), prevEndDate.toString()};
    }

    public static String getRandomString(int count) {
        String pattern = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLOMNOPQRSTUVWXYZ";
        StringBuilder str = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(pattern.length());
            str.append(pattern.charAt(index));
        }
        return str.toString();
    }

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /** 生成唯一卡号 */
    public static String generateCardNo(Integer groupId) {
        return generateCardNo("GC", groupId);
    }

    /**
     * 按指定业务前缀生成卡号。
     * 格式：前缀 + yyyyMMdd + 分组 ID + 4 位随机字符。
     */
    public static String generateCardNo(String prefix, Integer groupId) {
        return prefix + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + groupId
                + RandomUtil.randomString(CHARSET, 4);
    }

    /** 生成密码 */
    public static String generatePassword() {
        return RandomUtil.randomString(CHARSET, 8);
    }

    // 卡密脱敏
    public static String maskCardNumber(String cardNumber) {
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * 将指定日期转换为当天 23:59:59 的时间戳
     *
     * @param dateStr 日期字符串，格式应与解析格式一致
     * @return 当天最后一秒的时间戳 10位时间戳
     */
    public static Long getEndOfDayTimestamp(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            return calendar.getTimeInMillis()/1000;
        } catch (ParseException e) {
            throw new GlobalException("日期格式错误");
        }
    }
}
