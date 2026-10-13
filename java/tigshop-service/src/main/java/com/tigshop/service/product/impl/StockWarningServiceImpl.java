package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.product.StockWarningBatchSaveDTO;
import com.tigshop.bean.dto.product.StockWarningExportDTO;
import com.tigshop.bean.dto.product.StockWarningListDTO;
import com.tigshop.bean.dto.product.StockWarningSaveDTO;
import com.tigshop.bean.dto.product.StockWarningSettingSaveDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.product.StockWarningOwnerTypeEnum;
import com.tigshop.bean.enums.product.StockWarningStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.StockWarningRule;
import com.tigshop.bean.model.product.StockWarningSetting;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.config.GetStoreSelfOperatedVO;
import com.tigshop.bean.vo.product.StockWarningBatchResultVO;
import com.tigshop.bean.vo.product.StockWarningEvaluateRowVO;
import com.tigshop.bean.vo.product.StockWarningExcelVO;
import com.tigshop.bean.vo.product.StockWarningImportErrorExcelVO;
import com.tigshop.bean.vo.product.StockWarningImportExcelVO;
import com.tigshop.bean.vo.product.StockWarningImportResultVO;
import com.tigshop.bean.vo.product.StockWarningPageVO;
import com.tigshop.bean.vo.product.StockWarningRowVO;
import com.tigshop.bean.vo.product.StockWarningSettingVO;
import com.tigshop.bean.vo.product.StockWarningSnTargetVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.SkuValueTranslateUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.StockWarningRuleMapper;
import com.tigshop.mapper.product.StockWarningSettingMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.product.StockWarningService;
import com.tigshop.service.setting.ConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.tigshop.common.constant.Constants.XLSX;

