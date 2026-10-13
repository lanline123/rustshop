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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.TranslateDTO;
import com.tigshop.bean.dto.lang.MultipleTranslationDTO;
import com.tigshop.bean.dto.lang.TranslationsCreateDTO;
import com.tigshop.bean.dto.lang.TranslationsListDTO;
import com.tigshop.bean.dto.lang.TranslationsUpdateDTO;
import com.tigshop.bean.model.lang.Translations;
import com.tigshop.bean.param.lang.TranslationsBatchParam;
import com.tigshop.bean.param.lang.TranslationsCreateParam;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.bean.vo.lang.TranslationsDetailVO;
import com.tigshop.bean.vo.lang.TranslationsVO;
import com.tigshop.service.common.BaseService;
import com.volcengine.model.response.translate.TranslateTextResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 翻译服务
 *
 * @author Tigshop团队
 * @create 2024年12月19日 15:29
 */
public interface TranslationsService extends BaseService<Translations> {

    /**
     * 获取翻译语言3条
     *
     * @return
     */
    List<LocalesVO> getLocalesLimit3();

    /**
     * 翻译列表
     *
     * @param dto 查询参数
     * @return TranslationsListVO
     */
    Page<TranslationsVO> list(TranslationsListDTO dto);

    /**
     * 翻译详情
     *
     * @param id 翻译id
     * @return TranslationsVO
     */
    TranslationsDetailVO detail(Integer id);

    /**
     * 创建翻译
     */
    void create(TranslationsCreateParam param);

    /**
     * 更新翻译
     *
     * @param dto 翻译参数
     * @return boolean
     */
    boolean update(TranslationsUpdateDTO dto);

    /**
     * 翻译
     *
     * @param targetLanguage 翻译语言
     * @param textList       翻译内容
     * @return TranslationsVO
     */
    TranslateTextResponse.Translation getTranslations(String targetLanguage, String textList);

    TranslateTextResponse getTranslationsResponse(String targetLanguage, List<String> textList);

    boolean multiple(MultipleTranslationDTO dto);

    /**
     * 保存翻译内容
     */
    void createTranslations(TranslationsCreateParam param);

    /**
     * 批量操作
     */
    void batch(TranslationsBatchParam param);

    /**
     * 批量创建翻译
     *
     * @param dto
     */
    void batchCreate(TranslationsCreateDTO dto);

    /**
     * 批量保存业务数据翻译（支持图片 URL 翻译结果），逐条独立事务，单条失败不影响其它
     *
     * @param list 多条业务翻译保存参数
     * @return 含 successCount/failCount/errors 的结果
     */
    java.util.Map<String, Object> batchCreateTranslations(List<TranslationsCreateParam> list);

    void batchTranslations(List<Translations> translations, Integer localesId, Integer dataType);

    /**
     * 创建翻译
     *
     * @param dto
     */
    void translationName(TranslateDTO dto);

    @Transactional
    void updateTranslation();
}
