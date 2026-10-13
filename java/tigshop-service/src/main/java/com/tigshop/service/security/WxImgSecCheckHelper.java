package com.tigshop.service.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 微信小程序图片内容安全检测（鉴黄鉴违）。
 * <p>
 * 仅用于小程序端头像上传合规校验：当「后台小程序设置-图片鉴黄鉴违」开关开启，且请求头
 * X-Client-Type=miniProgram 时，调用微信 img_sec_check 同步接口做检测。
 * 命中违规内容时拦截（抛出异常）；未配置凭证、接口异常等情况放行并记录日志，避免误伤正常用户。
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/sec-check/security.imgSecCheck.html">微信 imgSecCheck 文档</a>
 */
@Slf4j
@Component
public class WxImgSecCheckHelper {

    /** 微信图片内容安全同步检测接口 */
    private static final String IMG_SEC_CHECK_URL = "https://api.weixin.qq.com/wxa/img_sec_check?access_token=";
    /** 微信图片内容安全：命中违规内容的错误码 */
    private static final int ERRCODE_RISKY = 87014;
    /** 小程序客户端类型标识（对应请求头 X-Client-Type） */
    private static final String CLIENT_TYPE_MINI = "miniProgram";
    /** 微信接口调用超时时间（毫秒） */
    private static final int REQUEST_TIMEOUT_MS = 8000;
    /** 微信素材限制：体积不超过 1MB */
    private static final int MAX_MEDIA_BYTES = 1024 * 1024;
    /** 微信素材限制：宽不超过 750px */
    private static final int MAX_MEDIA_WIDTH = 750;
    /** 微信素材限制：高不超过 1334px */
    private static final int MAX_MEDIA_HEIGHT = 1334;
    /** 压缩送检副本时依次尝试的 JPEG 质量 */
    private static final float[] JPEG_QUALITIES = {0.85F, 0.6F, 0.4F};
    /** 重新编码后的送检副本文件名 */
    private static final String COMPRESSED_MEDIA_NAME = "avatar.jpg";

    @Resource
    private ConfigService configService;
    @Resource
    private WechatOAuthService wechatOAuthService;

