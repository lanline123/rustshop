package com.tigshop.bean.vo.workbuddy;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class MediaUploadResultVO {
    private String status;
    private int total;
    private int successCount;
    private int failedCount;
    private List<MediaAssetVO> assets = new ArrayList<>();
    private Map<String, Integer> failureSummary = new LinkedHashMap<>();
    private String summary;
    private boolean retryable;
}
