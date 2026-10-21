// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.service.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.content.WechatLiveGoods;
import com.tigshop.mapper.content.WechatLiveGoodsMapper;
import com.tigshop.service.content.WechatLiveGoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 微信直播商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Service
public class WechatLiveGoodsServiceImpl extends ServiceImpl<WechatLiveGoodsMapper, WechatLiveGoods> implements WechatLiveGoodsService {

    @Override
    public List<WechatLiveGoods> getGoodsByShopId(Integer shopId) {
        return this.list(new LambdaQueryWrapper<WechatLiveGoods>()
                .eq(WechatLiveGoods::getShopId, shopId)
                .orderByDesc(WechatLiveGoods::getId));
    }

    @Override
    public WechatLiveGoods getGoodsByProductIdAndProductSkuIdAndShopId(Long productId, Long productSkuId, Integer shopId) {
        return this.lambdaQuery()
                .eq(WechatLiveGoods::getProductId, productId)
                .eq(WechatLiveGoods::getProductSkuId, productSkuId)
                .eq(WechatLiveGoods::getShopId, shopId)
                .orderByDesc(WechatLiveGoods::getId)
                .last("limit 1")
                .one();
    }

    @Override
    public WechatLiveGoods getGoodsByIdAndShopId(Long id, Integer shopId) {
        return this.lambdaQuery()
                .eq(WechatLiveGoods::getId, id)
                .eq(WechatLiveGoods::getShopId, shopId)
                .last("limit 1")
                .one();
    }

    @Override
    public WechatLiveGoods getGoodsByWechatGoodsIdAndShopId(Integer wechatGoodsId, Integer shopId) {
        return this.lambdaQuery()
                .eq(WechatLiveGoods::getWechatGoodsId, wechatGoodsId)
                .eq(WechatLiveGoods::getShopId, shopId)
                .orderByDesc(WechatLiveGoods::getId)
                .last("limit 1")
                .one();
    }
}
