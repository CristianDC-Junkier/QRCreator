package junkier.qrcreator.services;

import com.google.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para transformar los ojos de un QR a distintas formas.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-09-01
 * @version 1.0
 */
public class QrTransformEyesService {

    public enum EyeShape {
        SQUARE("Cuadrado"),
        CIRCLE("Círculo"),
        HEART("Corazón"),
        FLOWER("Flor"),
        STAR("Estrella"),
        ADD("Suma"),
        MULTIPLY("Multiplicación"),
        CROSS("Cruz"),
        SUN("Sol"),
        SNOWFLAKE("Copo de nieve");

        private final String displayName;

        EyeShape(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Método principal que transforma los ojos del BitMatrix según la forma
     * deseada. Detecta los ojos por tamaño (los más grandes) y selecciona los 3
     * más cercanos a las esquinas
     *
     * @param matrix matriz con los valores del qr
     * @param shape forma en la cual queremos colocar el qr
     * @return
     */
    public static BitMatrix transformEyes(BitMatrix matrix, EyeShape shape) {
        int size = matrix.getWidth();
        boolean[][] processed = new boolean[size][size];

        List<int[]> candidates = detectLargeBlocks(matrix, processed);

        List<int[]> eyes = selectEyesByProximityToCorners(candidates, size, 3);

        for (int[] eye : eyes) {
            applyShape(matrix, eye, shape);
        }

        return matrix;
    }

    /**
     * Detecta todos los bloques cuadrados grandes en la matriz. Devuelve lista
     * de {x, y, blockSize}.
     *
     * @param matrix matriz con los valores del QR
     * @param boolean tabla (lista bidimensional) donde se coloca si el bit fue
     * procesado o no
     * @return la lista bidimensional de candidatos
     */
    private static List<int[]> detectLargeBlocks(BitMatrix matrix, boolean[][] processed) {
        int size = matrix.getWidth();
        List<int[]> candidates = new ArrayList<>();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (matrix.get(x, y) && !processed[x][y]) {
                    int blockSize = detectSquareBlock(matrix, x, y);

                    candidates.add(new int[]{x, y, blockSize});

                    // Marcar procesado
                    for (int dx = 0; dx < blockSize; dx++) {
                        for (int dy = 0; dy < blockSize; dy++) {
                            if (x + dx < size && y + dy < size) {
                                processed[x + dx][y + dy] = true;
                            }
                        }
                    }
                }
            }
        }

        return candidates;
    }

    /**
     * Detecta el tamaño de un bloque cuadrado comenzando en (x, y).
     *
     * @param matrix matriz con los valores del QR
     * @param x posicion x inicial del bloque candidato
     * @param y posicion y inicial del bloque candidato
     * @return tamaño del bloque
     */
    private static int detectSquareBlock(BitMatrix matrix, int x, int y) {
        int size = matrix.getWidth();
        int blockSize = 1;

        while (x + blockSize < size && y + blockSize < size) {
            boolean square = true;
            for (int dx = 0; dx <= blockSize; dx++) {
                if (!matrix.get(x + dx, y + blockSize)) {
                    square = false;
                    break;
                }
            }
            for (int dy = 0; dy <= blockSize; dy++) {
                if (!matrix.get(x + blockSize, y + dy)) {
                    square = false;
                    break;
                }
            }
            if (!square) {
                break;
            }
            blockSize++;
        }

        return blockSize;
    }

    /**
     * Selecciona los n bloques más cercanos a cualquier esquina, filrando por
     * primero el tamaño maximo, luego los 3 más cercanos
     * 
     * @param candidates lista de candidatos previamente seleccionados
     * @param matrixSize tamaño completo de la matriz
     * @param n bloques totales a elegir
     * @return 
     */
    private static List<int[]> selectEyesByProximityToCorners(List<int[]> candidates, int matrixSize, int n) {
        int[][] corners = {
            {0, 0}, // top-left
            {matrixSize - 1, 0}, // top-right
            {0, matrixSize - 1} // bottom-left
        };

        int maxSize = candidates.stream().mapToInt(b -> b[2]).max().orElse(0);

        List<int[]> maxCandidates = new ArrayList<>();
        for (int[] c : candidates) {
            if (c[2] == maxSize) {
                maxCandidates.add(c);
            }
        }

        maxCandidates.sort((a, b) -> Integer.compare(minDistanceToCorners(a, corners), minDistanceToCorners(b, corners)));

        return maxCandidates.stream().limit(n).toList();
    }

    /**
     * Calcula la distancia mínima de un bloque a las esquinas.
     * 
     * @param candidate candidato elegido
     * @param corners esquinas de la matriz previamente asignadas
     * @return valor de la distancia minima
     */
    private static int minDistanceToCorners(int[] candidate, int[][] corners) {
        int x0 = candidate[0];
        int y0 = candidate[1];
        int blockSize = candidate[2];
        int cx = x0 + blockSize / 2;
        int cy = y0 + blockSize / 2;

        int minDist = Integer.MAX_VALUE;
        for (int[] corner : corners) {
            int dx = cx - corner[0];
            int dy = cy - corner[1];
            int distSq = dx * dx + dy * dy;
            if (distSq < minDist) {
                minDist = distSq;
            }
        }
        return minDist;
    }

