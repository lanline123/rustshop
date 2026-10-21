package com.tigshop.service.user;

import java.math.BigDecimal;

/** Single backend entry point for combining ordinary rank and paid-member benefits. */
public interface MemberBenefitService {
    BigDecimal resolveDiscount(Integer userId, Integer rankId);

    BigDecimal resolvePointMultiplier(Integer userId, Integer rankId);

    boolean resolveFreeShipping(Integer userId, Integer rankId);
}
