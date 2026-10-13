package com.tigshop.adminapi.mcp;

import com.tigshop.bean.dto.workbuddy.ProductDraftDTO;
import com.tigshop.bean.vo.workbuddy.ProductCategoryVO;
import com.tigshop.bean.vo.workbuddy.ProductImportResultVO;
import com.tigshop.bean.vo.workbuddy.ProductRequirementsVO;
import com.tigshop.bean.vo.workbuddy.ProductValidationVO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * 把结构化结果转成可直接转述给用户的中文摘要。
 *
 * <p>AI 客户端拿到的结构化载荷仍是原样返回，摘要只是额外附上的一段自然语言，
 * 使调用方无需自行解读字段就能把结果讲清楚。
 */
final class WorkBuddyMcpSummaries {

    /** 后台商品列表路径：前端路由为 history 模式，基址 admin。 */
    private static final String ADMIN_PRODUCT_LIST = "/admin/product/list";

    private WorkBuddyMcpSummaries() {
    }

    /**
     * 把字段规范讲成可直接转述的说明。
     *
     * <p>调用方是 AI，拿到裸 JSON 只能自行揣摩；这里显式区分必填与可选，
     * 并带上约束与默认值，减少来回试错。
     */
    static String requirements(ProductRequirementsVO requirements) {
        List<ProductRequirementsVO.Field> fields = nullSafe(requirements.getFields());
        List<String> lines = new ArrayList<>();
        lines.add("可以直接用自然语言描述要新建的商品，也可以附上图片或视频；我会根据当前信息只询问还缺的必要内容。");
        lines.add("以下是当前可识别的字段和约束，不需要客户复制固定模板。请在补齐阻塞字段后再进行上传、校验和创建确认。");

        List<ProductRequirementsVO.Field> required = fields.stream().filter(ProductRequirementsVO.Field::isRequired).toList();
        if (!required.isEmpty()) {
            lines.add("必填字段：");
            required.forEach(field -> lines.add(describe(field)));
        }

        List<ProductRequirementsVO.Field> optional = fields.stream().filter(field -> !field.isRequired()).toList();
        if (!optional.isEmpty()) {
            lines.add("可选字段：");
            optional.forEach(field -> lines.add(describe(field)));
        }

        List<String> workflow = nullSafe(requirements.getWorkflow());
        if (!workflow.isEmpty()) {
            lines.add("调用顺序：");
            lines.addAll(workflow);
        }

        List<String> unsupported = nullSafe(requirements.getUnsupported());
        if (!unsupported.isEmpty()) {
            lines.add("本期不支持：" + String.join("；", unsupported) + "。");
        }

        lines.add("如果信息不完整，我会一次只询问当前阻塞项；AI 推荐的价格、库存、货号或卖点会明确标注为建议，不能替代你的确认。");
        return String.join("\n", lines);
    }

    private static String describe(ProductRequirementsVO.Field field) {
        StringBuilder line = new StringBuilder("- ").append(field.getName());
        if (!isBlank(field.getDescription())) {
            line.append(' ').append(field.getDescription());
        }
        line.append("（").append(field.getType());
        if (!isBlank(field.getConstraint())) {
            line.append("，").append(field.getConstraint());
        }
        if (!isBlank(field.getDefaultValue())) {
            line.append("，不填则为 ").append(field.getDefaultValue());
        }
        if (!isBlank(field.getExample())) {
            line.append("，如 ").append(field.getExample());
        }
        return line.append("）").toString();
    }

    static String categories(List<ProductCategoryVO> categories, String keyword) {
        if (categories.isEmpty()) {
            String message = isBlank(keyword)
                    ? "没有查到分类，请确认后台已配置商品分类。"
                    : "没有找到名称含「" + keyword + "」的分类。可换个词再查，或不带关键词调用以列出顶级分类逐级下钻。";
            return message + " 当前查询是按名称包含匹配，不是语义搜索；如果始终没有合适的末级分类，我可以先为你整理分类建议。推荐只是建议，必须由你确认后才能调用分类导入创建。";
        }

        List<String> lines = new ArrayList<>();
        lines.add("找到 " + categories.size() + " 个分类：");
        for (ProductCategoryVO category : categories) {
            lines.add("- " + category.getPath() + "（categoryId " + category.getCategoryId() + "）"
                    + (category.isHasChildren() ? "，还有下级" : ""));
        }
        lines.add("商品必须挂在末级分类。请先让用户确认完整分类路径；标记‘还有下级’的候选不能直接用于创建，应继续下钻。");
        lines.add("当前查询是按名称包含匹配，不是语义搜索；如果没有合适的末级分类，我可以整理一个新分类建议。推荐只是建议，必须确认后再按父级到子级创建。");
        return String.join("\n", lines);
    }

