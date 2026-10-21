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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserPointsLogDTO;
import com.tigshop.bean.query.user.UserPointsLogListPageQuery;
import com.tigshop.service.user.UserPointsLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/user/userPointsLog")
@Tag(name="会员积分日志")
@PreAuthorize("@pms.hasPermission('integralLogManage')")
public class UserPointsLogController {
    @Resource
    private UserPointsLogService userPointsLogService;
    @GetMapping("/list")
    @Operation(summary = "会员积分列表")
    public Page<UserPointsLogDTO> list(UserPointsLogListPageQuery dto) {return userPointsLogService.list(dto);}

    @GetMapping("/getPoints")
    @Operation(summary = "获取会员积分")
    public Integer getPoints() {return userPointsLogService.getPoints();}

}
