// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.TaxRateDTO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.setting.TaxRate;
import com.tigshop.bean.query.setting.TaxRatePageQuery;
import com.tigshop.bean.vo.setting.TaxRateVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.setting.TaxRateMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.setting.TaxRateService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 税率管理服务实现类
 *
 * @author Tigshop团队
 * @create 2026年01月13日
 */
@Service
public class TaxRateServiceImpl extends BaseServiceImpl<TaxRateMapper, TaxRate> implements TaxRateService {

    @Resource
    private RegionService regionService;

    @Override
    public Page<TaxRateVO> list(TaxRatePageQuery query) {
        Page<TaxRate> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<TaxRate> queryWrapper = new LambdaQueryWrapper<>();

        // 根据国家和地区名称模糊搜索
        if (StrUtil.isNotBlank(query.getRegionName())) {
            queryWrapper.like(TaxRate::getRegionName, query.getRegionName());
        }

        // 根据是否生效筛选
        if (query.getIsActive() != null) {
            queryWrapper.eq(TaxRate::getIsActive, query.getIsActive());
        }

        // 根据创建时间范围筛选
        if (StrUtil.isNotBlank(query.getStartTime())) {
            Long startTime = StringUtils.dateToTimestampExample(query.getStartTime());
            queryWrapper.ge(TaxRate::getCreateTime, startTime);
        }
        if (StrUtil.isNotBlank(query.getEndTime())) {
            Long endTime = StringUtils.dateToTimestampExample(query.getEndTime());
            queryWrapper.le(TaxRate::getCreateTime, endTime);
        }

        if (StrUtil.isNotBlank(query.getSortField())) {
            buildSortOrder(page, query.getSortField(), query.getSortOrder());
        } else {
            queryWrapper.orderByDesc(TaxRate::getIsActive);
            queryWrapper.orderByDesc(TaxRate::getCreateTime);
        }

        Page<TaxRate> taxRatePage = this.page(page, queryWrapper);
        List<TaxRateVO> voList = taxRatePage.getRecords().stream().map(taxRate -> {
            TaxRateVO vo = new TaxRateVO();
            BeanUtils.copyProperties(taxRate, vo);
            return vo;
        }).toList();

        return PageUtil.convertPage(taxRatePage, voList);
    }

    @Override
    public TaxRateVO detail(Integer taxRateId) {
        TaxRate taxRate = this.getById(taxRateId);
        if (taxRate == null) {
            throw new GlobalException("税率不存在");
        }
        TaxRateVO vo = new TaxRateVO();
        BeanUtils.copyProperties(taxRate, vo);
        return vo;
    }

    @Override
    public boolean create(TaxRateDTO dto) {
        TaxRate taxRate = new TaxRate();
        BeanUtils.copyProperties(dto, taxRate);

        // 如果没有传入地区名称，根据地区ID获取地区名称
        if (StrUtil.isBlank(dto.getRegionName()) && dto.getRegionId() != null) {
            Region region = regionService.getById(dto.getRegionId());
            if (region != null) {
                taxRate.setRegionName(region.getRegionName());
            }
        }

        // 设置默认值
        if (taxRate.getIsActive() == null) {
            taxRate.setIsActive(Constants.YES);
        }
        taxRate.setCreateTime(StringUtils.getCurrentTime());
        taxRate.setUpdateTime(StringUtils.getCurrentTime());

        return this.save(taxRate);
    }

    @Override
    public boolean update(TaxRateDTO dto) {
        if (dto.getTaxRateId() == null) {
            throw new GlobalException("税率ID不能为空");
        }

        TaxRate taxRate = this.getById(dto.getTaxRateId());
        if (taxRate == null) {
            throw new GlobalException("税率不存在");
        }

        Integer oldRegionId = taxRate.getRegionId();
        BeanUtils.copyProperties(dto, taxRate);

        // 如果地区ID改变了，更新地区名称
        if (dto.getRegionId() != null && !dto.getRegionId().equals(oldRegionId)) {
            Region region = regionService.getById(dto.getRegionId());
            if (region != null) {
                taxRate.setRegionName(region.getRegionName());
            }
        }

        taxRate.setUpdateTime(StringUtils.getCurrentTime());

        return this.updateById(taxRate);
    }

    @Override
    public boolean del(Integer taxRateId) {
        return this.removeById(taxRateId);
    }

    @Override
    public TaxRate getActiveByRegionId(Integer regionId) {
        if (regionId == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<TaxRate>()
                .eq(TaxRate::getRegionId, regionId)
                .eq(TaxRate::getIsActive, Constants.YES)
                .orderByDesc(TaxRate::getUpdateTime)
                .orderByDesc(TaxRate::getCreateTime)
                .orderByDesc(TaxRate::getTaxRateId)
                .last("LIMIT 1"));
    }
}
