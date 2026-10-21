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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.ProductGroupCreateDTO;
import com.tigshop.bean.dto.product.ProductGroupListDTO;
import com.tigshop.bean.dto.product.ProductGroupUpdateDTO;
import com.tigshop.bean.model.product.ProductGroup;
import com.tigshop.bean.vo.product.ProductGroupVO;
import com.tigshop.service.product.ProductGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 商品分组控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/product/productGroup")
@Tag(name = "商品分组", description = "商品分组功能")
@PreAuthorize("@pms.hasPermission('productGroupManage')")
public class ProductGroupController {
    @Resource
    private ProductGroupService productGroupService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ProductGroupVO> list(ProductGroupListDTO listDTO) {
        return productGroupService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ProductGroupVO detail(@RequestParam Integer id) {
        return productGroupService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('productGroupModifyManage')")
    public void create(@RequestBody ProductGroupCreateDTO createDTO) {
        productGroupService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('productGroupModifyManage')")
    public void update(@RequestBody ProductGroupUpdateDTO updateDTO) {
        productGroupService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('productGroupModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        productGroupService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('productGroupModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        productGroupService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('productGroupModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        productGroupService.batch(batchDTO);
    }

    @GetMapping("/clientList")
    @Operation(summary = "获取列表")
    public List<ProductGroup> client(ProductGroupListDTO listDTO) {
        return productGroupService.lambdaQuery()
                .like(StrUtil.isNotBlank(listDTO.getProductGroupName()), ProductGroup::getProductGroupName, listDTO.getProductGroupName())
                .list();
    }
}
