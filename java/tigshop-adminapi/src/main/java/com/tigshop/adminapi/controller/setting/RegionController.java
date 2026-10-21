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

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.RegionCreateDTO;
import com.tigshop.bean.dto.setting.RegionUpdateDTO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.dto.setting.RegionListDTO;
import com.tigshop.bean.vo.setting.RegionListVO;
import com.tigshop.bean.vo.sys.RegionVO;
import com.tigshop.service.setting.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;
import com.tigshop.common.exception.GlobalException;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.REGION_FIELDS;

/**
 * 地区控制器
 *
 * @author Jayce
 * @create 2024年11月11日 16:22
 */
@RestController
@Tag(name = "地区管理")
@Validated
@RequestMapping("/adminapi/setting/region")
@PreAuthorize("@pms.hasPermission('regionManage')")
public class RegionController {

    @Resource
    private RegionService regionService;

    @GetMapping("/getAllRegionTree")
    @Operation(summary = "获取所有地区")
    public List<Tree<Integer>> getAllRegionTree() {
        return regionService.getAllRegionTree();
    }

    @GetMapping("/list")
    @Operation(summary = "地区列表")
    public Page<RegionListVO> list(RegionListDTO dto) {
        return regionService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "地区详情")
    public RegionListVO detail(@RequestParam("id") Integer id) {
        Region region = regionService.getById(id);
        if (region == null) {
            throw new GlobalException("地区不存在");
        }
        RegionListVO result = new RegionListVO();
        BeanUtils.copyProperties(region, result);
        return result;
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('regionUpdateManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        regionService.updateField(updateField, REGION_FIELDS);
    }

    @PostMapping("/create")
    @Operation(summary = "新增地区")
    @PreAuthorize("@pms.hasPermission('regionUpdateManage')")
    public void create(@Valid @RequestBody RegionCreateDTO dto) {
        regionService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "更新地区")
    @PreAuthorize("@pms.hasPermission('regionUpdateManage')")
    public void update(@Valid @RequestBody RegionUpdateDTO dto) {
        regionService.updateRegion(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('regionUpdateManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        regionService.del(operateDTO.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('regionBatchManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        regionService.batch(batchDTO);
    }

}
