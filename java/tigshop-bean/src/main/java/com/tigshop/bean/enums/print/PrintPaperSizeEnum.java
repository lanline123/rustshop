// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.print;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打印机纸张尺寸枚举。
 * <p>
 * <b>领域模型</b>：纸张只有几个业界标准值（58mm 小票、80mm 后厨/票据），采用枚举表达。
 * <p>
 * <b>边界层对接三件套（2026-04-22 引入，无需再到处写 of(Integer) 的样板代码）</b>：
 * <ul>
 *     <li>{@link EnumValue} 在 {@link #code} 上：MyBatis-Plus 存库/读库直接识别 tinyint 列 ↔ 枚举</li>
 *     <li>{@link JsonValue} 在 {@link #getCode()} 上：Jackson 序列化输出
 *         {@code "paperSize": 58}（而非 {@code "SIZE_58"}），前端看到的 JSON 和旧版本完全兼容</li>
 *     <li>{@link JsonCreator} 在 {@link #fromCode(Integer)} 上：Jackson 反序列化接受
 *         {@code 58 / 80 / null / 0 / 未知值}，全部通过 {@link #of(Integer)} 兜底</li>
 * </ul>
 * <p>
 * <b>兼容约定</b>：{@link #of(Integer)} 对 {@code null / 0 / 未知值} 一律返回 {@link #SIZE_58}，
 * 与历史数据行为一致（升级前打印机均为 58mm）。
 *
 * @author Tigshop团队
 * @since 2026/4/22
 */
@Getter
@AllArgsConstructor
public enum PrintPaperSizeEnum {

    SIZE_58(58, "58mm", 32, 28),
    SIZE_80(80, "80mm", 48, 44);

    /**
     * 纸宽（毫米），对应数据库 tinyint 存储值与前端 JSON 整数值。
     * <p>
     * {@code @EnumValue} 告诉 MyBatis-Plus：存库时写入本字段值，读库时按本字段值匹配枚举。
     */
    @EnumValue
    private final Integer code;

    /**
     * 展示名，例如 "58mm"
     */
    private final String desc;

    /**
     * 单行最大字符数（英文/数字按 1 宽，汉字按 2 宽）。用于分割线、列宽等总宽约束。
     */
    private final Integer charsPerLine;

    /**
     * "左 + 多行右" 类排版的右侧可用宽度保守值（预留 4 列防止汉字折行位置突兀）。
     * 对应 {@code FeieyunApiUtils.formatMultpartLine} 里的总宽约束。
     */
    private final Integer multipartRightWidth;

    /**
     * Jackson 序列化出口：输出 JSON number 58 / 80，与旧版本兼容。
     */
    @JsonValue
    public Integer toJson() {
        return code;
    }

    /**
     * Jackson 反序列化入口：前端传 {@code 58} / {@code 80} / {@code null} / {@code 0} /
     * 未知值，统一走 {@link #of(Integer)} 兜底。
     */
    @JsonCreator
    public static PrintPaperSizeEnum fromCode(Integer code) {
        return of(code);
    }

    /**
     * 按 code 取枚举，未知值（含 null / 0）返回 {@link #SIZE_58}（历史兼容）
     */
    public static PrintPaperSizeEnum of(Integer code) {
        if (code == null) {
            return SIZE_58;
        }
        for (PrintPaperSizeEnum v : values()) {
            if (v.code.equals(code)) {
                return v;
            }
        }
        return SIZE_58;
    }

    /**
     * 快捷查询：纸宽对应的行字符数，兼容 null / 0 默认 58mm
     */
    public static int charsPerLineOf(Integer code) {
        return of(code).charsPerLine;
    }
}
