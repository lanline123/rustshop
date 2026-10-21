package com.tigshop.core;

//**---------------------------------------------------------------------+
//** 配置项 -- 提示
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jayce
 * @create 2024/9/26 15:03
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class TranslatePackageImpl implements TranslatePackage {


    private final RedisCache redisCache;
    private final TigshopProperties tigshopProperties;

    /**
     * 获取提示返回内容
     *
     * @param text 提示
     * @return prompt
     */
    public String translate(String text) {

        return translate(text, DataType.API.getCode());
    }

    /**
     * 获取提示返回内容
     *
     * @param text     提示
     * @param dataType 翻译类型
     * @return prompt
     */
    public String translate(String text, Integer dataType) {
        // 从ThreadLocal中获取请求头中的语言类型
        String headerValue = ThreadLocalUtil.getValue();
        return translate(text, dataType, headerValue);
    }

    public String translate(String text, Integer dataType, String localeCode) {
        if (localeCode == null || localeCode.trim().isEmpty()) {
            return text;
        }
        return text;
    }

    @Override
    public Map<String, String> translateBatch(Collection<String> texts, Integer dataType, String localeCode) {
        Map<String, String> result = new HashMap<>();
        if (texts == null || texts.isEmpty()) {
            return result;
        }
        for (String text : texts) {
            if (text != null) {
                result.put(text, text);
            }
        }
        return result;
    }

}
