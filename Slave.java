import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

public class Slave {
  public static void main(String[] args) {
    int port = 8080;

    try (ServerSocket serverSocket = new ServerSocket(port);
        Socket masterSocket = serverSocket.accept();
        DataOutputStream dataOut = new DataOutputStream(masterSocket.getOutputStream());
        DataInputStream dataIn = new DataInputStream(masterSocket.getInputStream());
        DataInputStream ack = new DataInputStream(masterSocket.getInputStream())) {

      System.out.println("Master connected to the slave.");

      AtomicLong receivedStart = new AtomicLong(dataIn.readLong() + 1);
      System.out.println("Start: " + receivedStart.get());
      int limit = dataIn.readInt();
      int numThreads = dataIn.readInt();

      AtomicInteger primes = new AtomicInteger();

      int stride = (limit + numThreads - 1) / numThreads;

      List<Thread> threads = new ArrayList<>();

      for (int i = 0; i < numThreads; i++) {
        final int localStart = (int) receivedStart.getAndAdd(stride);
        final int localEnd = Math.min(localStart + stride - 1, limit);

        Thread thread = new Thread(() -> {

         

          for (int currentNum = localStart; currentNum <= localEnd; currentNum++) {
            if (checkPrime(currentNum)) {
              primes.incrementAndGet();
            
            }
            
          }
        });

        threads.add(thread);
        thread.start();
      }

      for (Thread thread : threads) {
          try {
              thread.join();
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      }

      dataOut.writeInt(primes.get());

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
