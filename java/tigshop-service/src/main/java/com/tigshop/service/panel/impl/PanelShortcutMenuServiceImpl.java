// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.panel.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.panel.PanelShortcutMenu;
import com.tigshop.bean.vo.panel.ShortcutMenuVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.panel.PanelShortcutMenuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.panel.PanelShortcutMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 工作台首页快捷功能配置服务实现
 *
 * @author Tigshop团队
 */
@Service
public class PanelShortcutMenuServiceImpl extends BaseServiceImpl<PanelShortcutMenuMapper, PanelShortcutMenu>
        implements PanelShortcutMenuService {

    /**
     * 宽松上限：前端 UX 限制最多 7 个，后端按 20 宽松处理
     */
    private static final int MAX_MENU_SIZE = 20;

    @Override
    public ShortcutMenuVO getShortcutMenu() {
        ShortcutMenuVO vo = new ShortcutMenuVO();
        PanelShortcutMenu config = getByIdentity();
        if (config == null || StrUtil.isBlank(config.getMenuKeys())) {
            return vo;
        }
        List<String> list = JSON.parseArray(config.getMenuKeys(), String.class);
        vo.setList(list == null ? new ArrayList<>() : list);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveShortcutMenu(List<String> list) {
        // 去空、去重、保序、限量（基础处理，不做合法 key 白名单校验）
        List<String> normalized = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            LinkedHashSet<String> set = new LinkedHashSet<>();
            for (String key : list) {
                if (StrUtil.isNotBlank(key)) {
                    set.add(key.trim());
                }
                if (set.size() >= MAX_MENU_SIZE) {
                    break;
                }
            }
            normalized.addAll(set);
        }
        String menuKeys = JSON.toJSONString(normalized);

        Integer adminId = SecurityUtils.getCurrentAdminId();
        Integer shopId = getShopId();
        long now = DateUtil.currentSeconds();

        PanelShortcutMenu config = getByIdentity();
        if (config == null) {
            config = new PanelShortcutMenu();
            config.setAdminId(adminId);
            config.setShopId(shopId);
            config.setMenuKeys(menuKeys);
            config.setUpdateTime(now);
            return this.save(config);
        }
        config.setMenuKeys(menuKeys);
        config.setUpdateTime(now);
        return this.updateById(config);
    }

    /**
     * 按当前登录身份（admin_id + shop_id）读取配置。
     *
     * @return PanelShortcutMenu，无则 null
     */
    private PanelShortcutMenu getByIdentity() {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        Integer shopId = getShopId();
        LambdaQueryWrapper<PanelShortcutMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PanelShortcutMenu::getAdminId, adminId)
                .eq(PanelShortcutMenu::getShopId, shopId)
                .last("limit 1");
        return this.getOne(queryWrapper);
    }
}
