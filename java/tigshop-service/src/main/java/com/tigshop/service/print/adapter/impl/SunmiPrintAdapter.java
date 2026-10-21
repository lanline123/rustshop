// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.adapter.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.print.adapter.BindResult;
import com.tigshop.bean.dto.print.adapter.PushResult;
import com.tigshop.bean.dto.print.adapter.UnbindResult;
import com.tigshop.bean.enums.print.PrintLogStatusEnum;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.common.utils.SunmiCloudPrintApiUtils;
import com.tigshop.feign.SunmiCloudPrintApiClient;
import com.tigshop.service.print.adapter.PrintPlatformAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 商米云打印机 V2 适配器（直推模式）
 * <p>
 * 官方文档：<a href="https://docs.sunmi.com/zh-CN/cdixeghjk491/xffdeghjk524">3、开发对接API接口</a>。
 * <p>
 * 协议细节（以云打印机 V2 文档原文为准，与通用 sunmi-openapi Java SDK 不同）：
 * <ul>
 *     <li>必传 {@code Source: openapi} header</li>
 *     <li>成功 code 为 {@code 10000}</li>
 *     <li>{@code shop_id} 为 JSON number（正整数），若无店铺需传空字符串 {@code ""}（文档原文约定）</li>
 *     <li>nonce 6 位随机数</li>
 * </ul>
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SunmiPrintAdapter implements PrintPlatformAdapter {

    /**
     * 云打印机 V2 成功码：<b>真实实现为 1</b>（文档里写的 10000 是文档错误，2026-04-22
     * smoke test 用 SN=6972253511562 实测 bindShop/pushContent/onlineStatus 成功响应都是 code=1）。
     */
    private static final int SUCCESS_CODE = 1;

    /**
     * 新订单类型（商米 order_type：1新订单/2取消订单/3催单/4退单/5其它）。
     * 本字段文档标注为"非必填"，MVP 阶段固定新订单，后续可从上下文传入。
     */
    private static final int ORDER_TYPE_NEW = 1;

    /**
     * 离线错误码（商米官方错误码表在 /design-pack 中，具体数值待 TigShop 联调后核实）
     * MVP 阶段通过消息关键字兜底识别
     */
    private static final String[] OFFLINE_MSG_KEYWORDS = {"离线", "offline", "设备不在线", "设备未连接"};

    /**
     * 商米无效 shop_id 的兜底占位（商米要求 shop_id 为正整数）。
     * <p>
     * 文档 §bindShop 备注里有"可传空字符串"字样，但实际后端按 integer 严格反序列化，
     * 2026-04-22 联调探针已证实：空字符串和 0 都会触发 10071400 parameter error。
     * 为避免业务侧 shop_id 脏数据（null / 0）把请求打废，此处兜底替换为 1，
     * 同时打 WARN 日志以便对账时追查。
     */
    private static final int SHOP_ID_FALLBACK = 1;

    /**
     * unbind 时视为"云端本就没有此设备 → 幂等成功"的错误码集合。
     * <p>
     * 覆盖文档 §接口错误码中三种"查不到设备归属"的场景：
     * {@code 10071701} 未知设备 / {@code 10071704} 不属本渠道 / {@code 10071707} 没有设备或不是此通道。
     * <p>
     * 修复背景（2026-04-22）：原先仅按 msg 中文关键词（"不存在"/"未绑定"）做幂等，
     * 但商米真实响应是英文 {@code device is unknown}，导致 {@code del()} 永远抛 {@code GlobalException}，
     * 用户无法删除"商米云已经没有"的本地打印机记录。
     */
    private static final Set<Integer> UNBIND_IDEMPOTENT_CODES = Set.of(10071701, 10071704, 10071707);

    /**
     * 商米云打印机 V2 业务错误码 → 给业务看的中文提示映射（文档 §接口错误码）。
     * <p>
     * 未命中时 fallback 到商米原始 msg。
     */
    private static final Map<Integer, String> ERROR_CODE_MESSAGES = Map.of(
            10071400, "商米拒绝参数：请检查 shop_id 是否为正整数、SN 是否正确",
            10071500, "商米云服务器错误，请稍后重试",
            10071701, "商米云查不到该打印机 SN，通常是 SN 错或未被商米销售绑定到本渠道下，请联系商米销售",
            10071702, "商米云：设备已绑定",
            10071703, "商米云：设备绑定异常，请联系商米技术支持",
            10071704, "该 SN 不属于本开发者渠道，请联系商米销售绑定到贵司渠道下",
            10071705, "商米云：订单已推送（订单号重复）",
            10071706, "商米云：订单未知",
            10071707, "商米云：没有该设备或不在此通道，请联系商米销售"
    );

    private final SunmiCloudPrintApiClient sunmiClient;

    @Override
    public Integer getPlatformCode() {
        return PrintPlatformEnum.SUNMI.getCode();
    }

    @Override
    public BindResult bind(Print print) {
        BindResult preCheck = preCheckCredentials(print, "商米云绑定");
        if (preCheck != null) {
            return preCheck;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sn", print.getPrintSn());
        payload.put("shop_id", shopIdForPayload(print.getShopId()));
        String body = buildJson(payload);
        SignCtx ctx = signCtx(body, print);
        logRequest("bindShop", print, body, ctx);
        String raw = safeCall(() -> sunmiClient.bindShop(
                print.getThirdAccount(), ctx.timestamp, ctx.nonce, ctx.sign,
                SunmiCloudPrintApiClient.SOURCE_OPENAPI, body));
        logResponse("bindShop", raw);
        JSONObject resp = parseResponse(raw);
        if (resp != null && isSuccessCode(resp)) {
            return BindResult.builder().success(true).rawResponse(raw).build();
        }
        return BindResult.builder()
                .success(false)
                .errorCode(codeStr(resp))
                .errorMsg(friendlyErrorMsg(resp, "绑定失败"))
                .rawResponse(raw)
                .build();
    }

    @Override
    public PushResult push(Print print, String orderId, String content, Integer copies, String mediaText) {
        int times = copies == null || copies <= 0 ? 1 : copies;
        // 2026-04-22 smoke test 证实云打印机 V2 的字段名是 trade_no / count（不是通用 OpenAPI 的 order_id / copies），
        // 真实响应：{"code":1,"data":{"trade_no":"..."},"msg":"success"}
        // pushContent 不需要 shop_id（文档原文和实测均未要求）
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sn", print.getPrintSn());
        payload.put("trade_no", nullSafe(orderId));
        payload.put("order_type", ORDER_TYPE_NEW);
        payload.put("content", nullSafe(content));
        payload.put("count", times);
        // 2026-04-22 语音播报支持：非空 mediaText 会随 pushContent 一并下发，机器 TTS 播报一次
        // （文档 §pushContent：media_text 非必须，cycle 默认 1 次）
        if (mediaText != null && !mediaText.isEmpty()) {
            payload.put("media_text", mediaText);
            payload.put("cycle", 1);
        }
        String body = buildJson(payload);

        SignCtx ctx = signCtx(body, print);
        logRequest("pushContent", print, body, ctx);
        String raw = safeCall(() -> sunmiClient.pushContent(
                print.getThirdAccount(), ctx.timestamp, ctx.nonce, ctx.sign,
                SunmiCloudPrintApiClient.SOURCE_OPENAPI, body));
        logResponse("pushContent", raw);
        JSONObject resp = parseResponse(raw);

        if (resp != null && isSuccessCode(resp)) {
            return PushResult.builder()
                    .success(true)
                    .mappedStatus(PrintLogStatusEnum.PUSHED.getCode())
                    .taskNo(orderId)
                    .rawResponse(raw)
                    .build();
        }
        Integer mapped = mapToLogStatus(resp);
        return PushResult.builder()
                .success(false)
                .mappedStatus(mapped)
                .taskNo(orderId)
                .errorCode(codeStr(resp))
                .errorMsg(friendlyErrorMsg(resp, "推送失败"))
                .rawResponse(raw)
                .build();
    }

    @Override
    public UnbindResult unbind(Print print) {
        UnbindResult preCheck = preCheckUnbindCredentials(print);
        if (preCheck != null) {
            return preCheck;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sn", print.getPrintSn());
        payload.put("shop_id", shopIdForPayload(print.getShopId()));
        String body = buildJson(payload);
        SignCtx ctx = signCtx(body, print);
        logRequest("unbindShop", print, body, ctx);
        String raw = safeCall(() -> sunmiClient.unbindShop(
                print.getThirdAccount(), ctx.timestamp, ctx.nonce, ctx.sign,
                SunmiCloudPrintApiClient.SOURCE_OPENAPI, body));
        logResponse("unbindShop", raw);
        JSONObject resp = parseResponse(raw);
        if (resp != null && isSuccessCode(resp)) {
            return UnbindResult.builder().success(true).rawResponse(raw).build();
        }
        // 幂等处理：商米返回"云端本就没有此设备"类错误，视为已解绑成功（避免本地记录永远删不掉）
        Integer code = resp == null ? null : resp.getInt("code");
        if (code != null && UNBIND_IDEMPOTENT_CODES.contains(code)) {
            String raw_msg = msgOr(resp, "");
            log.info("[商米云打印] unbind 幂等处理：code={} msg={}（视为已解绑）", code, raw_msg);
            return UnbindResult.builder()
                    .success(true)
                    .errorMsg("商米云端无此设备（" + code + " " + raw_msg + "），视为已解绑")
                    .rawResponse(raw)
                    .build();
        }
        // 兜底：历史中文关键词匹配（罕见场景）
        String msg = msgOr(resp, "");
        if (msg.contains("不存在") || msg.contains("未绑定") || msg.contains("not found") || msg.contains("unknown")) {
            return UnbindResult.builder()
                    .success(true)
                    .errorMsg(msg + "（视为已解绑）")
                    .rawResponse(raw)
                    .build();
        }
        return UnbindResult.builder()
                .success(false)
                .errorCode(codeStr(resp))
                .errorMsg(friendlyErrorMsg(resp, "解绑失败"))
                .rawResponse(raw)
                .build();
    }

    // ==== 私有工具 ====

    /**
     * 把 TigShop 侧的 shopId 转成商米 bindShop / pushContent / unbindShop 的 shop_id 字段值。
     * <p>
     * 2026-04-22 探针实测结果（{@code .ai-os/design-pack/sunmi-bind-probe.py}）：
     * <ul>
     *     <li>{@code shop_id: 1}   → 10071701 / 10071702 等业务码（网关已接受类型）</li>
     *     <li>{@code shop_id: 0}   → 10071400 parameter error（网关拒绝）</li>
     *     <li>{@code shop_id: ""}  → 10071400 parameter error（网关拒绝，尽管文档备注说可传）</li>
     * </ul>
     * 因此本方法对 null / 0 / 负数 一律兜底为 {@link #SHOP_ID_FALLBACK}，保证请求能进入业务层，
     * 同时打 WARN 让运维对账可追。合法正整数原样返回 {@link Integer}，JSON 序列化后为 number。
     */
    private static Object shopIdForPayload(Integer shopId) {
        if (shopId == null || shopId <= 0) {
            log.warn("[商米云打印] shop_id 非法（{}），已兜底为 {}；请业务侧确认打印机所属店铺",
                    shopId, SHOP_ID_FALLBACK);
            return 1;
        }
        return shopId + 1;
    }

    /**
     * 把商米错误响应转成给业务看的友好中文消息。
     * 格式：商米云({action}), {sunmi原始msg}
     */
    private static String friendlyErrorMsg(JSONObject resp, String action) {
        if (resp == null) {
            return "商米云(" + action + ")";
        }
        Integer code = resp.getInt("code");
        String mapped = code == null ? null : ERROR_CODE_MESSAGES.get(code);
        if (mapped != null) {
            return "商米云(" + action + "), " + mapped;
        }
        String msg = resp.getStr("msg");
        if (msg == null || msg.isEmpty()) {
            return "商米云(" + action + ")";
        }
        return "商米云(" + action + "), " + msg;
    }

    /**
     * 凭证前置校验：缺 AppID/AppKey/SN 时直接失败，避免把明显错误的请求发给商米。
     * 注意：shop_id 允许为 null/0（此时按文档传 ""），不在必填校验范围。
     */
    private static BindResult preCheckCredentials(Print print, String scene) {
        String missing = detectMissingCredential(print);
        if (missing == null) {
            return null;
        }
        log.warn("{}前置校验失败：{}", scene, missing);
        return BindResult.builder().success(false).errorMsg("商米云(绑定失败), " + missing).build();
    }

    private static UnbindResult preCheckUnbindCredentials(Print print) {
        String missing = detectMissingCredential(print);
        if (missing == null) {
            return null;
        }
        log.warn("商米云解绑前置校验失败：{}", missing);
        return UnbindResult.builder().success(false).errorMsg("商米云(解绑失败), " + missing).build();
    }

    private static String detectMissingCredential(Print print) {
        if (print == null) {
            return "入参为空";
        }
        if (print.getPrintSn() == null || print.getPrintSn().isBlank()) {
            return "打印机 SN 为空";
        }
        if (print.getThirdAccount() == null || print.getThirdAccount().isBlank()) {
            return "AppID 未配置";
        }
        if (print.getThirdKey() == null || print.getThirdKey().isBlank()) {
            return "AppKey 未配置";
        }
        return null;
    }

    /**
     * 生成一次性签名上下文：ts/nonce/sign 共享同一 timestamp+nonce 值，
     * 避免签名与 header 对不上。
     */
    private static SignCtx signCtx(String body, Print print) {
        String ts = SunmiCloudPrintApiUtils.getCurrentTimestamp();
        String nonce = SunmiCloudPrintApiUtils.generateNonce();
        String sign = SunmiCloudPrintApiUtils.generateSign(body, print.getThirdAccount(), ts, nonce, print.getThirdKey());
        return new SignCtx(ts, nonce, sign);
    }

    /**
     * Feign 调用时异常兜底，返回 null 让上层走失败分支（不抛出以保证消费者
     * 里 try/catch 不会中断 print_log 写入）。
     */
    private static String safeCall(java.util.concurrent.Callable<String> call) {
        try {
            return call.call();
        } catch (Exception e) {
            log.error("商米云 API 调用失败", e);
            return null;
        }
    }

    private record SignCtx(String timestamp, String nonce, String sign) {
    }

    /**
     * 联调期请求日志。body 不含敏感信息（只有 sn/shop_id/trade_no/hex content），可放心打印。
     */
    private static void logRequest(String api, Print print, String body, SignCtx ctx) {
        if (!log.isInfoEnabled()) {
            return;
        }
        log.info("[商米云打印] -> {} appid={} ts={} nonce={} sign={} body={}",
                api, print.getThirdAccount(), ctx.timestamp, ctx.nonce, ctx.sign, body);
    }

    private static void logResponse(String api, String raw) {
        log.info("[商米云打印] <- {} raw={}", api, raw);
    }

    private static String buildJson(Map<String, ?> map) {
        return JSONUtil.toJsonStr(map);
    }

    private static JSONObject parseResponse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return JSONUtil.parseObj(raw);
        } catch (Exception e) {
            log.warn("商米云响应解析失败：{}", raw);
            return null;
        }
    }

    /**
     * 云打印机 V2 文档原文：code == 10000 表示成功。
     */
    private static boolean isSuccessCode(JSONObject resp) {
        Integer code = resp.getInt("code");
        return code != null && code == SUCCESS_CODE;
    }

    private static String codeStr(JSONObject resp) {
        if (resp == null) {
            return null;
        }
        Integer code = resp.getInt("code");
        return code == null ? null : String.valueOf(code);
    }

    private static String msgOr(JSONObject resp, String fallback) {
        if (resp == null) {
            return fallback;
        }
        String msg = resp.getStr("msg");
        return msg == null || msg.isEmpty() ? fallback : msg;
    }

    private static Integer mapToLogStatus(JSONObject resp) {
        // 推送失败/离线区分：优先按消息关键字识别"离线"，否则算一般失败
        String msg = msgOr(resp, "").toLowerCase();
        for (String kw : OFFLINE_MSG_KEYWORDS) {
            if (msg.contains(kw.toLowerCase())) {
                return PrintLogStatusEnum.OFFLINE.getCode();
            }
        }
        return PrintLogStatusEnum.PUSH_FAILED.getCode();
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }
}
