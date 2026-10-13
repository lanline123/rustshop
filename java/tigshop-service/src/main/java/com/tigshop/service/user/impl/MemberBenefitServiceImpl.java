package com.tigshop.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.mapper.user.UserRankMapper;
import com.tigshop.service.user.MemberBenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MemberBenefitServiceImpl implements MemberBenefitService {
    private final UserRankMapper userRankMapper;

    @Override
    public BigDecimal resolveDiscount(Integer userId, Integer rankId) {
        UserRank rank = rank(rankId);
        BigDecimal discount = rank == null ? null : rank.getDiscount();
        return discount;
    }

    @Override
    public BigDecimal resolvePointMultiplier(Integer userId, Integer rankId) {
        UserRank rank = rank(rankId);
        BigDecimal multiplier = rank == null || rank.getRankPoint() == null || rank.getRankPoint().isBlank()
                ? BigDecimal.ONE : decimal(rank.getRankPoint(), BigDecimal.ONE);
        return multiplier;
    }

    @Override
    public boolean resolveFreeShipping(Integer userId, Integer rankId) {
        UserRank rank = rank(rankId);
        boolean freeShipping = rank != null && Integer.valueOf(1).equals(rank.getFreeShipping());
        return freeShipping;
    }

    private UserRank rank(Integer rankId) {
        return rankId == null ? null : userRankMapper.selectOne(new LambdaQueryWrapper<UserRank>().eq(UserRank::getRankId, rankId));
    }

    private BigDecimal decimal(String value, BigDecimal fallback) {
        try { return new BigDecimal(value); } catch (RuntimeException ignored) { return fallback; }
    }
}
