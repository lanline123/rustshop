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

package com.tigshop.service.authority;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminLogDTO;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.bean.query.adminlog.AdminLogPageQuery;
import com.tigshop.service.common.BaseService;

/**
 * 管理员日志
 *
 * @author Jayce
 * @create 2024年10月28日 10:30
 */
public interface AdminLogService extends BaseService<AdminLog> {

    /**
     * 创建管理员日志
     *
     * @param adminLog 日志
     * @return boolean
     */
    boolean create(AdminLogDTO adminLog);

    /**
     * 删除管理员日志
     *
     * @param query 查询参数
     * @return ListResVO
     */
    Page<AdminLogDTO> adminLogPage(AdminLogPageQuery query);

    /**
     * 创建日志
     *
     * @param logInfo 日志信息
     * @return boolean
     */
    boolean createByLogInfo(String logInfo);
}
