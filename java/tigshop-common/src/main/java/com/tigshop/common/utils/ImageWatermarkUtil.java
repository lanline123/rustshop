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

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 图片文字水印工具。
 *
 * <p>以半透明斜向平铺的方式把文字铺满整张图，用于营业执照等公示类图片的防盗用处理。
 * 字体使用内置的 {@code fonts/TigshopWatermark-Regular.otf}（Noto Sans SC 子集，OFL 协议，已按协议要求改名），
 * 不依赖服务器系统字体，避免 Linux 环境缺少中文字体导致渲染成方块。
 * 该子集只包含「营业执照信息公示专用」这几个字，若需修改水印文案需要重新裁剪字体。
 *
 * @author Tigshop团队
 */
@Slf4j
public final class ImageWatermarkUtil {

    private static final String FONT_RESOURCE = "fonts/TigshopWatermark-Regular.otf";

    /** 解码前的像素上限，防止小体积高分辨率图片解压后耗尽堆内存 */
    private static final long MAX_PIXELS = 40_000_000L;

    private static final double ROTATE_RADIANS = Math.toRadians(-30);
    private static final float ALPHA = 0.28f;
    private static final Color TEXT_COLOR = new Color(0x80, 0x80, 0x80);
    private static final float JPEG_QUALITY = 0.9f;

    private static volatile Font baseFont;

    private ImageWatermarkUtil() {
    }

    /**
     * 给图片铺满半透明斜向文字水印，并统一输出为 JPEG。
     *
     * @param imageBytes 原图字节（支持 ImageIO 可解码的格式：jpeg / png / gif / bmp）
     * @param text       水印文字
     * @return 加水印后的 JPEG 字节
     * @throws IOException 图片无法解码或尺寸超限
     */
    public static byte[] tileText(byte[] imageBytes, String text) throws IOException {
        BufferedImage source = decode(imageBytes);
        int width = source.getWidth();
        int height = source.getHeight();

        // 统一绘制到不带透明通道的 RGB 画布上，兼容 PNG 透明背景与调色板图，同时保证能编码为 JPEG
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.drawImage(source, 0, 0, null);
            drawTiledText(g, width, height, text);
        } finally {
            g.dispose();
        }
        return encodeJpeg(canvas);
    }

    private static void drawTiledText(Graphics2D g, int width, int height, String text) {
        float fontSize = Math.max(14f, width / 22f);
        Font font = loadFont().deriveFont(fontSize);
        g.setFont(font);
        g.setColor(TEXT_COLOR);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int stepX = (int) (textWidth * 1.6);
        int stepY = (int) (fontSize * 5);

        // 围绕画布中心旋转后，按对角线长度铺一个足够大的网格，保证旋转后四角也被覆盖
        g.rotate(ROTATE_RADIANS, width / 2.0, height / 2.0);
        int radius = (int) Math.ceil(Math.hypot(width, height) / 2);
        int centerX = width / 2;
        int centerY = height / 2;
        int row = 0;
        for (int y = centerY - radius; y <= centerY + radius; y += stepY, row++) {
            // 奇偶行交错半个步长，避免水印排成整齐的列而容易被局部裁切规避
            int offset = (row % 2 == 0) ? 0 : stepX / 2;
            for (int x = centerX - radius - stepX + offset; x <= centerX + radius; x += stepX) {
                g.drawString(text, x, y);
            }
        }
    }

    private static BufferedImage decode(byte[] imageBytes) throws IOException {
        try (InputStream in = new ByteArrayInputStream(imageBytes);
             ImageInputStream imageStream = new MemoryCacheImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            if (!readers.hasNext()) {
                throw new IOException("不支持的图片格式");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageStream, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > MAX_PIXELS) {
                    throw new IOException("图片分辨率过大");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IOException("图片解码失败");
                }
                return image;
            } finally {
                reader.dispose();
            }
        }
    }

    private static byte[] encodeJpeg(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("当前环境不支持 JPEG 编码");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream imageOut = ImageIO.createImageOutputStream(out)) {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(JPEG_QUALITY);
            }
            writer.setOutput(imageOut);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    private static Font loadFont() {
        Font font = baseFont;
        if (font != null) {
            return font;
        }
        synchronized (ImageWatermarkUtil.class) {
            if (baseFont == null) {
                baseFont = createFont();
            }
            return baseFont;
        }
    }

    private static Font createFont() {
        try (InputStream in = ImageWatermarkUtil.class.getClassLoader().getResourceAsStream(FONT_RESOURCE)) {
            if (in == null) {
                throw new IOException("字体资源不存在：" + FONT_RESOURCE);
            }
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (IOException | FontFormatException e) {
            // 内置字体加载失败时退回系统字体，功能不中断，但中文在缺字体的服务器上可能显示为方块
            log.error("加载水印字体失败，退回系统默认字体", e);
            return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }
}
