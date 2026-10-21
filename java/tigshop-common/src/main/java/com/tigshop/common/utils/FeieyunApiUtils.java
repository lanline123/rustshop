package com.tigshop.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * 飞鹅云打印机API工具类
 *
 * @author Tigshop团队
 */
@Slf4j
public class FeieyunApiUtils {

    /**
     * 生成签名
     *
     * @param user  飞鹅云后台注册的账号名
     * @param ukey  飞鹅云后台注册账号后生成的UKEY
     * @param stime 当前UNIX时间戳，10位，精确到秒
     * @return 签名
     */
    public static String generateSign(String user, String ukey, String stime) {
        try {
            String signStr = user + ukey + stime;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(signStr.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexStr = new StringBuilder();
            for (byte b : digest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexStr.append(0);
                }
                hexStr.append(shaHex);
            }
            return hexStr.toString();
        } catch (Exception e) {
            log.error("生成飞鹅云API签名失败", e);
            return "";
        }
    }

    /**
     * 获取当前UNIX时间戳，10位，精确到秒
     *
     * @return 时间戳字符串
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 格式化对齐商品价格信息
     *
     * @param price    单价
     * @param quantity 数量
     * @param subtotal 小计
     * @return 格式化后的字符串，字段间通过空格对齐
     */
    public static String formatLine(String price, int quantity, String subtotal) {
        // 定义每个字段的宽度（以字符数为单位）
        int priceWidth = 10;
        int quantityWidth = 10;
        int subtotalWidth = 10;

        // 格式化为字符串，右对齐
        String priceStr = String.format("%-" + priceWidth + "s", price);
        String quantityStr = centerString(String.valueOf(quantity), quantityWidth);
        String subtotalStr = String.format("%" + subtotalWidth + "s", subtotal);

        return priceStr + quantityStr + subtotalStr;
    }

    /**
     * 格式化对齐（支持汉字宽度）
     *
     * @param left  左侧字段（左对齐）
     * @param right 右侧字段（右对齐）
     * @return 对齐后的字符串
     */
    public static String formatLine(String left, String right) {
        boolean contains = right.contains("<BOLD>");
        if (contains) {
            right = right.replace("<BOLD>", "");
        }
        int leftDisplayWidth = 10;
        int rightDisplayWidth = 20;

        String leftStr = padRightConsideringChinese(left, leftDisplayWidth);
        String rightStr = padLeftConsideringChinese(right, rightDisplayWidth);

        return leftStr + (contains ? "<BOLD>" : "") + rightStr;
    }

