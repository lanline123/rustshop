// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.query.shop.StorePageQuery;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺表映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface ShopMapper extends BaseMapper<Shop> {
    Page<StoreListVO> storeListByLocation(@Param("page") Page<Shop> page,
                                          @Param("query") StorePageQuery pageQuery);

    Page<StoreListVO> storeListByRegion(@Param("page") Page<Shop> page,
                                        @Param("query") StorePageQuery pageQuery);

    Page<PickupListVO> pickupListByLocation(@Param("page") Page<Shop> page,
                                            @Param("query") PickupPageQuery query);

    List<Shop> pickupByLocation(@Param("query") PickupPageQuery query);

    /**
     * 查询店铺详情（包含计算后的评分）
     * 评价数量 < 100 时返回默认4.8，否则计算平均分
     *
     * @param shopId 店铺ID
     * @return 包含计算评分的店铺信息
     */
    Shop selectByIdWithCalculatedStar(@Param("shopId") Integer shopId);


}
