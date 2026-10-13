package com.tigshop.service.finance;

import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleMatchVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleTargetVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CategoryFeeRuleMatchService {

    CategoryFeeRuleMatchVO match(int targetType, int targetId, int categoryId);

    Map<Integer, CategoryFeeRuleMatchVO> batchMatch(int targetType, int targetId, Collection<Integer> categoryIds);

    List<CategoryFeeRuleTargetVO> listByTarget(int targetType, int targetId);

    void evictCacheByTargetType(int targetType);
}
