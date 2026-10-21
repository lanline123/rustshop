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
import com.tigshop.bean.model.salesman.SalesmanMaterial;
import com.tigshop.bean.model.salesman.SalesmanMaterialCategory;
import com.tigshop.bean.param.salesman.SalesmanMaterialEditParam;
import com.tigshop.bean.param.salesman.SalesmanMaterialSaveParam;
import com.tigshop.bean.query.salesman.SalesmanMaterialApiPageQuery;
import com.tigshop.bean.query.salesman.SalesmanMaterialPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 分销员分组服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SalesmanMaterialService extends BaseService<SalesmanMaterial> {
    /**
     * 列表
     *
     * @param query 查询参数
     * @return ListResVO
     */
    Page<SalesmanMaterialVO> list(SalesmanMaterialPageQuery query);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    SalesmanMaterialVO detail(Integer id);

    /**
     * 创建
     *
     * @param param 创建参数
     * @return boolean
     */
    boolean create(SalesmanMaterialSaveParam param);

    /**
     * 更新
     *
     * @param param 更新参数
     * @return boolean
     */
    boolean update(SalesmanMaterialEditParam param);

    List<SalesmanMaterialCategory> getAllCategory();

    /**
     * api: 列表
     */
    Page<SalesmanMaterialVO> apiPage(SalesmanMaterialApiPageQuery pageQuery);
}
