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
import com.tigshop.bean.model.salesman.SalesmanContent;
import com.tigshop.bean.param.salesman.SalesmanContentEditParam;
import com.tigshop.bean.param.salesman.SalesmanContentSaveParam;
import com.tigshop.bean.query.salesman.SalesmanContentPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanContentVO;
import com.tigshop.service.common.BaseService;

/**
 * 分销内容管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SalesmanContentService extends BaseService<SalesmanContent> {
    /**
     * 列表
     *
     * @param query 查询参数
     * @return ListResVO
     */
    Page<SalesmanContentVO> list(SalesmanContentPageQuery query);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    SalesmanContentVO detail(Integer id);

    /**
     * 创建
     */
    void create(SalesmanContentSaveParam param);

    /**
     * 更新
     */
    void update(SalesmanContentEditParam param);

}
