package com.tigshop.common.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 属性自动填充处理器
 *
 * @author kidd
 * @since 2025/4/1 11:13
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        long nowSecond = Instant.now().getEpochSecond();

        Long createById = Long.valueOf(SecurityUtils.getCurrentAdminId());
        String createByName = StrUtil.isNotBlank(SecurityUtils.getCurrentUsername()) ? SecurityUtils.getCurrentUsername() : "unknown";

        this.strictInsertFill(metaObject, "createTime", () -> nowSecond, Long.class);
        this.strictInsertFill(metaObject, "createById", () -> createById, Long.class);
        this.strictInsertFill(metaObject, "createByName", () -> createByName, String.class);


        this.strictInsertFill(metaObject, "updateTime", () -> nowSecond, Long.class);
        this.strictInsertFill(metaObject, "updateById", () -> createById, Long.class);
        this.strictInsertFill(metaObject, "updateByName", () -> createByName, String.class);

        this.strictInsertFill(metaObject, "isDel", () -> Constants.NO, Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        long nowSecond = Instant.now().getEpochSecond();

        Long createById = Long.valueOf(SecurityUtils.getCurrentAdminId());
        String createByName = StrUtil.isNotBlank(SecurityUtils.getCurrentUsername()) ? SecurityUtils.getCurrentUsername() : "unknown";

        this.strictInsertFill(metaObject, "updateTime", () -> nowSecond, Long.class);
        this.strictInsertFill(metaObject, "updateById", () -> createById, Long.class);
        this.strictInsertFill(metaObject, "updateByName", () -> createByName, String.class);
    }

}
