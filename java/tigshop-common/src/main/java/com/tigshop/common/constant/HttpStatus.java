package com.tigshop.common.constant;

/**
 * 返回状态码
 *
 * @author jayce
 * @create 2024-09-28 15:16:06
 */
public class HttpStatus {
    /**
     * 操作成功
     */
    public static final int SUCCESS = 200;

    /**
     * 对象创建成功
     */
    public static final int CREATED = 201;

    /**
     * 请求已经被接受
     */
    public static final int ACCEPTED = 202;

    /**
     * 操作已经执行成功，但是没有返回数据
     */
    public static final int NO_CONTENT = 204;

    /**
     * 资源已被移除
     */
    public static final int MOVED_PERM = 301;

    /**
     * 重定向
     */
    public static final int SEE_OTHER = 303;

    /**
     * 资源没有被修改
     */
    public static final int NOT_MODIFIED = 304;

    /**
     * 参数列表错误（缺少，格式不匹配）
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 访问受限，授权过期
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源，服务未找到
     */
    public static final int NOT_FOUND = 404;

    /**
     * 不允许的http方法
     */
    public static final int BAD_METHOD = 405;

    /**
     * 资源冲突，或者资源被锁
     */
    public static final int CONFLICT = 409;

    /**
     * 不支持的数据，媒体类型
     */
    public static final int UNSUPPORTED_TYPE = 415;

    /**
     * 系统内部错误
     */
    public static final int ERROR = 500;

    /**
     * 接口未实现
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * 系统警告消息
     */
    public static final int WARN = 601;

    /**
     * 失败
     */
    public static final int SERVICE_DATA_ERROR = 1001;

    /**
     * 需要行为验证
     */
    public static final int LOGIN_CHECK = 1002;

    // 5000 开头为自定义错误码
    /**
     * 失败
     */
    public static final int EXIST_CONFLICT_PROMOTION_ERROR = 5001;

    /**
     * 用户未绑定微信
     */
    public static final int USER_NOT_BIND_WECHAT = 5002;


    /**
     * 提交预约时间已经失效，建议刷新页面
     */
    public static final int RESERVATION_TIME_EXPIRED = 5003;

    /**
     * 操作预期提示 (假设拆单成功后，他再次拆单（我们在前端要展示✅提示）)
     */
    public static final int  OPT_ALTER_SUCCESS_CODE = 5004;

    // ===== 5300-5399 B2B 批发模块业务错误码（来源：.ai-os/specs/wholesaler-api-contract.md §0.3）=====

    /** 批发功能未启用 */
    public static final int WHOLESALE_DISABLED = 5300;
    /** 用户不存在 */
    public static final int WHOLESALE_USER_NOT_FOUND = 5301;
    /** 该用户已是批发商（仅手动添加场景；批量导入按 C15 改等级不报错）*/
    public static final int WHOLESALE_USER_ALREADY_EXISTS = 5302;
    /** 批发等级不存在 */
    public static final int WHOLESALE_GRADE_NOT_FOUND = 5303;
    /** 批发等级已禁用 */
    public static final int WHOLESALE_GRADE_DISABLED = 5304;
    /** 等级仍有客户绑定（删除前置校验：该等级下有批发商用户，需先删除这些批发商，CR-20260610-2210）*/
    public static final int WHOLESALE_GRADE_HAS_BINDING = 5305;
    /** 默认批发等级不可删除（CR-20260610-2210 改语义：需先在批发设置更换默认等级；原"等级不可删除"设计已废）*/
    public static final int WHOLESALE_GRADE_CANNOT_DELETE = 5306;

    /** 商品不存在或未配置批发能力 */
    public static final int WHOLESALE_PRODUCT_NOT_FOUND = 5310;
    /** 商品已配置批发能力（重复添加）*/
    public static final int WHOLESALE_PRODUCT_ALREADY_CONFIGURED = 5311;
    /** 批发价配置不合法（折率/固定金额/阶梯重复/阶梯>20 段）*/
    public static final int WHOLESALE_PRICE_INVALID = 5312;
    /** 起批规则不合法（min_quantity≤0 / multiple≤0）*/
    public static final int WHOLESALE_BUY_RULE_INVALID = 5313;

