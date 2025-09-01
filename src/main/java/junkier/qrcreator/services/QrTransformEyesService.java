package junkier.qrcreator.services;

import com.google.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para transformar los ojos de un QR a distintas formas.
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
     * deseada.
     *
     * @param matrix
     * @param shape
     * @return
     */
    public static BitMatrix transformEyes(BitMatrix matrix, EyeShape shape) {
        int size = matrix.getWidth();
        boolean[][] processed = new boolean[size][size];

        // Detectar todos los bloques grandes (potenciales ojos)
        List<int[]> candidates = detectLargeBlocks(matrix, processed);

        // Seleccionar los 3 bloques más cercanos a las esquinas
        List<int[]> eyes = selectEyesByProximityToCorners(candidates, size, 3);

        // Aplicar la forma deseada a cada ojo
        for (int[] eye : eyes) {
            applyShape(matrix, eye, shape);
        }

        return matrix;
    }

    /**
     * Detecta todos los bloques cuadrados grandes en la matriz. Devuelve lista
     * de {x, y, blockSize}.
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
     * Selecciona los n bloques más cercanos a cualquier esquina.
     */
    private static List<int[]> selectEyesByProximityToCorners(List<int[]> candidates, int matrixSize, int n) {
        int[][] corners = {
            {0, 0}, // top-left
            {matrixSize - 1, 0}, // top-right
            {0, matrixSize - 1} // bottom-left
        };

        // 1. Encontrar el tamaño máximo
        int maxSize = candidates.stream().mapToInt(b -> b[2]).max().orElse(0);

        // 2. Filtrar solo los bloques de tamaño máximo
        List<int[]> maxCandidates = new ArrayList<>();
        for (int[] c : candidates) {
            if (c[2] == maxSize) {
                maxCandidates.add(c);
            }
        }

        // 3. Ordenar los bloques máximos por distancia mínima a cualquier esquina
        maxCandidates.sort((a, b) -> Integer.compare(minDistanceToCorners(a, corners), minDistanceToCorners(b, corners)));

        // 4. Tomar los n primeros (los más cercanos a las esquinas)
        return maxCandidates.stream().limit(n).toList();
    }

    /**
     * Calcula la distancia mínima de un bloque a las esquinas.
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
                        double angle = Math.atan2(ny, nx);

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
