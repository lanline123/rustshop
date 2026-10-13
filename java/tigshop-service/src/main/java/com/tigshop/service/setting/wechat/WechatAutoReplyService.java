// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

import com.tigshop.bean.dto.setting.wechat.ReplyPayloadDTO;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;

/**
 * 公众号自动回复服务（M3）。
 * <p>
 * 提供「关注后回复」与「默认兜底回复」两条配置的读写接口；
 * 数据存储于 {@code config} 表，bizCode 见 {@link WechatAutoReplyBizCodes}，
 * bizVal 为 {@link ReplyPayloadDTO} 序列化后的 JSON 字符串。
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
public interface WechatAutoReplyService {

    /**
     * 读取关注后自动回复配置；记录不存在或解析失败时返回空载荷
     * （{@code msgType=1, content=""}），不抛异常。
     */
    ReplyPayloadVO getSubscribe();

    /**
     * 读取默认兜底自动回复配置；记录不存在或解析失败时返回空载荷
     * （{@code msgType=1, content=""}），不抛异常。
     */
    ReplyPayloadVO getDefault();

    /**
     * 保存关注后自动回复配置（upsert，按 bizCode 唯一）。
     */
    void saveSubscribe(ReplyPayloadDTO dto);

    /**
     * 保存默认兜底自动回复配置（upsert，按 bizCode 唯一）。
     */
    void saveDefault(ReplyPayloadDTO dto);

    /**
     * 读取扫码登录成功后自动回复配置；记录不存在或解析失败时返回空载荷
     * （{@code msgType=1, content=""}），不抛异常。
     */
    ReplyPayloadVO getLoginSuccess();

    /**
     * 保存扫码登录成功后自动回复配置（upsert，按 bizCode 唯一）。
     */
    void saveLoginSuccess(ReplyPayloadDTO dto);
}
