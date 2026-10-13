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

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.PcNavigationCreateDTO;
import com.tigshop.bean.dto.decorate.PcNavigationListDTO;
import com.tigshop.bean.dto.decorate.PcNavigationUpdateDTO;
import com.tigshop.bean.vo.decorate.PcNavigationVO;
import com.tigshop.service.decorate.PcNavigationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.PC_NAVIGATION_FIELDS;

/**
 * 首页分类栏控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/decorate/pcNavigation")
@Tag(name = "PC导航栏")
@PreAuthorize("@pms.hasPermission('pcNavigationManage')")
public class PcNavigationController {
    @Resource
    private PcNavigationService pcNavigationService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PcNavigationVO> list(PcNavigationListDTO listDTO) {
        return pcNavigationService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public PcNavigationVO detail(@RequestParam Integer id) {
        return pcNavigationService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('pcNavigationModifyManage')")
    public void create(@RequestBody PcNavigationCreateDTO createDTO) {
        pcNavigationService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('pcNavigationModifyManage')")
    public void update(@RequestBody PcNavigationUpdateDTO updateDTO) {
        pcNavigationService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('pcNavigationModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        pcNavigationService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('pcNavigationModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        pcNavigationService.updateField(updateField, PC_NAVIGATION_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('pcNavigationModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        pcNavigationService.batch(batchDTO);
    }

    @GetMapping("/selectLink")
    @Operation(summary = "选择链接地址")
    public List<Map<String, String>> selectLink() {
        return pcNavigationService.selectLink();
    }

    @GetMapping("/getParentNav")
    @Operation(summary = "上级导航处理")
    public List<Tree<Integer>> getParentNav(@RequestParam(name = "type", defaultValue = "0") Integer type) {
        return pcNavigationService.getParentNav(type, 0);
    }

}
