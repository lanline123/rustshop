// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.dto.setting.GalleryPicExternalDTO;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import com.tigshop.bean.vo.config.GalleryListResVO;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.setting.GalleryPicMapper;
import com.tigshop.mapper.video.GalleryVideoInfoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.common.constant.Constants.ALLOWED_IMAGE_TYPES;
import static com.tigshop.common.constant.ExceptionConstants.*;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;
import static com.tigshop.common.constant.pic.PicConstants.*;
import static com.tigshop.common.constant.setting.GalleryConstants.GALLERY_ID_IS_NULL;
import static com.tigshop.common.constant.setting.GalleryPicConstants.FILE_NOT_EXIST;
import static com.tigshop.common.constant.setting.GalleryPicConstants.INVALID_FILE_PATH;
import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * 相册图片服务实现
 *
 * @author Jayce
 * @create 2024年11月12日 13:49
 */
@Service
public class GalleryPicServiceImpl extends BaseServiceImpl<GalleryPicMapper, GalleryPic> implements GalleryPicService {
    @Resource
    private GalleryService galleryService;

    @Resource
    private ConfigService configService;

    @Resource
    private OssService ossService;

    @Resource
    private CosService cosService;

    @Resource
    private QiniuService qiniuService;

    @Resource
    private LocalStorageService localStorageService;

    @Resource
    private AwsS3Service awsS3Service;

    @Resource
    private com.tigshop.core.TranslatePackageImpl translatePackage;

    @Resource
    private com.tigshop.common.properties.TigshopProperties tigshopProperties;

    @Resource
    private GalleryVideoInfoMapper galleryVideoInfoMapper;

    /**
     * 获取相册图片列表（分页）
     *
     * @param dto 查询条件和分页信息
     * @return 分页后的相册图片列表
     */
    @Override
    public GalleryListResVO list(GalleryPicListDTO dto) {
        Page<GalleryPic> page = new Page<>(dto.getPage(), dto.getSize());
        String sortOrder = dto.getSortOrder();
        if ("manual".equals(sortOrder)) {
            buildSortOrder(page, "picSort", "asc");
        } else {
            String sortField = StrUtil.isBlank(dto.getSortField()) ? "addTime" : dto.getSortField();
            buildSortOrder(page, sortField, sortOrder);
        }
        // 构建查询条件
        LambdaQueryWrapper<GalleryPic> queryWrapper = buildGalleryPicQueryWrapper(dto);
        // 只显示 hidden 为 0 或 null 的记录
        queryWrapper.and(wrapper -> wrapper.eq(GalleryPic::getHidden, 0).or().isNull(GalleryPic::getHidden));
        // 执行分页查询
        Page<GalleryPic> galleryPicPage = this.page(page, queryWrapper);

        // 转换为 DTO 分页
        List<GalleryPicDTO> dtoRecords = galleryPicPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        Page<GalleryPicDTO> galleryPicDTOPage = PageUtil.convertPage(galleryPicPage, dtoRecords);

        // 获取相册的详情
        GalleryDTO gallery = null;

        // 获取子相册列表
        List<GalleryPicResDTO> result = new ArrayList<>();

        if (dto.getGalleryId() != null && dto.getGalleryId() > 0) {
            List<GalleryDTO> galleryList = galleryService.getGalleryByPid(dto.getGalleryId());
            // 构建结果，包含相册图片数据
            result = galleryList.stream()
                    .map(this::convertToGalleryPicResDTO)
                    .toList();

            gallery = galleryService.detail(dto.getGalleryId());
        }


        // 返回最终地响应结果
        return new GalleryListResVO(result, galleryPicDTOPage, gallery);
    }

    /**
     * 构建 GalleryPic 查询条件
     *
     * @param dto 查询条件
     * @return QueryWrapper
     */
    private LambdaQueryWrapper<GalleryPic> buildGalleryPicQueryWrapper(GalleryPicListDTO dto) {
        LambdaQueryWrapper<GalleryPic> queryWrapper = new LambdaQueryWrapper<>();
        Integer shopId = HeaderUtils.getShopId();
        queryWrapper.eq(shopId != null, GalleryPic::getShopId, shopId);

        Integer vendorId = HeaderUtils.getVendorId();
        queryWrapper.eq(vendorId != null, GalleryPic::getVendorId, vendorId);


        // galleryId 不传或为 0：全部（含未归组）；>0：该相册及其子孙
        if (dto.getGalleryId() != null && dto.getGalleryId() > 0) {
            List<Integer> galleryIds = galleryService.getSelfAndChildIds(dto.getGalleryId());
            queryWrapper.in(GalleryPic::getGalleryId, galleryIds);
        }

        // 关键字搜索
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            queryWrapper.like(GalleryPic::getPicName, dto.getKeyword());
        }

