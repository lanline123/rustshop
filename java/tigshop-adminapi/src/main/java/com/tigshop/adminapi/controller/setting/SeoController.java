// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.setting;

import com.tigshop.service.lang.TranslationsService;
import com.tigshop.service.seo.SeoService;
import com.volcengine.model.response.translate.TranslateTextResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.utils.SeoIdentificationUtil.formatEnglishText;

/**
 * seo控制器
 *
 * @author Tigshop团队
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/setting/seo")
@Tag(name = "seo")
public class SeoController {

    @Resource
    private TranslationsService translationsService;
    @Resource
    private SeoService seoService;

    /**
     * 中文转英文并格式化
     * 将中文字符串翻译为英文，并转换为小写，用中划线替换空格，去除其他特殊字符
     *
     * @param chineseText 中文文本
     * @return 格式化后的英文文本
     */
    @GetMapping("translateToSeoIdentification")
    @Operation(summary = "中文转英文并格式化")
    public String translateToEnglish(@RequestParam(value = "chineseText") String chineseText) {
        // 调用翻译服务将中文翻译为英文
        TranslateTextResponse.Translation translation = translationsService.getTranslations("en", chineseText);
        String englishText = translation.getTranslation();
        // 格式化处理：转小写、空格替换为中划线、去除特殊字符
        return formatEnglishText(englishText);
    }

    @PostMapping("batchUpdateCategoryLink")
    @Operation(summary = "商品类目链接批量更新")
    public void batchUpdateCategoryLink() {
        seoService.generateAndUpdateCategorySeoIdentification();
    }

    @PostMapping("batchUpdateProductLink")
    @Operation(summary = "商品链接批量更新")
    public void batchUpdateProductLink() {
        seoService.generateAndUpdateProductSeoIdentification();
    }
}
