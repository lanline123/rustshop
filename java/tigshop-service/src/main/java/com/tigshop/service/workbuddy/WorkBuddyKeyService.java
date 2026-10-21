package com.tigshop.service.workbuddy;

import com.tigshop.bean.vo.workbuddy.WorkBuddyKeySettingsVO;
import com.tigshop.bean.vo.workbuddy.WorkBuddyKeyStatusVO;

public interface WorkBuddyKeyService {
    WorkBuddyKeyStatusVO requireValid(String key, Integer shopId, String scope);
    WorkBuddyKeyStatusVO status(String key, Integer shopId);
    String generateKey();
    WorkBuddyKeySettingsVO settings();
    String revealKey();

}
