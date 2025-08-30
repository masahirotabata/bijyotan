// src/main/java/com/example/demo/image/ImageNormalizeService.java
package com.example.demo.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Service
public class ImageNormalizeService {

    // 添付画像と同じ比率に固定（例：3:5）。出力サイズは任意に調整OK
    public static final int TARGET_W = 900;
    public static final int TARGET_H = 1500; // 3:5

    /** cover: 画像を中央トリミングして 3:5 に合わせてからリサイズ */
    public byte[] normalizeCover(InputStream in, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Thumbnails.of(in)
                    .crop(Positions.CENTER)  // 中央で必要分だけクロップ
                    .size(TARGET_W, TARGET_H)
                    .outputFormat(format)    // "png" か "jpg"
                    .toOutputStream(baos);
            return baos.toByteArray();
        }
    }

    /** contain: 画像の全体を保持したまま 3:5 のキャンバスに余白を付けて収める */
    public byte[] normalizeContain(InputStream in, String format) throws IOException {
        BufferedImage src = ImageIO.read(in);
        if (src == null) throw new IOException("画像を読み込めませんでした");

        double scale = Math.min((double) TARGET_W / src.getWidth(),
                                (double) TARGET_H / src.getHeight());
        int newW = (int) Math.round(src.getWidth() * scale);
        int newH = (int) Math.round(src.getHeight() * scale);

        // リサイズ
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(src, 0, 0, newW, newH, null);
        g2.dispose();

        // 3:5キャンバス（背景は白）
        BufferedImage canvas = new BufferedImage(TARGET_W, TARGET_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, TARGET_W, TARGET_H);
        int x = (TARGET_W - newW) / 2;
        int y = (TARGET_H - newH) / 2;
        g.drawImage(resized, x, y, null);
        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(canvas, format, baos);
            return baos.toByteArray();
        }
    }
}
