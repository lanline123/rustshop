package com.tigshop.service.promotion.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.CouponIssueBatch;
import com.tigshop.bean.query.promotion.coupon.CouponIssueBatchPageQuery;
import com.tigshop.bean.vo.promotion.CouponIssueBatchVO;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.promotion.CouponIssueBatchMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.CouponIssueBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 优惠券发放批次服务实现
 */
@RequiredArgsConstructor
@Service
public class CouponIssueBatchServiceImpl extends BaseServiceImpl<CouponIssueBatchMapper, CouponIssueBatch> implements CouponIssueBatchService {

    private static final Map<Integer, String> SOURCE_TYPE_MAP = Map.of(
            0, "用户领取",
            1, "抽奖中奖",
            2, "手动发放"
    );

    @Override
    public Long createBatch(Integer couponId, Integer userCount, Integer couponCount, Integer sourceType) {
        CouponIssueBatch batch = CouponIssueBatch.builder()
                .couponId(couponId)
                .userCount(userCount)
                .couponCount(couponCount)
                .issueTime(StringUtils.getCurrentTime())
                .sourceType(sourceType)
                .build();
        this.save(batch);
        return batch.getId();
    }

    @Override
    public Page<CouponIssueBatchVO> list(CouponIssueBatchPageQuery query) {
        Page<CouponIssueBatchVO> page = new Page<>(query.getPage(), query.getSize());

        MPJLambdaWrapper<CouponIssueBatch> wrapper = new MPJLambdaWrapper<CouponIssueBatch>()
                .selectAll(CouponIssueBatch.class)
                .selectAs(Coupon::getCouponName, CouponIssueBatchVO::getCouponName)
                .leftJoin(Coupon.class, Coupon::getCouponId, CouponIssueBatch::getCouponId)
                .eq(query.getCouponId() != null, CouponIssueBatch::getCouponId, query.getCouponId())
                .eq(query.getSourceType() != null, CouponIssueBatch::getSourceType, query.getSourceType())
                .orderByDesc(CouponIssueBatch::getIssueTime);

        Page<CouponIssueBatchVO> result = this.selectJoinListPage(page, CouponIssueBatchVO.class, wrapper);

        result.getRecords().forEach(vo ->
                vo.setSourceTypeName(SOURCE_TYPE_MAP.getOrDefault(vo.getSourceType(), "未知"))
        );

        return result;
    }
}
