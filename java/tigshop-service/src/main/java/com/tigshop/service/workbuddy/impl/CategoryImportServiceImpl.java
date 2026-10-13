package com.tigshop.service.workbuddy.impl;

import com.tigshop.bean.dto.product.CategoryDTO;
import com.tigshop.bean.dto.workbuddy.CategoryImportDTO;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.workbuddy.CategoryImportPreviewVO;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.workbuddy.CategoryImportService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分类树导入服务。
 *
 * <p>客户端可以从自然语言或文件抽取统一的树结构；服务端只负责校验、预览和按父级到
 * 子级的顺序写入。预览和幂等记录在服务实例内短期保存，生产环境应由网关/Redis 提供
 * 跨实例一致的 requestId 和 previewId 存储。</p>
 */
@Service
public class CategoryImportServiceImpl implements CategoryImportService {
    private static final int CATEGORY_PAGE_SIZE = 1000;
    private static final long PREVIEW_TTL_MILLIS = 30 * 60 * 1000L;
    private static final long RESULT_TTL_MILLIS = 24 * 60 * 60 * 1000L;

    private final CategoryService categoryService;
    private final Map<String, PreviewRecord> previews = new ConcurrentHashMap<>();
    private final Map<String, ResultRecord> results = new ConcurrentHashMap<>();

