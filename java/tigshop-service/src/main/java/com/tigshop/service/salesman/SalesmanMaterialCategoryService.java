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
import com.tigshop.bean.model.salesman.SalesmanMaterialCategory;
import com.tigshop.bean.param.salesman.SalesmanMaterialCategoryEditParam;
import com.tigshop.bean.param.salesman.SalesmanMaterialCategorySaveParam;
import com.tigshop.bean.query.salesman.SalesmanMaterialCategoryPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialCategoryVO;
import com.tigshop.service.common.BaseService;

/**
 * 素材分类服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SalesmanMaterialCategoryService extends BaseService<SalesmanMaterialCategory> {
    /**
     * 列表
     *
     * @param query 查询参数
     * @return ListResVO
     */
    Page<SalesmanMaterialCategoryVO> list(SalesmanMaterialCategoryPageQuery query);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    SalesmanMaterialCategoryVO detail(Integer id);

    /**
     * 创建
     *
     * @param param 创建参数
     * @return boolean
     */
    boolean create(SalesmanMaterialCategorySaveParam param);

    /**
     * 更新
     *
     * @param param 更新参数
     * @return boolean
     */
    boolean update(SalesmanMaterialCategoryEditParam param);

    /**
     * 删除
     * @param id
     */
    void delete(Integer id);
}
