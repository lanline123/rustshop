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
import com.tigshop.bean.dto.product.AttributesTplDTO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.product.AttributesTplService;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.TPL_FIELDS;
import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 商品属性模板控制器
 *
 * @author Jayce
 * @create 2024年11月05日 09:49
 */
@RestController
@RequestMapping("/adminapi/product/productAttributesTpl")
@Tag(name = "商品属性模板")
@Validated
@PreAuthorize("@pms.hasPermission('productAttributesTplManage')")
public class AttributesTplController {
    @Resource
    private AttributesTplService attributesTplService;

    @GetMapping("/list")
    @Operation(summary = "商品属性模板列表")
    public Page<AttributesTplDTO> list(BasePage basePage) {
        return attributesTplService.list(basePage);
    }

    @GetMapping("/listByCategory")
    @Operation(summary = "按类目获取商品属性模板")
    @PreAuthorize("@pms.hasPermission('productAttributesTplManage','productManage')")
    public List<AttributesTplDTO> listByCategory(
            @RequestParam @NotNull(message = "商品类目不能为空") Integer categoryId) {
        return attributesTplService.listByCategory(categoryId);
    }

    @GetMapping("/detail")
    @Operation(summary = "商品属性模板详情")
    public AttributesTplDTO detail(@RequestParam @NotNull(message = ID_CANNOT_IS_NULL) Integer id) {
        return attributesTplService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品属性模板")
    @PreAuthorize("@pms.hasPermission('productAttributesTplModifyManage')")
    public void create(@Valid @RequestBody AttributesTplDTO attributesTplDTO) {
        attributesTplDTO.setShopId(HeaderUtils.getShopId());
        attributesTplService.create(attributesTplDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新商品属性模板")
    @PreAuthorize("@pms.hasPermission('productAttributesTplModifyManage')")
    public void update(@Valid @RequestBody AttributesTplDTO attributesTplDTO) {
        attributesTplService.update(attributesTplDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品属性模板")
    @PreAuthorize("@pms.hasPermission('productAttributesTplModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        attributesTplService.del(operateDTO.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作商品属性模板")
    @PreAuthorize("@pms.hasPermission('productAttributesTplModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        attributesTplService.batch(batchDTO);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新商品属性模板字段")
    @PreAuthorize("@pms.hasPermission('productAttributesTplModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateFieldDTO) {
        attributesTplService.updateField(updateFieldDTO, TPL_FIELDS);
    }
}
