package com.tigshop.adminapi.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountCreateDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountListDTO;
import com.tigshop.bean.dto.content.WechatVideoAccountUpdateDTO;
import com.tigshop.bean.vo.content.WechatVideoAccountVO;
import com.tigshop.service.content.WechatVideoAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 视频号账号管理
 */
@RestController
@RequestMapping("/adminapi/content/wechatVideoAccount")
@Tag(name = "视频号账号", description = "视频号账号管理接口")
@PreAuthorize("@pms.hasPermission('videoNumber')")
@Validated
public class WechatVideoAccountController {
    @Resource
    private WechatVideoAccountService wechatVideoAccountService;

    /**
     * 获取视频号账号列表
     * <p>请求参数格式：Query（URL参数）</p>
     * @param listDTO 查询条件
     * @return 分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取视频号账号列表", description = "请求参数格式：Query（URL参数）")
    public Page<WechatVideoAccountVO> list(WechatVideoAccountListDTO listDTO) {
        return wechatVideoAccountService.list(listDTO);
    }

    /**
     * 获取视频号账号详情
     * <p>请求参数格式：Query（URL参数）</p>
     * @param id 账号ID
     * @return 账号详情
     */
    @GetMapping("/detail")
    @Operation(summary = "获取视频号账号详情", description = "请求参数格式：Query（URL参数）")
    public WechatVideoAccountVO detail(@RequestParam Integer id) {
        return wechatVideoAccountService.detail(id);
    }

    /**
     * 新增视频号账号
     * <p>请求体格式：JSON</p>
     * @param createDTO 创建参数
     */
    @PostMapping("/create")
    @Operation(summary = "新增视频号账号", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('videoNumberModifyManage')")
    public void create(@Valid @RequestBody WechatVideoAccountCreateDTO createDTO) {
        wechatVideoAccountService.create(createDTO);
    }

    /**
     * 更新视频号账号
     * <p>请求体格式：JSON</p>
     * @param updateDTO 更新参数
     */
    @PostMapping("/update")
    @Operation(summary = "更新视频号账号", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('videoNumberModifyManage')")
    public void update(@Valid @RequestBody WechatVideoAccountUpdateDTO updateDTO) {
        wechatVideoAccountService.update(updateDTO);
    }

    /**
     * 删除视频号账号
     * <p>请求体格式：JSON</p>
     * @param DTO 操作参数（包含id）
     */
    @PostMapping("/delete")
    @Operation(summary = "删除视频号账号", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('videoNumberModifyManage')")
    public void delete(@RequestBody OperateDTO DTO) {
        wechatVideoAccountService.delete(DTO.getId());
    }
}
