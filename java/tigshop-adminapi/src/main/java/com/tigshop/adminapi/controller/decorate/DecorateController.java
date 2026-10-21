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
package com.tigshop.adminapi.controller.decorate;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.DecorateCreateDTO;
import com.tigshop.bean.dto.decorate.DecorateListDTO;
import com.tigshop.bean.dto.decorate.DecorateSaveDTO;
import com.tigshop.bean.dto.decorate.DecorateUpdateDTO;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.decorate.DecorateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.DECORATE_FIELDS;

/**
 * 页面管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/decorate/decorate")
@Tag(name = "装修管理")
public class DecorateController {
    @Resource
    private DecorateService decorateService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<DecorateVO> list(DecorateListDTO listDTO) {
        if (listDTO.getParentId() == null) {
            listDTO.setParentId(0);
        }
        if (listDTO.getPageType() == null || listDTO.getPageType().isEmpty()){
            listDTO.setPageType(List.of(1));
        }
        return decorateService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public DecorateVO detail(@RequestParam Integer id, @RequestParam(required = false) Integer localeId, @RequestParam(required = false)  Integer parentId, @RequestParam Integer decorateType) {
        return decorateService.detail(id, localeId, parentId, decorateType);
    }

    @PreAuthorize("@pms.hasPermission('decorateCreateManage')")
    @PostMapping("/create")
    @Operation(summary = "创建")
    public Map<String, Object> create(@RequestBody DecorateCreateDTO createDTO) {
        Integer decorateId = decorateService.create(createDTO);
        Map<String, Object> result = new HashMap<>();
        // 回传新建装修页ID：编辑器新建模式首次保存/预览依赖该ID继续后续草稿与发布
        result.put("decorateId", decorateId);
        return result;
    }

    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@RequestBody DecorateUpdateDTO updateDTO) {
        if (updateDTO.getParentId() == null) {
            updateDTO.setParentId(0);
        }
        if (updateDTO.getLocaleId() == null) {
            updateDTO.setLocaleId(0);
        }
        decorateService.update(updateDTO);
    }

    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    @PostMapping("/updateCustomDecorate")
    @Operation(summary = "自定义页面更新")
    public void updateCustomDecorate(@RequestBody DecorateUpdateDTO updateDTO) {
        Decorate decorate = decorateService.getById(updateDTO.getDecorateId());
        decorate.setDecorateTitle(updateDTO.getDecorateTitle());
        decorate.setSortOrder(updateDTO.getSortOrder());
        decorate.setUpdateTime(System.currentTimeMillis() / 1000);
        decorateService.updateByIdRefreshHome(decorate);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        decorateService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        decorateService.updateField(updateField, DECORATE_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        decorateService.batch(batchDTO);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制")
    public void copy(@RequestBody OperateDTO operateDTO) {
        decorateService.copy(operateDTO.getId());
    }
    @PostMapping("/setHome")
    @Operation(summary = "设置为首页")
    @PreAuthorize("@pms.hasPermission('decorateSetHomeManage')")
    public void setHome(@RequestBody OperateDTO operateDTO) {
        decorateService.setHome(operateDTO.getId(), operateDTO.getPageType());
    }

    @PostMapping("/saveDraft")
    @Operation(summary = "保存装修草稿")
    public void saveDraft(@RequestBody DecorateSaveDTO decorateSaveDTO) {
        decorateService.saveDecorateToDraft(decorateSaveDTO.getId(), decorateSaveDTO.getData(), decorateSaveDTO.getParentId(), decorateSaveDTO.getLocaleId());
    }
    @PostMapping("/publish")
    @Operation(summary = "发布装修")
    @PreAuthorize("@pms.hasPermission('decoratePublishManage')")
    public void publish(@RequestBody DecorateSaveDTO decorateSaveDTO) {
        decorateService.publishDecorate(decorateSaveDTO.getId(), decorateSaveDTO.getData(), decorateSaveDTO.getParentId(), decorateSaveDTO.getLocaleId());
    }

    @GetMapping("/loadDraftData")
    @Operation(summary = "加载草稿")
    public JSONObject loadDraftData(@RequestParam(value = "id") Integer id) {
        return decorateService.loadDraftData(id);
    }

    @GetMapping("/getH5DecorateList")
    @Operation(summary = "获取H5装修列表")
    public Page<DecorateVO> getH5DecorateList(DecorateListDTO listDTO) {
        listDTO.setDecorateType(List.of(DecorateType.H5.getCode()));
        listDTO.setStatus(Constants.YES);
        return decorateService.list(listDTO);
    }

    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    @PostMapping("/bindingH5Decorate")
    @Operation(summary = "绑定已发布的H5页面")
    public void bindingH5Decorate(@RequestBody DecorateUpdateDTO updateDTO) {
        Decorate decorate = decorateService.getById(updateDTO.getDecorateId());
        Assert.isTrue(decorate != null ,() -> new GlobalException("该装修页面不存在"));
        Assert.isTrue(updateDTO.getBindedH5Id() != null ,() -> new GlobalException("请选择要绑定的H5页面"));
        decorate.setBindedH5Id(updateDTO.getBindedH5Id());
        decorateService.updateByIdRefreshHome(decorate);
    }

    @GetMapping("/getCustomDecorateList")
    @Operation(summary = "H5获取自定义装修列表")
    public Page<DecorateVO> getCustomDecorateList(DecorateListDTO listDTO) {
        listDTO.setDecorateType(List.of(DecorateType.CUSTOM.getCode()));
        listDTO.setStatus(Constants.YES);
        return decorateService.list(listDTO);
    }

    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    @PostMapping("/bindingCustomDecorate")
    @Operation(summary = "绑定已发布的页面")
    public void bindingCustomDecorate(@RequestBody DecorateUpdateDTO updateDTO) {
        Decorate decorate = decorateService.getById(updateDTO.getDecorateId());
        Assert.isTrue(decorate != null ,() -> new GlobalException("该装修页面不存在"));
        Assert.isTrue(updateDTO.getBindedH5Id() != null ,() -> new GlobalException("请选择要绑定的自定义页面"));
        decorate.setBindedH5Id(updateDTO.getBindedH5Id());
        decorateService.updateByIdRefreshHome(decorate);
    }

    @PreAuthorize("@pms.hasPermission('decorateModifyManage')")
    @PostMapping("/cancelBinding")
    @Operation(summary = "绑定已发布的页面")
    public void cancelBinding(@RequestBody DecorateUpdateDTO updateDTO) {
        Decorate decorate = decorateService.getById(updateDTO.getDecorateId());
        Assert.isTrue(decorate != null ,() -> new GlobalException("该装修页面不存在"));
        decorate.setBindedH5Id(0);
        decorateService.updateByIdRefreshHome(decorate);
    }


}
