package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoListDTO;
import com.tigshop.bean.vo.config.GalleryVideoInfoVO;
import com.tigshop.bean.vo.setting.GalleryVideoInfoDetailVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.service.video.GalleryVideoInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频相册信息后台管理
 *
 * @author wzh
 */
@RestController
@RequestMapping("/adminapi/setting/galleryVideoInfo")
@Validated
@Tag(name = "视频相册信息后台管理接口", description = "视频相册信息后台管理接口")
@PreAuthorize("@pms.hasPermission('galleryVideoManage')")
@AllArgsConstructor
public class GalleryVideoInfoController {

    private final GalleryVideoInfoService galleryVideoInfoService;

    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('productManage','galleryVideoManage')")
    @Operation(summary = "相册视频列表")
    public GalleryVideoInfoVO list(GalleryVideoInfoListDTO dto){
        return galleryVideoInfoService.list(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除相册视频")
    public void del(@RequestBody @Valid OperateDTO dto) {
        galleryVideoInfoService.del(dto.getId());
    }

    @PostMapping("/uploadVideo")
    @Operation(summary = "上传视频")
    public UploadVideoInfoVO uploadVideo(@RequestParam("file") MultipartFile file) {
        return galleryVideoInfoService.uploadVideo(file);
    }

    @PostMapping("/create")
    @Operation(summary = "保存视频信息")
    public void save(@RequestBody @Valid GalleryVideoInfoDTO dto) {
        galleryVideoInfoService.save(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "修改视频信息")
    public void update(@RequestBody @Valid GalleryVideoInfoDTO dto) {
        galleryVideoInfoService.update(dto);
    }

    @GetMapping("/detail")
    @PreAuthorize("@pms.hasPermission('productManage','galleryVideoManage')")
    @Operation(summary = "视频信息详情")
    public GalleryVideoInfoDetailVO detail(@RequestParam Integer id) {
        return galleryVideoInfoService.detail(id);
    }

}
