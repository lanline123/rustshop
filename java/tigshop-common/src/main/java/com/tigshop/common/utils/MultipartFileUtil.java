package com.tigshop.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author wzh
 */
public class MultipartFileUtil {

    public static class CustomMultipartFile implements MultipartFile {

        private final byte[] fileBytes;
        private final String fileName;
        private final String contentType;

        public CustomMultipartFile(byte[] fileBytes, String fileName, String contentType) {
            this.fileBytes = fileBytes;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return fileBytes == null || fileBytes.length == 0;
        }

        @Override
        public long getSize() {
            return fileBytes.length;
        }

        @Override
        public byte[] getBytes() {
            return fileBytes;
        }

        @Override
        public ByteArrayInputStream getInputStream() {
            return new ByteArrayInputStream(fileBytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            java.nio.file.Files.write(dest.toPath(), fileBytes);
        }
    }


    public static MultipartFile urlToMultipartFile(String url) throws IOException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("请求失败，状态码：" + response.statusCode());
            }

            // 获取文件名（没有就生成随机UUID.jpg）
            String fileName = Paths.get(URI.create(url).getPath()).getFileName() != null
                    ? Paths.get(URI.create(url).getPath()).getFileName().toString()
                    : UUID.randomUUID() + ".jpg";

            // 获取 Content-Type
            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("application/octet-stream");

            byte[] bytes = toByteArray(response.body());

            return new CustomMultipartFile(bytes, fileName, contentType);

        } catch (Exception e) {
            throw new IOException("URL 转 MultipartFile 失败: " + e.getMessage(), e);
        }
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
