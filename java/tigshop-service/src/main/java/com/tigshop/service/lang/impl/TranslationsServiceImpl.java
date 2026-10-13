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

package com.tigshop.service.lang.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.TranslateDTO;
import com.tigshop.bean.dto.lang.MultipleTranslationDTO;
import com.tigshop.bean.dto.lang.TranslationsCreateDTO;
import com.tigshop.bean.dto.lang.TranslationsListDTO;
import com.tigshop.bean.dto.lang.TranslationsUpdateDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.content.Article;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.lang.Translations;
import com.tigshop.bean.model.lang.TranslationsData;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.param.lang.TranslationsBatchParam;
import com.tigshop.bean.param.lang.TranslationsCreateParam;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.bean.vo.lang.TranslationsDetailVO;
import com.tigshop.bean.vo.lang.TranslationsVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.enums.TranslationUpdateType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.TranslationsUtils;
import com.tigshop.mapper.lang.TranslationsMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.ArticleService;
import com.tigshop.service.lang.ImageI18nService;
import com.tigshop.service.lang.LocalesService;
import com.tigshop.service.lang.TranslationsDataService;
import com.tigshop.service.lang.TranslationsService;
import com.tigshop.service.product.BrandService;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import com.volcengine.model.response.translate.TranslateTextResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.setting.SettingsEnum.TRANSLATION_VERSION;
import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.ExceptionConstants.CODE_PARAM_ERROR;
import static com.tigshop.common.constant.ResultTextConstants.FAIL_CREATE;
import static com.tigshop.common.constant.ResultTextConstants.FAIL_UPDATE;

/**
 * 翻译服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月19日 16:42
 */
@Service
@Slf4j
public class TranslationsServiceImpl extends BaseServiceImpl<TranslationsMapper, Translations> implements TranslationsService {
    @Resource
    private LocalesService localesService;

    @Resource
    private TranslationsDataService translationsDataService;

    @Resource
    private ImageI18nService imageI18nService;

    @Resource
    private ConfigService configService;

    @Resource
    private ProductService productService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private BrandService brandService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ArticleService articleService;

    @Resource
    @Lazy
    private TranslationsService self;

    @Override
    public List<LocalesVO> getLocalesLimit3() {
        return localesService.getLocalesLimit3();
    }

    @Override
    public Page<TranslationsVO> list(TranslationsListDTO dto) {
        Page<Translations> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Translations> queryWrapper = new LambdaQueryWrapper<>();
        // 翻译内容名称
        String translationName = dto.getTranslationName();
        if (StrUtil.isNotEmpty(translationName)) {
            queryWrapper.like(Translations::getTranslationName, translationName);
        }
        /*String localeCode = dto.getLocaleCode();
        if (StrUtil.isNotEmpty(localeCode)) {
            queryWrapper.eq("locale_code", localeCode);
        }*/
        queryWrapper.eq(ObjectUtil.isNotEmpty(getShopId()), Translations::getShopId, getShopId());
        String dataType = dto.getDataType();
        if (StrUtil.isNotEmpty(dataType)) {
            queryWrapper.eq(Translations::getDataType, dataType);
        }
        // 获取翻译内容集合
        List<Integer> localeIds = StrUtil.split(dto.getLocaleIds(), StrUtil.COMMA).stream().map(Integer::parseInt).toList();

        List<TranslationsVO> list = this.list(page, queryWrapper).stream()
                .map(item -> getTranslationsVO(item, localeIds)).toList();
        return PageUtil.convertPage(page, list);
    }

