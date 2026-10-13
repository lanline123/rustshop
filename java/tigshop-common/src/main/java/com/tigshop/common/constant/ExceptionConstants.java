// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant;

/**
 * 异常类常量
 *
 * @author Jayce
 * @create 2024年10月24日 13:28
 */
public class ExceptionConstants {
    /**
     * 系统异常
     */
    public static final String SYSTEM_ERROR = "系统异常";

    /**
     * 业务异常
     */
    public static final String SERVICE_ERROR = "业务异常";

    /**
     * 资源未找到
     */
    public static final String NOT_FOUND_ERROR = "资源未找到";

    /**
     * 参数异常
     */
    public static final String PARAM_ERROR = "参数校验异常";

    /**
     * 权限不足
     */
    public static final String AUTHORIZATION_ERROR = "权限不足";

    /**
     * 数据类型异常
     */
    public static final String DATA_TYPE_ERROR = "数据类型异常";

    /**
     * 数据库异常对外通用提示（隐藏 SQL 细节时返回）
     */
    public static final String DATABASE_ERROR = "内部错误";

    /**
     * 上传失败
     */
    public static final String UPLOAD_FAIL = "上传失败";

    /**
     * 不支持的存储类型
     */
    public static final String UPLOAD_FILE_STORE_ERROR = "不支持的存储类型";

    /**
     * 文件不能为空
     */
    public static final String UPLOAD_FILE_IS_NULL = "文件不能为空";

    /**
     * 不支持的文件类型
     */
    public static final String UPLOAD_FILE_TYPE_ERROR = "不支持的文件类型";

    /**
     * 视频编码格式不支持（如 H.265/HEVC，无法解析首帧封面）
     */
    public static final String VIDEO_CODEC_NOT_SUPPORTED = "该视频的编码格式不支持，请重新上传视频";

    /**
     * 商户入驻申请审核异常 状态错误
     */
    public static final String MERCHANT_APPLY_AUDIT_STATUS_ERROR = "审核数据状态错误";

    // code参数错误
    public static final String CODE_PARAM_ERROR = "code参数错误";

    // data参数错误
    public static final String DATA_PARAM_ERROR = "data参数错误";

    // 邮箱地址错误
    public static final String EMAIL_ERROR = "邮箱地址错误";

    // 翻译错误
    public static final String TRANSLATIONS_ERROR = "翻译错误";

    /**
     * 文件路径不安全
     */
    public static final String UPLOAD_FILE_PATH_UNSAFE = "文件路径不安全";

    /**
     * 文件类型伪造
     */
    public static final String UPLOAD_FILE_TYPE_FAKE = "文件类型与实际不符";

    /**
     * 禁止上传的文件类型
     */
    public static final String UPLOAD_FILE_FORBIDDEN = "禁止上传该类型的文件";
}
