// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.pay.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.bo.pay.ProfitSharingPaymentContext;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.pay.ProfitSharingAwarePayService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 汇付斗拱聚合支付服务实现（一期：微信 huifuWechat / 支付宝 huifuAlipay）。
 *
 * <p>
 * 设计基线：docs/汇付斗拱-一期支付-设计文档.md。加签/验签采用 hutool（RSA2 = SHA256withRSA）自实现，
 * 完全对齐 AntomServiceImpl 模式（懒加载配置 + 自建 HTTP + 原始报文验签），不引入官方 SDK
 * （官方 dg-java-sdk 传递依赖 fastjson 1.2.70 高危 CVE，本项目无 fastjson）。
 * </p>
 *
 * <p>
 * 两个 payCode（huifuWechat/huifuAlipay）都路由到本 bean；pay() 内部按 paylog.payCode 判定钱包，
 * 再叠加 clientType 得到汇付 trade_type。金额单位为<b>元（两位小数字符串，不 ×100）</b>。
 * </p>
 *
 * <p>
 * <b>联调待校准（huifu_id 未开通，以下以汇付斗拱标准约定实现，联调时按实际文档核对）：</b>
 * 报文包裹字段（sys_id/product_id/data/sign）、各接口 URL 路径、pay_info 返回结构、
 * 微信/支付宝扩展参数（wx_data/alipay_data）字段名。
 * </p>
 *
 * @author Tigshop团队
 * @since 2026/07/15
 */
@Service("HuifuService")
@Slf4j
public class HuifuPayServiceImpl extends PayBaseServiceImpl implements ProfitSharingAwarePayService {

    /** 汇付聚合正扫（主扫）下单接口路径（联调核对） */
    private static final String PATH_AGGREGATE_PAY = "/v3/trade/payment/jspay";
    /** 汇付扫码交易退款接口路径（联调核对） */
    private static final String PATH_REFUND = "/v2/trade/payment/scanpay/refund";
    /** 汇付扫码交易查询接口路径（联调核对） */
    private static final String PATH_QUERY = "/v2/trade/payment/scanpay/query";

    /** 汇付统一收银台（托管支付）H5/PC 预下单接口路径 */
    private static final String PATH_HOSTING_PREORDER = "/v2/trade/hosting/payment/preorder";
    /** 汇付统一收银台（托管支付）退款接口路径 */
    private static final String PATH_HOSTING_REFUND = "/v2/trade/hosting/payment/htRefund";
    /** 汇付统一收银台（托管支付）订单查询接口路径 */
    private static final String PATH_HOSTING_QUERY = "/v2/trade/hosting/payment/queryorderinfo";
    /** 托管支付渠道标记，写入 paylog.tokenCode，用于退款/查询按原交易类型分流 */
    private static final String TOKEN_HOSTING = "HOSTING";

    private static final DateTimeFormatter REQ_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private ConfigService configService;
    @Resource
    private PaylogService paylogService;

    // ============ 配置 ============

    private String cfg(SettingsEnum key) {
        return configService.getConfigVal(key);
    }

    private String gateway() {
        String gw = cfg(SettingsEnum.HUIFU_GATEWAY);
        if (StrUtil.isBlank(gw)) {
            throw new GlobalException("汇付网关地址未配置");
        }
        return StrUtil.removeSuffix(gw, "/");
    }

    private void requireConfigured() {
        if (StrUtil.isBlank(cfg(SettingsEnum.HUIFU_MERCHANT_ID))
                || StrUtil.isBlank(cfg(SettingsEnum.HUIFU_RSA_PRIVATE_KEY))
                || StrUtil.isBlank(cfg(SettingsEnum.HUIFU_GATEWAY))) {
            throw new GlobalException("汇付支付未配置完整（商户号/私钥/网关）");
        }
    }

    // ============ 加签 / 验签（hutool RSA2） ============

    /** 用商户 RSA 私钥对 data 明文加签，返回 Base64 字符串 */
    private String sign(String data) {
        String privateKey = normalizeKey(cfg(SettingsEnum.HUIFU_RSA_PRIVATE_KEY));
        if (StrUtil.isBlank(privateKey)) {
            throw new GlobalException("汇付商户私钥未配置");
        }
        try {
            Sign signer = new Sign(SignAlgorithm.SHA256withRSA, privateKey, null);
            // 对齐官方 SDK AbstractRequest：加签原文为 sort4JsonString(reqData, 0)，非 data 原始字符串
            byte[] signed = signer.sign(sort4JsonString(data, 0).getBytes(StandardCharsets.UTF_8));
            return Base64.encode(signed);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            // 私钥内容/格式不正确时兜住底层加密异常，回人话错误，不暴露 KeySpec 堆栈
            log.error("汇付商户私钥加签失败，疑似私钥格式或内容不正确：{}", e.getMessage());
            throw new GlobalException("汇付商户私钥不正确，请检查后台配置的商户RSA私钥（需 PKCS#8 Base64）");
        }
    }

