package sbu.cs.CalculatePi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PiCalculator {

    /**
     * Calculate pi and represent it as a BigDecimal object with the given floating point number (digits after . )
     * There are several algorithms designed for calculating pi, it's up to you to decide which one to implement.
     Experiment with different algorithms to find accurate results.

     * You must design a multithreaded program to calculate pi. Creating a thread pool is recommended.
     * Create as many classes and threads as you need.
     * Your code must pass all of the test cases provided in the test folder.

     * @param floatingPoint the exact number of digits after the floating point
     * @return pi in string format (the string representation of the BigDecimal object)
     */


    // Set the number of threads according to available processors to optimize the use of system resources
    private static final int AVAILABLE_THREADS = Runtime.getRuntime ().availableProcessors ();

    public String calculate(int floatingPoint) {
        // Creates an ExecutorService to manage multiple threads
        ExecutorService threadPool = Executors.newFixedThreadPool(AVAILABLE_THREADS);
        // To hold the cumulative sum as an atomic reference safely across threads
        AtomicReference<BigDecimal> sum = new AtomicReference<>(BigDecimal.ZERO);
        // The precision limit for calculation
        BigDecimal limit = BigDecimal.ONE.scaleByPowerOfTen (-floatingPoint);

        // Each thread calculates terms of the BBP series starting from a different index which is
        //              thread's index, and their increments by thread nums
        for (int i = 0 ; i < AVAILABLE_THREADS ; i++) {
            threadPool.execute(new Calculator(i, floatingPoint, limit, sum, AVAILABLE_THREADS));
        }
        // Shutdown the executor and wait for all threads to finish
        threadPool.shutdown();
        try {
            // Wait up to 10 minutes for all tasks to complete and force shutdown if not all threads finish in time
            if (!threadPool.awaitTermination(10, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Shutdown if interrupted
            threadPool.shutdownNow();
        }
        // Calculate the final value of Pi by rounding the sum to the specified number of digits
        BigDecimal pi = sum.get().setScale(floatingPoint, RoundingMode.HALF_DOWN);
        return pi.toString();
    }

    public static class Calculator implements Runnable {
        private final int threadIndex;
        private final int floatingPoint;
        private final BigDecimal limit;
        private final AtomicReference<BigDecimal> sum;
        private final int numThreads;

        public Calculator(int threadIndex, int floatingPoint, BigDecimal limit, AtomicReference<BigDecimal> sum, int numThreads) {
            this.threadIndex = threadIndex;
            this.floatingPoint = floatingPoint;
            this.limit = limit;
            this.sum = sum;
            this.numThreads = numThreads;
        }

        // The run method that will be executed by each thread
        @Override
        public void run() {
            BigDecimal localSum = BigDecimal.ZERO;
            for (int k = threadIndex; ; k += numThreads) {
                // Calculate the value of the term at index k using the computeTerm method
                BigDecimal term = computeTerm(k, floatingPoint);
                localSum = localSum.add(term);
                // Check if term is smaller than floating number limit  and stop the calculation loop
                if (term.abs().compareTo(limit) < 0) {
                    break;
                }
            }
            // Safely add the localSum to the global sum to assure race condition not happening
            synchronized (sum) {
                BigDecimal finalLocalSum = localSum;
                sum.updateAndGet(currentSum -> currentSum.add(finalLocalSum));
            }
        }

        // Method to compute the term at index k using the BBP formula
        private BigDecimal computeTerm(int k, int floatingPoint) {
            BigDecimal numerator = BigDecimal.valueOf(4).divide(BigDecimal.valueOf(8L * k + 1), floatingPoint + 5, RoundingMode.HALF_DOWN)
                    .subtract(BigDecimal.valueOf(2).divide(BigDecimal.valueOf(8L * k + 4), floatingPoint + 5, RoundingMode.HALF_DOWN))
                    .subtract(BigDecimal.ONE.divide(BigDecimal.valueOf(8L * k + 5), floatingPoint + 5, RoundingMode.HALF_DOWN))
                    .subtract(BigDecimal.ONE.divide(BigDecimal.valueOf(8L * k + 6), floatingPoint + 5, RoundingMode.HALF_DOWN));
            BigDecimal denominator = BigDecimal.valueOf(16).pow(k);
            return numerator.divide(denominator, floatingPoint + 5, RoundingMode.HALF_EVEN);
        }
    }

    public static void main(String[] args) {
        // test
        Scanner scanner = new Scanner(System.in);
        PiCalculator piCalculator = new PiCalculator();
        while (true) {
            System.out.print ("Enter the number of Digits after the Floating Point: ");
            int floatingPoint = scanner.nextInt ();

            if (floatingPoint == 0) {
                //Closes the program if the entered input is 0
                return;
            }

            long startTime = System.nanoTime (); //to measure process's time
            String pi = piCalculator.calculate(floatingPoint); //to calculates π
            long endTime = System.nanoTime (); //to measure process's time

            System.out.println("π: " + pi);
            System.out.println ("Time taken: " + (endTime - startTime) / 1_000_000 + " ms\n\n"); //time taken to calculate π
        }
    }
}
