package junkier.qrcreator.services;

import javafx.scene.paint.Color;

/**
 *
 * @author Cristian
 */
public class LuminaliaService {

    /**
     * Verifica si el contraste entre dos colores es suficiente (mínimo 4.5:1)
     * basado en las reglas WCAG.
     *
     * @param foreground Color del patrón (QR)
     * @param background Color de fondo
     * @return true si el contraste es suficiente
     */
    public static boolean isContrastSufficient(Color foreground, Color background) {
        // Si son exactamente iguales o casi iguales, no hay contraste
        if (areColorsTooSimilar(foreground, background)) {
            return false;
        }

        double l1 = getRelativeLuminance(foreground);
        double l2 = getRelativeLuminance(background);

        if (l1 < l2) {
            double temp = l1;
            l1 = l2;
            l2 = temp;
        }

        double contrastRatio = (l1 + 0.05) / (l2 + 0.05);
        return contrastRatio >= 4.5;
    }

    /**
     * Método auxiliar para detectar si dos colores son demasiado parecidos.
     * Aquí definimos "demasiado parecido" como diferencia muy pequeña en RGB.
     */
    private static boolean areColorsTooSimilar(Color c1, Color c2) {
        final double THRESHOLD = 0.01; // tolerancia (1% diferencia por canal)

        return Math.abs(c1.getRed() - c2.getRed()) < THRESHOLD
                && Math.abs(c1.getGreen() - c2.getGreen()) < THRESHOLD
                && Math.abs(c1.getBlue() - c2.getBlue()) < THRESHOLD;
    }

    /**
     * Calcula la luminancia relativa de un color (según estándar WCAG).
     */
    private static double getRelativeLuminance(Color color) {
        double r = adjustComponent(color.getRed());
        double g = adjustComponent(color.getGreen());
        double b = adjustComponent(color.getBlue());
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * Ajuste gamma para el componente de color.
     */
    private static double adjustComponent(double c) {
        return (c <= 0.03928) ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
