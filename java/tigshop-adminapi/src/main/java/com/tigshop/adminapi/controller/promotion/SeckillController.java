package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.promotion.SeckillCreateDTO;
import com.tigshop.bean.dto.promotion.SeckillListDTO;
import com.tigshop.bean.dto.promotion.SeckillUpdateDTO;
import com.tigshop.bean.vo.promotion.SeckillListVO;
import com.tigshop.bean.vo.promotion.SeckillPromotionVO;
import com.tigshop.bean.vo.promotion.SeckillVO;
import com.tigshop.service.promotion.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 限时秒杀控制器
 *
 * @author kidd
 * @create 2025/7/2
 */
@RequiredArgsConstructor
@Tag(name = "限时秒杀", description = "限时秒杀功能")
@RestController
@PreAuthorize("@pms.hasPermission('seckillManage')")
@RequestMapping("/adminapi/promotion/seckill")
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<SeckillVO> list(SeckillListDTO listDTO) {
        return seckillService.list(listDTO);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public SeckillVO detail(@RequestParam("id") Integer id) {
        return seckillService.detail(id);
    }

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('seckillModifyManage')")
    @PostMapping("/create")
    public void create(@Valid @RequestBody SeckillCreateDTO createDTO) {
        seckillService.create(createDTO);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('seckillModifyManage')")
    @PostMapping("/update")
    public void update(@Valid @RequestBody SeckillUpdateDTO updateDTO) {
        seckillService.update(updateDTO);
    }

    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('seckillModifyManage')")
    @PostMapping("/del")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        seckillService.del(operateDTO.getId());
    }

    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('seckillModifyManage')")
    @PostMapping("/batch")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        seckillService.batch(batchDTO);
    }

    @Operation(summary = "获取秒杀商品列表")
    @GetMapping("/listForDecorate")
    public List<SeckillPromotionVO> listForDecorate(SeckillListDTO listDTO) {
        return seckillService.listForDecorate(listDTO);
    }

    /**
     * 装修预览专用：直接复用 C 端首页 {@code /api/home/home/getSeckill} 的 Service 方法，
     * 保证后台预览与前台首页选中同一个秒杀活动（同样的进行中过滤、上架过滤、排序与分页窗口）。
     * {@code unStarted} 固定为 0，与 C 端默认行为一致。
     */
    @Operation(summary = "装修预览：秒杀商品列表（与 C 端首页同口径）")
    @GetMapping("/listForHomePreview")
    public Page<SeckillListVO> listForHomePreview(
            @RequestParam(value = "size", defaultValue = "15", required = false) Integer size,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
        return seckillService.getSeckillProductList(size, page, 0);
    }
}
