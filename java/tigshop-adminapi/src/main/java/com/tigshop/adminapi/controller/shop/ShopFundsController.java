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
package com.tigshop.adminapi.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.ShopFundsCreateDTO;
import com.tigshop.bean.dto.shop.ShopFundsListDTO;
import com.tigshop.bean.dto.shop.ShopFundsUpdateDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.shop.ShopFundsVO;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopFundsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 店铺表控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/shopAccount")
@Tag(name = "店铺表", description = "店铺表功能")
public class ShopFundsController {
    @Resource
    private ShopFundsService shopFundsService;

    @Resource
    private ConfigService configService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ShopFundsVO> list(ShopFundsListDTO listDTO) {
        return shopFundsService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ShopFundsVO detail(@RequestParam Integer id) {
        return shopFundsService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@RequestBody ShopFundsCreateDTO createDTO) {
        shopFundsService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@RequestBody ShopFundsUpdateDTO updateDTO) {
        shopFundsService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@RequestBody OperateDTO operateDTO) {
        shopFundsService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        shopFundsService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@RequestBody BatchDTO batchDTO) {
        shopFundsService.batch(batchDTO);
    }

    @GetMapping("/applyShopAgreement")
    @Operation(summary = "申请入驻协议")
    public String applyShopAgreement() {
        return configService.getConfigVal(SettingsEnum.SHOP_AGREEMENT);
    }

}
