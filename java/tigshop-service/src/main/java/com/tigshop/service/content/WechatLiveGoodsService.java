// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.content.WechatLiveGoods;

import java.util.List;

/**
 * 微信直播商品服务
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
public interface WechatLiveGoodsService extends IService<WechatLiveGoods> {

    List<WechatLiveGoods> getGoodsByShopId(Integer shopId);

    /**
     * 根据商品+SKU+店铺维度获取直播商品主表记录
     *
     * @param productId    系统商品ID
     * @param productSkuId 系统SKU ID
     * @param shopId       店铺ID
     * @return 直播商品
     */
    WechatLiveGoods getGoodsByProductIdAndProductSkuIdAndShopId(Long productId, Long productSkuId, Integer shopId);

    /**
     * 根据主键和店铺获取直播商品
     *
     * @param id 主键ID
     * @param shopId 店铺ID
     * @return 直播商品
     */
    WechatLiveGoods getGoodsByIdAndShopId(Long id, Integer shopId);

    /**
     * 根据微信商品ID和店铺获取直播商品
     *
     * @param wechatGoodsId 微信商品ID
     * @param shopId 店铺ID
     * @return 直播商品
     */
    WechatLiveGoods getGoodsByWechatGoodsIdAndShopId(Integer wechatGoodsId, Integer shopId);
}
