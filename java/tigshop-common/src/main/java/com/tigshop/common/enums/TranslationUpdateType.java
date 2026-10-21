package com.tigshop.common.enums;

import lombok.Getter;

/**
 * 一键翻译更新类型
 *
 * @author Tigshop团队
 */
@Getter
public enum TranslationUpdateType {
    /**
     * 仅翻译未维护的内容（已存在且非空则跳过）
     */
    TRANSLATE_MISSING(1, "仅翻译未维护的内容"),
    /**
     * 覆盖全部内容（按 data_id 预删除后重新翻译）
     */
    OVERWRITE_ALL(2, "覆盖全部内容");

    private final Integer code;
    private final String description;

    TranslationUpdateType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isOverwriteAll(Integer code) {
        return OVERWRITE_ALL.getCode().equals(code);
    }
}
