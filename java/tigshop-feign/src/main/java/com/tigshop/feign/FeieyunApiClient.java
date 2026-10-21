// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 飞鹅云打印机API接口
 *
 * @author Tigshop团队
 */
@FeignClient(name = "feieyun-api", url = "http://api.feieyun.cn/Api/Open/")
public interface FeieyunApiClient {

    /**
     * 添加打印机
     *
     * @param user           飞鹅云后台注册的账号名
     * @param stime          当前UNIX时间戳，10位，精确到秒
     * @param sig            签名，详见API开发文档
     * @param apiname        固定值：Open_printerAddlist
     * @param printerContent 打印机信息，格式：sn#key#remark#carnum，多台打印机请换行（\n）添加新打印机信息
     * @return 添加结果
     */
    @PostMapping
    String addPrinter(
            @RequestParam("user") String user,
            @RequestParam("stime") String stime,
            @RequestParam("sig") String sig,
            @RequestParam("apiname") String apiname,
            @RequestParam("printerContent") String printerContent);

    /**
     * 打印订单
     *
     * @param user    飞鹅云后台注册的账号名
     * @param stime   当前UNIX时间戳，10位，精确到秒
     * @param sig     签名，详见API开发文档
     * @param apiname 固定值：Open_printMsg
     * @param sn      打印机编号
     * @param content 打印内容
     * @param times   打印联数
     * @return 打印结果
     */
    @PostMapping
    String printOrder(
            @RequestParam("user") String user,
            @RequestParam("stime") String stime,
            @RequestParam("sig") String sig,
            @RequestParam("apiname") String apiname,
            @RequestParam("sn") String sn,
            @RequestParam("content") String content,
            @RequestParam("times") Integer times);

    /**
     * 删除打印机
     *
     * @param user    飞鹅云后台注册的账号名
     * @param stime   当前UNIX时间戳，10位，精确到秒
     * @param sig     签名，详见API开发文档
     * @param apiname 固定值：Open_printerDelList
     * @param snlist  打印机编号，多台打印机请用逗号分隔
     * @return 删除结果
     */
    @PostMapping
    String deletePrinter(
            @RequestParam("user") String user,
            @RequestParam("stime") String stime,
            @RequestParam("sig") String sig,
            @RequestParam("apiname") String apiname,
            @RequestParam("snlist") String snlist);

}