    static String validation(ProductValidationVO result, List<ProductDraftDTO> drafts) {
        List<ProductValidationVO.Item> items = nullSafe(result.getItems());
        int total = result.getTotal() > 0 ? result.getTotal() : items.size();
        if (result.isValid()) {
            List<String> lines = new ArrayList<>();
            lines.add("校验通过：共 " + total + " 个商品，全部可导入。");
            if (hasImageInput(drafts)) {
                lines.add("当前商品包含图片：请确认当前 AI 使用的是支持图片识别的模型；不支持视觉时请先补充文字信息，不要臆测图片字段。");
            }
            lines.add("以下是服务端归一化后的最终草稿，请让用户核对名称、编码、价格、分类、素材和上下架状态：");
            lines.addAll(echo(result, drafts));
            lines.add("只有用户明确确认具体商品和是否上架后，才能调用导入工具，原样传入 previewId=" + result.getPreviewId()
                    + "，同时提供一个 requestId 作为幂等键。‘收到’、‘好的’或‘开始执行’不算确认。");
            return String.join("\n", lines);
        }

        List<ProductValidationVO.Item> bad = items.stream().filter(i -> !i.isValid()).toList();
        List<String> lines = new ArrayList<>();
        lines.add("校验未通过：共 " + total + " 个商品，其中 " + bad.size() + " 个存在字段问题，暂不能导入。");
        for (ProductValidationVO.Item item : bad) {
            List<String> fields = new ArrayList<>();
            for (ProductValidationVO.FieldError error : nullSafe(item.getErrors())) {
                fields.add(error.getField() + "(" + error.getCode() + ")");
            }
            lines.add("- 第 " + item.getIndex() + " 项 " + blankTo(item.getProductName(), "未命名")
                    + "：" + String.join("、", fields));
            if (!nullSafe(item.getMissingFields()).isEmpty()) {
                lines.add("  还缺少：" + String.join("、", item.getMissingFields()));
            }
        }
        if (!nullSafe(result.getMissingFields()).isEmpty()) {
            lines.add("当前阻塞字段：" + String.join("、", result.getMissingFields()));
        }
        lines.add("请只补齐上述阻塞字段后重新校验，不要用未经确认的 AI 猜测值代替。");
        if (hasImageInput(drafts)) {
            lines.add("当前商品包含图片：请先使用支持图片识别的模型完成图片分析，或让用户补充文字信息。");
        }
        return String.join("\n", lines);
    }

    private static boolean hasImageInput(List<ProductDraftDTO> drafts) {
        for (ProductDraftDTO draft : nullSafe(drafts)) {
            if (draft == null) continue;
            if (imageCount(draft) > 0) return true;
            if (draft.getMedia() != null && draft.getMedia().stream().anyMatch(m -> m != null && "image".equalsIgnoreCase(m.getType()))) return true;
        }
        return false;
    }

    /**
     * 逐项回显服务端归一化后的关键字段。
     *
     * <p>客户端可能将用户输入、默认值和建议值组合后提交；此处只取服务端反序列化
     * 与归一化后的值，避免把客户端串位的字段直接当作最终结果展示。
     */
    private static List<String> echo(ProductValidationVO result, List<ProductDraftDTO> drafts) {
        List<String> lines = new ArrayList<>();
        int index = 1;
        List<ProductValidationVO.Item> items = nullSafe(result.getItems());
        for (ProductDraftDTO draft : nullSafe(drafts)) {
            if (draft == null) {
                continue;
            }
            ProductValidationVO.Item item = index <= items.size() ? items.get(index - 1) : null;
            ProductDraftDTO normalized = item == null || item.getProduct() == null ? draft : item.getProduct();
            List<String> parts = new ArrayList<>();
            parts.add("售价 " + nullTo(normalized.getProductPrice(), "未填") + sourceSuffix(item, "productPrice"));
            parts.add("分类 " + blankTo(normalized.getCategoryName(),
                    normalized.getCategoryId() == null ? "未填" : "ID " + normalized.getCategoryId()) + sourceSuffix(item, "categoryName"));
            int mediaImageCount = imageCount(normalized);
            int mediaVideoCount = normalized.getMedia() == null ? 0 : (int) normalized.getMedia().stream()
                    .filter(m -> m != null && "video".equalsIgnoreCase(m.getType())).count();
            parts.add("图片 " + mediaImageCount + " 张" + sourceSuffix(item, "images"));
            if (mediaVideoCount > 0) parts.add("视频 " + mediaVideoCount + " 个");
            if (count(normalized.getSkus()) > 0) {
                parts.add("规格 " + count(normalized.getSkus()) + " 项");
            } else {
                parts.add("库存 " + nullTo(normalized.getProductStock(), "0") + sourceSuffix(item, "productStock"));
            }
            if (normalized.getMarketPrice() != null) {
                parts.add("市场价 " + normalized.getMarketPrice() + sourceSuffix(item, "marketPrice"));
            }
            if (count(normalized.getDetailImages()) > 0) {
                parts.add("详情图 " + count(normalized.getDetailImages()) + " 张");
            }
            lines.add("- " + index++ + ". " + blankTo(normalized.getProductName(), "未命名")
                    + "（编码 " + blankTo(normalized.getProductSn(), "未填") + sourceSuffix(item, "productSn") + "）："
                    + String.join("，", parts));
            if (item != null && !nullSafe(item.getWarnings()).isEmpty()) {
                lines.add("  提醒：" + String.join("；", item.getWarnings()));
            }
        }
        return lines;
    }