    /**
     * 用汇付公钥验<b>同步响应</b>签名。
     * 对齐官方 SDK {@code AbstractRequest}：同步响应验签原文为 {@code sort4JsonString(data, 5)}（对 data 递归按 key 排序后再验）。
     */
    private boolean verify(String data, String signStr) {
        String publicKey = normalizeKey(cfg(SettingsEnum.HUIFU_PUBLIC_KEY));
        if (StrUtil.isBlank(publicKey) || StrUtil.isBlank(data) || StrUtil.isBlank(signStr)) {
            return false;
        }
        try {
            Sign verifier = new Sign(SignAlgorithm.SHA256withRSA, null, publicKey);
            // 对齐官方 SDK AbstractRequest：同步响应验签原文为 sort4JsonString(data, 5)
            return verifier.verify(sort4JsonString(data, 5).getBytes(StandardCharsets.UTF_8), Base64.decode(signStr));
        } catch (Exception e) {
            log.error("汇付验签异常", e);
            return false;
        }
    }

    /**
     * 用汇付公钥验<b>异步通知</b>签名。
     *
     * <p>与同步响应不同：汇付斗拱官方文档（spin.cloudpnr.com/topds/asynNotice.html）明确要求，
     * 异步通知对<b>原始 {@code resp_data} 字符串直接验签，不做排序</b>：
     * {@code RsaUtils.verify(request.getParameter("resp_data"), PUBLIC_KEY, sign)}。
     * 若沿用同步响应的 {@code sort4JsonString} 重排，会与汇付签名时使用的原始串不一致而导致验签失败，
     * 表现为「异步回调没通」。故通知走本方法，原样验签。</p>
     */
    private boolean verifyNotify(String rawRespData, String signStr) {
        String publicKey = normalizeKey(cfg(SettingsEnum.HUIFU_PUBLIC_KEY));
        if (StrUtil.isBlank(publicKey) || StrUtil.isBlank(rawRespData) || StrUtil.isBlank(signStr)) {
            return false;
        }
        try {
            Sign verifier = new Sign(SignAlgorithm.SHA256withRSA, null, publicKey);
            return verifier.verify(rawRespData.getBytes(StandardCharsets.UTF_8), Base64.decode(signStr));
        } catch (Exception e) {
            log.error("汇付回调验签异常", e);
            return false;
        }
    }