/**
 * 库存预警服务实现类
 *
 * @author Tigshop团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockWarningServiceImpl extends BaseServiceImpl<StockWarningRuleMapper, StockWarningRule>
        implements StockWarningService {

    /** 未配置规则的商品/SKU 沿用升级前的固定阈值，保证存量环境预警能力不中断 */
    public static final int DEFAULT_LOWER_LIMIT = 100;

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;
    /** 导出中未配置项的占位符，与页面展示保持一致 */
    private static final String EMPTY_CELL = "--";

    private static final String TEMPLATE_FILE_NAME = "库存预警导入模板";
    /** 模板列顺序，与 StockWarningImportExcelVO 的字段声明保持一致 */
    private static final Set<String> TEMPLATE_COLUMNS =
            new LinkedHashSet<>(List.of("productSn", "skuSn", "lowerLimit", "upperLimit"));
    /** 编码命中方式：命中 product_sku.sku_sn */
    private static final int MATCH_TYPE_SKU_SN = 1;
    private static final int MATCH_TYPE_PRODUCT_SN = 2;
    /** Excel 第 1 行是表头，数据从第 2 行开始 */
    private static final int FIRST_DATA_ROW = 2;
    private static final int SN_QUERY_BATCH_SIZE = 500;

    private final StockWarningSettingMapper stockWarningSettingMapper;
    private final ConfigService configService;
    private final TigshopProperties tigshopProperties;
    private final AdminMsgService adminMsgService;
    private final ShopMapper shopMapper;
    private final TranslatePackage translatePackage;

    @Override
    public StockWarningSettingVO getSetting(Integer shopId) {
        int ownerShopId = normalizeShopId(shopId);
        StockWarningSetting setting = stockWarningSettingMapper.selectById(ownerShopId);
        StockWarningSettingVO vo = new StockWarningSettingVO();
        vo.setShopId(ownerShopId);
        // 无记录视为开启，升级后存量环境的预警能力不中断
        vo.setNotifyEnabled(setting == null ? ENABLED : setting.getNotifyEnabled());
        vo.setDefaultLowerLimit(DEFAULT_LOWER_LIMIT);
        return vo;
    }

    @Override
    public void saveSetting(StockWarningSettingSaveDTO saveDTO) {
        int ownerShopId = normalizeShopId(saveDTO.getShopId());
        int notifyEnabled = Objects.equals(saveDTO.getNotifyEnabled(), ENABLED) ? ENABLED : DISABLED;
        long now = StringUtils.getCurrentTime();

        StockWarningSetting exist = stockWarningSettingMapper.selectById(ownerShopId);
        if (exist == null) {
            StockWarningSetting setting = new StockWarningSetting();
            setting.setShopId(ownerShopId);
            setting.setNotifyEnabled(notifyEnabled);
            setting.setCreateTime(now);
            setting.setUpdateTime(now);
            stockWarningSettingMapper.insert(setting);
            return;
        }
        exist.setNotifyEnabled(notifyEnabled);
        exist.setUpdateTime(now);
        stockWarningSettingMapper.updateById(exist);
    }

    @Override
    public Page<StockWarningPageVO> list(StockWarningListDTO listDTO) {
        int ownerShopId = normalizeShopId(listDTO.getShopId());
        listDTO.setShopId(ownerShopId);
        listDTO.setUseStoreStock(useStoreStock(ownerShopId));
        listDTO.setUseTotalStock(useTotalStock());

        Page<StockWarningRowVO> rowPage = baseMapper.selectWarningPage(
                new Page<>(listDTO.getPage(), listDTO.getSize()), listDTO);

        Page<StockWarningPageVO> result = new Page<>(listDTO.getPage(), listDTO.getSize(), rowPage.getTotal());
        if (CollUtil.isEmpty(rowPage.getRecords())) {
            result.setRecords(new ArrayList<>());
            return result;
        }

        Owner owner = resolveOwner(ownerShopId);
        Set<Integer> visibleRelatedProductIds = queryVisibleRelatedProductIds(rowPage.getRecords(), ownerShopId);
        List<StockWarningPageVO> records = new ArrayList<>(rowPage.getRecords().size());
        for (StockWarningRowVO row : rowPage.getRecords()) {
            records.add(toPageVO(row, ownerShopId, owner, visibleRelatedProductIds));
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public List<StockWarningExcelVO> export(StockWarningExportDTO exportDTO) {
        int ownerShopId = normalizeShopId(exportDTO.getShopId());

        // 只按勾选行取数：不沿用页面的筛选条件，导出结果与勾选项严格一一对应
        StockWarningListDTO query = new StockWarningListDTO();
        query.setShopId(ownerShopId);
        query.setUseStoreStock(useStoreStock(ownerShopId));
        query.setUseTotalStock(useTotalStock());
        query.setTargets(parseTargets(exportDTO.getIds()));

        List<StockWarningRowVO> rows = baseMapper.selectWarningList(query);
        if (CollUtil.isEmpty(rows)) {
            return new ArrayList<>();
        }

        Owner owner = resolveOwner(ownerShopId);
        Set<Integer> visibleRelatedProductIds = queryVisibleRelatedProductIds(rows, ownerShopId);
        List<StockWarningExcelVO> records = new ArrayList<>(rows.size());
        for (StockWarningRowVO row : rows) {
            // 经列表 VO 中转，保证导出的翻译、状态名与页面完全一致
            records.add(toExcelVO(toPageVO(row, ownerShopId, owner, visibleRelatedProductIds)));
        }
        return records;
    }

    /**
     * 相关商品只在当前库存预警主体可见时返回，批量查询避免按列表行逐条查库。
     */
    private Set<Integer> queryVisibleRelatedProductIds(List<StockWarningRowVO> rows, int ownerShopId) {
        Set<Integer> relatedProductIds = new LinkedHashSet<>();
        for (StockWarningRowVO row : rows) {
            if (row != null) {
                relatedProductIds.addAll(StringUtils.str2IntList(row.getProductRelated()));
            }
        }
        if (relatedProductIds.isEmpty()) {
            return Set.of();
        }
        List<Integer> visibleProductIds = baseMapper.selectVisibleProductIds(ownerShopId, relatedProductIds);
        return CollUtil.isEmpty(visibleProductIds) ? Set.of() : new LinkedHashSet<>(visibleProductIds);
    }

    /**
     * 解析勾选行标识，每项格式 productId_skuId，单规格商品 skuId 为 0。
     */
    private List<StockWarningListDTO.Target> parseTargets(String ids) {
        if (StrUtil.isBlank(ids)) {
            throw new GlobalException("请先选择要导出的数据");
        }
        List<StockWarningListDTO.Target> targets = new ArrayList<>();
        for (String item : ids.split(",")) {
            String trimmed = item.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("_");
            if (parts.length != 2) {
                throw new GlobalException("导出参数格式错误，应为 productId_skuId：" + trimmed);
            }
            try {
                targets.add(new StockWarningListDTO.Target(
                        Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())));
            } catch (NumberFormatException e) {
                throw new GlobalException("导出参数格式错误，应为 productId_skuId：" + trimmed);
            }
        }
        if (targets.isEmpty()) {
            throw new GlobalException("请先选择要导出的数据");
        }
        return targets;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String fileName = URLEncoder.encode(TEMPLATE_FILE_NAME + XLSX, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        // 模板与导入解析共用同一个 VO，表头不会两处漂移
        new ExcelUtils<StockWarningImportExcelVO>(List.of(), StockWarningImportExcelVO.class)
                .exportExcelWebByTitle(response, TEMPLATE_COLUMNS, fileName, "模板");
    }

    @Override
    public StockWarningImportResultVO importExcel(MultipartFile file, Integer shopId) {
        List<StockWarningImportExcelVO> rows = readImportRows(file);
        return importRows(rows, normalizeShopId(shopId));
    }

    @Override
    public void getImportErrorData(MultipartFile file, Integer shopId, HttpServletResponse response) {
        List<StockWarningImportExcelVO> rows = readImportRows(file);
        int ownerShopId = normalizeShopId(shopId);
        Map<String, List<StockWarningSnTargetVO>> snIndex = indexTargetsBySn(ownerShopId, rows);
        List<StockWarningImportErrorExcelVO> errors = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            StockWarningImportExcelVO row = rows.get(i);
            if (isBlankRow(row)) {
                continue;
            }
            try {
                validateImportRow(row, snIndex);
            } catch (Exception e) {
                StockWarningImportErrorExcelVO error = new StockWarningImportErrorExcelVO();
                error.setProductSn(row.getProductSn());
                error.setSkuSn(row.getSkuSn());
                error.setUpperLimit(row.getUpperLimit());
                error.setLowerLimit(row.getLowerLimit());
                error.setErrorMsg(e instanceof GlobalException ? e.getMessage() : "导入失败");
                errors.add(error);
            }
        }
        new ExcelUtils<>(errors, StockWarningImportErrorExcelVO.class).exportExcelWeb(response);
    }

    private List<StockWarningImportExcelVO> readImportRows(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException("请上传文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(XLSX)) {
            throw new GlobalException("请上传.xlsx格式文件");
        }
        List<StockWarningImportExcelVO> rows =
                new ExcelUtils<StockWarningImportExcelVO>(file, StockWarningImportExcelVO.class).importExcel();
        if (CollUtil.isEmpty(rows)) {
            throw new GlobalException("表格数据为空");
        }
        return rows;
    }

    /**
     * 处理已解析出的导入行。与文件读取分离，便于直接对逐行校验与编码解析做测试。
     */
    StockWarningImportResultVO importRows(List<StockWarningImportExcelVO> rows, int ownerShopId) {
        if (CollUtil.isEmpty(rows)) {
            throw new GlobalException("表格数据为空");
        }
        Map<String, List<StockWarningSnTargetVO>> snIndex = indexTargetsBySn(ownerShopId, rows);

        StockWarningImportResultVO result = new StockWarningImportResultVO();
        for (int i = 0; i < rows.size(); i++) {
            StockWarningImportExcelVO row = rows.get(i);
            if (isBlankRow(row)) {
                // 模板尾部的空行不计入结果
                continue;
            }
            int rowNumber = i + FIRST_DATA_ROW;
            String sn = StrUtil.trim(row.getSkuSn());
            try {
                ValidatedImportRow validated = validateImportRow(row, snIndex);
                // 导入意味着要用这些阈值，规则一律置为开启
                saveRule(ownerShopId, validated.target().getProductId(), validated.target().getSkuId(), ENABLED,
                        validated.lowerLimit(), validated.upperLimit());
                result.addSuccess();
            } catch (Exception e) {
                log.warn("库存预警导入失败, shopId={}, row={}, sn={}", ownerShopId, rowNumber, sn, e);
                result.addFail(rowNumber, StrUtil.trim(row.getProductSn()), sn,
                        e instanceof GlobalException ? e.getMessage() : "导入失败");
            }
        }
        if (result.getTotalCount() == 0) {
            throw new GlobalException("表格数据为空");
        }
        return result;
    }

    private ValidatedImportRow validateImportRow(StockWarningImportExcelVO row,
                                                 Map<String, List<StockWarningSnTargetVO>> snIndex) {
        String productSn = StrUtil.trim(row.getProductSn());
        String skuSn = StrUtil.trim(row.getSkuSn());
        if (StrUtil.isBlank(productSn) && StrUtil.isBlank(skuSn)) {
            throw new GlobalException("规格编码不能为空");
        }
        Integer lowerLimit = parseLimit(row.getLowerLimit(), "下限");
        Integer upperLimit = parseLimit(row.getUpperLimit(), "上限");
        checkLimits(lowerLimit, upperLimit);
        StockWarningSnTargetVO target = resolveTargetBySn(snIndex, productSn, skuSn);
        return new ValidatedImportRow(lowerLimit, upperLimit, target);
    }

    private record ValidatedImportRow(Integer lowerLimit, Integer upperLimit, StockWarningSnTargetVO target) {
    }

    /**
     * 一次查完全部编码后按编码分组，避免逐行查库。
     */
    private Map<String, List<StockWarningSnTargetVO>> indexTargetsBySn(int ownerShopId,
                                                                      List<StockWarningImportExcelVO> rows) {
        Set<String> snList = new LinkedHashSet<>();
        for (StockWarningImportExcelVO row : rows) {
            String productSn = row == null ? null : StrUtil.trim(row.getProductSn());
            if (StrUtil.isNotBlank(productSn)) {
                snList.add(productSn);
            }
            String sn = row == null ? null : StrUtil.trim(row.getSkuSn());
            if (StrUtil.isNotBlank(sn)) {
                snList.add(sn);
            }
        }
        Map<String, List<StockWarningSnTargetVO>> index = new HashMap<>();
        if (snList.isEmpty()) {
            return index;
        }
        // 行数不可控，分批查询避免 IN 列表过长
        for (List<String> batch : CollUtil.split(snList, SN_QUERY_BATCH_SIZE)) {
            for (StockWarningSnTargetVO target : baseMapper.selectTargetsBySn(ownerShopId, batch)) {
                index.computeIfAbsent(target.getSn(), key -> new ArrayList<>()).add(target);
            }
        }
        return index;
    }

    /**
     * 商品编码与 SKU 编码分列匹配，SKU 必须属于该商品；无法唯一定位时让该行失败。
     */
    private StockWarningSnTargetVO resolveTargetBySn(Map<String, List<StockWarningSnTargetVO>> snIndex,
                                                     String productSn, String skuSn) {
        List<StockWarningSnTargetVO> products = snIndex.getOrDefault(productSn, List.of()).stream()
                .filter(item -> Objects.equals(item.getMatchType(), MATCH_TYPE_PRODUCT_SN))
                .toList();
        if (StrUtil.isBlank(productSn) && StrUtil.isNotBlank(skuSn)) {
            List<StockWarningSnTargetVO> skuMatched = snIndex.getOrDefault(skuSn, List.of()).stream()
                    .filter(item -> Objects.equals(item.getMatchType(), MATCH_TYPE_SKU_SN))
                    .toList();
            if (skuMatched.isEmpty()) {
                throw new GlobalException("编码不存在：" + skuSn);
            }
            if (skuMatched.size() > 1) {
                throw new GlobalException("规格编码重复，无法唯一定位规格：" + skuSn);
            }
            return skuMatched.get(0);
        }
        if (products.isEmpty()) {
            throw new GlobalException("编码不存在：" + productSn);
        }
        if (StrUtil.isNotBlank(skuSn)) {
            Set<Integer> productIds = new LinkedHashSet<>();
            products.forEach(item -> productIds.add(item.getProductId()));
            List<StockWarningSnTargetVO> skuMatched = snIndex.getOrDefault(skuSn, List.of()).stream()
                    .filter(item -> Objects.equals(item.getMatchType(), MATCH_TYPE_SKU_SN))
                    .filter(item -> productIds.contains(item.getProductId()))
                    .toList();
            if (skuMatched.isEmpty()) {
                throw new GlobalException("编码不存在：" + skuSn);
            }
            if (skuMatched.size() > 1) {
                throw new GlobalException("规格编码重复，无法唯一定位规格：" + skuSn);
            }
            return skuMatched.get(0);
        }
        if (products.size() > 1) {
            throw new GlobalException("商品编码重复，无法唯一定位商品：" + productSn);
        }
        StockWarningSnTargetVO matched = products.get(0);
        if (matched.getSkuCount() != null && matched.getSkuCount() > 0) {
            throw new GlobalException("该编码是多规格商品的商品编码，请填写具体规格的规格编码：" + productSn);
        }
        return matched;
    }

    /**
     * Excel 数值单元格常被读成 100.0，先按小数解析再要求为整数。
     */
    private Integer parseLimit(String value, String label) {
        String trimmed = StrUtil.trim(value);
        if (StrUtil.isBlank(trimmed)) {
            throw new GlobalException("库存" + label + "不能为空");
        }
        try {
            BigDecimal decimal = new BigDecimal(trimmed);
            if (decimal.stripTrailingZeros().scale() > 0) {
                throw new GlobalException("库存" + label + "必须为整数：" + trimmed);
            }
            return decimal.intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            throw new GlobalException("库存" + label + "必须为整数：" + trimmed);
        }
    }

    private boolean isBlankRow(StockWarningImportExcelVO row) {
        return row == null || (StrUtil.isBlank(row.getProductSn()) && StrUtil.isBlank(row.getSkuSn())
                && StrUtil.isBlank(row.getUpperLimit())
                && StrUtil.isBlank(row.getLowerLimit()));
    }

    @Override
    public void save(StockWarningSaveDTO saveDTO) {
        int ownerShopId = normalizeShopId(saveDTO.getShopId());
        int skuId = saveDTO.getSkuId() == null ? 0 : saveDTO.getSkuId();
        checkLimits(saveDTO.getLowerLimit(), saveDTO.getUpperLimit());
        saveRule(ownerShopId, saveDTO.getProductId(), skuId, saveDTO.getWarnEnabled(),
                saveDTO.getLowerLimit(), saveDTO.getUpperLimit());
    }

    @Override
    public StockWarningBatchResultVO batchSave(StockWarningBatchSaveDTO batchSaveDTO) {
        int ownerShopId = normalizeShopId(batchSaveDTO.getShopId());
        StockWarningBatchResultVO result = new StockWarningBatchResultVO();
        // 上下限对整批统一生效，非法即整批拒绝，避免逐条产生同一个错误
        checkLimits(batchSaveDTO.getLowerLimit(), batchSaveDTO.getUpperLimit());

        for (StockWarningBatchSaveDTO.Target target : batchSaveDTO.getTargets()) {
            if (target == null || target.getProductId() == null) {
                result.addFail(null, null, "商品ID不能为空");
                continue;
            }
            int skuId = target.getSkuId() == null ? 0 : target.getSkuId();
            try {
                saveRule(ownerShopId, target.getProductId(), skuId, batchSaveDTO.getWarnEnabled(),
                        batchSaveDTO.getLowerLimit(), batchSaveDTO.getUpperLimit());
                result.addSuccess();
            } catch (Exception e) {
                log.warn("库存预警批量设置失败, shopId={}, productId={}, skuId={}",
                        ownerShopId, target.getProductId(), skuId, e);
                String reason = e instanceof GlobalException ? e.getMessage() : "保存失败";
                result.addFail(target.getProductId(), skuId, reason);
            }
        }
        return result;
    }

    @Override
    public void evaluate(Integer shopId, Integer productId, Integer skuId) {
        if (productId == null) {
            return;
        }
        int ownerShopId = normalizeShopId(shopId);
        int targetSkuId = skuId == null ? 0 : skuId;
        try {
            StockWarningEvaluateRowVO row = baseMapper.selectEvaluateRow(ownerShopId, productId, targetSkuId,
                    useStoreStock(ownerShopId), useTotalStock());
            if (row == null || row.getCurrentStock() == null) {
                return;
            }
            StockWarningRule rule = getRule(ownerShopId, productId, targetSkuId);
            int stock = row.getCurrentStock();
            if (stock <= 0) {
                // 无货只保留一条既有的「商品无货」消息（由下单链路发送），此处仅记录状态以便回升后可重新预警
                updateWarnStatus(rule, StockWarningStatusEnum.OUT_OF_STOCK.getCode());
                return;
            }
            if (!isNotifyEnabled(ownerShopId)) {
                return;
            }

            Integer lowerLimit;
            Integer upperLimit;
            if (rule == null) {
                // 未配置规则：沿用默认下限兼容存量数据，无上限故不判高库存
                lowerLimit = DEFAULT_LOWER_LIMIT;
                upperLimit = null;
            } else if (Objects.equals(rule.getWarnEnabled(), ENABLED)) {
                lowerLimit = rule.getLowerLimit();
                upperLimit = rule.getUpperLimit();
            } else {
                return;
            }

            int status = resolveStatus(stock, lowerLimit, upperLimit);
            if (rule != null && Objects.equals(rule.getLastWarnStatus(), status)) {
                return;
            }
            if (StockWarningStatusEnum.LOW.getCode().equals(status)) {
                sendWarningMessage(ownerShopId, row, targetSkuId, stock, lowerLimit, StockWarningStatusEnum.LOW);
            } else if (StockWarningStatusEnum.HIGH.getCode().equals(status)) {
                sendWarningMessage(ownerShopId, row, targetSkuId, stock, upperLimit, StockWarningStatusEnum.HIGH);
            }
            updateWarnStatus(rule, status);
        } catch (Exception e) {
            // 预警是库存变动的附属动作，失败不影响主业务
            log.error("库存预警判定失败, shopId={}, productId={}, skuId={}", ownerShopId, productId, targetSkuId, e);
        }
    }

    /**
     * 库存状态判定：低于下限为低库存，高于上限为高库存，其余为正常。
     */
    private int resolveStatus(int stock, Integer lowerLimit, Integer upperLimit) {
        if (lowerLimit != null && stock < lowerLimit) {
            return StockWarningStatusEnum.LOW.getCode();
        }
        if (upperLimit != null && stock > upperLimit) {
            return StockWarningStatusEnum.HIGH.getCode();
        }
        return StockWarningStatusEnum.NORMAL.getCode();
    }

    private void saveRule(int ownerShopId, Integer productId, int skuId, Integer warnEnabled,
                          Integer lowerLimit, Integer upperLimit) {
        int enabled = Objects.equals(warnEnabled, ENABLED) ? ENABLED : DISABLED;
        long now = StringUtils.getCurrentTime();
        StockWarningRule rule = getRule(ownerShopId, productId, skuId);
        if (rule == null) {
            rule = new StockWarningRule();
            rule.setShopId(ownerShopId);
            rule.setProductId(productId);
            rule.setSkuId(skuId);
            rule.setWarnEnabled(enabled);
            rule.setLowerLimit(lowerLimit);
            rule.setUpperLimit(upperLimit);
            rule.setLastWarnStatus(StockWarningStatusEnum.NORMAL.getCode());
            rule.setLastWarnTime(0L);
            rule.setIsDelete(DISABLED);
            rule.setCreateTime(now);
            rule.setUpdateTime(now);
            baseMapper.insert(rule);
            return;
        }
        rule.setWarnEnabled(enabled);
        rule.setLowerLimit(lowerLimit);
        rule.setUpperLimit(upperLimit);
        // 阈值可能已改变，重置已发送状态，使新规则对后续库存变动立即生效
        rule.setLastWarnStatus(StockWarningStatusEnum.NORMAL.getCode());
        rule.setUpdateTime(now);
        baseMapper.updateById(rule);
    }

    private void checkLimits(Integer lowerLimit, Integer upperLimit) {
        if (lowerLimit == null || upperLimit == null) {
            throw new GlobalException("库存上下限不能为空");
        }
        if (lowerLimit < 0 || upperLimit < 0) {
            throw new GlobalException("库存上下限不能小于0");
        }
        if (upperLimit <= lowerLimit) {
            throw new GlobalException("库存上限必须大于库存下限");
        }
    }

    private StockWarningRule getRule(int ownerShopId, Integer productId, int skuId) {
        return baseMapper.selectOne(Wrappers.lambdaQuery(StockWarningRule.class)
                .eq(StockWarningRule::getShopId, ownerShopId)
                .eq(StockWarningRule::getProductId, productId)
                .eq(StockWarningRule::getSkuId, skuId)
                .last("limit 1"));
    }

    private void updateWarnStatus(StockWarningRule rule, int status) {
        if (rule == null) {
            return;
        }
        rule.setLastWarnStatus(status);
        rule.setLastWarnTime(StringUtils.getCurrentTime());
        baseMapper.updateById(rule);
    }

    private boolean isNotifyEnabled(int ownerShopId) {
        StockWarningSetting setting = stockWarningSettingMapper.selectById(ownerShopId);
        return setting == null || Objects.equals(setting.getNotifyEnabled(), ENABLED);
    }

    /**
     * 门店主体在启用门店独立库存时按门店库存判断，与库存管理列表口径一致。
     */
    private boolean useStoreStock(int ownerShopId) {
        if (ownerShopId <= 0) {
            return false;
        }
        if (!Objects.equals(tigshopProperties.getIsO2o(), 1)) {
            return false;
        }
        return ENABLED == parseSwitch(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK);
    }

    private boolean useTotalStock() {
        return ENABLED == parseSwitch(SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK);
    }

    private int parseSwitch(SettingsEnum setting) {
        String value = configService.getConfigVal(setting);
        return "1".equals(value) ? ENABLED : DISABLED;
    }

    private void sendWarningMessage(int ownerShopId, StockWarningEvaluateRowVO row, int skuId, int stock,
                                    Integer threshold, StockWarningStatusEnum status) {
        String productName = StrUtil.isBlank(row.getProductName()) ? "" :
                translatePackage.translate(row.getProductName(), DataType.PRODUCT.getCode());
        String skuName = SkuValueTranslateUtils.translateSkuValue(row.getSkuValue(), translatePackage);

        StringBuilder content = new StringBuilder();
        content.append("您的商品【").append(productName).append("】");
        if (StrUtil.isNotBlank(skuName)) {
            content.append("（规格：").append(skuName).append("）");
        }
        if (StockWarningStatusEnum.LOW.equals(status)) {
            content.append("库存不足,还剩").append(stock)
                    .append(",库存下限为").append(threshold).append(",请及时补充库存!");
        } else {
            content.append("库存积压,当前").append(stock)
                    .append(",库存上限为").append(threshold).append(",请及时处理!");
        }

        AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
        createDTO.setMsgType(AdminMsgTypeEnum.STOCK_WARNING.getCatId());
        createDTO.setTitle("商品库存预警:" + productName);
        createDTO.setContent(content.toString());
        createDTO.setShopId(ownerShopId);
        createDTO.setProductId(row.getProductId());
        createDTO.setVendorId(row.getVendorId());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("productId", row.getProductId());
        relatedData.put("skuId", skuId);
        relatedData.put("warnType", status.getCode());
        relatedData.put("currentStock", stock);
        relatedData.put("threshold", threshold);
        createDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(createDTO);
    }

    private StockWarningPageVO toPageVO(StockWarningRowVO row, int ownerShopId, Owner owner,
                                        Set<Integer> visibleRelatedProductIds) {
        StockWarningPageVO vo = new StockWarningPageVO();
        vo.setProductId(row.getProductId());
        vo.setSkuId(row.getSkuId());
        vo.setProductName(StrUtil.isBlank(row.getProductName()) ? "" :
                translatePackage.translate(row.getProductName(), DataType.PRODUCT.getCode()));
        vo.setProductSkuName(SkuValueTranslateUtils.translateSkuValue(row.getProductSkuName(), translatePackage));
        vo.setProductPicThumb(StrUtil.isBlank(row.getProductPicThumb()) ? "" :
                translatePackage.translate(row.getProductPicThumb(), DataType.IMAGE_TITLE.getCode()));
        vo.setProductSn(row.getProductSn());
        vo.setSkuSn(row.getSkuSn());
        vo.setProductTsn(row.getProductTsn());
        vo.setShopId(ownerShopId);
        vo.setShopName(owner.name());
        vo.setOwnerType(owner.type().getCode());
        vo.setOwnerTypeName(owner.type().getName());
        vo.setCurrentStock(row.getCurrentStock());
        vo.setTransitStock(row.getTransitStock() == null ? 0 : row.getTransitStock());
        vo.setLowerLimit(row.getLowerLimit());
        vo.setUpperLimit(row.getUpperLimit());
        vo.setStockStatus(row.getStockStatus());
        vo.setStockStatusName(StockWarningStatusEnum.getNameByCode(row.getStockStatus()));
        vo.setWarnEnabled(row.getWarnEnabled());
        vo.setUpdateTime(row.getUpdateTime());
        List<Integer> relatedProductIds = StringUtils.str2IntList(row.getProductRelated()).stream()
                .filter(visibleRelatedProductIds::contains)
                .toList();
        vo.setRelatedProductIds(relatedProductIds);
        vo.setRelatedProductCount(relatedProductIds.size());
        return vo;
    }

    private StockWarningExcelVO toExcelVO(StockWarningPageVO pageVO) {
        StockWarningExcelVO vo = new StockWarningExcelVO();
        vo.setProductName(pageVO.getProductName());
        vo.setProductSkuName(pageVO.getProductSkuName());
        vo.setProductSn(pageVO.getProductSn());
        vo.setSkuSn(pageVO.getSkuSn());
        vo.setShopName(pageVO.getShopName());
        vo.setCurrentStock(pageVO.getCurrentStock());
        vo.setTransitStock(pageVO.getTransitStock());
        vo.setLowerLimit(emptyIfNull(pageVO.getLowerLimit()));
        vo.setUpperLimit(emptyIfNull(pageVO.getUpperLimit()));
        vo.setStockStatusName(pageVO.getStockStatusName());
        vo.setWarnEnabledName(pageVO.getWarnEnabled() == null ? "未配置"
                : (Objects.equals(pageVO.getWarnEnabled(), ENABLED) ? "开启" : "关闭"));
        vo.setRelatedProductCount(pageVO.getRelatedProductCount());
        return vo;
    }

    private String emptyIfNull(Integer value) {
        return value == null ? EMPTY_CELL : String.valueOf(value);
    }

    /**
     * 解析列表「仓库」列展示的主体。
     * 自营不是 shop 表记录，名称取平台自营门店设置；店铺与门店取 shop 表标题，
     * 自提点与门店同属线下主体，一并按门店展示。
     */
    private Owner resolveOwner(int ownerShopId) {
        if (ownerShopId <= 0) {
            GetStoreSelfOperatedVO settings = configService.getStoreSelfOperatedSettings();
            String name = settings == null ? null : settings.getStoreSelfOperatedName();
            return new Owner(StrUtil.isBlank(name) ? StockWarningOwnerTypeEnum.SELF_OPERATED.getName() : name,
                    StockWarningOwnerTypeEnum.SELF_OPERATED);
        }
        Shop shop = shopMapper.selectById(ownerShopId);
        if (shop == null) {
            return new Owner("", StockWarningOwnerTypeEnum.SHOP);
        }
        String name = StrUtil.isBlank(shop.getShopTitle()) ? "" : shop.getShopTitle();
        StockWarningOwnerTypeEnum type = Objects.equals(shop.getShopType(), ShopTypeEnum.SHOP.getCode())
                ? StockWarningOwnerTypeEnum.SHOP
                : StockWarningOwnerTypeEnum.STORE;
        return new Owner(name, type);
    }

    private record Owner(String name, StockWarningOwnerTypeEnum type) {
    }

    private int normalizeShopId(Integer shopId) {
        return shopId == null || shopId < 0 ? 0 : shopId;
    }
}
