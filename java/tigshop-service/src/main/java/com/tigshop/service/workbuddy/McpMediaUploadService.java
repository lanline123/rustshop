package com.tigshop.service.workbuddy;

import com.tigshop.bean.dto.workbuddy.MediaUploadRequestDTO;
import com.tigshop.bean.vo.workbuddy.MediaUploadResultVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface McpMediaUploadService {
    MediaUploadResultVO upload(MediaUploadRequestDTO request, Integer shopId);
    Map<String,Object> stage(List<MultipartFile> files);
    MediaUploadResultVO uploadByTokens(MediaUploadRequestDTO request, Integer shopId);
    default MediaUploadResultVO upload(MediaUploadRequestDTO request) { return upload(request, 0); }
}
