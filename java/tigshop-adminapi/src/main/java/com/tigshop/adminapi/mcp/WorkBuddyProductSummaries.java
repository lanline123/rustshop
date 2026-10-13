package com.tigshop.adminapi.mcp;

import com.tigshop.bean.vo.workbuddy.*;
import java.util.ArrayList;
import java.util.List;

final class WorkBuddyProductSummaries {
    private WorkBuddyProductSummaries() {}

    static String search(ProductSearchVO value) {
        List<String> lines = new ArrayList<>();
        lines.add("在「" + value.getScope() + "」中找到 " + value.getTotal() + " 个商品，本页显示 " + value.getItems().size() + " 个。");
        for (ProductSummaryVO p : value.getItems()) lines.add(item(p));
        return String.join("\n", lines);
    }
    static String detail(ProductDetailVO p) {
        String text = "商品「" + p.getProductName() + "」（编号 " + p.getProductSn() + "）当前" + p.getStatus()
                + "，售价 " + p.getPrice() + "，库存 " + p.getStock() + "，审核状态：" + p.getAuditStatus() + "。";
        return p.getAuditReason() == null ? text : text + " 审核意见：" + p.getAuditReason();
    }

    /** 将编码推断证据透出到 MCP 文本，避免客户端只读取摘要时自行猜测货号。 */
    static String context(ProductContextVO value) {
        ProductContextVO context = value == null ? new ProductContextVO() : value;
        ProductContextVO.Metrics metrics = context.getMetrics();
        int count = metrics == null || metrics.getProductCount() == null
                ? (context.getSampleSize() == null ? 0 : context.getSampleSize())
                : metrics.getProductCount();
        List<String> lines = new ArrayList<>();
        lines.add("已读取 " + count + " 个历史商品，用于总结当前商城的商品编码和字段习惯。");
        ProductContextVO.EncodingInference encoding = context.getEncoding();
        if (encoding == null) {
            lines.add("未返回编码推断结果，请让用户提供或确认商品编码规则后再创建。");
            return String.join("\n", lines);
        }
        if (encoding.getSamples() != null && !encoding.getSamples().isEmpty()) {
            lines.add("历史编码样本：" + String.join("、", encoding.getSamples()));
        }
        if (encoding.getPattern() != null) {
            lines.add("推断编码规律：" + encoding.getPattern()
                    + (encoding.getPrefix() == null ? "" : "（前缀 " + encoding.getPrefix() + "）"));
        }
        if (encoding.getNextCandidate() != null && !encoding.isNeedsConfirmation()) {
            lines.add("按历史最大序号顺延的下一个候选编码：" + encoding.getNextCandidate());
        }
        if (encoding.getConfidence() != null) {
            lines.add("推断置信度：" + Math.round(encoding.getConfidence() * 100) + "%。");
        }
        if (encoding.getReason() != null) {
            lines.add("依据：" + encoding.getReason() + "。");
        }
        if (encoding.isNeedsConfirmation() || encoding.getNextCandidate() == null) {
            lines.add("当前无法可靠自动生成商品编码，请先让用户提供或确认本次编码规则；不能猜测、随机生成或硬编码为 SN 格式。");
        } else {
            lines.add("候选编码只是建议。创建商品前必须在草稿中回显 " + encoding.getNextCandidate()
                    + " 并取得用户明确确认；用户自行提供的编码优先。");
        }
        return String.join("\n", lines);
    }
    static String promotion(PromotionLinksVO p) {
        List<String> lines = new ArrayList<>(); lines.add("商品「" + p.getProductName() + "」的推广链接已生成。");
        if (p.getH5ShortLink() != null) lines.add("H5 短链：" + p.getH5ShortLink());
        if (p.getPcShortLink() != null) lines.add("PC 短链：" + p.getPcShortLink());
        if (p.getWechatShortLink() != null) lines.add("微信短链：" + p.getWechatShortLink());
        if (p.getWechatShortLinkError() != null) lines.add("微信短链未生成：" + p.getWechatShortLinkError() + "，请检查公众号配置后重试。");
        lines.add("商品海报由管理后台页面现场合成，当前接口不能直接提供海报图片。"); return String.join("\n", lines);
    }
    static String write(ProductWriteResultVO p) {
        List<String> lines = new ArrayList<>(); lines.add("已处理商品「" + p.getProductName() + "」：" + p.getChangedField() + "从「" + p.getBefore() + "」变为「" + p.getAfter() + "」。"); lines.addAll(p.getConsequences()); return String.join("\n", lines);
    }
    static String preview(BatchPreviewVO p) {
        List<String> lines = new ArrayList<>(); lines.add(p.getOperation() + "预览：可操作 " + p.getAffected().size() + " 个，不能操作 " + p.getBlocked().size() + " 个。");
        p.getAffected().forEach(v -> lines.add(item(v))); p.getBlocked().forEach(v -> lines.add("- 不能操作「" + v.getProductName() + "」：" + v.getReason())); lines.addAll(p.getConsequences());
        lines.add((p.isIrreversible() ? "此操作不可恢复。" : "") + "确认后请原样带回 previewId=" + p.getPreviewId() + " 并设置 confirmation=true。"); return String.join("\n", lines);
    }
    static String batch(BatchResultVO p) { return p.getOperation() + "完成：成功 " + p.getSuccessCount() + " 个。\n" + String.join("\n", p.getConsequences()); }
    private static String item(ProductSummaryVO p) { return "- 「" + p.getProductName() + "」（编号 " + p.getProductSn() + "）：" + p.getStatus() + "，售价 " + p.getPrice() + "，库存 " + p.getStock(); }
}
