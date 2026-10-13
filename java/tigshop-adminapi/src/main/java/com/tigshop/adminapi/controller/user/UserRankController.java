// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.user.RankConfigInitDTO;
import com.tigshop.bean.dto.user.UserRankDTO;
import com.tigshop.bean.dto.user.UserRankListDTO;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.query.user.UserRankEditParam;
import com.tigshop.bean.vo.user.UserRankConfigVO;
import com.tigshop.bean.vo.user.UserRankDetailVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.service.user.UserRankConfigService;
import com.tigshop.service.user.UserRankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.USER_RANK_FIELDS;

/**
 * @author Tigshop团队
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/user/userRank")
@Tag(name="会员等级")
@Validated
@PreAuthorize("@pms.hasPermission('userLevelProManage','levelManageManage','userRankManage')")
public class UserRankController {

    private final TigshopProperties tigshopProperties;

    private final UserRankService userRankService;

    private final UserRankConfigService userRankConfigService;

    @GetMapping("/listByPro")
    @Operation(summary = "获取会员等级列表")
    public Map<String, Object> listByPro(UserRankListDTO userRankDto) {
        if (tigshopProperties.getIsPro() != 1) {
            return Map.of("userRank", Collections.emptyList(), "rankConfig", Collections.emptyList());
        }
        UserRankConfig userRankConfig = userRankConfigService.getUserRankConfig("rank_config");
        UserRankConfigVO userRankConfigVO = new UserRankConfigVO();
        if (userRankConfig != null) {
            BeanUtils.copyProperties(userRankConfig, userRankConfigVO);
            userRankConfigVO.setData(JsonUtil.fromJson(userRankConfig.getData(), JSONObject.class));
            userRankConfigVO.setData(JsonUtil.fromJson(userRankConfig.getData(), JSONObject.class));
            userRankDto.setRankType(userRankConfig.getRankType());
        } else {
            RankConfigInitDTO rankConfigInitDTO = userRankService.getRankConfigInit();
            BeanUtils.copyProperties(rankConfigInitDTO, userRankConfigVO);
            userRankConfigVO.setData(JsonUtil.fromJson(JsonUtil.toJson(rankConfigInitDTO.getData()), JSONObject.class));
            userRankDto.setRankType(1);
        }
        return Map.of("userRank", userRankService.list(userRankDto), "rankConfig", userRankConfigVO);
    }

    @GetMapping("/list")
    @Operation(summary = "获取会员等级列表（非Pro）")
    public Map<String, Object> list(UserRankListDTO userRankDto) {
        return Map.of("userRank", userRankService.listNotPro(userRankDto), "rankConfig", Collections.emptyList());
    }

    @PostMapping("/create")
    @Operation(summary = "创建会员等级")
    @PreAuthorize("@pms.hasPermission('userRankModifyManage')")
    public void create(@RequestBody UserRankDTO userRank){
        userRankService.create(userRank);
    }

    @GetMapping("/detail")
    @Operation(summary = "会员等级详情")
    public UserRankDetailVO detail(){
        return userRankService.detail();
    }

    @PostMapping("/update")
    @Operation(summary = "更新会员等级")
    @PreAuthorize("@pms.hasPermission('userRankModifyManage')")
    public void update(@RequestBody @Validated UserRankEditParam param){
        userRankService.update(param);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新会员等级字段")
    @PreAuthorize("@pms.hasPermission('userRankModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField){
        userRankService.updateField(updateField, USER_RANK_FIELDS);
    }

    @PostMapping("/del")
    @Operation(summary = "删除会员等级")
    @PreAuthorize("@pms.hasPermission('userRankModifyManage')")
    public void del(@RequestBody OperateDTO operate){
        userRankService.del(operate.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('userRankModifyManage')")
    public void batch(@RequestBody BatchDTO batch){
        userRankService.batch(batch);
    }
}
