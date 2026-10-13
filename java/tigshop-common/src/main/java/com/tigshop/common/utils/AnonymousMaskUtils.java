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

/**
 * 匿名展示名脱敏工具（D1146）
 *
 * <p>不能复用 {@code @Sensitive}：它被 SensitiveSerializer 限制为仅 tigshop.is-demo=1 时生效。
 *
 * @author Tigshop团队
 */
public final class AnonymousMaskUtils {

    /**
     * 星号数恒为 2，不随真实长度变化，避免通过掩码长度反推原名长度
     */
    private static final String MIDDLE_MASK = "**";

    private static final String FULL_MASK = "***";

    private static final String SINGLE_MASK = "*";

    private AnonymousMaskUtils() {
    }

    /**
     * 按 Unicode 码点脱敏展示名，避免 emoji 代理对被截成乱码。
     *
     * <p>规则：空或空白 -> {@code ***}；1 个码点 -> {@code *}；
     * 2 个码点 -> 首码点 + {@code **}；3 个及以上 -> 首码点 + {@code **} + 尾码点。
     *
     * @param name 原始展示名
     * @return 脱敏后的展示名
     */
    public static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return FULL_MASK;
        }

        int[] codePoints = name.codePoints().toArray();
        int length = codePoints.length;

        if (length == 1) {
            return SINGLE_MASK;
        }
        String first = new String(codePoints, 0, 1);
        if (length == 2) {
            return first + MIDDLE_MASK;
        }
        String last = new String(codePoints, length - 1, 1);
        return first + MIDDLE_MASK + last;
    }
}
