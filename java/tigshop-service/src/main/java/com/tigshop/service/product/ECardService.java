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

package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.product.ECardSaveParam;
import com.tigshop.bean.query.product.ECardPageQuery;
import com.tigshop.bean.param.product.ECardEditParam;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.vo.product.ECardItemVO;
import com.tigshop.bean.vo.product.ECardVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 电子卡券服务
 *
 * @author kidd
 * @create 2025/7/2
 */
public interface ECardService extends BaseService<ECard> {

    /**
     * 列表
     */
    Page<ECardVO> list(ECardPageQuery pageQuery);

    /**
     * 详情
     */
    ECardVO detail(Integer id);

    /**
     * 创建
     */
    void create(ECardSaveParam param);

    /**
     * 更新
     */
    void update(ECardEditParam param);

    /**
     * 根据订单商品ID获取电子卡券
     *
     * @param orderItemId
     * @return
     */
    List<ECardItemVO> getCardByOrderItemId(Integer orderItemId);

    /**
     * 根据订单ID获取电子卡券
     *
     * @param orderId
     * @return
     */
    boolean getCardByOrderId(Integer orderId);

    /**
     * 根据卡券组ID获取新卡券
     *
     * @param cardGroupId
     * @param quantity
     * @return
     */
    List<ECard> getNewCardByGroupId(Integer cardGroupId, Integer quantity);
}
