// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.common;

import cn.hutool.core.util.ObjectUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.common.NavVO;
import com.tigshop.bean.vo.decorate.PcCatFloorListVO;
import com.tigshop.bean.vo.decorate.PcCatFloorListWrapperVO;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.service.decorate.PcCatFloorService;
import com.tigshop.service.decorate.PcNavigationService;
import com.tigshop.service.setting.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.tigshop.common.constant.Constants.CAT_FLOOR;

/**
 * PC控制器
 *
 * @author Tigshop团队
 * @create 2025年01月18日 13:53
 */
@RestController
@RequestMapping("/api/common/pc")
@Tag(name = "PC")
@Slf4j
public class PCController {
    @Resource
    private PcNavigationService pcNavigationService;

    @Resource
    private PcCatFloorService pcCatFloorService;

    @Resource
    private ConfigService configService;

    @Resource
    private RedisCache redisCache;

    @GetMapping("/getNav")
    @Operation(summary = "获取导航")
    public NavVO getNav() {
        return pcNavigationService.getAllNav(0);
    }

    @GetMapping("/getCatFloor")
    @Operation(summary = "获取阶梯菜单")
    public PcCatFloorListWrapperVO getCatFloor() {
        String cacheKey = CAT_FLOOR;
        String headerValue = ThreadLocalUtil.getValue();
        if (ObjectUtil.isNotEmpty(headerValue)) {
            cacheKey = cacheKey + "::" + headerValue;
        }

        PcCatFloorListWrapperVO cacheObject = redisCache.getCacheObject(cacheKey);
        if (cacheObject != null) {
            log.info("从缓存中获取阶梯菜单");
            return cacheObject;
        }
        String configVal = configService.getConfigVal(SettingsEnum.ICO_DEFINED_CSS);
        List<PcCatFloorListVO> catFloor = pcCatFloorService.getCatFloor();
        PcCatFloorListWrapperVO vo = PcCatFloorListWrapperVO.builder()
                .catFloor(catFloor)
                .icoDefinedCss(configVal)
                .build();
        redisCache.setCacheObject(cacheKey, vo, 1, TimeUnit.DAYS);
        return vo;
    }
}