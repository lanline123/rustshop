// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import com.tigshop.common.exception.GlobalException;
import com.volcengine.model.request.translate.TranslateTextRequest;
import com.volcengine.model.response.translate.TranslateTextResponse;
import com.volcengine.service.translate.ITranslateService;
import com.volcengine.service.translate.impl.TranslateServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.tigshop.common.constant.ExceptionConstants.TRANSLATIONS_ERROR;

/**
 * 翻译工具类
 *
 * @author Tigshop团队
 * @create 2025年01月02日 17:42
 */
@Slf4j
public class TranslationsUtils {

    /**
     * 翻译
     *
     * @param ak             ak
     * @param sk             sk
     * @param targetLanguage 目标语言
     * @param textList       待翻译文本
     * @return 翻译结果
     */
    public static TranslateTextResponse.Translation getTranslations(String ak, String sk, String targetLanguage, String textList) {
        ITranslateService translateService  = TranslateServiceImpl.getInstance();
        translateService.setAccessKey(ak);
        translateService.setSecretKey(sk);
        try {
            TranslateTextRequest translateTextRequest = new TranslateTextRequest();
            // 不设置表示自动检测
            // translateTextRequest.setSourceLanguage("en");
            translateTextRequest.setTargetLanguage(targetLanguage);
            translateTextRequest.setTextList(List.of(textList));

            TranslateTextResponse translateText = translateService.translateText(translateTextRequest);
            log.info(JsonUtil.toJson(translateText));
            return translateText.getTranslationList().getFirst();
        } catch (Exception e) {
            throw new GlobalException(TRANSLATIONS_ERROR);
        }
    }

    public static TranslateTextResponse getTranslateTextResponse(String ak, String sk, String targetLanguage, List<String> textList) {
        ITranslateService translateService  = TranslateServiceImpl.getInstance();
        translateService.setAccessKey(ak);
        translateService.setSecretKey(sk);
        try {
            TranslateTextRequest translateTextRequest = new TranslateTextRequest();
            // 不设置表示自动检测
            // translateTextRequest.setSourceLanguage("en");
            translateTextRequest.setTargetLanguage(targetLanguage);
            translateTextRequest.setTextList(textList);

            TranslateTextResponse translateText = translateService.translateText(translateTextRequest);
            log.info(JsonUtil.toJson(translateText));
            return translateText;
        } catch (Exception e) {
            throw new GlobalException(TRANSLATIONS_ERROR);
        }
    }
}
