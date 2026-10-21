import request from "@/utils/request";

/** 登录响应结果 */
export interface SigninResult {
    token?: string;
    adminType?: string;
}

/**
 * 管理员登录
 * @description 使用账号密码进行后台登录
 * @param data - 登录参数（账号、密码）
 * @param csrfToken - CSRF令牌
 * @returns 登录结果，包含token
 */
export const adminSignin = (data: object, csrfToken: string) => {
    return request<SigninResult>({
        url: "login/signin",
        method: "post",
        data,
        headers: {
            "X-CSRF-Token": csrfToken,
        }
    });
};

/**
 * 发送短信验证码
 * @description 发送手机登录验证码
 * @param data - 手机号等参数
 * @returns 发送结果
 */
export const sendMobileCode = (data: object) => {
    return request({
        url: "login/sendMobileCode",
        method: "post",
        data,
    });
};
