package com.tigshop.service.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigUpdateDTO;
import com.tigshop.bean.model.setting.LogisticsCompanyConfig;
import com.tigshop.bean.vo.setting.LogisticsCompanyConfigVO;
import com.tigshop.service.common.BaseService;

public interface LogisticsCompanyConfigService extends BaseService<LogisticsCompanyConfig> {

    Page<LogisticsCompanyConfigVO> list(LogisticsCompanyConfigListDTO listDTO);

    LogisticsCompanyConfigVO detail(Integer id);

    boolean create(LogisticsCompanyConfigCreateDTO createDTO);

    boolean update(LogisticsCompanyConfigUpdateDTO updateDTO);

    void setDefault(Integer id);
}
