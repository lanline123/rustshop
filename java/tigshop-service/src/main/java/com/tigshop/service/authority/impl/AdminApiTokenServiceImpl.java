package com.tigshop.service.authority.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.model.authority.AdminApiToken;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.vo.authority.AdminApiTokenGenerateVO;
import com.tigshop.bean.vo.authority.AdminApiTokenVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.mapper.authority.AdminApiTokenMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.service.authority.AdminApiTokenService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminApiTokenServiceImpl extends BaseServiceImpl<AdminApiTokenMapper, AdminApiToken> implements AdminApiTokenService {

    private static final int MAX_TOKENS_PER_USER = 10;
    private static final int TOKEN_BYTE_LENGTH = 16;
    private static final int REDIS_CACHE_HOURS = 1;

    @Resource
    private RedisCache redisCache;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public AdminApiTokenGenerateVO generateToken(Integer adminId, String name, Integer expiresInDays) {
        long activeCount = this.count(new LambdaQueryWrapper<AdminApiToken>()
                .eq(AdminApiToken::getAdminId, adminId)
                .eq(AdminApiToken::getIsActive, 1));
        if (activeCount >= MAX_TOKENS_PER_USER) {
            throw new GlobalException("每个管理员最多拥有 " + MAX_TOKENS_PER_USER + " 个有效Token");
        }

        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        new SecureRandom().nextBytes(randomBytes);
        String plainToken = HexUtil.encodeHexStr(randomBytes);

        String tokenHash = sha256(plainToken);

        Long expiresAt = null;
        if (expiresInDays != null && expiresInDays > 0) {
            expiresAt = System.currentTimeMillis() / 1000 + (long) expiresInDays * 86400;
        }

        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            throw new GlobalException("管理员不存在");
        }

        AdminApiToken token = new AdminApiToken();
        token.setAdminId(adminId);
        token.setTokenHash(tokenHash);
        token.setTokenPrefix(plainToken.substring(0, 8));
        token.setName(StrUtil.isBlank(name) ? "default" : name);
        token.setCreatedAt(System.currentTimeMillis() / 1000);
        token.setExpiresAt(expiresAt);
        token.setIsActive(1);
        this.save(token);

        String redisKey = Constants.ADMIN_API_TOKEN + tokenHash;
        redisCache.setCacheObject(redisKey, adminUser.getUsername(), REDIS_CACHE_HOURS * 60, TimeUnit.MINUTES);

        AdminApiTokenGenerateVO vo = new AdminApiTokenGenerateVO();
        vo.setId(token.getId());
        vo.setToken(plainToken);
        vo.setName(token.getName());
        vo.setExpiresAt(expiresAt);
        return vo;
    }

    @Override
    public List<AdminApiTokenVO> listTokens(Integer adminId) {
        List<AdminApiToken> tokens = this.list(new LambdaQueryWrapper<AdminApiToken>()
                .eq(AdminApiToken::getAdminId, adminId)
                .orderByDesc(AdminApiToken::getCreatedAt));
        return tokens.stream().map(t -> {
            AdminApiTokenVO vo = new AdminApiTokenVO();
            vo.setId(t.getId());
            vo.setTokenPrefix(t.getTokenPrefix());
            vo.setName(t.getName());
            vo.setCreatedAt(t.getCreatedAt());
            vo.setLastUsedAt(t.getLastUsedAt());
            vo.setExpiresAt(t.getExpiresAt());
            vo.setIsActive(t.getIsActive());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void revokeToken(Integer tokenId, Integer adminId) {
        AdminApiToken token = this.getById(tokenId);
        if (token == null || !token.getAdminId().equals(adminId)) {
            throw new GlobalException("Token不存在或无权操作");
        }
        token.setIsActive(0);
        this.updateById(token);

        String redisKey = Constants.ADMIN_API_TOKEN + token.getTokenHash();
        redisCache.deleteObject(redisKey);
    }

    @Override
    public String validateToken(String plainToken) {
        String tokenHash = sha256(plainToken);
        String redisKey = Constants.ADMIN_API_TOKEN + tokenHash;

        String username = redisCache.getCacheObject(redisKey);
        if (username != null) {
            asyncUpdateLastUsedAt(tokenHash);
            return username;
        }

        AdminApiToken token = this.getOne(new LambdaQueryWrapper<AdminApiToken>()
                .eq(AdminApiToken::getTokenHash, tokenHash)
                .eq(AdminApiToken::getIsActive, 1));
        if (token == null) {
            return null;
        }

        if (token.getExpiresAt() != null && token.getExpiresAt() < System.currentTimeMillis() / 1000) {
            token.setIsActive(0);
            this.updateById(token);
            return null;
        }

        AdminUser adminUser = adminUserMapper.selectById(token.getAdminId());
        if (adminUser == null) {
            return null;
        }

        redisCache.setCacheObject(redisKey, adminUser.getUsername(), REDIS_CACHE_HOURS * 60, TimeUnit.MINUTES);
        asyncUpdateLastUsedAt(tokenHash);
        return adminUser.getUsername();
    }

    @Async
    public void asyncUpdateLastUsedAt(String tokenHash) {
        try {
            this.update(new LambdaUpdateWrapper<AdminApiToken>()
                    .eq(AdminApiToken::getTokenHash, tokenHash)
                    .set(AdminApiToken::getLastUsedAt, System.currentTimeMillis() / 1000));
        } catch (Exception e) {
            log.warn("更新API Token最后使用时间失败: {}", e.getMessage());
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexUtil.encodeHexStr(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}
