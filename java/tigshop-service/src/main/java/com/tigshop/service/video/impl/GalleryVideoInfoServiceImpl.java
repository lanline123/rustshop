package com.tigshop.service.video.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.ConfigStorageDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoDTO;
import com.tigshop.bean.dto.setting.GalleryVideoInfoListDTO;
import com.tigshop.bean.dto.setting.GalleryVideoResDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import com.tigshop.bean.vo.config.GalleryVideoInfoVO;
import com.tigshop.bean.vo.setting.GalleryVideoInfoDetailVO;
import com.tigshop.bean.vo.setting.UploadVideoInfoVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.video.GalleryVideoInfoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.*;
import com.tigshop.service.video.GalleryVideoInfoService;
import com.tigshop.service.video.GalleryVideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.common.constant.ExceptionConstants.*;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * @author wzh
 * @description 针对表【gallery_video_info(视频相册)】的数据库操作Service实现
 * @createDate 2025-04-21 16:45:59
 */
@Slf4j
@Service
public class GalleryVideoInfoServiceImpl extends BaseServiceImpl<GalleryVideoInfoMapper, GalleryVideoInfo>
        implements GalleryVideoInfoService {

    @Resource
    private  GalleryVideoService galleryVideoService;
    @Resource
    private  ConfigService configService;
    @Resource
    private  LocalStorageService localStorageService;
    @Resource
    private  OssService ossService;
    @Resource
    private  CosService cosService;
    @Resource
    private QiniuService qiniuService;
    @Resource
    private AwsS3Service awsS3Service;

    @Value("${file.upload-dir}")
    private  String uploadDir;

    @Override
    public GalleryVideoInfoVO list(GalleryVideoInfoListDTO dto) {
        // 创建分页对象
        Page<GalleryVideoInfo> page = new Page<>(dto.getPage(), dto.getSize());
        // 构建查询条件
        LambdaQueryWrapper<GalleryVideoInfo> queryWrapper = buildGalleryVideoQueryWrapper(dto);
        // 执行分页查询
        Page<GalleryVideoInfo> galleryVideoPage = this.page(page, queryWrapper);
        GalleryVideo galleryVideo = null;

        List<GalleryVideoResDTO> result = new ArrayList<>();
        if (dto.getId() != null && dto.getId() > 0) {
            galleryVideo = galleryVideoService.getById(dto.getId());

            List<GalleryVideo> galleryVideoList = galleryVideoService.lambdaQuery()
                    .eq(GalleryVideo::getParentId, dto.getId())
                    .list();

            result = galleryVideoList.stream()
                    .map(this::convertToGalleryVideoResDTO)
                    .toList();
        }


        return new GalleryVideoInfoVO(result, galleryVideoPage, galleryVideo);
    }

    @Override
    public UploadVideoInfoVO uploadVideo(MultipartFile file) {
        // 校验文件类型和相册参数
        filterVideoType(file);
        //文件后缀名
        String originName = file.getOriginalFilename();
        if (StringUtils.isBlank(originName)) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }

        // 先提取首帧封面：jcodec 仅支持 H.264，遇到 H.265/HEVC 等不支持的编码会在此抛异常。
        // 提前拦截可避免视频已上传到存储后才报错而产生残留文件，并给出可读的提示。
        BufferedImage firstFrame;
        try {
            firstFrame = extractFirstFrame(file.getInputStream());
        } catch (Exception e) {
            log.warn("视频首帧解析失败，可能为不支持的编码格式，originName={}", originName, e);
            throw new GlobalException(VIDEO_CODEC_NOT_SUPPORTED);
        }

        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);
        // 根据「素材上传命名方式」配置解析素材名：displayName=展示名(video_name)，physicalName=物理存储文件基础名
        boolean keepOriginal = "1".equals(configService.getConfigVal(SettingsEnum.MATERIAL_UPLOAD_NAME_TYPE));
        String displayName;
        String physicalName;
        if (keepOriginal) {
            String base = StringUtils.sanitizeFileBaseName(originName);
            displayName = base;
            // 展示名可以保留中文；物理对象键始终使用 ASCII 随机名，避免 OSS 对中文及特殊字符解析不一致。
            physicalName = StringUtils.randomFileName();
        } else {
            String random = StringUtils.getFileName();
            displayName = random;
            physicalName = random;
        }

        String format = originName.substring(originName.lastIndexOf(StrUtil.DOT));
        String filePath = StrUtil.format("video/gallery/{}/{}{}", StringUtils.getCurrentYearMonth(), physicalName, format);
        try {
            String url = "";
            String storageUrl = "";
            // 根据不同的 storageType 执行不同的上传操作
            switch (storageType) {
                case STORAGE_LOCAL -> {
                    url = localStorageService.upload(file, filePath);
                    storageUrl = configStorage.getStorageLocalUrl();
                }
                case STORAGE_OSS -> {
                    url = ossService.uploadFile(file.getInputStream(), filePath);
                    storageUrl = configStorage.getStorageOssUrl();
                }
                case STORAGE_COS -> {
                    url = cosService.uploadFileStream(filePath, file.getInputStream());
                    storageUrl = configStorage.getStorageCosUrl();
                }
                case STORAGE_QINIU -> {
                    url = qiniuService.uploadFileStream(filePath, file.getInputStream());
                    storageUrl = configStorage.getStorageQiniuUrl();
                }
                case STORAGE_S3 -> {
                    url = awsS3Service.uploadFileStream(filePath, file.getInputStream());
                    storageUrl = configStorage.getStorageS3Url();
                }
                default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
            }
            String videoUrl = StrUtil.format("{}{}", storageUrl, url);

            String imgUrl = this.uploadFirstFrameImage(firstFrame, storageType, configStorage);
            String suffix = originName.substring(originName.lastIndexOf(StrUtil.DOT) + 1);
            return new UploadVideoInfoVO(videoUrl, displayName, suffix, imgUrl);
        } catch (IOException e) {
            throw new GlobalException(UPLOAD_FAIL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将已提取的视频首帧封面上传到存储
     */
    public String uploadFirstFrameImage(BufferedImage firstFrame, StorageType storageType, ConfigStorageDTO configStorage) throws Exception {
        // 将图片转换为输入流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(firstFrame, "jpg", os);
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        String fileName = StringUtils.getFileName();
        String filePath = StrUtil.format("img/gallery/{}/{}{}", StringUtils.getCurrentYearMonth(), fileName, ".jpg");
        String url;
        // 根据不同的 storageType 执行不同的上传操作
        switch (storageType) {
            case STORAGE_LOCAL -> {
                url = localStorageService.upload(inputStream, filePath);
            }
            case STORAGE_OSS -> {
                url = ossService.uploadFile(inputStream, filePath);
            }
            case STORAGE_COS -> {
                url = cosService.uploadFileStream(filePath, inputStream);
            }
            case STORAGE_QINIU -> {
                url = qiniuService.uploadFileStream(filePath, inputStream);
            }
            case STORAGE_S3 -> {
                url = awsS3Service.uploadFileStream(filePath, inputStream);
            }
            default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
        }
        // 根据存储类型返回对应的 URL
        String storageUrl = switch (storageType) {
            case STORAGE_LOCAL -> configStorage.getStorageLocalUrl();
            case STORAGE_OSS -> configStorage.getStorageOssUrl();
            case STORAGE_COS -> configStorage.getStorageCosUrl();
            case STORAGE_QINIU -> configStorage.getStorageQiniuUrl();
            case STORAGE_S3 -> configStorage.getStorageS3Url();
        };
        return StrUtil.format("{}{}", storageUrl, url);
    }

    private BufferedImage extractFirstFrame(InputStream inputStream) {
        File tempFile = FileUtil.createTempFile();
        FFmpegFrameGrabber grabber = null;
        try {
            IoUtil.copy(inputStream, FileUtil.getOutputStream(tempFile));
            // 使用 FFmpeg 解码，支持 H.264/H.265 等主流编码格式
            grabber = new FFmpegFrameGrabber(tempFile);
            grabber.start();
            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                Frame frame;
                int attempts = 0;
                // grabImage 只抓视频帧，跳过音频帧；取第一帧有图像数据的画面
                while ((frame = grabber.grabImage()) != null) {
                    BufferedImage image = converter.getBufferedImage(frame);
                    if (image != null) {
                        return image;
                    }
                    if (++attempts > 50) {
                        break;
                    }
                }
            }
            throw new RuntimeException("未能从视频中提取到有效画面帧");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception ex) {
                    log.warn("释放 FFmpegFrameGrabber 失败", ex);
                }
            }
            FileUtil.del(tempFile);
        }
    }

    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = List.of("mp4", "flv", "webm", "avi");
    /** 本地上传仅放开 mp4，兼容性最好；外链视频的可选格式见 ALLOWED_VIDEO_EXTENSIONS */
    private static final List<String> ALLOWED_UPLOAD_VIDEO_EXTENSIONS = List.of("mp4");
    private static final List<String> ALLOWED_UPLOAD_VIDEO_CONTENT_TYPES = List.of("video/mp4", "application/octet-stream");
    private static final List<String> BLOCKED_URL_PATTERNS = List.of(
            "baidu.com/s?", "image.baidu.com/search", "google.com/search",
            "bing.com/images/search", "sogou.com/pics"
    );

    @Override
    public void save(GalleryVideoInfoDTO dto) {
        saveAndReturnId(dto);
    }

    @Override
    public Integer saveAndReturnId(GalleryVideoInfoDTO dto) {
        if ("external".equals(dto.getSource())) {
            validateExternalVideoUrl(dto.getVideoUrl());
        }

        GalleryVideoInfo galleryVideoInfo = new GalleryVideoInfo();
        BeanUtils.copyProperties(dto, galleryVideoInfo);
        galleryVideoInfo.setGalleryId(dto.resolveGalleryId());
        galleryVideoInfo.setAddTime(StringUtils.getCurrentTime());
        galleryVideoInfo.setAddUserId(SecurityUtils.getCurrentAdminId());
        galleryVideoInfo.setShopId(getShopId());
        galleryVideoInfo.setVendorId(getVendorId());
        if (StrUtil.isNotBlank(dto.getSource())) {
            galleryVideoInfo.setSource(dto.getSource());
        }
        this.save(galleryVideoInfo);
        return galleryVideoInfo.getId();
    }

    private void validateExternalVideoUrl(String url) {
        if (StrUtil.isBlank(url)) {
            throw new GlobalException("视频URL不能为空");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new GlobalException("URL必须以http://或https://开头");
        }
        for (String pattern : BLOCKED_URL_PATTERNS) {
            if (url.contains(pattern)) {
                throw new GlobalException("不支持网页搜索结果地址，请提供视频直链");
            }
        }
        String ext = extractExtension(url);
        if (StrUtil.isBlank(ext) || !ALLOWED_VIDEO_EXTENSIONS.contains(ext)) {
            throw new GlobalException("不支持的视频格式，允许的格式: " + String.join(", ", ALLOWED_VIDEO_EXTENSIONS));
        }
    }

    private String extractExtension(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        String path = url;
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            path = url.substring(0, queryIndex);
        }
        int hashIndex = path.indexOf('#');
        if (hashIndex > 0) {
            path = path.substring(0, hashIndex);
        }
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < path.length() - 1) {
            return path.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public void update(GalleryVideoInfoDTO dto) {
        this.lambdaUpdate()
                .eq(GalleryVideoInfo::getId, dto.getId())
                .set(GalleryVideoInfo::getAddTime, StringUtils.getCurrentTime())
                .set(GalleryVideoInfo::getGalleryId, dto.resolveGalleryId())
                .set(GalleryVideoInfo::getVideoUrl, dto.getVideoUrl())
                .set(GalleryVideoInfo::getVideoName, dto.getVideoName())
                .set(GalleryVideoInfo::getVideoCover, dto.getVideoCover())
                .set(GalleryVideoInfo::getDescription, dto.getDescription())
                .set(GalleryVideoInfo::getFormat, dto.getFormat())
                .set(GalleryVideoInfo::getDuration, dto.getDuration())
                .set(GalleryVideoInfo::getSize, dto.getSize())
                .update();
    }

    @Override
    public GalleryVideoInfoDetailVO detail(Integer id) {
        GalleryVideoInfo galleryVideoInfo = this.getById(id);
        if (galleryVideoInfo == null) {
            throw new GlobalException("系统未找到视频信息");
        }
        return BeanUtil.copyProperties(galleryVideoInfo, GalleryVideoInfoDetailVO.class);
    }


    public ConfigStorageDTO buildConfigStorage() {
        List<SettingsEnum> settings = List.of(
                STORAGE_TYPE, STORAGE_LOCAL_URL, STORAGE_OSS_URL,
                STORAGE_OSS_ACCESS_KEY_ID, STORAGE_OSS_ACCESS_KEY_SECRET,
                STORAGE_OSS_BUCKET, STORAGE_OSS_REGION,
                STORAGE_COS_URL, STORAGE_COS_SECRET_ID,
                STORAGE_COS_SECRET_KEY, STORAGE_COS_BUCKET,
                STORAGE_COS_REGION,
                STORAGE_QINIU_URL, STORAGE_QINIU_ACCESS_KEY,
                STORAGE_QINIU_SECRET_KEY, STORAGE_QINIU_BUCKET,
                STORAGE_QINIU_REGION
        );

        Map<String, String> storageConfigMap = configService.getConfigMapBySettings(settings);
        return new ConfigStorageDTO(storageConfigMap);
    }

    public void filterVideoType(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL, SERVICE_DATA_ERROR);
        }

        // 以扩展名为准判定类型
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_UPLOAD_VIDEO_EXTENSIONS.contains(extension)) {
            throw new GlobalException(UPLOAD_FILE_TYPE_ERROR, SERVICE_DATA_ERROR);
        }

        // contentType 仅作辅助：uniapp / 小程序上传常传 application/octet-stream 或不带该头，不能据此拒绝
        String contentType = file.getContentType();
        if (StrUtil.isNotBlank(contentType)
                && !ALLOWED_UPLOAD_VIDEO_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new GlobalException(UPLOAD_FILE_TYPE_ERROR, SERVICE_DATA_ERROR);
        }
    }

    private GalleryVideoResDTO convertToGalleryVideoResDTO(GalleryVideo galleryVideo) {
        GalleryVideoResDTO galleryVideoResDTO = new GalleryVideoResDTO();
        BeanUtils.copyProperties(galleryVideo, galleryVideoResDTO);

        List<GalleryVideoInfo> galleryVideoInfoList = this.lambdaQuery()
                .eq(GalleryVideoInfo::getGalleryId, galleryVideo.getId())
                .last("limit 4")
                .list();
        galleryVideoResDTO.setGalleryVideoInfoList(galleryVideoInfoList);
        return galleryVideoResDTO;
    }

    private LambdaQueryWrapper<GalleryVideoInfo> buildGalleryVideoQueryWrapper(GalleryVideoInfoListDTO dto) {
        LambdaQueryWrapper<GalleryVideoInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideoInfo::getShopId, 0);
            queryWrapper.eq(GalleryVideoInfo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(getShopId() != null, GalleryVideoInfo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(getVendorId() != null, GalleryVideoInfo::getVendorId, getVendorId());
        }

        // id 不传或为 0：全部（含未归组）；>0：该相册及其子孙
        if (dto.getId() != null && dto.getId() > 0) {
            List<Integer> galleryIds = galleryVideoService.getSelfAndChildIds(dto.getId());
            queryWrapper.in(GalleryVideoInfo::getGalleryId, galleryIds);
        }

        queryWrapper.like(StringUtils.isNotBlank(dto.getKeyword()), GalleryVideoInfo::getVideoName, dto.getKeyword());
        queryWrapper.eq(StringUtils.isNotBlank(dto.getFormat()), GalleryVideoInfo::getFormat, dto.getFormat());

        // 来源筛选：external 精确匹配；local 匹配非 external（兼容历史 upload 及空值）
        if (StringUtils.isNotBlank(dto.getSource())) {
            if ("external".equals(dto.getSource())) {
                queryWrapper.eq(GalleryVideoInfo::getSource, "external");
            } else {
                queryWrapper.and(w -> w.ne(GalleryVideoInfo::getSource, "external")
                        .or().isNull(GalleryVideoInfo::getSource));
            }
        }

        queryWrapper.orderByDesc(GalleryVideoInfo::getId);

        return queryWrapper;
    }
}



