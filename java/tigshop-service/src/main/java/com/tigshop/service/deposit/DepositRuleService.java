package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositRuleDTO;
import com.tigshop.bean.dto.deposit.DepositRuleListDTO;
import com.tigshop.bean.dto.deposit.DepositRuleStatusDTO;
import com.tigshop.bean.model.deposit.ShopDepositRule;
import com.tigshop.bean.vo.deposit.DepositRuleVO;
import com.tigshop.service.common.BaseService;

/**
 * 店铺保证金规则服务。
 *
 * <p>规则维度：主体类型(个人/企业) × 店铺类型(店铺/供应商) × 多类目 × 生效期。
 * 同一(主体×店型×类目)维度下生效时间区间不可重叠，新增/编辑时校验。</p>
 *
 * @author Tigshop团队
 */
public interface DepositRuleService extends BaseService<ShopDepositRule> {

    /**
     * 分页列表（筛选：规则名称、状态、生效时间区间）。
     */
    Page<DepositRuleVO> list(DepositRuleListDTO listDTO);

    /**
     * 规则详情（含已选类目ID列表）。
     */
    DepositRuleVO detail(Integer id);

    /**
     * 创建规则（含类目关联写入 + 时间重叠校验）。
     */
    void create(DepositRuleDTO dto);

    /**
     * 更新规则（类目关联先删后插 + 时间重叠校验）。
     */
    void update(DepositRuleDTO dto);

    /**
     * 软删除（is_delete=1）。覆盖 BaseService.del 的物理删除语义。
     */
    @Override
    boolean del(Integer id);

    /**
     * 启用/停用。
     */
    void changeStatus(DepositRuleStatusDTO dto);
}
