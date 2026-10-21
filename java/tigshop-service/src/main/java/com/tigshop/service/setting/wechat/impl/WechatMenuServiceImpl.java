// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.setting.wechat.WxMenuNodeDTO;
import com.tigshop.bean.dto.setting.wechat.WxMenuRuleDTO;
import com.tigshop.bean.dto.setting.wechat.WxMenuSaveDTO;
import com.tigshop.bean.model.setting.wechat.WechatMenu;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.bean.vo.setting.wechat.SaveAndPublishVO;
import com.tigshop.bean.vo.setting.wechat.WxMenuVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.setting.wechat.WechatMenuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.wechat.WechatMenuService;
import com.tigshop.service.setting.wechat.WxMenuApiService;
import com.tigshop.service.setting.wechat.WxReplyRenderer;
import com.tigshop.service.setting.wechat.util.WxMenuConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 公众号自定义菜单服务实现
 *
 * <p>关键设计：
 * <ul>
 *     <li>「保存 + 发布」由 {@link #saveAndPublish(WxMenuSaveDTO)} 编排：先在事务外发起微信 RPC，
 *         推送成功后再开独立事务落库 menu_json + publishedAt，避免 DB（新 key）与微信（旧 key）错位
 *         导致 CLICK 回调失配。</li>
 *     <li>事务子方法 {@link #doSave} 通过 {@code @Lazy} 注入自身代理 {@code self} 调用，
 *         规避 Spring AOP 同类内部调用导致 {@code @Transactional} 失效。</li>
 *     <li>菜单字节长度按需求按 GBK 编码统计：一级 ≤8、二级 ≤16。</li>
 *     <li>click 叶子的 EventKey 由 {@link #normalizeMenuClickKeys} 在保存时统一分配（{@code MENU_ + uuid32}），
 *         前端不生成、不展示；非法或重复的旧 key 一律重分配。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Slf4j
@Service
public class WechatMenuServiceImpl extends BaseServiceImpl<WechatMenuMapper, WechatMenu> implements WechatMenuService {

    /** http(s) URL 校验：发布层做兜底，前端已校验 */
    private static final Pattern HTTP_URL = Pattern.compile("^https?://.+", Pattern.CASE_INSENSITIVE);

    /** 小程序 pagepath 校验：必须以 pages/ 开头 */
    private static final Pattern PAGE_PATH = Pattern.compile("^pages/.+");

    @Resource
    private WxMenuApiService wxMenuApiService;

    /**
     * 自身代理引用，用于内部子方法走 AOP 代理触发 {@link Transactional}
     */
    @Resource
    @Lazy
    private WechatMenuService self;

    @Override
    public WxMenuVO get() {
        WechatMenu entity = getOne(new LambdaQueryWrapper<WechatMenu>().last("LIMIT 1"), false);
        WxMenuVO vo = new WxMenuVO();
        if (entity == null) {
            WxMenuSaveDTO.WxMenuTree empty = new WxMenuSaveDTO.WxMenuTree();
            empty.setButtons(List.of());
            vo.setTree(empty);
            return vo;
        }
        vo.setId(entity.getId());
        vo.setPublishedAt(entity.getPublishedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        WxMenuSaveDTO.WxMenuTree tree;
        if (StrUtil.isNotBlank(entity.getMenuJson())) {
            try {
                tree = JSON.parseObject(entity.getMenuJson(), WxMenuSaveDTO.WxMenuTree.class);
            } catch (Exception e) {
                log.error("[WxMenu] menu_json 反序列化失败 id={}", entity.getId(), e);
                tree = new WxMenuSaveDTO.WxMenuTree();
                tree.setButtons(List.of());
            }
        } else {
            tree = new WxMenuSaveDTO.WxMenuTree();
            tree.setButtons(List.of());
        }
        vo.setTree(tree);
        return vo;
    }

    @Override
    public SaveAndPublishVO saveAndPublish(WxMenuSaveDTO dto) {
        // 1) 入参基本校验：tree / 一级菜单非空且 ≤3
        if (dto == null || dto.getTree() == null || CollectionUtil.isEmpty(dto.getTree().getButtons())) {
            throw new GlobalException("菜单不能为空");
        }
        List<WxMenuNodeDTO> topButtons = dto.getTree().getButtons();
        if (topButtons.size() > 3) {
            throw new GlobalException("一级菜单最多 3 个");
        }

        // 2) click 菜单：补齐 / 去重 / 清洗 EventKey（微信回调凭此取「发送消息」内容，与关键词规则无关）
        normalizeMenuClickKeys(dto.getTree());

        // 3) 业务侧深度校验（DTO @Valid 已覆盖基本注解，这里做字节数 + oneOf 联合 + 叶子必须 rule）
        for (int i = 0; i < topButtons.size(); i++) {
            validateLevelOne(topButtons.get(i), i);
        }

        // 4) 事务外发起微信 RPC：只有推送成功才允许落库，避免 DB（新 key）与微信（旧 key）错位导致 CLICK 回调失配
        WxMenu wxMenu = WxMenuConverter.toWxMenu(dto.getTree());
        WxMenuApiService.PublishResult pr = wxMenuApiService.menuCreate(wxMenu);

        SaveAndPublishVO vo = new SaveAndPublishVO();
        if (!pr.isSuccess()) {
            // 保留 DB 原有 menu_json 不动：微信侧未改变，CLICK 回调继续走旧 key -> 旧内容
            vo.setPublished(false);
            vo.setErrMsg(pr.getErrMsg());
            vo.setPublishedAt(null);
            return vo;
        }

        // 5) 推送成功再落库（独立事务，走代理），同步写入 publishedAt
        LocalDateTime publishedAt = LocalDateTime.now();
        self.doSave(dto.getTree(), publishedAt);

        vo.setPublished(true);
        vo.setErrMsg(null);
        vo.setPublishedAt(publishedAt);
        return vo;
    }

    @Override
    public ReplyPayloadVO findMenuClickPayload(String eventKey) {
        if (StrUtil.isBlank(eventKey)) {
            return null;
        }
        String key = eventKey.trim();
        // 只从「已发布」的菜单版本取内容：未推送成功的改动不会参与 CLICK 回调匹配
        WechatMenu entity = getOne(
                new LambdaQueryWrapper<WechatMenu>()
                        .isNotNull(WechatMenu::getPublishedAt)
                        .last("LIMIT 1"),
                false);
        if (entity == null || StrUtil.isBlank(entity.getMenuJson())) {
            return null;
        }
        WxMenuSaveDTO.WxMenuTree tree;
        try {
            tree = JSON.parseObject(entity.getMenuJson(), WxMenuSaveDTO.WxMenuTree.class);
        } catch (Exception e) {
            log.warn("[WxMenu] findMenuClickPayload 解析 menu_json 失败", e);
            return null;
        }
        if (tree == null || CollectionUtil.isEmpty(tree.getButtons())) {
            return null;
        }
        WxMenuRuleDTO rule = findClickRuleByEventKey(tree.getButtons(), key);
        if (rule == null || !isMenuClickReplyComplete(rule)) {
            return null;
        }
        return WxReplyRenderer.fromMenuClickRule(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatMenu doSave(WxMenuSaveDTO.WxMenuTree tree, LocalDateTime publishedAt) {
        String menuJson = JSON.toJSONString(tree);
        WechatMenu existing = getOne(new LambdaQueryWrapper<WechatMenu>().last("LIMIT 1"), false);
        if (existing == null) {
            WechatMenu po = new WechatMenu();
            po.setMenuJson(menuJson);
            po.setPublishedAt(publishedAt);
            save(po);
            return po;
        }
        existing.setMenuJson(menuJson);
        existing.setPublishedAt(publishedAt);
        updateById(existing);
        return existing;
    }

    // ====================== 校验私有方法 ======================

    /** click EventKey 合法字符集：仅允许 {@code [A-Za-z0-9_-]}，长度 ≤128 */
    private static final Pattern EVENT_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");

    /**
     * 为所有 click 叶子分配非空、格式合法且树内唯一的 EventKey，供微信 menuCreate 与 CLICK 回调关联「发送消息」。
     * 非法或重复的旧 key（空值、超长、含特殊字符）一律重新分配，避免走到 validateRule 报错迫使运营手动处理。
     */
    private void normalizeMenuClickKeys(WxMenuSaveDTO.WxMenuTree tree) {
        if (tree == null || CollectionUtil.isEmpty(tree.getButtons())) {
            return;
        }
        Set<String> used = new HashSet<>();
        for (WxMenuNodeDTO top : tree.getButtons()) {
            if (CollectionUtil.isNotEmpty(top.getChildren())) {
                for (WxMenuNodeDTO child : top.getChildren()) {
                    assignUniqueClickEventKey(child.getRule(), used);
                }
            } else {
                assignUniqueClickEventKey(top.getRule(), used);
            }
        }
    }

    private void assignUniqueClickEventKey(WxMenuRuleDTO rule, Set<String> used) {
        if (rule == null || !"click".equals(rule.getType())) {
            return;
        }
        String key = StrUtil.trimToEmpty(rule.getEventKey());
        boolean legal = !key.isEmpty() && EVENT_KEY_PATTERN.matcher(key).matches();
        if (!legal || used.contains(key)) {
            String nk;
            do {
                nk = "MENU_" + UUID.randomUUID().toString().replace("-", "");
            } while (used.contains(nk));
            rule.setEventKey(nk);
            key = nk;
        } else {
            // 统一 trim 回写，避免前后空格误差
            rule.setEventKey(key);
        }
        used.add(key);
    }

    private WxMenuRuleDTO findClickRuleByEventKey(List<WxMenuNodeDTO> nodes, String eventKey) {
        if (CollectionUtil.isEmpty(nodes)) {
            return null;
        }
        for (WxMenuNodeDTO n : nodes) {
            if (CollectionUtil.isNotEmpty(n.getChildren())) {
                WxMenuRuleDTO hit = findClickRuleByEventKey(n.getChildren(), eventKey);
                if (hit != null) {
                    return hit;
                }
            } else if (n.getRule() != null && "click".equals(n.getRule().getType())
                    && eventKey.equals(StrUtil.trim(n.getRule().getEventKey()))) {
                return n.getRule();
            }
        }
        return null;
    }

    /**
     * 菜单「发送消息」字段是否已配齐（与 {@link #validateRule(WxMenuRuleDTO, String)} 中 click 分支一致）。
     */
    private boolean isMenuClickReplyComplete(WxMenuRuleDTO rule) {
        if (rule == null || !"click".equals(rule.getType())) {
            return false;
        }
        Integer mt = rule.getMsgType();
        if (mt == null || (mt != 1 && mt != 2)) {
            return false;
        }
        if (mt == 1) {
            return StrUtil.isNotBlank(rule.getContent()) && rule.getContent().length() <= 600;
        }
        if (StrUtil.hasBlank(rule.getNewsTitle(), rule.getNewsDescription(),
                rule.getNewsCoverUrl(), rule.getNewsUrl())) {
            return false;
        }
        if (rule.getNewsTitle().length() > 64 || rule.getNewsDescription().length() > 120) {
            return false;
        }
        return HTTP_URL.matcher(rule.getNewsUrl()).matches();
    }

    /**
     * 一级节点校验：名称字节数 + 子节点 / rule 二选一
     */
    private void validateLevelOne(WxMenuNodeDTO node, int idx) {
        if (node == null) {
            throw new GlobalException(StrUtil.format("第 {} 个一级菜单为空", idx + 1));
        }
        if (StrUtil.isBlank(node.getName())) {
            throw new GlobalException(StrUtil.format("第 {} 个一级菜单名称不能为空", idx + 1));
        }
        if (byteLenGbk(node.getName()) > 8) {
            throw new GlobalException(StrUtil.format("一级菜单「{}」名称超过 8 字节", node.getName()));
        }

        boolean hasChildren = CollectionUtil.isNotEmpty(node.getChildren());
        if (hasChildren) {
            // 一级有 children 时完全忽略 rule
            List<WxMenuNodeDTO> children = node.getChildren();
            if (children.size() > 5) {
                throw new GlobalException(StrUtil.format("一级菜单「{}」二级数量超过 5 个", node.getName()));
            }
            for (int j = 0; j < children.size(); j++) {
                validateLevelTwo(children.get(j), node.getName(), j);
            }
            return;
        }
        // 叶子一级：必须有 rule
        if (node.getRule() == null) {
            throw new GlobalException(StrUtil.format("一级菜单「{}」缺少响应规则", node.getName()));
        }
        validateRule(node.getRule(), node.getName());
    }

    /**
     * 二级节点校验：名称字节数 + rule 必填
     */
    private void validateLevelTwo(WxMenuNodeDTO node, String parentName, int idx) {
        if (node == null) {
            throw new GlobalException(StrUtil.format("「{}」下第 {} 个二级菜单为空", parentName, idx + 1));
        }
        if (StrUtil.isBlank(node.getName())) {
            throw new GlobalException(StrUtil.format("「{}」下第 {} 个二级菜单名称不能为空", parentName, idx + 1));
        }
        if (byteLenGbk(node.getName()) > 16) {
            throw new GlobalException(StrUtil.format("二级菜单「{}」名称超过 16 字节", node.getName()));
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            throw new GlobalException(StrUtil.format("二级菜单「{}」不允许再含子菜单", node.getName()));
        }
        if (node.getRule() == null) {
            throw new GlobalException(StrUtil.format("二级菜单「{}」缺少响应规则", node.getName()));
        }
        validateRule(node.getRule(), node.getName());
    }

    /**
     * rule oneOf 校验：click/view/miniprogram 字段集合互斥
     */
    private void validateRule(WxMenuRuleDTO rule, String nodeName) {
        String type = rule.getType();
        if (StrUtil.isBlank(type)) {
            throw new GlobalException(StrUtil.format("菜单「{}」响应规则类型不能为空", nodeName));
        }
        switch (type) {
            case "click" -> {
                if (StrUtil.isBlank(rule.getEventKey())) {
                    throw new GlobalException(StrUtil.format("菜单「{}」事件 Key 不能为空", nodeName));
                }
                if (rule.getEventKey().trim().length() > 128) {
                    throw new GlobalException(StrUtil.format("菜单「{}」事件 Key 长度不能超过 128", nodeName));
                }
                Integer mt = rule.getMsgType();
                if (mt == null || (mt != 1 && mt != 2)) {
                    throw new GlobalException(StrUtil.format("菜单「{}」请选择发送消息类型（文本或图文）", nodeName));
                }
                if (mt == 1) {
                    if (StrUtil.isBlank(rule.getContent())) {
                        throw new GlobalException(StrUtil.format("菜单「{}」文本消息内容不能为空", nodeName));
                    }
                    if (rule.getContent().length() > 600) {
                        throw new GlobalException(StrUtil.format("菜单「{}」文本消息内容不能超过 600 字符", nodeName));
                    }
                } else {
                    if (StrUtil.hasBlank(rule.getNewsTitle(), rule.getNewsDescription(),
                            rule.getNewsCoverUrl(), rule.getNewsUrl())) {
                        throw new GlobalException(StrUtil.format("菜单「{}」图文消息的标题、摘要、封面、链接均不能为空", nodeName));
                    }
                    if (rule.getNewsTitle().length() > 64) {
                        throw new GlobalException(StrUtil.format("菜单「{}」图文标题不能超过 64 字符", nodeName));
                    }
                    if (rule.getNewsDescription().length() > 120) {
                        throw new GlobalException(StrUtil.format("菜单「{}」图文摘要不能超过 120 字符", nodeName));
                    }
                    if (!HTTP_URL.matcher(rule.getNewsUrl()).matches()) {
                        throw new GlobalException(StrUtil.format("菜单「{}」图文跳转链接必须是 http/https URL", nodeName));
                    }
                }
            }
            case "view" -> {
                if (StrUtil.isBlank(rule.getUrl()) || !HTTP_URL.matcher(rule.getUrl()).matches()) {
                    throw new GlobalException(StrUtil.format("菜单「{}」跳转地址必须是 http/https URL", nodeName));
                }
            }
            case "miniprogram" -> {
                if (StrUtil.isBlank(rule.getAppid())) {
                    throw new GlobalException(StrUtil.format("菜单「{}」小程序 appid 不能为空", nodeName));
                }
                if (StrUtil.isBlank(rule.getPagepath()) || !PAGE_PATH.matcher(rule.getPagepath()).matches()) {
                    throw new GlobalException(StrUtil.format("菜单「{}」小程序路径必须形如 pages/xxx/xxx", nodeName));
                }
                if (StrUtil.isNotBlank(rule.getMiniprogramUrl())
                        && !HTTP_URL.matcher(rule.getMiniprogramUrl()).matches()) {
                    throw new GlobalException(StrUtil.format("菜单「{}」备用网页必须是 http/https URL", nodeName));
                }
            }
            default -> throw new GlobalException(StrUtil.format("菜单「{}」响应规则类型非法：{}", nodeName, type));
        }
    }

    /**
     * 按 GBK 编码统计字节数（与微信侧字节长度规则保持一致：汉字 2 字节）
     */
    private int byteLenGbk(String s) {
        if (s == null) {
            return 0;
        }
        try {
            return s.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            // 极端情况兜底；GBK 在 JVM 中通常都存在
            return s.getBytes().length;
        }
    }
}
