package com.tigshop.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.CollectShopListDTO;
import com.tigshop.bean.model.shop.CollectShop;
import com.tigshop.bean.vo.user.CollectShopVO;
import com.tigshop.service.common.BaseService;

/**
 * 会员收藏店铺的记录列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface CollectShopService extends BaseService<CollectShop> {
    /**
     * 收藏
     * @return boolean
     *
     */
   boolean collect(Integer shopId);

    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<CollectShopVO> list(CollectShopListDTO listDTO);

    /**
     * 获取收藏店铺数量
     * @param userId
     * @return
     */
    Long getCollectShopCount(Integer userId);

}
