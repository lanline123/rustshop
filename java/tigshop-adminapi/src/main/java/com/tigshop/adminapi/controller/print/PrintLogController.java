// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.print;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.print.PrintLogPageQuery;
import com.tigshop.bean.vo.print.PrintLogPageVO;
import com.tigshop.service.print.PrintLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 打印日志管理
 * <p>
 * 兼容设置栏目与收银台设置的小票打印权限码。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@RestController
@RequestMapping("/adminapi/print/printLog")
@Tag(name = "打印日志", description = "打印日志查询")
@RequiredArgsConstructor
public class PrintLogController {

    private final PrintLogService printLogService;

    @Operation(summary = "获取打印日志分页列表")
    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('baseReceiptManage','shopBaseReceiptManage','posReceiptPrint')")
    public Page<PrintLogPageVO> list(PrintLogPageQuery query) {
        return printLogService.page(query);
    }
}
