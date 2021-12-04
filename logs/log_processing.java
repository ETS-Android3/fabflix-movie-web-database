// package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class log_processing {
    public static void main(String[] args) {
        // INSERT PATH TO LOG HERE
        File file = new File("---PATH---");

        long tsAvg = 0;
        long tjAvg = 0;
        long tsTime = 0;
        long tjTime = 0;
        int total = 0;
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
                String[] samples = line.split(",");
                try {
                    tsTime = Long.valueOf(samples[0]);
                    tjTime = Long.valueOf(samples[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                tsAvg += tsTime;
                tjAvg += tjTime;
                total++;
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        double tsA = tsAvg / total;
        double tjA = tjAvg / total;

        System.out.println("tsAvg: " + tsAvg / total + " (" + tsA / 1000000 + " ms)");
        System.out.println("tjAvg: " + tjAvg / total + " (" + tjA / 1000000 + " ms)");

    }

}
