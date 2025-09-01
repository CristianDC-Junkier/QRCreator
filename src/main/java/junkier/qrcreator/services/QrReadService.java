package junkier.qrcreator.services;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.image.BufferedImage;

/**
 *
 * @author USUARIO
 */
public class QrReadService {

    public static boolean isQrReadable(BufferedImage qrImage) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(qrImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result != null && !result.getText().isEmpty();
        } catch (Exception e) {
            return false; 
        }
    }
}
