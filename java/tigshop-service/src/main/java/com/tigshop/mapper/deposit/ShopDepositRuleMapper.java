package com.tigshop.mapper.deposit;

import com.tigshop.bean.model.deposit.ShopDepositRule;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ShopDepositRuleMapper extends BaseMapper<ShopDepositRule> {

    /**
     * 按 (主体类型 × 店铺类型 × 类目 × 生效期) 命中规则，返回各候选类目命中的最高保证金额。
     *
     * <p>过滤条件：status=1 且 is_delete=0 且 start_time&lt;=now 且 end_time&gt;=now，
     * 主体类型命中 merchant_types（FIND_IN_SET），店铺类型命中 shop_types（FIND_IN_SET），
     * 且关联类目 rc.category_id 落在传入的候选类目集合中。返回字段：categoryId、depositAmount。
     * 三级就近选取由 Java 侧按各商品类目向上溯源完成。</p>
     *
     * @param merchantType 店铺主体类型（1个人 2企业）
     * @param shopType     店铺类型（1店铺 2供应商）
     * @param categoryIds  候选类目集合（在售商品类目 + 其各级祖先）
     * @param now          当前时间戳（秒）
     * @return 每个命中类目一行，含 categoryId 与该类目命中的最高 depositAmount
     */
    List<Map<String, Object>> matchMaxByCategories(@Param("merchantType") Integer merchantType,
                                                   @Param("shopType") Integer shopType,
                                                   @Param("categoryIds") List<Integer> categoryIds,
                                                   @Param("now") Long now);
}
