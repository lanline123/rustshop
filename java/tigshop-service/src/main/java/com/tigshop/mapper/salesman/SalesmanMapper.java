// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.query.salesman.CustomerListPageQuery;
import com.tigshop.bean.query.salesman.CustomerApiPageQuery;
import com.tigshop.bean.vo.salesman.CustomerListVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 库存日志映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface SalesmanMapper extends BaseMapper<Salesman> {

    List<CustomerListVO> customerList(Page<Object> page, @Param("filter") CustomerListPageQuery pageQuery);

    List<CustomerListVO> customerApiList(Page<Object> page, @Param("filter") CustomerApiPageQuery pageQuery);

    List<CustomerListVO> customerApiSalesmanList(Page<Object> page, @Param("filter") CustomerApiPageQuery pageQuery);

    CustomerListVO customerApiDetail(@Param("salesmanId") Integer salesmanId, @Param("userId") Integer userId,
                                     @Param("startTime") String startTime, @Param("endTime") String endTime,
                                     @Param("shopId") Integer shopId);

    CustomerListVO customerApiSalesmanDetail(@Param("salesmanId") Integer salesmanId, @Param("userId") Integer userId,
                                             @Param("startTime") String startTime, @Param("endTime") String endTime,
                                             @Param("shopId") Integer shopId);

}
