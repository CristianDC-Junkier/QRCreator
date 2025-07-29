package junkier.qrcreator.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Cristian
 */
public class QrGeneratorService {

    public static String generator(String adressQrTF, String nameQrTF, String imagePathField,
            Color frontPatPicker, Color backGrPicker) {
        try {
            // Datos base
            String qrText = adressQrTF;

            String userHome = System.getProperty("user.home");
            File defaultDir = new File(userHome, "Downloads");

            String outputPath = defaultDir + File.separator + nameQrTF.trim() + ".png";
            String logoPath = imagePathField.isBlank() ? null : imagePathField;
            int size = 600, margin = 1, maxLogoSize = size / 6;

            // Colores seleccionados por el usuario
            java.awt.Color awtFront = new java.awt.Color(
                    (float) frontPatPicker.getRed(),
                    (float) frontPatPicker.getGreen(),
                    (float) frontPatPicker.getBlue(),
                    (float) frontPatPicker.getOpacity()
            );
            java.awt.Color awtBack = new java.awt.Color(
                    (float) backGrPicker.getRed(),
                    (float) backGrPicker.getGreen(),
                    (float) backGrPicker.getBlue(),
                    (float) backGrPicker.getOpacity()
            );

            int colorFront = awtFront.getRGB();
            int colorBack = awtBack.getRGB();

            // Crear QR
            BitMatrix matrix = new QRCodeWriter().encode(
                    qrText,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    Map.of(EncodeHintType.MARGIN, margin)
            );

            BufferedImage qr = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = qr.createGraphics();
            g.drawImage(MatrixToImageWriter.toBufferedImage(matrix, new MatrixToImageConfig(colorFront, colorBack)), 0, 0, null);
            g.dispose();

            if (logoPath != null && !logoPath.isBlank()) {
                BufferedImage logo = ImageIO.read(new File(logoPath));

                // Tamaño máximo del cuadro del logo
                int logoSize = maxLogoSize;
                int padding = 10;

                // Calcular el factor de escala para que quepa proporcionalmente dentro de logoSize - 2*padding
                int availableSize = logoSize - 2 * padding;
                float scaleFactor = Math.min((float) availableSize / logo.getWidth(), (float) availableSize / logo.getHeight());

                int scaledWidth = Math.round(logo.getWidth() * scaleFactor);
                int scaledHeight = Math.round(logo.getHeight() * scaleFactor);

                // Escalar logo proporcionalmente
                Image scaledLogo = logo.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                // Crear fondo blanco cuadrado
                BufferedImage logoBox = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gl = logoBox.createGraphics();
                gl.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Fondo blanco
                gl.setColor(awtBack);
                gl.fillRect(0, 0, logoSize, logoSize);

                // Dibujar logo centrado dentro del fondo blanco
                int xOffset = (logoSize - scaledWidth) / 2;
                int yOffset = (logoSize - scaledHeight) / 2;
                gl.drawImage(scaledLogo, xOffset, yOffset, null);
                gl.dispose();

                // Insertar logo en el centro del QR
                Graphics2D g2 = qr.createGraphics();
                int x = (size - logoSize) / 2;
                int y = (size - logoSize) / 2;
                g2.setComposite(AlphaComposite.SrcOver);
                g2.drawImage(logoBox, x, y, null);
                g2.dispose();
            }

            // Crear carpeta si no existe
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();

            // Guardar imagen
            ImageIO.write(qr, "png", outputFile);

            System.out.println("✅ QR generado en: " + outputPath);

            return outputPath;

        } catch (Exception e) {
            System.out.println("Error en el generador - " + e.getMessage());
            return null;
        }
    }
}
