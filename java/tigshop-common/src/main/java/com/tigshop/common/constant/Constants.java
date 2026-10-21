package com.tigshop.common.constant;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 通用常量信息
 *
 * @author Jayce
 * @create 2024年10月8日 13:28
 */
public interface Constants {
    /**
     * UTF-8 字符集
     */
    String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    String GBK = "GBK";

    /**
     * 系统语言
     */
    Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    /**
     * www主域
     */
    String WWW = "www.";

    /**
     * http请求
     */
    String HTTP = "http://";

    /**
     * https请求
     */
    String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    Integer SUCCESS = 0;

    /**
     * 通用失败标识
     */
    Integer FAIL = 1;

    /**
     * 登录成功
     */
    String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    String LOGOUT = "Logout";

    /**
     * 注册
     */
    String REGISTER = "Register";

    /**
     * 登录失败
     */
    String LOGIN_FAIL = "Error";

    /**
     * 所有权限标识
     */
    String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    String SUPER_ADMIN = "admin";

    /**
     * 角色权限分隔符
     */
    String ROLE_DELIMETER = ",";

    /**
     * 权限标识分隔符
     */
    String PERMISSION_DELIMETER = ",";

    /**
     * 验证码有效期（分钟）
     */
    Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    String TOKEN = "token";

    // x-shop-id
    String X_ADMIN_TYPE = "x-admin-type";


    //
    String X_POS_PAY_TYPE = "x-pos-pay-type";

    //
    String X_SHOP_TYPE = "x-shop-type";

    // x-shop-id
    String X_SHOP_ID = "x-shop-id";

    // x-vendor-id
    String X_VENDOR_ID = "x-vendor-id";

    // X-Client-Type
    String X_CLIENT_TYPE = "X-Client-Type";

    // X-CSRF-Token
    String X_CSRF_TOKEN = "X-Csrf-Token";

    /**
     * 令牌前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户ID
     */
    String JWT_USERID = "userid";

    /**
     * 用户头像
     */
    String JWT_AVATAR = "avatar";

    /**
     * 创建时间
     */
    String JWT_CREATED = "created";

    /**
     * 用户权限
     */
    String JWT_AUTHORITIES = "authorities";

    /**
     * 资源映射路径 前缀
     */
    String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 远程方法调用
     */
    String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    String LOOKUP_LDAPS = "ldaps:";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    String[] JSON_WHITELIST_STR = {"org.springframework", "com.tigshop"};

    /**
     * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
     */
    String[] JOB_WHITELIST_STR = {"com.tigshop.quartz.task"};

    /**
     * 定时任务违规的字符
     */
    String[] JOB_ERROR_STR = {"java.net.URL", "jakarta.naming.InitialContext", "org.yaml.snakeyaml", "org.springframework", "org.apache", "com.tigshop.common.utils.file", "com.tigshop.common.config"};

    List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/tiff");

    List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("video/mp4", "video/avi", "video/mpeg", "video/quicktime", "video/x-ms-wmv", "video/webm", "video/3gpp", "video/ogg", "video/x-flv", "video/x-matroska", "video/dvd", "video/3gpp2", "video/x-msvideo");

    // 时间格式
    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 默认版权
    String DEFAULT_COMPANY = "Copyright © 2024 Tigshop. All Rights Reserved";

    // 后台API Token字段-redis
    String ADMIN_API_TOKEN = "admin_api_token::";

    // API Token专用请求头
    String X_API_TOKEN_HEADER = "X-Api-Token";

    // 后台token字段-redis
    String ADMIN_TOKEN = "admin_token::";

    // 后台用户字段-redis
    String ADMIN_USER = "admin_user::";

    // 登录错误
    String LOGIN_ERROR = "login_error::";

    // 发送短信重试次数
    String MSG_RETRY_COUNT = "msg_retry_count::";

    // 取消订单重试次数
    String RECEIVE_ORDER = "receive_order::";

    // pc 分类导航
    String CAT_FLOOR = "cat_floor";

    // TRANSLATE 翻译
    String TRANSLATE = "translate::";

    // TRANSLATE 翻译
    String SEO_LINK_CATEGORY = "seo_link_category::";

    // excel格式
    String XLSX = ".xlsx";

    // 腾讯云智服
    String TENCENT_CUSTOMER_SERVICE_URL = "https://yzf.qq.com/xv/web/static/chat/index.html?sign=";

    // 企业客服
    String QY_CUSTOMER_SERVICE_URL = "https://work.weixin.qq.com/kfid/";

    /**
     * 是
     */
    Integer YES = 1;

    /**
     * 否
     */
    Integer NO = 0;

    String PASSWORD_TOO_SIMPLE = "password_too_simple:";

    // app_home_decorate
    String APP_HOME_DECORATE = "app_home_decorate";

    // 前端o2o拼音列表-redis
    String CITY_PINYING_LIST = "city_pinying_list";

    // pos登录时间
    String POS_LOGIN_TIME = "pos_login_time::";

    // 常数0
    Integer ZERO = 0;
    // 常数1
    Integer ONE = 1;

    public static String getLoginTokenFormat(String token, String username) {
        return ADMIN_TOKEN + username + ":" + token;
    }

    public static String getAllLoginTokenFormatByUserName(String username) {
        return ADMIN_TOKEN + username + ":*";
    }
}
