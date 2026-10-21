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

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.ShippingTypeCreateDTO;
import com.tigshop.bean.dto.setting.ShippingTypeListDTO;
import com.tigshop.bean.dto.setting.ShippingTypeUpdateDTO;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.model.setting.ShippingType;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTypeVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.ShippingTypeMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.LogisticsCompanyService;
import com.tigshop.service.setting.ShippingTypeService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 配送类型服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ShippingTypeServiceImpl extends BaseServiceImpl<ShippingTypeMapper, ShippingType> implements ShippingTypeService {
    @Resource
    LogisticsCompanyService logisticsCompanyService;
    @Resource
    AdminLogService adminLogService;

    @Override
    public Page<ShippingTypeVO> list(ShippingTypeListDTO listDTO) {
        // 分页
        Page<ShippingType> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ShippingType> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 根据店铺查询
        Integer shopId = getShopId();
        queryWrapper.eq(ShippingType::getShopId, shopId);

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ShippingType::getShippingTypeName, keyword);
        }
        // 执行查询
        Page<ShippingType> shippingTypePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShippingType> shippingTypePageRecords = shippingTypePage.getRecords();
        // 转换为VO
        List<ShippingTypeVO> shippingTypeVOList = shippingTypePageRecords.stream()
                .map(shippingType -> {
                    ShippingTypeVO shippingTypeVO = new ShippingTypeVO();
                    BeanUtils.copyProperties(shippingType, shippingTypeVO);
                    //查询物流公司名称
                    LogisticsCompany logisticsCompany = logisticsCompanyService.
                            getLogisticsCompanyById(shippingType.getShippingDefaultId());
                    if (logisticsCompany != null) {
                        shippingTypeVO.setLogisticsName(logisticsCompany.getLogisticsName());
                    }
                    return shippingTypeVO;
                }).toList();
        return PageUtil.convertPage(shippingTypePage, shippingTypeVOList);
    }

    @Override
    public ShippingTypeVO detail(Integer id) {
        if (id != null) {
            ShippingType shippingType = this.getById(id);
            ShippingTypeVO shippingTypeVO = new ShippingTypeVO();
            BeanUtils.copyProperties(shippingType, shippingTypeVO);
            return shippingTypeVO;
        }
        return null;
    }

    @Override
    public boolean create(ShippingTypeCreateDTO createDTO) {
        if (createDTO != null) {
            ShippingType shippingType = new ShippingType();
            BeanUtils.copyProperties(createDTO, shippingType);
            shippingType.setShopId(getShopId());
            adminLogService.createByLogInfo("新增配送类型:" + createDTO.getShippingTypeName());
            return this.save(shippingType);
        }
        return false;
    }

    @Override
    public boolean update(ShippingTypeUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShippingType shippingType = new ShippingType();
            BeanUtils.copyProperties(updateDTO, shippingType);
            shippingType.setShippingTypeId(updateDTO.getId());
            adminLogService.createByLogInfo("修改配送类型:" + updateDTO.getShippingTypeName());
            return this.updateById(shippingType);
        }
        return false;
    }

    @Override
    public List<ShippingTypeVO> getShippingTypeListByShopId(Integer shopId) {
        List<ShippingType> shippingTypeList = new ArrayList<>();
        shippingTypeList.add(new ShippingType(1L));

        return shippingTypeList.stream()
                .map(shippingType -> {
                    ShippingTypeVO shippingTypeVO = new ShippingTypeVO();
                    BeanUtils.copyProperties(shippingType, shippingTypeVO);
                    return shippingTypeVO;
                }).toList();
    }
}
