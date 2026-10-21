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
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.promotion.ProductPromotionDTO;
import com.tigshop.bean.dto.promotion.ProductPromotionListDTO;
import com.tigshop.bean.model.promotion.ProductPromotion;
import com.tigshop.bean.query.promotion.product.ConflictPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.ProductPromotionVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.product.ConflictPageVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 满减活动服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ProductPromotionService extends BaseService<ProductPromotion> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ProductPromotionVO> list(ProductPromotionListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ProductPromotionVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ProductPromotionDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ProductPromotionDTO updateDTO);

    /**
     * 更新字段
     */
    boolean updateField(UpdateFieldDTO updateField, String[] productPromotionFields);

//    ListResVO<ProductPromotionVO, ProductPromotionListDTO> conflict(ProductPromotionDTO conflictListDTO);

    /**
     * 获取配置
     * @return map
     */
    Map<Integer, String> getConfig();

    /**
     * 解析价格
     *
     * @param cartList    cartList
     * @param promotionVO 优惠数据
     */
    CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO);

    /**
     * 活动冲突列表
     */
    Page<ConflictPageVO> conflictPage(ConflictPageQuery query);
}
