package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 物流信息接口
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@FeignClient(name = "shipping-api", url = "https://api.kdniao.com/api/dist")
public interface ShippingInfoClient {
    /**
     * 物流信息查询
     * ShipperCode		快递公司编码》        必传
     * LogisticCode		快递单号             必传
     * CustomerName	String(50)	顺丰必填，跨越必填	寄件人or收件人 手机号后四位
     * @param requestData
     * @param eBusinessID
     * @param requestType
     * @param dataSign
     * @param dataType
     * @return
     */
    @PostMapping("")
    String getShippingInfo(@RequestParam(name = "RequestData") String requestData,
                           @RequestParam(name = "EBusinessID") String eBusinessID,
                           @RequestParam(name = "RequestType") String requestType,
                           @RequestParam(name = "DataSign") String dataSign,
                           @RequestParam(name = "DataType", defaultValue = "2") String dataType); // 固定值2不清楚含义

    /**
     * 获取电子面单
     *
     * @param requestData 业务数据（Json字符串）
     * @param eBusinessID 商户ID
     * @param requestType 请求类型：1007
     * @param dataSign 数据签名
     * @param dataType 固定为 2
     * @return 返回JSON字符串
     */
    @PostMapping("")
    String getElectronicWaybill(@RequestParam("RequestData") String requestData,
                                @RequestParam("EBusinessID") String eBusinessID,
                                @RequestParam("RequestType") String requestType,
                                @RequestParam("DataSign") String dataSign,
                                @RequestParam(name = "DataType", defaultValue = "2") String dataType);

    /**
     * 获取快递公司列表/编码表（用于同步物流公司）
     *
     * @param requestData 业务数据（Json字符串），通常可传 {} 或者按快递鸟要求传参
     * @param eBusinessID 商户ID
     * @param requestType 请求类型（例如 1002）
     * @param dataSign 数据签名
     * @param dataType 固定为 2
     * @return 返回JSON字符串
     */
    @PostMapping("")
    String getShipperList(@RequestParam("RequestData") String requestData,
                          @RequestParam("EBusinessID") String eBusinessID,
                          @RequestParam("RequestType") String requestType,
                          @RequestParam("DataSign") String dataSign,
                          @RequestParam(name = "DataType", defaultValue = "2") String dataType);
}
