import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class demo_log_processing {
    public static void main(String[] args) {
        File aws_slave = new File("logs/scaled_aws_slave.txt");
        File aws_master = new File("logs/scaled_aws_master.txt");
        File gcp_slave = new File("logs/scaled_gcp_slave.txt");
        File gcp_master = new File("logs/scaled_gcp_master.txt");
        File single_https = new File("logs/single_https.txt");

        long aws_tsAvg = 0;
        long aws_tjAvg = 0;
        long aws_tsTime = 0;
        long aws_tjTime = 0;
        int aws_total = 0;
        try {
            Scanner scanner_aws_master = new Scanner(aws_master);
            while (scanner_aws_master.hasNextLine()) {
                String line = scanner_aws_master.nextLine();
                System.out.println(line);
                String[] samples = line.split(",");
                try {
                    aws_tsTime = Long.valueOf(samples[0]);
                    aws_tjTime = Long.valueOf(samples[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                aws_tsAvg += aws_tsTime;
                aws_tjAvg += aws_tjTime;
                aws_total++;
            }

            scanner_aws_master.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scanner_aws_slave = new Scanner(aws_slave);
            while (scanner_aws_slave.hasNextLine()) {
                String line = scanner_aws_slave.nextLine();
                System.out.println(line);
                String[] samples = line.split(",");
                try {
                    aws_tsTime = Long.valueOf(samples[0]);
                    aws_tjTime = Long.valueOf(samples[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                aws_tsAvg += aws_tsTime;
                aws_tjAvg += aws_tjTime;
                aws_total++;
            }

            scanner_aws_slave.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long gcp_tsAvg = 0;
        long gcp_tjAvg = 0;
        long gcp_tsTime = 0;
        long gcp_tjTime = 0;
        int gcp_total = 0;
        try {
            Scanner scanner_gcp_master = new Scanner(gcp_master);
            while (scanner_gcp_master.hasNextLine()) {
                String line = scanner_gcp_master.nextLine();
                System.out.println(line);
                String[] samples = line.split(",");
                try {
                    gcp_tsTime = Long.valueOf(samples[0]);
                    gcp_tjTime = Long.valueOf(samples[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                gcp_tsAvg += gcp_tsTime;
                gcp_tjAvg += gcp_tjTime;
                gcp_total++;
            }

            scanner_gcp_master.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scanner_gcp_slave = new Scanner(gcp_slave);
            while (scanner_gcp_slave.hasNextLine()) {
                String line = scanner_gcp_slave.nextLine();
                System.out.println(line);
                String[] samples = line.split(",");
                try {
                    gcp_tsTime = Long.valueOf(samples[0]);
                    gcp_tjTime = Long.valueOf(samples[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                gcp_tsAvg += gcp_tsTime;
                gcp_tjAvg += gcp_tjTime;
                gcp_total++;
            }

            scanner_gcp_slave.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long single_tsAvg = 0;
        long single_tjAvg = 0;
        long single_tsTime = 0;
        long single_tjTime = 0;
        int single_total = 0;

        try {
            Scanner scanner_single_https = new Scanner(single_https);
            while (scanner_single_https.hasNextLine()) {
                String line = scanner_single_https.nextLine();
                System.out.println(line);
                String[] samples = line.split(",");
                try {
                    single_tsTime = Long.valueOf(samples[0]);
                    single_tjTime = Long.valueOf(samples[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                single_tsAvg += single_tsTime;
                single_tjAvg += single_tjTime;
                single_total++;
            }

            scanner_single_https.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        double aws_tsA = aws_tsAvg / aws_total;
        double aws_tjA = aws_tjAvg / aws_total;

        System.out.println("\nAWS tsAvg: " + aws_tsAvg / aws_total + " (" + aws_tsA / 1000000 + " ms)");
        System.out.println("AWS tjAvg: " + aws_tjAvg / aws_total + " (" + aws_tjA / 1000000 + " ms)\n");

        double gcp_tsA = gcp_tsAvg / gcp_total;
        double gcp_tjA = gcp_tjAvg / gcp_total;

        System.out.println("GCP tsAvg: " + gcp_tsAvg / gcp_total + " (" + gcp_tsA / 1000000 + " ms)");
        System.out.println("GCP tjAvg: " + gcp_tjAvg / gcp_total + " (" + gcp_tjA / 1000000 + " ms)\n");

        double single_tsA = single_tsAvg / single_total;
        double single_tjA = single_tjAvg / single_total;

        System.out.println("Single Instance tsAvg: " + single_tsAvg / single_total + " (" + single_tsA / 1000000 + " ms)");
        System.out.println("Single Instance tjAvg: " + single_tjAvg / single_total + " (" + single_tjA / 1000000 + " ms)\n");

    }

}