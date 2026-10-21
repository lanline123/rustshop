package com.tigshop.api.warmup;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.decorate.PcCatFloorListVO;
import com.tigshop.bean.vo.decorate.PcCatFloorListWrapperVO;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.service.decorate.PcCatFloorService;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.tigshop.common.constant.Constants.CAT_FLOOR;

/**
 * 缓存预热：应用启动后主动加载高频缓存，避免首次请求冷查询
 */
@Slf4j
@Component
public class CacheWarmUpRunner implements ApplicationRunner {

    @Resource
    private PcCatFloorService pcCatFloorService;

    @Resource
    private ConfigService configService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedisCache redisCache;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String configVal = configService.getConfigVal(SettingsEnum.ICO_DEFINED_CSS);
            List<PcCatFloorListVO> catFloor = pcCatFloorService.getCatFloor();
            PcCatFloorListWrapperVO vo = PcCatFloorListWrapperVO.builder()
                    .catFloor(catFloor)
                    .icoDefinedCss(configVal)
                    .build();
            redisCache.setCacheObject(CAT_FLOOR, vo, 1, TimeUnit.DAYS);
            log.info("缓存预热完成: {}", CAT_FLOOR);
        } catch (Exception e) {
            log.warn("缓存预热失败: {}, 将在首次请求时加载", CAT_FLOOR, e);
        }
        try {
            categoryService.getAllCategory();
            log.info("缓存预热完成: categoryTree snapshot (default locale)");
        } catch (Exception e) {
            log.warn("分类树快照预热失败，将在首次请求时加载", e);
        }
    }
}
