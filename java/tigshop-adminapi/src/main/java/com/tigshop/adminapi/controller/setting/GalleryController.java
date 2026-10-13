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
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.vo.setting.GalleryTreeNodeVO;
import com.tigshop.service.setting.GalleryService;
import com.tigshop.service.video.GalleryVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 相册控制器
 *
 * @author Jayce
 * @create 2024年11月12日 16:35
 */
@RestController
@RequestMapping("/adminapi/setting/gallery")
@Tag(name = "相册管理", description = "相册管理")
@PreAuthorize("@pms.hasPermission('galleryManage')")
@Validated
public class GalleryController {
    @Resource
    private GalleryService galleryService;
    @Resource
    private GalleryVideoService galleryVideoService;

    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('decorate','galleryManage')")
    @Operation(summary = "相册列表")
    public Page<GalleryDTO> list(GalleryListDTO dto) {
        return galleryService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "相册详情")
    public GalleryDTO detail(@RequestParam Integer id) {
        return galleryService.detail(id);
    }

    @PostMapping("/del")
    @Operation(summary = "删除相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void del(@RequestBody @Valid GalleryDelDTO dto) {
        String deleteMode = dto.getDeleteMode();
        if (deleteMode != null && !deleteMode.isEmpty()) {
            galleryService.delWithMode(dto);
        } else {
            galleryService.del(dto.getId());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "添加相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage','decorateModifyManage')")
    public void create(@RequestBody GalleryDTO dto) {
        galleryService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "修改相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void update(@RequestBody GalleryUpdateDTO dto) {
        galleryService.update(dto);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void batch(@RequestBody BatchDTO dto) {
        galleryService.batch(dto);
    }

    @GetMapping("/tree")
    @Operation(summary = "统一分组树")
    @PreAuthorize("@pms.hasPermission('decorate','galleryManage')")
    public Map<String, Object> tree(@RequestParam(defaultValue = "image") String type) {
        List<GalleryTreeNodeVO> records;
        if ("video".equals(type)) {
            records = galleryVideoService.tree();
        } else {
            records = galleryService.tree();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        return result;
    }

    @PostMapping("/sort")
    @Operation(summary = "分组排序")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void sort(@RequestBody @Valid GallerySortDTO dto) {
        if ("video".equals(dto.getType())) {
            galleryVideoService.sort(dto);
        } else {
            galleryService.sort(dto);
        }
    }

    @PostMapping("/move")
    @Operation(summary = "分组转移/素材移动")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void move(@RequestBody @Valid GalleryMoveDTO dto) {
        if ("video".equals(dto.getType())) {
            galleryVideoService.move(dto);
        } else {
            galleryService.move(dto);
        }
    }
}
