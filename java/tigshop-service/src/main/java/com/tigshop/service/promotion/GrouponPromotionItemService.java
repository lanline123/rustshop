package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemCreateParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemUpdateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponProductPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionItemPageQuery;
import com.tigshop.bean.vo.promotion.GrouponPostVO;
import com.tigshop.bean.vo.promotion.GrouponProductVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemDetailVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemPageVO;
import com.tigshop.service.common.BaseService;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_item(拼团活动商品)】的数据库操作Service
 * @createDate 2025-08-28 11:18:30
 */
public interface GrouponPromotionItemService extends BaseService<GrouponPromotionItem> {

    void create(PromotionPromotionItemCreateParam param);

    void update(PromotionPromotionItemUpdateParam param);

    GrouponPromotionItemDetailVO detail(Integer grouponPromotionItemId);

    GrouponPostVO post(Long grouponPromotionItemId);

    Page<GrouponPromotionItemPageVO> list(GrouponPromotionItemPageQuery pageQuery);

    Page<GrouponProductVO> productList(GrouponProductPageQuery pageQuery);

}
