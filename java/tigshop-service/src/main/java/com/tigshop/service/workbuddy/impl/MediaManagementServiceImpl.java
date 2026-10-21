package com.tigshop.service.workbuddy.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.GalleryDTO;
import com.tigshop.bean.dto.setting.GalleryDelDTO;
import com.tigshop.bean.dto.setting.GalleryPicBatchDTO;
import com.tigshop.bean.dto.setting.GalleryPicDTO;
import com.tigshop.bean.dto.setting.GalleryPicListDTO;
import com.tigshop.bean.dto.setting.GalleryPicTransferDTO;
import com.tigshop.bean.dto.setting.GalleryUpdateDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoListDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.workbuddy.*;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.vo.config.*;
import com.tigshop.bean.vo.setting.GalleryTreeNodeVO;
import com.tigshop.bean.vo.setting.GalleryVideoInfoDetailVO;
import com.tigshop.bean.vo.workbuddy.MediaGroupVO;
import com.tigshop.bean.vo.workbuddy.MediaUploadResultVO;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.setting.GalleryService;
import com.tigshop.service.product.ProductGalleryService;
import com.tigshop.service.product.ProductVideoService;
import com.tigshop.service.video.GalleryVideoInfoService;
import com.tigshop.service.video.GalleryVideoService;
import com.tigshop.service.workbuddy.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MediaManagementServiceImpl implements MediaManagementService {
    private final GalleryPicService pics;
    private final GalleryVideoInfoService videos;
    private final GalleryService groups;
    private final McpMediaUploadService uploader;
    private final ProductGalleryService productGalleries;
    private final GalleryVideoService videoGroups;
    private final ProductVideoService productVideos;
    private static final long PREVIEW_TTL_MILLIS = 30 * 60 * 1000L;
    private static final long RESULT_TTL_MILLIS = 24 * 60 * 60 * 1000L;
    private final Map<String, PreviewRecord> previews = new ConcurrentHashMap<>();
    private final Map<String, ResultRecord> idempotent = new ConcurrentHashMap<>();

    public MediaManagementServiceImpl(GalleryPicService pics, GalleryVideoInfoService videos, GalleryService groups) { this(pics,videos,groups,null,null,null,null); }
    public MediaManagementServiceImpl(GalleryPicService pics, GalleryVideoInfoService videos, GalleryService groups, McpMediaUploadService uploader) { this(pics,videos,groups,uploader,null,null,null); }
    public MediaManagementServiceImpl(GalleryPicService pics, GalleryVideoInfoService videos, GalleryService groups, McpMediaUploadService uploader, ProductGalleryService productGalleries) { this(pics,videos,groups,uploader,productGalleries,null,null); }
    public MediaManagementServiceImpl(GalleryPicService pics, GalleryVideoInfoService videos, GalleryService groups, McpMediaUploadService uploader, ProductGalleryService productGalleries, GalleryVideoService videoGroups) { this(pics,videos,groups,uploader,productGalleries,videoGroups,null); }
    @org.springframework.beans.factory.annotation.Autowired
    public MediaManagementServiceImpl(GalleryPicService pics, GalleryVideoInfoService videos, GalleryService groups, McpMediaUploadService uploader, ProductGalleryService productGalleries, GalleryVideoService videoGroups, ProductVideoService productVideos) { this.pics=pics; this.videos=videos; this.groups=groups; this.uploader=uploader; this.productGalleries=productGalleries; this.videoGroups=videoGroups; this.productVideos=productVideos; }

    @Override public Map<String,Object> query(MediaQueryDTO q, Integer shopId) {
        requirePlatform(shopId); q = q == null ? new MediaQueryDTO() : q;
        String op = q.getOperation()==null ? "list" : q.getOperation();
        if ("groups".equals(op) || "group_tree".equals(op)) {
            String groupType = q.getType() == null ? "image" : q.getType().trim().toLowerCase(Locale.ROOT);
            if ("video".equals(groupType) && videoGroups != null) {
                return Map.of("groups", mapGroups(videoGroups.tree(), "video"));
            }
            return Map.of("groups", mapGroups(groups.tree(), "image"));
        }
        if ("detail".equals(op)) {
            if (q.getId()==null) return error("INVALID_ARGUMENT");
            if ("video".equalsIgnoreCase(q.getType())) {
                if (!inPlatformVideoScope(q.getId())) return error("MEDIA_SCOPE_MISMATCH");
                return Map.of("item", mapVideo(videos.detail(q.getId())));
            }
            if (!inPlatformImageScope(q.getId())) return error("MEDIA_SCOPE_MISMATCH");
            return Map.of("item", mapPic(pics.detail(q.getId())));
        }
        String type = q.getType()==null ? "image" : q.getType();
        List<Map<String,Object>> items = new ArrayList<>(); long total=0;
        if ("video".equalsIgnoreCase(type)) {
            GalleryVideoInfoListDTO d = new GalleryVideoInfoListDTO(); d.setId(q.getGroupId()); d.setPage(q.getPage()); d.setSize(q.getPageSize());
            d.setKeyword(q.getKeyword());
            GalleryVideoInfoVO v = videos.list(d); if (v!=null && v.getGalleryVideoInfoPage()!=null) { total=v.getGalleryVideoInfoPage().getTotal(); v.getGalleryVideoInfoPage().getRecords().forEach(x -> items.add(mapVideo(x))); }
        } else {
            GalleryPicListDTO d = new GalleryPicListDTO(); d.setGalleryId(q.getGroupId()); d.setPage(q.getPage()); d.setSize(q.getPageSize()); d.setKeyword(q.getKeyword());
            GalleryListResVO v = pics.list(d); if (v!=null && v.getGalleryPicPage()!=null) { total=v.getGalleryPicPage().getTotal(); v.getGalleryPicPage().getRecords().forEach(x -> items.add(mapPic(x))); }
        }
        return Map.of("items",items,"total",total,"page",q.getPage()==null?1:q.getPage(),"pageSize",q.getPageSize()==null?15:q.getPageSize());
    }

    @Override public Map<String,Object> prepare(MediaOperationDTO op, Integer shopId) {
        requirePlatform(shopId);
        evictExpired();
        if (op == null) return error("INVALID_ARGUMENT");
        if (op.getLocalPath() != null) return error("LOCAL_PATH_NOT_ALLOWED");
        String operation = normalize(op.getOperation());
        if (!SUPPORTED_OPERATIONS.contains(operation)) return error("INVALID_OPERATION");
        if (operation.startsWith("group_") && !isSupportedType(op.getType())) return error("INVALID_TYPE");
        String requestId = trim(op.getRequestId());
        if (requestId == null) return error("REQUEST_ID_REQUIRED");
        String id = UUID.randomUUID().toString();
        String fingerprint = fingerprint(op);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("previewId",id);
        r.put("operation",operation);
        r.put("status","PREPARED");
        r.put("impactCount", impactCount(op));
        r.put("requestId", requestId);
        previews.put(id, new PreviewRecord(shopId == null ? 0 : shopId, requestId, fingerprint,
                System.currentTimeMillis() + PREVIEW_TTL_MILLIS));
        return r;
    }

    @Override public Map<String,Object> execute(MediaOperationDTO op, Integer shopId) {
        requirePlatform(shopId);
        evictExpired();
        if (op==null) return error("INVALID_ARGUMENT");
        if (op.getLocalPath()!=null) return error("LOCAL_PATH_NOT_ALLOWED");
        if (!Boolean.TRUE.equals(op.getConfirmation())) return error("CONFIRMATION_REQUIRED");
        String previewId = trim(op.getPreviewId());
        String requestId = trim(op.getRequestId());
        if (previewId == null) return error("PREVIEW_REQUIRED");
        if (requestId == null) return error("REQUEST_ID_REQUIRED");
        String fingerprint = fingerprint(op);
        ResultRecord previous = idempotent.get(requestId);
        if (previous != null) {
            if (previous.expiresAt() > System.currentTimeMillis() && previous.fingerprint().equals(fingerprint)) {
                return previous.result();
            }
            if (previous.expiresAt() > System.currentTimeMillis()) return error("IDEMPOTENCY_KEY_CONFLICT");
            idempotent.remove(requestId, previous);
        }
        PreviewRecord preview = previews.get(previewId);
        if (preview == null || preview.expiresAt() <= System.currentTimeMillis()) return error("PREVIEW_EXPIRED");
        if (preview.shopId() != (shopId == null ? 0 : shopId)) return error("PREVIEW_SCOPE_MISMATCH");
        if (!preview.requestId().equals(requestId)) return error("PREVIEW_REQUEST_MISMATCH");
        if (!preview.fingerprint().equals(fingerprint)) return error("PREVIEW_MISMATCH");
        Map<String,Object> r;
        String operation = normalize(op.getOperation());
        if (operation.startsWith("group_") && !isSupportedType(op.getType())) return error("INVALID_TYPE");
        try {
            switch (operation) {
                case "upload" -> { if (uploader==null) r=error("SERVICE_UNAVAILABLE"); else { MediaUploadRequestDTO req=new MediaUploadRequestDTO(); req.setConfirmation(true); req.setGroupId(op.getGroupId()); req.setFiles(op.getItems()); MediaUploadResultVO v=uploader.upload(req,0); r=objectMap(v); } }
                case "update" -> r = update(op);
                case "group_create" -> r = createGroup(op);
                case "group_update" -> r = updateGroup(op);
                case "group_delete" -> r = deleteGroup(op);
                case "delete" -> r = deleteDirectly(op);
                default -> r=error("INVALID_OPERATION");
            }
        } catch (WorkBuddyApiException e) {
            r = new LinkedHashMap<>();
            r.put("status", "failed");
            r.put("errorCode", e.getCode());
            r.put("message", e.getMessage());
            r.put("retryable", e.isRetryable());
        } catch (Exception e) { r=error("OPERATION_FAILED"); }
        // 需要补充二次确认或解除引用后才能继续的结果不能消耗预览，
        // 否则客户端拿到 token/处理引用后无法用同一 requestId 重试。
        if (!preservePreviewForRetry(r)) {
            idempotent.put(requestId, new ResultRecord(fingerprint, r, System.currentTimeMillis() + RESULT_TTL_MILLIS));
            previews.remove(previewId);
        }
        return r;
    }

    private void requirePlatform(Integer shopId) { if (shopId!=null && shopId!=0) throw new WorkBuddyApiException("PLATFORM_SCOPE_REQUIRED","media requires platform shop scope",403,false); }
    private static Map<String,Object> error(String c){ return Map.of("errorCode",c,"status","failed"); }

    private static boolean preservePreviewForRetry(Map<String, Object> result) {
        if (result == null || !"failed".equals(result.get("status"))) return false;
        Object code = result.get("errorCode");
        return "MEDIA_IN_USE".equals(code)
                || "SERVICE_UNAVAILABLE".equals(code);
    }

    private static final Set<String> SUPPORTED_OPERATIONS = Set.of(
            "upload", "update", "delete",
            "group_create", "group_update", "group_delete");

    private static String normalize(String operation) {
        return operation == null ? "" : operation.trim().toLowerCase(Locale.ROOT);
    }

    private static String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 预览和幂等校验使用稳定指纹，避免 requestId 被复用执行另一份素材操作。 */
    private static String fingerprint(MediaOperationDTO op) {
        StringBuilder canonical = new StringBuilder();
        canonical.append(normalize(op.getOperation())).append('\u0000')
                .append(op.getId()).append('\u0000')
                .append(op.getIds()).append('\u0000')
                .append(op.getName()).append('\u0000')
                .append(normalize(op.getType())).append('\u0000')
                .append(op.getGroupId()).append('\u0000')
                .append('\u0000');
        if (op.getItems() != null) {
            op.getItems().forEach(item -> canonical.append(item == null ? "null" : item.getId()).append('|')
                    .append(item == null ? "" : item.getClientFileId()).append('|')
                    .append(item == null ? "" : item.getName()).append('|')
                    .append(item == null ? "" : item.getMimeType()).append('|')
                    .append(item == null ? "" : contentDigest(item.getContentBase64())).append('|')
                    .append(item == null ? "" : item.getGroupId()).append('|')
                    .append(item == null ? "" : item.getRole()).append('\u0001'));
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private static String contentDigest(String content) {
        if (content == null) return "";
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        previews.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        idempotent.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
    }

    private Map<String, Object> update(MediaOperationDTO op) {
        Integer id = resolveId(op);
        if (id == null || id < 1) return error("INVALID_ARGUMENT");
        String type = resolveType(op);
        if (!"image".equals(type) && !"video".equals(type)) return error("INVALID_TYPE");
        if (!inPlatformScope(id, type)) return error("MEDIA_SCOPE_MISMATCH");
        String name = resolveName(op);
        boolean changed = false;
        if ("image".equals(type)) {
            if (name != null) {
                UpdateFieldDTO field = new UpdateFieldDTO();
                field.setId(id);
                field.setField("pic_name");
                field.setVal(name);
                if (!pics.updateField(field, new String[]{"pic_name"})) return error("OPERATION_FAILED");
                changed = true;
            }
            if (op.getGroupId() != null) {
                if (op.getGroupId() > 0 && !inPlatformGroupScope(op.getGroupId(), type)) return error("MEDIA_SCOPE_MISMATCH");
                GalleryPicTransferDTO transfer = new GalleryPicTransferDTO();
                transfer.setIds(List.of(id));
                transfer.setTargetGalleryId(op.getGroupId());
                transfer.setType("image");
                pics.transfer(transfer);
                changed = true;
            }
        } else {
            if (op.getGroupId() != null && op.getGroupId() > 0
                    && !inPlatformGroupScope(op.getGroupId(), type)) return error("MEDIA_SCOPE_MISMATCH");
            GalleryVideoInfoDetailVO current = videos.detail(id);
            if (current == null) return error("NOT_FOUND");
            GalleryVideoInfoDTO video = new GalleryVideoInfoDTO();
            video.setId(id);
            video.setVideoUrl(current.getVideoUrl());
            video.setVideoName(name == null ? current.getVideoName() : name);
            video.setVideoCover(current.getVideoCover());
            video.setDescription(current.getDescription());
            video.setFormat(current.getFormat());
            video.setDuration(current.getDuration());
            video.setSize(current.getSize());
            Integer galleryId = op.getGroupId() == null ? Optional.ofNullable(current.getGalleryId()).orElse(0) : op.getGroupId();
            video.setGalleryId(List.of(galleryId));
            videos.update(video);
            changed = name != null || op.getGroupId() != null;
        }
        return changed ? Map.of("status", "success") : error("NO_CHANGES");
    }

    private Map<String, Object> createGroup(MediaOperationDTO op) {
        if (op.getName() == null || op.getName().isBlank()) return error("INVALID_ARGUMENT");
        if (op.getGroupId() != null && op.getGroupId() > 0
                && !inPlatformGroupScope(op.getGroupId(), resolveGroupType(op))) {
            return error("MEDIA_SCOPE_MISMATCH");
        }
        if ("video".equals(resolveGroupType(op))) {
            if (videoGroups == null) return error("SERVICE_UNAVAILABLE");
            com.tigshop.bean.dto.setting.GalleryVideoCreateDTO group = new com.tigshop.bean.dto.setting.GalleryVideoCreateDTO();
            group.setName(op.getName().trim());
            group.setParentId(op.getGroupId() == null ? 0 : op.getGroupId());
            return Map.of("status", videoGroups.create(group) != null ? "success" : "failed", "type", "video");
        }
        GalleryDTO group = new GalleryDTO();
        group.setGalleryName(op.getName().trim());
        group.setParentId(op.getGroupId() == null ? 0 : op.getGroupId());
        return Map.of("status", groups.create(group) ? "success" : "failed", "type", "image");
    }

    private Map<String, Object> updateGroup(MediaOperationDTO op) {
        Integer id = op.getId() == null ? op.getGroupId() : op.getId();
        if (id == null || id < 1 || op.getName() == null || op.getName().isBlank()) return error("INVALID_ARGUMENT");
        if (!inPlatformGroupScope(id, resolveGroupType(op))) return error("MEDIA_SCOPE_MISMATCH");
        if ("video".equals(resolveGroupType(op))) {
            if (videoGroups == null) return error("SERVICE_UNAVAILABLE");
            com.tigshop.bean.dto.setting.GalleryVideoUpdateDTO group = new com.tigshop.bean.dto.setting.GalleryVideoUpdateDTO();
            group.setId(id);
            group.setName(op.getName().trim());
            return Map.of("status", videoGroups.update(group) != null ? "success" : "failed", "type", "video");
        }
        GalleryUpdateDTO group = new GalleryUpdateDTO();
        group.setId(id);
        group.setGalleryName(op.getName().trim());
        return Map.of("status", groups.update(group) ? "success" : "failed", "type", "image");
    }

    private Map<String, Object> deleteGroup(MediaOperationDTO op) {
        Integer id = op.getId() == null ? op.getGroupId() : op.getId();
        if (id == null || id < 1) return error("INVALID_ARGUMENT");
        if (!inPlatformGroupScope(id, resolveGroupType(op))) return error("MEDIA_SCOPE_MISMATCH");
        GalleryDelDTO group = new GalleryDelDTO();
        group.setId(id);
        group.setDeleteMode("only_group");
        if ("video".equals(resolveGroupType(op))) {
            if (videoGroups == null) return error("SERVICE_UNAVAILABLE");
            videoGroups.delWithMode(group);
            return Map.of("status", "success", "type", "video");
        }
        groups.delWithMode(group);
        return Map.of("status", "success", "type", "image");
    }

    private Map<String, Object> deleteDirectly(MediaOperationDTO op) {
        List<Integer> ids = resolveIds(op);
        String type = resolveType(op);
        if (ids.isEmpty() || type == null) return error("INVALID_ARGUMENT");
        if (!allInPlatformScope(ids, type)) return error("MEDIA_SCOPE_MISMATCH");
        if ("image".equals(type) && productGalleries != null && hasProductReference(ids)) {
            return error("MEDIA_IN_USE");
        }
        if ("video".equals(type) && productVideos != null && hasProductVideoReference(ids)) {
            return error("MEDIA_IN_USE");
        }
        GalleryPicBatchDTO batch = new GalleryPicBatchDTO();
        batch.setIds(ids);
        batch.setType("video".equals(type) ? "video" : "image");
        pics.batchDel(batch);
        return Map.of("status", "success", "successCount", ids.size(), "failureCount", 0);
    }

    private boolean hasProductReference(List<Integer> ids) {
        for (Integer id : ids) {
            GalleryPic pic = pics.detail(id);
            if (pic == null || pic.getPicUrl() == null) continue;
            if (productGalleries.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.tigshop.bean.model.product.ProductGallery>()
                    .and(w -> w.eq("pic_url", pic.getPicUrl())
                            .or().eq("pic_thumb", pic.getPicUrl())
                            .or().eq("pic_original", pic.getPicUrl())
                            .or().eq("pic_large", pic.getPicUrl()))) > 0) return true;
        }
        return false;
    }

    private boolean hasProductVideoReference(List<Integer> ids) {
        return productVideos.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.tigshop.bean.model.product.ProductVideo>()
                .in("video_id", ids)) > 0;
    }

    /**
     * MCP is a platform-only surface.  The underlying gallery services expose
     * broad ID-based update/delete methods, so an explicit ownership check is
     * required before invoking them.  A null entity is left to the existing
     * service to classify as not-found; this also keeps the adapter compatible
     * with service implementations that do not expose a detail row.
     */
    private boolean allInPlatformScope(List<Integer> ids, String type) {
        for (Integer id : ids) {
            if (!inPlatformScope(id, type)) return false;
        }
        return true;
    }

    private boolean inPlatformScope(Integer id, String type) {
        if (id == null || id < 1) return false;
        return "video".equals(type) ? inPlatformVideoScope(id) : inPlatformImageScope(id);
    }

    private boolean inPlatformImageScope(Integer id) {
        GalleryPic pic = pics.getById(id);
        return pic == null || tenantIsPlatform(pic.getShopId(), pic.getVendorId());
    }

    private boolean inPlatformVideoScope(Integer id) {
        com.tigshop.bean.model.video.GalleryVideoInfo video = videos.getById(id);
        return video == null || tenantIsPlatform(video.getShopId(), video.getVendorId());
    }

    private boolean inPlatformGroupScope(Integer id, String type) {
        if (id == null || id < 1) return false;
        if ("video".equals(type)) {
            if (videoGroups == null) return false;
            com.tigshop.bean.model.video.GalleryVideo group = videoGroups.getById(id);
            return group == null || tenantIsPlatform(group.getShopId(), group.getVendorId());
        }
        com.tigshop.bean.model.setting.Gallery group = groups.getById(id);
        return group == null || tenantIsPlatform(group.getShopId(), group.getVendorId());
    }

    private static boolean tenantIsPlatform(Integer shopId, Integer vendorId) {
        return (shopId == null || shopId == 0) && (vendorId == null || vendorId == 0);
    }

    private static Integer resolveId(MediaOperationDTO op) {
        if (op.getId() != null) return op.getId();
        List<Integer> ids = resolveIds(op);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private static List<Integer> resolveIds(MediaOperationDTO op) {
        if (op.getIds() != null && !op.getIds().isEmpty()) return op.getIds();
        if (op.getId() != null) return List.of(op.getId());
        if (op.getItems() != null) return op.getItems().stream().map(MediaUploadFileDTO::getId).filter(Objects::nonNull).toList();
        return List.of();
    }

    private static int impactCount(MediaOperationDTO op) {
        if (op.getItems() != null && !op.getItems().isEmpty()) return op.getItems().size();
        if (op.getIds() != null) return op.getIds().size();
        return op.getId() == null ? 0 : 1;
    }

    private static String resolveName(MediaOperationDTO op) {
        if (op.getName() != null && !op.getName().isBlank()) return op.getName().trim();
        if (op.getItems() != null && !op.getItems().isEmpty()) return op.getItems().get(0).getName();
        return null;
    }

    private static String resolveType(MediaOperationDTO op) {
        if (op.getType() != null && !op.getType().isBlank()) return op.getType().trim().toLowerCase(Locale.ROOT);
        if (op.getItems() != null && !op.getItems().isEmpty()) {
            String mime = op.getItems().get(0).getMimeType();
            if (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("video/")) return "video";
            if (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("image/")) return "image";
        }
        return null;
    }
    private static Map<String,Object> mapPic(GalleryPicDTO p){ if(p==null)return Map.of(); Map<String,Object> m=new LinkedHashMap<>(); m.put("id",p.getPicId());m.put("name",p.getPicName());m.put("url",p.getPicUrl());m.put("type","image");m.put("groupId",p.getGalleryId());m.put("format",p.getFileType());m.put("width",p.getPicWidth());m.put("height",p.getPicHeight());m.put("size",p.getPicSize());m.put("createdAt",p.getAddTime()); return m; }
    private static Map<String,Object> mapPic(GalleryPic p){ if(p==null)return Map.of(); GalleryPicDTO d=new GalleryPicDTO(); d.setPicId(p.getPicId());d.setPicName(p.getPicName());d.setPicUrl(p.getPicUrl());d.setGalleryId(p.getGalleryId());d.setFileType(p.getFileType());d.setPicWidth(p.getPicWidth());d.setPicHeight(p.getPicHeight());d.setPicSize(p.getPicSize());d.setAddTime(p.getAddTime()); return mapPic(d); }
    private static Map<String,Object> mapVideo(Object v){ if(v==null)return Map.of(); Map<String,Object> m=new LinkedHashMap<>(); if(v instanceof GalleryVideoInfoDetailVO d){m.put("id",d.getId());m.put("name",d.getVideoName());m.put("url",d.getVideoUrl());m.put("coverUrl",d.getVideoCover());m.put("type","video");m.put("groupId",d.getGalleryId());m.put("format",d.getFormat());m.put("duration",d.getDuration());m.put("size",d.getSize());} else if(v instanceof com.tigshop.bean.model.video.GalleryVideoInfo d){m.put("id",d.getId());m.put("name",d.getVideoName());m.put("url",d.getVideoUrl());m.put("coverUrl",d.getVideoCover());m.put("type","video");m.put("groupId",d.getGalleryId());m.put("format",d.getFormat());m.put("duration",d.getDuration());m.put("size",d.getSize());} return m; }
    private static List<MediaGroupVO> mapGroups(List<GalleryTreeNodeVO> nodes, String type){ if(nodes==null)return List.of(); List<MediaGroupVO> out=new ArrayList<>(); for(var n:nodes){MediaGroupVO g=new MediaGroupVO();g.setId(n.getId()!=null?n.getId():n.getGalleryId());g.setName(n.getName()!=null?n.getName():n.getGalleryName());g.setAssetCount(n.getPicCount()==null?0:n.getPicCount().intValue());g.setType(type);g.setChildren(mapGroups(n.getChildren(),type));out.add(g);} return out; }
    private static String resolveGroupType(MediaOperationDTO op) { return op.getType() == null ? "image" : op.getType().trim().toLowerCase(Locale.ROOT); }
    private static boolean isSupportedType(String type) {
        if (type == null || type.isBlank()) return true;
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        return "image".equals(normalized) || "video".equals(normalized);
    }
    private static Map<String,Object> objectMap(Object o){ if(o==null)return error("SERVICE_UNAVAILABLE"); Map<String,Object> m=new LinkedHashMap<>(); m.put("result",o); return m; }

    private record PreviewRecord(int shopId, String requestId, String fingerprint, long expiresAt) { }
    private record ResultRecord(String fingerprint, Map<String, Object> result, long expiresAt) { }
}
