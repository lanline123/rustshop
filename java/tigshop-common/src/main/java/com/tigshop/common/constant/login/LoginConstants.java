// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.login;

/**
 * 登录常量
 *
 * @author Jayce
 * @create 2024年10月24日 14:01
 */
public class LoginConstants {
    /* 异常信息 */
    public static final String USER_NOT_CODE = "验证码错误";
    public static final String MOBILE_USED = "手机号已经被使用";
    public static final String PASSWORD_NOT_LOGIN = "密码不能为空";
    public static final String ADMIN_USER_NOT_NAME = "管理员账号不能为空";
    public static final String ADMIN_USER_PASSWORD_ERROR = "管理员账号或密码错误";
    // 登录类型不能为空
    public static final String LOGIN_TYPE_NOT_NULL = "登录类型不能为空";

    // 旧密码错误
    public static final String OLD_PASSWORD_ERROR = "旧密码错误";

    // 密码不一致
    public static final String PASSWORD_NOT_CONSISTENT = "两次密码不一致";

    // USER_PASSWORD_ERROR
    public static final String USER_PASSWORD_ERROR = "账号或密码错误";

    // USER_NOT_NAME
    public static final String USER_NOT_NAME = "账号不能为空";

    /* 业务常量 */
    /**
     * 过期时间，单位：分钟
     */
    public static final int EXPIRE_MINUTES = 30;

    /**
     * 错误次数阈值，超过该次数需要输入验证码
     */
    public static final int ERROR_THRESHOLD = 3;
}
