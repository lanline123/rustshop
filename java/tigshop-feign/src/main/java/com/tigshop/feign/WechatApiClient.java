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

import com.tigshop.bean.feign.wechat.*;
import com.tigshop.bean.param.user.CreateQrcodeParam;
import com.tigshop.feign.config.WechatFeignConfig;
import feign.Headers;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 微信接口
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@FeignClient(name = "wechat-api", url = "https://api.weixin.qq.com", configuration = WechatFeignConfig.class)
public interface WechatApiClient {
    /**
     * 删除小程序模板
     * @param accessToken 接口调用凭证
     * @param body 要删除的模板id
     * @return WechatApiResult
     */
    @PostMapping("/wxaapi/newtmpl/deltemplate")
    WechatApiResult deleteTemplate(@RequestParam("access_token") String accessToken,
                                   @RequestBody @Param("body") Map<String, String> body);

    /**
     * 添加小程序模板
     * @param accessToken 接口调用凭证
     * @param param 新增模板入参
     * @return AddTemplateResult
     */
    @PostMapping("/wxaapi/newtmpl/addtemplate")
    AddTemplateResult addTemplate(@RequestParam("access_token") String accessToken,
                                  @RequestBody AddTemplateParam param);

    /**
     * 获取小程序模板
     * @param accessToken 接口调用凭证
     * @return GetTemplateResult
     */
    @GetMapping("/wxaapi/newtmpl/gettemplate")
    GetTemplateResult getTemplate(@RequestParam("access_token") String accessToken);

    /**
     * 获取公众号所有模板
     * @param accessToken 接口调用凭证
     * @return GetAllTemplateResult
     */
    @GetMapping("/cgi-bin/template/get_all_private_template")
    GetAllTemplateResult getAllTemplate(@RequestParam("access_token") String accessToken);

    /**
     * 删除公众号模板
     * @param accessToken 接口调用凭证
     * @param body 要删除的模板id
     * @return WechatApiResult
     */
    @PostMapping("/cgi-bin/template/del_private_template")
    WechatApiResult delPrivateTemplate(@RequestParam("access_token") String accessToken,
                                      @RequestBody Map<String, String> body);

    /**
     * 发送模板消息
     * @param accessToken 接口调用凭证
     * @param body 微信公众号发送模板消息
     * @return SendTemplateMessageResult
     */
    @PostMapping("/cgi-bin/message/template/send")
    SendTemplateMessageResult sendTemplateMessage(@RequestParam("access_token") String accessToken,
                                                  @RequestBody SendTemplateMessageParam body);

    /**
     * 获取token
     * @param grantType 获取access_token填写client_credential
     * @param appid 获取access_token填写client_credential
     * @param secret 第三方用户唯一凭证密钥，即appSecret
     * @return GetTokenResult
     */
    @GetMapping("/cgi-bin/token")
    GetTokenResult getToken(@RequestParam("grant_type") String grantType,
                                       @RequestParam("appid") String appid,
                                       @RequestParam("secret") String secret);

    @PostMapping("/wxa/business/getliveinfo")
    GetLiveInfoResult getLiveInfo(@RequestParam("access_token") String accessToken,
                                  @RequestBody GetLiveInfoParam body);

