import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedbackServer {
    // Shared memory simulation using static variables
    private static final AtomicInteger goodCount = new AtomicInteger(0);
    private static final AtomicInteger averageCount = new AtomicInteger(0);
    private static final AtomicInteger poorCount = new AtomicInteger(0);
    private static final AtomicInteger totalCount = new AtomicInteger(0);
    
    // Semaphore simulation using ReentrantLock
    private static final ReentrantLock lock = new ReentrantLock();
    
    public static void main(String[] args) {
        System.out.println("Mess Feedback Server started");
        System.out.println("Shared memory initialized - All counters reset to 0");
        
        // Keep server running and display status
        while (true) {
            try {
                Thread.sleep(5000);
                displayStatus();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    public static void submitFeedback(String type) {
        lock.lock();
        try {
            switch (type.toLowerCase()) {
                case "good":
                    goodCount.incrementAndGet();
                    break;
                case "average":
                    averageCount.incrementAndGet();
                    break;
                case "poor":
                    poorCount.incrementAndGet();
                    break;
                default:
                    System.out.println("Invalid feedback type: " + type);
                    return;
            }
            totalCount.incrementAndGet();
            System.out.println(type + " feedback submitted!");
        } finally {
            lock.unlock();
        }
    }
    
    public static String getStatus() {
        lock.lock();
        try {
            return String.format("{\"good\":%d,\"average\":%d,\"poor\":%d,\"total\":%d}",
                    goodCount.get(), averageCount.get(), poorCount.get(), totalCount.get());
        } finally {
            lock.unlock();
        }
    }
    
    private static void displayStatus() {
        lock.lock();
        try {
            System.out.printf("Current Feedback - Good: %d, Average: %d, Poor: %d, Total: %d%n",
                    goodCount.get(), averageCount.get(), poorCount.get(), totalCount.get());
        } finally {
            lock.unlock();
        }
    }
}