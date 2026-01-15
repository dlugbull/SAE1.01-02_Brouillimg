//LUGBULL Damien
//MAHAMANE Mansourah
//C1

import java.util.function.*;
import java.awt.image.BufferedImage;
public class Profiler {

    /**
     * donne le temps d'execution pour brouiller une image
     * @param oneMethod fonction pour brouiller
     * @param inputImg image à brouiller
     * @param perm tableau de permutation des lignes
     * @return image brouillée
     */
    public static BufferedImage analyse_scramble(BiFunction<BufferedImage, int[], BufferedImage> oneMethod, BufferedImage inputImg, int[] perm) {
        long start = timestamp();
        BufferedImage res = oneMethod.apply(inputImg, perm);
        String elapsed = timestamp(start);
        System.out.println(elapsed);
        return res;
    }

    /**
     * donne le temps d'execution pour casser la clé
     * @param oneMethod fonction pour casser la clé
     * @param img image brouillée
     * @param method méthode utilisée pour casser la clé
     * @return clé
     */
    public static int analyse_breakkey(BiFunction<int[][], String, Integer> oneMethod, int[][] img, String method) {
        long start = timestamp();
        int key = oneMethod.apply(img, method);
        String elapsed = timestamp(start);
        System.out.println(elapsed);
        return key;
    }

    /**
     * Si clock0 est >0, retourne une chaîne de caractères
     * représentant la différence de temps depuis clock0.
     * @param clock0 instant initial
     * @return expression du temps écoulé depuis clock0
     */
    public static String timestamp(long clock0) {
        String result = null;

        if (clock0 > 0) {
            double elapsed = (System.nanoTime() - clock0) / 1e9;
            String unit = "s";
            if (elapsed < 1.0) {
                elapsed *= 1000.0;
                unit = "ms";
            }
            result = String.format("%.4g%s elapsed", elapsed, unit);
        }
        return result;
    }

    /**
     * retourne l'heure courante en ns.
     * @return
     */
    public static long timestamp() {
        return System.nanoTime();
    }
}