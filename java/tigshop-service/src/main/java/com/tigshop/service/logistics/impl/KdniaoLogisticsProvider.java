package com.tigshop.service.logistics.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.enums.KuaiDiNiaoTypes;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.feign.ShippingInfoClient;
import com.tigshop.mapper.order.LogisticsApiLogMapper;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.service.logistics.LogisticsProvider;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 快递鸟物流服务商实现（电子面单 1007 + 即时轨迹 8001）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KdniaoLogisticsProvider implements LogisticsProvider {

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final ConfigService configService;
    private final LogisticsApiLogMapper logisticsApiLogMapper;
    private final ShippingInfoClient shippingInfoClient;

    @Override
    public int providerType() {
        return PROVIDER_KDNIAO;
    }

    @Override
    public boolean isConfigured() {
        return StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.KDNIAO_BUSINESS_ID))
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.KDNIAO_API_KEY));
    }

    @Override
    public Map<String, Object> createElectronicWaybill(OrderVO order, String remark) {
        LogisticsCompany logistics = logisticsCompanyMapper.selectById(order.getLogisticsId());
        if (logistics == null) {
            throw new GlobalException("未存在物流公司信息");
        }
        if (StrUtil.isBlank(logistics.getExpType())) {
            throw new GlobalException("物流公司[" + logistics.getLogisticsName() + "]未配置快递类型(ExpType)，请在后台物流公司管理中完善后再发货");
        }

        List<String> regionNames = order.getRegionNames();
        List<Integer> regionIds = order.getRegionIds();

        Map<String, Object> receiver = new HashMap<>();
        receiver.put("Name", order.getConsignee());
        receiver.put("Mobile", order.getMobile());
        receiver.put("ProvinceName", regionIds.get(0) > 1 ? regionNames.get(0) : regionNames.get(1));
        receiver.put("CityName", regionIds.get(0) > 1 ? regionNames.get(1) : regionNames.get(2));
        receiver.put("ExpAreaName", regionIds.get(0) > 1 ? regionNames.get(2) : regionNames.get(3));
        receiver.put("Address", order.getAddress());

        Map<String, Object> sender = new HashMap<>();
        sender.put("ProvinceName", configService.getConfigVal(SettingsEnum.PROVINCE_NAME));
        sender.put("CityName", configService.getConfigVal(SettingsEnum.CITY_NAME));
        sender.put("ExpAreaName", configService.getConfigVal(SettingsEnum.AREA_NAME));
        sender.put("Address", configService.getConfigVal(SettingsEnum.ADDRESS));
        sender.put("Name", configService.getConfigVal(SettingsEnum.SENDER));
        sender.put("Mobile", configService.getConfigVal(SettingsEnum.MOBILE));
        sender.put("PostCode", "000000");

        BigDecimal weight = BigDecimal.ZERO;
        for (OrderItemVO item : order.getItems()) {
            if (item.getProductWeight() != null) {
                weight = weight.add(item.getProductWeight());
            }
        }

        String startDate = DateUtil.format(DateUtil.offsetHour(new Date(), 1), "yyyy-MM-dd HH:mm:ss");
        String endDate = DateUtil.format(DateUtil.offsetHour(new Date(), 3), "yyyy-MM-dd HH:mm:ss");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("OrderCode", createOrderCode(order.getOrderSn(), order.getOrderId()));
        requestData.put("ShipperCode", logistics.getLogisticsCode());
        requestData.put("PayType", 3);
        requestData.put("ExpType", logistics.getExpType());
        requestData.put("Receiver", receiver);
        requestData.put("Sender", sender);
        requestData.put("Quantity", 1);
        requestData.put("Weight", weight.compareTo(BigDecimal.ONE) > 0 ? weight : BigDecimal.ONE);
        requestData.put("Volume", 1);
        requestData.put("Remark", remark);
        requestData.put("Commodity", List.of(Map.of("GoodsName", "电商产品")));
        requestData.put("CustomerName", logistics.getCustomerName());
        requestData.put("CustomerPwd", logistics.getCustomerPwd());
        requestData.put("SendSite", logistics.getMonthCode());
        requestData.put("SendStaff", logistics.getSendSite());
        requestData.put("MonthCode", logistics.getSendStaff());
        requestData.put("IsNotice", 0);
        requestData.put("StartDate", startDate);
        requestData.put("EndDate", endDate);
        requestData.put("CurrencyCode", "CNY");
        requestData.put("Dutiable", Map.of("DeclaredValue", order.getProductAmount()));
        requestData.put("IsReturnPrintTemplate", 1);

        String requestJson = JSONUtil.toJsonStr(requestData);
        String apiKey = configService.getConfigVal(SettingsEnum.KDNIAO_API_KEY);
        String kdnBusinessId = configService.getConfigVal(SettingsEnum.KDNIAO_BUSINESS_ID);
        if (StrUtil.isBlank(apiKey) || StrUtil.isBlank(kdnBusinessId)) {
            throw new GlobalException("快递鸟凭证未配置，请先在配送设置中填写 API Key 与服务ID");
        }
        String dataSign = Base64.encode(SecureUtil.md5(requestJson + apiKey), "UTF-8");

        String response = shippingInfoClient.getElectronicWaybill(
                URLEncoder.encode(requestJson, StandardCharsets.UTF_8),
                kdnBusinessId,
                "1007",
                URLEncoder.encode(dataSign, StandardCharsets.UTF_8),
                "2"
        );

        Map<String, Object> result = JSONUtil.parseObj(response).toBean(new TypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });

        if (!Boolean.parseBoolean(String.valueOf(result.get("Success")))) {
            throw new GlobalException(String.valueOf(result.get("Reason")));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> orderInfo = (Map<String, Object>) result.get("Order");
        LogisticsApiLog logisticsApiLog = new LogisticsApiLog();
        logisticsApiLog.setOrderId(order.getOrderId());
        logisticsApiLog.setProviderType(providerType());
        logisticsApiLog.setOrderCode(String.valueOf(orderInfo.get("OrderCode")));
        logisticsApiLog.setLogisticCode(String.valueOf(orderInfo.get("LogisticCode")));
        logisticsApiLog.setPrintTemplate(String.valueOf(result.getOrDefault("PrintTemplate", "")));
        logisticsApiLogMapper.insert(logisticsApiLog);
        return result;
    }

    @Override
    public ShippingResult queryTrack(LogisticsCompany logisticsCompany, String trackingNo, String mobile) {
        if (logisticsCompany == null || StrUtil.isBlank(logisticsCompany.getLogisticsCode()) || StrUtil.isBlank(trackingNo)) {
            return null;
        }

        String customerName = "";
        if (StrUtil.isNotBlank(mobile)
                && mobile.length() >= 4
                && (ObjectUtil.equals(logisticsCompany.getLogisticsCode(), "SF")
                || ObjectUtil.equals(logisticsCompany.getLogisticsCode(), "KYSY")
                || ObjectUtil.equals(logisticsCompany.getLogisticsCode(), "ZTO"))) {
            customerName = StrUtil.subSuf(mobile, mobile.length() - 4);
        }

        String requestData = "{"
                + "'ShipperCode': '" + logisticsCompany.getLogisticsCode() + "',"
                + "'LogisticCode': '" + trackingNo.trim() + "',"
                + "'CustomerName': '" + customerName + "'"
                + "}";
        String apiKey = configService.getConfigVal(SettingsEnum.KDNIAO_API_KEY);
        String kdnBusinessId = configService.getConfigVal(SettingsEnum.KDNIAO_BUSINESS_ID);
        if (StrUtil.isBlank(apiKey) || StrUtil.isBlank(kdnBusinessId)) {
            return null;
        }

        String dataSign = Base64.encode(SecureUtil.md5(requestData + apiKey), "UTF-8");
        String shippingInfo = shippingInfoClient.getShippingInfo(
                URLEncoder.encode(requestData, StandardCharsets.UTF_8),
                kdnBusinessId,
                KuaiDiNiaoTypes.KDNIAO_JSCX.getCode(),
                URLEncoder.encode(dataSign, StandardCharsets.UTF_8),
                "2");

        ShippingResult result = JSONUtil.toBean(shippingInfo, JSONConfig.create().setIgnoreCase(true), ShippingResult.class);
        if (result == null || ObjectUtil.equals(result.getSuccess(), "false")) {
            return null;
        }
        return result;
    }

    private String createOrderCode(String orderSn, Integer orderId) {
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return orderSn + orderId + random;
    }
}
