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
import com.tigshop.bean.dto.salesman.SalesmanProductAnalysisDTO;
import com.tigshop.bean.model.salesman.SalesmanProduct;
import com.tigshop.bean.param.salesman.SalesmanProductEditParam;
import com.tigshop.bean.param.salesman.SalesmanProductSaveParam;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanProductDetailVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.bean.vo.salesman.SalesmanProductVO;
import com.tigshop.service.common.BaseService;

import java.util.Map;

/**
 * 分销商品服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SalesmanProductService extends BaseService<SalesmanProduct> {

    /**
     * 列表
     */
    Page<SalesmanProductListVO> list(SalesmanProductPageQuery pageQuery);

    /**
     * 根据商品ID获取分销商品
     */
    SalesmanProductVO detailByProductId(Integer id);

    /**
     * 创建
     */
    void create(SalesmanProductSaveParam param);

    /**
     * 更新
     */
    void update(SalesmanProductEditParam param);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    SalesmanProductVO detail(Integer id);

    /**
     * 分销商品分析
     *
     * @param analysisDTO 分析参数
     */
    Page<Map<String, Object>> analysis(SalesmanProductAnalysisDTO analysisDTO);

    /**
     * 客户端分销商品详情
     */
    SalesmanProductDetailVO detailClient(Integer productId);
}
