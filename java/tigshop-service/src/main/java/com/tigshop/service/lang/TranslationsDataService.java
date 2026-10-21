// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.lang;

import com.tigshop.bean.dto.lang.TranslationsDataDTO;
import com.tigshop.bean.model.lang.TranslationsData;
import com.tigshop.bean.vo.lang.TranslationsDetailVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 翻译数据服务
 *
 * @author Tigshop团队
 * @create 2024年12月19日 17:12
 */
public interface TranslationsDataService extends BaseService<TranslationsData> {
    /**
     * 获取翻译信息
     *
     * @param dataType 数据类型
     * @param dataId   数据id
     * @return 翻译信息
     */
    TranslationsDetailVO getTranslations(int dataType, int dataId, String translationName);

    /**
     * 获取翻译信息
     *
     * @param localesIds 语言id
     * @return 翻译信息
     */
    List<TranslationsData> getData(Integer dataType, String translationName, List<Integer> localesIds);

    /**
     * 获取翻译信息
     *
     * @param dataType 翻译数据类型
     * @param dataId 翻译数据id
     * @return 翻译信息
     */
    List<TranslationsDataDTO> getData(Integer dataType, Integer dataId);

    /**
     * 获取翻译信息
     *
     * @param dataType        翻译数据类型
     * @param translationName 翻译名称
     * @return 翻译信息
     */
    List<TranslationsDataDTO> getData(Integer dataType, String translationName);

    /**
     * 获取翻译信息
     *
     * @param code 语言代码
     * @return 翻译信息
     */
    Map<String, String> getLocaleTranslations(String code);

}
