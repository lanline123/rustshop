// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.panel;

import com.tigshop.bean.model.panel.PanelShortcutMenu;
import com.tigshop.bean.vo.panel.ShortcutMenuVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 工作台首页快捷功能配置服务
 *
 * @author Tigshop团队
 */
public interface PanelShortcutMenuService extends BaseService<PanelShortcutMenu> {

    /**
     * 获取当前登录商家账号在当前店铺已固定到首页的功能列表。
     *
     * @return ShortcutMenuVO（无记录时 list 为空数组）
     */
    ShortcutMenuVO getShortcutMenu();

    /**
     * 整体覆盖保存当前登录商家账号在当前店铺的首页快捷功能配置。
     *
     * @param list 功能 key 有序数组
     * @return 是否成功
     */
    boolean saveShortcutMenu(List<String> list);
}
