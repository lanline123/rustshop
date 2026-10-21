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

import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.shop.PickupService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class PickupServiceImpl extends BaseServiceImpl<ShopMapper, Shop> implements PickupService {

    @Resource
    private RegionService regionService;

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
}
