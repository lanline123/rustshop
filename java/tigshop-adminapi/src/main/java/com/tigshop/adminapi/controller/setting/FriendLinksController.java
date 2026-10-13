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
import com.tigshop.bean.dto.setting.FriendLinksCreateDTO;
import com.tigshop.bean.dto.setting.FriendLinksListDTO;
import com.tigshop.bean.dto.setting.FriendLinksUpdateDTO;
import com.tigshop.bean.vo.setting.FriendLinksVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.setting.FriendLinksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 友情链接控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/setting/friendLinks")
@Tag(name = "友情链接", description = "友情链接功能")
@Validated
@PreAuthorize("@pms.hasPermission('friendLinksManage')")
public class FriendLinksController {
    @Resource
    private FriendLinksService friendLinksService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<FriendLinksVO> list(FriendLinksListDTO listDTO) {
        return friendLinksService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public FriendLinksVO detail(@RequestParam Integer id) {
        return friendLinksService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('friendLinksUpdateManage')")
    public void create(@Valid @RequestBody FriendLinksCreateDTO createDTO) {
        friendLinksService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('friendLinksUpdateManage')")
    public void update(@Valid @RequestBody FriendLinksUpdateDTO updateDTO) {
        friendLinksService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('friendLinksDelManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        friendLinksService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('friendLinksUpdateManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        friendLinksService.updateField(updateField, CheckFieldConstants.FRIEND_LINKS_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('friendLinksBatchManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        friendLinksService.batch(batchDTO);
    }
}
