package com.tigshop.bean.vo.setting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * icon配置
 *
 * @author kidd
 * @since 2025/4/10 09:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IconSettingsVO {

    private List<String> icoTig;

    private String tigClass;

    private List<String> icoDefined;

    private String definedClass;
}
