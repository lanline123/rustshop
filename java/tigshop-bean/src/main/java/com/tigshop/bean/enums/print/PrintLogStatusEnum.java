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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打印任务日志状态枚举
 * <p>
 * 状态机：<br>
 * 0 PENDING    —— 消费者收到 MQ 消息后立即插入<br>
 * 1 PUSHED     —— 平台推送 API 同步返回成功（飞鹅云终态）<br>
 * 2 PRINTED    —— 平台回调上报打印完成（仅商米云可达）<br>
 * 3 PUSH_FAILED —— 平台推送 API 返回业务错误或异常<br>
 * 4 OFFLINE    —— 平台推送/回调返回"打印机离线"特征码
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Getter
@AllArgsConstructor
public enum PrintLogStatusEnum {

    PENDING(0, "待推送"),
    PUSHED(1, "推送成功"),
    PRINTED(2, "打印成功"),
    PUSH_FAILED(3, "推送失败"),
    OFFLINE(4, "打印机离线");

    private final Integer code;
    private final String desc;

    /**
     * 根据状态码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (PrintLogStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }

    /**
     * 根据状态码获取枚举
     */
    public static PrintLogStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrintLogStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
