public class Test {
    public static void main(String[] args) throws InterruptedException {
        //Test for small scale
        int[][] arr3 = {
                {3, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 1},
                {1, 0, 0, 1, 0, 0, 1, 2},
                {0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 1},
                {0, 0, 0, 0, 1, 0, 1, 1},
                {0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1}
        };
        int[][] arr4 = {
                {0, 0, 3, 1, 1, 0, 0, 1},
                {0, 1, 0, 0, 1, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 1, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 2, 0, 1, 0, 0, 0, 1},
                {0, 0, 1, 0, 0, 1, 0, 1}
        };
        int[][] arr5 = {
                {1, 0, 0, 0, 1, 0, 2, 0},
                {0, 0, 1, 1, 0, 0, 0, 1},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0, 0, 1, 1},
                {0, 0, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 0},
                {3, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 1, 0, 1}
        };
        //Initialize the map so we can set it to whichever array we pass in

        // CONTROLLERS

        // LOGIC
        float avg = 0;
        int max = 0;
        int min = 20;
        float avg2 = 0;
        int max2 = 0;
        int min2 = 40;
        for (int i = 0; i < 8 * 8 * 4; i++) {

            if (arr3[(i / 4) / 8][(i / 4) % 8] != 1) {
                Map map = new Map(arr3);

                SearchAndRescueController sar = new SearchAndRescueController(map);
                LocalizationController localization = new LocalizationController(map, sar);
                int x = localization.run(i);
                if (x < min) {
                    min = x;
                }
                if (x > max) {
                    max = x;
                }
                avg += (float) x / (8 * 8 * 4);

                x = sar.run();
                if (x < min2) {
                    min2 = x;
                }
                if (x > max2) {
                    max2 = x;
                }
                avg2 += (float) x / (8 * 8 * 4);
            }
        }

        System.out.println("LOC Min: " + min + " Max: " + max + " Avg: " + avg);
        System.out.println("SAR Min: " + min2 + " Max: " + max2 + " Avg: " + avg2);
    }
}
