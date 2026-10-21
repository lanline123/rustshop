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

import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.setting.GalleryPicBatchDTO;
import com.tigshop.bean.dto.setting.GalleryPicExternalDTO;
import com.tigshop.bean.dto.setting.GalleryPicListDTO;
import com.tigshop.bean.dto.setting.GalleryPicSortDTO;
import com.tigshop.bean.dto.setting.GalleryPicTransferDTO;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.vo.config.GalleryListResVO;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.service.setting.GalleryPicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.tigshop.common.constant.CheckFieldConstants.GALLERY_FIELDS;

/**
 * 相册图片管理
 * @author Jayce
 * @create 2024年11月13日 17:38
 */
@RestController
@RequestMapping("/adminapi/setting/galleryPic")
@Tag(name = "相册图片管理", description = "相册图片管理")
@PreAuthorize("@pms.hasPermission('galleryPicManage')")
@Validated
public class GalleryPicController {
    @Resource
    private GalleryPicService galleryPicService;

    @GetMapping("/list")
    @Operation(summary = "相册图片列表")
    @PreAuthorize("@pms.hasPermission('decorate','galleryPicManage')")
    public GalleryListResVO list(GalleryPicListDTO dto){
        return galleryPicService.list(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除相册图片")
    @PreAuthorize("@pms.hasPermission('galleryPicModifyManage')")
    public void del(@RequestBody OperateDTO dto){
        galleryPicService.del(dto.getId());
    }

    @PostMapping("/uploadImg")
    @Operation(summary = "上传图片")
    public GalleryPicUploadVO uploadImg(
            @RequestParam(value = "galleryId", defaultValue = "0", required = false) Integer galleryId,
            @RequestParam("file") MultipartFile file){
        return galleryPicService.uploadImg(galleryId, file);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新权限字段")
    @PreAuthorize("@pms.hasPermission('galleryPicModifyManage')")
    public void updateField(@RequestBody @Valid UpdateFieldDTO updateField) {
        galleryPicService.updateField(updateField, GALLERY_FIELDS);
    }

    @GetMapping("/detail")
    @Operation(summary = "图片详情")
    public GalleryPic detail(@RequestParam Integer id) {
        return galleryPicService.detail(id);
    }

    @PostMapping("/sort")
    @Operation(summary = "图片手动排序")
    @PreAuthorize("@pms.hasPermission('galleryPicModifyManage')")
    public void sort(@RequestBody @Valid GalleryPicSortDTO dto) {
        galleryPicService.sort(dto);
    }

    @PostMapping("/transfer")
    @Operation(summary = "批量移动素材")
    @PreAuthorize("@pms.hasPermission('galleryPicModifyManage')")
    public void transfer(@RequestBody @Valid GalleryPicTransferDTO dto) {
        galleryPicService.transfer(dto);
    }

    @PostMapping("/batchDel")
    @Operation(summary = "批量删除素材")
    @PreAuthorize("@pms.hasPermission('galleryPicModifyManage')")
    public void batchDel(@RequestBody @Valid GalleryPicBatchDTO dto) {
        galleryPicService.batchDel(dto);
    }

    @PostMapping("/batchDownload")
    @Operation(summary = "批量下载素材")
    @IgnoreResponseAdvice
    public void batchDownload(@RequestBody @Valid GalleryPicBatchDTO dto, HttpServletResponse response) {
        galleryPicService.batchDownload(dto, response);
    }

    @PostMapping("/createFromExternal")
    @Operation(summary = "通过外部链接创建图片")
    public GalleryPicUploadVO createFromExternal(@RequestBody @Valid GalleryPicExternalDTO dto) {
        return galleryPicService.createFromExternal(dto);
    }
}