        // 格式筛选
        if (StrUtil.isNotBlank(dto.getFormat())) {
            queryWrapper.eq(GalleryPic::getFileType, dto.getFormat());
        }

        // 来源筛选：external=外部直链，local/upload=本地上传，空值不过滤
        if (StrUtil.isNotBlank(dto.getSource())) {
            String source = dto.getSource().trim();
            if ("external".equalsIgnoreCase(source)) {
                queryWrapper.eq(GalleryPic::getSource, "external");
            } else if ("local".equalsIgnoreCase(source) || "upload".equalsIgnoreCase(source)) {
                queryWrapper.eq(GalleryPic::getSource, "upload");
            }
        }

        // 素材回收站采用显式状态字段；历史记录的 NULL 按正常素材处理。
        return queryWrapper;
    }

    /**
     * 将 GalleryDTO 转换为 GalleryPicResDTO
     *
     * @param galleryDTO GalleryDTO
     * @return GalleryPicResDTO
     */
    private GalleryPicResDTO convertToGalleryPicResDTO(GalleryDTO galleryDTO) {
        GalleryPicResDTO galleryPicResDTO = new GalleryPicResDTO();
        BeanUtils.copyProperties(galleryDTO, galleryPicResDTO);
        // 获取该相册的图片数据
        List<GalleryPicDTO> galleryPics = getPicsByGid(galleryDTO.getGalleryId());
        galleryPicResDTO.setGalleryPics(galleryPics);
        return galleryPicResDTO;
    }

    /**
     * 获取指定 GalleryId 的所有图片
     *
     * @param galleryId 相册 ID
     * @return GalleryPicDTO 列表
     */
    @Override
    public List<GalleryPicDTO> getPicsByGid(Integer galleryId) {
        Page<GalleryPic> page = new Page<>(1, 4);
        LambdaQueryWrapper<GalleryPic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GalleryPic::getGalleryId, galleryId);
        List<GalleryPic> galleryPicList = this.list(page, queryWrapper);
        // 将图片实体列表转换为 DTO 列表
        return galleryPicList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 将 GalleryPic 实体转换为 DTO
     *
     * @param galleryPic GalleryPic 实体
     * @return GalleryPicDTO
     */
    private GalleryPicDTO convertToDTO(GalleryPic galleryPic) {
        GalleryPicDTO dto = new GalleryPicDTO();
        BeanUtils.copyProperties(galleryPic, dto);
        return dto;
    }

    @Override
    public GalleryPicUploadVO uploadImg(Integer galleryId, MultipartFile file) {
        return uploadImg(galleryId, file, false);
    }



    @Override
    public GalleryPicUploadVO uploadImg(Integer galleryId, MultipartFile file,boolean isHidden) {
        // 校验文件类型和相册参数
        filterImageType(file);
        filterImageSize(file);
        if (galleryId == null) {
            throw new GlobalException(GALLERY_ID_IS_NULL);
        }
        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);

        // 根据「素材上传命名方式」配置解析素材名：names[0]=展示名(写入 pic_name)，names[1]=物理存储文件基础名
        String[] names = resolveMaterialNames(file.getOriginalFilename());
        String displayName = names[0];
        String fileName = names[1];
        String filePath = StrUtil.format("img/gallery/{}/{}.jpeg", StringUtils.getCurrentYearMonth(), fileName);
        try {
            // 根据不同的 storageType 执行不同的上传操作
            switch (storageType) {
                case STORAGE_LOCAL -> {
                    String url = localStorageService.upload(file, filePath);
                    GalleryPic pic = createPic(url, configStorage.getStorageLocalUrl(), galleryId, displayName, isHidden, file);
                    return buildResult(pic);
                }
                case STORAGE_OSS -> {
                    String url = ossService.uploadFile(file.getInputStream(), filePath);
                    GalleryPic pic = createPic(url, configStorage.getStorageOssUrl(), galleryId, displayName, isHidden, file);
                    return buildResult(pic);
                }
                case STORAGE_COS -> {
                    String url = cosService.uploadFileStream(filePath, file.getInputStream());
                    GalleryPic pic = createPic(url, configStorage.getStorageCosUrl(), galleryId, displayName, isHidden, file);
                    return buildResult(pic);
                }
                case STORAGE_QINIU -> {
                    String url = qiniuService.uploadFileStream(filePath, file.getInputStream());
                    GalleryPic pic = createPic(url, configStorage.getStorageQiniuUrl(), galleryId, displayName, isHidden, file);
                    return buildResult(pic);
                }
                case STORAGE_S3 -> {
                    String url = awsS3Service.uploadFileStream(filePath, file.getInputStream());
                    GalleryPic pic = createPic(url, configStorage.getStorageS3Url(), galleryId, displayName, isHidden, file);
                    return buildResult(pic);
                }
                default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
            }
        } catch (IOException e) {
            throw new GlobalException(UPLOAD_FAIL);
        }
    }


    @Override
    public String uploadImg(MultipartFile file) {
        filterImageSize(file);
        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);

        // 根据「素材上传命名方式」配置解析物理存储文件名（该重载不落库，仅返回访问地址）
        String fileName = resolveMaterialNames(file.getOriginalFilename())[1];
        String filePath = StrUtil.format("img/gallery/{}/{}.jpeg", StringUtils.getCurrentYearMonth(), fileName);
        try {
            // 根据不同的 storageType 执行不同的上传操作
            switch (storageType) {
                case STORAGE_LOCAL -> {
                    String url = localStorageService.upload(file, filePath);
                    // 本地暂时没有做尺寸功能
                    return StrUtil.format("{}{}", configStorage.getStorageLocalUrl(), url);
                }
                case STORAGE_OSS -> {
                    String url = ossService.uploadFile(file.getInputStream(), filePath);
                    return StrUtil.format("{}{}", configStorage.getStorageOssUrl(), url);
                }
                case STORAGE_COS -> {
                    String url = cosService.uploadFileStream(filePath, file.getInputStream());
                    return StrUtil.format("{}{}", configStorage.getStorageCosUrl(), url);
                }
                case STORAGE_QINIU -> {
                    String url = qiniuService.uploadFileStream(filePath, file.getInputStream());
                    return StrUtil.format("{}{}", configStorage.getStorageQiniuUrl(), url);
                }
                case STORAGE_S3 -> {
                    String url = awsS3Service.uploadFileStream(filePath, file.getInputStream());
                    return StrUtil.format("{}{}", configStorage.getStorageS3Url(), url);
                }
                default -> throw new GlobalException(UPLOAD_FILE_STORE_ERROR);
            }
        } catch (IOException e) {
            throw new GlobalException(UPLOAD_FAIL);
        }
    }

    /**
     * 根据「素材上传命名方式」配置解析素材名。
     * <p>0-系统随机命名（默认）：展示名与物理文件名均使用系统随机名。
     * <p>1-保留原素材名：展示名使用清洗后的原始文件名，物理文件名始终使用随机 ASCII 名称，避免对象存储对中文及特殊字符的兼容问题。
     *
     * @param originalFilename 上传文件的原始文件名
     * @return 长度为 2 的数组：[0]=展示名，[1]=物理存储文件基础名
     */
    private String[] resolveMaterialNames(String originalFilename) {
        boolean keepOriginal = "1".equals(configService.getConfigVal(SettingsEnum.MATERIAL_UPLOAD_NAME_TYPE));
        if (keepOriginal) {
            String base = StringUtils.sanitizeFileBaseName(originalFilename);
            // 展示名可以保留中文；物理对象键必须保持 ASCII，避免 OSS 对中文、+、%、#、& 等字符编码不一致。
            String physical = StringUtils.randomFileName();
            return new String[]{base, physical};
        }
        String random = StringUtils.randomFileName();
        return new String[]{random, random};
    }

    /**
     * 校验文件类型
     *
     * @param file 文件
     */
    public void filterImageType(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL, SERVICE_DATA_ERROR);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new GlobalException(UPLOAD_FILE_TYPE_ERROR, SERVICE_DATA_ERROR);
        }

    }

    private void filterImageSize(MultipartFile file) {
        BigDecimal maxSizeMb = resolveImageUploadMaxSize();
        BigDecimal maxSizeBytes = maxSizeMb.multiply(BigDecimal.valueOf(1024L * 1024L));
        if (BigDecimal.valueOf(file.getSize()).compareTo(maxSizeBytes) > 0) {
            throw new GlobalException(
                    "图片大小不能超过" + maxSizeMb.stripTrailingZeros().toPlainString() + "MB",
                    SERVICE_DATA_ERROR);
        }
    }

    private BigDecimal resolveImageUploadMaxSize() {
        String configuredValue = configService.getConfigVal(SettingsEnum.IMAGE_UPLOAD_MAX_SIZE);
        try {
            BigDecimal value = new BigDecimal(configuredValue);
            if (value.signum() > 0) {
                return value;
            }
        } catch (Exception ignored) {
            // 配置缺失或异常时使用枚举默认值。
        }
        return new BigDecimal(SettingsEnum.IMAGE_UPLOAD_MAX_SIZE.getDefaultValue());
    }

    /**
     * 构建配置存储对象
     *
     * @return ConfigStorageDTO
     */
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
                STORAGE_QINIU_REGION,
                STORAGE_S3_URL, STORAGE_S3_ACCESS_KEY,
                STORAGE_S3_SECRET_KEY, STORAGE_S3_BUCKET,
                STORAGE_S3_REGION, STORAGE_S3_ENDPOINT
        );

        Map<String, String> storageConfigMap = configService.getConfigMapBySettings(settings);
        return new ConfigStorageDTO(storageConfigMap);
    }

    /**
     * 保存图片
     *
     * @param url        图片地址
     * @param storageUrl 存储地址
     * @param galleryId  相册ID
     * @param fileName   文件名
     * @return Boolean
     */
    public GalleryPic createPic(String url, String storageUrl, Integer galleryId, String fileName, boolean isHidden) {
        return createPic(url, storageUrl, galleryId, fileName, isHidden, null);
    }

    public GalleryPic createPic(String url, String storageUrl, Integer galleryId, String fileName, boolean isHidden, MultipartFile sourceFile) {
        url = StrUtil.format("{}{}", storageUrl, url);

        GalleryPic galleryPic = new GalleryPic();
        galleryPic.setGalleryId(galleryId);
        galleryPic.setPicUrl(url);
        galleryPic.setPicName(fileName);
        galleryPic.setPicThumb(StrUtil.format("{}{}", url, getImageSize().get("size_200")));
        galleryPic.setAddTime(String.valueOf(StringUtils.getCurrentTime()));
        galleryPic.setPicOwerId(SecurityUtils.getCurrentAdminId());
        galleryPic.setShopId(getShopId());
        Integer vendorId = HeaderUtils.getVendorId();
        galleryPic.setVendorId(vendorId);
        galleryPic.setHidden(isHidden ? 1 : 0);

        if (sourceFile != null) {
            galleryPic.setPicSize(sourceFile.getSize());
            String originalName = sourceFile.getOriginalFilename();
            if (StrUtil.isNotBlank(originalName) && originalName.contains(".")) {
                String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
                galleryPic.setFileType(ext);
            }
            // 只解析图片头获取尺寸，避免整图解码占用大量堆内存
            // 显式用内存缓存流，避免 ImageIO 在临时目录生成缓存文件，也不改动线程上下文的全局 useCache 开关
            try (InputStream sourceStream = sourceFile.getInputStream();
                 javax.imageio.stream.ImageInputStream imageStream = new javax.imageio.stream.MemoryCacheImageInputStream(sourceStream)) {
                java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(imageStream);
                if (readers.hasNext()) {
                    javax.imageio.ImageReader reader = readers.next();
                    try {
                        reader.setInput(imageStream, true, true);
                        galleryPic.setPicWidth(reader.getWidth(0));
                        galleryPic.setPicHeight(reader.getHeight(0));
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (Throwable e) {
                // 宽高属于附属元数据，解析失败（含 ImageIO 类初始化失败等 Error）不应影响上传本身
                log.warn("读取图片尺寸失败，跳过宽高解析：" + e);
            }
        }

        boolean isCreated = this.save(galleryPic);
        if (isCreated) {
            return galleryPic;
        }

        throw new GlobalException(UPLOAD_FAIL);
    }

    /**
     * 构建上传图片结果
     *
     * @param galleryPic 图片实体
     * @return GalleryPicUploadVO
     */
    public GalleryPicUploadVO buildResult(GalleryPic galleryPic) {
        GalleryPicUploadVO vo = new GalleryPicUploadVO(galleryPic.getPicUrl(), galleryPic.getPicName(), galleryPic.getPicId(), galleryPic.getPicThumb());
        vo.setPicWidth(galleryPic.getPicWidth());
        vo.setPicHeight(galleryPic.getPicHeight());
        vo.setPicSize(galleryPic.getPicSize());
        vo.setGalleryId(galleryPic.getGalleryId());
        vo.setFileType(galleryPic.getFileType());
        return vo;
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> selectFile(String timestamp, String filename, String fileType) {
        // 获取当前 JAR 包所在目录
        String jarDir = getJarDir();
        // 在 JAR 目录下创建 uploadDir 文件夹
        File pemDir = FileUtil.mkdir(jarDir + File.separator + uploadDir);
        String absolutePath = pemDir.getAbsolutePath();
        // 获取文件存储的根目录
        String uploadPath = "";
        if (fileType.equals("app")) {
            uploadPath = StrUtil.format("{}/{}/update/{}", absolutePath, fileType, timestamp);
        } else {
            uploadPath = StrUtil.format("{}/{}/gallery/{}", absolutePath, fileType, timestamp);
        }

        // 创建文件路径
        String filePath = Paths.get(uploadPath, filename).toString();

        // 创建资源
        try {
            UrlResource urlResource = new UrlResource(Paths.get(filePath).toUri());
            // 检查文件是否存在
            if (!urlResource.exists()) {
                throw new GlobalException(FILE_NOT_EXIST);
            }

            // 获取文件的 MIME 类型
            String contentType = URLConnection.guessContentTypeFromName(filename);
            contentType = contentType == null ? "application/octet-stream" : contentType;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(urlResource);
        } catch (MalformedURLException e) {
            log.error(StrUtil.format("{}{}", INVALID_FILE_PATH, filePath));
        }
        return ResponseEntity.notFound().build();
    }


    @Override
    public Map<String, String> getImageSize() {
        // 获取存储对象配置
        ConfigStorageDTO configStorage = buildConfigStorage();
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeCode = configStorage.getStorageType().toString();
        // 获取存储类型对应的枚举类型
        StorageType storageType = StorageType.fromTypeCode(storageTypeCode);
        // 根据不同的 storageType 执行不同的上传操作
        Map<String, String> map = new HashMap<>();
        switch (storageType) {
            case STORAGE_LOCAL -> {
                map.put("size_200", "");
                map.put("size_400", "");
                map.put("size_800", "");
                return map;
            }
            case STORAGE_COS -> {
                map.put("size_200", COS_SIZE_200);
                map.put("size_400", COS_SIZE_400);
                map.put("size_800", COS_SIZE_800);
                return map;
            }
            case STORAGE_QINIU -> {
                // 七牛云图片处理参数，可以根据需要配置
                map.put("size_200", "?imageView2/2/w/200");
                map.put("size_400", "?imageView2/2/w/400");
                map.put("size_800", "?imageView2/2/w/800");
                return map;
            }
            default -> {
                // 默认使用阿里云OSS
                map.put("size_200", OSS_SIZE_200);
                map.put("size_400", OSS_SIZE_400);
                map.put("size_800", OSS_SIZE_800);
                return map;
            }
        }
    }


    /**
     * 根据图片URL获取原图（去除尺寸处理参数）
     *
     * @param imageUrl 图片URL（可能带有尺寸参数）
     * @return 原图URL
     */
    @Override
    public GalleryPic detail(Integer id) {
        if (id == null || id < 1) {
            return null;
        }
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(GalleryPicSortDTO dto) {
        List<Integer> ids = dto.getIds();
        for (int i = 0; i < ids.size(); i++) {
            this.lambdaUpdate()
                    .eq(GalleryPic::getPicId, ids.get(i))
                    .set(GalleryPic::getPicSort, i + 1)
                    .update();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(GalleryPicTransferDTO dto) {
        String type = dto.getType();
        if ("video".equals(type)) {
            for (Integer id : dto.getIds()) {
                galleryVideoInfoMapper.update(null,
                        new LambdaUpdateWrapper<GalleryVideoInfo>()
                                .eq(GalleryVideoInfo::getId, id)
                                .set(GalleryVideoInfo::getGalleryId, dto.getTargetGalleryId()));
            }
        } else {
            for (Integer id : dto.getIds()) {
                this.lambdaUpdate()
                        .eq(GalleryPic::getPicId, id)
                        .set(GalleryPic::getGalleryId, dto.getTargetGalleryId())
                        .update();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDel(GalleryPicBatchDTO dto) {
        String type = dto.getType();
        if ("video".equals(type)) {
            galleryVideoInfoMapper.deleteByIds(dto.getIds());
        } else {
            this.removeByIds(dto.getIds());
        }
    }

    @Override
    public void batchDownload(GalleryPicBatchDTO dto, HttpServletResponse response) {
        String type = dto.getType();
        List<String> urls = new ArrayList<>();
        List<String> names = new ArrayList<>();

        if ("video".equals(type)) {
            List<GalleryVideoInfo> videos = galleryVideoInfoMapper.selectBatchIds(dto.getIds());
            for (GalleryVideoInfo v : videos) {
                if (StrUtil.isNotBlank(v.getVideoUrl())) {
                    urls.add(v.getVideoUrl());
                    names.add(StrUtil.isNotBlank(v.getVideoName()) ? v.getVideoName() : "video_" + v.getId());
                }
            }
        } else {
            List<GalleryPic> pics = this.listByIds(dto.getIds());
            for (GalleryPic p : pics) {
                String url = p.getPicUrl();
                if (StrUtil.isNotBlank(url)) {
                    urls.add(url);
                    names.add(StrUtil.isNotBlank(p.getPicName()) ? p.getPicName() : "image_" + p.getPicId());
                }
            }
        }

        if (urls.isEmpty()) {
            throw new GlobalException("没有可下载的素材");
        }

        String zipName = "video".equals(type) ? "视频素材.zip" : "图片素材.zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (int i = 0; i < urls.size(); i++) {
                String fileUrl = urls.get(i);
                String fileName = names.get(i);
                if (!fileName.contains(".")) {
                    String ext = fileUrl.substring(fileUrl.lastIndexOf('.'));
                    if (ext.contains("?")) {
                        ext = ext.substring(0, ext.indexOf('?'));
                    }
                    fileName = fileName + ext;
                }
                try {
                    URL url = new URL(fileUrl);
                    try (InputStream is = url.openStream()) {
                        zos.putNextEntry(new ZipEntry(fileName));
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    }
                } catch (Exception e) {
                    log.warn("下载素材失败: " + fileUrl);
                }
            }
            zos.finish();
        } catch (IOException e) {
            throw new GlobalException("批量下载失败");
        }
    }

    @Override
    public String getOriginalImage(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return "";
        }
        // 根据URL特征判断存储类型并去除对应的尺寸参数
        if (imageUrl.contains("?x-oss-process=")) {
            // 阿里云OSS
            return imageUrl.substring(0, imageUrl.indexOf("?x-oss-process="));
        } else if (imageUrl.contains("?imageMogr2/")) {
            // 腾讯云COS
            return imageUrl.substring(0, imageUrl.indexOf("?imageMogr2/"));
        } else if (imageUrl.contains("?imageView2/")) {
            // 七牛云
            return imageUrl.substring(0, imageUrl.indexOf("?imageView2/"));
        }else if(imageUrl.contains("!!pic")){
            // 处理 !!pic 格式：xxxx!!pic400x400.xxx -> xxxx!!pic.xxx
            int picIndex = imageUrl.indexOf("!!pic");
            if (picIndex != -1) {
                int dotIndex = imageUrl.lastIndexOf(".");
                if (dotIndex != -1) {
                    String prefix = imageUrl.substring(0, picIndex);
                    String extension = imageUrl.substring(dotIndex);
                    return prefix + "!!pic" + extension;
                }
            }

        }
        //还有一种格式 https://oss.tigshop.com/img/item/demo/16820459267BbteufW57MnRkZwCo!!pic.jpeg
        // 本地存储、S3 或无尺寸参数，直接返回原URL
        return imageUrl;
    }

    @Override
    public String translateProductPic(String picUrl, Integer dataType) {
        if (tigshopProperties.getIsOverseas() != 1 || StrUtil.isBlank(picUrl)) {
            return picUrl;
        }
        int type = (dataType == null) ? 7 : dataType;
        String originalImage = getOriginalImage(picUrl);
        if (StrUtil.isBlank(originalImage)) {
            return picUrl;
        }
        String translate = translatePackage.translate(originalImage, type);
        if (StrUtil.isBlank(translate)) {
            return picUrl;
        }
        if (!java.util.Objects.equals(originalImage, translate)) {
            // 命中翻译：把尺寸后缀拼回翻译图地址
            if (picUrl.startsWith(originalImage)) {
                return translate + picUrl.substring(originalImage.length());
            }
            return translate;
        }
        // !!pic 兼容：xxx!!pic.xxx 转 xxx!!pic400x400.xxx 再查一次
        if (originalImage.contains("!!pic")) {
            String picPrefix = originalImage.substring(0, originalImage.indexOf("!!pic") + 5);
            String picSuffix = originalImage.substring(originalImage.indexOf("!!pic") + 5);
            String translatedPic = picPrefix + "400x400" + picSuffix;
            String picTranslate = translatePackage.translate(translatedPic, type);
            if (!java.util.Objects.equals(translatedPic, picTranslate)) {
                return picTranslate;
            }
        }
        // 缩略图即原图：直接拿整串再查一次
        String fullTranslate = translatePackage.translate(picUrl, type);
        if (!java.util.Objects.equals(picUrl, fullTranslate)) {
            return fullTranslate;
        }
        return picUrl;
    }

    @Override
    public GalleryPicUploadVO createFromExternal(GalleryPicExternalDTO dto) {
        String picUrl = dto.getPicUrl();
        validateExternalUrl(picUrl, ALLOWED_IMAGE_EXTENSIONS);

        GalleryPic galleryPic = new GalleryPic();
        galleryPic.setPicUrl(picUrl);
        galleryPic.setPicThumb(StrUtil.isNotBlank(dto.getPicThumb()) ? dto.getPicThumb() : picUrl);
        galleryPic.setPicName(StrUtil.isNotBlank(dto.getPicName()) ? dto.getPicName() : "外部图片");
        galleryPic.setGalleryId(dto.getGalleryId());
        galleryPic.setSource("external");
        galleryPic.setAddTime(String.valueOf(StringUtils.getCurrentTime()));
        galleryPic.setPicOwerId(SecurityUtils.getCurrentAdminId());
        galleryPic.setShopId(getShopId());
        galleryPic.setVendorId(HeaderUtils.getVendorId());
        galleryPic.setHidden(0);

        String ext = extractExtension(picUrl);
        if (StrUtil.isNotBlank(ext)) {
            galleryPic.setFileType(ext);
        }

        boolean created = this.save(galleryPic);
        if (!created) {
            throw new GlobalException("外部图片创建失败");
        }
        return buildResult(galleryPic);
    }

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("bmp", "jpg", "jpeg", "png", "gif");
    private static final List<String> BLOCKED_URL_PATTERNS = List.of(
            "baidu.com/s?", "image.baidu.com/search", "google.com/search",
            "bing.com/images/search", "sogou.com/pics"
    );

    private void validateExternalUrl(String url, List<String> allowedExtensions) {
        if (StrUtil.isBlank(url)) {
            throw new GlobalException("URL不能为空");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new GlobalException("URL必须以http://或https://开头");
        }
        for (String pattern : BLOCKED_URL_PATTERNS) {
            if (url.contains(pattern)) {
                throw new GlobalException("不支持网页搜索结果地址，请提供图片/视频直链");
            }
        }
        String ext = extractExtension(url);
        if (StrUtil.isBlank(ext) || !allowedExtensions.contains(ext)) {
            throw new GlobalException("不支持的文件格式，允许的格式: " + String.join(", ", allowedExtensions));
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
}
