package com.tigshop.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.Sign;
import com.tigshop.bean.model.promotion.SignInSetting;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.product.ProductInfoVo;
import com.tigshop.bean.vo.user.UserSignInfoVo;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.user.UserSignMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.SignInSettingService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.user.UserSignService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wzh
 */
@Service
@AllArgsConstructor
public class UserSignServiceImpl extends BaseServiceImpl<UserSignMapper, Sign> implements UserSignService {

    private final SignInSettingService signInSettingService;

    private final ProductService productService;

    private final ShopService shopService;

    private final ProductSkuService productSkuService;

    private final UserService userService;
    private final ConfigService configService;

    @Override
    public UserSignInfoVo getUserSignInfoVo() {

        UserSignInfoVo userSignInfoVo = new UserSignInfoVo();
        //1：获取用户id
        Integer userId = SecurityUtils.getCurrentUserId();

        //2：获取签到配置
        // 用户端首页展示顺序与后台配置一致；签到奖励计算仍通过 dayNum 精确匹配，不依赖列表顺序。
        List<SignInSetting> signInSettingList = signInSettingService.lambdaQuery()
                .orderByAsc(SignInSetting::getSortOrder)
                .orderByAsc(SignInSetting::getCreateTime)
                .orderByAsc(SignInSetting::getId)
                .list();
        userSignInfoVo.setDays(signInSettingList);

        //3：获取上次签到记录的次数
        int record = this.getSignCount(userId);
        if (isContinuousSignInEnabled() && isSignInInterrupted(userId)) {
            record = 0;
        }
        userSignInfoVo.setRecord(record);

        //4：检验用户今天是否签到
        int isSign = this.checkUserSignIn(userId);
        userSignInfoVo.setIsSign(isSign);

        //5：获取今日能获取的积分
        int signPoints = this.getSignPoints(signInSettingList, record, isSign);
        userSignInfoVo.setSignPoints(signPoints);

        //6:获取推荐商品信息
        List<ProductInfoVo> productInfoList = this.getProductInfo();
        userSignInfoVo.setRecommendGoods(productInfoList);

        return userSignInfoVo;
    }

    private int getSignCount(Integer userId) {
        Sign sign = this.lambdaQuery()
                .eq(Sign::getUserId, userId)
                .orderByDesc(Sign::getId)
                .last("limit 1")
                .one();
        return sign != null ? sign.getSignNum() : 0;
    }

    @Override
    public void sign() {
        //1：获取用户id
        Integer userId = SecurityUtils.getCurrentUserId();

        //2：检查用户是否已经签到
        int isSign = this.checkUserSignIn(userId);
        Assert.isTrue(isSign == Constants.NO, "今日已经签到");

        //3：获取签到配置
        List<SignInSetting> signInSettingList = signInSettingService.lambdaQuery()
                .orderByAsc(SignInSetting::getDayNum)
                .list();
        int allTotal = signInSettingList.size();

        //4：获取上次签到记录的次数
        int preNum = this.getSignCount(userId);
        if (isContinuousSignInEnabled() && isSignInInterrupted(userId)) {
            preNum = 0;
        }

        //5: 计算当前签到次数
        int signNum = (allTotal > preNum) ? preNum + 1 : 1;

        //6: 执行签到操作
        boolean result = this.addSignIn(userId, signNum, signInSettingList);

        //7：签到结果
        Assert.isTrue(result, "签到失败");
    }

    public boolean addSignIn(Integer userId, Integer signNum, List<SignInSetting> signInSettingList) {
        Sign sign = new Sign();
        sign.setUserId(userId);
        sign.setSignNum(signNum);
        sign.setAddTime(StringUtils.getCurrentTime());

        boolean inserted = this.save(sign);
        if (!inserted) {
            return false;
        }

        // 获取用户当前签到次数
        int record = this.getSignCount(userId);

        // 计算当前应该奖励多少积分
        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        int signPoint = this.getSignPoints(signInSettingList, record, 1);
        if (signPoint > 0) {
            userService.incPoints(signPoint, userId, "签到赠送" + configVal);
        }
        return true;
    }

    private List<ProductInfoVo> getProductInfo() {
        //产品信息
        List<Product> productList = productService.lambdaQuery()
                .eq(Product::getIsHot, Constants.YES)
                .eq(Product::getProductStatus, Constants.YES)
                .eq(Product::getIsDelete, Constants.NO)
                .last("limit 8")
                .list();

        if (CollUtil.isEmpty(productList)){
            return List.of();
        }
        List<Integer> productIds = productList.stream().map(Product::getProductId).toList();

        List<Integer> shopIds = productList.stream().map(Product::getShopId).toList();

        //产品规格信息
        List<ProductSku> productSkuList = productSkuService.lambdaQuery()
                .in(ProductSku::getProductId, productIds)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .list();
        //店铺信息
        List<Shop> shopList = shopService.lambdaQuery().in(Shop::getShopId, shopIds).list();

        Map<Integer, Shop> shopMap = Optional.ofNullable(shopList)
                .filter(CollUtil::isNotEmpty)
                .map(list -> list.stream().collect(Collectors.toMap(Shop::getShopId, shop -> shop)))
                .orElse(Collections.emptyMap());

        Map<Integer, List<ProductSku>> productSkuMap = Optional.ofNullable(productSkuList)
                .filter(CollUtil::isNotEmpty)
                .map(list -> list.stream().collect(Collectors.groupingBy(ProductSku::getProductId)))
                .orElse(Collections.emptyMap());

        return productList.stream()
                .map(product -> new ProductInfoVo(
                        product,
                        productSkuMap.getOrDefault(product.getProductId(), List.of()),
                        shopMap.getOrDefault(product.getShopId(), null)
                ))
                .toList();
    }

    private int checkUserSignIn(Integer userId) {
        if (userId == null || userId == 0) {
            return 0;
        }
        // 获取今天零点的秒级时间戳
        long nowSecond = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond();

        Sign sign = this.lambdaQuery()
                .eq(Sign::getUserId, userId)
                .gt(Sign::getAddTime, nowSecond)
                .select(Sign::getId)
                .last("limit 1")
                .one();

        return sign != null ? 1 : 0;
    }

    private boolean isContinuousSignInEnabled() {
        return "1".equals(configService.getConfigVal(SettingsEnum.CONTINUOUS_SIGN_IN));
    }

    private boolean isSignInInterrupted(Integer userId) {
        Sign latest = this.lambdaQuery()
                .eq(Sign::getUserId, userId)
                .orderByDesc(Sign::getId)
                .last("limit 1")
                .one();
        if (latest == null || latest.getAddTime() == null) {
            return false;
        }
        LocalDate latestDate = Instant.ofEpochSecond(latest.getAddTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return latestDate.isBefore(LocalDate.now(ZoneId.systemDefault()).minusDays(1));
    }

    private int getSignPoints(List<SignInSetting> signInSettingList, int count, int userSign) {
        if (userSign == 0) {
            count += 1;
        }
        int signPoints = 0;
        for (SignInSetting setting : signInSettingList) {
            if (setting.getDayNum() == count) {
                signPoints = setting.getPoints();
                break;
            }
        }
        return signPoints;
    }
}
