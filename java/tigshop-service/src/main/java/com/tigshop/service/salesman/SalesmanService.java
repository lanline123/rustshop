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

package com.tigshop.service.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.salesman.SalesmanStatisticalDetailPageQuery;
import com.tigshop.bean.dto.user.UserDTO;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.param.salesman.SalesmanEditParam;
import com.tigshop.bean.param.salesman.SalesmanSaveParam;
import com.tigshop.bean.query.salesman.CustomerListPageQuery;
import com.tigshop.bean.query.salesman.CustomerApiPageQuery;
import com.tigshop.bean.query.salesman.SalesmanPageQuery;
import com.tigshop.bean.query.salesman.SalesmanRankingPageQuery;
import com.tigshop.bean.param.salesman.CustomerRemarkUpdateParam;
import com.tigshop.bean.vo.salesman.*;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 分销员接口
 *
 * @author kidd
 * @create 2025/6/21
 */
public interface SalesmanService extends BaseService<Salesman> {

    /**
     * 列表
     */
    Page<SalesmanPageVO> list(SalesmanPageQuery listDTO);

    /**
     * 详情
     */
    SalesmanDetailVO detail(Integer id);

    /**
     * 创建
     */
    void create(SalesmanSaveParam param);

    /**
     * 更新
     */
    void update(SalesmanEditParam param);

    /**
     * 统计明细
     */
    Page<SalesmanStatisticalVO> statistical(SalesmanStatisticalDetailPageQuery pageQuery, HttpServletResponse response);

    /**
     * 佣金详情
     */
    CommissionDetailsVO commissionDetails(Integer salesmanId);

    /**
     * 排行榜
     */
    Page<SalesmanRankingVO> ranking(SalesmanRankingPageQuery pageQuery);

    /**
     * 详情
     */
    SalesmanUserDetailVO detailForApi();

    /**
     * 获取分销员
     */
    SalesmanVO getSalesman(Integer id);

    /**
     * 根据手机号或者用户名查询用户
     *
     * @param search 手机号或者用户名
     * @return UserDTO
     */
    UserDTO getUserByMobileOrUsername(String search);



    /**
     * 分销员下拉列表
     *
     * @param nickname
     * @return
     */
    Page<SalesmanSelectVO> salesmanList(String nickname);

    /**
     * 客户数量
     *
     * @param pageQuery
     * @return
     */
    Page<CustomerListVO> customerList(CustomerListPageQuery pageQuery);

    Page<CustomerListVO> customerApiList(CustomerApiPageQuery pageQuery);

    CustomerDetailVO customerApiDetail(Integer salesmanId, Integer userId, String startTime, String endTime, Integer shopId);

    /**
     * 更新当前分销员与客户绑定关系上的备注。
     */
    void updateCustomerRemark(Integer salesmanId, CustomerRemarkUpdateParam param);
}

