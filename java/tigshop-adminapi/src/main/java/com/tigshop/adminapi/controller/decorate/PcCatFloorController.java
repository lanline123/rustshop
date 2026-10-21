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
package com.tigshop.adminapi.controller.decorate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorCreateDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorListDTO;
import com.tigshop.bean.dto.decorate.PcCatFloorUpdateDTO;
import com.tigshop.bean.vo.decorate.PcCatFloorVO;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.service.decorate.PcCatFloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 首页分类栏控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/decorate/pcCatFloor")
@Tag(name = "PC分类抽屉")
@PreAuthorize("@pms.hasPermission('pcCatFloorManage')")
public class PcCatFloorController {
    @Resource
    private PcCatFloorService pcCatFloorService;

    @Resource
    private RedisCache redisCache;


    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PcCatFloorVO> list(PcCatFloorListDTO listDTO) {
        return pcCatFloorService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public PcCatFloorVO detail(@RequestParam Integer id) {
        return pcCatFloorService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('pcCatFloorModifyManage')")
    public void create(@RequestBody @Validated PcCatFloorCreateDTO createDTO) {
        pcCatFloorService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('pcCatFloorModifyManage')")
    public void update(@RequestBody @Validated PcCatFloorUpdateDTO updateDTO) {
        pcCatFloorService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('pcCatFloorModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        pcCatFloorService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('pcCatFloorModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        pcCatFloorService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('pcCatFloorModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        pcCatFloorService.batch(batchDTO);
    }

    @PostMapping("/clearCache")
    @Operation(summary = "清除缓存")
    public void clearCache() {
        redisCache.deleteObject("cat");
    }

}
