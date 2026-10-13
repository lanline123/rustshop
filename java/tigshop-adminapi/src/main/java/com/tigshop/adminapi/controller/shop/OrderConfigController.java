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
package com.tigshop.adminapi.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.OrderConfigCreateDTO;
import com.tigshop.bean.dto.shop.OrderConfigListDTO;
import com.tigshop.bean.dto.shop.OrderConfigUpdateDTO;
import com.tigshop.bean.vo.shop.OrderConfigVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.shop.OrderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单配置控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/shop/orderConfig")
@Tag(name = "订单配置", description = "订单配置功能")
@Validated
public class OrderConfigController {
    @Resource
    private OrderConfigService orderConfigService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<OrderConfigVO> list(OrderConfigListDTO listDTO) {
        return orderConfigService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public OrderConfigVO detail(@RequestParam Integer id) {
        return orderConfigService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@Valid @RequestBody OrderConfigCreateDTO createDTO) {
        orderConfigService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@Valid @RequestBody OrderConfigUpdateDTO updateDTO) {
        orderConfigService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        orderConfigService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        orderConfigService.updateField(updateField,  CheckFieldConstants.ORDER_CONFIG_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        orderConfigService.batch(batchDTO);
    }
}
