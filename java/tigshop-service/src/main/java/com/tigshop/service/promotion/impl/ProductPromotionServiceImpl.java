// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.promotion.ProductPromotionDTO;
import com.tigshop.bean.dto.promotion.ProductPromotionListDTO;
import com.tigshop.bean.enums.promotion.ProductPromotionTypeEnum;
import com.tigshop.bean.enums.promotion.PromotionRangeEnum;
import com.tigshop.bean.enums.promotion.PromotionTimeStatusEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.ProductGift;
import com.tigshop.bean.model.promotion.ProductPromotion;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.query.promotion.product.ConflictPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.ProductPromotionVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.product.ConflictPageVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.mapper.promotion.ProductPromotionMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.ProductGiftService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.promotion.ProductPromotionService;
import com.tigshop.service.promotion.PromotionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.promotion.ProductPromotionTypeEnum.*;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.HttpStatus.EXIST_CONFLICT_PROMOTION_ERROR;
import static com.tigshop.common.constant.promotion.ProductPromotionConstants.EXIST_CONFLICT_PROMOTION;
import static com.tigshop.common.constant.promotion.ProductPromotionConstants.PRODUCT_PROMOTION_TYPE_ERROR;
import static java.math.BigDecimal.ROUND_HALF_DOWN;

/**
 * 满减活动服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ProductPromotionServiceImpl extends BaseServiceImpl<ProductPromotionMapper, ProductPromotion> implements ProductPromotionService {

    @Resource
    private PromotionService promotionService;

    @Resource
    private ProductGiftService productGiftService;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    private GalleryPicService galleryPicService;

    @Resource
    private com.tigshop.service.user.MemberAudienceService memberAudienceService;

    @Override
    public Page<ProductPromotionVO> list(ProductPromotionListDTO listDTO) {
        // 分页
        Page<ProductPromotion> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ProductPromotion> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ProductPromotion::getPromotionName, keyword);
        }

        String promotionType = listDTO.getPromotionType();
        if (StrUtil.isNotEmpty(promotionType)) {
            //promotionType 格式为1,2 将其转成List<Integer>
            List<Integer> typeList = Arrays.stream(promotionType.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            queryWrapper.in(ProductPromotion::getPromotionType, typeList);
        }

        Integer shopId = getShopId();
//        if (shopId != null && shopId > 0) {
            queryWrapper.eq(ProductPromotion::getShopId, shopId);
//        }

        // 状态检索
        Integer isGoing = listDTO.getIsGoing();
        if (isGoing != null && isGoing > 0) {
            long currentTime = StringUtils.getCurrentTime();
            if (isGoing == PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getCode()) {
                queryWrapper.le(ProductPromotion::getStartTime, currentTime).ge(ProductPromotion::getEndTime, currentTime);
            } else if (isGoing == PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getCode()) {
                queryWrapper.gt(ProductPromotion::getStartTime, currentTime);
            } else if (isGoing == PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getCode()) {
                queryWrapper.lt(ProductPromotion::getEndTime, currentTime);
            }
        }

        // 执行查询
        Page<ProductPromotion> productPromotionPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ProductPromotion> productPromotionPageRecords = productPromotionPage.getRecords();
        // 转换为VO
        List<ProductPromotionVO> productPromotionVOList = productPromotionPageRecords.stream()
                .map(productPromotion -> {
                    ProductPromotionVO productPromotionVO = new ProductPromotionVO();
                    convert(productPromotion, productPromotionVO);

                    BeanUtils.copyProperties(productPromotion, productPromotionVO);
                    return productPromotionVO;
                }).toList();
        return PageUtil.convertPage(productPromotionPage, productPromotionVOList);
    }

    /**
     * 数据转换
     *
     * @param productPromotion   productPromotion
     * @param productPromotionVO productPromotionVO
     */
    public void convert(ProductPromotion productPromotion, ProductPromotionVO productPromotionVO) {
        // 时间转换
        Long startTime = productPromotion.getStartTime();

        List<String> productTime = new ArrayList<>();

        if (startTime > 0) {
            DateTime startDate = DateUtil.date(startTime * 1000);
            productPromotionVO.setStartTime(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));
            productTime.add(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            productPromotionVO.setStartTime("0");
        }

        Long endTime = productPromotion.getEndTime();
        if (endTime > 0) {
            DateTime endDate = DateUtil.date(endTime * 1000);
            productPromotionVO.setEndTime(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
            productTime.add(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            productPromotionVO.setEndTime("0");
        }
        productPromotionVO.setProductTime(productTime);

        // json 转换
        productPromotionVO.setLimitUserRank(StringUtils.str2IntList(productPromotion.getLimitUserRank()));
        productPromotionVO.setRangeData(StringUtils.str2IntList(productPromotion.getRangeData()));

        // 状态
        long currentTime = StringUtils.getCurrentTime();
        if (currentTime >= productPromotion.getStartTime() && currentTime <= productPromotion.getEndTime()) {
            productPromotionVO.setProductStatus(PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getDescription());
        } else if (currentTime < productPromotion.getStartTime()) {
            productPromotionVO.setProductStatus(PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getDescription());
        } else if (currentTime > productPromotion.getEndTime()) {
            productPromotionVO.setProductStatus(PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getDescription());
        }
        // 活动类型
        productPromotionVO.setPromotionTypeName(ProductPromotionTypeEnum.getTypeName(productPromotion.getPromotionType()));
        // 类型相关数据
        String promotionTypeData = productPromotion.getPromotionTypeData();
        if (StrUtil.isNotEmpty(promotionTypeData)) {
            List<Integer> giftIds = new ArrayList<>();
            List<ProductPromotionVO.PromotionTypeData> promotionTypeDataList = JsonUtil.fromJson(promotionTypeData, new TypeReference<>() {
            });
            for (ProductPromotionVO.PromotionTypeData typeData : promotionTypeDataList) {
                if (typeData.getGiftId() != null) {
                    giftIds.add(typeData.getGiftId());
                }
            }
            productPromotionVO.setPromotionTypeData(promotionTypeDataList);
//            for (Object obj : promotionTypeDataList) {
//                if (obj instanceof JSONObject jsonObject) {
//                    Integer giftId = jsonObject.getInt("gift_id");
//                    if (giftId != null) {
//                        giftIds.add(giftId);
//                    }
//                }
//            }

            if (!giftIds.isEmpty()) {
                List<ProductGift> productGiftList = productGiftService.listByIds(giftIds);
                productPromotionVO.setProductGift(productGiftList);
            }

        }
    }

    @Override
    public ProductPromotionVO detail(Integer id) {
        if (id != null) {
            ProductPromotion productPromotion = this.getById(id);
            if (productPromotion == null) {
                return null;
            }
            ProductPromotionVO productPromotionVO = new ProductPromotionVO();
            convert(productPromotion, productPromotionVO);
            BeanUtils.copyProperties(productPromotion, productPromotionVO);

            // 从 promotion 表获取适用会员范围字段
            Integer type = gainType(productPromotion.getPromotionType());
            Promotion promotion = promotionService.getOne(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getRelationId, id)
                    .eq(Promotion::getType, type));
            if (promotion != null) {
                productPromotionVO.setMemberRange(promotion.getMemberRange());
                productPromotionVO.setMemberMarks(StringUtils.str2IntList(promotion.getMemberMarks()));
                com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget =
                        memberAudienceService.parseScope(promotion.getMemberScope());
                productPromotionVO.setSendUserTarget(memberAudienceService.fillTargetUsers(sendUserTarget));
                productPromotionVO.setMemberAudience(memberAudienceService.toLegacy(sendUserTarget));
            }

            return productPromotionVO;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(ProductPromotionDTO createDTO) {
        if (createDTO != null) {
            // 判断是否有商品冲突
            List<ProductPromotion> conflictList = getConflictList(createDTO);
            if (CollUtil.isNotEmpty(conflictList)) {
                // 返回 5001 给前端做特殊处理
                throw new GlobalException(EXIST_CONFLICT_PROMOTION, EXIST_CONFLICT_PROMOTION_ERROR);
            }

            ProductPromotion productPromotion = new ProductPromotion();
            Integer shopId = getShopId();
            shopId = shopId != null ? shopId : 0;
            createDTO.setShopId(shopId);
            // 默认无上限 = 0
            productPromotion.setMaxOrderAmount(BigDecimal.valueOf(0));
            dataJudge(createDTO, productPromotion);

            BeanUtils.copyProperties(createDTO, productPromotion);

            boolean isSave = this.save(productPromotion);
            if (isSave) {
                // 保存营销活动
                savePromotion(productPromotion, createDTO.getMemberRange(), createDTO.getMemberMarks(),
                        memberAudienceService.resolveWriteScope(createDTO.getSendUserTarget(), createDTO.getMemberAudience()));
            }

            return isSave;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public void savePromotion(ProductPromotion productPromotion, Integer memberRange, List<Integer> memberMarks, String memberScope) {
        Integer relationId = productPromotion.getPromotionId();
        relationId = relationId != null ? relationId : 0;
        Long startTime = productPromotion.getStartTime();
        Long endTime = productPromotion.getEndTime();
        Integer type = gainType(productPromotion.getPromotionType());
        String memberMarksStr = memberMarks != null ? memberMarks.toString() : null;

        // 根据 relationId 查询是否有营销活动
        if (promotionService.count(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, relationId)
                .in(Promotion::getType, PromotionType.TYPE_REDUCE.getCode(),
                        PromotionType.TYPE_DISCOUNT.getCode(),
                        PromotionType.TYPE_GIVE.getCode())) > 0) {
            // 更新部分字段（使用 in 匹配原有类型，同时更新 type 字段以支持类型变更）
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, relationId)
                    .in(Promotion::getType, PromotionType.TYPE_REDUCE.getCode(),
                            PromotionType.TYPE_DISCOUNT.getCode(),
                            PromotionType.TYPE_GIVE.getCode())
                    .set(Promotion::getType, type)
                    .set(Promotion::getStartTime, startTime)
                    .set(Promotion::getEndTime, endTime)
                    .set(Promotion::getPromotionName, productPromotion.getPromotionName())
                    .set(Promotion::getIsAvailable, 1)
                    .set(Promotion::getRange, productPromotion.getRange())
                    .set(Promotion::getRangeData, productPromotion.getRangeData())
                    .set(Promotion::getMemberRange, memberRange)
                    .set(Promotion::getMemberMarks, memberMarksStr)
                    .set(Promotion::getMemberScope, memberScope));
        } else {
            Promotion promotion = new Promotion();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            promotion.setPromotionName(productPromotion.getPromotionName());
            promotion.setStartTime(startTime);
            promotion.setEndTime(endTime);
            promotion.setType(type);
            promotion.setShopId(shopId);
            promotion.setRelationId(productPromotion.getPromotionId());
            promotion.setRange(productPromotion.getRange());
            promotion.setRangeData(productPromotion.getRangeData());
            promotion.setMemberRange(memberRange);
            promotion.setMemberMarks(memberMarksStr);
            promotion.setMemberScope(memberScope);

            boolean isSuccess = promotionService.save(promotion);
            if (!isSuccess) {
                throw new GlobalException(SERVICE_ERROR);
            }
        }
    }

//    @Override
//    public ListResVO<ProductPromotionVO, ProductPromotionListDTO> conflict(ProductPromotionDTO dto) {
//        List<ProductPromotion> conflictList = getConflictList(dto);
//        Integer shopId = getShopId();
//        shopId = shopId != null ? shopId : 0;
//        // 获取店铺下商品的总量
//        Long productCount = productService.count(new LambdaQueryWrapper<Product>()
//                .eq(Product::getProductStatus, 1)
//                .eq(Product::getIsDelete, 0)
//                .eq(Product::getShopId, shopId)
//                .select(Product::getProductId));
//
//        List<Product> rangeProduct;
//        if (!conflictList.isEmpty()) {
//            if (dto.getRange() == PromotionRange.PROMOTION_RANGE_PRODUCT.getCode()) {
//                // 获取范围内的商品数据
//                 rangeProduct = productService.list(new LambdaQueryWrapper<Product>()
//                        .in(Product::getProductId, dto.getRangeData())
//                        .eq(Product::getProductStatus, 1)
//                        .eq(Product::getIsDelete, 0)
//                        .eq(Product::getShopId, shopId));
//
//            }
//        }
//       // @ 待完善
//    }

    /**
     * 获取商品活动冲突列表
     *
     * @param dto dto
     * @return List<ProductPromotion>
     */
    public List<ProductPromotion> getConflictList(ProductPromotionDTO dto) {
        // ProductPromotion 表查询数据
        LambdaQueryWrapper<ProductPromotion> productPromotionQueryWrapper = new LambdaQueryWrapper<>();

        // 活动时间检索：标准区间重叠判断
        // existing.start_time <= new.end_time AND existing.end_time >= new.start_time
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)) {
            Long startTimeLong = StringUtils.dateToTimestampExample(startTime);
            Long endTimeLong = StringUtils.dateToTimestampExample(endTime);
            productPromotionQueryWrapper
                    .le(ProductPromotion::getStartTime, endTimeLong)
                    .ge(ProductPromotion::getEndTime, startTimeLong);
        }

        // 活动范围数据
        Integer range = dto.getRange();
        List<Integer> rangeData = dto.getRangeData();
        if (range != null && range > 0 && !rangeData.isEmpty()) {
            // 指定商品
            if (range == PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode()) {
                productPromotionQueryWrapper.and(w -> {
                    for (Integer productId : rangeData) {
                        w.or().apply("JSON_CONTAINS(range_data, {0})", productId.toString());
                    }
                });
            }
        }

        Integer shopId = getShopId();
        if (shopId != null ) {
            productPromotionQueryWrapper.eq(ProductPromotion::getShopId, shopId);
        }

        // 优惠类型区分
        Integer promotionType = dto.getPromotionType();
        if (promotionType != null && promotionType > 0) {
            productPromotionQueryWrapper.eq(ProductPromotion::getPromotionType, promotionType);
        }
        productPromotionQueryWrapper.eq(ProductPromotion::getIsAvailable, 1);

        // 排除自身
        Integer promotionId = dto.getPromotionId();
        promotionId = promotionId != null ? promotionId : 0;
        productPromotionQueryWrapper.ne(ProductPromotion::getPromotionId, promotionId);
        // 未结束的活动
        // Long now = StringUtils.getCurrentTime();
        // productPromotionQueryWrapper.ge(ProductPromotion::getEndTime, now);

        return this.list(productPromotionQueryWrapper);

    }

    /**
     * 获取类型
     *
     * @param type 活动类型
     * @return Integer
     */
    public Integer gainType(Integer type) {
        int gainType;
        switch (type) {
            case 1 -> gainType = PromotionType.TYPE_REDUCE.getCode();
            case 2 -> gainType = PromotionType.TYPE_DISCOUNT.getCode();
            case 3 -> gainType = PromotionType.TYPE_GIVE.getCode();
            default -> throw new GlobalException(PRODUCT_PROMOTION_TYPE_ERROR);
        }
        return gainType;
    }

    /**
     * 活动数据校验
     *
     * @param dto              dto
     * @param productPromotion productPromotion
     */
    public void dataJudge(ProductPromotionDTO dto, ProductPromotion productPromotion) {
        // 订单金额下限取 promotion_type_data 二维数组内 minAmount 的最小值
        BigDecimal minOrderAmount;
        List<ProductPromotionDTO.PromotionTypeData> promotionTypeData = dto.getPromotionTypeData();

        if (promotionTypeData != null && !promotionTypeData.isEmpty()) {
            Optional<BigDecimal> minAmountOptional = promotionTypeData.stream()
                    .map(ProductPromotionDTO.PromotionTypeData::getMinAmount)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo);

            minOrderAmount = minAmountOptional.orElse(BigDecimal.ZERO);
        } else {
            minOrderAmount = BigDecimal.ZERO;
        }
        productPromotion.setMinOrderAmount(minOrderAmount);

        // 时间转换
        Long startTime = dto.getStartTime() != null ? StringUtils.dateToTimestampExample(dto.getStartTime()) : 0;
        productPromotion.setStartTime(startTime);
        Long endTime = dto.getEndTime() != null ? StringUtils.dateToTimestampExample(dto.getEndTime()) : 0;
        productPromotion.setEndTime(endTime);

        // json 转换
        productPromotion.setLimitUserRank(dto.getLimitUserRank() != null ? dto.getLimitUserRank().toString() : "");
        productPromotion.setPromotionTypeData(dto.getPromotionTypeData() != null ? JSON.toJSONString(dto.getPromotionTypeData()) : "");
        productPromotion.setRangeData(dto.getRangeData() != null ? dto.getRangeData().toString() : "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ProductPromotionDTO updateDTO) {
        if (updateDTO != null) {
            // 判断是否有商品冲突
            List<ProductPromotion> conflictList = getConflictList(updateDTO);
            if (conflictList != null && !conflictList.isEmpty()) {
                throw new GlobalException(EXIST_CONFLICT_PROMOTION, EXIST_CONFLICT_PROMOTION_ERROR);
            }
            ProductPromotion productPromotion = new ProductPromotion();
            BeanUtils.copyProperties(updateDTO, productPromotion);
            dataJudge(updateDTO, productPromotion);
            boolean isSave = this.updateById(productPromotion);
            if (isSave) {
                savePromotion(productPromotion, updateDTO.getMemberRange(), updateDTO.getMemberMarks(),
                        memberAudienceService.resolveWriteScope(updateDTO.getSendUserTarget(), updateDTO.getMemberAudience()));
            }
            return isSave;
        }
        return false;
    }

