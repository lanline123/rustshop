package com.tigshop.bean.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.dto.content.WechatLiveContextDTO;
import com.tigshop.common.utils.StringUtils;

import java.util.*;

import static com.tigshop.common.constant.content.WechatLiveOrderSyncConstants.*;

/**
 * 微信直播订单上下文工具
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
public class WechatLiveOrderContextUtils {

    private WechatLiveOrderContextUtils() {
    }

    public static String mergeIntoExtraData(String extraData, WechatLiveContextDTO context) {
        JSONObject jsonObject = parseObject(extraData);
        if (context != null && !isEmpty(context)) {
            jsonObject.put(EXTRA_DATA_WECHAT_LIVE_CONTEXT, JSON.toJSON(context));
        }
        return jsonObject.isEmpty() ? "" : jsonObject.toJSONString();
    }

    public static WechatLiveContextDTO extractFromExtraData(String extraData) {
        JSONObject jsonObject = parseObject(extraData);
        if (!jsonObject.containsKey(EXTRA_DATA_WECHAT_LIVE_CONTEXT)) {
            return null;
        }
        return jsonObject.getObject(EXTRA_DATA_WECHAT_LIVE_CONTEXT, WechatLiveContextDTO.class);
    }

    public static boolean isSameContext(String extraData, WechatLiveContextDTO context) {
        WechatLiveContextDTO current = extractFromExtraData(extraData);
        if (isEmpty(current) && isEmpty(context)) {
            return true;
        }
        if (isEmpty(current) || isEmpty(context)) {
            return false;
        }
        return Objects.equals(current.getWechatLiveId(), context.getWechatLiveId())
                && Objects.equals(current.getRoomId(), context.getRoomId())
                && Objects.equals(current.getWechatGoodsId(), context.getWechatGoodsId())
                && Objects.equals(current.getProductId(), context.getProductId())
                && Objects.equals(current.getSkuId(), context.getSkuId())
                && Objects.equals(current.getShareOpenId(), context.getShareOpenId())
                && Objects.equals(current.getViewerOpenId(), context.getViewerOpenId())
                && Objects.equals(current.getScene(), context.getScene())
                && Objects.equals(current.getCustomParams(), context.getCustomParams());
    }

    public static String mergeIntoPromotionData(String promotionData, WechatLiveContextDTO context) {
        JSONObject jsonObject = parsePromotionObject(promotionData);
        if (context != null && !isEmpty(context)) {
            jsonObject.put(EXTRA_DATA_WECHAT_LIVE_CONTEXT, JSON.toJSON(context));
        }
        return jsonObject.isEmpty() ? "" : jsonObject.toJSONString();
    }

    public static WechatLiveContextDTO extractFromPromotionData(String promotionData) {
        return extractFromExtraData(promotionData);
    }

    public static String enrichOrderExtension(String orderExtension, Collection<WechatLiveContextDTO> contexts) {
        JSONObject jsonObject = parseObject(orderExtension);
        List<WechatLiveContextDTO> validContexts = normalizeContexts(contexts);
        if (!validContexts.isEmpty()) {
            jsonObject.put(ORDER_EXTENSION_SUB_SOURCE, ORDER_SUB_SOURCE_WECHAT_LIVE);
            jsonObject.put(ORDER_EXTENSION_WECHAT_LIVE_CONTEXTS, JSON.toJSON(validContexts));
        }
        return jsonObject.toJSONString();
    }

    public static List<WechatLiveContextDTO> extractFromOrderExtension(String orderExtension) {
        JSONObject jsonObject = parseObject(orderExtension);
        JSONArray jsonArray = jsonObject.getJSONArray(ORDER_EXTENSION_WECHAT_LIVE_CONTEXTS);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return Collections.emptyList();
        }
        return jsonArray.toJavaList(WechatLiveContextDTO.class);
    }

    public static String extractSubSource(String orderExtension) {
        JSONObject jsonObject = parseObject(orderExtension);
        return jsonObject.getString(ORDER_EXTENSION_SUB_SOURCE);
    }

    public static String applyLastCallback(String orderExtension, JSONObject callbackSnapshot) {
        JSONObject jsonObject = parseObject(orderExtension);
        if (callbackSnapshot != null && !callbackSnapshot.isEmpty()) {
            jsonObject.put(ORDER_EXTENSION_WECHAT_LIVE_LAST_CALLBACK, callbackSnapshot);
        }
        return jsonObject.toJSONString();
    }

    public static boolean isEmpty(WechatLiveContextDTO context) {
        return context == null
                || (context.getWechatLiveId() == null
                && context.getRoomId() == null
                && context.getWechatGoodsId() == null
                && context.getProductId() == null
                && context.getSkuId() == null
                && StringUtils.isBlank(context.getShareOpenId())
                && StringUtils.isBlank(context.getViewerOpenId())
                && StringUtils.isBlank(context.getScene())
                && StringUtils.isBlank(context.getCustomParams()));
    }

    private static JSONObject parseObject(String json) {
        if (StringUtils.isBlank(json)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private static JSONObject parsePromotionObject(String json) {
        if (StringUtils.isBlank(json)) {
            return new JSONObject();
        }
        try {
            Object parsed = JSON.parse(json);
            if (parsed instanceof JSONObject jsonObject) {
                return jsonObject;
            }
            JSONObject wrapper = new JSONObject();
            wrapper.put("originPromotionData", parsed);
            return wrapper;
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private static List<WechatLiveContextDTO> normalizeContexts(Collection<WechatLiveContextDTO> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, WechatLiveContextDTO> contextMap = new LinkedHashMap<>();
        for (WechatLiveContextDTO context : contexts) {
            if (isEmpty(context)) {
                continue;
            }
            contextMap.put(buildContextKey(context), context);
        }
        return new ArrayList<>(contextMap.values());
    }

    private static String buildContextKey(WechatLiveContextDTO context) {
        return String.join("_",
                String.valueOf(context.getWechatLiveId()),
                String.valueOf(context.getRoomId()),
                String.valueOf(context.getWechatGoodsId()),
                String.valueOf(context.getProductId()),
                String.valueOf(context.getSkuId()),
                String.valueOf(context.getShareOpenId()),
                String.valueOf(context.getViewerOpenId()),
                String.valueOf(context.getScene()),
                String.valueOf(context.getCustomParams()));
    }
}
