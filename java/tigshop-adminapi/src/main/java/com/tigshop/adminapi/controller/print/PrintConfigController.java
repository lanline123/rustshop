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

import com.tigshop.bean.param.print.PrintConfigUpdateParam;
import com.tigshop.bean.param.print.PrintOrdersParam;
import com.tigshop.bean.vo.print.PrintConfigVO;
import com.tigshop.service.print.PrintConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tigshop团队
 * @create 2025/7/22 10:29
 */
@RestController
@RequestMapping("/adminapi/print/printConfig")
@Tag(name = "打印机配置管理", description = "打印机配置管理")
@RequiredArgsConstructor
public class PrintConfigController {

    private final PrintConfigService printConfigService;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping("/getConfigsByPrintId")
    @Operation(summary = "获取打印机配置")
    @PreAuthorize("@pms.hasPermission('baseReceiptManage','shopBaseReceiptManage','posReceiptPrint')")
    public PrintConfigVO getConfigsByPrintId(@RequestParam @NotNull(message = "打印机ID不能为空") Integer printId) {
        return printConfigService.getConfigsByPrintId(printId);
    }


    @PostMapping("/update")
    @Operation(summary = "更新打印机配置")
    @PreAuthorize("@pms.hasPermission('baseReceiptUpdateManage','shopBaseReceiptUpdateManage','posReceiptPrintUpdateManage')")
    public Boolean update(@RequestBody @Valid PrintConfigUpdateParam param) {
        return printConfigService.updateConfig(param);
    }

    @PostMapping("/print")
    @Operation(summary = "打印订单")
    public Boolean print(@RequestBody @Valid PrintOrdersParam param) {
        return printConfigService.print(param);
    }
}
