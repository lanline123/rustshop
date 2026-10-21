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

package com.tigshop.adminapi.controller.authority;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminLogDTO;
import com.tigshop.bean.query.adminlog.AdminLogPageQuery;
import com.tigshop.service.authority.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员日志
 *
 * @author Jayce
 * @create 2024年10月28日 10:57
 */
@RestController
@Tag(name = "管理员日志")
@RequestMapping("/adminapi/authority/adminLog")
public class AdminLogController {
    @Resource
    private AdminLogService adminLogService;

    @GetMapping("/list")
    @Operation(summary = "管理员日志列表")
    @PreAuthorize("@pms.hasPermission('adminLogManage','EmployeeLogManagement')")
    public Page<AdminLogDTO> page(AdminLogPageQuery query) {
        return adminLogService.adminLogPage(query);
    }
}