    public CategoryImportServiceImpl(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public Map<String, Object> prepare(CategoryImportDTO request, Integer shopId) {
        requirePlatform(shopId);
        evictExpired();
        String requestId = request == null ? null : trim(request.getRequestId());
        if (request == null || !"import".equalsIgnoreCase(trim(request.getOperation()))) {
            return error("INVALID_ARGUMENT", "operation 必须为 import");
        }
        if (request.getNodes() == null || request.getNodes().isEmpty()) {
            return error("INVALID_ARGUMENT", "nodes 不能为空");
        }
        if (requestId == null) {
            return error("REQUEST_ID_REQUIRED", "prepare 必须提供 requestId");
        }

        Normalization normalization = normalize(request.getNodes());
        String previewId = UUID.randomUUID().toString();
        CategoryImportPreviewVO preview = normalization.preview();
        preview.setPreviewId(previewId);
        preview.setStatus(normalization.importable() ? "PREPARED" : "REJECTED");
        preview.setSummary(summary(preview));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("preview", preview);
        result.put("previewId", previewId);
        result.put("requestId", requestId);
        result.put("status", normalization.importable() ? "prepared" : "failed");
        if (!normalization.importable()) {
            result.put("errorCode", "PREVIEW_NOT_IMPORTABLE");
        }
        previews.put(previewId, new PreviewRecord(shopId == null ? 0 : shopId, normalization.fingerprint(),
                normalization.nodes(), normalization.importable(), System.currentTimeMillis() + PREVIEW_TTL_MILLIS));
        return result;
    }

    @Override
    public synchronized Map<String, Object> execute(CategoryImportDTO request, Integer shopId) {
        requirePlatform(shopId);
        evictExpired();
        if (!Boolean.TRUE.equals(request == null ? null : request.getConfirmation())) {
            return error("CONFIRMATION_REQUIRED", "分类写入前必须明确 confirmation: true");
        }
        if (!"import".equalsIgnoreCase(trim(request.getOperation()))) {
            return error("INVALID_ARGUMENT", "operation 必须为 import");
        }
        String previewId = trim(request.getPreviewId());
        String requestId = trim(request.getRequestId());
        if (previewId == null) {
            return error("PREVIEW_REQUIRED", "execute 必须提供 previewId");
        }
        if (requestId == null) {
            return error("REQUEST_ID_REQUIRED", "execute 必须提供 requestId");
        }
        if (request.getNodes() == null || request.getNodes().isEmpty()) {
            return error("INVALID_ARGUMENT", "nodes 不能为空");
        }

        Normalization current = normalize(request.getNodes());
        ResultRecord previous = results.get(requestId);
        if (previous != null) {
            if (previous.fingerprint().equals(current.fingerprint())) {
                return previous.result();
            }
            return error("IDEMPOTENCY_KEY_CONFLICT", "相同 requestId 已用于另一份分类数据");
        }

        PreviewRecord preview = previews.get(previewId);
        if (preview == null || preview.expiresAt() <= System.currentTimeMillis()) {
            return error("PREVIEW_EXPIRED", "分类预览不存在或已过期");
        }
        if (!Objects.equals(preview.shopId(), shopId == null ? 0 : shopId)) {
            return error("PREVIEW_SCOPE_MISMATCH", "分类预览不属于当前管理作用域");
        }
        if (!preview.fingerprint().equals(current.fingerprint())) {
            return error("PREVIEW_MISMATCH", "分类数据已变化，请重新生成预览");
        }
        if (!preview.importable() || !current.importable()) {
            return error("PREVIEW_NOT_IMPORTABLE", "预览包含重复、空名称或无效父级，修正后再执行");
        }

        Map<String, Integer> resolvedIds = new LinkedHashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        int success = 0;
        int failed = 0;
        for (FlatNode node : current.nodes()) {
            Integer parentId = node.parentPath().isEmpty() ? 0 : resolvedIds.get(node.parentPath());
            if (!node.parentPath().isEmpty() && parentId == null) {
                failed++;
                items.add(item(node.path(), false, null, "MISSING_PARENT", "父级分类未创建，不能创建子级", false));
                continue;
            }
            try {
                Category existing = node.explicitId() == null ? find(node.name(), parentId)
                        : categoryService.getById(node.explicitId());
                Integer id;
                if (existing != null) {
                    if (!Objects.equals(trim(existing.getCategoryName()), node.name())
                            || !Objects.equals(normalizeParent(existing.getParentId()), normalizeParent(parentId))) {
                        failed++;
                        items.add(item(node.path(), false, null, "CATEGORY_ID_NOT_FOUND", "提供的分类 ID 不存在或层级不匹配", false));
                        continue;
                    }
                    id = existing.getCategoryId();
                } else if (node.explicitId() != null) {
                    failed++;
                    items.add(item(node.path(), false, null, "CATEGORY_ID_NOT_FOUND", "提供的分类 ID 不存在或层级不匹配", false));
                    continue;
                } else {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setCategoryName(node.name());
                    dto.setParentId(parentId == null ? 0 : parentId);
                    dto.setIsShow(1);
                    dto.setIsHot(0);
                    dto.setSortOrder(0);
                    categoryService.create(dto);
                    id = dto.getCategoryId();
                    if (id == null) {
                        Category persisted = find(node.name(), parentId);
                        id = persisted == null ? null : persisted.getCategoryId();
                    }
                    if (id == null) {
                        throw new IllegalStateException("CATEGORY_ID_NOT_RETURNED");
                    }
                }
                resolvedIds.put(node.path(), id);
                success++;
                items.add(item(node.path(), true, id, null, null, false));
            } catch (RuntimeException ex) {
                failed++;
                items.add(item(node.path(), false, null, "CATEGORY_CREATE_FAILED", safeMessage(ex), true));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", failed == 0 ? "success" : success == 0 ? "failed" : "partial");
        result.put("total", items.size());
        result.put("successCount", success);
        result.put("failureCount", failed);
        result.put("items", items);
        result.put("requestId", requestId);
        result.put("summary", "分类导入完成：成功 " + success + " 个，失败 " + failed + " 个");
        results.put(requestId, new ResultRecord(current.fingerprint(), result, System.currentTimeMillis() + RESULT_TTL_MILLIS));
        return result;
    }

    private Normalization normalize(List<CategoryImportDTO.Node> input) {
        List<FlatNode> flat = new ArrayList<>();
        List<CategoryImportPreviewVO.NodeResult> output = new ArrayList<>();
        Set<String> paths = new LinkedHashSet<>();
        Set<CategoryImportDTO.Node> active = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        Map<String, Integer> knownIds = new HashMap<>();
        flatten(input, "", flat, output, paths, active, knownIds);
        boolean importable = !output.isEmpty() && output.stream().noneMatch(n ->
                "conflict".equals(n.getState()) || "missing_parent".equals(n.getState()));
        CategoryImportPreviewVO preview = new CategoryImportPreviewVO();
        preview.setNodes(output);
        preview.setTotal(output.size());
        preview.setExistingCount((int) output.stream().filter(n -> "existing".equals(n.getState())).count());
        preview.setMissingParentCount((int) output.stream().filter(n -> "missing_parent".equals(n.getState())).count());
        preview.setConflictCount((int) output.stream().filter(n -> "conflict".equals(n.getState())).count());
        return new Normalization(flat, preview, importable, fingerprint(flat));
    }

    private void flatten(List<CategoryImportDTO.Node> nodes, String parentPath, List<FlatNode> flat,
                         List<CategoryImportPreviewVO.NodeResult> output, Set<String> paths,
                         Set<CategoryImportDTO.Node> active, Map<String, Integer> knownIds) {
        if (nodes == null) {
            return;
        }
        for (CategoryImportDTO.Node raw : nodes) {
            if (raw == null) {
                continue;
            }
            String name = trim(raw.getName());
            String path = parentPath.isEmpty() ? name : parentPath + "/" + name;
            CategoryImportPreviewVO.NodeResult result = new CategoryImportPreviewVO.NodeResult();
            result.setPath(path);
            result.setName(name);
            result.setId(raw.getId());
            result.setHasChildren(raw.getChildren() != null && !raw.getChildren().isEmpty());
            result.setLeaf(!Boolean.TRUE.equals(result.getHasChildren()));
            output.add(result);

            if (name == null) {
                result.setState("conflict");
                result.setReason("分类名称不能为空");
                continue;
            }
            if (name.length() > 30) {
                result.setState("conflict");
                result.setReason("分类名称不能超过 30 个字符");
                continue;
            }
            if (active.contains(raw)) {
                result.setState("conflict");
                result.setReason("检测到分类树循环");
                continue;
            }
            if (!paths.add(path)) {
                result.setState("conflict");
                result.setReason("输入中存在重复分类路径");
                continue;
            }
            if (!active.add(raw)) {
                result.setState("conflict");
                result.setReason("检测到分类树循环");
                continue;
            }

            Integer parentId = parentPath.isEmpty() ? 0 : knownIds.get(parentPath);
            Integer lookupParentId = Objects.equals(parentId, -1) ? null : parentId;
            Category existing = Objects.equals(parentId, -1) ? null : find(name, lookupParentId);
            if (raw.getId() != null) {
                Category byId = categoryService.getById(raw.getId());
                if (byId == null || !Objects.equals(byId.getCategoryName(), name)
                        || !Objects.equals(normalizeParent(byId.getParentId()), normalizeParent(lookupParentId))) {
                    result.setState("missing_parent");
                    result.setReason("提供的分类 ID 不存在或不属于当前父级");
                } else {
                    existing = byId;
                    result.setState("existing");
                    knownIds.put(path, byId.getCategoryId());
                }
            } else if (existing != null) {
                result.setState("existing");
                knownIds.put(path, existing.getCategoryId());
            } else {
                // A missing node itself is safe to create after confirmation. A child whose
                // parent was not resolvable is marked missing_parent and blocks execution.
                boolean parentKnown = parentPath.isEmpty() || knownIds.containsKey(parentPath);
                result.setState(parentKnown ? "missing" : "missing_parent");
                if (!parentKnown) {
                    result.setReason("父级分类不存在");
                }
                // -1 is a planning marker: the parent node is supplied in this same tree
                // and will receive its real id during execute.
                knownIds.put(path, -1);
            }
            flat.add(new FlatNode(path, parentPath, name, raw.getId()));
            flatten(raw.getChildren(), path, flat, output, paths, active, knownIds);
            active.remove(raw);
        }
    }

    private Category find(String name, Integer parentId) {
        if (categoryService == null || name == null) {
            return null;
        }
        List<Category> list = categoryService.getCategoryByParentId(normalizeParent(parentId), CATEGORY_PAGE_SIZE);
        if (list == null) {
            return null;
        }
        return list.stream().filter(c -> Objects.equals(trim(c.getCategoryName()), name)).findFirst().orElse(null);
    }

    private static Integer normalizeParent(Integer parentId) {
        return parentId == null ? 0 : parentId;
    }

    private static String fingerprint(List<FlatNode> nodes) {
        StringBuilder canonical = new StringBuilder();
        for (FlatNode node : nodes) {
            canonical.append(node.path()).append('\u0000').append(node.name()).append('\u0000')
                    .append(node.explicitId()).append('\u0001');
        }
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 不可用", ex);
        }
    }

    private static String summary(CategoryImportPreviewVO preview) {
        int pending = preview.getTotal() - preview.getExistingCount() - preview.getConflictCount();
        return "分类预览完成：共 " + preview.getTotal() + " 个节点，已有 " + preview.getExistingCount()
                + " 个，待创建 " + Math.max(0, pending) + " 个，缺失父级 " + preview.getMissingParentCount() + " 个";
    }

    private static Map<String, Object> item(String path, boolean success, Integer id, String code, String message,
                                             boolean retryable) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("path", path);
        value.put("success", success);
        if (id != null) {
            value.put("categoryId", id);
        }
        if (code != null) {
            value.put("errorCode", code);
            value.put("retryable", retryable);
        }
        if (message != null) {
            value.put("error", message);
        }
        return value;
    }

    private static Map<String, Object> error(String code, String message) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("status", "failed");
        value.put("errorCode", code);
        value.put("message", message);
        return value;
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? "分类创建失败" : message.substring(0, Math.min(200, message.length()));
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        previews.entrySet().removeIf(e -> e.getValue().expiresAt() <= now);
        results.entrySet().removeIf(e -> e.getValue().expiresAt() <= now);
    }

    private static void requirePlatform(Integer shopId) {
        if (shopId != null && shopId != 0) {
            throw new WorkBuddyApiException("PLATFORM_SCOPE_REQUIRED", "category requires platform shop scope", 403, false);
        }
    }

    private record FlatNode(String path, String parentPath, String name, Integer explicitId) { }
    private record Normalization(List<FlatNode> nodes, CategoryImportPreviewVO preview, boolean importable,
                                 String fingerprint) { }
    private record PreviewRecord(int shopId, String fingerprint, List<FlatNode> nodes, boolean importable,
                                 long expiresAt) { }
    private record ResultRecord(String fingerprint, Map<String, Object> result, long expiresAt) { }
}
