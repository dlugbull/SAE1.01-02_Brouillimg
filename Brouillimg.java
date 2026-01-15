//LUGBULL Damien
//MAHAMANE Mansourah
//C1

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Brouillimg {
    public static void main(String[] args) throws IOException {
        String outPath = "out.png";

        String process = args[1];
        String inPath = args[0];

        BufferedImage inputImage = ImageIO.read(new File(inPath));
        BufferedImage scrambledImage = inputImage;
        if (inputImage == null) {
            throw new IOException("Format d’image non reconnu: " + inPath);
        }

        final int height = inputImage.getHeight();
        final int width = inputImage.getWidth();
        System.out.println("Dimensions de l'image : " + width + "x" + height);

        if (process.equals("scramble")) {
            if (args.length < 3) {
                System.err.println("Usage: java Brouillimg <image_claire> scramble <clé> [image_sortie]");
                System.exit(1);
            }
            outPath = (args.length >= 4) ? args[3] : "out.png";
            // Masque 0x7FFF pour garantir que la clé ne dépasse pas les 15 bits
            int key = Integer.parseInt(args[2]) & 0x7FFF;
            int[] perm = generatePermutation(height, key);

            scrambledImage = Profiler.analyse_scramble(Brouillimg::scrambleLines, inputImage, perm);
        } else if (process.equals("unscramble")) {
            if (args.length < 4) {
                System.err.println("Usage: java Brouillimg <image_claire> unscramble <method> <speed> [image_sortie]");
                System.exit(1);
            }
            outPath = (args.length >= 5) ? args[4] : "out.png";
            String method = args[2];
            // Pré‑calcul des lignes en niveaux de gris pour accélérer le calcul du critère
            int[][] inputImageGL = rgb2gl(inputImage);
            System.out.println("Grey leveled");
            int key = 0;
            if (args[3].equals("fast")) {
                key = Profiler.analyse_breakkey(Brouillimg::breakKeyFast, inputImageGL, method);
            } else if (args[3].equals("slow")) {
                key = Profiler.analyse_breakkey(Brouillimg::breakKey, inputImageGL, method);
            } else {
                System.err.println("speed must be slow or fast");
                System.exit(1);
            }

            System.out.println("key : " + key);

            int[] perm = generatePermutation(height, key);
            scrambledImage = unScrambleLines(inputImage, perm);
        } else {
            System.err.println("process must be scramble or unscramble");
            System.exit(1);
        }
        ImageIO.write(scrambledImage, "png", new File(outPath));
        System.out.println("Image écrite: " + outPath);
    }

    /**
     * Convertit une image RGB en niveaux de gris (GL). → modifiée pour paralléliser et utiliser tout les coeurs
     *
     * @param inputRGB image d'entrée en RGB
     * @return tableau 2D des niveaux de gris (0-255)
     */
    public static int[][] rgb2gl(BufferedImage inputRGB) {
        final int height = inputRGB.getHeight();
        final int width = inputRGB.getWidth();
        int[][] outGL = new int[height][width];

        IntStream.range(0,height).parallel().forEach( y ->
                {
                    for (int x = 0; x < width; x++) {
                        int argb = inputRGB.getRGB(x, y);
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;
                        // luminance simple (évite float)
                        int gray = (r * 299 + g * 587 + b * 114) / 1000;
                        outGL[y][x] = gray;
                    }
                }
        );
        return outGL;
    }

    /**
     * Génère une permutation des entiers 0..size-1 en fonction d'une clé.
     *
     * @param size taille de la permutation
     * @param key  clé de génération (15 bits)
     * @return tableau de taille 'size' contenant une permutation des entiers 0..size-1
     */
    public static int[] generatePermutation(int size, int key) {
        int[] scrambleTable = new int[size];
        for (int i = 0; i < size; i++) {
            scrambleTable[i] = scrambledId(i, size, key);
        }
        return scrambleTable;
    }

    /**
     * Mélange les lignes d'une image selon une permutation donnée.
     *
     * @param inputImg image d'entrée
     * @param perm permutation des lignes (taille = hauteur de l'image)
     * @return image de sortie avec les lignes mélangées
     */
    public static BufferedImage scrambleLines(BufferedImage inputImg, int[] perm) {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        if (perm.length != height) throw new IllegalArgumentException("Taille d'image <> taille permutation");

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            out.setRGB(0, perm[y], width, 1, inputImg.getRGB(0, y, width, 1, null, 0, width), 0, width);
        }

        return out;
    }

    /**
     * Renvoie la position de la ligne id dans l'image brouillée.
     *
     * @param id   indice de la ligne dans l'image claire (0..size-1)
     * @param size nombre total de lignes dans l'image
     * @param key  clé de brouillage (15 bits)
     * @return indice de la ligne dans l'image brouillée (0..size-1)
     * a completer
     */
    public static int scrambledId(int id, int size, int key) {
        int s = key & 0x7F;
        int r = key >> 7;
        while (pgcd(2 * s + 1, size) != 1) {
            s = (s + 1) & 0x7F;
        }
        id = (r + (2 * s + 1) * id) % size;
        return id;
    }

    /**
     * calcule le pgcd de 2 entiers
     *
     * @param a
     * @param b
     * @return pgcd
     */
    public static int pgcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * renvoie l'image déchiffrée
     *
     * @param inputImg image chiffrée
     * @param perm     permutation des lignes
     * @return image déchiffrée
     */
    public static BufferedImage unScrambleLines(BufferedImage inputImg, int[] perm) {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        if (perm.length != height) throw new IllegalArgumentException("Taille d'image <> taille permutation");

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            out.setRGB(0, y, width, 1, inputImg.getRGB(0, perm[y], width, 1, null, 0, width), 0, width);
        }

        return out;
    }


    /**
     * cherche la meilleure clé pour dechiffrer l'image en lançant la méthode demandée -- optimisé
     *
     * @param inputImg image à déchiffrer
     * @return meilleure clé (entier entre 0 et 32767)
     */
    public static int breakKeyFast(int[][] inputImg, String method) {
        if (method.equals("euclide")) {
            return BreakKey.breakKeyEuclidean(inputImg);
        } else if (method.equals("pearson")) {
            return BreakKey.breakKeyPearson(inputImg);
        }
        System.err.println("Méthode doit être euclide ou pearson.");
        return -1;
    }

    /**
     * cherche la meilleure clé pour dechiffrer l'image en lançant la méthode demandée -- force brute
     *
     * @param scrambleImage
     * @param method
     * @return meilleure clé (entier entre 0 et 32767)
     */
    public static int breakKey(int[][] scrambleImage, String method) {
        int bestKey = 0;
        double bestScore = (method.equals("euclide")) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        double score;

        if (method.equals("euclide")) {
            for (int key = 0; key < 32768; key++) {
                int[] perm = generatePermutation(scrambleImage.length, key);

                score = BreakKey.scoreEuclidean(scrambleImage, perm, false);

                if (score < bestScore) {
                    bestScore = score;
                    bestKey = key;
                }
            }
        } else if (method.equals("pearson")) {
            for (int key = 0; key < 32768; key++) {
                int[] perm = generatePermutation(scrambleImage.length, key);

                score = BreakKey.scorePearson(scrambleImage, perm, false);

                if (score > bestScore) {
                    bestScore = score;
                    bestKey = key;
                }
            }
        } else {
            System.err.println("Méthode doit être euclide ou pearson.");
            return -1;
        }
        return bestKey;
    }
}