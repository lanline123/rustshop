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
import com.tigshop.bean.dto.product.ECardGroupCreateDTO;
import com.tigshop.bean.dto.product.ECardGroupListDTO;
import com.tigshop.bean.dto.product.ECardGroupUpdateDTO;
import com.tigshop.bean.vo.product.ECardGroupVO;
import com.tigshop.service.product.ECardGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.tigshop.common.constant.CheckFieldConstants.E_CARD_GROUP_FIELDS;

/**
 * 电子卡券组控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/product/eCardGroup")
@Tag(name = "电子卡券组", description = "电子卡券组功能")
@PreAuthorize("@pms.hasPermission('eCardManage')")
@Validated
public class ECardGroupController {
    @Resource
    private ECardGroupService eCardGroupService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ECardGroupVO> list(ECardGroupListDTO listDTO, HttpServletResponse response) {
        return eCardGroupService.list(listDTO, response);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ECardGroupVO detail(@RequestParam Integer id) {
        return eCardGroupService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@Valid @RequestBody ECardGroupCreateDTO createDTO) {
        eCardGroupService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@Valid @RequestBody ECardGroupUpdateDTO updateDTO) {
        eCardGroupService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        eCardGroupService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public boolean updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        return eCardGroupService.updateField(updateField, E_CARD_GROUP_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        eCardGroupService.batch(batchDTO);
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入")
    public void batchImport(Integer groupId, @RequestParam("file") MultipartFile file) {
        eCardGroupService.batchImport(groupId, file);
    }

}
