package com.tigshop.service.workbuddy;

import com.tigshop.bean.dto.workbuddy.CategoryImportDTO;
import java.util.Map;

/** 分类树导入编排服务。仅在 execute 且明确确认后调用底层分类写入。 */
public interface CategoryImportService {
    Map<String, Object> prepare(CategoryImportDTO request, Integer shopId);
    Map<String, Object> execute(CategoryImportDTO request, Integer shopId);
}
