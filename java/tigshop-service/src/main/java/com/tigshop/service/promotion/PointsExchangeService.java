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

package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.PointsExchangeCreateDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeListDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeUpdateDTO;
import com.tigshop.bean.model.promotion.PointsExchange;
import com.tigshop.bean.vo.promotion.PointsExchangeVO;
import com.tigshop.service.common.BaseService;

/**
 * 积分商品服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PointsExchangeService extends BaseService<PointsExchange> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<PointsExchangeVO> list(PointsExchangeListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    PointsExchangeVO detail(Integer id);

    PointsExchangeVO clientDetail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(PointsExchangeCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(PointsExchangeUpdateDTO updateDTO);

    /**
     * 根据商品ID获取积分商品信息
     *
     * @param productId 商品ID
     * @param skuId     SKU ID
     * @return PointsExchange
     */
    PointsExchange getInfoByProductId(Integer productId, int skuId);
}
