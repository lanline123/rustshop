package com.tigshop.bean.enums.merchant;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

@Getter
public enum MerchantApplyStatusEnum {
    //待审核
    PENDING_REVIEW(1, "待审核"),
    //审核通过
    AUDIT_PASS(10, "审核通过"),
    //审核未通过
    AUDIT_NOT_PASS(20, "审核未通过");

    private final Integer code;
    private final String description;

    MerchantApplyStatusEnum(int code, String description){
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取状态名称
     * @param code 来源标签代码
     * @return 对应的描述，如果代码无效则返回空字符串
     */
    public static String getStatusName(int code){
        for(MerchantApplyStatusEnum status : MerchantApplyStatusEnum.values()){
            if(status.getCode() == code){
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

    /**
     * 根据类型编码获取类型
     *
     * @param code 类型编码
     * @return 类型
     */
    public static MerchantApplyStatusEnum fromStatusCode(Integer code) {
        for (MerchantApplyStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}