    /**
     * 忠实对齐汇付斗拱官方 SDK {@code com.huifu.bspay.sdk.opps.core.sign.JsonUtils.sort4JsonString}
     * 作为加签/验签原文（官方：请求 maxLayer=0、响应/回调 maxLayer=5）。
     *
     * <p>官方 {@code JSONObject.parseObject(json, TreeMap.class)} 会把<b>所有层级</b>的对象递归解析为
     * TreeMap，因此效果等价于：<b>递归按 key 的 ASCII 升序排序所有对象</b>（含数组内的对象），数组元素顺序不变；
     * {@code maxLayer} 对最终输出<b>无影响</b>（已用官方 SDK 实测 L0==L5 证实）。序列化对齐 fastjson
     * {@code JSON.toJSONString}：紧凑无空白、<b>丢弃 null 值字段</b>、数字/布尔裸输出、字符串仅转义
     * {@code " \ } 与控制字符（{@code /} 不转义、非 ASCII 原样）。</p>
     *
     * <p>本项目不引官方 SDK（其传递依赖 fastjson 1.2.70 高危 CVE），改用 hutool 解析 + 自实现等价序列化，
     * 已用官方 SDK 对同组报文逐字节比对一致。{@code maxLayer} 形参仅为对齐官方调用语义而保留。</p>
     */
    private static String sort4JsonString(String sourceJson, int maxLayer) {
        if (StrUtil.isBlank(sourceJson)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        writeSorted(sb, JSONUtil.parse(sourceJson));
        return sb.toString();
    }

    /** 递归排序对象 key、丢弃 null 值字段，按 fastjson 规则紧凑序列化。 */
    private static void writeSorted(StringBuilder sb, Object value) {
        if (value == null || value instanceof JSONNull) {
            sb.append("null");
        } else if (value instanceof Map<?, ?> map) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                Object v = e.getValue();
                if (v == null || v instanceof JSONNull) {
                    continue;
                }
                sorted.put(String.valueOf(e.getKey()), v);
            }
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : sorted.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                writeJsonString(sb, e.getKey());
                sb.append(':');
                writeSorted(sb, e.getValue());
            }
            sb.append('}');
        } else if (value instanceof Iterable<?> arr) {
            sb.append('[');
            boolean first = true;
            for (Object item : arr) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                writeSorted(sb, item);
            }
            sb.append(']');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else {
            writeJsonString(sb, value.toString());
        }
    }

    private static void writeJsonString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    /**
     * 规整 RSA 密钥字符串，使其成为 hutool {@link Sign} 可直接消费的单行 Base64：
     * <ul>
     *   <li>剥离 {@code -----BEGIN xxx-----} / {@code -----END xxx-----} PEM 头尾；</li>
     *   <li>去掉所有空白（换行 / 空格 / 制表 / 回车）；</li>
     *   <li>若为 PKCS#1（{@code -----BEGIN RSA PRIVATE KEY-----}）则兜底转成 PKCS#8。</li>
     * </ul>
     * 兼容运营直接粘贴 PEM 文件内容的情况（否则 hutool 会抛
     * {@code failed to construct sequence ... object truncated}）。私钥/公钥通用。
     */
    private String normalizeKey(String raw) {
        if (StrUtil.isBlank(raw)) {
            return raw;
        }
        boolean pkcs1 = raw.contains("RSA PRIVATE KEY");
        String cleaned = raw.replaceAll("-----BEGIN[^-]*-----", "")
                .replaceAll("-----END[^-]*-----", "")
                .replaceAll("\\s", "");
        if (!pkcs1) {
            return cleaned;
        }
        try {
            return Base64.encode(pkcs1ToPkcs8(Base64.decode(cleaned)));
        } catch (Exception e) {
            log.warn("汇付私钥 PKCS#1→PKCS#8 转换失败，按原样返回：{}", e.getClass().getSimpleName());
            return cleaned;
        }
    }

    /** 把 PKCS#1(RSAPrivateKey DER) 包一层 PrivateKeyInfo，得到 PKCS#8 DER。 */
    private static byte[] pkcs1ToPkcs8(byte[] pkcs1) {
        // version INTEGER 0
        byte[] version = {0x02, 0x01, 0x00};
        // AlgorithmIdentifier { rsaEncryption(1.2.840.113549.1.1.1), NULL }
        byte[] algId = {0x30, 0x0d, 0x06, 0x09, 0x2a, (byte) 0x86, 0x48,
                (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00};
        byte[] octet = derTlv((byte) 0x04, pkcs1);
        return derTlv((byte) 0x30, concat(version, algId, octet));
    }

    /** 组一个 DER TLV（tag + 长度 + 内容）。 */
    private static byte[] derTlv(byte tag, byte[] content) {
        byte[] len = derLen(content.length);
        byte[] out = new byte[1 + len.length + content.length];
        out[0] = tag;
        System.arraycopy(len, 0, out, 1, len.length);
        System.arraycopy(content, 0, out, 1 + len.length, content.length);
        return out;
    }

    /** DER 长度编码（短格式 / 长格式）。 */
    private static byte[] derLen(int len) {
        if (len < 0x80) {
            return new byte[]{(byte) len};
        }
        int n = 0;
        for (int tmp = len; tmp > 0; tmp >>>= 8) {
            n++;
        }
        byte[] out = new byte[1 + n];
        out[0] = (byte) (0x80 | n);
        for (int i = 0; i < n; i++) {
            out[1 + n - 1 - i] = (byte) (len & 0xff);
            len >>>= 8;
        }
        return out;
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) {
            total += a.length;
        }
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, out, pos, a.length);
            pos += a.length;
        }
        return out;
    }

    /** 截断响应体用于日志，避免超长内容刷屏 */
    private static String brief(String s) {
        if (s == null) {
            return "null";
        }
        String t = s.strip();
        return t.length() > 500 ? t.substring(0, 500) + "...(truncated)" : t;
    }

    /** 组装汇付报文包裹并 POST，返回业务响应 data（验签通过后） */
    protected JSONObject post(String path, JSONObject data) {
        String dataStr = data.toString();
        Map<String, Object> body = new HashMap<>();
        body.put("sys_id", cfg(SettingsEnum.HUIFU_SYS_ID));
        body.put("product_id", cfg(SettingsEnum.HUIFU_PRODUCT_ID));
        // 对齐官方 SDK：data 以对象形式上送（request.put("data", parseObject(reqData))），非字符串；
        // sign 仍对同一份 data 内容的 sort4JsonString 结果加签
        body.put("data", data);
        body.put("sign", sign(dataStr));

        String url = gateway() + path;
        int status;
        String respStr;
        try (HttpResponse resp = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                // 对齐官方 SDK：允许 gzip 压缩传输，hutool 会按 Content-Encoding 自动解压
                .header("Accept-Encoding", "gzip")
                .body(JSONUtil.toJsonStr(body))
                .timeout(20000)
                .execute()) {
            status = resp.getStatus();
            respStr = resp.body();
        }
        // 对齐官方 SDK：先判 HTTP 200 再解析；非 200 视为通信失败，避免把网关错误页/非 JSON 当报文解析
        if (status != 200 || StrUtil.isBlank(respStr)) {
            log.error("汇付网关通信失败 url={} httpStatus={} body={}", url, status, brief(respStr));
            throw new GlobalException("汇付网关通信失败(HTTP " + status + ")，请检查网关地址与网络");
        }
        JSONObject respJson;
        try {
            respJson = JSONUtil.parseObj(respStr);
        } catch (Exception e) {
            // 汇付返回了非 JSON（多为网关地址/环境错误导致的 HTML 错误页），打印原始响应便于定位
            log.error("汇付网关返回非JSON响应 url={} httpStatus={} body={}", url, status, brief(respStr));
            throw new GlobalException("汇付网关返回非JSON响应，请检查后台汇付网关地址/环境配置是否正确");
        }
        String respData = respJson.getStr("data");
        String respSign = respJson.getStr("sign");
        if (StrUtil.isNotBlank(respData) && !verify(respData, respSign)) {
            throw new GlobalException("汇付响应验签失败");
        }
        if (StrUtil.isBlank(respData)) {
            return respJson;
        }
        try {
            return JSONUtil.parseObj(respData);
        } catch (Exception e) {
            log.error("汇付响应 data 字段非JSON url={} data={}", url, brief(respData));
            throw new GlobalException("汇付响应data解析失败");
        }
    }

    // ============ trade_type 映射 ============

    /**
     * (钱包, clientType) -> 汇付 trade_type（对齐 WechatPayServiceImpl 的 clientType 分支结构）。
     * 具体枚举值以汇付文档为准，联调核对。
     */
    private String resolveTradeType(String wallet, String clientType) {
        if ("alipay".equals(wallet)) {
            return switch (clientType == null ? "" : clientType) {
                case "pc" -> "A_NATIVE";
                default -> "A_JSAPI"; // h5/wap/生活号/小程序
            };
        }
        // 微信
        return switch (clientType == null ? "" : clientType) {
            case "miniProgram" -> "T_MINIAPP";
            case "wechat" -> "T_JSAPI";
            case "pc" -> "T_NATIVE";
            case "h5" -> "T_H5";
            case "app" -> "T_APP";
            default -> "T_JSAPI";
        };
    }

    /** 根据 paySn 读取 paylog.payCode 判定钱包（huifuAlipay -> alipay，其余 -> wechat） */
    private String walletByPaySn(String paySn) {
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        String payCode = paylog == null ? null : paylog.getPayCode();
        return walletByPayCode(payCode);
    }

    private String walletByPayCode(String payCode) {
        return payCode != null && payCode.toLowerCase().contains("alipay") ? "alipay" : "wechat";
    }

    // ============ PayService ============

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        return pay(orderVO, openid, clientType, ProfitSharingPaymentContext.none());
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType,
                                   ProfitSharingPaymentContext context) {
        requireConfigured();
        String wallet = walletByPaySn(orderVO.getPaySn());
        // PC 端：配置了统一收银台托管项目号(project_id)则走托管收银台（托管支付）预下单；
        // 未配置则回退到原聚合正扫(jspay)——因为托管收银台依赖 project_id。
        String hostingProjectId = "pc".equals(clientType)
                ? cfg(SettingsEnum.HUIFU_HOSTING_PROJECT_ID) : null;
        if (StrUtil.isNotBlank(hostingProjectId)) {
            if (context != null && "HUIFU".equals(context.getChannel())) {
                throw new GlobalException("支付配置已变更，请重新发起支付");
            }
            return payViaHostingCashier(orderVO, hostingProjectId);
        }
        String tradeType = resolveTradeType(wallet, clientType);

        // req_seq_id 全局唯一（req_date+req_seq_id 唯一）；沿用 Antom 做法附 uuid 后缀防重复下单被拒，
        // 回调时按 "-" 前段还原 paySn。
        String reqSeqId = orderVO.getPaySn() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String reqDate = LocalDate.now().format(REQ_DATE_FMT);
        String transAmt = orderVO.getUnpaidAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();

        JSONObject data = new JSONObject();
        data.set("req_date", reqDate);
        data.set("req_seq_id", reqSeqId);
        data.set("huifu_id", cfg(SettingsEnum.HUIFU_MERCHANT_ID));
        data.set("goods_desc", safeGoodsDesc(orderVO));
        data.set("trade_type", tradeType);
        data.set("trans_amt", transAmt);
        data.set("notify_url", getPayNotifyUrl("huifu"));
        data.set("delay_acct_flag", context != null && "HUIFU".equals(context.getChannel()) ? "Y" : "N");
        // 微信钱包扩展参数：sub_appid + sub_openid 必须成对且同源——openid 由哪个 appid 的授权获取，
        // sub_appid 就必须是同一个（小程序取小程序 AppID，公众号 JSAPI 取公众号 AppID），否则汇付/微信拒绝。
        if ("wechat".equals(wallet) && StrUtil.isNotBlank(openid)) {
            String subAppid = "miniProgram".equals(clientType)
                    ? cfg(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID)
                    : cfg(SettingsEnum.WECHAT_APP_ID);
            JSONObject wxData = new JSONObject();
            if (StrUtil.isNotBlank(subAppid)) {
                wxData.set("sub_appid", subAppid);
            }
            wxData.set("sub_openid", openid);
            data.set("wx_data", wxData.toString());
        }

        JSONObject resp = post(PATH_AGGREGATE_PAY, data);
        String respCode = resp.getStr("resp_code");
        // 斗拱聚合下单成功返回 00000100（受理成功、待用户支付），00000000 为即时完成类，二者均视为下单成功
        if (respCode != null && !respCode.startsWith("00000000") && !"00000000".equals(respCode) && !"00000100".equals(respCode)) {
            log.error("汇付下单失败，pay_sn:{}, resp:{}", orderVO.getPaySn(), resp);
            throw new GlobalException("汇付下单失败：" + resp.getStr("resp_desc"));
        }

        // 暂存 req_seq_id 到 paylog.transactionId；并把真实 req_date 存入 notifyData，
        // 供退款/查询下发 org_req_date（汇付必填=用户发起支付日期，避免用 addTime 推导跨天出错）
        try {
            JSONObject meta = new JSONObject();
            meta.set("hfReqDate", reqDate);
            paylogService.lambdaUpdate()
                    .eq(Paylog::getPaySn, orderVO.getPaySn())
                    .set(Paylog::getTransactionId, reqSeqId)
                    .set(Paylog::getNotifyData, meta.toString())
                    .update();
        } catch (Exception ex) {
            log.warn("汇付暂存 req_seq_id 到 paylog 失败，pay_sn:{}", orderVO.getPaySn(), ex);
        }

        return buildPayInfo(wallet, clientType, resp);
    }

    /**
     * PC 端统一收银台（托管支付）预下单。
     *
     * <p>汇付聚合网关不支持微信 PC 正扫（NATIVE），故 PC 走托管收银台预下单
     * （{@code /v2/trade/hosting/payment/preorder}，{@code request_type=P}），响应 {@code pay_info}
     * 为收银台页面地址，前端渲染为二维码供 PC 扫码支付。下单时写入 {@code paylog.tokenCode=HOSTING}，
     * 供退款/查询按原交易类型分流到托管接口。最终支付结果以异步通知为准。</p>
     */
    private PayCreateVO.PayInfo payViaHostingCashier(OrderVO orderVO, String projectId) {
        String reqSeqId = orderVO.getPaySn() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String reqDate = LocalDate.now().format(REQ_DATE_FMT);
        String transAmt = orderVO.getUnpaidAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();

        JSONObject data = new JSONObject();
        data.set("req_date", reqDate);
        data.set("req_seq_id", reqSeqId);
        data.set("huifu_id", cfg(SettingsEnum.HUIFU_MERCHANT_ID));
        data.set("trans_amt", transAmt);
        data.set("goods_desc", safeGoodsDesc(orderVO));
        // 预下单类型：1=普通托管收银台预下单
        data.set("pre_order_type", "1");
        // 统一收银台扩展参数：PC 页面版 + 项目信息 + 前端回跳地址
        JSONObject hostingData = new JSONObject();
        hostingData.set("project_title", safeGoodsDesc(orderVO));
        // project_id 为汇付托管项目号（后台可配 HUIFU_HOSTING_PROJECT_ID）；
        // 留空则不下发（用于验证是否强制，并避免下发非法/伪造值）
        if (StrUtil.isNotBlank(projectId)) {
            hostingData.set("project_id", projectId);
        }
        hostingData.set("request_type", "P");
        hostingData.set("callback_url", getReturnUrl(orderVO.getOrderId()));
        data.set("hosting_data", hostingData.toString());
        data.set("notify_url", getPayNotifyUrl("huifu"));
        // 一期即时到账，不冻结、不分账
        data.set("delay_acct_flag", "N");
        // 统一收银台为多支付方式托管页，由用户在汇付收银台自行选择微信/支付宝扫码，不下发 trans_type 锁定。
        // 实测锁定值 T_H5 被汇付判「交易类型不支持」，且官方通知报文 trans_type 枚举无 T_H5/T_NATIVE，
        // 统一收银台亦未提供合法的单一方式锁定枚举；如后续需锁定，待汇付确认合法参数后再补。

        JSONObject resp = post(PATH_HOSTING_PREORDER, data);
        String respCode = resp.getStr("resp_code");
        // 00000000 受理/完成、00000100 受理成功待支付，均视为下单成功
        if (respCode != null && !"00000000".equals(respCode) && !"00000100".equals(respCode)) {
            log.error("汇付统一收银台下单失败，pay_sn:{}, resp:{}", orderVO.getPaySn(), resp);
            throw new GlobalException("汇付下单失败：" + resp.getStr("resp_desc"));
        }

        // 暂存 req_seq_id + 托管渠道标记 + 真实 req_date（notifyData），供退款/查询分流与定位
        try {
            JSONObject meta = new JSONObject();
            meta.set("hfReqDate", reqDate);
            paylogService.lambdaUpdate()
                    .eq(Paylog::getPaySn, orderVO.getPaySn())
                    .set(Paylog::getTransactionId, reqSeqId)
                    .set(Paylog::getTokenCode, TOKEN_HOSTING)
                    .set(Paylog::getNotifyData, meta.toString())
                    .update();
        } catch (Exception ex) {
            log.warn("汇付统一收银台暂存 req_seq_id 到 paylog 失败，pay_sn:{}", orderVO.getPaySn(), ex);
        }

        // pay_info 为收银台页面地址，前端渲染为二维码供 PC 扫码支付
        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        payInfo.setCodeUrl(resp.getStr("pay_info"));
        return payInfo;
    }

    /**
     * 把汇付返回的 pay_info 按钱包/端映射到前端 PayInfo（具体字段联调核对）。
     */
    private PayCreateVO.PayInfo buildPayInfo(String wallet, String clientType, JSONObject resp) {
        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        String payInfoStr = resp.getStr("pay_info");
        String qrCode = resp.getStr("qr_code");

        if ("pc".equals(clientType)) {
            // 正扫二维码
            payInfo.setCodeUrl(StrUtil.isNotBlank(qrCode) ? qrCode : payInfoStr);
            return payInfo;
        }
        if ("alipay".equals(wallet)) {
            // 支付宝 JS/生活号：pay_info 为可拉起的交易串
            payInfo.setProvider("alipay");
            payInfo.setHtml(payInfoStr);
            payInfo.setOrderInfo(payInfoStr);
            return payInfo;
        }
        // 微信 JSAPI/小程序：pay_info 通常为 wx.chooseWXPay 参数 JSON
        payInfo.setProvider("wxpay");
        if (StrUtil.isNotBlank(payInfoStr) && JSONUtil.isTypeJSON(payInfoStr)) {
            JSONObject wx = JSONUtil.parseObj(payInfoStr);
            payInfo.setAppId(wx.getStr("appId", wx.getStr("appid")));
            payInfo.setTimeStamp(wx.getStr("timeStamp"));
            payInfo.setNonceStr(wx.getStr("nonceStr"));
            payInfo.setPackageValue(wx.getStr("package", wx.getStr("packageValue")));
            payInfo.setSignType(wx.getStr("signType"));
            payInfo.setPaySign(wx.getStr("paySign"));
        } else {
            payInfo.setOrderInfo(payInfoStr);
        }
        return payInfo;
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount,
                                 BigDecimal totalAmount, String clientType, String subMchId) {
        requireConfigured();
        String reqDate = LocalDate.now().format(REQ_DATE_FMT);
        // 原交易定位（汇付要求：org_req_seq_id/org_hf_seq_id/org_party_order_id 三选一 + org_req_date 必填）：
        // 优先用支付成功回写的汇付全局流水号 org_hf_seq_id（存于 notifyData，最稳，不受 transactionId 覆盖影响）；
        // org_req_date 优先用下单时存的真实 req_date（notifyData.hfReqDate）。历史旧单无该信息时兜底。
        // tokenCode=HOSTING 表示原交易走统一收银台（托管支付），退款须走托管退款接口。
        Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, outTradeNo));
        boolean hosting = paylog != null && TOKEN_HOSTING.equals(paylog.getTokenCode());
        JSONObject meta = huifuMeta(paylog);
        String hfSeqId = meta.getStr("hfSeqId");
        String hfReqDate = meta.getStr("hfReqDate");

        JSONObject data = new JSONObject();
        data.set("req_date", reqDate);
        data.set("req_seq_id", refundNo);
        data.set("huifu_id", cfg(SettingsEnum.HUIFU_MERCHANT_ID));
        data.set("ord_amt", refundAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        if (StrUtil.isNotBlank(hfSeqId)) {
            data.set("org_hf_seq_id", hfSeqId);
        } else {
            // 兜底：历史旧单无汇付流水号时退回原请求流水号（transactionId）
            String orgReqSeqId = paylog != null && StrUtil.isNotBlank(paylog.getTransactionId())
                    ? paylog.getTransactionId() : outTradeNo;
            data.set("org_req_seq_id", orgReqSeqId);
        }
        // org_req_date 汇付必填=用户发起支付日期；优先真实 req_date，兜底用 addTime 推导
        data.set("org_req_date", StrUtil.isNotBlank(hfReqDate) ? hfReqDate : reqDateOfPaylog(paylog));
        // 预留异步退款回调（汇付退款以同步为主，若支持异步则回调此地址）
        data.set("notify_url", getRefundNotifyUrl("huifu"));


        try {
            JSONObject resp = post(hosting ? PATH_HOSTING_REFUND : PATH_REFUND, data);
            String transStat = resp.getStr("trans_stat");
            String respCode = resp.getStr("resp_code");
            boolean ok = "S".equalsIgnoreCase(transStat) || "P".equalsIgnoreCase(transStat)
                    || (respCode != null && respCode.startsWith("00000000"));
            if (ok && "S".equalsIgnoreCase(transStat)) {
                // 同步明确成功：直接回写退款成功（RefundApplyServiceImpl 会据 code=SUCCESS 触发 refundSuccess）
                return new PayNotifyVO("SUCCESS", "退款成功");
            }
            if (ok) {
                // 处理中：等待异步退款回调
                log.info("汇付退款受理处理中，refund_sn:{}, trans_stat:{}", refundNo, transStat);
                return new PayNotifyVO("PROCESSING", "退款处理中");
            }
            log.error("汇付退款失败，refund_sn:{}, resp:{}", refundNo, resp);
            return new PayNotifyVO("FAIL", "退款失败：" + resp.getStr("resp_desc"));
        } catch (Exception e) {
            log.error("汇付退款异常，refund_sn:{}", refundNo, e);
            return new PayNotifyVO("FAIL", "退款异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payNotify(Map<String, Object> map) {
        JSONObject data = extractVerifiedData(map);
        if (data == null) {
            return "FAIL";
        }
        try {
            String transStat = data.getStr("trans_stat");
            if (!"S".equalsIgnoreCase(transStat)) {
                log.info("汇付支付通知非成功状态，data:{}", data);
                return "FAIL";
            }
            String reqSeqId = data.getStr("req_seq_id");
            String hfSeqId = data.getStr("hf_seq_id");
            String paySn = resolvePaySn(reqSeqId);
            if (StrUtil.isBlank(paySn)) {
                log.error("汇付支付通知无法还原 pay_sn，req_seq_id:{}, data:{}", reqSeqId, data);
                return "FAIL";
            }
            // 把汇付全局流水号并入 notifyData（保留下单存的 hfReqDate），
            // 供退款/查询用 org_hf_seq_id 定位；幂等，重复通知也安全
            if (StrUtil.isNotBlank(hfSeqId)) {
                try {
                    Paylog cur = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
                    JSONObject meta = huifuMeta(cur);
                    meta.set("hfSeqId", hfSeqId);
                    paylogService.lambdaUpdate()
                            .eq(Paylog::getPaySn, paySn)
                            .set(Paylog::getNotifyData, meta.toString())
                            .update();
                } catch (Exception ex) {
                    log.warn("汇付回写 hf_seq_id 到 notifyData 失败，pay_sn:{}", paySn, ex);
                }
            }
            if (!checkOrderPayStatus(paySn)) {
                paySuccess(paySn, StrUtil.isNotBlank(hfSeqId) ? hfSeqId : reqSeqId);
                log.info("汇付支付成功处理完成，pay_sn:{}, hf_seq_id:{}", paySn, hfSeqId);
            } else {
                log.info("汇付订单已支付，跳过，pay_sn:{}", paySn);
            }
            // 汇付斗拱通知成功应答约定：RECV_ORD_ID_{req_seq_id}，避免汇付误判未收到而重复重试
            return "RECV_ORD_ID_" + reqSeqId;
        } catch (Exception e) {
            log.error("汇付支付通知处理异常", e);
            return "FAIL";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refundNotify(Map<String, Object> map) {
        JSONObject data = extractVerifiedData(map);
        if (data == null) {
            return "FAIL";
        }
        try {
            String transStat = data.getStr("trans_stat");
            if (!"S".equalsIgnoreCase(transStat)) {
                log.info("汇付退款通知非成功状态，data:{}", data);
                return "FAIL";
            }
            String refundSn = data.getStr("req_seq_id");
            if (StrUtil.isBlank(refundSn)) {
                log.error("汇付退款通知缺失 req_seq_id");
                return "FAIL";
            }
            refundSuccess(refundSn);
            log.info("汇付退款成功处理完成，refund_sn:{}", refundSn);
            // 汇付斗拱通知成功应答约定：RECV_ORD_ID_{req_seq_id}
            return "RECV_ORD_ID_" + refundSn;
        } catch (Exception e) {
            log.error("汇付退款通知处理异常", e);
            return "FAIL";
        }
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        try {
            requireConfigured();
            Paylog paylog = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, outTradeNo));
            boolean hosting = paylog != null && TOKEN_HOSTING.equals(paylog.getTokenCode());
            JSONObject meta = huifuMeta(paylog);
            String hfSeqId = meta.getStr("hfSeqId");
            String hfReqDate = meta.getStr("hfReqDate");
            // 定位标识三选一：显式 tradeNo > 汇付全局流水号 hfSeqId > 原请求流水号 transactionId
            String orgReqSeqId = StrUtil.isNotBlank(tradeNo)
                    ? tradeNo
                    : (paylog != null ? paylog.getTransactionId() : null);
            if (StrUtil.isBlank(hfSeqId) && StrUtil.isBlank(orgReqSeqId)) {
                return null;
            }
            JSONObject data = new JSONObject();
            data.set("huifu_id", cfg(SettingsEnum.HUIFU_MERCHANT_ID));
            if (StrUtil.isBlank(tradeNo) && StrUtil.isNotBlank(hfSeqId)) {
                data.set("org_hf_seq_id", hfSeqId);
            } else {
                data.set("org_req_seq_id", orgReqSeqId);
            }
            // org_req_date：优先真实下单日期，兜底用 addTime 推导，避免跨天查询失败
            data.set("org_req_date", StrUtil.isNotBlank(hfReqDate) ? hfReqDate : reqDateOfPaylog(paylog));
            if (hosting) {
                // 托管支付查询需带本次请求流水
                data.set("req_date", LocalDate.now().format(REQ_DATE_FMT));
                data.set("req_seq_id", outTradeNo + "-q" + UUID.randomUUID().toString().replace("-", "").substring(0, 6));
            }
            JSONObject resp = post(hosting ? PATH_HOSTING_QUERY : PATH_QUERY, data);
            PayPlatformStateBO bo = new PayPlatformStateBO();
            bo.setOutTradeNo(outTradeNo);
            bo.setTradeNo(Optional.ofNullable(resp.getStr("hf_seq_id")).orElse(orgReqSeqId));
            bo.setTradeState(Optional.ofNullable(resp.getStr("trans_stat")).orElse("UNKNOWN"));
            return bo;
        } catch (Exception e) {
            log.error("汇付查询支付状态异常，outTradeNo:{}", outTradeNo, e);
            return null;
        }
    }

    // ============ helpers ============

    /** 从回调 map 取业务数据并验签（回调 body 通常携带 data/resp_data + sign 字段） */
    private JSONObject extractVerifiedData(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        String dataStr = firstNonBlank(map, "resp_data", "data");
        String signStr = firstNonBlank(map, "sign");
        // 兼容原始 body 通道：requestBody 为整包 JSON
        if (dataStr == null && map.get("requestBody") != null) {
            JSONObject body = JSONUtil.parseObj(String.valueOf(map.get("requestBody")));
            dataStr = Optional.ofNullable(body.getStr("resp_data")).orElse(body.getStr("data"));
            signStr = body.getStr("sign");
        }
        if (StrUtil.isBlank(dataStr) || StrUtil.isBlank(signStr)) {
            log.error("汇付回调缺失 data/sign");
            return null;
        }
        // 异步通知按原始 resp_data 串验签（不排序），与汇付签名口径一致
        if (!verifyNotify(dataStr, signStr)) {
            log.error("汇付回调验签失败，data:{}", dataStr);
            return null;
        }
        return JSONUtil.parseObj(dataStr);
    }

    private String firstNonBlank(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null && StrUtil.isNotBlank(String.valueOf(v))) {
                return String.valueOf(v);
            }
        }
        return null;
    }

    private String extractPaySn(String reqSeqId) {
        if (StrUtil.isBlank(reqSeqId)) {
            return null;
        }
        int idx = reqSeqId.lastIndexOf('-');
        return idx <= 0 ? reqSeqId : reqSeqId.substring(0, idx);
    }

    /**
     * 从回调 req_seq_id 还原业务 paySn，双通道兜底：
     * 1) 按 "-" 截断得到候选 paySn，命中 paylog 则返回；
     * 2) 截断结果未命中时，用完整 req_seq_id 反查 paylog.transactionId（下单时写入），取其 paySn。
     * 避免仅依赖回调原样回传 req_seq_id 的字符串规则。
     */
    private String resolvePaySn(String reqSeqId) {
        if (StrUtil.isBlank(reqSeqId)) {
            return null;
        }
        String candidate = extractPaySn(reqSeqId);
        if (StrUtil.isNotBlank(candidate)
                && paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, candidate)) != null) {
            return candidate;
        }
        Paylog byTxn = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getTransactionId, reqSeqId));
        if (byTxn != null && StrUtil.isNotBlank(byTxn.getPaySn())) {
            return byTxn.getPaySn();
        }
        return candidate;
    }

    /**
     * 用 paylog.addTime（Unix 秒，下单时间）推导汇付 org_req_date（yyyyMMdd）。
     * 缺失时退回当天，避免 NPE。
     */
    private String reqDateOfPaylog(Paylog paylog) {
        if (paylog != null && paylog.getAddTime() != null) {
            return Instant.ofEpochSecond(paylog.getAddTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(REQ_DATE_FMT);
        }
        return LocalDate.now().format(REQ_DATE_FMT);
    }

    /**
     * 解析 paylog.notifyData 中存的汇付原交易定位信息（下单存 hfReqDate，支付通知并入 hfSeqId）。
     * 兼容历史旧单（空/非 JSON）返回空对象，由调用方走 addTime/transactionId 兜底。
     */
    private JSONObject huifuMeta(Paylog paylog) {
        if (paylog == null || StrUtil.isBlank(paylog.getNotifyData())) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(paylog.getNotifyData());
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private String safeGoodsDesc(OrderVO orderVO) {
        String name = "Tigshop Order " + orderVO.getOrderSn();
        return name.length() > 100 ? name.substring(0, 100) : name;
    }
}
