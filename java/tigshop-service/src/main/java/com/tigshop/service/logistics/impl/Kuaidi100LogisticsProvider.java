package com.tigshop.service.logistics.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.constant.setting.Kuaidi100CodeMapping;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.feign.Kuaidi100LabelClient;
import com.tigshop.feign.Kuaidi100PollClient;
import com.tigshop.mapper.order.LogisticsApiLogMapper;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.service.logistics.LogisticsProvider;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 快递100 物流服务商实现（实时查询 + 电子面单 V2）。
 * <p>轨迹结果、电子面单返回值均归一到快递鸟既有契约，保证 OrderServiceImpl 无感。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Kuaidi100LogisticsProvider implements LogisticsProvider {

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final ConfigService configService;
    private final LogisticsApiLogMapper logisticsApiLogMapper;
    private final Kuaidi100PollClient pollClient;
    private final Kuaidi100LabelClient labelClient;

    @Override
    public int providerType() {
        return PROVIDER_KUAIDI100;
    }

    @Override
    public boolean isConfigured() {
        return StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.KUAIDI100_CUSTOMER))
                && StrUtil.isNotBlank(configService.getConfigVal(SettingsEnum.KUAIDI100_KEY));
    }

    @Override
    public ShippingResult queryTrack(LogisticsCompany logisticsCompany, String trackingNo, String mobile) {
        if (logisticsCompany == null || StrUtil.isBlank(trackingNo)) {
            return null;
        }
        String customer = configService.getConfigVal(SettingsEnum.KUAIDI100_CUSTOMER);
        String key = configService.getConfigVal(SettingsEnum.KUAIDI100_KEY);
        if (StrUtil.isBlank(customer) || StrUtil.isBlank(key)) {
            log.warn("快递100凭证未配置，跳过轨迹查询");
            return null;
        }

        String com = resolveKuaidi100Code(logisticsCompany);
        JSONObject param = new JSONObject();
        param.set("com", com);
        param.set("num", trackingNo.trim());
        if (StrUtil.isNotBlank(mobile)) {
            param.set("phone", mobile.trim());
        }
        param.set("resultv2", "4");

        String paramJson = param.toString();
        String sign = SecureUtil.md5(paramJson + key + customer).toUpperCase();

        String response;
        try {
            response = pollClient.query(customer, sign, paramJson);
        } catch (Exception e) {
            log.error("快递100轨迹查询失败, com={}, num={}", com, trackingNo, e);
            return null;
        }

        JSONObject resp = JSONUtil.parseObj(response);
        // 成功：status=200 或 message=ok
        String status = StrUtil.blankToDefault(resp.getStr("status"), resp.getStr("returnCode"));
        if (!"200".equals(status) && !"ok".equalsIgnoreCase(resp.getStr("message"))) {
            log.warn("快递100轨迹查询未成功: status={}, message={}, resp={}", status, resp.getStr("message"), response);
            return null;
        }

        ShippingResult result = new ShippingResult();
        result.setSuccess("true");
        result.setShipperCode(com);
        result.setLogisticCode(trackingNo.trim());
        result.setState(resp.getStr("state"));
        result.setStateEx(resp.getStr("state"));
        result.setLocation(resp.getStr("location"));

        List<ShippingResult.TraceVO> traces = new ArrayList<>();
        JSONArray data = resp.getJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                ShippingResult.TraceVO trace = new ShippingResult.TraceVO();
                trace.setAcceptStation(item.getStr("context"));
                trace.setAcceptTime(item.getStr("ftime"));
                trace.setLocation(item.getStr("location"));
                trace.setAction(item.getStr("status"));
                traces.add(trace);
            }
        }
        result.setTraces(traces);
        return result;
    }

    @Override
    public Map<String, Object> createElectronicWaybill(OrderVO order, String remark) {
        LogisticsCompany logistics = logisticsCompanyMapper.selectById(order.getLogisticsId());
        if (logistics == null) {
            throw new GlobalException("未存在物流公司信息");
        }

        String customer = configService.getConfigVal(SettingsEnum.KUAIDI100_CUSTOMER);
        String key = configService.getConfigVal(SettingsEnum.KUAIDI100_KEY);
        String secret = configService.getConfigVal(SettingsEnum.KUAIDI100_SECRET);
        if (StrUtil.isBlank(customer) || StrUtil.isBlank(key) || StrUtil.isBlank(secret)) {
            throw new GlobalException("快递100凭证未配置完整（customer/key/secret），请先在配送设置中填写");
        }

        String printType = StrUtil.blankToDefault(configService.getConfigVal(SettingsEnum.KUAIDI100_PRINT_TYPE), "HTML");
        String siid = configService.getConfigVal(SettingsEnum.KUAIDI100_SIID);
        if ("CLOUD".equalsIgnoreCase(printType) && StrUtil.isBlank(siid)) {
            throw new GlobalException("快递100云打印需要配置云打印机设备码(siid)");
        }
        // 网点面单在 IMAGE/HTML/CLOUD 打印模式下必须指定面单模板ID(tempId)，否则快递100 无法生成面单
        boolean needTemplate = "IMAGE".equalsIgnoreCase(printType)
                || "HTML".equalsIgnoreCase(printType) || "CLOUD".equalsIgnoreCase(printType);
        if (needTemplate && StrUtil.isBlank(logistics.getKuaidi100TempId())) {
            throw new GlobalException("物流公司[" + logistics.getLogisticsName()
                    + "]未配置快递100面单模板ID(tempId)，打印类型[" + printType + "]无法生成面单，请在物流公司管理中补充");
        }

        String com = resolveKuaidi100Code(logistics);
        String orderCode = createOrderCode(order.getOrderSn(), order.getOrderId());

        List<String> regionNames = order.getRegionNames();
        String province = safeRegion(regionNames, 0);
        String city = safeRegion(regionNames, 1);
        String area = safeRegion(regionNames, 2);
        String recvAddr = StrUtil.join("", province, city, area, StrUtil.blankToDefault(order.getAddress(), ""));

        String sendProvince = configService.getConfigVal(SettingsEnum.PROVINCE_NAME);
        String sendCity = configService.getConfigVal(SettingsEnum.CITY_NAME);
        String sendArea = configService.getConfigVal(SettingsEnum.AREA_NAME);
        String sendDetail = configService.getConfigVal(SettingsEnum.ADDRESS);
        String sendAddr = StrUtil.join("", sendProvince, sendCity, sendArea, sendDetail);

        Map<String, Object> recMan = new HashMap<>();
        recMan.put("name", order.getConsignee());
        recMan.put("mobile", order.getMobile());
        recMan.put("printAddr", recvAddr);

        Map<String, Object> sendMan = new HashMap<>();
        sendMan.put("name", configService.getConfigVal(SettingsEnum.SENDER));
        sendMan.put("mobile", configService.getConfigVal(SettingsEnum.MOBILE));
        sendMan.put("printAddr", sendAddr);

        BigDecimal weight = BigDecimal.ONE;
        if (order.getItems() != null) {
            BigDecimal sum = BigDecimal.ZERO;
            for (OrderItemVO item : order.getItems()) {
                if (item.getProductWeight() != null) {
                    sum = sum.add(item.getProductWeight());
                }
            }
            if (sum.compareTo(BigDecimal.ZERO) > 0) {
                weight = sum;
            }
        }

        JSONObject param = new JSONObject();
        param.set("printType", printType);
        param.set("kuaidicom", com);
        // 网点电子面单账户参数（快递100 专属字段，来自物流公司配置）
        if (StrUtil.isNotBlank(logistics.getKuaidi100PartnerId())) {
            param.set("partnerId", logistics.getKuaidi100PartnerId());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100PartnerKey())) {
            param.set("partnerKey", logistics.getKuaidi100PartnerKey());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100PartnerSecret())) {
            param.set("partnerSecret", logistics.getKuaidi100PartnerSecret());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100Net())) {
            param.set("net", logistics.getKuaidi100Net());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100CheckMan())) {
            param.set("checkMan", logistics.getKuaidi100CheckMan());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100ExpType())) {
            param.set("expType", logistics.getKuaidi100ExpType());
        }
        // 面单模板（网点面单在 IMAGE/HTML/CLOUD 下必填 tempId；子单/回单模板按快递公司要求可选）
        if (StrUtil.isNotBlank(logistics.getKuaidi100TempId())) {
            param.set("tempId", logistics.getKuaidi100TempId());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100ChildTempId())) {
            param.set("childTempId", logistics.getKuaidi100ChildTempId());
        }
        if (StrUtil.isNotBlank(logistics.getKuaidi100BackTempId())) {
            param.set("backTempId", logistics.getKuaidi100BackTempId());
        }
        param.set("recMan", recMan);
        param.set("sendMan", sendMan);
        param.set("cargo", "电商产品");
        param.set("count", 1);
        param.set("weight", weight.doubleValue());
        param.set("payType", "SHIPPER");
        param.set("remark", StrUtil.blankToDefault(remark, ""));
        param.set("orderId", orderCode);
        if (StrUtil.isNotBlank(siid)) {
            param.set("siid", siid);
        }

        String paramJson = param.toString();
        String t = String.valueOf(System.currentTimeMillis());
        String sign = SecureUtil.md5(paramJson + t + key + secret).toUpperCase();

        String response;
        try {
            response = labelClient.order("order", key, sign, t, paramJson);
        } catch (Exception e) {
            log.error("快递100电子面单下单失败, orderId={}, com={}", order.getOrderId(), com, e);
            throw new GlobalException("快递100电子面单下单失败：" + e.getMessage());
        }

        JSONObject resp = JSONUtil.parseObj(response);
        boolean success = resp.getBool("success", false) || Integer.valueOf(200).equals(resp.getInt("code"));
        if (!success) {
            throw new GlobalException(StrUtil.blankToDefault(resp.getStr("message"), "快递100电子面单下单失败"));
        }

        JSONObject data = resp.getJSONObject("data");
        if (data == null) {
            throw new GlobalException("快递100电子面单返回缺少 data");
        }
        String kuaidinum = data.getStr("kuaidinum");
        String label = StrUtil.blankToDefault(data.getStr("label"), "");

        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("OrderCode", orderCode);
        orderInfo.put("LogisticCode", kuaidinum);

        Map<String, Object> result = new HashMap<>();
        result.put("Success", true);
        result.put("Reason", "");
        result.put("Order", orderInfo);
        result.put("PrintTemplate", label);

        LogisticsApiLog logisticsApiLog = new LogisticsApiLog();
        logisticsApiLog.setOrderId(order.getOrderId());
        logisticsApiLog.setProviderType(providerType());
        logisticsApiLog.setOrderCode(orderCode);
        logisticsApiLog.setLogisticCode(kuaidinum);
        logisticsApiLog.setPrintTemplate(label);
        logisticsApiLogMapper.insert(logisticsApiLog);
        return result;
    }

    private String resolveKuaidi100Code(LogisticsCompany company) {
        if (StrUtil.isNotBlank(company.getKuaidi100Code())) {
            return company.getKuaidi100Code().trim().toLowerCase();
        }
        String mapped = Kuaidi100CodeMapping.resolve(company.getLogisticsCode());
        if (StrUtil.isNotBlank(mapped)) {
            return mapped;
        }
        throw new GlobalException("物流公司[" + company.getLogisticsName()
                + "]未配置快递100编码(kuaidi100Code)，且内置映射无匹配物流编码["
                + company.getLogisticsCode() + "]，请在后台物流公司管理中补充");
    }

    private String safeRegion(List<String> regionNames, int index) {
        if (regionNames == null || regionNames.size() <= index) {
            return "";
        }
        return StrUtil.blankToDefault(regionNames.get(index), "");
    }

    private String createOrderCode(String orderSn, Integer orderId) {
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return orderSn + orderId + random;
    }
}
