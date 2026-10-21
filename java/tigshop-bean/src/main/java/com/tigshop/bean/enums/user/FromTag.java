package com.tigshop.bean.enums.user;

import lombok.Getter;


@Getter
public enum FromTag {
    /**
     * 公众号
     */
    WECHAT(1, "公众号"),
    /**
     * 小程序
     */
    MINI_PROGRAM(2, "小程序"),
    /**
     * H5
     */
    H5(3, "H5"),
    /**
     * PC
     */
    PC(4, "PC"),
    /**
     * Android
     */
    ANDROID(5, "Android"),
    /**
     * IOS
     */
    IOS(6, "IOS"),

    ADMIN_ADD(7, "Admin"),

    APP(8, "APP");
    ;

    private final int code;
    private final String description;

    FromTag(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据来源标签代码获取对应的描述
     *
     * @param code 来源标签代码
     * @return 对应的描述，如果代码无效则返回空字符串
     */
    public static String getFromTagName(int code) {
        for (FromTag tag : values()) {
            if (tag.getCode() == code) {
                return tag.getDescription();
            }
        }
        return "";
    }

    /**
     * 将请求客户端类型转换为绑定渠道展示名称。
     *
     * <p>绑定关系建立时记录该名称，避免后续从订单来源反推客户绑定渠道。</p>
     *
     * @param clientType X-Client-Type 请求头值
     * @return 渠道展示名称，缺失或未知时返回空字符串
     */
    public static String getDescriptionByClientType(String clientType) {
        if (clientType == null) {
            return "";
        }
        for (FromTag tag : values()) {
            String expectedClientType = switch (tag) {
                case WECHAT -> "wechat";
                case MINI_PROGRAM -> "miniProgram";
                case H5 -> "h5";
                case PC -> "pc";
                case ANDROID -> "android";
                case IOS -> "ios";
                case ADMIN_ADD -> "admin";
                case APP -> "app";
            };
            if (expectedClientType.equals(clientType)) {
                return tag.getDescription();
            }
        }
        return "";
    }

    /**
     * 判断一个值是否为有效的来源标签代码
     *
     * @param code 来源标签代码
     * @return 如果代码有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (FromTag tag : values()) {
            if (tag.getCode() == code) {
                return true;
            }
        }
        return false;
    }
}
