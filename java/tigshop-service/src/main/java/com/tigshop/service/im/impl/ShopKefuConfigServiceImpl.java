package com.tigshop.service.im.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.im.KefuAccessConfigSaveDTO;
import com.tigshop.bean.model.im.ShopKefuConfig;
import com.tigshop.bean.param.settings.config.KefuSaveParam;
import com.tigshop.bean.vo.im.KefuAccessConfigVO;
import com.tigshop.bean.vo.setting.config.KefuSettingsVO;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.im.ShopKefuConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ShopKefuConfigService;
import org.springframework.stereotype.Service;

/**
 * 店铺客服接入配置服务实现类
 *
 * @author Tigshop团队
 */
@Service
public class ShopKefuConfigServiceImpl extends BaseServiceImpl<ShopKefuConfigMapper, ShopKefuConfig> implements ShopKefuConfigService {

    @Override
    public KefuSettingsVO getKefuSettings(Integer shopId) {
        KefuSettingsVO settings = toKefuSettings(getByShopId(shopId));
        settings.encryptData();
        return settings;
    }

    @Override
    public void saveKefuSettings(Integer shopId, KefuSaveParam param) {
        ShopKefuConfig config = findByShopId(shopId);
        long now = StringUtils.getCurrentTime();
        if (config == null) {
            config = createDefault(shopId);
            config.setCreateTime(now);
        } else {
            KefuSettingsVO current = toKefuSettings(config);
            current.encryptData();
            param.noUpdate(current);
        }

        BeanUtil.copyProperties(param, config, CopyOptions.create().ignoreNullValue());
        config.setModifyTime(now);
        if (config.getId() == null) {
            this.save(config);
        } else {
            this.updateById(config);
        }
    }

    @Override
    public ShopKefuConfig getByShopId(Integer shopId) {
        ShopKefuConfig config = findByShopId(shopId);
        if (config == null) {
            config = createDefault(shopId);
        }
        return config;
    }

    @Override
    public KefuAccessConfigVO getAccessConfig(Integer shopId) {
        ShopKefuConfig config = getByShopId(shopId);
        KefuAccessConfigVO vo = new KefuAccessConfigVO();
        vo.setShopId(shopId);
        vo.setAutoAccessMode(config.getAutoAccessMode() == null ? 0 : config.getAutoAccessMode());
        vo.setReturnCustomerAccess(config.getReturnCustomerAccess() == null ? 0 : config.getReturnCustomerAccess());
        return vo;
    }

    @Override
    public void saveAccessConfig(KefuAccessConfigSaveDTO dto) {
        ShopKefuConfig config = findByShopId(dto.getShopId());
        long now = StringUtils.getCurrentTime();
        if (config == null) {
            config = createDefault(dto.getShopId());
            config.setAutoAccessMode(dto.getAutoAccessMode() == null ? 0 : dto.getAutoAccessMode());
            config.setReturnCustomerAccess(dto.getReturnCustomerAccess() == null ? 0 : dto.getReturnCustomerAccess());
            config.setCreateTime(now);
            config.setModifyTime(now);
            this.save(config);
        } else {
            if (dto.getAutoAccessMode() != null) {
                config.setAutoAccessMode(dto.getAutoAccessMode());
            }
            if (dto.getReturnCustomerAccess() != null) {
                config.setReturnCustomerAccess(dto.getReturnCustomerAccess());
            }
            config.setModifyTime(now);
            this.updateById(config);
        }
    }

    private ShopKefuConfig findByShopId(Integer shopId) {
        return this.getOne(new LambdaQueryWrapper<ShopKefuConfig>()
                .eq(ShopKefuConfig::getShopId, shopId)
                .last("limit 1"));
    }

    private ShopKefuConfig createDefault(Integer shopId) {
        ShopKefuConfig config = new ShopKefuConfig();
        config.setShopId(shopId);
        config.setKefuType(0);
        config.setKefuYzfType(0);
        config.setKefuYzfSign("");
        config.setKefuWorkwxId("");
        config.setCorpId("");
        config.setKefuCode("");
        config.setKefuCodeBlank(0);
        config.setKefuWhatsappPhone("");
        config.setKefuPhone("");
        config.setKefuTime("");
        config.setAutoAccessMode(0);
        config.setReturnCustomerAccess(0);
        return config;
    }

    private KefuSettingsVO toKefuSettings(ShopKefuConfig config) {
        KefuSettingsVO settings = new KefuSettingsVO();
        BeanUtil.copyProperties(config, settings);
        return settings;
    }
}
