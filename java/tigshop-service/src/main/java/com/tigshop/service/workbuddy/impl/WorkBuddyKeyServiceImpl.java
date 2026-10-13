package com.tigshop.service.workbuddy.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.vo.workbuddy.WorkBuddyKeySettingsVO;
import com.tigshop.bean.vo.workbuddy.WorkBuddyKeyStatusVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.workbuddy.WorkBuddyKeyService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import static com.tigshop.common.constant.HttpStatus.UNAUTHORIZED;

/** 全局单 MCP 密钥服务。密钥和有效期均来自 config 表，普通接口只返回掩码。 */
@Service
public class WorkBuddyKeyServiceImpl implements WorkBuddyKeyService {
    public static final String KEY_CODE = "workbuddyApiKey";
    public static final String EXPIRES_AT_CODE = "workbuddyApiKeyExpiresAt";
    private final ConfigService configService;

    public WorkBuddyKeyServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    /** 仅生成，不保存；保存由全局设置 saveGlobal 统一完成。 */
    @Override
    public String generateKey() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return "wb_" + HexFormat.of().formatHex(bytes);
    }

    @Override
    public String revealKey() {
        String stored = configValue(KEY_CODE);
        if (StrUtil.isBlank(stored)) throw new GlobalException("MCP密钥未配置", UNAUTHORIZED);
        return stored;
    }

    @Override
    public WorkBuddyKeySettingsVO settings() {
        WorkBuddyKeySettingsVO vo = new WorkBuddyKeySettingsVO();
        String key = configValue(KEY_CODE);
        String expires = configValue(EXPIRES_AT_CODE);
        vo.setConfigured(StrUtil.isNotBlank(key));
        if (StrUtil.isNotBlank(expires)) {
            try { vo.setExpiresAt(Long.parseLong(expires)); } catch (NumberFormatException ignored) { }
        }
        vo.setMaskedKey(vo.isConfigured() ? mask(key) : "");
        return vo;
    }

    @Override
    public WorkBuddyKeyStatusVO requireValid(String key, Integer ignoredShopId, String requiredScope) {
        WorkBuddyKeyStatusVO status = status(key, null);
        if (!status.isValid()) throw new GlobalException(status.getMessage(), UNAUTHORIZED);
        // 单密钥模式不按调用方配置 scope 或工具白名单拦截；requiredScope 仅保留在
        // 方法签名中以兼容现有门面与旧客户端。
        return status;
    }

    @Override
    public WorkBuddyKeyStatusVO status(String key, Integer ignoredShopId) {
        WorkBuddyKeyStatusVO vo = new WorkBuddyKeyStatusVO();
        String expected = configValue(KEY_CODE);
        String expires = configValue(EXPIRES_AT_CODE);
        if (StrUtil.isBlank(key) || StrUtil.isBlank(expected)) {
            vo.setMessage("MCP密钥缺失或未配置");
            return vo;
        }
        if (!MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
            vo.setMessage("MCP密钥无效");
            return vo;
        }
        long expiresAt;
        try { expiresAt = Long.parseLong(expires); }
        catch (Exception e) {
            vo.setMessage("MCP密钥到期配置无效");
            return vo;
        }
        if (expiresAt <= System.currentTimeMillis() / 1000) {
            vo.setMessage("MCP密钥已过期");
            return vo;
        }
        vo.setValid(true);
        vo.setShopId(0);
        vo.setExpiresAt(expiresAt);
        vo.setKeyId(0L);
        vo.setKeyName("默认 MCP 密钥");
        vo.setMessage("连接正常");
        return vo;
    }

    private String configValue(String code) {
        try {
            ConfigPO config = configService.getConfigByCode(code);
            return config == null ? null : config.getBizVal();
        } catch (GlobalException ignored) {
            return null;
        }
    }

    private static String mask(String key) {
        return StringUtils.maskMiddleHalf(key);
    }
}
