package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.param.finance.SubMerchantBindParam;
import com.tigshop.bean.query.finance.SubMerchantPageQuery;
import com.tigshop.bean.vo.finance.SubMerchantVO;
import com.tigshop.service.common.BaseService;

/**
 * 子商户 Service
 *
 * @author Tigshop团队
 */
public interface SubMerchantService extends BaseService<SubMerchant> {

    /**
     * 子商户分页列表
     */
    Page<SubMerchantVO> listPage(SubMerchantPageQuery query);

    /**
     * 手动绑定子商户
     */
    void bind(SubMerchantBindParam param);

    /**
     * 解绑（冻结）子商户
     */
    void unbind(Long id);

    /**
     * 根据店铺ID与支付渠道查询有效子商户号，无则返回 null
     */
    SubMerchant getActiveByShop(Long shopId, String payChannel);
}
