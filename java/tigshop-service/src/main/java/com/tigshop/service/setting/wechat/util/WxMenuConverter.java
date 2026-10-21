// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat.util;

import cn.hutool.core.collection.CollectionUtil;
import com.tigshop.bean.dto.setting.wechat.WxMenuNodeDTO;
import com.tigshop.bean.dto.setting.wechat.WxMenuRuleDTO;
import com.tigshop.bean.dto.setting.wechat.WxMenuSaveDTO;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 后台自有菜单结构 → 微信官方 {@link WxMenu} 结构转换器
 * <p>纯工具类，不依赖 Spring；与 Service 解耦便于单测。
 *
 * <p>转换约束：
 * <ul>
 *     <li>一级菜单存在 children 时，仅设置 {@code name + subButtons}，{@code rule} 完全忽略</li>
 *     <li>叶子节点按 {@code rule.type} 分派字段；miniprogram 的 fallback 网页对应 WxJava 的 {@code url}</li>
 *     <li>同级按 {@code sortOrder} 升序输出，确保微信侧菜单展示顺序与后台一致</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
public final class WxMenuConverter {

    private static final Comparator<WxMenuNodeDTO> BY_SORT =
            Comparator.comparing(n -> n.getSortOrder() == null ? 0 : n.getSortOrder());

    private WxMenuConverter() {
    }

    /**
     * 转换菜单树
     *
     * @param tree 后台自有结构（已通过 Service 侧业务校验）
     * @return WxMenu 微信官方结构
     */
    public static WxMenu toWxMenu(WxMenuSaveDTO.WxMenuTree tree) {
        WxMenu wxMenu = new WxMenu();
        List<WxMenuButton> topButtons = new ArrayList<>();
        if (tree != null && CollectionUtil.isNotEmpty(tree.getButtons())) {
            tree.getButtons().stream()
                    .sorted(BY_SORT)
                    .forEach(node -> topButtons.add(buildButton(node, true)));
        }
        wxMenu.setButtons(topButtons);
        return wxMenu;
    }

    /**
     * 构建单个菜单按钮（递归一层）
     *
     * @param node  节点
     * @param isTop 是否一级菜单
     * @return WxMenuButton
     */
    private static WxMenuButton buildButton(WxMenuNodeDTO node, boolean isTop) {
        WxMenuButton button = new WxMenuButton();
        button.setName(node.getName());

        boolean hasChildren = isTop && CollectionUtil.isNotEmpty(node.getChildren());
        if (hasChildren) {
            // 父级菜单：仅 name + subButtons，按 sortOrder 升序
            List<WxMenuButton> subButtons = new ArrayList<>();
            node.getChildren().stream()
                    .sorted(BY_SORT)
                    .forEach(child -> subButtons.add(buildButton(child, false)));
            button.setSubButtons(subButtons);
            return button;
        }

        // 叶子节点：按 rule.type 分派
        WxMenuRuleDTO rule = node.getRule();
        if (rule == null || rule.getType() == null) {
            return button;
        }
        String type = rule.getType();
        button.setType(type);
        switch (type) {
            case "click" -> button.setKey(rule.getEventKey());
            case "view" -> button.setUrl(rule.getUrl());
            case "miniprogram" -> {
                button.setAppId(rule.getAppid());
                button.setPagePath(rule.getPagepath());
                // WxJava 的 miniprogram 按钮使用 url 字段承载老客户端 fallback 网页
                button.setUrl(rule.getMiniprogramUrl());
            }
            default -> {
                // 其它类型不处理；正常应已被 Service 侧校验拦截
            }
        }
        return button;
    }
}
