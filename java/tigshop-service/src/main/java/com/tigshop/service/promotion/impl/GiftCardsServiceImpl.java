package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.finance.ClientUserRechargeOrderUpdateDTO;
import com.tigshop.bean.dto.finance.UserRechargeOrderUpdateDTO;
import com.tigshop.bean.dto.promotion.GiftCardUpdateStatusDTO;
import com.tigshop.bean.enums.product.ProductStockRecordTypeEnum;
import com.tigshop.bean.enums.promotion.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.product.ProductStockRecord;
import com.tigshop.bean.model.product.ProductStockRecordDetail;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCardOrder;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.param.promotion.giftcard.ExchangeCardParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardClaimParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardCreateParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardUpdateParam;
import com.tigshop.bean.query.promotion.card.GiftCardsPageQuery;
import com.tigshop.bean.vo.product.ExchangeProductToCartVO;
import com.tigshop.bean.vo.product.ProductSkuVO;
import com.tigshop.bean.vo.promotion.card.*;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.promotion.GiftCardGroupsMapper;
import com.tigshop.mapper.promotion.GiftCardOrderMapper;
import com.tigshop.mapper.promotion.GiftCardsMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserRechargeOrderService;
import com.tigshop.service.promotion.GiftCardGroupsService;
import com.tigshop.service.promotion.GiftCardsService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.tigshop.bean.enums.cart.CartTypeEnum.*;
import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.Constants.YES;

/**
 * @author Tigshop项目组
 */
@RequiredArgsConstructor
@Service
public class GiftCardsServiceImpl extends BaseServiceImpl<GiftCardsMapper, GiftCards> implements GiftCardsService {
    private final GiftCardGroupsMapper giftCardGroupsMapper;
    private final GiftCardOrderMapper giftCardOrderMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final UserRechargeOrderService userRechargeOrderService;
    private final GiftCardGroupsService giftCardGroupsService;
    private final CartService cartService;
    private final ConfigService configService;
    private final ShopService shopService;
    private final RedisCache redisCache;
    private final OrderItemMapper orderItemMapper;

    @Override
    public void importTemplate(HttpServletResponse response) {
        List<GiftCardsTemplateVO> build = List.of(GiftCardsTemplateVO.builder()
                .cardNumber("GC20251031133058")
                .cardPassword("PASSWORD123456")
                .build());
        ExcelUtils<GiftCardsTemplateVO> excelUtils = new ExcelUtils<>(build, GiftCardsTemplateVO.class);
        excelUtils.exportExcelWeb(response);
    }

