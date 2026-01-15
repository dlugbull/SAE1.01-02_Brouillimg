//LUGBULL Damien
//MAHAMANE Mansourah
//C1

public class BreakKey {
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
     * renvoie la distance euclidienne entre 2 lignes consécutives de l'image
     *
     * @param x une ligne de l'image
     * @param y la ligne suivante de l'image
     * @return renvoie la distance euclidienne
     */
    public static double euclideanDistance(int[] x, int[] y) {
        double somme = 0;
        double difference;
        for (int i = 0; i < x.length; i++) {
            difference = (double) x[i] - (double) y[i];
            somme += difference * difference;
        }
        return Math.sqrt(somme);
    }

    /**
     * calcule la somme des distances euclidienne de l'image
     *
     * @param inputImg image a calculer
     * @param perm     tableau de permutation des lignes inversé
     * @param skipLines si on saute des lignes ou non
     * @return somme des distance euclidienne
     */
    public static double scoreEuclidean(int[][] inputImg, int[] perm, boolean skipLines) {
        double somme = 0;
        int step = (skipLines) ? inputImg.length / 100 : 1;

        for (int i = 0; i < inputImg.length - 1; i += step) {
            somme += euclideanDistance(inputImg[perm[i]], inputImg[perm[i + 1]]);
        }
        return somme;
    }

    /**
     * trouve la meilleure clé pour déchiffrer l'image à l'aide de la méthode euclidienne
     *
     * @param inputImg
     * @return meilleure clé
     */
    public static int breakKeyEuclidean(int[][] inputImg){
        double min = Double.POSITIVE_INFINITY;
        int height = inputImg.length;
        int bestKey = 0;
        int bestS=0;
        double score;

        long start = Profiler.timestamp();
        for (int s = 0; s < 128; s++) {
            if (pgcd(2 * s + 1, height) != 1) continue;

            score = scoreEuclidean(inputImg, Brouillimg.generatePermutation(height, s), true);

            if (score < min) {
                min = score;
                bestS = s;
            }
        }
        System.out.println("Found s in "+(System.nanoTime() - start) / 1e6+" ms");

        min = Double.POSITIVE_INFINITY;

        start = Profiler.timestamp();
        for (int r = 0; r < 256; r++) {
            int key = (r<<7)|bestS;
            score = scoreEuclidean(inputImg, Brouillimg.generatePermutation(height, key), false);

            if (score < min) {
                min = score;
                bestKey = key;
            }
        }
        System.out.println("Found r in "+(System.nanoTime() - start) / 1e6+" ms");

        return bestKey;
    }


    /**
     * trouve la meilleure clé pour déchiffrer l'image à l'aide de la méthode de Pearson
     *
     * @param inputImg
     * @return meilleure clé
     */
    public static int breakKeyPearson(int[][] inputImg) {
        int height = inputImg.length;
        int bestKey = 0;
        double max = Double.NEGATIVE_INFINITY;
        int bestS = 0;
        double score;

        long start = Profiler.timestamp();
        for (int s = 0; s < 128; s++) {
            if (pgcd(2 * s + 1, height) != 1) continue;


            score = scorePearson(inputImg, Brouillimg.generatePermutation(height, s), true);

            if (score > max) {
                max = score;
                bestS = s;
            }
        }
        System.out.println("Found s in "+(System.nanoTime() - start) / 1e6+" ms");

        max = Double.NEGATIVE_INFINITY;

        start = Profiler.timestamp();
        for (int r = 0; r < 256; r++) {
            int key = (r<<7)|bestS;
            score = scorePearson(inputImg, Brouillimg.generatePermutation(height, key), false);

            if (score > max) {
                max = score;
                bestKey = key;
            }
        }
        System.out.println("Found r in "+(System.nanoTime() - start) / 1e6+" ms");
        return bestKey;
    }

    /**
     * calcule le pgcd de deux entiers
     *
     * @param n
     * @return pgcd
     */
    public static double average(int[] n){
        double somme = 0 ;

        for (int i = 0; i < n.length; i++){
            somme += n[i];
        }

        return somme / n.length;
    }


    /**
     * calcule la distance de Pearson entre 2 lignes
     *
     * @param x
     * @param y
     * @return distance
     */
    public static double pearsonCorrelation( int[] x, int[]y){
        double sommeUn = 0;
        double sommeDeux = 0;
        double sommeTrois = 0;
        double avgX = average(x);
        double avgY = average(y);


        for (int i = 0; i < x.length; i++){
            sommeUn   += (x[i] - avgX) * (y[i] - avgY);
            sommeDeux += Math.pow(x[i] - avgX, 2);
            sommeTrois += Math.pow(y[i] - avgY, 2);
        }

        return sommeUn /(Math.sqrt(sommeDeux)* Math.sqrt(sommeTrois));
    }

    /**
     * calcule le score de Pearson d'une image
     *
     * @param inputImg image a calculer
     * @param perm     tableau de permutation des lignes inversé
     * @param skipLines si on saute des lignes ou non
     * @return score total
     */
    public static double scorePearson(int[][] inputImg, int[] perm, boolean skipLines) {
        double total=0;
        int step = (skipLines) ? inputImg.length/100 : 1;
        for (int i=0; i<inputImg.length-1; i+=step){
            total += pearsonCorrelation(inputImg[perm[i]], inputImg[perm[i+1]]);
        }
        return total;
    }
}