    /**
     * 批发商品归属校验失败（CR-20260610-162600 / CD-039 多租户改语义）：
     * 操作者只能配置自己名下商品（平台 admin=平台商品 / 商家=本店商品），跨店操作抛此码。
     * 历史语义（CR-20260603-141000「仅平台商品支持配置批发能力」）已废，错误码沿用避免段位复用混乱。
     */
    public static final int WHOLESALE_NON_PLATFORM_PRODUCT = 5314;

    /**
     * 商品类型不支持配置批发（2026-06-11 用户决策）：仅允许普通（实物，product_type=1）
     * 且非预售（sale_mode 为 0/null）的商品配置批发能力；虚拟/卡密/预约/礼品卡/预售商品一律拒绝。
     */
    public static final int WHOLESALE_PRODUCT_TYPE_NOT_SUPPORTED = 5315;

    /** 未达起批量（加车/下单时校验失败）*/
    public static final int WHOLESALE_MOQ_NOT_MET = 5320;
    /** 数量需为倍数（加车/下单时校验失败）*/
    public static final int WHOLESALE_MULTIPLE_NOT_MET = 5321;
    /** 批发价已下线（加车后批发价被禁用/删除）*/
    public static final int WHOLESALE_PRICE_EXPIRED = 5322;
    /** 混批未达最低起订档（CR-20260604-1430：multi_spec_price_mode=1 下某 SKU 在全规格总数下无命中阶梯档）*/
    public static final int WHOLESALE_TIER_NOT_MET = 5323;

    /** 多店铺暂不支持批发能力（C8 校验）*/
    public static final int WHOLESALE_MULTI_SHOP_NOT_SUPPORT = 5330;
    /** 未配置默认批发等级（启用自动转批发时校验）*/
    public static final int WHOLESALE_DEFAULT_GRADE_NOT_CONFIGURED = 5331;
    /** 必填配置项缺失（设置项保存校验）*/
    public static final int WHOLESALE_CONFIG_REQUIRED = 5332;

    /** 批量导入部分失败（含失败行清单）*/
    public static final int WHOLESALE_IMPORT_PARTIAL_FAIL = 5340;
    /** 批量加车部分失败 */
    public static final int WHOLESALE_CART_BATCH_PARTIAL_FAIL = 5341;

    // ===== 5400-5413 店铺保证金模块业务错误码（合并 D1095-shop-deposit 时遗漏，补回）=====
    /** 保证金功能未开启 */
    public static final int DEPOSIT_NOT_ENABLED = 5400;
    /** 保证金金额非法 */
    public static final int DEPOSIT_AMOUNT_INVALID = 5401;
    /** 保证金余额不足 */
    public static final int DEPOSIT_BALANCE_NOT_ENOUGH = 5402;
    /** 充值单状态非法 */
    public static final int DEPOSIT_RECHARGE_STATUS_INVALID = 5403;
    /** 退还单状态非法 */
    public static final int DEPOSIT_REFUND_STATUS_INVALID = 5404;
    /** 保证金不足,禁止上架 */
    public static final int DEPOSIT_INSUFFICIENT_FORBID_ONSHELF = 5405;
    /** 保证金不足,禁止下单 */
    public static final int DEPOSIT_INSUFFICIENT_FORBID_ORDER = 5406;
    /** 全额退还需店铺停业 */
    public static final int DEPOSIT_REFUND_SHOP_NOT_CLOSED = 5407;
    /** 存在进行中订单 */
    public static final int DEPOSIT_REFUND_HAS_ONGOING_ORDER = 5408;
    /** 存在进行中售后 */
    public static final int DEPOSIT_REFUND_HAS_ONGOING_AFTERSALE = 5409;
    /** 未过末单收货冷静期 */
    public static final int DEPOSIT_REFUND_COOLDOWN_NOT_REACHED = 5410;
    /** 存在进行中退还单 */
    public static final int DEPOSIT_REFUND_HAS_ONGOING_REFUND = 5411;
    /** 提现超过可提现上限 */
    public static final int DEPOSIT_WITHDRAW_EXCEED_LIMIT = 5412;
    /** 保证金规则同维度生效时间重叠 */
    public static final int DEPOSIT_RULE_TIME_OVERLAP = 5413;
}