    @Override
    public List<GiftCardsExportVO> exportList(Integer cardGroupsId) {
        List<GiftCards> giftCardList = this.lambdaQuery().eq(GiftCards::getCardGroupsId, cardGroupsId).list();

        return giftCardList.stream().map(giftCards -> GiftCardsExportVO.builder()
                .cardNumber(giftCards.getCardNumber())
                .cardPassword(giftCards.getCardPassword())
                .giftCardSn(giftCards.getGiftCardSn())
                .isSold(giftCards.getUserId() != null ? "是" : "否")
                .isExchange(giftCards.getStatus() == 1 ? "是" : "否")
                .addTime(TigUtils.handelTime(giftCards.getAddTime()))
                .build()
        ).toList();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public GiftCardsImportVO importList(Integer cardGroupsId, MultipartFile file) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));

        GiftCardGroups cardGroups = giftCardGroupsService.getById(cardGroupsId);
        Assert.isTrue(cardGroups != null, () -> new GlobalException("请选择正确的礼品卡"));
        ExcelUtils<GiftCardsTemplateVO> excelUtils = new ExcelUtils<>();
        List<GiftCardsTemplateVO> giftCardsTemplateList = excelUtils.importExcel(file, GiftCardsTemplateVO.class);

        List<GiftCards> giftCardList = giftCardsTemplateList.stream().map(item -> GiftCards.builder()
                .cardGroupsId(cardGroupsId)
                .cardNumber(item.getCardNumber())
                .cardPassword(item.getCardPassword())
                .giftCardSn(IdUtil.simpleUUID())
                .isAvailable(YES)
                .status(NO)
                .addTime(StringUtils.getCurrentTime())
                .expiredTime(0L)
                .build()).toList();

        if (Objects.equals(cardGroups.getCardType(), GiftCardEnum.PHYSICAL_CARD.getCode())){
            for (GiftCards card : giftCardList) {
                card.setExpiredTime(cardGroups.getExpireTime() == null ? 0L : cardGroups.getExpireTime());
            }
        }

        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (GiftCards card : giftCardList) {
            try {
                if (card.getCardNumber().length() < 8) {
                    failList.add(card.getCardNumber() + "（卡号长度不能小于8位）");
                }
                if (card.getCardPassword().length() < 6 || card.getCardPassword().length() > 16) {
                    failList.add(card.getCardNumber() + "（密码长度不能小于6位或者大于16位）");
                }
                this.save(card);
                successList.add(card.getCardNumber());
            } catch (DuplicateKeyException e) {
                failList.add(card.getCardNumber());
            } catch (Exception e) {
                failList.add(card.getCardNumber() + "（导入失败）");
            }
        }

        cardGroups.setAvailabilityStock(cardGroups.getAvailabilityStock() + successList.size());

        giftCardGroupsService.updateById(cardGroups);

        return GiftCardsImportVO.builder()
                .failList(failList)
                .failCount(failList.size())
                .successCount(successList.size())
                .build();
    }

    @Override
    public Page<GiftCardsVO> list(GiftCardsPageQuery query) {
        Page<GiftCards> page = buildSortOrder(query);
        Page<GiftCards> giftCardsPage = this.lambdaQuery()
                .like(query.getKeyword() != null, GiftCards::getCardNumber, query.getKeyword())
                .eq(GiftCards::getCardGroupsId, query.getCardGroupsId())
                .page(page);

        List<GiftCardsVO> results = giftCardsPage.getRecords().stream().map(giftCards -> GiftCardsVO.builder()
                .giftCardsId(giftCards.getGiftCardsId())
                .cardGroupsId(giftCards.getCardGroupsId())
                .cardNumber(giftCards.getCardNumber())
                .giftCardSn(giftCards.getGiftCardSn())
                .cardPassword(giftCards.getCardPassword())
                .isAvailable(giftCards.getIsAvailable())
                .isSold(giftCards.getUserId() != null ? "是" : "否")
                .isExchange(giftCards.getStatus() == 1 ? "是" : "否")
                .addTime(giftCards.getAddTime().toString())
                .build()
        ).toList();
        return PageUtil.convertPage(giftCardsPage, results);
    }

    @Override
    public GiftCardsVO giftCardsDetail(String giftCardSn, boolean showPassword) {
        GiftCards cards = this.getOne(Wrappers.lambdaQuery(GiftCards.class)
                .eq(GiftCards::getGiftCardSn, giftCardSn)
                .last("limit 1"));

        if (cards != null) {
            GiftCardGroups cardGroups = giftCardGroupsService.getById(cards.getCardGroupsId());
            String shopTitle = null;
            String shopLogo = null;
            if (cardGroups.getShopId() == 0) {
                shopTitle = configService.getConfigVal(SettingsEnum.SHOP_NAME);
                shopLogo = configService.getConfigVal(SettingsEnum.SHOP_LOGO);
            } else {
                Shop shop = shopService.getById(cardGroups.getShopId());
                shopTitle = shop.getShopTitle();
                shopLogo = shop.getShopLogo();
            }
            String cardNumber = StringUtils.maskMiddleHalf(cards.getCardNumber());
            String cardPassword = StringUtils.maskMiddleHalf(cards.getCardPassword());
            if (showPassword) {
                cardNumber = cards.getCardNumber();
                cardPassword = cards.getCardPassword();
            }
            return GiftCardsVO.builder()
                    .giftCardsId(cards.getGiftCardsId())
                    .cardGroupsId(cards.getCardGroupsId())
                    .cardNumber(cardNumber)
                    .cardPassword(cardPassword)
                    .giftCardSn(cards.getGiftCardSn())
                    .status(cards.getStatus())
                    .isAvailable(cards.getIsAvailable())
                    .isSold(cards.getUserId() != null ? "是" : "否")
                    .isExchange(cards.getStatus() == 1 ? "是" : "否")
                    .addTime(cards.getAddTime().toString())
                    .expiredTime(cards.getExpiredTime())
                    .shopTitle(shopTitle)
                    .shopLogo(shopLogo)
                    .shopId(cardGroups.getShopId())
                    .build();
        }
        return new GiftCardsVO();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGiftCards(Integer giftCardsId) {
        GiftCards cards = this.getById(giftCardsId);

        if (cards == null) {
            throw new GlobalException("礼品卡不存在");
        }

        // 如果未过期，但是已经售出未兑换，不可删除
        if (cards.getStatus() == 0 && cards.getUserId() != null) {
            throw new GlobalException("该礼品卡已售出，请勿删除");
        }
        //减可用库存
        GiftCardGroups cardGroups = giftCardGroupsService.getById(cards.getCardGroupsId());
        if (cardGroups != null) {
            if (cards.getIsAvailable() == 1 && cardGroups.getAvailabilityStock() > 0){
                cardGroups.setAvailabilityStock(cardGroups.getAvailabilityStock() - 1);
                giftCardGroupsService.updateById(cardGroups);
            }
        }

        this.removeById(giftCardsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGiftCards(GiftCardCreateParam param) {
        GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(param.getCardGroupsId());
        Assert.notNull(giftCardGroups, () -> new GlobalException("卡组不存在"));

        if (param.getCardNumber().length() < 8) {
            throw new GlobalException(param.getCardNumber() + "（卡号长度不能小于8位）");
        }

        if (param.getCardPassword().length() < 6 || param.getCardPassword().length() > 16) {
            throw new GlobalException(param.getCardNumber() + "（密码长度不能小于6位或者大于16位）");
        }
        giftCardGroups.setAvailabilityStock(giftCardGroups.getAvailabilityStock() + 1);
        giftCardGroupsService.updateById(giftCardGroups);
        GiftCards card = GiftCards.builder()
                .cardGroupsId(param.getCardGroupsId())
                .cardNumber(param.getCardNumber())
                .cardPassword(param.getCardPassword())
                .giftCardSn(IdUtil.simpleUUID())
                .isAvailable(param.getIsAvailable())
                .status(0)
                .addTime(StringUtils.getCurrentTime())
                .expiredTime(0L)
                .build();

        if (Objects.equals(giftCardGroups.getCardType(), GiftCardEnum.PHYSICAL_CARD.getCode())){
            card.setExpiredTime(giftCardGroups.getExpireTime() == null ? 0L : giftCardGroups.getExpireTime());
        }

        try {
            this.save(card);
        } catch (DuplicateKeyException e) {
            throw new GlobalException(StrUtil.format("卡号已存在：{}", card.getCardNumber()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGiftCardsBatch(Long cardGroupsId, Integer count) {
        GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(cardGroupsId.intValue());
        Assert.notNull(giftCardGroups, () -> new GlobalException("卡组不存在"));

        // 一次生成 count 个唯一卡号（内存级去重）
        Set<String> cardNos = new HashSet<>();
        while (cardNos.size() < count) {
            cardNos.add(StringUtils.generateCardNo(cardGroupsId.intValue()));
        }

        // 过滤数据库中已存在的卡号
        List<String> existList = lambdaQuery()
                .in(GiftCards::getCardNumber, cardNos)
                .list()
                .stream()
                .map(GiftCards::getCardNumber)
                .toList();

        existList.forEach(cardNos::remove);

        // 构造实体
        List<GiftCards> cards = cardNos.stream().map(cardNo -> GiftCards.builder()
                .cardGroupsId(cardGroupsId.intValue())
                .cardNumber(cardNo)
                .giftCardSn(IdUtil.simpleUUID())
                .cardPassword(StringUtils.generatePassword())
                .isAvailable(YES)
                .status(0)
                .addTime(StringUtils.getCurrentTime())
                .expiredTime(0L)
                .build()).toList();
        for (GiftCards card : cards) {
            if (Objects.equals(giftCardGroups.getCardType(), GiftCardEnum.PHYSICAL_CARD.getCode())){
                card.setExpiredTime(giftCardGroups.getExpireTime() == null ? 0L : giftCardGroups.getExpireTime());
            }
        }
        giftCardGroups.setAvailabilityStock(giftCardGroups.getAvailabilityStock() + cards.size());
        giftCardGroupsService.updateById(giftCardGroups);

        // 批量写入
        saveBatch(cards, 500);

    }

    @Override
    public void updateGiftCards(GiftCardUpdateParam param) {
        Assert.notNull(param.getCardId(), () -> new GlobalException("卡ID不能为空"));

        GiftCards dbCards = this.getById(param.getCardId());
        String cardPassword = param.getCardPassword();
        if (cardPassword == null && param.getIsAvailable() == null) {
            return;
        }

        if (cardPassword != null && cardPassword.length() < 6) {
            throw new GlobalException("密码长度不能小于6位");
        }
        //如果修改了卡可用状态
        GiftCardGroups cardGroups = giftCardGroupsService.getById(dbCards.getCardGroupsId());
        if (!Objects.equals(dbCards.getIsAvailable(), param.getIsAvailable())) {
            //已售出的卡不能修改
            Assert.isTrue(dbCards.getUserId() == null, () -> new GlobalException("已售出的卡不能修改可用状态"));
            if (param.getIsAvailable() == 0) {
                //如果卡不可用，则扣减卡组可用库存
                if (cardGroups != null) {
                    if (cardGroups.getAvailabilityStock() > 0){
                        cardGroups.setAvailabilityStock(cardGroups.getAvailabilityStock() - 1);
                    }
                }
            } else {
                //如果卡可用，则增加卡组可用库存
                if (cardGroups != null) {
                    cardGroups.setAvailabilityStock(cardGroups.getAvailabilityStock() + 1);
                }
            }
        }
        giftCardGroupsService.updateById(cardGroups);

        lambdaUpdate().eq(GiftCards::getGiftCardsId, param.getCardId())
                .set(cardPassword != null, GiftCards::getCardPassword, cardPassword)
                .set(param.getIsAvailable() != null, GiftCards::getIsAvailable, param.getIsAvailable())
                .update();
    }

    // 领取卡
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void claimCard(GiftCardClaimParam param, Integer userId) {
        // 1. 检查卡是否存在
        GiftCards card = this.getOne(Wrappers.lambdaQuery(GiftCards.class)
                .eq(GiftCards::getGiftCardSn, param.getGiftCardSn())
                .last("limit 1"));

        Assert.notNull(card, () -> new GlobalException("该礼品卡不存在"));

        // 3. 判断当前状态是否为可领取
        Assert.isTrue(GiftCardClaimedEnum.RECEIVABLE.getCode().equals(card.getClaimedStatus()), () -> new GlobalException("该礼品卡不可领取"));
    }

    // 转赠卡
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String transferCard(GiftCardClaimParam param, Integer currentUserId) {
        // 1. 检查卡是否存在
        GiftCards card = this.getOne(Wrappers.lambdaQuery(GiftCards.class)
                .eq(GiftCards::getGiftCardSn, param.getGiftCardSn())
                .last("limit 1"));

        Assert.notNull(card, () -> new GlobalException("该礼品卡不存在"));

        // 2. 检查卡是否已经被领取
        if (GiftCardClaimedEnum.RECEIVED.getCode().equals(card.getClaimedStatus())) {
            Assert.notNull(card, () -> new GlobalException("该礼品卡已经被领取，无法转赠"));
        }

        // 4. 更新卡的当前领取状态
        card.setClaimedStatus(GiftCardClaimedEnum.RECEIVABLE.getCode());
        this.updateById(card);

        return getTransferLink(card.getGiftCardSn(), param.getIsFrom());
    }

    /**
     * 判断礼品卡是否过期
     */
    @Override
    public boolean isGiftCardExpired(GiftCards card) {
        long now = StringUtils.getCurrentTime();

        // 2=固定到期时间
        if (card.getExpiredTime() != null) {
            if (card.getExpiredTime() == 0) {
                return false;
            }
            return now > card.getExpiredTime();
        }
        return true;
    }

    // 通过卡号和卡密兑换卡
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GiftCardsVO exchangeCard(String cardNumber, String cardPassword, Integer userId) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("userExchangeCard_" + userId, uuid, 10, TimeUnit.SECONDS);
        if (lock) {
            try {
                // 1. 检查卡号和密码是否正确
                GiftCards card = lambdaQuery().eq(GiftCards::getCardNumber, cardNumber)
                        .eq(GiftCards::getCardPassword, cardPassword)
                        .eq(GiftCards::getIsAvailable, YES)
                        .eq(GiftCards::getStatus, NO)
                        .last("limit 1")
                        .one();

                Assert.notNull(card, () -> new GlobalException("兑换失败，请检查卡号/密码后重试"));

                GiftCardGroups cardGroups = giftCardGroupsService.getById(card.getCardGroupsId());
                Assert.notNull(cardGroups, () -> new GlobalException("该卡组不存在"));

                Assert.isTrue(card.getClaimedUserId() == null, () -> new GlobalException("该礼品卡已经被兑换"));

                // 2. 检查礼品卡是否过期
                GiftCardGroups group = giftCardGroupsMapper.selectById(card.getCardGroupsId());
                boolean giftCardExpired = isGiftCardExpired(card);
                Assert.isTrue(!giftCardExpired, () -> new GlobalException("该礼品卡已过期"));

                // 3. 兑换储值卡/兑换兑换卡
                GiftGroupsEnum gge = GiftGroupsEnum.getByCode(group.getGroupType());
                return switch (gge) {
                    case TOP_UP -> {
                        BigDecimal faceAmount = group.getFaceAmount();

                        Integer rechargeOrderId = userRechargeOrderService.rechargeOperation(
                                ClientUserRechargeOrderUpdateDTO.builder()
                                        .amount(faceAmount)
                                        .userId(userId)
                                        .build()
                        );

                        userRechargeOrderService.update(
                                UserRechargeOrderUpdateDTO.builder()
                                        .amount(faceAmount)
                                        .discountMoney(BigDecimal.ZERO)
                                        .orderId(rechargeOrderId)
                                        .postscript("充值卡兑换")
                                        .status(YES)
                                        .userId(userId)
                                        .build()
                        );

                        card.setUserId(userId);
                        card.setClaimedUserId(userId);
                        card.setClaimedStatus(GiftCardClaimedEnum.RECEIVED.getCode());
                        card.setUsedTime(StringUtils.getCurrentTime());
                        card.setStatus(YES);
                        this.updateById(card);

                        yield this.giftCardsDetail(card.getGiftCardSn(), false);
                    }

                    case EXCHANGE -> {

                        GiftCardsVO giftCardsVO = this.giftCardsDetail(card.getGiftCardSn(), false);
                        GiftCardGroupsVO giftCardGroupsDetail = giftCardGroupsService.detail(giftCardsVO.getCardGroupsId());
                        List<ProductSkuVO> giftCardProducts = giftCardGroupsDetail.getGiftCardProducts();
                        Assert.isTrue(CollUtil.isNotEmpty(giftCardProducts), () -> new GlobalException("该礼品卡绑定的商品已下架"));
                        giftCardsVO.setGiftCardProducts(giftCardProducts);

                        //清空购物车
                        cartService.clearCart(userId, TYPE_GIFT_EXCHANGE.getCode());
                        giftCardProducts.forEach(item -> {
                            AddToCartDTO addToCartDTO = new AddToCartDTO();
                            addToCartDTO.setId(item.getProductId());
                            addToCartDTO.setNumber(1);
                            addToCartDTO.setSkuId(item.getSkuId());
                            addToCartDTO.setType(TYPE_GIFT_EXCHANGE.getCode());
                            addToCartDTO.setShopId(item.getShopId());
                            addToCartDTO.setIsQuick(false);
                            addToCartDTO.setGiftCardData(ExchangeCardParam.builder()
                                    .giftCardsId(card.getGiftCardsId())
                                    .cardNumber(cardNumber)
                                    .cardPassword(cardPassword)
                                    .userId(userId)
                                    .build());
                            cartService.addProductToCart(userId, addToCartDTO);

                        });
                        giftCardsVO.setExchangeProductToCart(
                                ExchangeProductToCartVO.builder()
                                        .item(true)
                                        .flowType(TYPE_GIFT_EXCHANGE.getCode())
                                        .build());
                        yield giftCardsVO;
                    }
                    default -> throw new GlobalException("不支持的礼品卡类型");
                };
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("userExchangeCard_" + userId, uuid);
            }
        } else {
            throw new GlobalException("您点的太快了");
        }

    }

    @Override
    public String getTransferLink(String giftCardSn, Integer isFrom) {

        Assert.isTrue(Objects.equals(isFrom, GiftCardTransferFromType.H5.getCode()) || Objects.equals(isFrom, GiftCardTransferFromType.PC.getCode()), () -> new GlobalException("没有设置赠送来源"));
        Assert.isTrue(StringUtils.isNotBlank(giftCardSn), () -> new GlobalException("没有设置礼品卡编号"));

        //礼品卡名称
        GiftCards giftCard = getOne(Wrappers.lambdaQuery(GiftCards.class).eq(GiftCards::getGiftCardSn, giftCardSn));
        Assert.isTrue(giftCard != null, () -> new GlobalException("没有找到该礼品卡"));

        GiftCardGroups giftCardGroups = giftCardGroupsService.getOne(Wrappers.lambdaQuery(GiftCardGroups.class).eq(GiftCardGroups::getCardGroupsId, giftCard.getCardGroupsId()));
        Assert.isTrue(giftCardGroups != null, () -> new GlobalException("没有找到该礼品卡组"));

        String shopTitle = "";
        String baseUrl = "";

        // 获取店铺名称
        if (giftCardGroups.getShopId() == 0) {
            shopTitle = configService.getConfigVal(SettingsEnum.SHOP_NAME);
        } else {
            shopTitle = shopService.getById(giftCardGroups.getShopId()).getShopTitle();
        }
        // 获取域名
        if (isFrom.equals(GiftCardTransferFromType.PC.getCode())) {
            String pcDomain = configService.getConfigVal(SettingsEnum.PC_DOMAIN);
            if (StrUtil.isEmpty(pcDomain)) {
                pcDomain = "https://" + HeaderUtils.getRequestDomain();
            }
            baseUrl = pcDomain + "/giftCard/transfer/";
        }
        if (isFrom.equals(GiftCardTransferFromType.H5.getCode())) {
            String h5Domain = configService.getConfigVal(SettingsEnum.H5_DOMAIN);
            if (StrUtil.isEmpty(h5Domain)) {
                h5Domain = "https://" + HeaderUtils.getRequestDomain() + "/mobile";
            }
            baseUrl = h5Domain + "/pages/giftCard/transfer?giftCardSn=";
        }


        return "【" + shopTitle + "】" + baseUrl + giftCardSn + "  [" + giftCardGroups.getCardGroupName() + "]";
    }

    @Override
    public GiftCardDetailVO giftCardExchangeDetail(Integer giftCardsId) {
        // 1. 查询礼品卡信息
        GiftCards giftCard = this.getById(giftCardsId);
        Assert.notNull(giftCard, () -> new GlobalException("礼品卡不存在"));

        // 2. 查询礼品卡组信息
        GiftCardGroups cardGroup = giftCardGroupsMapper.selectById(giftCard.getCardGroupsId());
        Assert.notNull(cardGroup, () -> new GlobalException("礼品卡组不存在"));

        // 3. 构建返回对象
        GiftCardDetailVO detailVO = new GiftCardDetailVO();
        detailVO.setGroupType(cardGroup.getGroupType());
        detailVO.setCardType(cardGroup.getCardType());
        detailVO.setCardNumber(giftCard.getCardNumber());
        detailVO.setCardPassword(giftCard.getCardPassword());
        detailVO.setIsSold(giftCard.getUserId() != null ? "是" : "否");
        detailVO.setIsExchange(giftCard.getStatus() == 1 ? "是" : "否");

        // 判断是否是储值卡
        boolean isTopUpCard = Objects.equals(cardGroup.getGroupType(), GiftGroupsEnum.TOP_UP.getCode());
        // 兑换卡
        boolean isExchangeCard = Objects.equals(cardGroup.getGroupType(), GiftGroupsEnum.EXCHANGE.getCode());

        // 查询关联的订单信息
        Order giftCardOrder = null;
        OrderItem orderItem = orderItemMapper.selectOne(Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getGiftCardId, giftCardsId).last("limit 1"));
        if (orderItem != null) {
            giftCardOrder = orderMapper.selectById(orderItem.getOrderId());
        }


        // 构建用户地址信息
        GiftCardDetailVO.ExchangeInfoVO exchangeInfo = new GiftCardDetailVO.ExchangeInfoVO();
        GiftCardDetailVO.UserAddressInfoVO userAddressInfo = new GiftCardDetailVO.UserAddressInfoVO();
        GiftCardDetailVO.PayInfoVO payInfoVO = new GiftCardDetailVO.PayInfoVO();
        GiftCardDetailVO.DeliveryInfoVO deliveryInfo = new GiftCardDetailVO.DeliveryInfoVO();

        exchangeInfo.setDeliveryInfo(deliveryInfo);
        exchangeInfo.setPayInfo(payInfoVO);
        exchangeInfo.setUserAddressInfo(userAddressInfo);
        detailVO.setExchangeInfo(exchangeInfo);

        if (isTopUpCard && giftCard.getUserId() != null && giftCard.getUsedTime() != null) {

            detailVO.setOrderId(null);
            User user = userMapper.selectById(giftCard.getUserId());
            userAddressInfo.setUsername(user.getUsername());
            userAddressInfo.setMobile(user.getMobile());

            payInfoVO.setPayTypeName("储值卡支付");
            payInfoVO.setExchangeAmount(cardGroup.getFaceAmount());
            payInfoVO.setExchangeTime(TigUtils.handelTime(giftCard.getUsedTime()));

            exchangeInfo.setPayInfo(payInfoVO);
            exchangeInfo.setUserAddressInfo(userAddressInfo);
            detailVO.setExchangeInfo(exchangeInfo);
            return detailVO;
        }
        if (giftCardOrder != null && giftCardOrder.getOrderId() != null) {

            // 规则2和3: 处理兑换信息
            if (isExchangeCard) {
                // 规则3: 如果是兑换卡且是电子卡，则兑换信息是兑换订单里面的信息
                Order order = orderMapper.selectById(giftCardOrder.getOrderId());
                if (order != null) {

                    detailVO.setOrderId(order.getOrderId());
                    User user = userMapper.selectById(order.getUserId());
                    if (user != null) {
                        userAddressInfo.setUsername(user.getUsername());
                    }
                    userAddressInfo.setConsignee(order.getConsignee());
                    userAddressInfo.setMobile(order.getMobile());
                    userAddressInfo.setEmail(order.getEmail());

                    // 处理地区信息
                    if (StrUtil.isNotBlank(order.getRegionNames())) {
                        List<?> regionNamesList = JsonUtil.fromJson(order.getRegionNames(), List.class);
                        if (regionNamesList != null && !regionNamesList.isEmpty()) {
                            List<String> regionNames = regionNamesList.stream()
                                    .map(String::valueOf)
                                    .toList();
                            userAddressInfo.setRegionName(String.join(" ", regionNames));
                        }
                    }
                    userAddressInfo.setAddress(order.getAddress());
                    userAddressInfo.setBuyerNote(order.getBuyerNote());

                    // 构建配送信息
                    deliveryInfo.setShippingTypeName(order.getShippingTypeName());
                    if (order.getShippingTime() != null && order.getShippingTime() > 0) {
                        deliveryInfo.setShippingTime(TigUtils.handelTime(order.getShippingTime()));
                    }
                    deliveryInfo.setLogisticsName(order.getLogisticsName());
                    deliveryInfo.setTrackingNo(order.getTrackingNo());

                    payInfoVO.setPayTypeName("兑换卡支付");
                    payInfoVO.setExchangeTime(TigUtils.handelTime(giftCard.getUsedTime()));
                    payInfoVO.setReceivedTime(TigUtils.handelTime(order.getReceivedTime()));

                    exchangeInfo.setPayInfo(payInfoVO);
                    exchangeInfo.setUserAddressInfo(userAddressInfo);
                    exchangeInfo.setDeliveryInfo(deliveryInfo);
                    detailVO.setExchangeInfo(exchangeInfo);
                }
            }
        }


        return detailVO;
    }

    @Override
    public void updateGiftCardsAvailableStatus(GiftCardUpdateStatusDTO dto) {

        GiftCards giftCards = getById(dto.getGiftCardsId());
        Assert.isTrue(giftCards != null, () -> new GlobalException("该礼品卡信息异常"));
        GiftCardGroups cardGroups = giftCardGroupsService.getById(giftCards.getCardGroupsId());
        if (!Objects.equals(giftCards.getIsAvailable(), dto.getIsAvailable())){
            //已售出的卡不能修改
            Assert.isTrue(giftCards.getUserId() == null, () -> new GlobalException("已售出的卡不能修改可用状态"));
            if (dto.getIsAvailable() == 1){
                cardGroups.setAvailabilityStock(cardGroups.getAvailabilityStock() + 1);
            }else if (dto.getIsAvailable() == 0){
                if (cardGroups.getAvailabilityStock() > 0){
                    cardGroups.setAvailabilityStock(cardGroups.getAvailabilityStock() - 1);
                }
            }
            giftCardGroupsService.updateById(cardGroups);

            giftCards.setIsAvailable(dto.getIsAvailable());
            updateById(giftCards);
        }
    }

    public GiftCardOrderMapper getGiftCardOrderMapper() {
        return giftCardOrderMapper;
    }
}
