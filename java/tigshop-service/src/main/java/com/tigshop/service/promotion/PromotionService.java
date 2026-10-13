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
import com.tigshop.bean.dto.promotion.PromotionCreateDTO;
import com.tigshop.bean.dto.promotion.PromotionListDTO;
import com.tigshop.bean.dto.promotion.PromotionUpdateDTO;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.vo.promotion.PromotionCountVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.service.common.BaseService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 营销管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PromotionService extends BaseService<Promotion> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<PromotionVO> list(PromotionListDTO listDTO);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(PromotionCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(PromotionUpdateDTO updateDTO);

    /**
     * 获取活动数量
     * @return map
     */
    PromotionCountVO getCount();

    /**
     * 获取所有可用的营销活动
     *
     * @param shopId 店铺ID
     * @return List<Promotion>
     */
    List<PromotionVO> getAllAvailablePromotion(Integer shopId,Integer userId);

    List<PromotionVO> getAllAvailablePromotion(Integer shopId);

    /**
     * 按多个店铺一次性取可用营销活动。
     * <p>
     * 列表页等需要跨店铺装配促销的场景用本方法，把「每店铺一次全量查询 + 逐活动查券/查会员」
     * 收敛成常量条 SQL；单店铺调用 {@link #getAllAvailablePromotion(Integer, Integer)} 即可，
     * 两者共用同一套装配逻辑。
     * <p>
     * 注意与 {@code shopId == null} 语义的区别：单店方法传 null 表示「不限店铺、取全平台活动」，
     * 而本方法只按传入的店铺过滤，null 元素会被忽略。
     *
     * @param shopIds 店铺ID集合，null 元素与重复项自动忽略
     * @return 店铺ID → 该店可用活动；入参为空时返回空 Map，无活动的店铺对应空列表
     */
    Map<Integer, List<PromotionVO>> getAllAvailablePromotionByShopIds(Collection<Integer> shopIds, Integer userId);
}