    /**
     * 将素材查询结果写入 MCP 文本内容，避免远程客户端只展示固定状态摘要而忽略 structuredContent。
     * 结构化载荷仍由调用方单独返回，文本最多展示 20 条可读明细。
     */
    static String mediaQuery(Map<String, Object> payload, String operation) {
        Map<String, Object> safe = payload == null ? Map.of() : payload;
        String op = isBlank(operation) ? "list" : operation;
        if ("groups".equalsIgnoreCase(op)) {
            List<Map<String, Object>> groups = mapList(safe.get("groups"));
            if (groups.isEmpty()) return "素材分组为空。";
            List<String> lines = new ArrayList<>();
            lines.add("素材分组：共 " + groups.size() + " 个。");
            groups.stream().limit(20).forEach(group -> lines.add("- "
                    + value(group, "name", "未命名分组") + "（分组 ID：" + value(group, "id", "未返回")
                    + "，类型：" + value(group, "type", "image") + "，素材数：" + value(group, "assetCount", "0") + "）"));
            if (groups.size() > 20) lines.add("其余分组已省略，可按分组 ID 再查询。");
            return String.join("\n", lines);
        }
        if ("detail".equalsIgnoreCase(op)) {
            Map<String, Object> item = mapValue(safe.get("item"));
            return item.isEmpty() ? "没有找到该素材。" : mediaItem("素材详情", item);
        }

        List<Map<String, Object>> items = mapList(safe.get("items"));
        long total = number(safe.get("total"), items.size());
        if (items.isEmpty()) return total == 0 ? "素材中心没有找到符合条件的素材。" : "素材查询完成，但当前页没有可展示的素材。";
        List<String> lines = new ArrayList<>();
        lines.add("素材查询完成：共找到 " + total + " 个素材，本次展示 " + Math.min(items.size(), 20) + " 个。");
        items.stream().limit(20).forEach(item -> lines.add(mediaItem(null, item)));
        if (items.size() > 20 || total > 20) lines.add("其余素材已省略，可通过分页或关键词继续查询。");
        return String.join("\n", lines);
    }

