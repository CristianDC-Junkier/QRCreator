package junkier.qrcreator.services;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Servicio que se encarga de comprobar si un QR se puede leer, o devolver
 * sobre una imagen con QR, su contenido
 *
 * @author Cristian Delgado Cruz
 * @since 2025-08-29
 * @version 1.0
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

    public static String readQrContent(File qrImage) {
        try {
            BufferedImage qrMatrix = ImageIO.read(qrImage);
            LuminanceSource source = new BufferedImageLuminanceSource(qrMatrix);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            return null; 
        }
    }
}
