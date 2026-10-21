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

import com.tigshop.bean.dto.setting.wechat.WxMenuSaveDTO;
import com.tigshop.bean.model.setting.wechat.WechatMenu;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.bean.vo.setting.wechat.SaveAndPublishVO;
import com.tigshop.bean.vo.setting.wechat.WxMenuVO;
import com.tigshop.service.common.BaseService;

import java.time.LocalDateTime;

/**
 * 公众号自定义菜单服务（单例存储 + 一键发布到微信）
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
public interface WechatMenuService extends BaseService<WechatMenu> {

    /**
     * 获取当前菜单（单例；从未保存过时返回空树容器）
     *
     * @return WxMenuVO
     */
    WxMenuVO get();

    /**
     * 按微信菜单 CLICK 的 EventKey，从当前落库的菜单树中解析「发送消息」载荷。
     * <p>未命中或字段不完整时返回 {@code null}，由调用方走默认回复。
     *
     * @param eventKey 微信推送的 EventKey（与菜单 click.key 一致）
     * @return 可交给 {@link WxReplyRenderer} 的载荷；未配置时返回 null
     */
    ReplyPayloadVO findMenuClickPayload(String eventKey);

    /**
     * 保存菜单并同步发布到微信。
     * <p>流程：入参/业务校验 → 微信 RPC 推送 → 推送成功才落库（含 publishedAt）。
     * 入参或业务校验失败抛 {@code GlobalException} 由全局处理器返回 4xx；
     * 微信推送失败仍返回 200，错误信息携带在 {@link SaveAndPublishVO#getErrMsg()}，
     * 此时 DB 保持旧菜单 JSON 不变（与微信侧一致，避免 CLICK 回调失配）。
     *
     * @param dto 菜单树参数
     * @return SaveAndPublishVO
     */
    SaveAndPublishVO saveAndPublish(WxMenuSaveDTO dto);

    /**
     * 内部子方法：菜单 JSON + publishedAt 一次性落库（独立事务）。
     * <p>仅在微信 RPC 推送成功后调用，避免 DB 版本与微信侧错位。
     * 从外部调用时必须经 Spring 代理（通过 {@code self} 注入），否则 {@code @Transactional} 失效。
     *
     * @param tree        菜单树
     * @param publishedAt 推送成功时间；与 menu_json 同步写入
     * @return 落库后的实体
     */
    WechatMenu doSave(WxMenuSaveDTO.WxMenuTree tree, LocalDateTime publishedAt);
}
