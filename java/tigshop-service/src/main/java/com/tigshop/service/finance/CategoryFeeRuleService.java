package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleConflictCheckDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRulePageDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleSaveDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleUpdateStatusDTO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleConflictVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleDetailVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleListVO;

import java.util.List;

public interface CategoryFeeRuleService {

    Page<CategoryFeeRuleListVO> page(CategoryFeeRulePageDTO dto);

    CategoryFeeRuleDetailVO detail(Integer id);

    List<String> save(CategoryFeeRuleSaveDTO dto);

    List<String> update(CategoryFeeRuleSaveDTO dto);

    List<String> updateStatus(CategoryFeeRuleUpdateStatusDTO dto);

    void delete(List<Integer> ids);

    CategoryFeeRuleConflictVO checkConflict(CategoryFeeRuleConflictCheckDTO dto);
}
