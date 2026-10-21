package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupsCreateParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupsUpdateParam;
import com.tigshop.bean.query.promotion.card.GiftCardGroupsPageQuery;
import com.tigshop.bean.vo.promotion.card.GiftCardGroupsVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * @author Tigshop项目组
 */
public interface GiftCardGroupsService extends BaseService<GiftCardGroups> {
    Page<GiftCardGroupsVO> list(GiftCardGroupsPageQuery query);

    GiftCardGroupsVO detail(Integer cardGroupsId);

    void updateStatus(Integer cardGroupsId, Integer status);

    void createGiftCardGroups(GiftCardGroupsCreateParam param);

    void updateGiftCardGroups(GiftCardGroupsUpdateParam param);

    List<GiftCardGroupsVO> getGiftCardGroupsByCardType(Integer cardType, Integer giftCardGroupsId, Integer shopId);

    void deleteGiftCardGroups(Integer cardGroupsId);

    void giftCardGroupProductsUpdate(GiftCardGroupsUpdateParam param);
}
