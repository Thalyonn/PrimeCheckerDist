import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Master {
  static final int LIMIT = 10000000;

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.print("ERROR: No IP specified. Exiting.");
      return;
    }
    String slaveAddress = args[0];
    int slavePort = 8080;
    AtomicInteger primes = new AtomicInteger();
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter the starting point of integers to check: ");
    int startNum = scanner.nextInt();

    System.out.print("Enter the ending point of integers to check: ");
    int endNum = scanner.nextInt();

    System.out.print("Enter the number of threads to use: ");
    int numThreads = scanner.nextInt();

    int end = (startNum + endNum) / 2;

    try (Socket socket = new Socket(slaveAddress, slavePort);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream ack = new DataOutputStream(socket.getOutputStream())) {

      out.writeLong(end);
      out.writeInt(endNum);
      out.writeInt(numThreads);
      out.flush();


      long startTime = System.nanoTime();
      int start = startNum;
      int splitSize = (end - start + 1) / numThreads;
      List<Thread> threads = new ArrayList<>();

      Thread slaveThread = new Thread(() -> {
        try {
        @SuppressWarnings("unchecked")
        int slavePrimes = in.readInt();
        primes.addAndGet(slavePrimes);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      threads.add(slaveThread);
      slaveThread.start();

      for (int i = 0; i < numThreads; i++) {
        final int startThread = start + i * splitSize;
        final int endThread;

        if (i == numThreads - 1) {
          endThread = end;
        } else {
          endThread = startThread + splitSize - 1;
        }

        Thread thread = new Thread(() -> {
          int threadStart = startThread;
          int threadEnd = Math.min(endThread, end);
          int threadPrimes = 0;

          for (int currentNum = threadStart; currentNum <= threadEnd; currentNum++) {
            if (checkPrime(currentNum)) {
              primes.incrementAndGet();
              
            } else {

            }
          }

      });

      threads.add(thread);
      thread.start();
    }
    try {
      for (Thread thread : threads) {
        thread.join();

      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000;

    System.out.println(primes.get() + " primes were found.");
    System.out.println("Runtime is " + duration + " microseconds.");

    scanner.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static boolean checkPrime(final int n) {
    for (int i = 2; i * i <= n; i++) {
      if (n % i == 0) {
        return false;
      }
    }
    return true;
  }
}
