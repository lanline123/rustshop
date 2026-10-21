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

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.LogisticsCompanyCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyUpdateDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.enums.order.LogisticsProviderType;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.setting.LogisticsCompanyVO;
import com.tigshop.bean.vo.setting.SyncLogisticsCompanyResultVO;
import com.tigshop.common.constant.setting.Kuaidi100CodeMapping;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.logistics.LogisticsProviderRouter;
import com.tigshop.service.setting.LogisticsCompanyService;
import com.tigshop.bean.model.setting.LogisticsCompanyConfig;
import com.tigshop.service.setting.LogisticsCompanyConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 配送方式配置信息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class LogisticsCompanyServiceImpl extends BaseServiceImpl<LogisticsCompanyMapper, LogisticsCompany> implements LogisticsCompanyService {
    @Resource
    private AdminLogService adminLogService;

    @Resource
    private LogisticsProviderRouter logisticsProviderRouter;

    @Lazy
    @Resource
    private LogisticsCompanyConfigService logisticsCompanyConfigService;

    /**
     * 语雀「快递公司编码表（国内）」接口（公开读取）。
     * 返回 JSON，数据在 data.content（lake HTML）里。
     */
    private static final String YUQUE_KDN_LOGISTICS_DOC_URL_CN =
            "https://www.yuque.com/api/docs/mza2ln?book_id=32573955&include_contributors=true&include_like=true&include_hits=true&merge_dynamic_data=false";

    /**
     * 语雀「快递公司编码表（国际）」接口（公开读取）。
     * 返回 JSON，数据在 data.content（lake HTML）里。
     */
    private static final String YUQUE_KDN_LOGISTICS_DOC_URL_INTL =
            "https://www.yuque.com/api/docs/xkc4h1?book_id=32573955&include_contributors=true&include_like=true&include_hits=true&merge_dynamic_data=false";

    private static final Pattern TR_PATTERN = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>");
    private static final Pattern TD_PATTERN = Pattern.compile("(?is)<td\\b[^>]*>(.*?)</td>");

    @Override
    public Page<LogisticsCompanyVO> list(LogisticsCompanyListDTO listDTO) {
        // 分页
        Page<LogisticsCompany> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer shopId = listDTO.getShopId();
        if (shopId != null && IsShowType.fromTypeCode(shopId) != null) {
            queryWrapper.eq(LogisticsCompany::getShopId, shopId);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(LogisticsCompany::getLogisticsName, keyword);
        }

        // 是否启用
        Integer isEnabled = listDTO.getIsEnabled();
        if (isEnabled != null) {
            queryWrapper.eq(LogisticsCompany::getIsEnabled, isEnabled);
        }

        applyProviderFilter(queryWrapper, listDTO.getProviderType());

        // 默认按启用状态降序排序，并使用主键作为二级排序保证分页稳定
        queryWrapper.orderByDesc(LogisticsCompany::getIsEnabled)
                .orderByDesc(LogisticsCompany::getLogisticsId);

        // 执行查询
        Page<LogisticsCompany> logisticsCompanyPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<LogisticsCompany> logisticsCompanyPageRecords = logisticsCompanyPage.getRecords();

        // 查询当前租户已绑定的物流公司ID集合
        Integer currentShopId = getShopId();
        Integer currentVendorId = getVendorId();
        int realShopId = currentShopId != null ? currentShopId : 0;
        int realVendorId = currentVendorId != null ? currentVendorId : 0;
        Set<Integer> boundLogisticsIds = logisticsCompanyConfigService.lambdaQuery()
                .eq(LogisticsCompanyConfig::getShopId, realShopId)
                .eq(LogisticsCompanyConfig::getVendorId, realVendorId)
                .list()
                .stream()
                .map(LogisticsCompanyConfig::getLogisticsId)
                .collect(Collectors.toSet());

        // 转换为VO
        List<LogisticsCompanyVO> logisticsCompanyVOList = logisticsCompanyPageRecords.stream()
                .map(logisticsCompany -> {
                    LogisticsCompanyVO logisticsCompanyVO = new LogisticsCompanyVO();
                    BeanUtils.copyProperties(logisticsCompany, logisticsCompanyVO);
                    logisticsCompanyVO.setIsBound(boundLogisticsIds.contains(logisticsCompany.getLogisticsId()) ? 1 : 0);
                    return logisticsCompanyVO;
                }).toList();
        return PageUtil.convertPage(logisticsCompanyPage, logisticsCompanyVOList);
    }

    @Override
    public LogisticsCompanyVO detail(Integer id) {
        if (id != null) {
            LogisticsCompany logisticsCompany = this.getById(id);
            LogisticsCompanyVO logisticsCompanyVO = new LogisticsCompanyVO();
            BeanUtils.copyProperties(logisticsCompany, logisticsCompanyVO);
            return logisticsCompanyVO;
        }
        return null;
    }

    @Override
    public boolean create(LogisticsCompanyCreateDTO createDTO) {
        if (createDTO != null) {
            int providerType = logisticsProviderRouter.resolveProviderType();
            if (providerType == LogisticsProviderType.KUAIDI100.getCode()
                    && StrUtil.isBlank(createDTO.getKuaidi100Code())) {
                throw new GlobalException("快递100物流公司编码不能为空");
            }
            if (providerType == LogisticsProviderType.KDNIAO.getCode()
                    && StrUtil.isBlank(createDTO.getLogisticsCode())) {
                throw new GlobalException("快递鸟物流公司编码不能为空");
            }
            LogisticsCompany logisticsCompany = new LogisticsCompany();
            BeanUtils.copyProperties(createDTO, logisticsCompany);
            logisticsCompany.setShopId(0);
            adminLogService.createByLogInfo("新增物流公司:"+ createDTO.getLogisticsName());
            return this.save(logisticsCompany);
        }
        return false;
    }

    @Override
    public boolean update(LogisticsCompanyUpdateDTO updateDTO) {
        if (updateDTO != null) {
            int providerType = logisticsProviderRouter.resolveProviderType();
            if (providerType == LogisticsProviderType.KUAIDI100.getCode()
                    && StrUtil.isBlank(updateDTO.getKuaidi100Code())) {
                throw new GlobalException("快递100物流公司编码不能为空");
            }
            if (providerType == LogisticsProviderType.KDNIAO.getCode()
                    && StrUtil.isBlank(updateDTO.getLogisticsCode())) {
                throw new GlobalException("快递鸟物流公司编码不能为空");
            }
            LogisticsCompany logisticsCompany = new LogisticsCompany();
            BeanUtils.copyProperties(updateDTO, logisticsCompany);
            LogisticsCompany oldLogisticsCompany = this.getById(updateDTO.getId());
            logisticsCompany.setLogisticsId(updateDTO.getId());
            adminLogService.createByLogInfo("更新物流公司:"+ oldLogisticsCompany.getLogisticsName());
            return this.updateById(logisticsCompany);
        }
        return false;
    }

    @Override
    public LogisticsCompany getLogisticsCompanyById(Integer id) {
        if (id != null) {
            return this.getById(id);
        }
        return  null;
    }

    @Override
    @Transactional
    public SyncLogisticsCompanyResultVO syncKdnLogisticsCompanies(boolean overwriteName, int defaultIsEnabled) {
        defaultIsEnabled = defaultIsEnabled == 1 ? 1 : 0;

        // 从语雀文档接口取数据（快递公司编码表：lake HTML table）
        Map<String, String> remoteCn = fetchYuqueKdnCompanies(YUQUE_KDN_LOGISTICS_DOC_URL_CN);
        Map<String, String> remoteIntl = fetchYuqueKdnCompanies(YUQUE_KDN_LOGISTICS_DOC_URL_INTL);

        // 合并去重：编码忽略大小写与首尾空白，国内优先，避免同一承运商重复新增
        Map<String, String> remoteByCode = new LinkedHashMap<>();
        for (Map<String, String> remote : List.of(remoteCn, remoteIntl)) {
            for (Map.Entry<String, String> entry : remote.entrySet()) {
                String normalizedCode = normalizeLogisticsCode(entry.getKey());
                if (StrUtil.isNotBlank(normalizedCode)) {
                    remoteByCode.putIfAbsent(normalizedCode, entry.getValue());
                }
            }
        }

        if (remoteByCode.isEmpty()) {
            SyncLogisticsCompanyResultVO empty = new SyncLogisticsCompanyResultVO();
            empty.setRemoteTotal(0);
            empty.setInserted(0);
            empty.setUpdated(0);
            empty.setConflicted(0);
            return empty;
        }

        Integer shopId = getShopId();
        List<LogisticsCompany> existing = this.list(new LambdaQueryWrapper<LogisticsCompany>()
                .eq(LogisticsCompany::getShopId, shopId));
        Map<String, LogisticsCompany> existingByCode = existing.stream()
                .filter(x -> StrUtil.isNotBlank(x.getLogisticsCode()))
                .collect(Collectors.toMap(
                        x -> normalizeLogisticsCode(x.getLogisticsCode()),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new));

        int inserted = 0;
        int updated = 0;
        List<LogisticsCompany> updateBatch = new ArrayList<>();
        List<LogisticsCompany> saveBatch = new ArrayList<>();

        for (Map.Entry<String, String> e : remoteByCode.entrySet()) {
            String code = e.getKey();
            String name = e.getValue();
            if (StrUtil.isBlank(code)) {
                continue;
            }

            LogisticsCompany old = existingByCode.get(code);
            if (old == null) {
                LogisticsCompany toSave = new LogisticsCompany();
                toSave.setLogisticsCode(code);
                toSave.setLogisticsName(StrUtil.blankToDefault(name, code));
                toSave.setLogisticsDesc("");
                toSave.setSortOrder(50);
                toSave.setIsShow(1);
                toSave.setShopId(shopId);
                toSave.setApiLogisticsCode("");
                toSave.setIsEnabled(defaultIsEnabled);
                saveBatch.add(toSave);
                inserted++;
            } else if (overwriteName && StrUtil.isNotBlank(name) && !StrUtil.equals(old.getLogisticsName(), name)) {
                LogisticsCompany toUpdate = new LogisticsCompany();
                toUpdate.setLogisticsId(old.getLogisticsId());
                toUpdate.setLogisticsName(name);
                updateBatch.add(toUpdate);
                updated++;
            }
        }

        if (!saveBatch.isEmpty()) {
            this.saveBatch(saveBatch);
        }
        if (!updateBatch.isEmpty()) {
            this.updateBatchById(updateBatch);
        }

        adminLogService.createByLogInfo("同步快递鸟物流公司（语雀编码表：国内/国际），新增:" + inserted + "，更新:" + updated);

        SyncLogisticsCompanyResultVO result = new SyncLogisticsCompanyResultVO();
        result.setRemoteTotal(remoteByCode.size());
        result.setInserted(inserted);
        result.setUpdated(updated);
        result.setConflicted(0);
        return result;
    }

    @Override
    @Transactional
    public SyncLogisticsCompanyResultVO syncKuaidi100LogisticsCompanies(boolean overwriteName, int defaultIsEnabled) {
        defaultIsEnabled = defaultIsEnabled == 1 ? 1 : 0;

        // 种子来源：内置常见承运商（快递100 编码 → 中文名），离线可用、无需外部鉴权
        Map<String, String> seed = com.tigshop.common.constant.setting.Kuaidi100CodeMapping.commonCarriers();
        if (seed.isEmpty()) {
            SyncLogisticsCompanyResultVO empty = new SyncLogisticsCompanyResultVO();
            empty.setRemoteTotal(0);
            empty.setInserted(0);
            empty.setUpdated(0);
            empty.setConflicted(0);
            return empty;
        }

        Integer shopId = getShopId();
        List<LogisticsCompany> existing = this.list(new LambdaQueryWrapper<LogisticsCompany>()
                .eq(LogisticsCompany::getShopId, shopId));
        // 显式编码保留全部行，重复编码必须报告冲突，不能静默选择其中一行
        Map<String, List<LogisticsCompany>> byKuaidi100 = existing.stream()
                .filter(x -> StrUtil.isNotBlank(x.getKuaidi100Code()))
                .collect(Collectors.groupingBy(
                        x -> normalizeKuaidi100Code(x.getKuaidi100Code()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        int inserted = 0;
        int updated = 0;
        int conflicted = 0;
        List<LogisticsCompany> saveBatch = new ArrayList<>();
        List<LogisticsCompany> updateBatch = new ArrayList<>();

        for (Map.Entry<String, String> e : seed.entrySet()) {
            String code = normalizeKuaidi100Code(e.getKey());
            String name = e.getValue();
            List<LogisticsCompany> directMatches = byKuaidi100.getOrDefault(code, List.of());
            if (directMatches.size() > 1) {
                conflicted++;
                continue;
            }

            if (directMatches.size() == 1) {
                LogisticsCompany old = directMatches.get(0);
                if (overwriteName && StrUtil.isNotBlank(name) && !StrUtil.equals(old.getLogisticsName(), name)) {
                    LogisticsCompany toUpdate = new LogisticsCompany();
                    toUpdate.setLogisticsId(old.getLogisticsId());
                    toUpdate.setLogisticsName(name);
                    updateBatch.add(toUpdate);
                    updated++;
                }
                continue;
            }

            Set<String> reverseCodes = Kuaidi100CodeMapping.reverseResolve(code);
            List<LogisticsCompany> reverseMatches = existing.stream()
                    .filter(company -> StrUtil.isNotBlank(company.getLogisticsCode()))
                    .filter(company -> reverseCodes.contains(
                            company.getLogisticsCode().trim().toUpperCase(Locale.ROOT)))
                    .toList();
            if (reverseMatches.size() > 1) {
                conflicted++;
                continue;
            }

            if (reverseMatches.size() == 1) {
                LogisticsCompany old = reverseMatches.get(0);
                if (StrUtil.isNotBlank(old.getKuaidi100Code())
                        && !StrUtil.equals(normalizeKuaidi100Code(old.getKuaidi100Code()), code)) {
                    conflicted++;
                    continue;
                }
                LogisticsCompany toUpdate = new LogisticsCompany();
                toUpdate.setLogisticsId(old.getLogisticsId());
                toUpdate.setKuaidi100Code(code);
                if (overwriteName && StrUtil.isNotBlank(name) && !StrUtil.equals(old.getLogisticsName(), name)) {
                    toUpdate.setLogisticsName(name);
                }
                updateBatch.add(toUpdate);
                updated++;
            } else {
                LogisticsCompany toSave = new LogisticsCompany();
                toSave.setLogisticsName(StrUtil.blankToDefault(name, code));
                // 快递100 行仅用 kuaidi100Code 标识；logisticsCode 留空，避免污染快递鸟 ShipperCode 空间与去重
                toSave.setLogisticsCode("");
                toSave.setKuaidi100Code(code);
                toSave.setLogisticsDesc("");
                toSave.setSortOrder(50);
                toSave.setIsShow(1);
                toSave.setShopId(shopId);
                toSave.setApiLogisticsCode("");
                toSave.setIsEnabled(defaultIsEnabled);
                saveBatch.add(toSave);
                inserted++;
            }
        }

        if (!saveBatch.isEmpty()) {
            this.saveBatch(saveBatch);
        }
        if (!updateBatch.isEmpty()) {
            this.updateBatchById(updateBatch);
        }

        adminLogService.createByLogInfo("同步快递100物流公司（内置常见承运商），新增:" + inserted
                + "，更新:" + updated + "，冲突:" + conflicted);

        SyncLogisticsCompanyResultVO result = new SyncLogisticsCompanyResultVO();
        result.setRemoteTotal(seed.size());
        result.setInserted(inserted);
        result.setUpdated(updated);
        result.setConflicted(conflicted);
        return result;
    }

    static void applyProviderFilter(LambdaQueryWrapper<LogisticsCompany> queryWrapper, Integer providerType) {
        if (Integer.valueOf(1).equals(providerType)) {
            queryWrapper.isNotNull(LogisticsCompany::getLogisticsCode)
                    .apply("TRIM(logistics_code) <> ''");
        } else if (Integer.valueOf(2).equals(providerType)) {
            queryWrapper.and(wrapper -> {
                wrapper.isNotNull(LogisticsCompany::getKuaidi100Code)
                        .apply("TRIM(kuaidi100_code) <> ''")
                        .or();
                applyMappedLogisticsCodeFilter(wrapper);
                wrapper.or().apply("TRIM(logistics_code) COLLATE utf8mb4_bin REGEXP '^[a-z0-9]+$'");
            });
        }
    }

    private static void applyMappedLogisticsCodeFilter(LambdaQueryWrapper<LogisticsCompany> queryWrapper) {
        Object[] mappedCodes = Kuaidi100CodeMapping.mappedLogisticsCodes().toArray();
        String placeholders = IntStream.range(0, mappedCodes.length)
                .mapToObj(index -> "{" + index + "}")
                .collect(Collectors.joining(","));
        queryWrapper.apply("UPPER(TRIM(logistics_code)) IN (" + placeholders + ")", mappedCodes);
    }

    private static String normalizeKuaidi100Code(String code) {
        return code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeLogisticsCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    Map<String, String> fetchYuqueKdnCompanies(String url) {
        String responseBody = doHttpGet(url);
        JSONObject root = JSONUtil.parseObj(responseBody);
        JSONObject data = root.getJSONObject("data");
        if (data == null) {
            throw new GlobalException("语雀接口返回异常：缺少 data 字段");
        }
        String content = data.getStr("content");
        if (StrUtil.isBlank(content)) {
            throw new GlobalException("语雀接口返回异常：缺少 data.content（快递公司编码表内容）");
        }
        return parseKdnCompanyTableFromYuqueContent(content);
    }

    private static String doHttpGet(String url) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Tigshop-Api")
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) {
                throw new GlobalException("语雀接口请求失败，HTTP " + resp.statusCode());
            }
            return resp.body();
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("语雀接口请求失败：" + e.getMessage());
        }
    }

    /**
     * 从语雀 doc 的 data.content（lake HTML）里解析快递公司编码表。
     * 该表格通常为 3 列（序号/公司名称/公司编码），也兼容 2 列（名称/编码 或 编码/名称）。
     *
     * @return key=公司编码(大写去空格)，value=公司名称
     */
    private static Map<String, String> parseKdnCompanyTableFromYuqueContent(String contentHtml) {
        Map<String, String> result = new LinkedHashMap<>();

        Matcher trMatcher = TR_PATTERN.matcher(contentHtml);
        while (trMatcher.find()) {
            String trHtml = trMatcher.group(1);

            List<String> cells = new ArrayList<>();
            Matcher tdMatcher = TD_PATTERN.matcher(trHtml);
            while (tdMatcher.find()) {
                String tdHtml = tdMatcher.group(1);
                String text = stripHtml(tdHtml);
                text = normalizeCellText(text);
                if (StrUtil.isNotBlank(text)) {
                    cells.add(text);
                } else {
                    cells.add("");
                }
            }

            if (cells.size() < 2) {
                continue;
            }

            String joined = String.join(" ", cells);
            // 跳过表头/提示行
            if (joined.contains("快递公司") && (joined.contains("编码") || joined.contains("名称") || joined.contains("序号"))) {
                continue;
            }

            String name;
            String code;
            if (cells.size() >= 3) {
                name = cells.get(1);
                code = cells.get(2);
            } else {
                String a = cells.get(0);
                String b = cells.get(1);
                boolean aLooksCode = looksLikeKdnCode(a);
                boolean bLooksCode = looksLikeKdnCode(b);
                if (aLooksCode && !bLooksCode) {
                    code = a;
                    name = b;
                } else if (!aLooksCode && bLooksCode) {
                    code = b;
                    name = a;
                } else {
                    // 兜底：默认第二列为编码
                    name = a;
                    code = b;
                }
            }

            code = normalizeCode(code);
            name = StrUtil.trim(name);
            if (StrUtil.isBlank(code) || StrUtil.isBlank(name)) {
                continue;
            }

            // 保留首个名称即可（同编码重复行忽略后续）
            result.putIfAbsent(code, name);
        }

        return result;
    }

    private static String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        // 去标签
        String text = html.replaceAll("(?is)<[^>]+>", "");
        // 简单反转义
        text = text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        return text;
    }

    private static String normalizeCellText(String text) {
        if (text == null) {
            return "";
        }
        // 合并空白
        String t = text.replace('\u00A0', ' ');
        t = t.replaceAll("\\s+", " ");
        return StrUtil.trim(t);
    }

    private static String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        String c = code.replaceAll("\\s+", "");
        c = StrUtil.trim(c);
        return StrUtil.upperFirst(c).toUpperCase();
    }

    private static boolean looksLikeKdnCode(String s) {
        if (StrUtil.isBlank(s)) {
            return false;
        }
        String t = s.replaceAll("\\s+", "");
        // 常见快递鸟编码：英文大写/数字/下划线，长度一般不大
        return t.length() <= 32 && t.matches("^[A-Za-z0-9_\\-]+$") && !t.matches("^\\d+$");
    }
}
