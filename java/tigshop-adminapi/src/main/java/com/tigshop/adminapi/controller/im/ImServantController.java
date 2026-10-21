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
package com.tigshop.adminapi.controller.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.ImServantListDTO;
import com.tigshop.bean.dto.im.ServantModifyStatusDTO;
import com.tigshop.bean.vo.im.ImServantVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.im.ImServantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 客服控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/im/servant/servant")
@Tag(name = "客服", description = "客服功能")
@Validated
public class ImServantController {
    @Resource
    private ImServantService imServantService;

    @GetMapping("/transferList")
    @Operation(summary = "获取列表")
    public Page<ImServantVO> list(ImServantListDTO listDTO) {
        Integer shopId = HeaderUtils.getShopId();
        listDTO.setShopId(shopId);
        return imServantService.list(listDTO);
    }

    @PostMapping("/modifyStatus")
    @Operation(summary = "修改状态")
    public void modifyStatus(@Valid @RequestBody ServantModifyStatusDTO dto) {
        Integer adminUserId = SecurityUtils.getCurrentAdminId();
        dto.setServantId(adminUserId);
        dto.setShopId(HeaderUtils.getShopId());
        imServantService.modifyStatus(dto);
    }

    @GetMapping("/getStatus")
    @Operation(summary = "获取客服状态")
    public ImServantVO getStatus() {
        Integer servantId = SecurityUtils.getCurrentAdminId();
        Integer shopId = HeaderUtils.getShopId();
        return imServantService.getStatus(servantId, shopId);
    }
}