    /**
     * 小程序头像上传内容安全校验。
     * <p>
     * 命中违规内容时抛出 {@link GlobalException}；其余情况（非小程序端、开关关闭、
     * 未获取到凭证、接口异常）均放行。
     *
     * @param file 上传的头像文件
     */
    public void checkAvatar(MultipartFile file) {
        // 仅小程序端生效
        String clientType = HeaderUtils.getClientType();
        if (!CLIENT_TYPE_MINI.equals(clientType)) {
            log.error("[imgSecCheck] 跳过：非小程序端，X-Client-Type={}", clientType);
            return;
        }
        // 后台「图片鉴黄鉴违」开关未开启则跳过
        String switchVal = configService.getConfigVal(SettingsEnum.MINI_IMG_SEC_CHECK);
        if (!"1".equals(switchVal)) {
            log.error("[imgSecCheck] 跳过：开关未开启，miniImgSecCheck={}", switchVal);
            return;
        }
        if (file == null || file.isEmpty()) {
            log.error("[imgSecCheck] 跳过：上传文件为空");
            return;
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            log.error("[imgSecCheck] 读取头像文件字节失败，放行", e);
            return;
        }
        String accessToken = wechatOAuthService.getMiniAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            log.error("[imgSecCheck] 跳过：未获取到小程序 access_token，请检查配置 wechatMiniProgramAppId / wechatMiniProgramSecret");
            return;
        }
        try {
            CheckMedia media = buildCheckMedia(bytes, StrUtil.blankToDefault(file.getOriginalFilename(), COMPRESSED_MEDIA_NAME));
            log.error("[imgSecCheck] 开始调用微信检测：fileName={}, size={}B", media.fileName(), media.bytes().length);
            String body = HttpRequest.post(IMG_SEC_CHECK_URL + accessToken)
                    .form("media", media.bytes(), media.fileName())
                    .timeout(REQUEST_TIMEOUT_MS)
                    .execute()
                    .body();
            log.error("[imgSecCheck] 头像内容安全检测返回: {}", body);
            JSONObject obj = JSONUtil.parseObj(body);
            int errcode = obj.getInt("errcode", -1);
            if (errcode == ERRCODE_RISKY) {
                log.error("[imgSecCheck] 命中违规内容，拦截本次头像上传");
                throw new GlobalException("上传的头像图片含有违规信息，请更换后重试");
            }
            if (errcode == 0) {
                log.error("[imgSecCheck] 检测通过，放行");
            } else {
                // 微信未真正完成检测（如 40006 素材超限、限频等），此时等于漏检，单独记录便于排查
                log.error("[imgSecCheck] 微信未完成检测，放行：errcode={}", errcode);
            }
        } catch (GlobalException ge) {
            throw ge;
        } catch (Exception e) {
            // 微信接口异常（网络、超时、限频等）不阻断正常用户上传，放行并记录日志
            log.error("[imgSecCheck] 头像内容安全校验调用异常，放行", e);
        }
    }

    /**
     * 送检副本：提交给微信的字节内容与文件名。
     */
    private record CheckMedia(byte[] bytes, String fileName) {
    }

    /**
     * 生成满足微信素材限制（体积 ≤ 1MB、分辨率 ≤ 750x1334）的送检副本。
     * <p>
     * 超限时微信直接返回 40006 而不做任何检测，等于漏检，因此送检前统一等比缩放并压缩。
     * 副本只用于送检，不参与头像落库，用户实际头像仍是原图。
     * 已满足限制时原样返回，避免无谓的重新编码；无法解码（非图片、格式不支持）时也返回原字节，交由微信判定。
     *
     * @param original 原始图片字节
     * @param fileName 原始文件名
     * @return 送检副本
     */
    private CheckMedia buildCheckMedia(byte[] original, String fileName) {
        try {
            // setUseCache 按线程上下文生效，必须在当前请求线程内设置，否则 ImageIO 会在临时目录生成缓存文件
            ImageIO.setUseCache(false);
            BufferedImage source;
            try (ByteArrayInputStream in = new ByteArrayInputStream(original)) {
                source = ImageIO.read(in);
            }
            if (source == null) {
                log.error("[imgSecCheck] 送检副本：无法解码为图片，按原字节送检：fileName={}", fileName);
                return new CheckMedia(original, fileName);
            }
            int width = source.getWidth();
            int height = source.getHeight();
            if (original.length <= MAX_MEDIA_BYTES && width <= MAX_MEDIA_WIDTH && height <= MAX_MEDIA_HEIGHT) {
                return new CheckMedia(original, fileName);
            }
            BufferedImage scaled = scaleToLimit(source);
            byte[] encoded = null;
            for (float quality : JPEG_QUALITIES) {
                encoded = encodeJpeg(scaled, quality);
                if (encoded == null) {
                    break;
                }
                if (encoded.length <= MAX_MEDIA_BYTES) {
                    log.error("[imgSecCheck] 送检副本已压缩：{}x{} {}B -> {}x{} {}B, quality={}",
                            width, height, original.length, scaled.getWidth(), scaled.getHeight(), encoded.length, quality);
                    return new CheckMedia(encoded, COMPRESSED_MEDIA_NAME);
                }
            }
            if (encoded == null) {
                log.error("[imgSecCheck] 送检副本压缩失败，按原字节送检：fileName={}", fileName);
                return new CheckMedia(original, fileName);
            }
            // 最低质量仍超限（极端大图）：仍送最小副本，微信侧若拒收会在返回码日志中体现
            log.error("[imgSecCheck] 送检副本压缩后仍超限：{}x{} {}B -> {}B", width, height, original.length, encoded.length);
            return new CheckMedia(encoded, COMPRESSED_MEDIA_NAME);
        } catch (Exception e) {
            log.error("[imgSecCheck] 送检副本生成异常，按原字节送检", e);
            return new CheckMedia(original, fileName);
        }
    }

    /**
     * 等比缩放到微信素材分辨率上限内，并铺白底转为不透明画布。
     * <p>
     * JPEG 不支持透明通道，直接编码带 alpha 的 PNG 会让透明区域变黑，故先铺白底再绘制。
     * 只缩小不放大。
     */
    private BufferedImage scaleToLimit(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        double ratio = Math.min(1.0D, Math.min(MAX_MEDIA_WIDTH / (double) width, MAX_MEDIA_HEIGHT / (double) height));
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));
        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return canvas;
    }

    /**
     * 按指定质量编码为 JPEG 字节。
     *
     * @return JPEG 字节；当前 JRE 无 JPEG 编码器时返回 null
     */
    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            return null;
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ImageOutputStream imageOut = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(imageOut);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
            imageOut.flush();
            return out.toByteArray();
        } finally {
            writer.dispose();
        }
    }
}
