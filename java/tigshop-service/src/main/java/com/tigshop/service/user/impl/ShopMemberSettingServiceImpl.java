package com.tigshop.service.user.impl;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.user.ShopMemberSettingParam;
import com.tigshop.bean.vo.user.ShopMemberSettingVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.helper.SettingsHelper;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.ConfigValCache;
import com.tigshop.service.user.ShopMemberSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 店铺会员总开关设置实现（参照 {@code WholesaleSettingServiceImpl} 的 readInt/writeInt 范式）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopMemberSettingServiceImpl implements ShopMemberSettingService {

    private final ConfigService configService;

    private final SettingsHelper settingsHelper;

    private final ConfigValCache configValCache;

    @Override
    public ShopMemberSettingVO get() {
        ShopMemberSettingVO vo = new ShopMemberSettingVO();
        vo.setShopMemberEnabled(readInt(SettingsEnum.SHOP_MEMBER_ENABLED));
        vo.setShopMemberDesensitize(readInt(SettingsEnum.SHOP_MEMBER_DESENSITIZE));
        vo.setShopMemberAllowAdd(readInt(SettingsEnum.SHOP_MEMBER_ALLOW_ADD));
        vo.setShopMemberAllowImport(readInt(SettingsEnum.SHOP_MEMBER_ALLOW_IMPORT));
        return vo;
    }

    @Override
    public void save(ShopMemberSettingParam param) {
        writeInt(SettingsEnum.SHOP_MEMBER_ENABLED, defaultZero(param.getShopMemberEnabled()));
        if (param.getShopMemberDesensitize() != null) {
            writeInt(SettingsEnum.SHOP_MEMBER_DESENSITIZE, param.getShopMemberDesensitize());
        }
        if (param.getShopMemberAllowAdd() != null) {
            writeInt(SettingsEnum.SHOP_MEMBER_ALLOW_ADD, param.getShopMemberAllowAdd());
        }
        if (param.getShopMemberAllowImport() != null) {
            writeInt(SettingsEnum.SHOP_MEMBER_ALLOW_IMPORT, param.getShopMemberAllowImport());
        }
        log.info("[shop-member-setting] save committed; enabled={}, desensitize={}, allowAdd={}, allowImport={}",
                param.getShopMemberEnabled(), param.getShopMemberDesensitize(),
                param.getShopMemberAllowAdd(), param.getShopMemberAllowImport());
    }

    @Override
    public boolean isEnabled() {
        Integer v = readInt(SettingsEnum.SHOP_MEMBER_ENABLED);
        return v != null && v == 1;
    }

    @Override
    public boolean isDesensitizeEnabled() {
        Integer v = readInt(SettingsEnum.SHOP_MEMBER_DESENSITIZE);
        return v != null && v == 1;
    }

    @Override
    public void assertEnabledForShop() {
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0 && !isEnabled()) {
            throw new GlobalException("平台未开放店铺会员功能，当前所有会员功能处于冻结状态，如有疑问请联系平台客服。");
        }
    }

    @Override
    public void assertAddEnabled() {
        Integer v = readInt(SettingsEnum.SHOP_MEMBER_ALLOW_ADD);
        if (v == null || v != 1) {
            throw new GlobalException("平台未开放「店铺添加客户」功能。");
        }
    }

    @Override
    public void assertImportEnabled() {
        Integer v = readInt(SettingsEnum.SHOP_MEMBER_ALLOW_IMPORT);
        if (v == null || v != 1) {
            throw new GlobalException("平台未开放「店铺导入客户」功能。");
        }
    }

    // ==================== 私有工具 ====================

    private Integer readInt(SettingsEnum key) {
        String raw = configService.getConfigVal(key);
        if (raw == null || raw.isEmpty()) {
            return parseIntOrNull(key.getDefaultValue());
        }
        Integer parsed = parseIntOrNull(raw);
        return parsed != null ? parsed : parseIntOrNull(key.getDefaultValue());
    }

    private void writeInt(SettingsEnum key, Integer value) {
        if (value == null) {
            return;
        }
        // 与框架标准保存路径（saveConfigSettings）保持一致：先落库，再失效并广播 ConfigValCache，
        // 否则 getConfigVal 会一直命中启动时预载的旧缓存，出现「已写库但读不到」。
        settingsHelper.set(key, value);
        configValCache.invalidateAndBroadcast(List.of(key.getBizCode()));
    }

    private static Integer defaultZero(Integer v) {
        return v != null ? v : 0;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
