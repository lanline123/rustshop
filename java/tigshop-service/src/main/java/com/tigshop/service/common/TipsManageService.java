package com.tigshop.service.common;

import com.tigshop.bean.vo.common.TipsVO;

import java.util.List;

/**
 * 提示管理接口
 *
 * @author kidd
 * @since 2025/6/19 13:44
 */
public interface TipsManageService {

    /**
     * 获取提示列表
     */
    List<TipsVO> list(String url);

}
