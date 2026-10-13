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

import com.tigshop.service.setting.GalleryPicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件访问控制器
 *
 * @author Tigshop团队
 * @create 2025年01月13日 10:07
 */
@RestController
@Tag(name = "文件访问控制器")
public class FilesController {
    @Resource
    private GalleryPicService galleryPicService;

    @GetMapping("/img/gallery/{timestamp}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> findImage(@PathVariable String timestamp, @PathVariable String filename) {
        return galleryPicService.selectFile(timestamp, filename, "img");
    }

    @GetMapping("/video/gallery/{timestamp}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> findVideo(@PathVariable String timestamp, @PathVariable String filename) {
        return galleryPicService.selectFile(timestamp, filename, "video");
    }

    @GetMapping("/app/update/{timestamp}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> findAoo(@PathVariable String timestamp, @PathVariable String filename) {
        return galleryPicService.selectFile(timestamp, filename, "app");
    }
}
