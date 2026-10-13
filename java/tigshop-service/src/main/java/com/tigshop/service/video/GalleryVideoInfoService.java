package com.tigshop.service.video;

import com.tigshop.bean.dto.setting.GalleryVideoInfoDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoListDTO;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import com.tigshop.bean.vo.config.GalleryVideoInfoVO;
import com.tigshop.bean.vo.setting.GalleryVideoInfoDetailVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.service.common.BaseService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author wzh
*/
public interface GalleryVideoInfoService extends BaseService<GalleryVideoInfo> {


    /**
     * 查询视频列表
     *
     * @param dto 查询参数
     * @return 视频列表
     */
    GalleryVideoInfoVO list(GalleryVideoInfoListDTO dto);

    /**
     * 上次视频
     *
     * @param file 文件
     * @return 视频上传信息
     */
    UploadVideoInfoVO uploadVideo(MultipartFile file);


    /**
     * 保存视频信息
     * @param dto 视频信息
     */
    void save(GalleryVideoInfoDTO dto);

    Integer saveAndReturnId(GalleryVideoInfoDTO dto);


    /**
     * 修改视频信息
     * @param dto 视频信息
     */
    void update(GalleryVideoInfoDTO dto);

    /**
     * 获取视频信息详情
     * @param id 视频信息id
     * @return 视频信息详情
     */
    GalleryVideoInfoDetailVO detail(Integer id);

}
