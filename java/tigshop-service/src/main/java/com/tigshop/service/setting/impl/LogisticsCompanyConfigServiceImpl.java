package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyConfigUpdateDTO;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.model.setting.LogisticsCompanyConfig;
import com.tigshop.bean.vo.setting.LogisticsCompanyConfigVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.LogisticsCompanyConfigMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.LogisticsCompanyConfigService;
import com.tigshop.service.setting.LogisticsCompanyService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LogisticsCompanyConfigServiceImpl extends BaseServiceImpl<LogisticsCompanyConfigMapper, LogisticsCompanyConfig> implements LogisticsCompanyConfigService {

    @Resource
    private AdminLogService adminLogService;

    @Lazy
    @Resource
    private LogisticsCompanyService logisticsCompanyService;

    @Override
    public Page<LogisticsCompanyConfigVO> list(LogisticsCompanyConfigListDTO listDTO) {
        Page<LogisticsCompanyConfig> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        LambdaQueryWrapper<LogisticsCompanyConfig> queryWrapper = new LambdaQueryWrapper<>();

        int realShopId = getShopId() != null ? getShopId() : 0;
        int realVendorId = getVendorId() != null ? getVendorId() : 0;
        queryWrapper.eq(LogisticsCompanyConfig::getShopId, realShopId);
        queryWrapper.eq(LogisticsCompanyConfig::getVendorId, realVendorId);

        if (listDTO.getIsEnabled() != null) {
            queryWrapper.eq(LogisticsCompanyConfig::getIsEnabled, listDTO.getIsEnabled());
        }

        String keyword = listDTO.getKeyword();
        Integer providerType = listDTO.getProviderType();
        if (StrUtil.isNotEmpty(keyword) || Integer.valueOf(1).equals(providerType)
                || Integer.valueOf(2).equals(providerType)) {
            LambdaQueryWrapper<LogisticsCompany> companyQueryWrapper = new LambdaQueryWrapper<>();
            if (StrUtil.isNotEmpty(keyword)) {
                companyQueryWrapper.like(LogisticsCompany::getLogisticsName, keyword);
            }
            LogisticsCompanyServiceImpl.applyProviderFilter(companyQueryWrapper, providerType);
            List<Integer> matchedIds = logisticsCompanyService.list(companyQueryWrapper)
                    .stream()
                    .map(LogisticsCompany::getLogisticsId)
                    .toList();
            if (matchedIds.isEmpty()) {
                return PageUtil.convertPage(page, List.of());
            }
            queryWrapper.in(LogisticsCompanyConfig::getLogisticsId, matchedIds);
        }

        queryWrapper.orderByDesc(LogisticsCompanyConfig::getIsDefault);
        queryWrapper.orderByDesc(LogisticsCompanyConfig::getIsEnabled);

        Page<LogisticsCompanyConfig> configPage = this.page(page, queryWrapper);
        List<LogisticsCompanyConfig> records = configPage.getRecords();
        if (records.isEmpty()) {
            return PageUtil.convertPage(configPage, List.of());
        }

        List<Integer> logisticsIds = records.stream()
                .map(LogisticsCompanyConfig::getLogisticsId)
                .distinct()
                .toList();
        Map<Integer, LogisticsCompany> companyMap = logisticsCompanyService.listByIds(logisticsIds)
                .stream()
                .collect(Collectors.toMap(LogisticsCompany::getLogisticsId, Function.identity(), (a, b) -> a));

        List<LogisticsCompanyConfigVO> voList = records.stream().map(config -> {
            LogisticsCompanyConfigVO vo = new LogisticsCompanyConfigVO();
            BeanUtils.copyProperties(config, vo);
            LogisticsCompany company = companyMap.get(config.getLogisticsId());
            if (company != null) {
                vo.setLogisticsName(company.getLogisticsName());
                vo.setLogisticsCode(company.getLogisticsCode());
                vo.setKuaidi100Code(company.getKuaidi100Code());
                vo.setLogisticsDesc(company.getLogisticsDesc());
            }
            return vo;
        }).toList();

        return PageUtil.convertPage(configPage, voList);
    }

    @Override
    public LogisticsCompanyConfigVO detail(Integer id) {
        if (id == null) {
            return null;
        }
        LogisticsCompanyConfig config = this.getById(id);
        if (config == null) {
            return null;
        }
        LogisticsCompanyConfigVO vo = new LogisticsCompanyConfigVO();
        BeanUtils.copyProperties(config, vo);
        LogisticsCompany company = logisticsCompanyService.getById(config.getLogisticsId());
        if (company != null) {
            vo.setLogisticsName(company.getLogisticsName());
            vo.setLogisticsCode(company.getLogisticsCode());
            vo.setKuaidi100Code(company.getKuaidi100Code());
            vo.setLogisticsDesc(company.getLogisticsDesc());
        }
        return vo;
    }

    @Override
    public boolean create(LogisticsCompanyConfigCreateDTO createDTO) {
        if (createDTO == null) {
            return false;
        }
        Assert.notNull(createDTO.getLogisticsId(), "物流公司不能为空");
        LogisticsCompany company = logisticsCompanyService.getById(createDTO.getLogisticsId());
        Assert.notNull(company, "物流公司不存在");

        Integer shopId = getShopId();
        Integer vendorId = getVendorId();
        int realShopId = shopId != null ? shopId : 0;
        int realVendorId = vendorId != null ? vendorId : 0;

        long existCount = this.lambdaQuery()
                .eq(LogisticsCompanyConfig::getShopId, realShopId)
                .eq(LogisticsCompanyConfig::getVendorId, realVendorId)
                .eq(LogisticsCompanyConfig::getLogisticsId, createDTO.getLogisticsId())
                .count();
        if (existCount > 0) {
            throw new GlobalException("该物流公司已绑定，请勿重复添加");
        }

        LogisticsCompanyConfig config = new LogisticsCompanyConfig();
        BeanUtils.copyProperties(createDTO, config);
        config.setShopId(realShopId);
        config.setVendorId(realVendorId);
        if (config.getSortOrder() == null) {
            config.setSortOrder(50);
        }
        if (config.getIsEnabled() == null) {
            config.setIsEnabled(1);
        }
        config.setIsDefault(0);

        adminLogService.createByLogInfo("新增物流公司配置:" + company.getLogisticsName());
        return this.save(config);
    }

    @Override
    public boolean update(LogisticsCompanyConfigUpdateDTO updateDTO) {
        if (updateDTO == null) {
            return false;
        }
        LogisticsCompanyConfig existing = this.getById(updateDTO.getId());
        Assert.notNull(existing, "配置记录不存在");

        if (updateDTO.getLogisticsId() != null && !updateDTO.getLogisticsId().equals(existing.getLogisticsId())) {
            Integer shopId = getShopId();
            Integer vendorId = getVendorId();
            int realShopId = shopId != null ? shopId : 0;
            int realVendorId = vendorId != null ? vendorId : 0;

            long existCount = this.lambdaQuery()
                    .eq(LogisticsCompanyConfig::getShopId, realShopId)
                    .eq(LogisticsCompanyConfig::getVendorId, realVendorId)
                    .eq(LogisticsCompanyConfig::getLogisticsId, updateDTO.getLogisticsId())
                    .ne(LogisticsCompanyConfig::getConfigId, updateDTO.getId())
                    .count();
            if (existCount > 0) {
                throw new GlobalException("该物流公司已绑定，请勿重复添加");
            }
        }

        LogisticsCompanyConfig config = new LogisticsCompanyConfig();
        BeanUtils.copyProperties(updateDTO, config);
        config.setConfigId(updateDTO.getId());

        LogisticsCompany company = logisticsCompanyService.getById(
                updateDTO.getLogisticsId() != null ? updateDTO.getLogisticsId() : existing.getLogisticsId());
        String companyName = company != null ? company.getLogisticsName() : String.valueOf(updateDTO.getId());
        adminLogService.createByLogInfo("更新物流公司配置:" + companyName);
        return this.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Integer id) {
        LogisticsCompanyConfig config = this.getById(id);
        Assert.notNull(config, "配置记录不存在");

        this.lambdaUpdate()
                .eq(LogisticsCompanyConfig::getShopId, config.getShopId())
                .eq(LogisticsCompanyConfig::getVendorId, config.getVendorId())
                .set(LogisticsCompanyConfig::getIsDefault, 0)
                .update();

        this.lambdaUpdate()
                .eq(LogisticsCompanyConfig::getConfigId, id)
                .set(LogisticsCompanyConfig::getIsDefault, 1)
                .update();

        LogisticsCompany company = logisticsCompanyService.getById(config.getLogisticsId());
        String companyName = company != null ? company.getLogisticsName() : String.valueOf(id);
        adminLogService.createByLogInfo("设置默认物流公司:" + companyName);
    }
}
