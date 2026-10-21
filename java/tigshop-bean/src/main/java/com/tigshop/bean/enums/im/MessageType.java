package com.tigshop.bean.enums.im;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * message 类型
 *
 * @author 企业认证实体类
 * @create  2024-12-25
 */
@Getter
public enum MessageType {
    TEXT("text", "文本"),
    IMAGE("image", "图片"),
    VOICE("custom", "自定义"),
    VIDEO("video", "视频"),
    LOCATION("sound", "语音"),
    LINK("file", "文件");

    private final String code;
    private final String description;

    MessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(String code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

}
