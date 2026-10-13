package com.tigshop.bean.enums.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum SmsProviderEnum {

    ALIYUN(1, "阿里云"),
    TENCENT(2, "腾讯云"),
    HUAWEI(3, "华为云"),
    VOLCENGINE(4, "火山引擎");

    private final Integer code;
    private final String name;

    private static final Map<Integer, SmsProviderEnum> CODE_MAP = new HashMap<>();

    static {
        for (SmsProviderEnum e : values()) {
            CODE_MAP.put(e.code, e);
        }
    }

    public static SmsProviderEnum fromCode(Integer code) {
        if (code == null) {
            return ALIYUN;
        }
        SmsProviderEnum result = CODE_MAP.get(code);
        return result != null ? result : ALIYUN;
    }
}
