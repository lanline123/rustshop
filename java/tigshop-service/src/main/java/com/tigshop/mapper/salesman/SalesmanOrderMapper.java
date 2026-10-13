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
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.query.salesman.CustomerTransactionPageQuery;
import com.tigshop.bean.query.salesman.SalesmanRankingPageQuery;
import com.tigshop.bean.vo.salesman.CustomerTransactionVO;
import com.tigshop.bean.vo.salesman.SalesmanRankingVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface SalesmanOrderMapper extends BaseMapper<SalesmanOrder> {
    /**
     * 成交客户列表
     *
     * @param page
     * @param pageQuery
     * @return
     */
    List<CustomerTransactionVO> listCustomerTransaction(Page<?> page, @Param("filter") CustomerTransactionPageQuery pageQuery);

    /**
     * 分销员排行
     */
    Page<SalesmanRankingVO> ranking(@Param("page") Page<SalesmanRankingVO> page, @Param("pageQuery") SalesmanRankingPageQuery pageQuery);
}
