import java.io.*;
import java.util.concurrent.locks.*;

// Worker thread class
class FileWorker extends Thread {
    private BufferedReader reader;
    private Lock lock;
    private int id;
    private static int totalWordCount = 0; // shared resource

    public FileWorker(BufferedReader reader, Lock lock, int id) {
        this.reader = reader;
        this.lock = lock;
        this.id = id;
    }

    @Override
    public void run() {
        System.out.println("Thread " + id + " started.");
        try {
            String line;
            while (true) {
                lock.lock(); // lock before accessing shared BufferedReader
                try {
                    line = reader.readLine();
                    if (line == null) break; // end of file
                } finally {
                    lock.unlock();
                }

                // Process the line (word count)
                String[] words = line.trim().split("\\s+");
                int count = (line.isEmpty()) ? 0 : words.length;

                // Update shared word counter safely
                synchronized (FileWorker.class) {
                    totalWordCount += count;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Thread " + id + " finished.");
    }

    public static int getTotalWordCount() {
        return totalWordCount;
    }
}

public class MultiThreadFileProcessor {
    public static void main(String[] args) {
        String fileName = "largefile.txt"; // replace with your file path

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            Lock lock = new ReentrantLock();

            // Create multiple threads to process the file concurrently
            FileWorker t1 = new FileWorker(reader, lock, 1);
            FileWorker t2 = new FileWorker(reader, lock, 2);
            FileWorker t3 = new FileWorker(reader, lock, 3);

            // Start threads (NEW -> RUNNABLE -> RUNNING)
            t1.start();
            t2.start();
            t3.start();

            // Wait for threads to finish (TERMINATED)
            t1.join();
            t2.join();
            t3.join();

            System.out.println("Total word count: " + FileWorker.getTotalWordCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

