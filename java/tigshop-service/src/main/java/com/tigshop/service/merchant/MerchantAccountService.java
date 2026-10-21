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

package com.tigshop.service.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.merchant.MerchantAccountCreateDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountListDTO;
import com.tigshop.bean.dto.merchant.MerchantAccountUpdateDTO;
import com.tigshop.bean.model.merchant.MerchantAccount;
import com.tigshop.bean.vo.merchant.MerchantAccountVO;
import com.tigshop.service.common.BaseService;

/**
 * 商家账户表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface MerchantAccountService extends BaseService<MerchantAccount> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<MerchantAccountVO> list(MerchantAccountListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    MerchantAccountVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(MerchantAccountCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(MerchantAccountUpdateDTO updateDTO);

    /**
     * 统计商家数量
     * @param merchantId
     * @param shopId
     * @return
     */

    Integer getCardCount(Integer merchantId, Integer shopId);

    /**
     * 判断当前商户Id
     * @param adminId 后台账号id
     * @param merchantId 商户id
     * @return boolean
     */
    boolean checkMerchantAuth(Integer adminId, Integer merchantId);

}
