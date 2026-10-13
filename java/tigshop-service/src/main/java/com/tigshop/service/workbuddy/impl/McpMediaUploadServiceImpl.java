package com.tigshop.service.workbuddy.impl;

import com.tigshop.bean.dto.setting.GalleryVideoInfoDTO;
import com.tigshop.bean.dto.workbuddy.MediaUploadFileDTO;
import com.tigshop.bean.dto.workbuddy.MediaUploadRequestDTO;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.bean.vo.workbuddy.MediaAssetVO;
import com.tigshop.bean.vo.workbuddy.MediaUploadResultVO;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.setting.GalleryService;
import com.tigshop.service.video.GalleryVideoInfoService;
import com.tigshop.service.video.GalleryVideoService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import com.tigshop.service.workbuddy.McpMediaUploadService;
import com.tigshop.common.exception.GlobalException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.tigshop.common.constant.ExceptionConstants.SYSTEM_ERROR;
import static com.tigshop.common.constant.ExceptionConstants.UPLOAD_FAIL;
import static com.tigshop.common.constant.ExceptionConstants.UPLOAD_FILE_STORE_ERROR;

@Service
public class McpMediaUploadServiceImpl implements McpMediaUploadService {
    private static final int MAX_FILES = 20;
    private static final long BATCH_CAP = 40L * 1024 * 1024;
    private static final long TOKEN_TTL_MILLIS = 30 * 60 * 1000L;
    private final Map<String, TempFile> staged = new java.util.concurrent.ConcurrentHashMap<>();
    @Resource private GalleryPicService galleryPicService;
    @Resource private GalleryService galleryService;
    @Resource private GalleryVideoInfoService galleryVideoInfoService;
    @Resource private GalleryVideoService galleryVideoService;
    @Resource private ConfigService configService;
    @Value("${spring.servlet.multipart.max-file-size:50MB}") private String multipartMaxFileSize;

    @Override
    public MediaUploadResultVO upload(MediaUploadRequestDTO request, Integer shopId) {
        // This MCP endpoint is platform scoped.  The controller still passes the
        // authenticated key's shop id for a stable service contract, but this
        // adapter deliberately normalizes it to platform id 0.  Existing gallery
        // services resolve tenant scope from WorkBuddyRequestContext.
        int platformShopId = normalizePlatformShopId(shopId);
        MediaUploadResultVO result = new MediaUploadResultVO();
        List<MediaUploadFileDTO> files = request == null || request.getFiles() == null ? List.of() : request.getFiles();
        result.setTotal(files.size());
        if (request == null || !request.isConfirmation()) {
            result.setStatus("failed"); result.setFailedCount(files.size()); result.setSummary("CONFIRMATION_REQUIRED"); result.getFailureSummary().put("CONFIRMATION_REQUIRED", files.size()); return result;
        }
        if (files.isEmpty() || files.size() > MAX_FILES) {
            result.setStatus("failed"); result.setFailedCount(files.size()); result.getFailureSummary().put("INVALID_FILE_COUNT", 1); result.setSummary("file count must be 1-20"); return result;
        }
        if (request.getGroupId() != null && request.getGroupId() < 0) {
            result.setStatus("failed");
            result.setFailedCount(files.size());
            result.getFailureSummary().put("INVALID_GROUP", files.size());
            result.setSummary("INVALID_GROUP");
            return result;
        }
        long totalBytes = 0;
        for (MediaUploadFileDTO f : files) {
            try {
                byte[] data = decode(f);
                if (totalBytes + data.length > BATCH_CAP) throw invalid("FILE_TOO_LARGE");
                totalBytes += data.length;
                MediaAssetVO asset = persist(f, data, platformShopId, request.getGroupId());
                result.getAssets().add(asset);
            } catch (Exception e) {
                Failure failure = classifyFailure(e);
                result.getFailureSummary().merge(failure.code(), 1, Integer::sum);
                result.setRetryable(result.isRetryable() || failure.retryable());
            }
        }
        result.setSuccessCount(result.getAssets().size());
        result.setFailedCount(result.getTotal() - result.getSuccessCount());
        result.setStatus(result.getSuccessCount() == result.getTotal() ? "success" : result.getSuccessCount() == 0 ? "failed" : "partial");
        result.setSummary(String.format("本次上传完成：共 %d 个文件，成功 %d 个，失败 %d 个。",
                result.getTotal(), result.getSuccessCount(), result.getFailedCount()));
        return result;
    }

