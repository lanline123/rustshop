package com.tigshop.service.im;

import com.tigshop.bean.dto.im.KefuAccessConfigSaveDTO;
import com.tigshop.bean.model.im.ShopKefuConfig;
import com.tigshop.bean.param.settings.config.KefuSaveParam;
import com.tigshop.bean.vo.im.KefuAccessConfigVO;
import com.tigshop.bean.vo.setting.config.KefuSettingsVO;
import com.tigshop.service.common.BaseService;

/**
 * 店铺客服接入配置服务
 *
 * @author Tigshop团队
 */
public interface ShopKefuConfigService extends BaseService<ShopKefuConfig> {

    /**
     * 获取店铺客服配置（不存在返回默认值）
     *
     * @param shopId 店铺ID
     * @return KefuSettingsVO
     */
    KefuSettingsVO getKefuSettings(Integer shopId);

    /**
     * 保存店铺客服配置（upsert）
     *
     * @param shopId 店铺ID
     * @param param 保存参数
     */
    void saveKefuSettings(Integer shopId, KefuSaveParam param);

    /**
     * 获取店铺接入规则（不存在返回默认值）
     *
     * @param shopId 店铺ID
     * @return KefuAccessConfigVO
     */
    KefuAccessConfigVO getAccessConfig(Integer shopId);

    /**
     * 保存店铺接入规则（upsert）
     *
     * @param dto 保存参数
     */
    void saveAccessConfig(KefuAccessConfigSaveDTO dto);

    /**
     * 获取店铺客服配置实体（不存在返回默认实体，不落库）
     *
     * @param shopId 店铺ID
     * @return ShopKefuConfig
     */
    ShopKefuConfig getByShopId(Integer shopId);
}
