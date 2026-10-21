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
package com.tigshop.adminapi.controller.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.merchant.MerchantCreateDTO;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.dto.merchant.MerchantUpdateDTO;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.service.merchant.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 商家表控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/merchant")
@Tag(name = "商户管理", description = "商户管理功能")
public class MerchantController {
    @Resource
    private MerchantService merchantService;

    @PostMapping("/uploadLicense")
    @Operation(summary = "入驻营业执照专用上传", description = "添加公示水印，不进入相册，返回图片地址保存至 merchantData.businessLicenseImg")
    @PreAuthorize("@pms.hasPermission('merchantModifyManage')")
    public String uploadLicense(@RequestParam("file") MultipartFile file) {
        return merchantService.uploadLicensePic(file);
    }

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<MerchantVO> list(MerchantListPageQuery pageQuery) {
        return merchantService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public MerchantVO detail(@RequestParam Integer id) {
        return merchantService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('merchantModifyManage')")
    public void create(@RequestBody MerchantCreateDTO createDTO) {
        merchantService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('merchantModifyManage')")
    public void update(@RequestBody MerchantUpdateDTO updateDTO) {
        merchantService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('merchantModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        merchantService.delMerchant(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('merchantModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        merchantService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('merchantModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        merchantService.batch(batchDTO);
    }
}
