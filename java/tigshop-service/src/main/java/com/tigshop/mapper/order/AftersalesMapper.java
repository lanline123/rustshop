// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.order;

import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.order.Aftersales;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
public interface AftersalesMapper extends BaseMapper<Aftersales> {
    /**
     * 根据售后单ID和状态查询明细
     * @param aftersaleId 售后单ID
     * @param status 状态
     * @return 总额
     */
    BigDecimal selectTotalAmount(@Param("aftersaleId") Integer aftersaleId,
                                 @Param("status") Integer status);
}