    @Override
    public Map<String, Object> stage(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.size() > MAX_FILES) {
            throw new WorkBuddyApiException("INVALID_FILE_COUNT", "files must contain 1-20 files", 400, false);
        }
        cleanupStaged();
        List<Map<String, Object>> result = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                if (file == null || file.isEmpty()) throw invalid("INVALID_FILE");
                byte[] bytes = file.getBytes();
                if (bytes.length > multipartBytes()) throw invalid("FILE_TOO_LARGE");
                String mime = file.getContentType() == null ? "application/octet-stream" : file.getContentType().toLowerCase(Locale.ROOT);
                String name = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
                String token = "mcp_" + UUID.randomUUID();
                long expiresAt = System.currentTimeMillis() + TOKEN_TTL_MILLIS;
                staged.put(token, new TempFile(name, mime, bytes, expiresAt));
                result.add(Map.of("fileToken", token, "name", name, "mimeType", mime, "size", bytes.length, "expiresAt", expiresAt / 1000));
            } catch (IOException e) {
                throw new WorkBuddyApiException("UPLOAD_RETRYABLE", "临时文件读取失败", 503, true);
            }
        }
        return Map.of("status", "success", "files", result);
    }

    @Override
    public MediaUploadResultVO uploadByTokens(MediaUploadRequestDTO request, Integer shopId) {
        if (request == null || request.getFiles() == null || request.getFiles().isEmpty()) {
            throw new WorkBuddyApiException("UPLOAD_MODE_REQUIRED", "媒体上传必须提供 fileToken 文件列表", 400, false);
        }
        cleanupStaged();
        for (MediaUploadFileDTO file : request.getFiles()) {
            if (file == null || blank(file.getFileToken())) {
                throw new WorkBuddyApiException("UPLOAD_MODE_REQUIRED", "媒体上传只允许 fileToken，禁止使用 Base64、本地路径或其他上传方式", 400, false);
            }
            TempFile temp = staged.get(file.getFileToken());
            if (temp == null) throw new WorkBuddyApiException("FILE_TOKEN_EXPIRED", "临时文件不存在或已过期", 400, false);
            // Token metadata is authoritative; callers may only choose role/group.
            file.setName(temp.name());
            file.setMimeType(temp.mimeType());
            file.setContentBase64(Base64.getEncoder().encodeToString(temp.bytes()));
        }
        MediaUploadResultVO result = upload(request, shopId);
        request.getFiles().forEach(file -> { if (file != null && !blank(file.getFileToken())) staged.remove(file.getFileToken()); });
        return result;
    }

    private void cleanupStaged() {
        long now = System.currentTimeMillis();
        staged.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
    }

    private record TempFile(String name, String mimeType, byte[] bytes, long expiresAt) { }

    private byte[] decode(MediaUploadFileDTO f) {
        String name = f == null ? null : f.getName();
        String rawMime = f == null ? null : f.getMimeType();
        String rawContent = f == null ? null : f.getContentBase64();
        if (f == null || blank(name) || name.length() > 200 || blank(rawContent)) throw invalid("INVALID_FILE");
        if (invalidName(name)) throw invalid("INVALID_PATH");
        String mime = rawMime == null ? "" : rawMime.toLowerCase(Locale.ROOT);
        Set<String> allowedImages = Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/tiff");
        if (!allowedImages.contains(mime) && !"video/mp4".equals(mime)) throw invalid("UNSUPPORTED_MIME");
        if (!matchesExtension(name, mime)) throw invalid("UNSUPPORTED_MIME");
        String value = rawContent; int comma = value.indexOf(',');
        if (value.regionMatches(true, 0, "data:", 0, 5)) {
            if (comma < 0 || !value.substring(0, comma).toLowerCase(Locale.ROOT).endsWith(";base64")) throw invalid("INVALID_BASE64");
            String prefixMime = value.substring(5, value.indexOf(';')).toLowerCase(Locale.ROOT);
            if (!prefixMime.equals(mime)) throw invalid("UNSUPPORTED_MIME");
            value = value.substring(comma + 1);
        }
        // MIME/Base64 clients commonly wrap long payloads; normalize whitespace before
        // length and signature checks while never logging or persisting the raw payload.
        value = value.replaceAll("\\s+", "");
        if (value.length() % 4 != 0) throw invalid("INVALID_BASE64");
        long estimated = (long) value.length() * 3 / 4;
        long limit = "video/mp4".equals(mime) ? multipartBytes() : imageBytes();
        if (estimated > limit || estimated > BATCH_CAP) throw invalid("FILE_TOO_LARGE");
        final byte[] bytes;
        try { bytes = Base64.getDecoder().decode(value); }
        catch (IllegalArgumentException ex) { throw invalid("INVALID_BASE64"); }
        if (bytes.length == 0 || bytes.length > limit) throw invalid("FILE_TOO_LARGE");
        if (!signatureValid(bytes, mime)) throw invalid("UNSUPPORTED_MIME");
        return bytes;
    }

    private MediaAssetVO persist(MediaUploadFileDTO f, byte[] data, int platformShopId, Integer defaultGroupId) throws IOException {
        Integer group = f.getGroupId() == null ? (defaultGroupId == null ? 0 : defaultGroupId) : f.getGroupId();
        if (group < 0) throw invalid("INVALID_GROUP");
        String mime = f.getMimeType().toLowerCase(Locale.ROOT); String type = mime.startsWith("video/") ? "video" : "image";
        String fileName = f.getName();
        if (group > 0 && !groupInPlatformScope(group, type)) throw invalid("MEDIA_SCOPE_MISMATCH");
        MultipartFile mf = new BytesMultipartFile(fileName, mime, data);
        MediaAssetVO a = new MediaAssetVO(); a.setClientFileId(f.getClientFileId()); a.setType(type); a.setGroupId(group);
        String role = blank(f.getRole()) ? (type.equals("image") ? "gallery_image" : "video") : f.getRole();
        if (!(type.equals("image") && Set.of("main_image", "gallery_image", "detail_image").contains(role))
                && !(type.equals("video") && Set.of("video").contains(role))) throw invalid("INVALID_ROLE");
        a.setRole(role);
        if (type.equals("image")) {
            GalleryPicUploadVO v = galleryPicService.uploadImg(group, mf); a.setAssetId(v.getPicId()); a.setUrl(v.getPicUrl()); a.setFormat(extension(fileName));
        } else {
            UploadVideoInfoVO v = galleryVideoInfoService.uploadVideo(mf); GalleryVideoInfoDTO d = new GalleryVideoInfoDTO(); d.setVideoUrl(v.getVideoUrl()); d.setVideoName(v.getVideoName()); d.setVideoCover(v.getVideoCover()); d.setFormat(v.getFormat()); d.setSource("upload"); d.setGalleryId(List.of(group)); a.setAssetId(galleryVideoInfoService.saveAndReturnId(d)); a.setUrl(v.getVideoUrl()); a.setCoverUrl(v.getVideoCover()); a.setFormat(v.getFormat());
        }
        return a;
    }

    /** Group IDs are client supplied, so verify ownership before passing them
     * to the legacy upload services (whose write methods update by ID only). */
    private boolean groupInPlatformScope(Integer groupId, String type) {
        if (groupId == null || groupId <= 0) return true;
        if ("video".equals(type)) {
            if (galleryVideoService == null) return false;
            com.tigshop.bean.model.video.GalleryVideo group = galleryVideoService.getById(groupId);
            return group != null && (group.getShopId() == null || group.getShopId() == 0)
                    && (group.getVendorId() == null || group.getVendorId() == 0);
        }
        if (galleryService == null) return false;
        com.tigshop.bean.model.setting.Gallery group = galleryService.getById(groupId);
        return group != null && (group.getShopId() == null || group.getShopId() == 0)
                && (group.getVendorId() == null || group.getVendorId() == 0);
    }

    private int normalizePlatformShopId(Integer shopId) {
        if (shopId != null && shopId < 0) {
            throw new WorkBuddyApiException("INVALID_SHOP_ID", "shopId must be non-negative", 400, false);
        }
        Integer contextShopId = currentContextShopId();
        if (contextShopId != null && contextShopId != 0) {
            throw new WorkBuddyApiException("PLATFORM_SCOPE_REQUIRED", "media upload requires platform shop scope", 403, false);
        }
        return 0;
    }

    /**
     * Validate the request context that downstream Gallery services use for
     * tenant scoping. The controller's WorkBuddyRequestContext wrapper exposes
     * {@code X-ADMIN-TYPE=admin}, which resolves to shop 0 in HeaderUtils.
     * Direct service callers without a servlet context remain platform-scoped
     * by the normalized value above.
     */
    private Integer currentContextShopId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        HttpServletRequest request = servletAttributes.getRequest();
        String adminType = request.getHeader("X-ADMIN-TYPE");
        if (adminType == null || adminType.isBlank() || "admin".equalsIgnoreCase(adminType)) {
            return 0;
        }
        if ("shop".equalsIgnoreCase(adminType) || "pos".equalsIgnoreCase(adminType)) {
            String value = request.getHeader("X-SHOP-ID");
            if (value == null || value.isBlank()) {
                return -1;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        // Any other authenticated tenant type (for example vendor) is outside
        // this platform-only endpoint and must not reach gallery persistence.
        return -1;
    }

    private static ValidationException invalid(String code) {
        return new ValidationException(code);
    }

    /** Classifies by typed exceptions/status, never by localized exception text fragments. */
    private Failure classifyFailure(Throwable error) {
        WorkBuddyApiException workBuddy = null;
        GlobalException global = null;
        ValidationException validation = null;
        boolean numberFormat = false;
        boolean io = false;
        boolean runtime = false;
        Throwable current = error;
        while (current != null) {
            if (workBuddy == null && current instanceof WorkBuddyApiException e) {
                workBuddy = e;
            }
            if (global == null && current instanceof GlobalException e) {
                global = e;
            }
            if (validation == null && current instanceof ValidationException e) {
                validation = e;
            }
            numberFormat |= current instanceof NumberFormatException;
            io |= current instanceof IOException;
            runtime |= current instanceof RuntimeException;
            current = current.getCause();
        }
        if (workBuddy != null) {
            String code = blank(workBuddy.getErrorCode()) ? statusCode(workBuddy.getCode()) : workBuddy.getErrorCode();
            boolean retryable = workBuddy.isRetryable() || "STORAGE_NOT_CONFIGURED".equals(code) || "UPLOAD_RETRYABLE".equals(code);
            return new Failure(code, retryable);
        }
        if (validation != null) {
            return new Failure(validation.errorCode(), false);
        }
        if (global != null) {
            String message = global.getMessage();
            if ("STORAGE_NOT_CONFIGURED".equals(message) || UPLOAD_FILE_STORE_ERROR.equals(message)) {
                return new Failure("STORAGE_NOT_CONFIGURED", true);
            }
            if ("UPLOAD_RETRYABLE".equals(message) || UPLOAD_FAIL.equals(message) || SYSTEM_ERROR.equals(message)) {
                return new Failure("UPLOAD_RETRYABLE", true);
            }
            if (com.tigshop.common.constant.ExceptionConstants.UPLOAD_FILE_TYPE_ERROR.equals(message)
                    || com.tigshop.common.constant.ExceptionConstants.VIDEO_CODEC_NOT_SUPPORTED.equals(message)) {
                return new Failure("UNSUPPORTED_MIME", false);
            }
            if (global.getCode() == 413) {
                return new Failure("FILE_TOO_LARGE", false);
            }
            if (global.getCode() == 415) {
                return new Failure("UNSUPPORTED_MIME", false);
            }
            if (global.getCode() >= 500) {
                return new Failure("UPLOAD_RETRYABLE", true);
            }
            return new Failure("INVALID_FILE", false);
        }
        if (io) {
            return new Failure("UPLOAD_RETRYABLE", true);
        }
        // ConfigStorageDTO parses STORAGE_TYPE as an integer; absent/malformed
        // configuration is a typed NumberFormatException rather than an input
        // validation failure. Other unchecked exceptions are treated as SDK or
        // storage runtime failures and may be retried.
        if (numberFormat) {
            return new Failure("STORAGE_NOT_CONFIGURED", true);
        }
        if (runtime) {
            return new Failure("UPLOAD_RETRYABLE", true);
        }
        return new Failure("INVALID_FILE", false);
    }

    private static String statusCode(int status) {
        if (status == 413) return "FILE_TOO_LARGE";
        if (status == 415) return "UNSUPPORTED_MIME";
        if (status >= 500) return "UPLOAD_RETRYABLE";
        return "INVALID_FILE";
    }

    private record Failure(String code, boolean retryable) { }

    private static final class ValidationException extends IllegalArgumentException {
        private final String errorCode;
        private ValidationException(String errorCode) {
            super(errorCode);
            this.errorCode = errorCode;
        }
        private String errorCode() { return errorCode; }
    }
    private long imageBytes() { long configured = 20L * 1048576; try { String v=configService.getConfigVal(com.tigshop.bean.enums.setting.SettingsEnum.IMAGE_UPLOAD_MAX_SIZE); configured = (long)(Double.parseDouble(v)*1048576); } catch(Exception ignored) { } return Math.min(configured, multipartBytes()); }
    private long multipartBytes() { String s=multipartMaxFileSize==null?"50MB":multipartMaxFileSize.toUpperCase(); try { if(s.endsWith("MB")) return Long.parseLong(s.replace("MB", "").trim())*1048576; if(s.endsWith("KB")) return Long.parseLong(s.replace("KB", "").trim())*1024; return Long.parseLong(s.trim()); } catch(Exception e){ return 50L*1048576; } }
    private static boolean blank(String s){ return s==null||s.isBlank(); } private static boolean invalidName(String n){ return n.contains("/")||n.contains("\\")||n.contains("..")||n.contains(":"); } private static String extension(String n){ int i=n.lastIndexOf('.'); return i>0?n.substring(i+1).toLowerCase():"";} private static boolean matchesExtension(String name, String mime) { String ext = extension(name); return switch (mime) { case "image/jpeg" -> Set.of("jpg", "jpeg").contains(ext); case "image/png" -> "png".equals(ext); case "image/gif" -> "gif".equals(ext); case "image/webp" -> "webp".equals(ext); case "image/tiff" -> Set.of("tif", "tiff").contains(ext); case "video/mp4" -> "mp4".equals(ext); default -> false; }; }
    private static boolean signatureValid(byte[] b,String m){ if(m.equals("image/png")) return b.length>=8&&b[0]==(byte)137&&b[1]==80&&b[2]==78&&b[3]==71; if(m.equals("image/jpeg")) return b.length>=3&&(b[0]&255)==255&&(b[1]&255)==216&&(b[2]&255)==255; if(m.equals("image/gif")) return b.length>=6&&new String(b,0,6).startsWith("GIF"); if(m.equals("image/webp")) return b.length>=12&&new String(b,0,4).equals("RIFF")&&new String(b,8,4).equals("WEBP"); if(m.equals("image/tiff")) return b.length>=4&&((b[0]=='I'&&b[1]=='I'&&b[2]==42&&b[3]==0)||(b[0]=='M'&&b[1]=='M'&&b[2]==0&&b[3]==42)); if(m.equals("video/mp4")) return b.length>=8&&b[4]=='f'&&b[5]=='t'&&b[6]=='y'&&b[7]=='p'; return false; }
    private record BytesMultipartFile(String name,String contentType,byte[] bytes) implements MultipartFile { public String getName(){return name;} public String getOriginalFilename(){return name;} public String getContentType(){return contentType;} public boolean isEmpty(){return bytes.length==0;} public long getSize(){return bytes.length;} public byte[] getBytes(){return bytes;} public InputStream getInputStream(){return new ByteArrayInputStream(bytes);} public void transferTo(java.io.File dest) throws IOException { java.nio.file.Files.write(dest.toPath(),bytes);} }
}
