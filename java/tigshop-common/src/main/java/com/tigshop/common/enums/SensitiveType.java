package com.tigshop.common.enums;

/**
 * 数据脱敏类型枚举
 */
public enum SensitiveType {

    /** 手机号：保留前3后4，中间用****替换 */
    PHONE,

    /** 身份证号：保留前3后4 */
    ID_CARD,

    /** 邮箱：用户名保留首尾字符，中间用*** */
    EMAIL,

    /** 银行卡号：保留前4后4 */
    BANK_CARD
}
