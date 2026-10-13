package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.CollectProductListDTO;
import com.tigshop.bean.dto.user.CollectProductSaveDTO;
import com.tigshop.bean.query.user.CollectProductCancelParam;
import com.tigshop.bean.vo.user.CollectProductVO;
import com.tigshop.service.user.CollectProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品收藏服务
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/collectProduct"))
@Tag(name = "商品收藏")
@Validated
public class CollectProductController {
    @Resource
    CollectProductService collectProductService;

    @GetMapping("/list")
    @Operation(summary = "商品收藏列表")
    public Page<CollectProductVO> list(CollectProductListDTO listDTO) {
        return collectProductService.list(listDTO);
    }

    @PostMapping("/save")
    @Operation(summary = "商品收藏")
    public void save(@RequestBody CollectProductSaveDTO saveDTO) {
        collectProductService.create(saveDTO);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消商品收藏")
    public void cancel(@RequestBody @Validated CollectProductCancelParam param) {
        collectProductService.cancel(param);
    }
}
