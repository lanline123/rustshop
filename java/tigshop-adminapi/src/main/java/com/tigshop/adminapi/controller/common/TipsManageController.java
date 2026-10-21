package com.tigshop.adminapi.controller.common;

import com.tigshop.bean.vo.common.TipsVO;
import com.tigshop.service.common.TipsManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提示管理控制器
 *
 * @author kidd
 * @since 2025/6/19 13:33
 */
@RequiredArgsConstructor
@Tag(name = "提示管理")
@RestController
@RequestMapping("/adminapi/common/tipsManage")
public class TipsManageController {

    private final TipsManageService tipsManageService;

    @Operation(summary = "提示管理列表")
    @GetMapping("/list")
    public List<TipsVO> list(@RequestParam("url") String url) {
        return tipsManageService.list(url);
    }

}
