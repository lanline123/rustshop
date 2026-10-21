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
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.FileSecurityUtils;
import com.tigshop.service.setting.LocalStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.tigshop.common.constant.ExceptionConstants.*;
import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * 本地存储
 *
 * @author Tigshop团队
 * @create 2025年01月08日 10:50
 */
@Service
public class LocalStorageServiceImpl implements LocalStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file, String filePath) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }

        // 安全验证：检查文件路径是否包含遍历攻击
        if (FileSecurityUtils.containsPathTraversal(filePath)) {
            log.warn("检测到路径遍历攻击尝试: {}", filePath);
            throw new GlobalException(UPLOAD_FILE_PATH_UNSAFE);
        }

        // 安全验证：检查是否为危险文件类型（脚本文件等）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && FileSecurityUtils.isDangerousExtension(originalFilename)) {
            log.warn("检测到禁止上传的文件类型: {}", originalFilename);
            throw new GlobalException(UPLOAD_FILE_FORBIDDEN);
        }
        // 同时检查目标路径的扩展名
        if (FileSecurityUtils.isDangerousExtension(filePath)) {
            log.warn("检测到禁止上传的文件类型(路径): {}", filePath);
            throw new GlobalException(UPLOAD_FILE_FORBIDDEN);
        }

        // 安全验证：验证文件魔数
        String contentType = file.getContentType();
        try {
            // 魔数验证只需文件头，避免整文件读入内存
            byte[] fileHeader;
            try (InputStream headerStream = file.getInputStream()) {
                fileHeader = headerStream.readNBytes(FileSecurityUtils.MAGIC_NUMBER_HEADER_SIZE);
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(fileHeader)) {
                if (!FileSecurityUtils.validateMagicNumber(bais, contentType)) {
                    // 检测实际文件类型
                    bais.reset();
                    String detectedType = FileSecurityUtils.detectFileType(bais);
                    if (detectedType != null && !detectedType.equalsIgnoreCase(contentType)) {
                        log.warn("文件类型伪造检测: 声明类型={}, 实际类型={}, 文件名={}",
                                contentType, detectedType, originalFilename);
                        throw new GlobalException(UPLOAD_FILE_TYPE_FAKE);
                    }
                }
            }
        } catch (IOException e) {
            log.error("文件魔数验证失败", e);
            throw new GlobalException(UPLOAD_FAIL);
        }

        // 清理并标准化路径
        String sanitizedPath = FileSecurityUtils.sanitizePath(filePath);
        if (sanitizedPath.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_PATH_UNSAFE);
        }

        // 处理文件存储
        try {
            Path targetLocation = this.getPath(sanitizedPath);

            try (InputStream inputStream = file.getInputStream()) {
                // 写入文件
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return sanitizedPath;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new GlobalException(UPLOAD_FAIL);
        }
    }

    /**
     * 获取安全的文件存储路径
     * 包含路径遍历攻击的二次防护
     */
    private Path getPath(String filePath) throws IOException {
        // 获取当前 JAR 包所在目录
        String jarDir = getJarDir();
        // 在 JAR 目录下创建 uploadDir 文件夹
        File uploadBaseDir = FileUtil.mkdir(jarDir + File.separator + uploadDir);
        String basePath = uploadBaseDir.getCanonicalPath();

        // 构建目标文件的完整路径
        Path targetLocation = Paths.get(basePath, filePath).normalize();

        // 安全验证：确保最终路径仍在上传目录内（防止路径遍历）
        String targetPath = targetLocation.toFile().getCanonicalPath();
        if (!targetPath.startsWith(basePath)) {
            log.warn("路径遍历攻击检测(二次防护): basePath={}, targetPath={}", basePath, targetPath);
            throw new IOException("非法的文件路径");
        }

        // 创建文件存储目录（如果不存在）
        Files.createDirectories(targetLocation.getParent());
        return targetLocation;
    }

    @Override
    public String upload(InputStream inputStream, String filePath) {
        // 检查输入流是否为空
        if (inputStream == null) {
            throw new GlobalException(UPLOAD_FILE_IS_NULL);
        }

        // 安全验证：检查文件路径是否包含遍历攻击
        if (FileSecurityUtils.containsPathTraversal(filePath)) {
            log.warn("检测到路径遍历攻击尝试: {}", filePath);
            throw new GlobalException(UPLOAD_FILE_PATH_UNSAFE);
        }

        // 安全验证：检查是否为危险文件类型（脚本文件等）
        if (FileSecurityUtils.isDangerousExtension(filePath)) {
            log.warn("检测到禁止上传的文件类型: {}", filePath);
            throw new GlobalException(UPLOAD_FILE_FORBIDDEN);
        }

        // 清理并标准化路径
        String sanitizedPath = FileSecurityUtils.sanitizePath(filePath);
        if (sanitizedPath.isEmpty()) {
            throw new GlobalException(UPLOAD_FILE_PATH_UNSAFE);
        }

        // 处理文件存储
        try {
            // 将输入流读取到内存中，以便进行魔数验证
            byte[] fileBytes = inputStream.readAllBytes();

            // 安全验证：检测文件实际类型
            try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
                String detectedType = FileSecurityUtils.detectFileType(bais);
                // 如果检测到文件类型，验证扩展名与实际类型是否匹配
                if (detectedType != null) {
                    String extension = FileSecurityUtils.getFileExtension(filePath);
                    if (!isTypeMatchExtension(detectedType, extension)) {
                        log.warn("文件类型与扩展名不匹配: 检测类型={}, 扩展名={}", detectedType, extension);
                        // 这里仅记录警告，不阻止上传（因为某些场景下可能是合理的）
                    }
                }
            }

            Path targetLocation = this.getPath(sanitizedPath);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
                // 写入文件
                Files.copy(bais, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return sanitizedPath;
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new GlobalException(UPLOAD_FAIL);
        }
    }

    @Override
    public InputStream download(String filePath) {
        try {
            return Files.newInputStream(getPath(FileSecurityUtils.sanitizePath(filePath)));
        } catch (IOException | RuntimeException exception) {
            throw new GlobalException(UPLOAD_FAIL);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            return Files.deleteIfExists(getPath(FileSecurityUtils.sanitizePath(filePath)));
        } catch (IOException | RuntimeException exception) {
            throw new GlobalException(UPLOAD_FAIL);
        }
    }

    /**
     * 检查文件类型与扩展名是否匹配
     */
    private boolean isTypeMatchExtension(String mimeType, String extension) {
        if (mimeType == null || extension == null) {
            return true;
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg" -> "jpg".equals(extension) || "jpeg".equals(extension);
            case "image/png" -> "png".equals(extension);
            case "image/gif" -> "gif".equals(extension);
            case "image/webp" -> "webp".equals(extension);
            case "image/bmp" -> "bmp".equals(extension);
            case "image/tiff" -> "tiff".equals(extension) || "tif".equals(extension);
            case "application/pdf" -> "pdf".equals(extension);
            case "application/zip" -> "zip".equals(extension) || "docx".equals(extension)
                    || "xlsx".equals(extension) || "pptx".equals(extension);
            default -> true;
        };
    }
}
