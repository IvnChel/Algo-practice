import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class Main {
    private static final int[] SIZES = {1280, 2560, 5120, 10240, 20480, 40960, 81920};
    private static final int REPETITIONS = 5;
    private static final int WARMUP_ROUNDS = 100;
    private static final int WARMUP_SIZE = 1000;
    private static final String[] NAMES = {"Bubble", "Merge", "Arrays.sort"};

    public static void main(String[] args) {
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("Время в наносекундах. Все исходные замеры выводятся для переноса в Excel.");

        // Как в учебном примере: прогреваем Bubble и Merge.
        // Arrays.sort впервые вызывается во время основных измерений.
        Random warmupRandom = new Random(29112026L ^ 0x5DEECE66DL);
        for (int run = 0; run < WARMUP_ROUNDS; run++) {
            int[] input = generateArray(WARMUP_SIZE, warmupRandom);
            bubbleSort(input.clone());
            mergeSort(input.clone());
        }

        Random random = new Random(29112026L);
        Random orderRandom = new Random(29112027L);
        double[][] averages = new double[SIZES.length][3];
        System.out.println("ОСНОВНЫЕ ЗАМЕРЫ (Arrays.sort без предварительного прогрева)");
        System.out.println("N\tПовтор\tАлгоритм\tПорядок\tВремя, нс");
        for (int sizeIndex = 0; sizeIndex < SIZES.length; sizeIndex++) {
            int size = SIZES[sizeIndex];
            long[] total = new long[3];
            for (int repeat = 0; repeat < REPETITIONS; repeat++) {
                int[] input = generateArray(size, random);
                // Алгоритмы получают копии одного массива, порядок запуска меняется.
                int[] order = {0, 1, 2};
                for (int i = order.length - 1; i > 0; i--) {
                    int j = orderRandom.nextInt(i + 1);
                    int saved = order[i];
                    order[i] = order[j];
                    order[j] = saved;
                }
                long[] times = new long[3];
                for (int algorithm : order) {
                    times[algorithm] = measure(input, algorithm);
                    total[algorithm] += times[algorithm];
                }
                // Печать происходит после измерения всех трёх алгоритмов.
                for (int position = 0; position < order.length; position++) {
                    int algorithm = order[position];
                    System.out.printf("%d\t%d\t%s\t%d\t%d%n", size, repeat + 1,
                            NAMES[algorithm], position + 1, times[algorithm]);
                }
            }
            for (int algorithm = 0; algorithm < 3; algorithm++) {
                averages[sizeIndex][algorithm] = total[algorithm] / (double) REPETITIONS;
            }
        }
        System.out.println("СРЕДНЕЕ ПО 5 ПОВТОРАМ, нс");
        System.out.printf("%8s %18s %18s %18s%n", "N", "Bubble", "Merge", "Arrays.sort");
        for (int i = 0; i < SIZES.length; i++) {
            System.out.printf(Locale.ROOT, "%8d %18.0f %18.0f %18.0f%n",
                    SIZES[i], averages[i][0], averages[i][1], averages[i][2]);
        }
    }

    private static int[] generateArray(int size, Random random) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) array[i] = random.nextInt(100000);
        return array;
    }

    private static long measure(int[] original, int algorithm) {
        int[] array = original.clone();
        long start = System.nanoTime();
        switch (algorithm) {
            case 0 -> bubbleSort(array);
            case 1 -> mergeSort(array);
            case 2 -> Arrays.sort(array);
            default -> throw new IllegalArgumentException("Неизвестный алгоритм");
        }
        long elapsed = System.nanoTime() - start;
        if (!isSorted(array)) throw new IllegalStateException("Ошибка: " + NAMES[algorithm]);
        return elapsed;
    }

    private static boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) return false;
        }
        return true;
    }


    public static void bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // Merge: O(N log N) по времени, O(N) дополнительной памяти.
    public static void mergeSort(int[] a) {
        mergeSort(a, 0, a.length, new int[a.length]);
    }

    private static void mergeSort(int[] a, int left, int right, int[] buffer) {
        if (right - left <= 1) return;
        int middle = left + (right - left) / 2;
        mergeSort(a, left, middle, buffer);
        mergeSort(a, middle, right, buffer);

        int i = left;
        int j = middle;
        int k = left;
        while (i < middle && j < right) {
            if (a[i] <= a[j]) buffer[k++] = a[i++];
            else buffer[k++] = a[j++];
        }
        while (i < middle) buffer[k++] = a[i++];
        while (j < right) buffer[k++] = a[j++];
        System.arraycopy(buffer, left, a, left, right - left);
    }
}
