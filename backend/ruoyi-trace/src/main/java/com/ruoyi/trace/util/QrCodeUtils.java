package com.ruoyi.trace.util;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;

/**
 * 二维码生成工具（ZXing）
 *
 * @author ruoyi
 */
public class QrCodeUtils
{
    /**
     * 生成二维码 PNG 到 {profile}/qrcode/{fileName}.png
     *
     * @return 资源路径，如 /profile/qrcode/WM202610310001.png
     */
    public static String generate(String content, String fileName, int size) throws Exception
    {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        File dir = new File(RuoYiConfig.getProfile(), "qrcode");
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        Path file = new File(dir, fileName + ".png").toPath();
        MatrixToImageWriter.writeToPath(matrix, "PNG", file);
        return Constants.RESOURCE_PREFIX + "/qrcode/" + fileName + ".png";
    }
}
