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
package com.tigshop.adminapi.controller.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.ShippingTypeCreateDTO;
import com.tigshop.bean.dto.setting.ShippingTypeListDTO;
import com.tigshop.bean.dto.setting.ShippingTypeUpdateDTO;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTypeVO;
import com.tigshop.service.setting.ShippingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.SHIPPING_TYPE_FIELDS;

/**
 * 配送类型控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/setting/shippingType")
@Tag(name = "配送类型", description = "配送类型功能")
@PreAuthorize("@pms.hasPermission('shippingTypeManage')")
public class ShippingTypeController {
    @Resource
    private ShippingTypeService shippingTypeService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ShippingTypeVO> list(ShippingTypeListDTO listDTO) {
        return shippingTypeService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ShippingTypeVO detail(@RequestParam Integer id) {
        return shippingTypeService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('shippingTypeUpdateManage')")
    public void create(@RequestBody ShippingTypeCreateDTO createDTO) {
        shippingTypeService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('shippingTypeUpdateManage')")
    public void update(@RequestBody ShippingTypeUpdateDTO updateDTO) {
        shippingTypeService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('shippingTypeDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        shippingTypeService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('shippingTypeUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        shippingTypeService.updateField(updateField, SHIPPING_TYPE_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('shippingTypeBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        shippingTypeService.batch(batchDTO);
    }
}
