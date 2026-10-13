// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.user;

import com.tigshop.bean.query.user.UserPointsLogListPageQuery;
import com.tigshop.bean.vo.user.UserPointsLogPageVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.user.UserPointsLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分列表控制器
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/pointsLog"))
@Tag(name = "积分列表")
public class PointsLogController {

    @Resource
    private UserPointsLogService userPointsLogService;

    @GetMapping("/list")
    @Operation(summary = "积分列表")
    public UserPointsLogPageVO list(UserPointsLogListPageQuery pageQuery) {
        pageQuery.setUserId(SecurityUtils.getCurrentUserId());
        return userPointsLogService.listPage(pageQuery);
    }
}