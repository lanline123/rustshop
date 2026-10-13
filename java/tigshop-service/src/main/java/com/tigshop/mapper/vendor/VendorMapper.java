package com.tigshop.mapper.vendor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.supplychain.VendorSourceTypeEnum;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.mapper.common.BaseMapper;

import java.util.List;

/**
* @author Admin
* @description 针对表【vendor(供应商表)】的数据库操作Mapper
* @createDate 2025-07-04 10:52:58
* @Entity generator.model.Vendor
*/
public interface VendorMapper extends BaseMapper<Vendor> {

    default Vendor selectOnboardedById(Integer vendorId) {
        if (vendorId == null) {
            return null;
        }
        return selectOne(Wrappers.lambdaQuery(Vendor.class)
                .eq(Vendor::getVendorId, vendorId)
                .eq(Vendor::getSourceType, VendorSourceTypeEnum.ONBOARDED.getCode())
                .last("limit 1"));
    }

    default List<Vendor> selectOnboardedList(LambdaQueryWrapper<Vendor> wrapper) {
        wrapper.eq(Vendor::getSourceType, VendorSourceTypeEnum.ONBOARDED.getCode());
        return selectList(wrapper);
    }
}



