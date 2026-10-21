package com.tigshop.bean.vo.workbuddy;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class McpCapabilitiesVO {
    private String version = "1.1";
    private List<String> supportedIntents = new ArrayList<>(List.of(
            "create_product",
            "query_product",
            "update_product",
            "manage_media",
            "import_category",
            "analyze_product"
    ));
    private Product product = new Product();
    private Media media = new Media();
    /** 面向所有 MCP 客户端的自然语言业务指引，不绑定具体客户端。 */
    private Assistant assistant = new Assistant();

    @Data
    public static class Assistant {
        private String mode = "natural_language";
        private Boolean requiresTemplate = false;
        private String rulesVersion = "1.0";
        private List<String> rules = new ArrayList<>();
        /** 图片参与商品流程时，客户端必须选用支持视觉输入的模型。 */
        private Boolean requiresVisionModelForImages = true;
        private String visionModelGuidance = "图片参与商品创建时，请使用支持图片识别的模型；不支持视觉时请先补充文字信息，不能臆测图片字段。";
        private String imageWorkflow = "vision_required";
    }
    private List<String> unsupported = new ArrayList<>(List.of("external_market_data"));

    @Data
    public static class Product {
        private List<String> createRequired = new ArrayList<>(List.of("productName", "productSn", "productPrice", "categoryId|categoryName", "images"));
        private List<String> confirmationFlow = new ArrayList<>(List.of("prepare.validate", "user_confirmation", "execute.create"));
        private Integer maxBatchProducts = 50;
    }
    @Data
    public static class Media {
        /** 客户端有本地附件时必须走 multipart 暂存 + 短时 fileToken。 */
        private Boolean supportsFileToken = true;
        private String preferredUploadMode = "fileToken";
        private String uploadProtocol = "multipart/form-data";
        private String stageEndpoint = "/adminapi/openapi/mcp/v1/media/upload-token";
        private Long tokenTtlSeconds = 1800L;
        private Boolean base64Fallback = false;
        private Boolean strictUploadMode = true;
        /** 客户端必须具备的附件桥接能力；不满足时应立即停止媒体上传。 */
        private String requiredClientCapability = "multipart_attachment_bridge";
        private String workflowVersion = "2026-09-04-upload-token-v2";
        /** 客户端预览外链素材时的兼容提示；不同客户端可能受 CSP 白名单限制。 */
        private Boolean supportsMarkdownPreview = true;
        private Boolean externalUrlMayBeBlockedByCsp = true;
        private String previewFallback = "client_local_gallery";
        private List<String> types = new ArrayList<>(List.of("image", "video"));
        private List<String> imageExtensions = new ArrayList<>(List.of("jpeg", "png", "gif", "webp", "tiff"));
        private List<String> videoExtensions = new ArrayList<>(List.of("mp4"));
        private Long maxFileBytes = 52428800L;
        private Long imageMaxFileBytes = 52428800L;
        private Long videoMaxFileBytes = 52428800L;
        private Long maxBatchBytes = 41943040L;
        private Integer maxFilesPerCall = 20;
        private Integer defaultGroupId = 0;
    }
}
