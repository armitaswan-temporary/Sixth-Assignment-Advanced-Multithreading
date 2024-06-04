package sbu.cs.Semaphore;

import java.util.concurrent.Semaphore;

public class Operator extends Thread {
    private String name;
    private Semaphore semaphore;
    public Operator(String name, Semaphore semaphore) {
        this.name = name;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            // Acquire a permit before entering the critical section
            semaphore.acquire();
            System.out.println("Operator <" + name + "> accessed to Resources.");
            // Critical section - a Maximum of 2 operators can access the resource concurrently
            for (int i = 0; i < 10; i++) {
                Resource.accessResource();
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Operator <" + name + "> finished their access from Resources.");
            semaphore.release();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
