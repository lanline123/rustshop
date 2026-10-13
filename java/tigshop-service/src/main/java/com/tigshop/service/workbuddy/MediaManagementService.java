package com.tigshop.service.workbuddy;

import com.tigshop.bean.dto.workbuddy.MediaOperationDTO;
import com.tigshop.bean.dto.workbuddy.MediaQueryDTO;
import java.util.Map;

public interface MediaManagementService {
    Map<String,Object> query(MediaQueryDTO query, Integer shopId);
    Map<String,Object> prepare(MediaOperationDTO operation, Integer shopId);
    Map<String,Object> execute(MediaOperationDTO operation, Integer shopId);
}
