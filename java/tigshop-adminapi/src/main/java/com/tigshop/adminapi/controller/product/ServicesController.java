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

package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.ServicesDTO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.product.ServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.SERVICES_FIELDS;

/**
 * 商品服务控制器
 *
 * @author Jayce
 * @create 2024年11月06日 14:24
 */
@RestController
@RequestMapping("/adminapi/product/productServices")
@Tag(name = "商品服务")
@Validated
@PreAuthorize("@pms.hasPermission('productServicesManage')")
public class ServicesController {
    @Resource
    private ServicesService servicesService;

    @GetMapping("/list")
    @Operation(summary = "商品服务列表")
    public Page<ServicesDTO> list(BasePage basePage) {
        return servicesService.list(basePage);
    }

    @GetMapping("/detail")
    @Operation(summary = "商品服务详情")
    public ServicesDTO detail(@RequestParam Integer id) {
        return servicesService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品服务")
    @PreAuthorize("@pms.hasPermission('productServicesModifyManage')")
    public void create(@RequestBody ServicesDTO servicesDTO) {
        servicesService.create(servicesDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新商品服务")
    @PreAuthorize("@pms.hasPermission('productServicesModifyManage')")
    public void update(@RequestBody ServicesDTO servicesDTO) {
        servicesService.update(servicesDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品服务")
    @PreAuthorize("@pms.hasPermission('productServicesModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        servicesService.del(operateDTO.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作商品服务")
    @PreAuthorize("@pms.hasPermission('productServicesModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        servicesService.batch(batchDTO);
    }

    @PostMapping("/updateField")
    @Operation(summary = "商品服务字段修改")
    @PreAuthorize("@pms.hasPermission('productServicesModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateFieldDTO) {
        servicesService.updateField(updateFieldDTO, SERVICES_FIELDS);
    }
}
