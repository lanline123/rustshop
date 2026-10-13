package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.product.ECardSaveParam;
import com.tigshop.bean.param.product.ECardEditParam;
import com.tigshop.bean.query.product.ECardPageQuery;
import com.tigshop.bean.vo.product.ECardVO;
import com.tigshop.service.product.ECardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.E_CARD_GROUP_FIELDS;

/**
 * 电子卡券控制器
 *
 * @author kidd
 * @create 2025/7/2
 */
@RequiredArgsConstructor
@Tag(name = "电子卡券", description = "电子卡券功能")
@RestController
@PreAuthorize("@pms.hasPermission('eCardManage')")
@RequestMapping("/adminapi/product/eCard")
public class ECardController {

    private final ECardService eCardService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<ECardVO> list(@Validated ECardPageQuery pageQuery) {
        return eCardService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public ECardVO detail(@RequestParam("id") Integer id) {
        return eCardService.detail(id);
    }

    @Operation(summary = "创建")
    @PostMapping("/create")
    public void create(@Validated @RequestBody ECardSaveParam param) {
        eCardService.create(param);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    public void update(@Validated @RequestBody ECardEditParam param) {
        eCardService.update(param);
    }

    @Operation(summary = "删除")
    @PostMapping("/del")
    public void del(@Validated @RequestBody OperateDTO operateDTO) {
        eCardService.del(operateDTO.getId());
    }

    @Operation(summary = "更新字段")
    @PostMapping("/updateField")
    public void updateField(@Validated @RequestBody UpdateFieldDTO updateField) {
        eCardService.updateField(updateField, E_CARD_GROUP_FIELDS);
    }

    @Operation(summary = "批量操作")
    @PostMapping("/batch")
    public void batch(@Validated @RequestBody BatchDTO batchDTO) {
        eCardService.batch(batchDTO);
    }
}