    /**
     * 获取小程序码
     * @param accessToken 接口调用凭证
     * @param param 获取小程序码入参
     * @return GetWechatCodeResult
     */
    @PostMapping(value = "/wxa/getwxacodeunlimit", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    ResponseEntity<byte[]> getWechatCode(@RequestParam("access_token") String accessToken,
                                         @RequestBody GetWechatCodeParam param);

    /**
     * 小程序登录
     */
    @GetMapping("/sns/jscode2session")
    String code2Session(@RequestParam("appid") String appid,
                                     @RequestParam("secret") String secret,
                                     @RequestParam("js_code") String jsCode,
                                     @RequestParam(value = "grant_type") String grantType
                                     );

    /**
     * 生成微信二维码
     */
    @GetMapping("/cgi-bin/qrcode/create")
    CreateQrcodeResult qrcodeCreate(@RequestParam("access_token") String accessToken, @RequestBody CreateQrcodeParam param);

    /**
     * 发货信息录入
     */
    @PostMapping("/wxa/sec/order/upload_shipping_info")
    UploadShippingInfoResult uploadShippingInfo(@RequestParam("access_token") String accessToken, @RequestBody UploadShippingInfoParam param);

    /**
     * Short Link
     */
    @PostMapping("/wxa/genwxashortlink")
    ShortLinkResult genwxashortlink (@RequestParam("access_token") String accessToken, @RequestBody ShortLinkParam param);

    // ==================== 视频号助手API ====================

    /**
     * 视频号获取当前的直播记录
     * @param accessToken 接口调用凭证
     * @return GetFinderLiveRecordListResult
     * @see <a href="https://developers.weixin.qq.com/doc/channels/api/channels/live/api_getfinderliverecordlist.html">微信文档</a>
     */
    @PostMapping("/channels/ec/finderlive/getfinderliverecordlist")
    GetFinderLiveRecordListResult getFinderLiveRecordList(@RequestParam("access_token") String accessToken);

    // ==================== 微信小程序直播API ====================

    /**
     * 创建直播间
     * @param accessToken 接口调用凭证
     * @param body 创建参数
     * @return CreateRoomResult
     */
    @PostMapping("/wxaapi/broadcast/room/create")
    CreateRoomResult createRoom(@RequestParam("access_token") String accessToken,
                                @RequestBody CreateRoomParam body);

    /**
     * 编辑直播间
     * @param accessToken 接口调用凭证
     * @param body 编辑参数
     * @return WechatApiResult
     */
    @PostMapping("/wxaapi/broadcast/room/editroom")
    WechatApiResult editRoom(@RequestParam("access_token") String accessToken,
                             @RequestBody EditRoomParam body);

    /**
     * 删除直播间
     * @param accessToken 接口调用凭证
     * @param body 删除参数
     * @return WechatApiResult
     */
    @PostMapping("/wxaapi/broadcast/room/deleteroom")
    WechatApiResult deleteRoom(@RequestParam("access_token") String accessToken,
                               @RequestBody DeleteRoomParam body);

    /**
     * 获取推流地址
     * @param accessToken 接口调用凭证
     * @param roomId 房间ID
     * @return GetPushUrlResult
     */
    @GetMapping("/wxaapi/broadcast/room/getpushurl")
    GetPushUrlResult getPushUrl(@RequestParam("access_token") String accessToken,
                                 @RequestParam("roomId") Integer roomId);

    /**
     * 获取直播间分享二维码
     * @param accessToken 接口调用凭证
     * @param roomId 房间ID
     * @param params 自定义参数
     * @return GetSharedCodeResult
     */
    @GetMapping("/wxaapi/broadcast/room/getsharedcode")
    GetSharedCodeResult getSharedCode(@RequestParam("access_token") String accessToken,
                                      @RequestParam("roomId") Integer roomId,
                                      @RequestParam(value = "params", required = false) String params);

    /**
     * 添加商品到直播间
     * @param accessToken 接口调用凭证
     * @param body 添加商品参数
     * @return AddGoodsResult
     */
    @PostMapping("/wxaapi/broadcast/room/addgoods")
    AddGoodsResult addGoods(@RequestParam("access_token") String accessToken,
                            @RequestBody AddGoodsParam body);

    /**
     * 添加商品到微信直播商品库并提审
     * @param accessToken 接口调用凭证
     * @param body 商品信息
     * @return BroadcastGoodsAddResult
     */
    @PostMapping("/wxaapi/broadcast/goods/add")
    BroadcastGoodsAddResult addBroadcastGoods(@RequestParam("access_token") String accessToken,
                                              @RequestBody BroadcastGoodsAddParam body);

    /**
     * 删除直播间商品
     * @param accessToken 接口调用凭证
     * @param body 删除商品参数
     * @return WechatApiResult
     */
    @PostMapping("/wxaapi/broadcast/goods/deleteInRoom")
    WechatApiResult deleteGoods(@RequestParam("access_token") String accessToken,
                                @RequestBody DeleteGoodsParam body);

    /**
     * 直播间商品排序
     * @param accessToken 接口调用凭证
     * @param body 排序参数
     * @return WechatApiResult
     */
    @PostMapping("/wxaapi/broadcast/room/sortgoods")
    WechatApiResult sortGoods(@RequestParam("access_token") String accessToken,
                              @RequestBody SortGoodsParam body);

    /**
     * 获取直播间列表
     * @param accessToken 接口调用凭证
     * @param body 请求参数
     * @return GetLiveInfoResult
     */
    @PostMapping("/wxa/business/getliveinfo")
    GetLiveInfoResult getLiveList(@RequestParam("access_token") String accessToken,
                                   @RequestBody GetLiveInfoParam body);

    /**
     * 获取直播间详细信息
     * @param accessToken 接口调用凭证
     * @param roomId 房间ID
     * @return RoomInfoResult
     */
    @GetMapping("/wxaapi/broadcast/room/get_room_info")
    RoomInfoResult getRoomInfo(@RequestParam("access_token") String accessToken,
                                @RequestParam("roomId") Integer roomId);

}
