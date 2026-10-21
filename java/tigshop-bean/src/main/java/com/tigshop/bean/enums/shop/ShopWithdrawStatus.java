package com.tigshop.bean.enums.shop;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

@Getter
public enum ShopWithdrawStatus {
    PENDING_REVIEW(0, "待审核"),

    AUDIT_PROCESS(1, "正在处理"),

    AUDIT_NOT_PASS(2, "审核不通过"),

    AUDIT_PASS(3, "完成"),

    STATUS_WAIT_PAYMENT(4, "待打款");;

    private final Integer code;
    private final String description;

    ShopWithdrawStatus(int code, String description){
        this.code = code;
        this.description = description;
    }

    /**
       根据code获取状态名称
     * @param code 来源标签代码
     * @return 对应的描述，如果代码无效则返回空字符串
     */
    public static String getStatusName(int code){
        for(ShopWithdrawStatus status : ShopWithdrawStatus.values()){
            if(status.code == code){
                return status.description;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

    public String getStatus() {
        return description;
    }
}
