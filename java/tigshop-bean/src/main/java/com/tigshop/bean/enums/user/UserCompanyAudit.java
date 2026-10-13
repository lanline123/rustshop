package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 企业认证类型
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum UserCompanyAudit {
    //待审核
    PENDING(1, "待审核"),
    //审核通过
    PASS(2, "审核通过"),
    //审核不通过
    FAIL(3, "审核不通过");

    private final Integer code;
    private final String description;

    UserCompanyAudit(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     根据code获取状态名称
     * @param code 来源标签代码
     * @return 对应的描述，如果代码无效则返回空字符串
     */
    public static String getStatusName(int code) {
        for (UserCompanyAudit status : UserCompanyAudit.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
