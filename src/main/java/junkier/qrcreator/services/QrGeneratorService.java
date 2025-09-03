package junkier.qrcreator.services;

import com.google.zxing.*;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.AlphaComposite;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;

import java.io.File;
import java.util.Map;
import javax.imageio.ImageIO;
import junkier.qrcreator.services.QrTransformEyesService.EyeShape;

/**
 * Servicio que se encarga de generar, tanto el preview como el Qr en si
 *
 * @author Cristian Delgado Cruz
 * @since 2025-07-28
 * @version 1.3
 */
public class QrGeneratorService {

    private static final int SIZE = 600;
    private static final int MARGIN = 1;

    // Generar y guardar en disco 
    public static String generator(String adressQrTF, String nameQrTF, String imagePathField,
            javafx.scene.paint.Color frontPatPicker, javafx.scene.paint.Color backGrPicker, EyeShape eyeshape) {
        try {
            String outputPath = buildOutputPath(nameQrTF);
            BufferedImage qr = generateQrBuffered(adressQrTF, imagePathField, frontPatPicker, backGrPicker, eyeshape);
            saveImage(qr, outputPath);
            return outputPath;
        } catch (Exception e) {
            System.out.println("Error en el generador - " + e.getMessage());
            return null;
        }
    }

    // Generar preview para ImageView
    public static BufferedImage generatorPreview(String adressQrTF, String imagePathField,
            javafx.scene.paint.Color frontPatPicker, javafx.scene.paint.Color backGrPicker, EyeShape eyeshape) {
        try {
            return generateQrBuffered(adressQrTF, imagePathField, frontPatPicker, backGrPicker, eyeshape);
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage generateQrBuffered(String adressQrTF, String imagePathField,
            javafx.scene.paint.Color frontPatPicker, javafx.scene.paint.Color backGrPicker, EyeShape eyeshape) throws Exception {

        String logoPath = imagePathField.isBlank() ? null : imagePathField;

        Color awtFront = fxColorToAwt(frontPatPicker);
        Color awtBack = fxColorToAwt(backGrPicker);

        BitMatrix matrix = createMatrix(adressQrTF);

        matrix = QrTransformEyesService.transformEyes(matrix, eyeshape);

        int matrixSize = matrix.getWidth();
        double modulePixel = (double) SIZE / matrixSize;

        BufferedImage qr = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = qr.createGraphics();

        // Dibujar módulos del QR 
        for (int mx = 0; mx < matrixSize; mx++) {
            for (int my = 0; my < matrixSize; my++) {
                int color = matrix.get(mx, my) ? awtFront.getRGB() : awtBack.getRGB();
                int x0 = (int) Math.round(mx * modulePixel);
                int y0 = (int) Math.round(my * modulePixel);
                int w = (int) Math.ceil(modulePixel);
                int h = (int) Math.ceil(modulePixel);
                g2.setColor(new Color(color, true));
                g2.fillRect(x0, y0, w, h);
            }
        }

        g2.dispose();

        // Insertar logo si existe
        if (logoPath != null) {
            BufferedImage logo = ImageIO.read(new File(logoPath));
            qr = insertLogo(qr, logo, awtBack);
        }

        return qr;
    }

    // Insertar logo centrado con fondo sólido
    private static BufferedImage insertLogo(BufferedImage qr, BufferedImage logo, Color bg) {
        int maxLogoSize = qr.getWidth() / 6;
        int padding = 10;
        int availableSize = maxLogoSize - 2 * padding;

        float scaleFactor = Math.min(
                (float) availableSize / logo.getWidth(),
                (float) availableSize / logo.getHeight()
        );

        int scaledWidth = Math.round(logo.getWidth() * scaleFactor);
        int scaledHeight = Math.round(logo.getHeight() * scaleFactor);

        Image scaledLogo = logo.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        // Crear fondo sólido para el logo
        BufferedImage logoBox = new BufferedImage(maxLogoSize, maxLogoSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gl = logoBox.createGraphics();
        gl.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Fondo sólido
        gl.setComposite(AlphaComposite.Src);
        gl.setColor(bg);
        gl.fillRect(0, 0, maxLogoSize, maxLogoSize);

        // Dibujar logo sobre el fondo
        gl.setComposite(AlphaComposite.SrcOver);
        int xOffset = (maxLogoSize - scaledWidth) / 2;
        int yOffset = (maxLogoSize - scaledHeight) / 2;
        gl.drawImage(scaledLogo, xOffset, yOffset, null);
        gl.dispose();

        // Insertar logo en QR
        Graphics2D g2 = qr.createGraphics();
        g2.setComposite(AlphaComposite.SrcOver);
        int x = (qr.getWidth() - maxLogoSize) / 2;
        int y = (qr.getHeight() - maxLogoSize) / 2;
        g2.drawImage(logoBox, x, y, null);
        g2.dispose();

        return qr;
    }

    // Convertir Color JavaFX → AWT
    private static Color fxColorToAwt(javafx.scene.paint.Color fxColor) {
        return new Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity()
        );
    }

    // Crear Matriz del QR
    private static BitMatrix createMatrix(String text) throws WriterException {
        return new QRCodeWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                SIZE,
                SIZE,
                Map.of(EncodeHintType.MARGIN, MARGIN)
        );
    }

    // Guardar imagen en disco
    private static void saveImage(BufferedImage img, String path) throws Exception {
        File outputFile = new File(path);
        outputFile.getParentFile().mkdirs();
        ImageIO.write(img, "png", outputFile);
    }

    // Construir path de salida
    private static String buildOutputPath(String name) {
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Downloads");
        return defaultDir + File.separator + name.trim() + ".png";
    }

}