    /**
     * Aplica la forma deseada a un bloque de la matriz.
     * CIRCULO, CORAZON, ESTRELLA, FLOR, SUMA, MULTIPLICACIÓN
     * CRUZ, SOL, COPO DE NIEVE o DEFAULT = CUADRADO
     * 
     * @param matrix Matriz original 
     * @param eye bloque que hay que cambiar
     * @param shape forma elegida
     */
    private static void applyShape(BitMatrix matrix, int[] eye, EyeShape shape) {
        int x0 = eye[0];
        int y0 = eye[1];
        int blockSize = eye[2];

        double cx = x0 + blockSize / 2.0;
        double cy = y0 + blockSize / 2.0;

        for (int dx = 0; dx < blockSize; dx++) {
            for (int dy = 0; dy < blockSize; dy++) {
                double nx = (dx + x0 - cx + 0.5) / (blockSize / 2.0);
                double ny = (dy + y0 - cy + 0.5) / (blockSize / 2.0);

                boolean keep;
                switch (shape) {
                    case CIRCLE ->
                        keep = (nx * nx + ny * ny) <= 1;
                    case HEART -> {
                        double x = nx * 1.3; // escala horizontal
                        double y = -ny * 1.3 + 0.2; // escala vertical
                        keep = Math.pow(x * x + y * y - 1, 3) - x * x * y * y * y <= 0;
                    }

                    case STAR -> {
                        double angle = Math.atan2(ny, nx);
                        double r = Math.sqrt(nx * nx + ny * ny);
                        double rOuter = 1.0;
                        double rInner = 0.5;
                        int spikes = 5;
                        double theta = angle * spikes;
                        double starRadius = rInner + (rOuter - rInner) * (Math.cos(theta) * 0.5 + 0.5);
                        keep = r <= starRadius;
                    }
                    case FLOWER -> {
                        double angle = Math.atan2(ny, nx);
                        double r = Math.sqrt(nx * nx + ny * ny);
                        double star = Math.cos(5 * angle) * 0.5 + 0.5;
                        keep = r <= star;
                    }
                    case ADD ->
                        keep = Math.abs(nx) <= 0.2 || Math.abs(ny) <= 0.2; // una cruz simple
                    case CROSS -> {
                        double verticalWidth = 0.2;      // ancho del brazo vertical
                        double horizontalHeight = 0.2;   // grosor del brazo horizontal
                        double horizontalLength = 0.75;  // longitud del brazo horizontal
                        double horizontalOffset = 0.25;   // desplazamiento vertical del brazo horizontal hacia arriba

                        boolean vertical = Math.abs(nx) <= verticalWidth && ny >= -1 && ny <= 1;
                        boolean horizontal = Math.abs(ny + horizontalOffset) <= horizontalHeight && nx >= -horizontalLength && nx <= horizontalLength;

                        keep = vertical || horizontal;
                    }

                    case MULTIPLY ->
                        keep = Math.abs(nx + ny) <= 0.2 || Math.abs(nx - ny) <= 0.2;
                    case SUN -> {
                        double r = Math.sqrt(nx * nx + ny * ny);

                        double centerRadius = 0.6;
                        int spikes = 8;
                        double spikeLength = 0.4;
                        double spikeWidth = 0.05;

                        boolean central = r <= centerRadius;

                        // calcular el ángulo de cada rayo
                        double sector = 2 * Math.PI / spikes;
                        boolean rays = false;
                        for (int i = 0; i < spikes; i++) {
                            double rayAngle = i * sector;
                            // vector perpendicular al rayo
                            double dxSun = nx - Math.cos(rayAngle) * r;
                            double dySun = ny - Math.sin(rayAngle) * r;
                            double dist = Math.sqrt(dxSun * dxSun + dySun * dySun);
                            if (r > centerRadius && r <= centerRadius + spikeLength && dist <= spikeWidth) {
                                rays = true;
                                break;
                            }
                        }

                        keep = central || rays;
                    }

                    case SNOWFLAKE -> {
                        double angle = Math.atan2(ny, nx);
                        double r = Math.sqrt(nx * nx + ny * ny);
                        keep = r <= 1 && (Math.abs(Math.sin(6 * angle)) > 0.5 || Math.abs(nx) < 0.1 || Math.abs(ny) < 0.1); // copo de nieve estilizado
                    }
                    default ->
                        keep = true;
                }

                if (!keep) {
                    matrix.unset(x0 + dx, y0 + dy);
                }
            }
        }
    }
}
