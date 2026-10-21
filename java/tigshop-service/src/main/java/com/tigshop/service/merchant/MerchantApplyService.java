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
import com.tigshop.bean.dto.merchant.MerchantApplyCreateDTO;
import com.tigshop.bean.dto.merchant.MerchantApplyUpdateDTO;
import com.tigshop.bean.model.merchant.MerchantApply;
import com.tigshop.bean.param.merchant.MerchantApplyApplyParam;
import com.tigshop.bean.param.merchant.MerchantApplyAuditParam;
import com.tigshop.bean.query.merchant.MerchantApplyListPageQuery;
import com.tigshop.bean.vo.merchant.MerchantApplyDetailVO;
import com.tigshop.bean.vo.merchant.MerchantApplyVO;
import com.tigshop.bean.vo.merchant.MerchantMyApplyVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 入驻申请服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface MerchantApplyService extends BaseService<MerchantApply> {

    /**
     * 列表
     */
    Page<MerchantApplyVO> list(MerchantApplyListPageQuery pageQuery);

    /**
     * 详情
     */
    MerchantApplyVO detail(Integer id);

    /**
     * 创建
     */
    boolean create(MerchantApplyCreateDTO createDTO);

    /**
     * 更新
     */
    boolean update(MerchantApplyUpdateDTO updateDTO);

    /**
     * 审核操作
     */
    void audit(MerchantApplyAuditParam param);

    /**
     * 配置
     */
    List<Map<String, String>> config();

    /**
     * 我的申请
     */
    MerchantMyApplyVO myApply();

    /**
     * 申请详情
     */
    MerchantApplyDetailVO applyDetail(Integer id);

    /**
     * 申请入驻
     */
    MerchantApplyDetailVO apply(MerchantApplyApplyParam param);
}