/**
 * 更新商品促销字段信息
 * 该方法使用事务注解，确保在发生异常时能够回滚
 * @param updateField 包含要更新的字段和值的DTO对象
 * @param productPromotionFields 需要更新的字段数组
 * @return 更新成功返回true
 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateField(UpdateFieldDTO updateField, String[] productPromotionFields) {
    // 根据ID获取商品促销信息
        ProductPromotion productPromotion = this.getById(updateField.getId());
    // 断言商品促销信息存在，不存在则抛出异常
        Assert.notNull(productPromotion, "商品促销不存在");
    // 获取当前时间
        Long currentTime = StringUtils.getCurrentTime();
        // 禁用活动与调整排序不会新增冲突，若一并校验会把「消除冲突」的操作也挡住，导致冲突活动无法关闭
        boolean isEnabling = "isAvailable".equals(updateField.getField()) && "1".equals(updateField.getVal());
        // 只校验进行中的活动
        if (isEnabling && currentTime >= productPromotion.getStartTime() && currentTime <= productPromotion.getEndTime()) {
        // 初始化范围数据列表为空列表
            List<Integer> rangeData = Collections.emptyList();
        // 如果促销范围数据不为空，则解析JSON数据
            if (StrUtil.isNotBlank(productPromotion.getRangeData())) {
                rangeData = JSON.parseArray(productPromotion.getRangeData(), Integer.class);
            }

        // 创建促销DTO对象用于冲突检查
            ProductPromotionDTO updateDTO = new ProductPromotionDTO();
            updateDTO.setPromotionId(productPromotion.getPromotionId());
            updateDTO.setStartTime(TigUtils.handelTime(productPromotion.getStartTime()));
            updateDTO.setEndTime(TigUtils.handelTime(productPromotion.getEndTime()));
            updateDTO.setRange(productPromotion.getRange());
            updateDTO.setRangeData(rangeData);
            updateDTO.setPromotionType(productPromotion.getPromotionType());

            // 判断是否有商品冲突
            List<ProductPromotion> conflictList = getConflictList(updateDTO);
            if (CollUtil.isNotEmpty(conflictList)) {
                throw new GlobalException(EXIST_CONFLICT_PROMOTION, EXIST_CONFLICT_PROMOTION_ERROR);
            }
        }
        super.updateField(updateField, productPromotionFields);

        // 同步 promotion
        if ("isAvailable".equals(updateField.getField())) {
            promotionService.lambdaUpdate()
                    .set(Promotion::getIsAvailable, updateField.getVal())
                    .eq(Promotion::getRelationId, productPromotion.getPromotionId())
                    .update();
        }


        return true;
    }

    @Override
    public Map<Integer, String> getConfig() {
        //PromotionTimeStatus 转成map
        return Arrays.stream(PromotionTimeStatusEnum.values())
                .collect(Collectors.toMap(PromotionTimeStatusEnum::getCode, PromotionTimeStatusEnum::getDescription));
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO) {
        CartPromotionParsePriceDTO cartPromotionParsePriceDTO = new CartPromotionParsePriceDTO();
        ProductPromotionVO productPromotion = this.detail(promotionVO.getRelationId());

        if (productPromotion == null) {
            return null;
        }

        // 计算比较是按元/件 满减
        BigDecimal compareData = productPromotion.getUnit() == 1
                // 按金额
                ? cartList.stream().map(cartVO -> cartVO.getPrice().multiply(BigDecimal.valueOf(cartVO.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add)
                // 按件
                : BigDecimal.valueOf(cartList.stream().map(CartVO::getQuantity).reduce(0, Integer::sum));
        if (productPromotion.getMinOrderAmount().compareTo(compareData) <= 0) {
            //满足要求则计算总的优惠并分摊到每个商品上
            ProductPromotionVO.PromotionTypeData currentPromotion = null;
            cartPromotionParsePriceDTO.setPromotion(promotionVO);
            for (ProductPromotionVO.PromotionTypeData data : productPromotion.getPromotionTypeData()) {
                BigDecimal minAmount = data.getMinAmount();
                if (isMoreApplicablePromotionRule(currentPromotion, minAmount, compareData)) {
                    currentPromotion = data;
                }
            }
            if (currentPromotion == null) {
                return null;
            }
            BigDecimal priceAmount = BigDecimal.ZERO;
            for (CartVO cartVO : cartList) {
                priceAmount = priceAmount.add(cartVO.getPrice().multiply(new BigDecimal(cartVO.getQuantity())));
            }
            //如果是满减满折则计算单价
            if (productPromotion.getPromotionType() == 1 || productPromotion.getPromotionType() == 2) {
                //最大优惠比例100，也就是0折
                BigDecimal discountProportion = BigDecimal.ZERO;
                if (productPromotion.getPromotionType() == 1) {
                    BigDecimal totalDiscount = BigDecimal.ZERO;
                    if (productPromotion.getRulesType() == 1) {
                        totalDiscount = currentPromotion.getReduce();
                    } else {
                        BigDecimal num = compareData.divide(currentPromotion.getMinAmount(), 0, RoundingMode.DOWN);
                        totalDiscount = currentPromotion.getReduce().multiply(num).setScale(2, RoundingMode.HALF_UP);
                    }

                    // 按比例看总优惠金额是多少
                    if (totalDiscount.compareTo(priceAmount) >= 0) {
                        promotionVO.setAmount(priceAmount);
                    } else {
                        promotionVO.setAmount(totalDiscount);
                        discountProportion = priceAmount.subtract(totalDiscount).divide(priceAmount, 6, RoundingMode.HALF_UP);
                    }
                } else {
                    // 满折 -- 满折没有循环优惠
                    discountProportion = currentPromotion.getReduce().divide(new BigDecimal(10), 6, RoundingMode.HALF_UP);
                    BigDecimal totalDiscount = priceAmount.multiply(BigDecimal.ONE.subtract(discountProportion));
                    if (totalDiscount.compareTo(priceAmount) > 0) {
                        totalDiscount = priceAmount;
                    }
                    promotionVO.setAmount(totalDiscount);
                }
                for (CartVO cartVO : cartList) {
                    cartVO.setPrice(cartVO.getPrice().multiply(discountProportion).setScale(2, RoundingMode.HALF_UP));
                }
            } else {
                promotionVO.setAmount(BigDecimal.ZERO);
                //如果是满赠返回满赠的信息
                ProductGift productGift = productGiftService.getById(currentPromotion.getGiftId());
                boolean hasConfiguredGiftSku = productGift != null && productGift.getSkuId() != null && productGift.getSkuId() > 0;
                ProductSku configuredGiftSku = hasConfiguredGiftSku
                        ? productSkuMapper.selectById(productGift.getSkuId()) : null;
                boolean giftSkuEnabled = !hasConfiguredGiftSku || (configuredGiftSku != null
                        && !Objects.equals(configuredGiftSku.getIsEnabled(), Constants.NO));
                if (productGift != null && productGift.getGiftStock() > 0 && giftSkuEnabled) {
                    CartPromotionParsePriceDTO.Gift gift = new CartPromotionParsePriceDTO.Gift();
                    gift.setGiftId(productGift.getGiftId());
                    gift.setProductId(productGift.getProductId());
                    gift.setSkuId(productGift.getSkuId());
                    Product product = productMapper.selectById(productGift.getProductId());
                    if (product != null) {
                        gift.setProductName(product.getProductName());
                        gift.setPicThumb(product.getPicThumb());
                        gift.setProductSn(product.getProductSn());
                        gift.setProductType(product.getProductType());
                        gift.setShopId(product.getShopId());
                        gift.setType(4);
                        ProductSku productSku = configuredGiftSku;
                        if (productSku != null) {
                            gift.setSkuData(JSONUtil.parseArray(productSku.getSkuData()));
                        } else {
                            gift.setSkuData(new JSONArray());
                        }
                        gift.setRulesType(productPromotion.getRulesType());
                        gift.setNum(0);
                        // 获取参加满赠的商品付款金额
                        BigDecimal payAmount = cartList.stream()
                                .map(CartVO::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        // currentPromotion 已按比较金额选出最高可用规则；赠品数量必须基于该规则计算。
                        // 旧逻辑在循环计价时遍历全部规则，后续未达门槛的规则会把已计算数量覆盖为 0，
                        // 导致配置多个阶梯后购物车达到门槛却不返回赠品。
                        BigDecimal giftMinAmount = currentPromotion.getMinAmount();
                        Integer giftRuleNum = Optional.ofNullable(currentPromotion.getNum()).orElse(0);
                        gift.setNum(calculateGiftQuantity(productPromotion.getRulesType(), payAmount,
                                giftMinAmount, giftRuleNum));
                        if (gift.getNum() != 0) {
                            Integer giftStock = Optional.ofNullable(productGift.getGiftStock()).orElse(0);
                            if (gift.getNum() > giftStock) {
                                gift.setNum(giftStock);
                            }
                            cartPromotionParsePriceDTO.setGift(gift);
                        }
                    }
                }
            }
            cartPromotionParsePriceDTO.setCartList(cartList);
            cartPromotionParsePriceDTO.setPromotion(promotionVO);
        }else{
            return null;
        }
        return cartPromotionParsePriceDTO;
    }

    /**
     * 选择当前金额可用的最高门槛规则，不依赖后台提交规则的顺序。
     */
    static boolean isMoreApplicablePromotionRule(ProductPromotionVO.PromotionTypeData currentPromotion,
                                                  BigDecimal candidateMinAmount,
                                                  BigDecimal compareData) {
        if (candidateMinAmount == null || compareData == null || candidateMinAmount.compareTo(compareData) > 0) {
            return false;
        }
        return currentPromotion == null
                || currentPromotion.getMinAmount() == null
                || candidateMinAmount.compareTo(currentPromotion.getMinAmount()) > 0;
    }

    static int calculateGiftQuantity(Integer rulesType, BigDecimal payAmount,
                                     BigDecimal minAmount, Integer configuredQuantity) {
        if (payAmount == null || minAmount == null || minAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int quantity = Optional.ofNullable(configuredQuantity).orElse(0);
        if (Objects.equals(rulesType, 1)) {
            return quantity;
        }
        return payAmount.divide(minAmount, 0, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(quantity)).intValue();
    }

    @Override
    public Page<ConflictPageVO> conflictPage(ConflictPageQuery query) {
        Page<ProductPromotion> page = buildSortOrder(query);

        boolean promotionGroupFlag = ProductPromotionTypeEnum.PROMOTION_GROUP.contains(query.getPromotionType());
        Integer shopId = HeaderUtils.getShopId();

        Long startTime = StringUtils.dateToTimestampExample(query.getStartTime());
        Long endTime = StringUtils.dateToTimestampExample(query.getEndTime());

        Page<ProductPromotion> productPromotionPage = this.lambdaQuery()
                .eq(ProductPromotion::getIsAvailable, Constants.YES)
                .eq(ProductPromotion::getShopId, shopId)
                .and(query.getStartTime() != null && query.getEndTime() != null,
                        wrapper -> wrapper.le(ProductPromotion::getStartTime, startTime).ge(ProductPromotion::getEndTime, endTime)
                )
                .in(promotionGroupFlag, ProductPromotion::getPromotionType, query.getPromotionType())
                .eq(!promotionGroupFlag, ProductPromotion::getPromotionType, query.getPromotionType())
                .and(PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode() == query.getRange(), w -> {
                    for (String val : StrUtil.split(query.getRangeData(), StrPool.COMMA)) {
                        w.or().apply("JSON_CONTAINS(range_data, {0}, '$')", val);
                    }
                })
                // .ge(ProductPromotion::getEndTime, StringUtils.getCurrentTime())
                .page(page);

        Long totalProductCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getProductStatus, Constants.YES)
                        .eq(Product::getIsDelete, Constants.NO)
                        .eq(Product::getShopId, shopId)
        );

        List<ProductPromotion> productPromotionPageRecords = productPromotionPage.getRecords();
        List<Product> rangeProducts;
        if (CollUtil.isNotEmpty(productPromotionPageRecords)) {
            // 获取指定商品信息
            if (PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode() == query.getRange()) {
                rangeProducts = productMapper.selectList(
                        new LambdaQueryWrapper<Product>()
                                .eq(Product::getProductStatus, Constants.YES)
                                .eq(Product::getIsDelete, Constants.NO)
                                .eq(Product::getShopId, shopId)
                                .in(Product::getProductId, query.getRangeData())
                );

                Map<Integer, List<ProductPromotion>> promotionDataByProductId = new HashMap<>();
                for (ProductPromotion productPromotion : productPromotionPageRecords) {
                    List<Integer> productIds = StringUtils.str2IntList(productPromotion.getRangeData());
                    for (Integer productId : productIds) {
                        promotionDataByProductId.computeIfAbsent(productId, k -> new ArrayList<>()).add(productPromotion);
                    }
                }

                for (Product product : rangeProducts) {
                    Integer productId = product.getProductId();
                    if (!promotionDataByProductId.containsKey(productId)) {
                        rangeProducts.remove(product);
                    }
                }
            }

            for (ProductPromotion productPromotion : productPromotionPageRecords) {
                if (PromotionRangeEnum.PROMOTION_RANGE_EXCLUDE_PRODUCT.getCode() == productPromotion.getRange()) {
                    // 不含商品 -- 传参的元素与表内元素去重合并之后的数量 != 商品总量时候，才冲突
                    List<Integer> itemRangeData = StringUtils.str2IntList(productPromotion.getRangeData());

                    Set<Integer> merged = new HashSet<>(itemRangeData);
                    List<Integer> list = StrUtil.split(productPromotion.getRangeData(), StrPool.COMMA).stream().map(Integer::parseInt).toList();
                    merged.addAll(list);
                    if (merged.size() == totalProductCount) {
                        productPromotionPageRecords.remove(productPromotion);
                    }
                }
            }
        }

        List<ConflictPageVO> resultRecords = productPromotionPageRecords.stream().map(ConflictPageVO::new).toList();

        return PageUtil.convertPage(productPromotionPage, resultRecords);
    }

    @Override
    @Transactional
    public boolean del(Integer id) {
        if (id == null) {
            throw new GlobalException("id错误");
        }
        ProductPromotion productPromotion = getById(id);
        if (productPromotion == null) {
            throw new GlobalException("优惠活动不存在");
        }
        if (productPromotion.getIsAvailable() == 1) {
            throw new GlobalException("请先停用该活动，再删除");
        }
        //删除 ProductPromotion
        boolean flag = removeById(id);
        int type = -1;

        if (productPromotion.getPromotionType() == PROMOTION_TYPE_FULL_REDUCE.getCode()) {
            type = PromotionType.TYPE_REDUCE.getCode();
        } else if (productPromotion.getPromotionType() == PROMOTION_TYPE_FULL_DISCOUNT.getCode()) {
            type = PromotionType.TYPE_DISCOUNT.getCode();
        } else if (productPromotion.getPromotionType() == PROMOTION_TYPE_FULL_REDUCE_NAME.getCode()) {
            type = PromotionType.TYPE_GIVE.getCode();
        }

        //删除 Promotion
        boolean remove = promotionService.remove(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getType, type)
                .eq(Promotion::getRelationId, id)
        );
        return flag && remove;
    }
}
