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

package com.tigshop.service.shop.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.merchant.MerchantAdminDataDTO;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.enums.merchant.MerchantType;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.enums.shop.StoreType;
import com.tigshop.bean.enums.vendor.VendorIsAdminEnum;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.o2o.AreaStoreManagerShop;
import com.tigshop.bean.model.o2o.ShopHomeServiceConfig;
import com.tigshop.bean.model.o2o.ShopLocalDeliveryConfig;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreTip;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.CollectShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.login.LoginChooseAuth;
import com.tigshop.bean.param.login.LoginChooseParam;
import com.tigshop.bean.param.vendor.ShopVendorSettingUpdateParam;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.query.shop.StoreDetailQuery;
import com.tigshop.bean.query.shop.StorePageQuery;
import com.tigshop.bean.vo.config.GetStoreSelfOperatedVO;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.o2o.StoreDetailVO;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.bean.vo.o2o.TipListVO;
import com.tigshop.bean.vo.setting.config.ShopSettingsVO;
import com.tigshop.bean.vo.shop.*;
import com.tigshop.bean.vo.vendor.ShopVendorSettingVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.mapper.o2o.ShopHomeServiceConfigMapper;
import com.tigshop.mapper.o2o.ShopLocalDeliveryConfigMapper;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.o2o.StoreTipMapper;
import com.tigshop.mapper.o2o.TipMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.mapper.shop.CollectShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.CommentService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.shop.AdminUserShopService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.shop.ShopEmployeeWorkbenchCandidatePolicy;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.enums.setting.SettingsEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.shop.ShopConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class ShopServiceImpl extends BaseServiceImpl<ShopMapper, Shop> implements ShopService {
    @Resource
    MerchantService merchantService;
    @Resource
    MerchantUserService merchantUserService;
    @Resource
    ConfigService configService;
    @Resource
    SubMerchantService subMerchantService;
    @Resource
    AdminUserShopService adminUserShopService;
    @Resource
    ProductService productService;
    @Resource
    ShippingTplService shippingTplService;
    @Resource
    DecorateService decorateService;
    @Resource
    AdminRoleService adminRoleService;
    @Resource
    ShopEmployeeWorkbenchCandidatePolicy workbenchCandidatePolicy;
    @Resource
    CollectShopMapper collectShopMapper;
    @Resource
    MessageHelper messageHelper;
    @Resource
    private AdminLogService adminLogService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private RedisTemplate<String, LoginChooseAuth> redisTemplate;
    @Resource
    private TigshopProperties tigshopProperties;
    @Resource
    private RegionService regionService;
    @Resource
    private PromotionMapper promotionMapper;

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private StoreTipMapper storeTipMapper;
    @Resource
    private TipMapper tipMapper;
    @Resource
    private ShopLocalDeliveryConfigMapper shopLocalDeliveryConfigMapper;
    @Resource
    private ShopHomeServiceConfigMapper shopHomeServiceConfigMapper;
    @Autowired
    private StoreProductMapper storeProductMapper;
    @Autowired
    private CommentService commentService;
    @Resource
    private OrderService orderService;
    @Resource
    private AftersalesService aftersalesService;
    @Resource
    private OrderItemMapper orderItemMapper;

    /**
     * "进行中订单"口径：待支付(0)、待发货(1)、已发货/待收货(2)；已取消(3)/无效(4)/已完成(5) 为终态。
     * 与保证金全额退还/提现的判定口径一致（DepositRefundServiceImpl.ONGOING_ORDER_STATUS）。
     */
    private static final List<Integer> ONGOING_ORDER_STATUS = List.of(
            OrderStatusEnum.PENDING.getCode(),
            OrderStatusEnum.CONFIRMED.getCode(),
            OrderStatusEnum.PROCESSING.getCode());

    @Override
    public Page<ShopVO> list(ShopListDTO listDTO) {
        // 分页
        Page<Shop> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        MPJLambdaWrapper<Shop> queryWrapper = new MPJLambdaWrapper<>();
        // 过滤已删除的店铺（is_delete 为 null 或 0 表示未删除）
        queryWrapper.and(w -> w.isNull(Shop::getIsDelete).or().eq(Shop::getIsDelete, 0));
        if (listDTO.getIsMyShop() != 1) {
            queryWrapper.eq(Shop::getShopType, listDTO.getShopType());
        } else if (CollUtil.isNotEmpty(listDTO.getShopTypes())) {
            queryWrapper.in(Shop::getShopType, listDTO.getShopTypes());
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        // 状态筛选
        Integer status = listDTO.getStatus();
        if (status != null && status > 0) {
            queryWrapper.eq(Shop::getStatus, status);
        }

        if (StringUtils.isNotEmpty(listDTO.getAddStartTime())) {
            queryWrapper.ge(Shop::getAddTime, StringUtils.dateToTimestampExample(listDTO.getAddStartTime() + " 00:00:00"));
        }
        if (StringUtils.isNotEmpty(listDTO.getAddEndTime())) {
            queryWrapper.le(Shop::getAddTime, StringUtils.dateToTimestampExample(listDTO.getAddEndTime() + " 23:59:59"));
        }
        if (ObjectUtil.isNotNull(listDTO.getStoreParentId())) {
            queryWrapper.eq(Shop::getStoreParentId, listDTO.getStoreParentId());
        }

        if (ObjectUtil.isNotEmpty(listDTO.getMainAccount())) {
            List<Integer> shopIds = adminUserService.getBindMainShopIds(listDTO.getMainAccount(), true);
            if (ObjectUtil.isEmpty(shopIds)) {
                return new Page<>(0, listDTO.getSize());
            } else {
                queryWrapper.in(Shop::getShopId, shopIds);
            }
        }

        if (ObjectUtil.isNotEmpty(listDTO.getAccount())) {
            List<Integer> shopIds = adminUserService.getBindMainShopIds(listDTO.getAccount(), false);
            if (ObjectUtil.isEmpty(shopIds)) {
                return new Page<>(0, listDTO.getSize());
            } else {
                queryWrapper.in(Shop::getShopId, shopIds);
            }
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(Shop::getShopTitle, keyword);
        }

        if (listDTO.getShopIds() != null && !listDTO.getShopIds().isEmpty()) {
            queryWrapper.in(Shop::getShopId, listDTO.getShopIds());
        } else {
            Integer shopId = getShopId();
            if (shopId != null && shopId > 0) {
                queryWrapper.eq(Shop::getShopId, shopId);
            }
        }
        Integer areaStoreManagerId = listDTO.getAreaStoreManagerId();
        if (areaStoreManagerId != null) {
            queryWrapper.leftJoin(AreaStoreManagerShop.class, AreaStoreManagerShop::getShopId, Shop::getShopId).eq(AreaStoreManagerShop::getAreaStoreManagerId, areaStoreManagerId);
        }
        if (listDTO.getShopRegionId() != null) {
            queryWrapper.like(Shop::getShopRegionId, listDTO.getShopRegionId());
        }
        // 关联Merchant表按公司名称搜索
        if (StringUtils.isNotEmpty(listDTO.getCorporateName())) {
            queryWrapper.leftJoin(Merchant.class, Merchant::getMerchantId, Shop::getMerchantId)
                    .and(w -> w.isNull(Merchant::getIsDelete).or().eq(Merchant::getIsDelete, 0))
                    .like(Merchant::getCorporateName, listDTO.getCorporateName());
        }
        boolean distributionSelf = false;
        // 执行查询
        Page<Shop> shopPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Shop> shopPageRecords = shopPage.getRecords();

        // ===== N+1 批量预取：按当前页 shopIds 一次性载入，替换循环内逐行查询 =====
        List<Integer> shopIdList = shopPageRecords.stream().map(Shop::getShopId).toList();
        // 店铺管理员：一次载入本页店铺的管理员绑定 + 管理员账号
        Map<Integer, AdminUserShop> shopAdminBindMap = shopIdList.isEmpty() ? Collections.emptyMap()
                : adminUserShopService.list(new LambdaQueryWrapper<AdminUserShop>()
                        .in(AdminUserShop::getShopId, shopIdList)
                        .eq(AdminUserShop::getIsAdmin, VendorIsAdminEnum.YES.getCode()))
                .stream().collect(Collectors.toMap(AdminUserShop::getShopId, Function.identity(), (a, b) -> a));
        List<Integer> shopAdminIds = shopAdminBindMap.values().stream()
                .map(AdminUserShop::getAdminId).filter(Objects::nonNull).distinct().toList();
        Map<Integer, AdminUser> shopAdminUserMap = shopAdminIds.isEmpty() ? Collections.emptyMap()
                : adminUserService.listByIds(shopAdminIds).stream()
                .collect(Collectors.toMap(AdminUser::getAdminId, Function.identity(), (a, b) -> a));

        // 转换为VO
        List<ShopVO> shopVOList = new ArrayList<>(shopPageRecords.stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            shopVO.setShopStar(shop.getShopStar());
            long timestampInSeconds = shop.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            shopVO.setAddTime(sdf.format(date));
            //查询出商户的数据
            MerchantVO merchantVO = merchantService.getMerchantVO(shop.getMerchantId());
            AdminUserShop shopAdminBind = shopAdminBindMap.get(shop.getShopId());
            if (shopAdminBind != null) {
                AdminUser adminUser = shopAdminUserMap.get(shopAdminBind.getAdminId());
                if (adminUser != null) {
                    MerchantAdminDataDTO admin = new MerchantAdminDataDTO();
                    admin.setAdminId(adminUser.getAdminId());
                    admin.setUsername(adminUser.getUsername());
                    merchantVO.setAdmin(admin);
                }
            }
            shopVO.setMerchant(merchantVO);
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            // 我的店铺：回填收货信息（配送设置 → 收货信息），供售后退货地址预填使用
            if (listDTO.getIsMyShop() != null && listDTO.getIsMyShop() == 1) {
                ShopVO.ShopDeliveryInfo receiveInfo = parseReceiveInfo(shop);
                if (receiveInfo != null) {
                    shopVO.setReceiveInfo(receiveInfo);
                }
            }
            return shopVO;
        }).toList());
        if ((tigshopProperties.getIsO2o() == 1 && listDTO.getSelect() == 1) || (distributionSelf)) {
            if (ObjectUtil.isEmpty(listDTO.getKeyword()) || "自营店铺".contains(listDTO.getKeyword())) {
                ShopVO shopVO = new ShopVO();
                shopVO.setShopId(0);
                shopVO.setShopTitle("自营店铺");
                shopVOList.addFirst(shopVO);
            }
        }
        appendSelfOperatedStore(listDTO, shopPage, shopVOList);
        return PageUtil.convertPage(shopPage, shopVOList);
    }

    /**
     * 平台自营主体（shopId=0）不是 shop 表记录，而是由平台配置生成的虚拟店铺/门店。
     * 当调用方显式传 includeSelfOperated=1 时，按请求的 shopType 在第 1 页首位插入，并计入 total；
     * 除名称关键字外的其它筛选条件不作用于它。店铺级管理员（header shopId>0）不返回。
     */
    private void appendSelfOperatedStore(ShopListDTO listDTO, Page<Shop> shopPage, List<ShopVO> shopVOList) {
        if (!ObjectUtil.equals(listDTO.getIncludeSelfOperated(), 1)
                || (!ObjectUtil.equals(listDTO.getShopType(), ShopTypeEnum.SHOP.getCode())
                && !ObjectUtil.equals(listDTO.getShopType(), ShopTypeEnum.STORE.getCode()))
                || listDTO.getPage() != 1) {
            return;
        }
        Integer currentShopId = getShopId();
        if (currentShopId != null && currentShopId > 0) {
            return;
        }
        if (shopVOList.stream().anyMatch(vo -> ObjectUtil.equals(vo.getShopId(), 0))) {
            return;
        }
        String shopTitle;
        GetStoreSelfOperatedVO storeSettings = null;
        if (ObjectUtil.equals(listDTO.getShopType(), ShopTypeEnum.SHOP.getCode())) {
            ShopSettingsVO shopSettings = configService.getShopSettings();
            if (shopSettings == null) {
                return;
            }
            shopTitle = shopSettings.getDefaultShopName();
        } else {
            storeSettings = configService.getStoreSelfOperatedSettings();
            if (storeSettings == null) {
                return;
            }
            shopTitle = storeSettings.getStoreSelfOperatedName();
        }
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword) && (StringUtils.isEmpty(shopTitle) || !shopTitle.contains(keyword))) {
            return;
        }
        ShopVO selfOperated = new ShopVO();
        selfOperated.setShopId(0);
        selfOperated.setShopTitle(shopTitle);
        selfOperated.setShopType(listDTO.getShopType());
        if (storeSettings != null) {
            selfOperated.setShopLogo(resolveSelfOperatedStoreLogo(storeSettings));
            selfOperated.setShopDetailedAddress(storeSettings.getStoreSelfOperatedAddress());
            selfOperated.setContactMobile(storeSettings.getStoreSelfOperatedPhone());
            selfOperated.setShopRegionNames(storeSettings.getStoreSelfOperatedRegions());
            selfOperated.setShopLongitude(parseCoordinate(storeSettings.getStoreSelfOperatedLongitude()));
            selfOperated.setShopLatitude(parseCoordinate(storeSettings.getStoreSelfOperatedLatitude()));
            selfOperated.setDescription(storeSettings.getStoreSelfOperatedDescription());
            selfOperated.setStatus(storeSettings.getStoreSelfOperatedStatus());
            selfOperated.setShopContactConfig(storeSettings.getShopContactConfig());
            selfOperated.setShopOpenCloseConfig(storeSettings.getShopOpenCloseConfig());
            fillSelfOperatedShopTips(selfOperated, storeSettings);
        } else {
            // 店铺展示复用全站店铺 logo；店铺主体没有门店专属地址/营业时间等字段。
            selfOperated.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
        }
        shopVOList.addFirst(selfOperated);
        shopPage.setTotal(shopPage.getTotal() + 1);
    }

    /**
     * 平台自营门店（shopId=0）营业状态为「暂停运营」时，C 端不展示。
     * 与真实门店口径一致（storeListByLocation / storeListByRegion 过滤 status IN (1, 10)）。
     */
    private boolean isSelfOperatedStoreSuspended(GetStoreSelfOperatedVO settings) {
        return settings != null && ObjectUtil.equals(settings.getStoreSelfOperatedStatus(), StoreType.PAUSE.getCode());
    }

    /**
     * C 端门店列表是否应插入平台自营占位。
     * <ul>
     *   <li>配送过滤开启时不插（自营无门店级配送配置）</li>
     *   <li>显式 {@code shopIds} 含 0：指定优先，忽略 excludeSelfOperated</li>
     *   <li>显式 {@code shopIds} 非空且不含 0：不插</li>
     *   <li>否则（无 shopIds）：仅显式 {@code excludeSelfOperated=0} 时插入，默认排除</li>
     * </ul>
     */
    private boolean shouldInsertSelfOperatedStore(StorePageQuery query, boolean onlyLocalDelivery) {
        if (onlyLocalDelivery) {
            return false;
        }
        List<Integer> shopIds = query.getShopIds();
        if (CollUtil.isNotEmpty(shopIds) && shopIds.contains(0)) {
            return true;
        }
        if (CollUtil.isNotEmpty(shopIds)) {
            return false;
        }
        // 默认排除；前端传 0 才纳入自营
        return ObjectUtil.equals(query.getExcludeSelfOperated(), 0);
    }

    /**
     * 门店语义下的自营 logo：优先「平台门店设置」的门店 logo，未配置时回退全站 logo。
     * 店铺语义（自营店铺占位）仍直接使用全站 logo，不走本方法。
     */
    private String resolveSelfOperatedStoreLogo(GetStoreSelfOperatedVO settings) {
        String storeLogo = settings == null ? null : settings.getStoreSelfOperatedLogo();
        return StringUtils.isNotEmpty(storeLogo) ? storeLogo : configService.getConfigVal(SettingsEnum.SHOP_LOGO);
    }

    /** 管理端 ShopVO：写入 shopTips（ID）与 tips（ID+名称），口径对齐入驻门店 detail。 */
    private void fillSelfOperatedShopTips(ShopVO shopVO, GetStoreSelfOperatedVO settings) {
        if (shopVO == null || settings == null) {
            return;
        }
        List<Integer> tipIds = settings.getShopTips();
        shopVO.setShopTips(tipIds);
        if (CollUtil.isEmpty(tipIds)) {
            return;
        }
        List<TipListVO> tips = new ArrayList<>();
        for (Integer tipId : tipIds) {
            var tip = tipMapper.selectById(tipId);
            if (tip == null) {
                continue;
            }
            TipListVO tipListVO = new TipListVO();
            tipListVO.setTipId(tipId);
            tipListVO.setTipName(tip.getTipName());
            tips.add(tipListVO);
        }
        shopVO.setTips(tips);
    }

    /** C 端 tipNames：按 tipId 查名称，已删 tip 过滤。 */
    private List<String> resolveSelfOperatedTipNames(GetStoreSelfOperatedVO settings) {
        if (settings == null || CollUtil.isEmpty(settings.getShopTips())) {
            return Collections.emptyList();
        }
        return settings.getShopTips().stream()
                .map(tipMapper::selectById)
                .filter(Objects::nonNull)
                .map(tip -> tip.getTipName())
                .toList();
    }

    private void fillSelfOperatedStoreListVO(StoreListVO vo, GetStoreSelfOperatedVO settings) {
        if (settings == null) {
            return;
        }
        vo.setStatus(settings.getStoreSelfOperatedStatus());
        vo.setDescription(settings.getStoreSelfOperatedDescription());
        vo.setContactConfigs(settings.getShopContactConfig());
        vo.setTipNames(resolveSelfOperatedTipNames(settings));
        if (StringUtils.isNotEmpty(settings.getStoreSelfOperatedOpenCloseJson())) {
            vo.setShopOpenCloseConfig(JSONUtil.toBean(
                    settings.getStoreSelfOperatedOpenCloseJson(), StoreListVO.ShopOpenCloseConfig.class));
        }
    }

    private BigDecimal parseCoordinate(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ShopVO detail(Integer id) {
        return detail(id, false);
    }

    @Override
    public ShopVO detail(Integer id, boolean onlyCurrentAdmin) {
        if (id != null) {
            if(id==0){
                ShopVO shopVO = new ShopVO();
                shopVO.setShopId(0);
                if(tigshopProperties.getIsO2o() == 1){
                    GetStoreSelfOperatedVO storeSelfOperatedSettings = configService.getStoreSelfOperatedSettings();
                    if(Objects.nonNull(storeSelfOperatedSettings)){
                        shopVO.setShopType(ShopTypeEnum.STORE.getCode());
                        shopVO.setShopTitle(storeSelfOperatedSettings.getStoreSelfOperatedName());
                        shopVO.setShopLongitude(new BigDecimal(storeSelfOperatedSettings.getStoreSelfOperatedLongitude()));
                        shopVO.setShopLatitude(new BigDecimal(storeSelfOperatedSettings.getStoreSelfOperatedLatitude()));
                        shopVO.setShopDetailedAddress(storeSelfOperatedSettings.getStoreSelfOperatedAddress());
                        shopVO.setShopLogo(resolveSelfOperatedStoreLogo(storeSelfOperatedSettings));
                        shopVO.setShopRegionNames(storeSelfOperatedSettings.getStoreSelfOperatedRegions());
                        shopVO.setContactMobile(storeSelfOperatedSettings.getStoreSelfOperatedPhone());
                        shopVO.setDescription(storeSelfOperatedSettings.getStoreSelfOperatedDescription());
                        shopVO.setStatus(storeSelfOperatedSettings.getStoreSelfOperatedStatus());
                        shopVO.setShopContactConfig(storeSelfOperatedSettings.getShopContactConfig());
                        shopVO.setShopOpenCloseConfig(storeSelfOperatedSettings.getShopOpenCloseConfig());
                        fillSelfOperatedShopTips(shopVO, storeSelfOperatedSettings);
                    }
                }

                if(tigshopProperties.getIsMerchant() == 1){
                    ShopSettingsVO shopSettings = configService.getShopSettings();
                    if(Objects.nonNull(shopSettings)){
                        shopVO.setShopTitle(shopSettings.getDefaultShopName());
                    }
                }
                return shopVO;
            }
            Shop shop = this.getById(id);
            if (shop == null) {
                throw new GlobalException(SHOP_NOT_EXIST);
            }
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            ShopVO.ShopDeliveryInfo shipInfo = parseShipInfo(shop);
            if (shipInfo != null) {
                shopVO.setShipInfo(shipInfo);
            }
            ShopVO.ShopDeliveryInfo receiveInfo = parseReceiveInfo(shop);
            if (receiveInfo != null) {
                shopVO.setReceiveInfo(receiveInfo);
            }
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            long timestampInSeconds = shop.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            shopVO.setAddTime(sdf.format(date));
            MerchantVO merchantVO = null;
            merchantVO = merchantService.getMerchantVO(shop.getMerchantId());
            //获取adminUserShop
            LambdaQueryWrapper<AdminUserShop> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUserShop::getShopId, id);
            if (onlyCurrentAdmin) {
                queryWrapper.eq(AdminUserShop::getAdminId, getCurrentAdminId());
            }
            queryWrapper.last("limit 1");
            AdminUserShop adminUserShop = adminUserShopService.getOne(queryWrapper);
            if (adminUserShop != null) {
                AdminUserShopVO adminUserShopVO = new AdminUserShopVO();
                BeanUtils.copyProperties(adminUserShop, adminUserShopVO);
                adminUserShopVO.setAddTime(TigUtils.handelTime(adminUserShop.getAddTime()));
                if (adminUserShop.getAuthList() != null && !"[]".equals(adminUserShop.getAuthList())) {
                    adminUserShopVO.setAuthList(JsonUtil.fromJson(adminUserShop.getAuthList(), JSONArray.class));
                } else {
                    adminUserShopVO.setAuthList(new JSONArray());
                }
                shopVO.setAdminUserShop(adminUserShopVO);
            }

            //base_data shop_data 需要隐藏
            merchantVO.setBaseData(null);
            merchantVO.setShopData(null);
            shopVO.setMerchant(merchantVO);
            if (ObjectUtil.isEmpty(shop.getKefuInlet())) {
                shopVO.setKefuInlet(new ArrayList<>());
            } else {
                shopVO.setKefuInlet(JSONUtil.parseArray(shop.getKefuInlet()).toList(Integer.class));
            }
            return shopVO;
        }
        return null;
    }

    @Override
    public ShopVO detailWithScore(Integer id) {
        ShopVO shopVO = detail(id);
        if (shopVO == null || id == null || id == 0) {
            return shopVO;
        }
        // 计算后的评星（评价数<100时默认4.8，否则取 shop_star），与前台 clientDetail 保持一致
        Shop shopWithStar = getBaseMapper().selectByIdWithCalculatedStar(id);
        if (shopWithStar != null) {
            shopVO.setShopStar(shopWithStar.getShopStar());
        }
        // 商品评分：AVG(comment_rank) 按 shop_id 分组，与前台 clientDetail 保持一致
        Map<String, Object> result = commentService.getMap(new QueryWrapper<Comment>()
                .select("AVG(comment_rank) as avgScore")
                .eq("shop_id", id)
                .groupBy("shop_id"));
        shopVO.setProductScore(result != null ? new BigDecimal(result.get("avgScore").toString()) : BigDecimal.ZERO);
        return shopVO;
    }

    @Override
    @Transactional
    public boolean create(ShopCreateDTO createDTO) {
        Shop shop = new Shop();
        setStoreParamIfPresent(createDTO, shop);
        shop.setShopStar(new BigDecimal(4.8));

        Integer shopId = getShopId();
        Integer currentAdminId = getCurrentAdminId();
        if (createDTO != null) {

            BeanUtils.copyProperties(createDTO, shop);
            if (shopId > 0) {
                Shop currentShop = this.getById(shopId);
                shop.setMerchantId(currentShop.getMerchantId());
                createDTO.setMerchantId(currentShop.getMerchantId());
            }
            Integer shopType = createDTO.getShopType();
            boolean checkMerchantId = createDTO.getMerchantId() <= 0 && (ShopTypeEnum.SHOP.getCode() == shopType || ShopTypeEnum.STORE.getCode() == shopType);
            if (createDTO.getMerchantId() == null || checkMerchantId) {
                throw new GlobalException(messageHelper.get("error.merchant.select.required"));
            }
            shop.setAddTime(StringUtils.getCurrentTime());
            Shop shopCreate = applicationContext.getBean(ShopService.class).updateShop(shopId, shop, true);
            // 更新门店标签管理相关
            List<Integer> shopTips = createDTO.getShopTips();
            int sort = 0;
            if (shopTips != null && !shopTips.isEmpty()) {
                for (Integer tipId : shopTips) {
                    StoreTip storeTip = new StoreTip();
                    storeTip.setShopId(shopCreate.getShopId());
                    storeTip.setTipId(tipId);
                    storeTip.setSort(sort++);
                    storeTip.setAddTime(System.currentTimeMillis());
                    storeTipMapper.insert(storeTip);
                }
            }
            Integer roleId = adminRoleService.initAdminRole(shopCreate.getMerchantId(), shopCreate.getShopId());
            adminRoleService.initEmployeeRoles(shopCreate.getShopId());
            AdminUserShopCreateDTO adminUserShopCreateDTO = new AdminUserShopCreateDTO();
            adminUserShopCreateDTO.setShopId(shopCreate.getShopId());
            adminUserShopCreateDTO.setAdminId(getCurrentAdminId());
            MerchantUser merchantUser = new MerchantUser();
            if (shopId > 0) {
                // 店铺后台内新建店铺：绑定当前登录账号
                merchantUser.setAdminUserId(getCurrentAdminId());
            } else {
                // 平台端为商户新增店铺：绑定该商户的主账号
                MerchantUser merchantMainUser = merchantUserService.getDetailByMerchantId(createDTO.getMerchantId(), true);
                merchantUser.setAdminUserId(merchantMainUser != null ? merchantMainUser.getAdminUserId() : getCurrentAdminId());
            }
            adminUserShopCreateDTO.setAdminId(merchantUser.getAdminUserId());
            adminUserShopCreateDTO.setIsAdmin(1);
            adminUserShopCreateDTO.setRoleId(roleId);
            adminUserShopCreateDTO.setAuthList(List.of("all"));
            adminUserShopService.createAdminUserShop(adminUserShopCreateDTO);
            return true;
        }
        return false;
    }

    private void setStoreParamIfPresent(StoreExtendedDto dto, Shop shop) {
        if (dto.getShopType() != null) {
            shop.setShopType(dto.getShopType());
        }
        if (dto.getStoreParentId() != null) {
            shop.setStoreParentId(dto.getStoreParentId());
        }
        if (ObjectUtil.isNotEmpty(dto.getShopCoverPicture())) {
            shop.setShopCoverPicture(dto.getShopCoverPicture());
        }
        if (dto.getShopShowPicture() != null) {
            shop.setShopShowPicture(JSONUtil.toJsonStr(dto.getShopShowPicture()));
        }
        if (dto.getDescription() != null) {
            shop.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            shop.setStatus(dto.getStatus());
        }
        if (dto.getShopContactConfig() != null) {
            shop.setShopContactJson(JSONUtil.toJsonStr(dto.getShopContactConfig()));
        }
        if (dto.getShopOpenCloseConfig() != null) {
            shop.setShopOpenCloseJson(JSONUtil.toJsonStr(dto.getShopOpenCloseConfig()));
        }
        if (dto.getShopRegionIds() != null) {
            shop.setShopRegionId(JSONUtil.toJsonStr(dto.getShopRegionIds()));
        }
        if (dto.getShopDetailedAddress() != null) {
            shop.setShopDetailedAddress(dto.getShopDetailedAddress());
        }
        if (dto.getShopLatitude() != null) {
            shop.setShopLatitude(dto.getShopLatitude());
        }
        if (dto.getShopLongitude() != null) {
            shop.setShopLongitude(dto.getShopLongitude());
        }
    }

    private void checkStoreParam(StoreExtendedDto dto, Shop shop) {
        if (tigshopProperties.getIsO2o() == 1 && (dto.getShopType().equals(ShopTypeEnum.STORE.getCode()) || dto.getShopType().equals(ShopTypeEnum.PICKUP.getCode()))) {
            if (dto.getShopType().equals(ShopTypeEnum.STORE.getCode())) {
                if (ObjectUtil.isEmpty(dto.getShopCoverPicture())) {
                    throw new GlobalException("门店封面不能为空");
                }
            }
            if (dto.getShopContactConfig() == null) {
                throw new GlobalException("请填写联系方式");
            }
            if (dto.getShopOpenCloseConfig() == null) {
                throw new GlobalException("请填写营业时间");
            }
            if (dto.getShopRegionIds() == null) {
                throw new GlobalException("请选择区域");
            }
            if (dto.getShopLongitude() == null || dto.getShopLatitude() == null) {
                throw new GlobalException("经纬度不能为空");
            }
            if (dto.getShopType() == null) {
                throw new GlobalException("请选择店铺类型");
            }
            if (!Arrays.asList(1, 2, 3).contains(dto.getShopType())) {
                throw new GlobalException("类型错误");
            }
            shop.setShopType(dto.getShopType());
            if (dto.getShopType().equals(ShopTypeEnum.PICKUP.getCode())) {
                if (dto.getStoreParentId() == null) {
                    throw new GlobalException("父门店id不能为空");
                }
                shop.setStoreParentId(dto.getStoreParentId());
            }
            shop.setShopCoverPicture(dto.getShopCoverPicture());
            shop.setShopShowPicture(JSONUtil.toJsonStr(dto.getShopShowPicture()));
            shop.setDescription(dto.getDescription());
            shop.setStatus(dto.getStatus());
            shop.setShopContactJson(JSONUtil.toJsonStr(dto.getShopContactConfig()));
            shop.setShopOpenCloseJson(JSONUtil.toJsonStr(dto.getShopOpenCloseConfig()));
            shop.setShopRegionId(JSONUtil.toJsonStr(dto.getShopRegionIds()));
            shop.setShopDetailedAddress(dto.getShopDetailedAddress());
            shop.setShopLatitude(dto.getShopLatitude());
            shop.setShopLongitude(dto.getShopLongitude());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ShopUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Shop shop = new Shop();
            BeanUtils.copyProperties(updateDTO, shop);

            checkStoreParam(updateDTO, shop);

            updateShop(shop.getShopId(), shop, false);

            // 更新门店标签管理相关
            List<Integer> shopTips = updateDTO.getShopTips();
            // 删除所有门店标签，添加新的门店标签
            storeTipMapper.delete(new LambdaQueryWrapper<StoreTip>().eq(StoreTip::getShopId, shop.getShopId()));
            int sort = 0;
            if (shopTips != null && !shopTips.isEmpty()) {
                for (Integer tipId : shopTips) {
                    StoreTip storeTip = new StoreTip();
                    storeTip.setShopId(shop.getShopId());
                    storeTip.setTipId(tipId);
                    storeTip.setSort(sort++);
                    storeTip.setAddTime(System.currentTimeMillis());
                    storeTipMapper.insert(storeTip);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public BigDecimal getSumShopMoney(Integer shopId) {
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        //sum shop_money
        if (shopId > 0) {
            queryWrapper.eq("shop_id", shopId).select("sum(shop_money) as shop_money");
        } else {
            queryWrapper.select("sum(shop_money) as shop_money");
        }

        Shop shop = this.getOne(queryWrapper);

        return null != shop ? shop.getShopMoney() : BigDecimal.ZERO;

    }

    @Override
    public BigDecimal getSumFrozenMoney(Integer shopId) {
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        //sum shop_money

        if (shopId > 0) {
            queryWrapper.eq("shop_id", shopId).select("sum(frozen_money) as frozen_money");
        } else {
            queryWrapper.select("sum(frozen_money) as frozen_money");
        }

        Shop shop = this.getOne(queryWrapper);

        return null != shop ? shop.getFrozenMoney() : BigDecimal.ZERO;
    }

    @Override
    public List<Shop> getShopByShopTitle(String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Shop::getShopTitle, keyword);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ClientShopVO> clientList(ShopListDTO listDTO) {
        //分页
        Page<Shop> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        // 搜索关键字
        if (StringUtils.isNotEmpty(listDTO.getKeyword())) {
            // 跨境翻译支持：如果是跨境模式，通过翻译表搜索店铺ID
                queryWrapper.like(Shop::getShopTitle, listDTO.getKeyword());
        }
        queryWrapper.eq(Shop::getStatus, 1);

        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 执行查询
        Page<Shop> shopPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Shop> queryPageRecords = shopPage.getRecords();
        int productLimit = Integer.max(Optional.ofNullable(listDTO.getProductNumber()).orElse(5), 0);

        if (CollUtil.isEmpty(queryPageRecords)) {
            List<ClientShopVO> emptyList = new ArrayList<>();
            prependClientSelfOperatedStore(listDTO, emptyList, productLimit);
            return PageUtil.convertPage(shopPage, emptyList);
        }

        // 提取所有店铺ID
        List<Integer> shopIds = queryPageRecords.stream().map(Shop::getShopId).toList();

        // 1. 批量查询所有店铺的自营商品（每个店铺取前N个）
        List<Product> allProducts = productService.list(new LambdaQueryWrapper<Product>()
                .in(Product::getShopId, shopIds)
                .eq(Product::getProductStatus, 1)
                .eq(Product::getIsDelete, 0)
                .orderByDesc(Product::getProductId));
        // 按店铺ID分组
        Map<Integer, List<Product>> productsByShopId = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getShopId));

        // 2. 批量查询门店商品（O2O模式）
        Map<Integer, List<StoreProduct>> storeProductsByShopId = new HashMap<>();
        Map<Integer, Long> storeProductCountByShopId = new HashMap<>();

        // 3. 批量查询收藏数
        List<Map<String, Object>> collectCounts = collectShopMapper.selectMaps(
                new QueryWrapper<CollectShop>()
                        .select("shop_id, COUNT(DISTINCT user_id) as collect_count")
                        .in("shop_id", shopIds)
                        .groupBy("shop_id"));
        Map<Integer, Long> collectCountByShopId = collectCounts.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("shop_id")).intValue(),
                        m -> ((Number) m.get("collect_count")).longValue()));

        // 4. 批量查询每个店铺的自营商品总数
        List<Map<String, Object>> productCounts = productService.listMaps(
                new QueryWrapper<Product>()
                        .select("shop_id, COUNT(*) as product_count")
                        .in("shop_id", shopIds)
                        .eq("product_status", 1)
                        .eq("is_delete", 0)
                        .groupBy("shop_id"));
        Map<Integer, Long> productCountByShopId = productCounts.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("shop_id")).intValue(),
                        m -> ((Number) m.get("product_count")).longValue()));

        // 5. 批量查询商品评分 (AVG(comment_rank) GROUP BY shop_id)
        List<Map<String, Object>> productScores = commentService.listMaps(
                new QueryWrapper<Comment>()
                        .select("shop_id, AVG(comment_rank) as avgScore")
                        .in("shop_id", shopIds)
                        .groupBy("shop_id"));
        Map<Integer, BigDecimal> productScoreByShopId = productScores.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("shop_id")).intValue(),
                        m -> new BigDecimal(m.get("avgScore").toString())));

        // 5. 预加载需要补充的平台商品
        Set<Integer> needProductIds = new HashSet<>();
        Map<Integer, List<StoreProduct>> finalStoreProductsByShopId = storeProductsByShopId;
        for (Shop shop : queryPageRecords) {
            List<Product> shopProducts = productsByShopId.getOrDefault(shop.getShopId(), List.of());
        }
        Map<Integer, Product> additionalProductMap = new HashMap<>();
        if (CollUtil.isNotEmpty(needProductIds)) {
            List<Product> additionalProducts = productService.getProductByIds(new ArrayList<>(needProductIds));
            additionalProductMap = additionalProducts.stream()
                    .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<Integer, Long> finalStoreProductCountByShopId = storeProductCountByShopId;
        Map<Integer, Product> finalAdditionalProductMap = additionalProductMap;
        // shopStar计算默认值
        BigDecimal defaultShopStar = new BigDecimal("4.8");

        List<ClientShopVO> clientShopVOList = new ArrayList<>(queryPageRecords.stream().map(shop -> {
            ClientShopVO clientShopVO = new ClientShopVO();
            BeanUtils.copyProperties(shop, clientShopVO);
            clientShopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));

            long timestampInSeconds = shop.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            clientShopVO.setAddTime(sdf.format(date));

            // 从缓存获取商品列表
            List<Product> shopProducts = productsByShopId.getOrDefault(shop.getShopId(), new ArrayList<>());
            List<Product> productList = shopProducts.stream().limit(productLimit).collect(Collectors.toList());

            // 如果自营商品不足，补充平台分配商品
            clientShopVO.setListingProduct(CollUtil.isNotEmpty(productList) ? productList : List.of());

            // 从缓存获取收藏数
            clientShopVO.setCollectCount(collectCountByShopId.getOrDefault(shop.getShopId(), 0L));

            // 从缓存获取商品总数
            Long listingCount = productCountByShopId.getOrDefault(shop.getShopId(), 0L);
            Long storeProduceCount = finalStoreProductCountByShopId.getOrDefault(shop.getShopId(), 0L);
            clientShopVO.setListingCount(listingCount + storeProduceCount);

            // 计算shopStar：评价数量<100时默认4.8，否则使用shop_star
            Integer shopStarNum = shop.getShopStarNum();
            if (shopStarNum == null || shopStarNum < 100) {
                clientShopVO.setShopStar(defaultShopStar);
            } else {
                clientShopVO.setShopStar(shop.getShopStar() != null ? shop.getShopStar() : defaultShopStar);
            }

            // 商品评分
            clientShopVO.setProductScore(productScoreByShopId.getOrDefault(shop.getShopId(), BigDecimal.ZERO));

            return clientShopVO;
        }).toList());

        prependClientSelfOperatedStore(listDTO, clientShopVOList, productLimit);
        return PageUtil.convertPage(shopPage, clientShopVOList);
    }

    /**
     * C 端店铺列表：仅显式 {@code excludeSelfOperated=0} 时在第 1 页首位插入平台自营；默认排除。
     * 与 clientList 的 status=1 过滤对齐，仅开业状态纳入；暂停运营 / 关店不展示。
     */
    private void prependClientSelfOperatedStore(ShopListDTO listDTO, List<ClientShopVO> vos, int productLimit) {
        if (listDTO.getPage() != 1 || !ObjectUtil.equals(listDTO.getExcludeSelfOperated(), 0)) {
            return;
        }
        if (vos.stream().anyMatch(vo -> ObjectUtil.equals(vo.getShopId(), 0))) {
            return;
        }
        GetStoreSelfOperatedVO settings = configService.getStoreSelfOperatedSettings();
        if (settings == null || !ObjectUtil.equals(settings.getStoreSelfOperatedStatus(), StoreType.OPEN.getCode())) {
            return;
        }
        String shopTitle = settings.getStoreSelfOperatedName();
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword) && (StringUtils.isEmpty(shopTitle) || !shopTitle.contains(keyword))) {
            return;
        }
        vos.addFirst(buildClientSelfOperatedShopVO(settings, productLimit));
    }

    private ClientShopVO buildClientSelfOperatedShopVO(GetStoreSelfOperatedVO settings, int productLimit) {
        ClientShopVO vo = new ClientShopVO();
        vo.setShopId(0);
        vo.setShopTitle(settings.getStoreSelfOperatedName());
        vo.setShopLogo(resolveSelfOperatedStoreLogo(settings));
        vo.setStatus(settings.getStoreSelfOperatedStatus());
        if (settings.getStoreSelfOperatedStatus() != null) {
            vo.setStatusText(ShopType.getTypeName(settings.getStoreSelfOperatedStatus()));
        }
        vo.setDescription(settings.getStoreSelfOperatedDescription());
        vo.setContactMobile(settings.getStoreSelfOperatedPhone());
        vo.setShopStar(new BigDecimal("4.8"));


        List<Product> products = productLimit <= 0 ? List.of() : productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, 0)
                .eq(Product::getProductStatus, 1)
                .eq(Product::getIsDelete, 0)
                .orderByDesc(Product::getProductId)
                .last("limit " + productLimit));
        vo.setListingProduct(products);
        vo.setListingCount(productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, 0)
                .eq(Product::getProductStatus, 1)
                .eq(Product::getIsDelete, 0)));
        vo.setCollectCount(collectShopMapper.selectCount(new LambdaQueryWrapper<CollectShop>()
                .eq(CollectShop::getShopId, 0)));
        Map<String, Object> scoreRow = commentService.getMap(new QueryWrapper<Comment>()
                .select("AVG(comment_rank) as avgScore")
                .eq("shop_id", 0));
        if (scoreRow != null && scoreRow.get("avgScore") != null) {
            vo.setProductScore(new BigDecimal(scoreRow.get("avgScore").toString()));
        } else {
            vo.setProductScore(BigDecimal.ZERO);
        }
        return vo;
    }

    /**
     * O2O 下补齐平台自营门店展示字段，口径对齐 storeList 的 fillSelfOperatedStoreListVO。
     */

    @Override
    public ClientShopDetailVO clientDetail(Integer id) {
        if (id == 0) {
            ClientShopDetailVO e = new ClientShopDetailVO();
            e.setShopId(0);
            if(tigshopProperties.getIsO2o() == 1) {
                e.setShopTitle(configService.getConfigVal(SettingsEnum.STORE_SELF_OPERATED_NAME));
                e.setShopLogo(configService.getConfigVal(SettingsEnum.STORE_SELF_OPERATED_LOGO));
                if(StringUtils.isBlank(e.getShopLogo())){
                    e.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
                }
            } else {
                e.setShopTitle(configService.getConfigVal(SettingsEnum.SHOP_NAME));
                e.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
            }

            return e;
        }

        // 使用包含计算评分的查询方法，评价数量<100时默认4.8，否则计算平均分
        Shop shop = getBaseMapper().selectByIdWithCalculatedStar(id);
        if (shop == null) {
            throw new GlobalException(messageHelper.get("error.shop.not.exist"));
        }
        ClientShopDetailVO clientShopDetailVO = new ClientShopDetailVO();
        BeanUtils.copyProperties(shop, clientShopDetailVO);
        clientShopDetailVO.setStatusText(ShopType.getTypeName(shop.getStatus()));

        long timestampInSeconds = shop.getAddTime();
        Date date = new Date(timestampInSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        clientShopDetailVO.setAddTime(sdf.format(date));

        if (shop.getMerchantId() > 0) {
            LambdaQueryWrapper<Merchant> merchantQueryWrapper = new LambdaQueryWrapper<>();
            merchantQueryWrapper.eq(Merchant::getMerchantId, shop.getMerchantId());
            Merchant merchant = merchantService.getOne(merchantQueryWrapper);
            ClientShopDetailVO.Merchant merchantVO = new ClientShopDetailVO.Merchant();
            BeanUtils.copyProperties(merchant, merchantVO);
            merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));

            long timeInSeconds = shop.getAddTime();
            Date dateMerchant = new Date(timeInSeconds * 1000L);
            SimpleDateFormat sdfMerchant = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            merchantVO.setAddTime(sdfMerchant.format(dateMerchant));
            merchantVO.setMerchantData(JsonUtil.fromJson(merchant.getMerchantData(), JSONObject.class));

            clientShopDetailVO.setMerchant(merchantVO);
        }

        //统计商品总数
        //店铺自己的商品数
        LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(Product::getShopId, id);
        //平台分配给门店的商品
        Long storeProduceCount = storeProductMapper.selectCount(Wrappers.<StoreProduct>lambdaQuery().eq(StoreProduct::getShopId, shop.getShopId()).eq(StoreProduct::getProductStatus, 1).eq(StoreProduct::getIsDelete, 0));
        clientShopDetailVO.setProductCount(productService.count(productQueryWrapper) + storeProduceCount);


        //统计新商品总数
        LambdaQueryWrapper<Product> productQueryWrapperNew = new LambdaQueryWrapper<>();
        productQueryWrapperNew.eq(Product::getShopId, id);
        productQueryWrapperNew.eq(Product::getIsNew, 1);
        long countBySelfShop = productService.count(productQueryWrapperNew);
        //平台分配给店铺的新品
        List<StoreProduct> storeProducts = storeProductMapper.selectList(Wrappers.<StoreProduct>lambdaQuery().eq(StoreProduct::getShopId, shop.getShopId()).eq(StoreProduct::getProductStatus, 1).eq(StoreProduct::getIsDelete, 0));
        if (CollectionUtil.isNotEmpty(storeProducts)) {
            List<Integer> productIds = storeProducts.stream().map(StoreProduct::getProductId).distinct().collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(productIds)) {
                long count = productService.count(Wrappers.<Product>lambdaQuery().in(Product::getProductId, productIds).eq(Product::getIsNew, 1));
                countBySelfShop = countBySelfShop + count;
            }
        }
        clientShopDetailVO.setNewProductCount(countBySelfShop);

        //查看是否有收藏店铺
        LambdaQueryWrapper<CollectShop> collectShopQueryWrapper = new LambdaQueryWrapper<>();
        collectShopQueryWrapper.eq(CollectShop::getShopId, id);
        collectShopQueryWrapper.eq(CollectShop::getUserId, getCurrentUserId());
        CollectShop collectShop = collectShopMapper.selectOne(collectShopQueryWrapper);
        clientShopDetailVO.setCollectShop(collectShop != null);

        //商品评分(AVG(comment_rank) GROUP BY shop_id)
        Map<String, Object> result = commentService.getMap(new QueryWrapper<Comment>()
                .select("AVG(comment_rank) as avgScore")
                .eq("shop_id", id)
                .groupBy("shop_id"));
        clientShopDetailVO.setProductScore(result != null ? new BigDecimal(result.get("avgScore").toString()) : BigDecimal.ZERO);

        //店铺收藏数
        Long collectCount = collectShopMapper.selectObjs(new QueryWrapper<CollectShop>()
                        .select("COUNT(DISTINCT user_id)")
                        .eq("shop_id", id)).stream()
                .findFirst()
                .map(value -> ((Number) value).longValue())
                .orElse(0L);
        clientShopDetailVO.setCollectCount(collectCount);

        clientShopDetailVO.setBusinessLicensePic("");
        clientShopDetailVO.setBusinessLicensePic(merchantService.getApprovedLicensePic(shop.getMerchantId()));

        return clientShopDetailVO;
    }

    @Override
    public List<Shop> getListByIds(List<Integer> ids) {
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Shop::getShopId, ids);
        return this.list(queryWrapper);
    }

    /**
     * 统一拦截店铺新增：所有 insert 路径（updateShop、商户入驻审核等）经此分配随机 shop_id。
     * shop 表已去 AUTO_INCREMENT 且实体为 IdType.INPUT，必须显式带 id。
     */
    @Override
    public boolean save(Shop entity) {
        assignRandomShopId(entity);
        return super.save(entity);
    }

    /**
     * 为新店铺分配恒定 10 位随机未占用 shop_id（范围 [1000000000, 2000000000)，落在有符号 int 范围内）。
     * 已显式指定 id（大于 0）的不处理；查重重试若仍失败则抛错，由调用方/用户重试。
     * 主键唯一约束为最终兜底，并发极端情况下 insert 会抛 DuplicateKey。
     */
    private void assignRandomShopId(Shop shop) {
        if (shop == null || (shop.getShopId() != null && shop.getShopId() > 0)) {
            return;
        }
        for (int i = 0; i < 10; i++) {
            int candidate = ThreadLocalRandom.current().nextInt(1_000_000_000, 2_000_000_000);
            if (this.getById(candidate) == null) {
                shop.setShopId(candidate);
                return;
            }
        }
        throw new GlobalException("店铺ID生成失败，请重试");
    }

    @Transactional
    @Override
    public Shop updateShop(Integer id, Shop shopData, boolean isAdd) {
        Integer shopType = shopData.getShopType();
        if (isAdd) {
            if (shopData.getMerchantId() > 0) {
                if (!Objects.equals(shopData.getShopType(), ShopTypeEnum.PICKUP.getCode())) {
                    //自提点没有限制,店铺和门店
                    String maxShopCountVal = configService.getConfigByCode(SettingsEnum.MAX_SHOP_COUNT.getBizCode()).getBizVal();
                    long maxShopCount = Long.parseLong(maxShopCountVal);
                    QueryWrapper<Shop> shopCountQuery = new QueryWrapper<Shop>()
                            .eq("merchant_id", shopData.getMerchantId())
                            .eq("shop_type", shopData.getShopType())
                            .and(w -> w.isNull("is_delete").or().eq("is_delete", 0));
                    long shopCount = this.count(shopCountQuery);
                    if (shopCount >= maxShopCount) {
                        if (Objects.equals(shopData.getShopType(), ShopTypeEnum.STORE.getCode())) {
                            throw new GlobalException(messageHelper.get("error.shop.store.count.limit", maxShopCount));
                        } else if (Objects.equals(shopData.getShopType(), ShopTypeEnum.SHOP.getCode())) {
                            throw new GlobalException(messageHelper.get("error.shop.count.limit", maxShopCount));
                        }
                        throw new GlobalException(messageHelper.get("error.shop.count.limit", maxShopCount));
                    }
                }
            }
            this.save(shopData);
            //初始化运费模板
            shippingTplService.initShippingTpl(shopData.getShopId());
            //初始化装修模板
            decorateService.initDecorate(shopData.getShopId());
            //初始化角色
            adminRoleService.initAdminRole(shopData.getMerchantId(), shopData.getShopId());
            adminRoleService.initEmployeeRoles(shopData.getShopId());
            //初始化商品
            //productService.initProductByShopId(shopData.getShopId());
            //多门店版本且为门店类型时，新增默认自提点
        } else {
            if (id <= 0) {
                throw new GlobalException(SERVICE_ERROR);
            }
            Shop shop = new Shop();
            BeanUtils.copyProperties(shopData, shop);
            this.updateById(shop);
        }
        return shopData;
    }

    @Override
    public Boolean checkShopStatus(Integer shopId, String productName) {
        // 平台自营店（shopId=0）由配置生成，不对应 shop 表记录，无需检查实体店状态。
        if (shopId == null) {
            return false;
        }
        if (shopId == 0) {
            return true;
        }
        Integer shopStatus = detail(shopId).getStatus();
        if (Objects.equals(shopStatus, ShopType.CLOSE.getCode())) {
            throw new GlobalException(messageHelper.get("error.shop.suspended", productName));
        }
        String mchidType = configService.getConfigVal(SettingsEnum.WECHAT_MCHID_TYPE);
        String splitMethod = configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD);
        if (configService.isProfitSharingEnabled()
                && "2".equals(StrUtil.trimToEmpty(mchidType))
                && "2".equals(StrUtil.trimToEmpty(splitMethod))) {
            SubMerchant merchant = subMerchantService.getActiveByShop(shopId.longValue(), "WECHAT");
            if (merchant == null || StrUtil.isBlank(merchant.getSubMchId())) {
                throw new GlobalException("该店铺尚未完成微信进件，暂不支持购买");
            }
        }
        return true;
    }

    @Override
    public List<Shop> getShopList(ShopListDTO dto) {
        return this.list(new LambdaQueryWrapper<Shop>().eq(Shop::getShopType, dto.getShopType())
                // 过滤已删除的店铺（is_delete 为 null 或 0 表示未删除），与组织店铺列表口径保持一致
                .and(w -> w.isNull(Shop::getIsDelete).or().eq(Shop::getIsDelete, 0))
                .and(StringUtils.isNotBlank(dto.getShopTitle()) || StringUtils.isNotEmpty(dto.getKeyword()),
                        wrapper -> wrapper
                                .like(StringUtils.isNotBlank(dto.getShopTitle()), Shop::getShopTitle, dto.getShopTitle())
                                .or()
                                .like(StringUtils.isNotEmpty(dto.getKeyword()), Shop::getShopTitle, dto.getKeyword())));
    }

    private List<Integer> posCashierRoleIds() {
        return adminRoleService.list(new LambdaQueryWrapper<AdminRole>()
                        .eq(AdminRole::getRoleCode, "pos_cashier")
                        .select(AdminRole::getRoleId))
                .stream()
                .map(AdminRole::getRoleId)
                .toList();
    }

    @Override
    public StaffShowVO staffShow(Integer shopId) {
        List<Integer> cashierRoleIds = posCashierRoleIds();
        boolean excludeCashier = ObjectUtil.isNotEmpty(cashierRoleIds);
        // 已有员工数
        long totalUsingUser = adminUserShopService.count(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getIsUsing, 0).eq(AdminUserShop::getShopId, shopId).notIn(excludeCashier, AdminUserShop::getRoleId, cashierRoleIds));
        // 子管理员数
        long subUsingUser = adminUserShopService.count(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getIsUsing, 0).eq(AdminUserShop::getShopId, shopId).eq(AdminUserShop::getIsAdmin, 0).notIn(excludeCashier, AdminUserShop::getRoleId, cashierRoleIds));

        // 停用员工数
        long stopUsingUser = adminUserShopService.count(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getIsUsing, 1).eq(AdminUserShop::getShopId, shopId).notIn(excludeCashier, AdminUserShop::getRoleId, cashierRoleIds));

        int maxSubAdministrator = Integer.parseInt(configService.getSettings(ShopSettingsVO.class).getMaxSubAdministrator());
        long residue = maxSubAdministrator > 0 ? maxSubAdministrator - (subUsingUser + stopUsingUser) : 0;

        List<Integer> adminIds = adminUserShopService.list(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getShopId, shopId).notIn(excludeCashier, AdminUserShop::getRoleId, cashierRoleIds)).stream().map(AdminUserShop::getAdminId).toList();

        List<AdminLog> list = adminLogService.list(new LambdaQueryWrapper<AdminLog>().in(ObjectUtil.isNotEmpty(adminIds), AdminLog::getUserId, adminIds).orderByDesc(AdminLog::getLogId).last("limit 5"));

        StaffShowVO staffShowVO = new StaffShowVO();
        staffShowVO.setUsingUser((int) totalUsingUser);
        staffShowVO.setStopUsingUser((int) stopUsingUser);
        staffShowVO.setResidue((int) residue);
        staffShowVO.setAdminLog(list.stream().map(item -> {
            StaffShowVO.AdminLogVO adminLogVO = new StaffShowVO.AdminLogVO();
            adminLogVO.setLogId(item.getLogId());
            DateTime date = DateUtil.date(item.getLogTime() * 1000);
            adminLogVO.setLogTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
            adminLogVO.setLogInfo(item.getLogInfo());
            adminLogVO.setIpAddress(item.getIpAddress());
            adminLogVO.setUserId(item.getUserId());
            AdminUser adminUser = adminUserService.getById(item.getUserId());
            if (adminUser != null) {
                adminLogVO.setUsername(adminUser.getUsername());
            }
            return adminLogVO;
        }).toList());
        return staffShowVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Shop updateInfo(ShopUpdateInfoDTO dto) {
        // 获取店铺ID
        Integer shopId = HeaderUtils.getShopId();

        // 如果传入的店铺ID与获取的店铺ID不相等，抛出异常
        if (!Objects.equals(dto.getShopId(), shopId)) {
            throw new GlobalException("没有此店铺");
        }

        // 将传入的DTO对象转换为Shop对象
        Shop shop = BeanUtil.copyProperties(dto, Shop.class);

        checkStoreParam(dto, shop);

        // 更新店铺信息
        return this.updateShop(shopId, shop, false);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setting(ShopSettingDTO request) {
        Integer shopId = request.getShopId();
        Integer currentShopId = getShopId();

        if (!shopId.equals(currentShopId)) {
            throw new GlobalException("没有此店铺");
        }

        // 停业(status=4)前置校验：店铺所有订单已完结且无进行中售后，才允许停业
        // 口径与保证金全额退还/提现一致（进行中订单 orderStatus∈{0,1,2}；未完结售后 status∉{完成,取消}）
        if (request.getStatus() != null && request.getStatus() == 4) {
            long ongoingOrderCount = orderService.lambdaQuery()
                    .eq(Order::getShopId, shopId)
                    .eq(Order::getIsDel, 0)
                    .in(Order::getOrderStatus, ONGOING_ORDER_STATUS)
                    .count();
            if (ongoingOrderCount > 0) {
                throw new GlobalException("存在进行中订单，无法停业，请先处理完所有订单");
            }
            long ongoingAftersalesCount = aftersalesService.lambdaQuery()
                    .eq(Aftersales::getShopId, shopId)
                    .notIn(Aftersales::getStatus,
                            AftersalesStatusEnum.COMPLETE.getCode(),
                            AftersalesStatusEnum.CANCEL.getCode())
                    .count();
            if (ongoingAftersalesCount > 0) {
                throw new GlobalException("存在未完结售后，无法停业，请先处理完所有售后");
            }
        }

        Shop shop = new Shop();
        BeanUtils.copyProperties(request, shop, "shipInfo", "receiveInfo");
        if (request.getShopShowCategory() != null) {
            shop.setShopShowCategory(JSONUtil.toJsonStr(request.getShopShowCategory()));
        }
        if (request.getKefuInlet() != null) {
            shop.setKefuInlet(JsonUtil.toJson(request.getKefuInlet()));
        }
        ShopSettingDTO.ShopDeliveryInfo shipInfo = request.getShipInfo();
        if (shipInfo != null) {
            shop.setShipInfo(JSONUtil.toJsonStr(shipInfo));
        }

        ShopSettingDTO.ShopDeliveryInfo receiveInfo = request.getReceiveInfo();
        if (receiveInfo != null) {
            shop.setReceiveInfo(JSONUtil.toJsonStr(receiveInfo));
        }
        updateById(shop);

        //店铺暂停运营后，下架店铺内所以商品
        if (request.getStatus() != null && request.getStatus() == 4) {
            productService.lambdaUpdate().set(Product::getProductStatus, 0).eq(Product::getShopId, shopId).update();
        }
    }

    private ShopVO.ShopDeliveryInfo parseShipInfo(Shop shop) {
        return parseDeliveryInfo(shop.getShipInfo());
    }

    private ShopVO.ShopDeliveryInfo parseReceiveInfo(Shop shop) {
        return parseDeliveryInfo(shop.getReceiveInfo());
    }

    private ShopVO.ShopDeliveryInfo parseDeliveryInfo(String json) {
        if (StringUtils.isEmpty(json) || !JSONUtil.isTypeJSON(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, ShopVO.ShopDeliveryInfo.class);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void choose(LoginChooseParam loginChooseParam) {
        int adminId = SecurityUtils.getCurrentAdminId();
        String id = loginChooseParam.getId();

        AdminUser adminUser = adminUserService.getById(adminId);
        Integer roleId = adminUser.getRoleId();
        String authJsonArrayStr = adminUser.getAuthList();

        if (ObjectUtil.equals("shop", loginChooseParam.getAdminType())) {
            Shop byId = this.getById(id);
            if (byId == null) {
                throw new GlobalException("没有此店铺");
            }
            AdminUserShop adminUserShop = adminUserShopService.getOne(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getShopId, id).eq(AdminUserShop::getAdminId, adminId));
            if (adminUserShop == null) {
                throw new GlobalException("没有此店铺权限");
            }
            if (!ObjectUtil.equals(adminUserShop.getIsUsing(), 0)) {
                throw new GlobalException("用户已经被禁用");
            }
            roleId = adminUserShop.getRoleId();
            authJsonArrayStr = adminUserShop.getAuthList();
        }

        // RBAC: 有角色走角色权限
        if (roleId != null && roleId > 0) {
            AdminRole role = adminRoleService.getById(roleId);
            if (role != null) {
                authJsonArrayStr = role.getAuthorityList();
            }
        }

        // 1. 根据选择，查询权限保存到 redis ；key 为 admin::auth::[[admin_id]]
        LoginChooseAuth loginChooseAuth = new LoginChooseAuth();
        loginChooseAuth.setLoginChooseParam(loginChooseParam);
        loginChooseAuth.setAuthorities(JSONUtil.toList(authJsonArrayStr, String.class));
        redisTemplate.opsForValue().set(StrUtil.format("{}{}{}", ADMIN_USER, "auth::", adminId), loginChooseAuth);
    }

    @Override
    public List<String> checkAndReturnAuthList(Integer adminId, String id, String adminType) {
        AdminUser adminUser = adminUserService.getById(adminId);
        Integer roleId = adminUser.getRoleId();
        String authJsonArrayStr = adminUser.getAuthList();

        if (ObjectUtil.equals("shop", adminType)) {
            Shop byId = this.getById(id);
            if (byId == null) {
                throw new GlobalException("没有此店铺");
            }
            HeaderUtils.setAttrValue(X_SHOP_TYPE, byId.getShopType() + "");
            AdminUserShop adminUserShop = adminUserShopService.getOne(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getShopId, id).eq(AdminUserShop::getAdminId, adminId));
            if (adminUserShop == null) {
                return List.of();
            }
            roleId = adminUserShop.getRoleId();
            authJsonArrayStr = adminUserShop.getAuthList();
        }

        if (ObjectUtil.equals("admin", adminType)
                && (roleId == null || roleId <= 0)
                && isEmptyAuthorityList(authJsonArrayStr)) {
            AdminUserShop binding = getSelfOperatedEmployeeBinding(adminId);
            AdminRole role = getEnabledWorkbenchRole(binding);
            if (role != null) {
                return parseAuthorityList(role.getAuthorityList());
            }
            return List.of();
        }

        // RBAC: 有角色走角色权限，无角色走旧 auth_list（老客户兼容）
        if (roleId != null && roleId > 0) {
            AdminRole role = adminRoleService.getById(roleId);
            if (role != null) {
                return parseAuthorityList(role.getAuthorityList());
            }
        }
        return parseAuthorityList(authJsonArrayStr);
    }

    private boolean isPlatformSystemAdministrator(Integer adminId) {
        AdminUser user = adminUserService.getById(adminId);
        if (user == null || !"admin".equals(user.getAdminType())
                || Integer.valueOf(1).equals(user.getIsDeleted())
                || Integer.valueOf(1).equals(user.getIsUsing())
                || user.getRoleId() == null || user.getRoleId() <= 0) {
            return false;
        }
        AdminRole role = adminRoleService.getById(user.getRoleId());
        return role != null && Integer.valueOf(1).equals(role.getIsSystem())
                && "admin".equals(role.getAdminType())
                && Integer.valueOf(0).equals(role.getShopId())
                && (role.getVendorId() == null || role.getVendorId() == 0)
                && (role.getMerchantId() == null || role.getMerchantId() == 0)
                && parseAuthorityList(role.getAuthorityList()).contains("all");
    }

    @Override
    public boolean checkIsUsing(Integer adminId, String id, String adminType) {
        if (ObjectUtil.equals("admin", adminType)) {
            AdminUser adminUser = adminUserService.getById(adminId);
            if (adminUser == null
                    || (adminUser.getRoleId() != null && adminUser.getRoleId() > 0)
                    || !isEmptyAuthorityList(adminUser.getAuthList())) {
                    return true;
            }
            AdminUserShop binding = getSelfOperatedEmployeeBinding(adminId);
            AdminRole role = getWorkbenchRole(binding);
            if (role != null) {
                return ObjectUtil.equals(binding.getIsUsing(), 0);
            }
            return true;
        }
        if (ObjectUtil.equals("shop", adminType)) {
            AdminUserShop adminUserShop = adminUserShopService.getOne(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, id)
                    .eq(AdminUserShop::getAdminId, adminId));
            return adminUserShop == null || ObjectUtil.equals(adminUserShop.getIsUsing(), 0);
        }
        return true;
    }

    private AdminUserShop getSelfOperatedEmployeeBinding(Integer adminId) {
        return adminUserShopService.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .eq(AdminUserShop::getShopId, 0));
    }

    private AdminRole getEnabledWorkbenchRole(AdminUserShop binding) {
        if (binding == null || !ObjectUtil.equals(binding.getIsUsing(), 0)) {
            return null;
        }
        return getWorkbenchRole(binding);
    }

    private AdminRole getWorkbenchRole(AdminUserShop binding) {
        if (binding == null
                || ObjectUtil.equals(binding.getIsAdmin(), 1)
                || binding.getRoleId() == null
                || binding.getRoleId() <= 0) {
            return null;
        }
        AdminRole role = adminRoleService.getById(binding.getRoleId());
        return workbenchCandidatePolicy.isKefuCandidate(binding, role)
                || workbenchCandidatePolicy.isPosCandidate(binding, role) ? role : null;
    }

    private boolean isEmptyAuthorityList(String authorityList) {
        if (StringUtils.isEmpty(authorityList)) {
            return true;
        }
        if (!JSONUtil.isTypeJSONArray(authorityList)) {
            return false;
        }
        try {
            return JSONUtil.parseArray(authorityList).isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private List<String> parseAuthorityList(String authorityList) {
        if (StringUtils.isEmpty(authorityList) || !JSONUtil.isTypeJSONArray(authorityList)) {
            return List.of();
        }
        return JSONUtil.toList(authorityList, String.class);
    }

    @Override
    public ShopVendorSettingVO getVendorSetting() {
        Shop shop = this.getById(getShopId());
        ShopVendorSettingVO shopVendorSettingVO = new ShopVendorSettingVO();
        shopVendorSettingVO.setVendorSetPriceType(shop.getVendorSetPriceType());
        shopVendorSettingVO.setVendorSetPriceAutoValue(shop.getVendorSetPriceAutoValue());
        return shopVendorSettingVO;
    }

    @Override
    public void updateVendorSetting(ShopVendorSettingUpdateParam param) {
        Shop shop = this.getById(getShopId());
        shop.setVendorSetPriceType(param.getVendorSetPriceType());
        if (ObjectUtil.isNotEmpty(param.getVendorSetPriceAutoValue())) {
            shop.setVendorSetPriceAutoValue(param.getVendorSetPriceAutoValue());
        }
        updateById(shop);
    }

    @Override
    public Page<PickupListVO> pickupList(PickupPageQuery query) {
        Integer shopId = query.getShopId();
        if (getById(shopId) == null && shopId > 0) {
            throw new GlobalException("没有此门店");
        }
        String configVal = configService.getConfigVal(SettingsEnum.STORE_SHOW_OTHER_CITY_STORE);
        if ("1".equals(configVal)) {
            query.setRegionId(null);
        }
        Page<Shop> page = new Page<>(query.getPage(), query.getSize());
        if (query.getLatitude() == null || query.getLatitude().compareTo(BigDecimal.ZERO) == 0) {
            query.setLatitude(null);
        }
        Page<PickupListVO> shopPage = getBaseMapper().pickupListByLocation(page, query);
        shopPage.getRecords().forEach(pickupListVO -> {
            pickupListVO.setShopRegionIds(JSONUtil.toList(pickupListVO.getShopRegionIdsJson(), Integer.class));
            pickupListVO.setContactConfigs(JSONUtil.toList(pickupListVO.getContactJson(), StoreExtendedDto.ShopContactConfig.class));
            pickupListVO.setShopOpenCloseConfig(JSONUtil.toBean(pickupListVO.getShopOpenCloseConfigJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
            pickupListVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(pickupListVO.getShopRegionIds()));
        });
        return shopPage;
    }

    @Override
    public Page<StoreListVO> storeList(StorePageQuery storePageQuery) {
        String configVal = configService.getConfigVal(SettingsEnum.STORE_SHOW_OTHER_CITY_STORE);
        if ("1".equals(configVal)) {
            storePageQuery.setRegionId(null);
        }
        if (storePageQuery.getStoreRegionId() != null) {
            storePageQuery.setRegionId(storePageQuery.getStoreRegionId());
        }
        // 配送类型过滤开启时，自营门店(shopId=0)无门店级配送配置，按普通门店一起判断，不再插入占位
        boolean hasLocation = storePageQuery.getLatitude() != null
                && storePageQuery.getLatitude().compareTo(BigDecimal.ZERO) != 0;
        // 配送类型过滤（多选 OR，编号同商品列表）：含 4 按同城配送开关过滤、含 2 按预约上门开关过滤；1/3/5 不过滤
        List<Integer> deliveryTypes = storePageQuery.getDeliveryTypes();
        boolean deliveryTypeFilter = CollUtil.isNotEmpty(deliveryTypes)
                && (deliveryTypes.contains(2) || deliveryTypes.contains(4));
        // 配送范围过滤仅在传了经纬度时生效
        boolean inRangeFilter = storePageQuery.getWithinDeliveryRange() != null
                && storePageQuery.getWithinDeliveryRange() == 1
                && hasLocation;
        boolean onlyLocalDelivery = deliveryTypeFilter || inRangeFilter;
        if (storePageQuery.getProductId() != null) {
            Product byId = productService.getById(storePageQuery.getProductId());
            if (byId != null && byId.getShopId() != null && byId.getShopId() > 0) {
                // 门店自建商品（非平台分配）：仅展示该商品归属的门店，不混入 store_product 平台分发记录
                storePageQuery.setShopIds(new ArrayList<>(List.of(byId.getShopId())));
            } else {
                // 平台自建商品（shopId=0）：按 store_product 平台分发记录展示门店
                List<StoreProduct> storeProducts = storeProductMapper.selectList(new LambdaQueryWrapper<StoreProduct>().eq(StoreProduct::getProductId, storePageQuery.getProductId()).eq(StoreProduct::getIsDelete, NO).eq(StoreProduct::getProductStatus, YES));
                List<Integer> shopIds = new ArrayList<>(storeProducts.stream().map(StoreProduct::getShopId).map(Long::intValue).toList());
                storePageQuery.setShopIds(shopIds);
                if (ObjectUtil.isEmpty(storePageQuery.getShopIds())) {
                    Page<StoreListVO> storeListVOPage = new Page<>(storePageQuery.getPage(), storePageQuery.getSize());
                    // 同城配送过滤 / excludeSelfOperated=1 / 暂停运营时不返回自营门店占位
                    if (!shouldInsertSelfOperatedStore(storePageQuery, onlyLocalDelivery)) {
                        return storeListVOPage;
                    }
                    GetStoreSelfOperatedVO selfOperatedSettings = configService.getStoreSelfOperatedSettings();
                    if (isSelfOperatedStoreSuspended(selfOperatedSettings)) {
                        return storeListVOPage;
                    }
                    StoreListVO e = new StoreListVO();
                    e.setShopId(0);
                    e.setShopTitle(configService.getConfigVal(SettingsEnum.STORE_SELF_OPERATED_NAME));
                    e.setShopLogo(resolveSelfOperatedStoreLogo(selfOperatedSettings));
                    fillSelfOperatedStoreListVO(e, selfOperatedSettings);

                    storeListVOPage.setRecords(List.of(e));
                    return storeListVOPage;
                }
            }
        }

        Page<Shop> page = new Page<>(storePageQuery.getPage(), storePageQuery.getSize());
        Page<StoreListVO> shopPage;
        if (storePageQuery.getLatitude() != null && storePageQuery.getLatitude().compareTo(BigDecimal.ZERO) != 0) {
            shopPage = getBaseMapper().storeListByLocation(page, storePageQuery);
        } else {
            shopPage = getBaseMapper().storeListByRegion(page, storePageQuery);
        }
        if (storePageQuery.getPage() == 1 && shouldInsertSelfOperatedStore(storePageQuery, onlyLocalDelivery)) {
            GetStoreSelfOperatedVO selfOperatedSettings = configService.getStoreSelfOperatedSettings();
            if (!isSelfOperatedStoreSuspended(selfOperatedSettings)) {
                StoreListVO e = new StoreListVO();
                e.setShopId(0);
                e.setShopTitle(configService.getConfigVal(SettingsEnum.STORE_SELF_OPERATED_NAME));
                e.setShopLogo(resolveSelfOperatedStoreLogo(selfOperatedSettings));
                fillSelfOperatedStoreListVO(e, selfOperatedSettings);


                if (CollUtil.isEmpty(shopPage.getRecords())) {
                    shopPage.setRecords(List.of(e));
                } else {
                    shopPage.getRecords().addFirst(e);
                }
            }
        }
        // 选中门店强制置顶：搜索/过滤（regionId、keyword、分页）后结果集不含 selectedShopId 时，
        // 单独查询该门店并插入到第 1 页首位；绕过 regionId / keyword，但仍要求门店营业状态与门店类型合法
        Integer selectedShopId = storePageQuery.getSelectedShopId();
        if (storePageQuery.getPage() == 1 && selectedShopId != null && selectedShopId > 0) {
            boolean alreadyInList = shopPage.getRecords().stream()
                    .anyMatch(record -> selectedShopId.equals(record.getShopId()));
            if (!alreadyInList) {
                Shop selectedShop = this.lambdaQuery()
                        .eq(Shop::getShopId, selectedShopId)
                        .eq(Shop::getShopType, ShopTypeEnum.STORE.getCode())
                        .in(Shop::getStatus, List.of(1, 10))
                        .one();
                if (selectedShop != null) {
                    StoreListVO selectedVO = new StoreListVO();
                    selectedVO.setShopId(selectedShop.getShopId());
                    selectedVO.setShopLogo(selectedShop.getShopLogo());
                    selectedVO.setShopTitle(selectedShop.getShopTitle());
                    selectedVO.setStatus(selectedShop.getStatus());
                    selectedVO.setAddress(selectedShop.getShopDetailedAddress());
                    selectedVO.setLongitude(selectedShop.getShopLongitude());
                    selectedVO.setLatitude(selectedShop.getShopLatitude());
                    selectedVO.setContactJson(selectedShop.getShopContactJson());
                    // 与 mapper calculateShopStar 保持一致：评价数<100 或为空/0 时默认 4.8
                    Integer selectedStarNum = selectedShop.getShopStarNum();
                    if (selectedStarNum == null || selectedStarNum < 100 || selectedStarNum == 0) {
                        selectedVO.setShopStar(new BigDecimal("4.8"));
                    } else {
                        selectedVO.setShopStar(selectedShop.getShopStar());
                    }
                    selectedVO.setShopStarNum(selectedShop.getShopStarNum());
                    selectedVO.setDescription(selectedShop.getDescription());
                    selectedVO.setShopSales(selectedShop.getShopSales());
                    selectedVO.setShopOpenCloseConfigJson(selectedShop.getShopOpenCloseJson());
                    shopPage.getRecords().addFirst(selectedVO);
                }
            }
        }
        // 批量预取，避免逐店 N+1（券数量 / 门店标签 / 标签名 / 收藏数 / 当前用户收藏）
        List<Integer> storeShopIds = shopPage.getRecords().stream()
                .map(StoreListVO::getShopId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<Integer> couponShopIds;
        Map<Integer, List<StoreTip>> shopTipsMap;
        Map<Integer, String> tipNameMap;
        Map<Integer, Long> collectCountMap;
        Set<Integer> userCollectShopIds;
        if (CollUtil.isNotEmpty(storeShopIds)) {
            // 有效店铺券（type=2、可用、未删、未过期）→ 有券店铺集合
            List<Promotion> coupons = promotionMapper.selectList(new LambdaQueryWrapper<Promotion>()
                    .in(Promotion::getRelationId, storeShopIds)
                    .eq(Promotion::getType, 2)
                    .eq(Promotion::getIsAvailable, YES)
                    .eq(Promotion::getIsDelete, NO)
                    .gt(Promotion::getEndTime, StringUtils.getCurrentTime()));
            couponShopIds = coupons.stream().map(Promotion::getRelationId).collect(Collectors.toSet());
            // 门店标签（全局按 sort 升序，分组后每店内仍保持 sort 序）
            List<StoreTip> allTips = storeTipMapper.selectList(new LambdaQueryWrapper<StoreTip>()
                    .in(StoreTip::getShopId, storeShopIds)
                    .orderByAsc(StoreTip::getSort));
            shopTipsMap = allTips.stream().collect(Collectors.groupingBy(StoreTip::getShopId));
            List<Integer> tipIds = allTips.stream().map(StoreTip::getTipId).filter(Objects::nonNull).distinct().toList();
            tipNameMap = CollUtil.isEmpty(tipIds) ? Collections.emptyMap()
                    : tipMapper.selectBatchIds(tipIds).stream()
                    .collect(Collectors.toMap(t -> t.getTipId(), t -> t.getTipName(), (a, b) -> a));
            // 收藏：一次取回本页店铺全部收藏行，聚合出每店收藏数 + 当前用户收藏集合
            List<CollectShop> collects = collectShopMapper.selectList(new LambdaQueryWrapper<CollectShop>()
                    .in(CollectShop::getShopId, storeShopIds));
            collectCountMap = collects.stream()
                    .collect(Collectors.groupingBy(CollectShop::getShopId,
                            Collectors.mapping(CollectShop::getUserId, Collectors.collectingAndThen(
                                    Collectors.toSet(), set -> (long) set.size()))));
            Integer currentUserId = getCurrentUserId();
            userCollectShopIds = collects.stream()
                    .filter(cs -> currentUserId != null && currentUserId.equals(cs.getUserId()))
                    .map(CollectShop::getShopId)
                    .collect(Collectors.toSet());
        } else {
            couponShopIds = Collections.emptySet();
            shopTipsMap = Collections.emptyMap();
            tipNameMap = Collections.emptyMap();
            collectCountMap = Collections.emptyMap();
            userCollectShopIds = Collections.emptySet();
        }
        // 配送类型（多选，编号与商品列表 ProductListDTO.deliveryTypes 一致）：含 4 判同城配送范围、含 2 判预约上门范围
        // 只在传了经纬度时逐店计算；批量取配置避免 N+1；否则空 Map（对应布尔保持默认 false）
        boolean needLocalRange = hasLocation && CollUtil.isNotEmpty(deliveryTypes) && deliveryTypes.contains(4) && CollUtil.isNotEmpty(storeShopIds);
        boolean needHomeRange = hasLocation && CollUtil.isNotEmpty(deliveryTypes) && deliveryTypes.contains(2) && CollUtil.isNotEmpty(storeShopIds);
        final Map<Integer, ShopLocalDeliveryConfig> localDeliveryConfigMap;
        // 门店运力二选一：切到第三方运力时自建 enabled 会被置 0，同城配送仍然可用
        // o2o 关闭时第三方运力不存在，thirdPartyCarrierShopIds 保持空集（lambda 捕获需 effectively final，用临时变量兜底后一次性赋值）
        Set<Integer> thirdPartyCarrierShopIdsTmp = Collections.emptySet();
        if (needLocalRange) {
            localDeliveryConfigMap = shopLocalDeliveryConfigMapper.selectList(new LambdaQueryWrapper<ShopLocalDeliveryConfig>()
                            .in(ShopLocalDeliveryConfig::getShopId, storeShopIds))
                    .stream()
                    .collect(Collectors.toMap(ShopLocalDeliveryConfig::getShopId, cfg -> cfg, (a, b) -> a));
        } else {
            localDeliveryConfigMap = Collections.emptyMap();
        }
        final Set<Integer> thirdPartyCarrierShopIds = thirdPartyCarrierShopIdsTmp;
        final Map<Integer, ShopHomeServiceConfig> homeServiceConfigMap;
        if (needHomeRange) {
            homeServiceConfigMap = shopHomeServiceConfigMapper.selectList(new LambdaQueryWrapper<ShopHomeServiceConfig>()
                            .in(ShopHomeServiceConfig::getShopId, storeShopIds))
                    .stream()
                    .collect(Collectors.toMap(ShopHomeServiceConfig::getShopId, cfg -> cfg, (a, b) -> a));
        } else {
            homeServiceConfigMap = Collections.emptyMap();
        }
        shopPage.getRecords().forEach(storeListVO -> {
            // 平台自营占位(shopId=0)已由 fillSelfOperatedStoreListVO 写入联系方式/标签/营业时间；
            // 不可再按 contactJson / store_tip / openCloseJson 覆盖（虚拟门店无这些源数据）。
            boolean selfOperatedPlaceholder = ObjectUtil.equals(storeListVO.getShopId(), 0);
            if (!selfOperatedPlaceholder) {
                storeListVO.setContactConfigs(JSONUtil.toList(storeListVO.getContactJson(), StoreExtendedDto.ShopContactConfig.class));
                List<StoreTip> storeTips = shopTipsMap.getOrDefault(storeListVO.getShopId(), Collections.emptyList());
                // tip 被删时 tipNameMap 无键 → 过滤 null（修复原 selectById 返回 null 直接 getTipName 的 NPE）
                storeListVO.setTipNames(storeTips.stream().map(item -> tipNameMap.get(item.getTipId())).filter(Objects::nonNull).toList());
                storeListVO.setShopOpenCloseConfig(JSONUtil.toBean(storeListVO.getShopOpenCloseConfigJson(), StoreListVO.ShopOpenCloseConfig.class));
            }
            storeListVO.setHasCoupon(couponShopIds.contains(storeListVO.getShopId()) ? 1 : 0);
            storeListVO.setShopCollect(collectCountMap.getOrDefault(storeListVO.getShopId(), 0L));
            storeListVO.setCollectShop(userCollectShopIds.contains(storeListVO.getShopId()));
            // 选中门店标识：selectedShopId 命中时置为 true
            storeListVO.setSelected(storePageQuery.getSelectedShopId() != null
                    && storePageQuery.getSelectedShopId() > 0
                    && storePageQuery.getSelectedShopId().equals(storeListVO.getShopId()));
            // shopStar 保留一位小数
            if (storeListVO.getShopStar() != null) {
                storeListVO.setShopStar(storeListVO.getShopStar().setScale(1, java.math.RoundingMode.HALF_UP));
            }

            if (storePageQuery.getLatitude() != null && storePageQuery.getLatitude().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal distance = GpsDistanceUtil.getDistance(storePageQuery.getLongitude(), storePageQuery.getLatitude(), storeListVO.getLongitude(), storeListVO.getLatitude());
                storeListVO.setDistance(distance);
            }
            // 同城配送(4)：开启 且 delivery_radius(米)>0 且 距离(米) <= delivery_radius（与商品服务口径一致，按米）
            BigDecimal dist = storeListVO.getDistance();
            if (dist != null) {
                ShopLocalDeliveryConfig localConfig = localDeliveryConfigMap.get(storeListVO.getShopId());
                if (localConfig != null
                        && (Integer.valueOf(1).equals(localConfig.getEnabled())
                        || thirdPartyCarrierShopIds.contains(storeListVO.getShopId()))
                        && localConfig.getDeliveryRadius() != null
                        && localConfig.getDeliveryRadius().compareTo(BigDecimal.ZERO) > 0
                        && dist.compareTo(localConfig.getDeliveryRadius()) <= 0) {
                    storeListVO.setInLocalDeliveryRange(true);
                }
                // 上门服务/预约上门(2)：开启 且 service_radius(米)>0 且 距离(米) <= service_radius
                ShopHomeServiceConfig homeConfig = homeServiceConfigMap.get(storeListVO.getShopId());
                if (homeConfig != null
                        && Integer.valueOf(1).equals(homeConfig.getEnabled())
                        && homeConfig.getServiceRadius() != null
                        && homeConfig.getServiceRadius().compareTo(BigDecimal.ZERO) > 0
                        && dist.compareTo(homeConfig.getServiceRadius()) <= 0) {
                    storeListVO.setInHomeServiceRange(true);
                }
            }
        });
        List<StoreListVO> records = shopPage.getRecords();
        if(records!=null && records.size()>0 && shopPage.getTotal()<=0L){
            shopPage.setTotal((long) records.size());
        }
        return shopPage;
    }

    @Override
    public StoreDetailVO storeDetail(StoreDetailQuery query) {
        if (query.getShopId() == 0) {
            GetStoreSelfOperatedVO selfOperatedSettings = configService.getStoreSelfOperatedSettings();
            if (isSelfOperatedStoreSuspended(selfOperatedSettings)) {
                throw new GlobalException(SHOP_NOT_EXIST);
            }
            StoreDetailVO e = new StoreDetailVO();
            e.setShopId(0);
            e.setShopType(ShopTypeEnum.STORE.getCode());
            e.setShopTitle(configService.getConfigVal(SettingsEnum.STORE_SELF_OPERATED_NAME));
            e.setShopLogo(resolveSelfOperatedStoreLogo(selfOperatedSettings));
            e.setDescription(selfOperatedSettings.getStoreSelfOperatedDescription());
            e.setStatus(selfOperatedSettings.getStoreSelfOperatedStatus());
            e.setShopContactConfig(selfOperatedSettings.getShopContactConfig());
            e.setShopOpenCloseConfig(selfOperatedSettings.getShopOpenCloseConfig());
            e.setShopRegionNames(selfOperatedSettings.getStoreSelfOperatedRegions());
            e.setTipNames(resolveSelfOperatedTipNames(selfOperatedSettings));
            e.setShopSales(Optional.ofNullable(orderItemMapper.getShopSalesByShopId(0)).orElse(0));

            // 与普通门店详情一致：返回当前用户是否已收藏平台自营（shopId=0）
            try {
                CollectShop collectShop = collectShopMapper.selectOne(new LambdaQueryWrapper<CollectShop>()
                        .eq(CollectShop::getShopId, 0)
                        .eq(CollectShop::getUserId, getCurrentUserId()));
                e.setCollectShop(collectShop != null);
            } catch (Exception ignored) {
                // 未登录等场景与下方普通门店分支保持一致，不抛错
            }
            return e;
        }
        Integer shopId = query.getShopId();
        if (shopId != null) {
            Shop shop = this.getById(shopId);
            if (shop == null) {
                throw new GlobalException(SHOP_NOT_EXIST);
            }
            StoreDetailVO storeDetailVO = new StoreDetailVO();
            BeanUtils.copyProperties(shop, storeDetailVO);
            storeDetailVO.setShopStar(shop.getShopStar());
            storeDetailVO.setShopSales(shop.getShopSales());
            storeDetailVO.setShopStarNum(shop.getShopStarNum());
            List<StoreTip> storeTips = storeTipMapper.selectList(new LambdaQueryWrapper<StoreTip>().eq(StoreTip::getShopId, shop.getShopId()).orderByAsc(StoreTip::getSort));
            // tip 被删时 selectById 返回 null → 过滤，避免 getTipName NPE（N5）
            storeDetailVO.setTipNames(storeTips.stream()
                    .map(item -> tipMapper.selectById(item.getTipId()))
                    .filter(Objects::nonNull)
                    .map(tip -> tip.getTipName())
                    .toList());
            // shopStar 保留一位小数
            if (storeDetailVO.getShopStar() != null) {
                if (Optional.ofNullable(storeDetailVO.getShopStarNum()).orElse(0) <= 100) {
                    storeDetailVO.setShopStar(new BigDecimal("4.8"));
                }
                storeDetailVO.setShopStar(storeDetailVO.getShopStar().setScale(1, java.math.RoundingMode.HALF_UP));
            }
            try {
                LambdaQueryWrapper<CollectShop> collectShopQueryWrapper = new LambdaQueryWrapper<>();
                collectShopQueryWrapper.eq(CollectShop::getShopId, shopId);
                collectShopQueryWrapper.eq(CollectShop::getUserId, getCurrentUserId());
                CollectShop collectShop = collectShopMapper.selectOne(collectShopQueryWrapper);
                storeDetailVO.setCollectShop(collectShop != null);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (query.getLatitude() != null && query.getLatitude().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal distance = GpsDistanceUtil.getDistance(query.getLongitude(), query.getLatitude(), shop.getShopLongitude(), shop.getShopLatitude());
                storeDetailVO.setDistance(distance.toString());
                // 配送类型（多选，编号与商品列表 ProductListDTO.deliveryTypes 一致）：含 4 判同城配送范围、含 2 判预约上门范围
                List<Integer> deliveryTypes = query.getDeliveryTypes();
                if (CollUtil.isNotEmpty(deliveryTypes)) {
                    // 同城配送(4)：开启 且 delivery_radius(米)>0 且 距离(米) <= delivery_radius（与商品服务口径一致，按米）
                    if (deliveryTypes.contains(4)) {
                        ShopLocalDeliveryConfig deliveryConfig = shopLocalDeliveryConfigMapper.selectOne(
                                new LambdaQueryWrapper<ShopLocalDeliveryConfig>()
                                        .eq(ShopLocalDeliveryConfig::getShopId, shopId)
                                        .last("LIMIT 1"));
                        // 门店运力二选一：切到第三方运力时自建 enabled 会被置 0，同城配送仍然可用
                        boolean thirdPartyCarrier = false;
                        if (deliveryConfig != null
                                && (Integer.valueOf(1).equals(deliveryConfig.getEnabled())
                                || thirdPartyCarrier)
                                && deliveryConfig.getDeliveryRadius() != null
                                && deliveryConfig.getDeliveryRadius().compareTo(BigDecimal.ZERO) > 0
                                && distance.compareTo(deliveryConfig.getDeliveryRadius()) <= 0) {
                            storeDetailVO.setInLocalDeliveryRange(true);
                        }
                    }
                    // 上门服务/预约上门(2)：开启 且 service_radius(米)>0 且 距离(米) <= service_radius
                    if (deliveryTypes.contains(2)) {
                        ShopHomeServiceConfig homeConfig = shopHomeServiceConfigMapper.selectOne(
                                new LambdaQueryWrapper<ShopHomeServiceConfig>()
                                        .eq(ShopHomeServiceConfig::getShopId, shopId)
                                        .last("LIMIT 1"));
                        if (homeConfig != null
                                && Integer.valueOf(1).equals(homeConfig.getEnabled())
                                && homeConfig.getServiceRadius() != null
                                && homeConfig.getServiceRadius().compareTo(BigDecimal.ZERO) > 0
                                && distance.compareTo(homeConfig.getServiceRadius()) <= 0) {
                            storeDetailVO.setInHomeServiceRange(true);
                        }
                    }
                }
            }
            storeDetailVO.setBusinessLicensePic("");
            storeDetailVO.setBusinessLicensePic(merchantService.getApprovedLicensePic(shop.getMerchantId()));
            return storeDetailVO;
        }
        return null;
    }

    @Override
    public List<StoreListVO> getStoreListByProductId(Integer productId) {
        // 获取所有门店
        List<Shop> shops = this.lambdaQuery().eq(Shop::getShopType, ShopTypeEnum.STORE.getCode()).list();
        // 已分配该商品的门店
        List<StoreListVO> storeList = shops.stream().map(shop -> {
            StoreListVO storeListVO = new StoreListVO();
            // 获取门店分配该商品的数量
            Long count = storeProductMapper.selectCount(Wrappers.lambdaQuery(StoreProduct.class).eq(StoreProduct::getShopId, shop.getShopId()).eq(StoreProduct::getProductId, productId));
            // 如果分配了，就返回对应的门店信息
            if (count > 0) {
                storeListVO.setShopId(shop.getShopId());
                storeListVO.setShopTitle(shop.getShopTitle());
            }
            //评分操作
            return storeListVO;
        }).filter(storeListVO -> storeListVO.getShopId() != null).toList();
        return storeList;
    }

    @Override
    public PickupListVO pickupDetail(Integer shopId) {
        Shop pickup = getById(shopId);
        if (pickup == null) {
            throw new GlobalException("没有此自提点");
        }
        PickupListVO pickupListVO = new PickupListVO();
        BeanUtils.copyProperties(pickup, pickupListVO);
        pickupListVO.setLatitude(pickup.getShopLatitude());
        pickupListVO.setLongitude(pickup.getShopLongitude());
        pickupListVO.setAddress(pickup.getShopDetailedAddress());
        pickupListVO.setShopRegionIds(JSONUtil.toList(pickup.getShopRegionId(), Integer.class));
        pickupListVO.setContactConfigs(JSONUtil.toList(pickup.getShopContactJson(), StoreExtendedDto.ShopContactConfig.class));
        pickupListVO.setShopOpenCloseConfig(JSONUtil.toBean(pickup.getShopOpenCloseJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
        pickupListVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(pickupListVO.getShopRegionIds()));
        return pickupListVO;
    }

    @Override
    public List<Shop> getAllShops() {
        return this.list();
    }

    @Override
    public List<Integer> getAllStoreIds() {
        return this.lambdaQuery().eq(Shop::getShopType, ShopTypeEnum.STORE.getCode()).list().stream().map(Shop::getShopId).toList();
    }

    @Override
    public boolean delShop(OperateDTO operateDTO) {

        String[] shopTypeNames = {"平台", "店铺", "门店", "自提点"};
        Shop shop = this.getById(operateDTO.getId());
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        String shopTypeName = shopTypeNames[shop.getShopType()];
        
        // 检查是否已停止营业（status = 4 表示停业）
        if (!ShopType.PAUSE.getCode().equals(shop.getStatus()) && !Objects.equals(ShopTypeEnum.PICKUP.getCode(), shop.getShopType())) {
            throw new GlobalException(shopTypeName + "需要先停止营业才能删除");
        }
        
        List<Shop> pickUpShops = this.list(Wrappers.lambdaQuery(Shop.class)
                .eq(Shop::getStoreParentId, operateDTO.getId())
                .and(w -> w.isNull(Shop::getIsDelete).or().eq(Shop::getIsDelete, 0)));
        if (CollUtil.isNotEmpty(pickUpShops)) {
            throw new GlobalException("门店下有自提点，请先删除自提点");
        }
        Assert.isTrue(shop.getShopMoney().compareTo(BigDecimal.ZERO) == 0, () -> new GlobalException(shopTypeName + "资金不为0，请先处理店铺资金"));
        Assert.isTrue(shop.getFrozenMoney().compareTo(BigDecimal.ZERO) == 0, () -> new GlobalException(shopTypeName + "冻结资金不为0，请先处理店铺冻结资金"));

        long shopProductCount = productService.count(new LambdaQueryWrapper<>(Product.class).eq(Product::getShopId, operateDTO.getId()));
        Assert.isTrue(shopProductCount == 0, () -> new GlobalException(shopTypeName + "下有商品，请先处理商品"));
        
        // 软删除：设置 is_delete = 1
        shop.setIsDelete(1);
        return this.updateById(shop);
    }
}
