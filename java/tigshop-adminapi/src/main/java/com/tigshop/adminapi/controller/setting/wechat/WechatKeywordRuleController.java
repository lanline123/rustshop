// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.setting.wechat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleDelDTO;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleListDTO;
import com.tigshop.bean.dto.setting.wechat.KeywordRuleSaveDTO;
import com.tigshop.bean.dto.setting.wechat.SetEnabledDTO;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleDetailVO;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleSaveVO;
import com.tigshop.bean.vo.setting.wechat.KeywordRuleVO;
import com.tigshop.service.setting.wechat.WechatKeywordRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公众号关键字回复规则控制器
 *
 * <p>路径前缀 {@code /adminapi/wechat/keywordRule}，对齐接口契约 §2。
 * 类级权限 {@code wechatKeywordReply}（查看）；写接口额外声明 {@code wechatKeywordReplyManage}（管理）。
 * 响应由 {@link com.tigshop.common.handler.GlobalResponseAdvice} 统一包装为 {@code AjaxResult}，
 * 故方法层直接返回业务 VO/Page/void 即可。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@RestController
@RequestMapping("/adminapi/wechat/keywordRule")
@Tag(name = "公众号关键字回复", description = "公众号关键字自动回复规则配置")
@Validated
@PreAuthorize("@pms.hasPermission('wechatKeywordReply')")
public class WechatKeywordRuleController {

    @Resource
    private WechatKeywordRuleService wechatKeywordRuleService;

    @GetMapping("/list")
    @Operation(summary = "规则列表")
    public Page<KeywordRuleVO> list(KeywordRuleListDTO dto) {
        return wechatKeywordRuleService.page(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "规则详情")
    public KeywordRuleDetailVO detail(@RequestParam("id") Long id) {
        return wechatKeywordRuleService.detail(id);
    }

    @PostMapping("/save")
    @Operation(summary = "新增或编辑规则")
    @PreAuthorize("@pms.hasPermission('wechatKeywordReplyManage')")
    public KeywordRuleSaveVO save(@Valid @RequestBody KeywordRuleSaveDTO dto) {
        Long id = wechatKeywordRuleService.save(dto);
        KeywordRuleSaveVO vo = new KeywordRuleSaveVO();
        vo.setId(id);
        return vo;
    }

    @PostMapping("/del")
    @Operation(summary = "批量删除规则")
    @PreAuthorize("@pms.hasPermission('wechatKeywordReplyManage')")
    public void del(@Valid @RequestBody KeywordRuleDelDTO dto) {
        wechatKeywordRuleService.del(dto.getIds());
    }

    @PostMapping("/setEnabled")
    @Operation(summary = "批量启停规则")
    @PreAuthorize("@pms.hasPermission('wechatKeywordReplyManage')")
    public void setEnabled(@Valid @RequestBody SetEnabledDTO dto) {
        wechatKeywordRuleService.setEnabled(dto.getIds(), dto.getEnabled());
    }
}
