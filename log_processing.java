// package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class log_processing {
    public static void main(String[] args) {
        File file = new File("log.txt");

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("tsAvg: " + tsAvg / total);
        System.out.println("tjAvg: " + tjAvg / total);

    }

}
