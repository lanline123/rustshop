package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.GiftCardUpdateStatusDTO;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.param.promotion.giftcard.GiftCardClaimParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardCreateParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardUpdateParam;
import com.tigshop.bean.query.promotion.card.GiftCardsPageQuery;
import com.tigshop.bean.vo.promotion.card.*;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Tigshop项目组
 */
public interface GiftCardsService extends BaseService<GiftCards> {
    // 卡导入模板
    void importTemplate(HttpServletResponse response);

    // 卡列表导出
    List<GiftCardsExportVO> exportList(Integer cardGroupsId);

    // 卡列表导入
    GiftCardsImportVO importList(Integer cardGroupsId, MultipartFile file);

    // 卡列表查询
    Page<GiftCardsVO> list(GiftCardsPageQuery query);

    // 卡详情
    GiftCardsVO giftCardsDetail( String giftCardSn, boolean showPassword);

    // 卡删除
    void deleteGiftCards(Integer giftCardsId);
  
    // 卡添加
    void createGiftCards(GiftCardCreateParam param);

    // 卡批量生成
    void createGiftCardsBatch(Long cardGroupsId, Integer count);

    // 卡编辑
    void updateGiftCards(GiftCardUpdateParam param);

    // 领取卡
    void claimCard(GiftCardClaimParam  param, Integer userId);

    // 转赠卡
    String transferCard(GiftCardClaimParam  param, Integer currentUserId);

    // 卡是否过期
    boolean isGiftCardExpired(GiftCards card);

    // 通过卡号和卡密兑换卡
    GiftCardsVO exchangeCard(String cardNumber, String cardPassword, Integer userId);

    String getTransferLink(String giftCardSn, Integer isFrom);

    GiftCardDetailVO giftCardExchangeDetail(Integer giftCardsId);

    void updateGiftCardsAvailableStatus(GiftCardUpdateStatusDTO dto);
}
