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
import com.tigshop.bean.param.salesman.SalesmanGroupSaveParam;
import com.tigshop.bean.param.salesman.SalesmanGroupEditParam;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.query.salesman.SalesmanGroupPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanGroupVO;
import com.tigshop.service.common.BaseService;

/**
 * 分销员分组接口
 *
 * @author kidd
 * @since 2025/06/21
 */
public interface SalesmanGroupService extends BaseService<SalesmanGroup> {

    /**
     * 列表
     */
    Page<SalesmanGroupVO> list(SalesmanGroupPageQuery pageQuery);

    /**
     * 详情
     */
    SalesmanGroupVO detail(Integer id);

    /**
     * 创建
     */
    void create(SalesmanGroupSaveParam param);

    /**
     * 更新
     */
    void update(SalesmanGroupEditParam param);

}