    /**
     * 格式化对齐（支持汉字宽度）多行
     *
     * @param left  左侧字段（左对齐）
     * @param right 右侧字段（右侧长文本）
     * @return 对齐后的字符串（带<BR>换行）
     */
    public static String formatMultpartLine(String left, String right) {
        int leftWidth = getDisplayWidth(left);
        int availableRightWidth = 28 - leftWidth;

        List<String> rightLines = splitByDisplayWidth(right, availableRightWidth);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < rightLines.size(); i++) {
            if (i == 0) {
                result.append(left).append(rightLines.get(i));
            } else {
                result.append("<BR>").append(" ".repeat(leftWidth)).append(rightLines.get(i));
            }
        }
        return result.toString();
    }

    // =========================================================================
    // 2026-04-22 纸张自适应重载：新增带 totalWidth 参数的版本，老重载逻辑一字节不动（strict_compat）。
    //   58mm: 32 字符/行，multipart 右侧保守 28
    //   80mm: 48 字符/行，multipart 右侧保守 44
    // =========================================================================

    /**
     * 三列商品行带总宽度的重载：按 {@code totalWidth / 3} 均分 价格 / 数量 / 小计 列宽。
     * <p>
     * <b>display-width 感知</b>：使用 {@link #padRightConsideringChinese} /
     * {@link #centerConsideringChinese} / {@link #padLeftConsideringChinese} 补齐，
     * 全角符号（如 ￥）按 2 宽度计，保证 80mm 纸宽 48 显示列不溢出。
     *
     * @param price      单价
     * @param quantity   数量
     * @param subtotal   小计
     * @param totalWidth 行总宽（32 for 58mm，48 for 80mm）
     */
    public static String formatLine(String price, int quantity, String subtotal, int totalWidth) {
        int col = Math.max(totalWidth / 3, 6);

        String priceStr = padRightConsideringChinese(price, col);
        String quantityStr = centerConsideringChinese(String.valueOf(quantity), col);
        String subtotalStr = padLeftConsideringChinese(subtotal, col);

        return priceStr + quantityStr + subtotalStr;
    }

    /**
     * 两列（左 + 右）带总宽度的重载：左占 1/3、右占 2/3，按汉字宽度补齐。
     */
    public static String formatLine(String left, String right, int totalWidth) {
        boolean contains = right.contains("<BOLD>");
        if (contains) {
            right = right.replace("<BOLD>", "");
        }
        int leftDisplayWidth = Math.max(totalWidth / 3, 6);
        int rightDisplayWidth = totalWidth - leftDisplayWidth;

        String leftStr = padRightConsideringChinese(left, leftDisplayWidth);
        String rightStr = padLeftConsideringChinese(right, rightDisplayWidth);

        return leftStr + (contains ? "<BOLD>" : "") + rightStr;
    }

    /**
     * 左 + 多行右，带总宽度的重载。
     *
     * @param totalWidth 可用总宽（保守值：58mm=28，80mm=44）
     */
    public static String formatMultpartLine(String left, String right, int totalWidth) {
        int leftWidth = getDisplayWidth(left);
        int availableRightWidth = Math.max(totalWidth - leftWidth, 8);

        List<String> rightLines = splitByDisplayWidth(right, availableRightWidth);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < rightLines.size(); i++) {
            if (i == 0) {
                result.append(left).append(rightLines.get(i));
            } else {
                result.append("<BR>").append(" ".repeat(leftWidth)).append(rightLines.get(i));
            }
        }
        return result.toString();
    }

    /**
     * 将字符串按显示宽度拆分成多行
     */
    private static List<String> splitByDisplayWidth(String str, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        int currentWidth = 0;

        for (char c : str.toCharArray()) {
            int charWidth = isChinese(c) ? 2 : 1;
            if (currentWidth + charWidth > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder().append(c);
                currentWidth = charWidth;
            } else {
                line.append(c);
                currentWidth += charWidth;
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /**
     * 计算字符串显示宽度：英文=1，汉字=2
     */
    public static int getDisplayWidth(String str) {
        int width = 0;
        for (char c : str.toCharArray()) {
            width += isChinese(c) ? 2 : 1;
        }
        return width;
    }

    /**
     * 判断字符是否为双宽度显示字符（在热敏打印机上占 2 个显示列）。
     * <p>
     * 覆盖以下 Unicode 块：
     * <ul>
     *   <li>U+4E00-U+9FA5：CJK 统一汉字（汉字主区间）</li>
     *   <li>U+3000-U+303F：CJK 符号和标点（"、""。"等）</li>
     *   <li>U+FF01-U+FF60：全角 ASCII 变体（！、（、）等）</li>
     *   <li>U+FFE0-U+FFE6：全角符号（含 ￥ U+FFE5、￡、￠ 等）</li>
     * </ul>
     * <b>注意</b>：原实现只检测 U+4E00-U+9FA5，全角 ￥（U+FFE5）被误判为单宽，
     * 导致 80mm 商品价格行实际显示宽度溢出纸宽进而折行（2026-04-27 修复）。
     */
    private static boolean isChinese(char c) {
        return (c >= '\u4e00' && c <= '\u9fa5')   // CJK 统一汉字
                || (c >= '\u3000' && c <= '\u303f') // CJK 符号和标点
                || (c >= '\uff01' && c <= '\uff60') // 全角 ASCII 变体
                || (c >= '\uffe0' && c <= '\uffe6'); // 全角符号（含 ￥）
    }

    /**
     * 右填充空格（左对齐）。考虑汉字宽度=2。
     */
    public static String padRightConsideringChinese(String str, int targetDisplayWidth) {
        int currentWidth = getDisplayWidth(str);
        int paddingSpaces = targetDisplayWidth - currentWidth;
        return str + " ".repeat(Math.max(0, paddingSpaces));
    }

    /**
     * 左填充空格（右对齐）。考虑汉字宽度=2。
     */
    public static String padLeftConsideringChinese(String str, int targetDisplayWidth) {
        int currentWidth = getDisplayWidth(str);
        int paddingSpaces = targetDisplayWidth - currentWidth;
        return " ".repeat(Math.max(0, paddingSpaces)) + str;
    }

    /**
     * 按字符串长度居中（对纯 ASCII 场景准确）。
     * <p>
     * <b>历史兼容</b>：本方法用 {@code String.length()} 计算，对汉字不准。原代码就这么写，保留不改。
     * 80mm 需要中文感知居中时请用 {@link #centerConsideringChinese(String, int)}。
     */
    public static String centerString(String text, int width) {
        if (text == null) text = "";
        int textLength = text.length();
        if (textLength >= width) return text;

        int padding = width - textLength;
        int paddingLeft = padding / 2;
        int paddingRight = padding - paddingLeft;

        return " ".repeat(paddingLeft) + text + " ".repeat(paddingRight);
    }

    /**
     * 中文感知居中：汉字按 2 宽度计算，英文数字按 1 宽度。80mm 新排版用。
     */
    public static String centerConsideringChinese(String text, int targetDisplayWidth) {
        if (text == null) text = "";
        int textWidth = getDisplayWidth(text);
        if (textWidth >= targetDisplayWidth) return text;

        int padding = targetDisplayWidth - textWidth;
        int paddingLeft = padding / 2;
        int paddingRight = padding - paddingLeft;

        return " ".repeat(paddingLeft) + text + " ".repeat(paddingRight);
    }
}