    private static String mediaItem(String title, Map<String, Object> item) {
        String name = value(item, "name", "未命名素材");
        String type = value(item, "type", "未知");
        StringBuilder line = new StringBuilder();
        if (title != null) line.append(title).append("：");
        line.append(name).append("\n  素材 ID：").append(value(item, "id", "未返回"))
                .append("，类型：").append(type)
                .append("，分组：").append(value(item, "groupId", "未分组"))
                .append("\n  地址：").append(value(item, "url", "未返回"));
        String url = value(item, "url", "");
        if ("image".equalsIgnoreCase(type) && httpUrl(url)) {
            line.append("\n  ![").append(markdownText(name)).append("](").append(url).append(")");
        }
        if ("video".equalsIgnoreCase(type) && httpUrl(value(item, "coverUrl", ""))) {
            line.append("\n  视频封面：![").append(markdownText(name)).append("](")
                    .append(value(item, "coverUrl", "")).append(")");
        }
        return line.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().filter(item -> item instanceof Map<?, ?>)
                .map(item -> (Map<String, Object>) item).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static String value(Map<String, Object> item, String key, String fallback) {
        Object value = item.get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
    }

    private static long number(Object value, long fallback) {
        try { return value == null ? fallback : Long.parseLong(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static boolean httpUrl(String value) {
        return value != null && (value.startsWith("https://") || value.startsWith("http://"));
    }

    private static String markdownText(String value) {
        return value == null ? "素材" : value.replace("[", "\\[").replace("]", "\\]");
    }

    private static String sourceSuffix(ProductValidationVO.Item item, String field) {
        if (item == null || item.getSource() == null) return "";
        String source = item.getSource().get(field);
        if (source == null && "categoryName".equals(field)) source = item.getSource().get("categoryId");
        return source == null ? "" : "（" + sourceText(source) + "）";
    }

    private static String sourceText(String source) {
        return switch (source) {
            case "ai_recommended" -> "AI建议";
            case "site_default" -> "系统默认";
            case "user_provided" -> "用户提供";
            default -> source;
        };
    }

    private static int count(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private static int imageCount(ProductDraftDTO draft) {
        Set<String> images = new LinkedHashSet<>();
        for (String url : nullSafe(draft.getImages())) {
            if (!isBlank(url)) images.add("url:" + url);
        }
        int anonymousIndex = 0;
        if (draft.getMedia() != null) {
            for (var media : draft.getMedia()) {
                if (media == null || !"image".equalsIgnoreCase(media.getType())
                        || "detail_image".equalsIgnoreCase(media.getRole())) {
                    continue;
                }
                String key = !isBlank(media.getUrl()) ? "url:" + media.getUrl()
                        : media.getAssetId() != null ? "asset:" + media.getAssetId()
                        : "media:" + anonymousIndex++;
                images.add(key);
            }
        }
        return images.size();
    }

    private static String nullTo(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    static String importResult(ProductImportResultVO result, String baseUrl) {
        List<ProductImportResultVO.Item> items = nullSafe(result.getItems());
        int total = result.getTotal() > 0 ? result.getTotal() : items.size();

        List<String> lines = new ArrayList<>();
        lines.add("导入完成：共 " + total + " 个，成功 " + result.getSuccessCount()
                + " 个，失败 " + result.getFailureCount() + " 个。");

        List<ProductImportResultVO.Item> created = items.stream().filter(ProductImportResultVO.Item::isSuccess).toList();
        List<ProductImportResultVO.Item> failed = items.stream().filter(i -> !i.isSuccess()).toList();

        if (!created.isEmpty()) {
            lines.add("已创建的商品：");
            for (ProductImportResultVO.Item item : created) {
                lines.add("- " + item.getProductName() + "（编号 " + item.getProductSn() + "，商品ID "
                        + (item.getProductId() == null ? "未返回" : item.getProductId()) + "）当前为"
                        + statusText(item.getProductStatus()) + "状态");
                ProductImportResultVO.Verification verification = item.getVerification();
                if (verification == null) {
                    lines.add("  写后核对：未返回核对结果，请在后台核对该商品");
                } else {
                    lines.add("  写后核对：" + verificationText(verification));
                }
            }
            // 建议已随规格补全等逐项行为而不同，只取第一条会让其余商品的提示消失
            List<String> distinct = created.stream()
                    .map(ProductImportResultVO.Item::getSuggestion)
                    .filter(s -> !isBlank(s))
                    .distinct()
                    .toList();
            if (distinct.size() == 1) {
                lines.add(distinct.get(0) + "。");
            } else {
                for (ProductImportResultVO.Item item : created) {
                    if (!isBlank(item.getSuggestion())) {
                        lines.add(item.getProductName() + "：" + item.getSuggestion() + "。");
                    }
                }
            }
            lines.add("后台商品列表：" + baseUrl + ADMIN_PRODUCT_LIST + "（按商品编码搜索即可定位）");
            lines.add("我还能帮你：查询商品详情、补充库存、调整上下架、生成推广链接、查询关联素材或分析同类商品表现。你想继续哪一项？");
        }

        if (!failed.isEmpty()) {
            lines.add("未创建的商品：");
            for (ProductImportResultVO.Item item : failed) {
                String who = blankTo(item.getProductName(), "第 " + item.getIndex() + " 项");
                String sn = isBlank(item.getProductSn()) ? "" : "（编号 " + item.getProductSn() + "）";
                String reason = blankTo(item.getMessage(), blankTo(item.getErrorCode(), "创建失败"));
                String tail = isBlank(item.getSuggestion()) ? "" : "；" + item.getSuggestion();
                lines.add("- " + who + sn + "：" + reason + tail);
            }
        }
        return String.join("\n", lines);
    }

    private static String verificationText(ProductImportResultVO.Verification verification) {
        String status = verification.getStatus();
        if ("verified".equals(status)) return "已核对一致";
        if ("mismatch".equals(status)) return "商品已创建，但字段不一致：" + String.join("、", nullSafe(verification.getMismatches()));
        return "创建结果已返回，但写后核对失败" + (isBlank(verification.getMessage()) ? "" : "：" + verification.getMessage());
    }

    private static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "上架" : status == 0 ? "下架" : "未知";
    }

    private static <T> List<T> nullSafe(List<T> value) {
        return value == null ? List.of() : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankTo(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
