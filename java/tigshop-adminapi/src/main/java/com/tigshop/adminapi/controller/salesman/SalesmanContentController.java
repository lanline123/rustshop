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
package com.tigshop.adminapi.controller.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.enums.salesman.ContentStatus;
import com.tigshop.bean.model.salesman.SalesmanContent;
import com.tigshop.bean.param.salesman.SalesmanContentEditParam;
import com.tigshop.bean.param.salesman.SalesmanContentSaveParam;
import com.tigshop.bean.query.salesman.SalesmanContentPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanContentVO;
import com.tigshop.service.salesman.SalesmanContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.SALESMAN_CONTENT_FIELDS;

/**
 * 分销内容管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/salesman/content")
@Tag(name = "分销内容管理", description = "分销内容管理功能")
public class SalesmanContentController {
    @Resource
    private SalesmanContentService salesmanContentService;

    @Resource
    private com.tigshop.common.core.TranslatePackage translatePackage;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanContentVO> list(SalesmanContentPageQuery query) {
        return salesmanContentService.list(query);
    }

    @GetMapping("/config")
    @Operation(summary = "获取配置")
    public Map<Integer, String> config() {
        //取出ContentStatus所有枚举做一个map
        Map<Integer, String> statusMap = new HashMap<>();
        for (ContentStatus status : ContentStatus.values()) {
            statusMap.put(status.getCode(), translatePackage.translate(status.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
        }
        return statusMap;
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SalesmanContentVO detail(@RequestParam Integer id) {
        return salesmanContentService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('salesmanContentModifyManage')")
    public void create(@RequestBody SalesmanContentSaveParam param) {
        salesmanContentService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanContentModifyManage')")
    public void update(@RequestBody @Validated SalesmanContentEditParam param) {
        salesmanContentService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('salesmanContentModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        salesmanContentService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('salesmanContentModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        salesmanContentService.updateField(updateField, SALESMAN_CONTENT_FIELDS);
        if (updateField.getField().equals("isAvailable") && updateField.getVal().equals("0")) {
            SalesmanContent byId = salesmanContentService.getById(updateField.getId());
            byId.setEndTime(System.currentTimeMillis() / 1000);
            salesmanContentService.updateById(byId);
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('salesmanContentModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        salesmanContentService.batch(batchDTO);
    }
}
