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
import com.tigshop.bean.dto.setting.ShippingTplListDTO;
import com.tigshop.bean.dto.setting.ShippingTplUpdateDTO;
import com.tigshop.bean.param.settings.shippingtpl.ShippingTplSaveParam;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTplVO;
import com.tigshop.bean.vo.setting.shippingtpl.ShippingTypeVO;
import com.tigshop.service.setting.ShippingTplService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 运费模板控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/setting/shippingTpl")
@Tag(name = "运费模板", description = "运费模板功能")
@PreAuthorize("@pms.hasPermission('shippingTplManage','shopShippingTplManage')")
public class ShippingTplController {

    private static String[] FIELDS = { "is_show", "sort_order", "status", "shipping_tpl_name"};

    @Resource
    private ShippingTplService shippingTplService;


    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ShippingTplVO> list(ShippingTplListDTO listDTO) {
        return shippingTplService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ShippingTplVO detail(@RequestParam(value = "id") @NotNull(message = "模版主键不能为空") Integer id) {
        return shippingTplService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('shippingTplUpdateManage')")
    public void create(@RequestBody ShippingTplSaveParam param) {
        shippingTplService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('shippingTplUpdateManage')")
    public void update(@RequestBody ShippingTplUpdateDTO updateDTO) {
        shippingTplService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('shippingTplDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        shippingTplService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('shippingTplUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        shippingTplService.updateField(updateField,  FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('shippingTplBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        shippingTplService.batch(batchDTO);
    }

    @GetMapping("/config")
    @Operation(summary = "获取运费模版")
    public List<ShippingTypeVO> config() {
        return shippingTplService.getShippingTypeListByShopId();
    }

    @PostMapping("/setDefault")
    @Operation(summary = "设置默认运费模板")
    @PreAuthorize("@pms.hasPermission('shippingTplUpdateManage')")
    public void setDefault(@RequestParam(value = "id",required = true) Long id) {
        shippingTplService.setDefault(id);
    }
}
