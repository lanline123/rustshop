// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.lang.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.lang.LocalesDTO;
import com.tigshop.bean.dto.lang.TranslationsDataDTO;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.lang.TranslationsData;
import com.tigshop.bean.vo.lang.TranslationsDetailVO;
import com.tigshop.common.enums.DataType;
import com.tigshop.mapper.lang.TranslationsDataMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.lang.LocalesService;
import com.tigshop.service.lang.TranslationsDataService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 翻译数据
 *
 * @author Tigshop团队
 * @create 2024年12月19日 17:13
 */
@Service
public class TranslationsDataServiceImpl extends BaseServiceImpl<TranslationsDataMapper, TranslationsData> implements TranslationsDataService {
    @Resource
    private LocalesService localesService;

    @Override
    public TranslationsDetailVO getTranslations(int dataType, int dataId, String translationName) {
        TranslationsDetailVO translationsDetailVO = new TranslationsDetailVO();
        List<TranslationsDataDTO> data;
        if (dataId > 0) {
            data = getData(dataType, dataId);
        } else {
            data = getData(dataType, translationName);
        }
        translationsDetailVO.setItems(data);
        translationsDetailVO.setId(dataId);
        translationsDetailVO.setDataType(dataType);
        if (ObjectUtil.isNotEmpty(data)) {
            translationsDetailVO.setTranslationName(data.getFirst().getTranslationName());
        }
        return translationsDetailVO;
    }

    @Override
    public List<TranslationsData> getData(Integer dataType, String translationName, List<Integer> localesIds) {
        LambdaQueryWrapper<TranslationsData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TranslationsData::getTranslationName, translationName)
                .eq(TranslationsData::getDataType, dataType)
                .in(TranslationsData::getLocaleId, localesIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<TranslationsDataDTO> getData(Integer dataType, Integer dataId) {
        LambdaQueryWrapper<TranslationsData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TranslationsData::getDataType, dataType)
                .eq(TranslationsData::getDataId, dataId);

        return this.list(queryWrapper).stream().map(data -> {
            TranslationsDataDTO dto = new TranslationsDataDTO();
            BeanUtils.copyProperties(data, dto);
            dto.setLocales(getLocalesDTO(data.getLocaleId()));
            return dto;
        }).toList();
    }

    @Override
    public List<TranslationsDataDTO> getData(Integer dataType, String translationName) {
        LambdaQueryWrapper<TranslationsData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TranslationsData::getDataType, dataType)
                .eq(TranslationsData::getTranslationName, translationName);

        return this.list(queryWrapper).stream().map(data -> {
            TranslationsDataDTO dto = new TranslationsDataDTO();
            BeanUtils.copyProperties(data, dto);
            dto.setLocales(getLocalesDTO(data.getLocaleId()));
            return dto;
        }).toList();
    }

    /**
     * 获取语言地区
     *
     * @param localeId 语言地区ID
     * @return 语言地区
     */
    public LocalesDTO getLocalesDTO(Integer localeId) {
        Locales locales = localesService.getById(localeId);
        if (locales != null) {
            LocalesDTO localesDTO = new LocalesDTO();
            BeanUtils.copyProperties(locales, localesDTO);
            return localesDTO;
        }
        return null;
    }

    @Override
    public Map<String, String> getLocaleTranslations(String code) {
        // 查询语言地区
        Locales locales = localesService.getOne(new LambdaQueryWrapper<Locales>()
                .eq(Locales::getLocaleCode, code)
                .eq(Locales::getIsEnabled, 1)
                .last("limit 1"));

        // 如果语言地区不为空，则查询语言地区下的翻译数据
        if (locales != null) {
            List<TranslationsData> list = this.list(new LambdaQueryWrapper<TranslationsData>()
                    .eq(TranslationsData::getLocaleId, locales.getId())
                    .in(TranslationsData::getDataType, List.of(DataType.PAGE.getCode())));
            return list.stream()
                    .filter(data -> data.getTranslationName() != null && data.getTranslationValue() != null)
                    .collect(Collectors.toMap(
                            TranslationsData::getTranslationName,
                            TranslationsData::getTranslationValue,
                            (existingValue, newValue) -> existingValue
                    ));
        }
        return Map.of();
    }
}
