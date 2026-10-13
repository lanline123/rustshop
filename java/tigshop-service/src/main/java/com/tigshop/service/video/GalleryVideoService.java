package com.tigshop.service.video;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryVideoCreateDTO;
import com.tigshop.bean.dto.setting.GalleryVideoUpdateDTO;
import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.vo.setting.GalleryTreeNodeVO;
import com.tigshop.bean.vo.setting.GalleryVideoListVO;
import com.tigshop.bean.vo.setting.GalleryVideoVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * @author wzh
 */
public interface GalleryVideoService extends BaseService<GalleryVideo> {


    /**
     * 相册列表
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    Page<GalleryVideoVO> list(GalleryListDTO dto);

    /**
     * 树形结构视频列表
     * @return 树形结构视频列表
     */
    List<GalleryVideoListVO> treeList();


    /**
     * 相册详情
     *
     * @param id 相册id
     * @return 相册详情
     */
    GalleryVideoVO detail(Integer id);


    /**
     * 新建相册
     *
     * @param dto 相册信息
     */
    Integer create(GalleryVideoCreateDTO dto);


    /**
     * 修改相册
     * @param dto 相册信息
     */
    Integer update(GalleryVideoUpdateDTO dto);

    /**
     * 获取视频相册自身及其所有子孙相册的 ID 集合
     *
     * @param galleryId 相册 ID
     * @return 包含自身及全部子孙相册的 ID 列表
     */
    List<Integer> getSelfAndChildIds(Integer galleryId);

    List<GalleryTreeNodeVO> tree();
    void sort(GallerySortDTO dto);
    void move(GalleryMoveDTO dto);
    void delWithMode(GalleryDelDTO dto);
}
