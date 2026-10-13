package com.tigshop.mapper.product;

import com.tigshop.bean.model.product.StockWarningSetting;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存预警通知总开关映射类
 *
 * @author Tigshop团队
 */
@Mapper
public interface StockWarningSettingMapper extends BaseMapper<StockWarningSetting> {
}
