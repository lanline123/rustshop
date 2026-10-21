// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件安全验证工具类
 * <p>
 * 提供文件上传安全验证功能：
 * 1. 文件魔数验证 - 防止文件类型伪造
 * 2. 路径遍历检测 - 防止目录遍历攻击
 * 3. 危险文件类型过滤 - 禁止上传脚本等危险文件
 *
 * @author Tigshop团队
 * @create 2025年12月29日
 */
public class FileSecurityUtils {

    private FileSecurityUtils() {
        // 工具类禁止实例化
    }

    /**
     * 魔数识别所需的文件头长度，需不小于 {@link #FILE_MAGIC_NUMBERS} 中最长的魔数。
     * 调用方若只截取文件头做校验，必须按此长度读取。
     */
    public static final int MAGIC_NUMBER_HEADER_SIZE = 16;

    /**
     * 常见文件类型的魔数定义
     * Key: 文件类型名称
     * Value: 魔数字节数组
     */
    private static final Map<String, byte[]> FILE_MAGIC_NUMBERS = new LinkedHashMap<>();

    static {
        // 图片文件
        FILE_MAGIC_NUMBERS.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        FILE_MAGIC_NUMBERS.put("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        FILE_MAGIC_NUMBERS.put("image/gif", new byte[]{0x47, 0x49, 0x46, 0x38}); // GIF87a 或 GIF89a
        FILE_MAGIC_NUMBERS.put("image/webp", new byte[]{0x52, 0x49, 0x46, 0x46}); // RIFF....WEBP
        FILE_MAGIC_NUMBERS.put("image/bmp", new byte[]{0x42, 0x4D}); // BM
        FILE_MAGIC_NUMBERS.put("image/tiff", new byte[]{0x49, 0x49, 0x2A, 0x00}); // Little-endian TIFF
        FILE_MAGIC_NUMBERS.put("image/tiff-be", new byte[]{0x4D, 0x4D, 0x00, 0x2A}); // Big-endian TIFF

        // PDF文件
        FILE_MAGIC_NUMBERS.put("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF

        // ZIP文件（包括docx, xlsx, pptx等Office文档）
        FILE_MAGIC_NUMBERS.put("application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04});

        // 视频文件
        FILE_MAGIC_NUMBERS.put("video/mp4", new byte[]{0x00, 0x00, 0x00}); // ftyp在第4-7字节
        FILE_MAGIC_NUMBERS.put("video/avi", new byte[]{0x52, 0x49, 0x46, 0x46}); // RIFF....AVI
    }

    /**
     * 禁止上传的危险文件扩展名（脚本、可执行文件等）
     */
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
            // 服务端脚本
            "php", "php3", "php4", "php5", "phtml", "phar",
            "jsp", "jspx", "jspa", "jsw", "jsv", "jspf",
            "asp", "aspx", "asa", "asax", "ascx", "ashx", "asmx",
            "cfm", "cfml", "cfc",
            "pl", "cgi",
            // Shell脚本
            "sh", "bash", "zsh", "ksh", "csh",
            "bat", "cmd", "ps1", "psm1", "psd1",
            // 可执行文件
            "exe", "dll", "so", "dylib",
            "com", "msi", "app", "dmg", "deb", "rpm",
            "jar", "war", "ear",
            "class",
            // 脚本语言
            "py", "pyc", "pyo", "pyw",
            "rb", "erb",
            "js", "mjs", "cjs",
            "vbs", "vbe", "wsf", "wsh",
            // 其他危险文件
            "htaccess", "htpasswd",
            "ini", "conf", "config",
            "sql",
            "svg", // SVG可包含脚本
            "html", "htm", "xhtml", "shtml",
            "xml", "xsl", "xslt"
    ));

    /**
     * 允许上传的安全文件扩展名（白名单）
     */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            // 图片
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff", "tif", "ico",
            // 文档
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv",
            // 视频
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v", "3gp",
            // 音频
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a",
            // 压缩包
            "zip", "rar", "7z", "tar", "gz"
    ));

    /**
     * 路径遍历检测的危险模式
     */
    private static final List<String> PATH_TRAVERSAL_PATTERNS = Arrays.asList(
            "..", // 上级目录
            "./", // 当前目录
            ".\\", // Windows当前目录
            "..\\", // Windows上级目录
            "%2e%2e", // URL编码的..
            "%2e%2e%2f", // URL编码的../
            "%2e%2e/", // 部分编码
            "..%2f", // 部分编码
            "%2e%2e%5c", // URL编码的..\
            "..%5c", // 部分编码
            "%252e%252e", // 双重URL编码
            "....//", // 绕过尝试
            "....\\\\", // 绕过尝试
            "%00", // 空字节注入
            "\0" // 空字符
    );

    /**
     * 验证文件魔数是否与声明的文件类型匹配
     *
     * @param inputStream 文件输入流（需支持mark/reset）
     * @param contentType 声明的Content-Type
     * @return 验证结果
     */
    public static boolean validateMagicNumber(InputStream inputStream, String contentType) {
        if (inputStream == null || contentType == null) {
            return false;
        }

        try {
            // 读取文件头部字节
            byte[] header = new byte[MAGIC_NUMBER_HEADER_SIZE];
            inputStream.mark(MAGIC_NUMBER_HEADER_SIZE);
            int bytesRead = inputStream.read(header);
            inputStream.reset();

            if (bytesRead < 2) {
                return false;
            }

            // 根据Content-Type获取对应的魔数
            byte[] expectedMagic = FILE_MAGIC_NUMBERS.get(contentType.toLowerCase());

            // 特殊处理TIFF（有两种字节序）
            if ("image/tiff".equalsIgnoreCase(contentType)) {
                return matchesMagicNumber(header, FILE_MAGIC_NUMBERS.get("image/tiff"))
                        || matchesMagicNumber(header, FILE_MAGIC_NUMBERS.get("image/tiff-be"));
            }

            // 如果没有定义该类型的魔数，跳过魔数验证（但仍需通过其他验证）
            if (expectedMagic == null) {
                return true;
            }

            return matchesMagicNumber(header, expectedMagic);

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 通过魔数检测文件的真实类型
     *
     * @param inputStream 文件输入流（需支持mark/reset）
     * @return 检测到的文件类型，未知类型返回null
     */
    public static String detectFileType(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        try {
            byte[] header = new byte[MAGIC_NUMBER_HEADER_SIZE];
            inputStream.mark(MAGIC_NUMBER_HEADER_SIZE);
            int bytesRead = inputStream.read(header);
            inputStream.reset();

            if (bytesRead < 2) {
                return null;
            }

            // 遍历所有已知魔数进行匹配
            for (Map.Entry<String, byte[]> entry : FILE_MAGIC_NUMBERS.entrySet()) {
                if (matchesMagicNumber(header, entry.getValue())) {
                    String type = entry.getKey();
                    // 合并TIFF类型
                    if ("image/tiff-be".equals(type)) {
                        return "image/tiff";
                    }
                    return type;
                }
            }

            return null;

        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 检查字节数组是否匹配魔数
     */
    private static boolean matchesMagicNumber(byte[] header, byte[] magic) {
        if (header.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (header[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检测路径是否包含遍历攻击模式
     *
     * @param path 待检测的路径
     * @return true=包含危险模式，false=安全
     */
    public static boolean containsPathTraversal(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // 转换为小写进行检测
        String lowerPath = path.toLowerCase();

        for (String pattern : PATH_TRAVERSAL_PATTERNS) {
            if (lowerPath.contains(pattern.toLowerCase())) {
                return true;
            }
        }

        // 检测绝对路径
        if (path.startsWith("/") || path.startsWith("\\")) {
            return true;
        }

        // 检测Windows盘符路径 (如 C:, D:)
        if (path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            return true;
        }

        return false;
    }

    /**
     * 清理并标准化文件路径
     * 移除危险字符，只保留文件名和安全的目录结构
     *
     * @param path 原始路径
     * @return 清理后的安全路径
     */
    public static String sanitizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // 移除空字节
        path = path.replace("\0", "").replace("%00", "");

        // 统一路径分隔符
        path = path.replace("\\", "/");

        // 移除连续的斜杠
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        // 移除开头的斜杠
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        // 分割路径并过滤危险部分
        String[] parts = path.split("/");
        StringBuilder safePath = new StringBuilder();

        for (String part : parts) {
            // 跳过空部分和危险部分
            if (part.isEmpty() || ".".equals(part) || "..".equals(part)) {
                continue;
            }
            // 移除部分中的危险字符
            String safePart = sanitizeFileName(part);
            if (!safePart.isEmpty()) {
                if (safePath.length() > 0) {
                    safePath.append("/");
                }
                safePath.append(safePart);
            }
        }

        return safePath.toString();
    }

    /**
     * 清理文件名，移除危险字符
     *
     * @param fileName 原始文件名
     * @return 安全的文件名
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // 移除空字节和控制字符
        fileName = fileName.replaceAll("[\\x00-\\x1f\\x7f]", "");

        // 移除路径分隔符
        fileName = fileName.replaceAll("[/\\\\]", "");

        // 移除其他危险字符（保留字母、数字、中文、点、下划线、连字符）
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]", "");

        // 防止文件名以点开头（隐藏文件）
        while (fileName.startsWith(".")) {
            fileName = fileName.substring(1);
        }

        // 限制文件名长度
        if (fileName.length() > 200) {
            // 保留扩展名
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                String name = fileName.substring(0, dotIndex);
                String ext = fileName.substring(dotIndex);
                if (name.length() > 200 - ext.length()) {
                    name = name.substring(0, 200 - ext.length());
                }
                fileName = name + ext;
            } else {
                fileName = fileName.substring(0, 200);
            }
        }

        return fileName;
    }

    /**
     * 获取文件扩展名（小写）
     *
     * @param fileName 文件名
     * @return 扩展名（不含点），如果没有扩展名返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * 检查文件扩展名是否为危险类型
     *
     * @param fileName 文件名
     * @return true=危险类型，false=安全
     */
    public static boolean isDangerousExtension(String fileName) {
        String ext = getFileExtension(fileName);
        return DANGEROUS_EXTENSIONS.contains(ext.toLowerCase());
    }

    /**
     * 检查文件扩展名是否在白名单中
     *
     * @param fileName 文件名
     * @return true=允许，false=不允许
     */
    public static boolean isAllowedExtension(String fileName) {
        String ext = getFileExtension(fileName);
        return ALLOWED_EXTENSIONS.contains(ext.toLowerCase());
    }

    /**
     * 添加允许的文件扩展名
     *
     * @param extensions 扩展名数组（不含点）
     */
    public static void addAllowedExtensions(String... extensions) {
        for (String ext : extensions) {
            ALLOWED_EXTENSIONS.add(ext.toLowerCase());
        }
    }

    /**
     * 综合文件安全验证
     *
     * @param inputStream   文件输入流
     * @param fileName      文件名
     * @param contentType   Content-Type
     * @param validateMagic 是否验证魔数
     * @return 验证结果对象
     */
    public static FileValidationResult validateFile(InputStream inputStream, String fileName,
                                                    String contentType, boolean validateMagic) {
        FileValidationResult result = new FileValidationResult();

        // 1. 检查文件名是否为空
        if (fileName == null || fileName.trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("文件名不能为空");
            return result;
        }

        // 2. 检查危险扩展名
        if (isDangerousExtension(fileName)) {
            result.setValid(false);
            result.setMessage("不允许上传该类型的文件");
            return result;
        }

        // 3. 验证魔数
        if (validateMagic && inputStream != null && contentType != null) {
            if (!validateMagicNumber(inputStream, contentType)) {
                // 检测实际文件类型
                String detectedType = detectFileType(inputStream);
                if (detectedType != null && !detectedType.equalsIgnoreCase(contentType)) {
                    result.setValid(false);
                    result.setMessage("文件类型与声明不符，可能存在安全风险");
                    result.setDetectedType(detectedType);
                    return result;
                }
            }
        }

        result.setValid(true);
        result.setSanitizedFileName(sanitizeFileName(fileName));
        return result;
    }

    /**
     * 文件验证结果
     */
    public static class FileValidationResult {
        private boolean valid;
        private String message;
        private String sanitizedFileName;
        private String detectedType;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSanitizedFileName() {
            return sanitizedFileName;
        }

        public void setSanitizedFileName(String sanitizedFileName) {
            this.sanitizedFileName = sanitizedFileName;
        }

        public String getDetectedType() {
            return detectedType;
        }

        public void setDetectedType(String detectedType) {
            this.detectedType = detectedType;
        }
    }
}