    @Override
    public TranslationsDetailVO detail(Integer id) {
        Translations translations = this.getById(id);
        if (translations != null) {
            TranslationsDetailVO translationsDetailVO = new TranslationsDetailVO();
            BeanUtils.copyProperties(translations, translationsDetailVO);
            // 优先通过 translationName 查询，因为 translations_data 表中 data_id 可能为空
            translationsDetailVO.setItems(translationsDataService.getData(translations.getDataType(), translations.getTranslationName()));
            return translationsDetailVO;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TranslationsCreateParam param) {

        Integer isExists = this.count(Wrappers.lambdaQuery(Translations.class)
                .eq(Translations::getTranslationName, param.getTranslationName())
                .eq(Translations::getDataType, param.getDataType())
                .eq(Translations::getShopId, getShopId())) > 0 ? YES : NO;

        if (isExists.equals(YES)) {
            throw new GlobalException("已添加该翻译,请勿重复添加");
        }

        // 创建 Translations 实体并设置相关字段
        Translations translations = new Translations();
        Integer dataType = param.getDataType();
        translations.setDataType(dataType);

        // 设置翻译名称和翻译键
        String translationName = param.getTranslationName();
        translations.setTranslationName(translationName);

        // 设置店铺 ID 和 MD5 加密的翻译键
        translations.setShopId(getShopId());
        String translationKey = SecureUtil.md5(translationName);
        translations.setTranslationKey(translationKey);

        // 保存 Translations 实体
        if (this.count(new LambdaQueryWrapper<Translations>()
                .eq(Translations::getTranslationKey, translationKey)
                .eq(Translations::getDataType, dataType)) == 0) {
            this.save(translations);

        }


        // 调用提取的批量处理方法
        processTranslationsData(param.getItems(), translations.getId(), translationName, dataType, translationKey);

        updateTranslation();
    }

    /**
     * 处理翻译数据
     *
     * @param translationsDatas 翻译数据
     * @param id                translations主键
     * @param translationName   翻译名称
     * @param dataType          数据类型
     * @param translationKey    翻译键
     */
    @Transactional(rollbackFor = Exception.class)
    protected void processTranslationsData(List<TranslationsData> translationsDatas,
                                           Integer id,
                                           String translationName,
                                           Integer dataType,
                                           String translationKey) {
        // 获取翻译键对应的翻译数据数量
        long count = translationsDataService.count(
                new LambdaQueryWrapper<TranslationsData>()
                        .eq(TranslationsData::getTranslationKey, translationKey)
                        .eq(TranslationsData::getDataType, dataType)
        );

        // 如果翻译数据不为空
        if (translationsDatas != null && !translationsDatas.isEmpty()) {

            if (count > 0) {
                // 遍历设置值
                translationsDatas.forEach(item -> {
                    item.setTranslationName(translationName);
                    item.setDataId(id);
                    item.setDataType(dataType);
                    item.setTranslationKey(translationKey);
                    item.setId(getTranslationsDataId(item));
                });
                // 批量更新翻译数据
                boolean isUpdated = translationsDataService.updateBatchById(translationsDatas);
                if (!isUpdated) {
                    throw new GlobalException(FAIL_UPDATE);
                }
            } else {
                // 遍历设置值
                translationsDatas.forEach(item -> {
                    item.setTranslationName(translationName);
                    item.setDataId(id);
                    item.setDataType(dataType);
                    item.setTranslationKey(translationKey);
                });
                // 批量保存翻译数据
                boolean isCreated = translationsDataService.saveBatch(translationsDatas);
                if (!isCreated) {
                    throw new GlobalException(FAIL_CREATE);
                }
            }
        }
    }

    /**
     * 获取翻译数据id
     *
     * @param item 翻译数据参数
     * @return Integer
     */
    public Integer getTranslationsDataId(TranslationsData item) {
        Integer localeId = item.getLocaleId();
        String translationKey = item.getTranslationKey();
        if (localeId == null || translationKey == null) {
            throw new GlobalException(CODE_PARAM_ERROR);
        }
        // 如果查到了就返回数据库中的id
        // 历史数据中同一 (translationKey, dataType, localeId) 可能存在多行，取最早一条，避免 selectOne 抛异常
        TranslationsData translationsData = translationsDataService.getOne(
                new LambdaUpdateWrapper<TranslationsData>()
                        .eq(TranslationsData::getTranslationKey, translationKey)
                        .eq(TranslationsData::getDataType, item.getDataType())
                        .eq(TranslationsData::getLocaleId, localeId)
                        .orderByAsc(TranslationsData::getId)
                        .last("limit 1"));

        if (translationsData != null) {
            return translationsData.getId();
        }
        // 没查到就手动保存一条，然后再返回id
        boolean save = translationsDataService.save(item);
        if (!save) {
            throw new GlobalException(FAIL_CREATE);
        }
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(TranslationsUpdateDTO dto) {

        Translations oldTranslations = this.getById(dto.getId());
        // 判断翻译名称是否已存在
        if (!oldTranslations.getTranslationName().equals(dto.getTranslationName())) {
            Integer isExists = this.count(Wrappers.lambdaQuery(Translations.class)
                    .eq(Translations::getTranslationName, dto.getTranslationName())
                    .eq(Translations::getDataType, dto.getDataType())
                    .eq(Translations::getShopId, getShopId())) > 0 ? YES : NO;

            if (isExists.equals(YES)) {
                throw new GlobalException("已添加该翻译,请勿重复添加");
            }
        }
        Translations translations = new Translations();
        BeanUtils.copyProperties(dto, translations);
        boolean isUpdated = this.updateById(translations);
        if (isUpdated) {
            processTranslationsData(dto.getItems(), dto.getId(), dto.getTranslationName(), dto.getDataType(), dto.getTranslationKey());
        }
        updateTranslation();
        return isUpdated;
    }

    /**
     * 获取翻译内容
     *
     * @param targetLanguage 目标语言
     * @param textList       翻译内容
     * @return TranslationTextVO
     */
    @Override
    public TranslateTextResponse.Translation getTranslations(String targetLanguage, String textList) {
        // 获取语言配置
        String langVolcengineAccessKey = configService.getConfigByCode(SettingsEnum.LANG_VOLCENGINE_ACCESS_KEY.getBizCode()).getBizVal();
        String langVolcengineSecret = configService.getConfigByCode(SettingsEnum.LANG_VOLCENGINE_SECRET.getBizCode()).getBizVal();

        // 获取翻译内容
        TranslateTextResponse.Translation translations = TranslationsUtils.getTranslations(
                langVolcengineAccessKey,
                langVolcengineSecret,
                targetLanguage, textList);

        Map<String, Object> detectedSourceLanguage = Map.of(
                "DetectedSourceLanguage", translations.getDetectedSourceLanguage(),
                "Translation", translations.getTranslation()
        );
        return translations;
    }

    @Override
    public TranslateTextResponse getTranslationsResponse(String targetLanguage, List<String> textList) {
        // 获取语言配置
        String langVolcengineAccessKey = configService.getConfigByCode(SettingsEnum.LANG_VOLCENGINE_ACCESS_KEY.getBizCode()).getBizVal();
        String langVolcengineSecret = configService.getConfigByCode(SettingsEnum.LANG_VOLCENGINE_SECRET.getBizCode()).getBizVal();

        // 获取翻译内容
        TranslateTextResponse translateTextResponse = TranslationsUtils.getTranslateTextResponse(langVolcengineAccessKey,
                langVolcengineSecret, targetLanguage, textList);
        if (translateTextResponse.getResponseMetadata().getError() != null) {
            throw new GlobalException("请检查语言翻译服务密钥配置");
        }
        return translateTextResponse;
    }

    @Transactional
    @Override
    public boolean multiple(MultipleTranslationDTO dto) {
        Integer shopId = getShopId();
        Integer dataType = dto.getDataType();
        Integer updateType = dto.getUpdateType();
        int offset = (dto.getPage() - 1) * dto.getSize();

        if (DataType.PAGE.getCode() == dataType || DataType.API.getCode() == dataType) {
            List<Translations> translations = this.lambdaQuery()
                    .eq(Translations::getDataType, dataType)
                    .eq(shopId != null, Translations::getShopId, shopId)
                    .in(CollUtil.isNotEmpty(dto.getIds()), Translations::getId, dto.getIds())
                    .orderByDesc(Translations::getId)
                    .last("LIMIT " + dto.getSize() + " OFFSET " + offset)
                    .list();

            this.dispatchBatchTranslations(translations, dto.getLocalesId(), dataType, updateType);
        }

        if (DataType.PRODUCT.getCode() == dataType) {
            List<Product> products = productService.lambdaQuery()
                    .eq(Product::getIsDelete, NO)
                    .eq(shopId != null, Product::getShopId, shopId)
                    .in(CollUtil.isNotEmpty(dto.getIds()), Product::getProductId, dto.getIds())
                    .orderByDesc(Product::getProductId)
                    .last("LIMIT " + dto.getSize() + " OFFSET " + offset)
                    .list();

            List<Translations> translations = products.stream()
                    .map(item -> {
                        MD5 md5 = MD5.create();
                        String translationKey = md5.digestHex(item.getProductName());
                        return Translations.builder()
                                .id(item.getProductId())
                                .dataType(dataType)
                                .translationKey(translationKey)
                                .translationName(item.getProductName())
                                .build();
                    }).toList();
            this.dispatchBatchTranslations(translations, dto.getLocalesId(), dataType, updateType);
        }

        if (DataType.CATEGORY.getCode() == dataType) {
            List<Category> categories = categoryService.lambdaQuery()
                    .in(CollUtil.isNotEmpty(dto.getIds()), Category::getCategoryId, dto.getIds())
                    .orderByDesc(Category::getCategoryId)
                    .last("LIMIT " + dto.getSize() + " OFFSET " + offset)
                    .list();

            List<Translations> translations = categories.stream()
                    .map(item -> {
                        MD5 md5 = MD5.create();
                        String translationKey = md5.digestHex(item.getCategoryName());
                        return Translations.builder()
                                .id(item.getCategoryId())
                                .dataType(dataType)
                                .translationKey(translationKey)
                                .translationName(item.getCategoryName())
                                .build();
                    }).toList();
            this.dispatchBatchTranslations(translations, dto.getLocalesId(), dataType, updateType);
        }

        if (DataType.BRAND.getCode() == dataType) {
            List<Brand> brands = brandService.lambdaQuery()
                    .in(CollUtil.isNotEmpty(dto.getIds()), Brand::getBrandId, dto.getIds())
                    .orderByDesc(Brand::getBrandId)
                    .last("LIMIT " + dto.getSize() + " OFFSET " + offset)
                    .list();

            List<Translations> translations = brands.stream()
                    .map(item -> {
                        MD5 md5 = MD5.create();
                        String translationKey = md5.digestHex(item.getBrandName());
                        return Translations.builder()
                                .id(item.getBrandId())
                                .dataType(dataType)
                                .translationKey(translationKey)
                                .translationName(item.getBrandName())
                                .build();
                    }).toList();
            this.dispatchBatchTranslations(translations, dto.getLocalesId(), dataType, updateType);
        }

        if (DataType.ARTICLE_TITLE.getCode() == dataType) {
            List<Article> articles = articleService.lambdaQuery()
                    .in(CollUtil.isNotEmpty(dto.getIds()), Article::getArticleId, dto.getIds())
                    .orderByDesc(Article::getArticleId)
                    .last("LIMIT " + dto.getSize() + " OFFSET " + offset)
                    .list();

            List<Translations> translations = articles.stream()
                    .map(item -> {
                        MD5 md5 = MD5.create();
                        String translationKey = md5.digestHex(item.getArticleTitle());
                        return Translations.builder()
                                .id(item.getArticleId())
                                .dataType(dataType)
                                .translationKey(translationKey)
                                .translationName(item.getArticleTitle())
                                .build();
                    }).toList();
            this.dispatchBatchTranslations(translations, dto.getLocalesId(), dataType, updateType);
        }

        if (DataType.SETTING.getCode() == dataType) {
            String shopNameVal = configService.getConfigByCode(SettingsEnum.SHOP_NAME.getBizCode()).getBizVal();
            String shopTitleVal = configService.getConfigByCode(SettingsEnum.SHOP_TITLE.getBizCode()).getBizVal();
            String shopTitleSuffixVal = configService.getConfigByCode(SettingsEnum.SHOP_TITLE_SUFFIX.getBizCode()).getBizVal();
            String shopDescVal = configService.getConfigByCode(SettingsEnum.SHOP_DESC.getBizCode()).getBizVal();
            String shopKeywordsVal = configService.getConfigByCode(SettingsEnum.SHOP_KEYWORDS.getBizCode()).getBizVal();
            String kefuAddressVal = configService.getConfigByCode(SettingsEnum.KEFU_ADDRESS.getBizCode()).getBizVal();
            String shopIcpNoVal = configService.getConfigByCode(SettingsEnum.SHOP_ICP_NO.getBizCode()).getBizVal();


            List<Translations> translations = new ArrayList<>() {
                {
                    add(new Translations(1, dataType, shopNameVal));
                    add(new Translations(2, dataType, shopTitleVal));
                    add(new Translations(3, dataType, shopTitleSuffixVal));
                    add(new Translations(4, dataType, shopDescVal));
                    add(new Translations(5, dataType, shopKeywordsVal));
                    add(new Translations(6, dataType, kefuAddressVal));
                    add(new Translations(7, dataType, shopIcpNoVal));
                }
            };

            this.dispatchBatchTranslations(translations, dto.getLocalesId(), dataType, updateType);
        }
        updateTranslation();
        return true;
    }

    /**
     * 根据 updateType 预处理后委派给 batchTranslations。
     * updateType=2（OVERWRITE_ALL）时，先按 (locale_id, data_type, data_id IN batch) 清空本批已有译文，
     * 再走"仅翻译未维护"的统一流程，等价于全量覆盖；其他情况直接走"仅翻译未维护"。
     */
    private void dispatchBatchTranslations(List<Translations> translations, Integer localesId, Integer dataType, Integer updateType) {
        if (CollUtil.isEmpty(translations)) {
            return;
        }
        if (TranslationUpdateType.isOverwriteAll(updateType)) {
            List<Integer> dataIds = translations.stream().map(Translations::getId).filter(Objects::nonNull).toList();
            if (CollUtil.isNotEmpty(dataIds)) {
                translationsDataService.lambdaUpdate()
                        .eq(TranslationsData::getLocaleId, localesId)
                        .eq(TranslationsData::getDataType, dataType)
                        .in(TranslationsData::getDataId, dataIds)
                        .remove();
            }
        }
        this.batchTranslations(translations, localesId, dataType);
    }

    @Transactional
    @Override
    public void createTranslations(TranslationsCreateParam param) {
        String translationKey = SecureUtil.md5(param.getTranslationName());
        if (CollUtil.isNotEmpty(param.getItems())) {
            // 仅商品图片场景写入 translationSource
            boolean handleSource = param.getDataType() != null && param.getDataType() == DataType.IMAGE_TITLE.getCode();

            for (TranslationsData item : param.getItems()) {
                if (item.getLocaleId() == null) {
                    continue;
                }
                // 按 (translationKey, dataType, localeId) 幂等 upsert：仅更新/插入本次 locale，
                // 不触碰同 key 其它语言、也不触碰同 dataId 其它图片的译文
                TranslationsData existing = translationsDataService.getOne(new LambdaQueryWrapper<TranslationsData>()
                        .eq(TranslationsData::getTranslationKey, translationKey)
                        .eq(TranslationsData::getDataType, param.getDataType())
                        .eq(TranslationsData::getLocaleId, item.getLocaleId())
                        .last("limit 1"));

                Integer source = item.getTranslationSource() != null ? item.getTranslationSource() : param.getTranslationSource();
                Integer persistSource = handleSource ? source : null;

                if (existing != null) {
                    existing.setTranslationName(param.getTranslationName());
                    existing.setTranslationValue(item.getTranslationValue());
                    existing.setDataId(param.getDataId());
                    if (persistSource != null) {
                        existing.setTranslationSource(persistSource);
                    }
                    translationsDataService.updateById(existing);
                } else {
                    TranslationsData data = TranslationsData.builder()
                            .localeId(item.getLocaleId())
                            .translationName(param.getTranslationName())
                            .translationKey(translationKey)
                            .translationValue(item.getTranslationValue())
                            .dataType(param.getDataType())
                            .dataId(param.getDataId())
                            .translationSource(persistSource)
                            .build();
                    translationsDataService.save(data);
                }

                Locales locales = localesService.getById(item.getLocaleId());
                if (locales != null) {
                    String key = item.getTranslationValue() + "_" + locales.getLocaleCode();
                    redisCache.setCacheObject(key, item.getTranslationValue());
                    // 图片译文（dataType 7=商品图片；8=规格图片）双写到 image_i18n 专用表并清缓存；
                    // dataType 8 的规格文本不写（按 translationName 是否像图片 URL 判断）。
                    if (isImageTranslation(param.getDataType(), param.getTranslationName())) {
                        imageI18nService.upsert(item.getLocaleId(), locales.getLocaleCode(),
                                param.getDataType(), param.getTranslationName(), item.getTranslationValue());
                    }
                }
            }
        }
        updateTranslation();
    }

    /**
     * 判断是否为图片本地化译文：仅图片类（7/8 的图片部分）才双写 image_i18n。
     * dataType 7 全为商品图片，无条件写；dataType 8 文本+图片混用，须按"图片 URL/路径特征"
     * 严格判定，避免把含 "/" 的规格文本（如尺码表 "165/84A 推荐57-62kg"）误写入 image_i18n。
     * 判定口径与迁移 SQL 完全一致：http* / img/* / *!!pic* / 图片扩展名。
     */
    static boolean isImageTranslation(Integer dataType, String translationName) {
        if (dataType == null || translationName == null) {
            return false;
        }
        if (dataType == DataType.IMAGE_TITLE.getCode()) {
            return true;
        }
        if (dataType == DataType.SKU.getCode()) {
            String name = translationName.toLowerCase();
            return name.startsWith("http")
                    || name.startsWith("img/")
                    || name.contains("!!pic")
                    || name.endsWith(".jpg") || name.endsWith(".jpeg")
                    || name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(".gif");
        }
        return false;
    }

    @Override
    public Map<String, Object> batchCreateTranslations(List<TranslationsCreateParam> list) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TranslationsCreateParam param = list.get(i);
                try {
                    if (param == null || ObjectUtil.isEmpty(param.getTranslationName())) {
                        throw new GlobalException("第" + (i + 1) + "条翻译原文不能为空");
                    }
                    if (param.getDataType() == null) {
                        throw new GlobalException("第" + (i + 1) + "条数据类型不能为空");
                    }
                    // 通过 self 代理逐条进入独立事务，单条失败不影响其它
                    self.createTranslations(param);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "条: " + e.getMessage());
                    log.error("批量保存业务翻译失败, index={}, msg={}", i, e.getMessage());
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    @Transactional
    @Override
    public void batch(TranslationsBatchParam param) {
        if ("del".equals(param.getType())) {
            List<Translations> translations = this.listByIds(param.getIds());
            List<Integer> translationIds = translations.stream().map(Translations::getId).toList();

            List<TranslationsData> translationsDataList = translationsDataService.lambdaQuery()
                    .in(TranslationsData::getDataId, translationIds)
                    .list();
            this.removeBatchByIds(translations);
            // translationsDataList 删除缓存
            List<Integer> localeIds = translationsDataList.stream().map(TranslationsData::getLocaleId).toList();
            if (ObjectUtil.isEmpty(localeIds)) {
                return;
            }
            List<Locales> localesList = localesService.listByIds(localeIds);
            Map<Integer, List<Locales>> localeMap = localesList.stream().collect(Collectors.groupingBy(Locales::getId, Collectors.toList()));
            translationsDataList.forEach(item -> {
                List<Locales> currLocales = localeMap.get(item.getLocaleId());

                if (CollUtil.isNotEmpty(currLocales)) {
                    Locales locales = currLocales.getFirst();

                    String key = item.getTranslationValue() + "_" + locales.getLocaleCode();
                    redisCache.setCacheObject(key, item.getTranslationValue());
                }
            });
            translationsDataService.removeBatchByIds(translationsDataList);
        }
        updateTranslation();
    }


    @Override
    @Transactional
    public void batchCreate(TranslationsCreateDTO dto) {
        List<Locales> list = localesService.list(new LambdaQueryWrapper<Locales>().eq(Locales::getIsEnabled, 1));
        if (ObjectUtil.isEmpty(dto.getTranslationName())) {
            throw new GlobalException("内容不能为空");
        }

        List<Translations> translationsList = new ArrayList<>();
        String[] split = dto.getTranslationName().split("\n");
        for (String translationName : split) {
            for (Integer dateType : dto.getDataType()) {
                if (ObjectUtil.isNotEmpty(translationName)) {
                    String translationKey = SecureUtil.md5(translationName);
                    long count = count(new LambdaQueryWrapper<Translations>()
                            .eq(Translations::getTranslationKey, translationKey)
                            .eq(Translations::getDataType, dateType)
                    );
                    if (count > 0) {
                        throw new GlobalException("该内容已存在.:" + translationName);
                    }
                    Translations translations = Translations.builder()
                            .translationName(translationName)
                            .translationKey(translationKey)
                            .dataType(dateType)
                            .build();
                    save(translations);
                    translationsList.add(translations);
                }
            }
        }
        list.forEach(item -> {
            for (Integer dateType : dto.getDataType()) {
                batchTranslations(translationsList, item.getId(), dateType);
            }
        });
        updateTranslation();
    }

    @Override
    @Transactional
    public void batchTranslations(List<Translations> translations, Integer localesId, Integer dataType) {
        Locales locales = localesService.lambdaQuery().eq(Locales::getId, localesId).one();
        String localeCode = locales.getLocaleCode();

        if (ObjectUtil.isEmpty(translations)) {
            log.error("翻译失败，翻译数据为空");
            return;
        }

        List<Translations> surplusTranslations = new ArrayList<>();
        // 仅对 translation_value 为空的占位行做清理，非空行保留不动，避免覆盖人工已校对的译文
        List<Integer> emptyRecordIds = new ArrayList<>();

        translations.forEach(item -> {
            // 历史数据中同一 (dataId, dataType, localeId) 可能存在多行，按列表取，避免 selectOne 抛异常
            List<TranslationsData> existingList = translationsDataService.lambdaQuery()
                    .eq(TranslationsData::getDataId, item.getId())
                    .eq(TranslationsData::getDataType, item.getDataType())
                    .eq(TranslationsData::getLocaleId, localesId)
                    .orderByAsc(TranslationsData::getId)
                    .list();

            TranslationsData translated = existingList.stream()
                    .filter(data -> ObjectUtil.isNotEmpty(data.getTranslationValue()))
                    .findFirst().orElse(null);

            if (translated != null) {
                String key = TRANSLATE + localeCode + "::" + translated.getTranslationName();
                redisCache.setCacheObject(key, translated.getTranslationValue());
            } else {
                existingList.forEach(data -> emptyRecordIds.add(data.getId()));
                surplusTranslations.add(item);
            }

        });

        if (CollUtil.isNotEmpty(emptyRecordIds)) {
            translationsDataService.removeBatchByIds(emptyRecordIds);
        }

        if (CollUtil.isNotEmpty(surplusTranslations)) {
            List<String> translationNames = surplusTranslations.stream().map(Translations::getTranslationName).toList();
            TranslateTextResponse translationsResponse = this.getTranslationsResponse(localeCode, translationNames);
            List<TranslateTextResponse.Translation> translationList = translationsResponse.getTranslationList();

            List<TranslationsData> newTranslationsDataList = new ArrayList<>();
            for (int i = 0; i < surplusTranslations.size(); i++) {
                Translations item = surplusTranslations.get(i);

                TranslationsData translationsData = TranslationsData.builder()
                        .localeId(localesId)
                        .translationName(item.getTranslationName())
                        .translationKey(item.getTranslationKey())
                        .translationValue(translationList.get(i).getTranslation())
                        .dataType(item.getDataType())
                        .dataId(item.getId())
                        .build();
                newTranslationsDataList.add(translationsData);

                String key = TRANSLATE + localeCode + "::" + translationsData.getTranslationName();
                if (ObjectUtil.isNotEmpty(translationsData.getTranslationValue())) {
                    redisCache.setCacheObject(key, translationsData.getTranslationValue());
                }
            }

            translationsDataService.saveBatch(newTranslationsDataList);
        }

    }

    /**
     * 获取翻译内容VO
     *
     * @param translations db数据
     * @param localesIds   语言id
     * @return TranslationsVO
     */
    public TranslationsVO getTranslationsVO(Translations translations, List<Integer> localesIds) {
        TranslationsVO translationsVO = new TranslationsVO();
        BeanUtils.copyProperties(translations, translationsVO);
        if (CollUtil.isNotEmpty(localesIds)) {
            translationsVO.setTdata(
                    translationsDataService.getData(
                            translations.getDataType(), translations.getTranslationName(), localesIds));
        }
        return translationsVO;
    }

    @Transactional
    @Override
    public void translationName(TranslateDTO dto) {
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.TRANSLATION_NAME_ROUTING_KEY, dto);
    }

    @Transactional
    @Override
    public void updateTranslation() {
        configService.lambdaUpdate().eq(ConfigPO::getBizCode, TRANSLATION_VERSION.getBizCode())
                .setSql("biz_val = CASE WHEN biz_val >= 100000 THEN 1 ELSE biz_val + 1 END").update();
    }
}
