package org.fabric3.threadpool;

import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ThreadPoolTestCase extends TestCase {
    public void testSomething(){

    }
    
    public void tesstIt() throws Exception {

//        PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>(10000, new TestComparator());
        LinkedBlockingQueue queue = new LinkedBlockingQueue(10000);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, Long.MAX_VALUE, TimeUnit.DAYS, queue);
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler(){
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("----------------------------------><---------------------------------------------");
                throw new RejectedExecutionException();
            }
        });

        for (int i = 0; i < 1000; i++) {

            new Thread(new Runnable() {
                public void run() {
                    execute(executor);
                }
            }).start();
        }
        Thread.sleep(1000000000);
    }

    private void execute(ThreadPoolExecutor executor) {
        int x = 0;
        for (int i = 0; i < 100000; i++) {
            int priority;
            if (x == 1000) {
                priority = 2;
                x = 0;
            } else {
                priority = 1;
                x++;
            }
            PrioritizedRunnable runnable = new PrioritizedRunnableImpl(i, priority);
            executor.execute(runnable);
        }
    }

    private class TestComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof PrioritizedRunnable && o2 instanceof PrioritizedRunnable) {
                return (((PrioritizedRunnable) o1).getPriority() - (((PrioritizedRunnable) o2).getPriority()));
            }
            return 0;
        }
    }

    private interface PrioritizedRunnable extends Runnable {

        int getPriority();
    }

    private class PrioritizedRunnableImpl implements PrioritizedRunnable {
        private int count;
        private int priority;

        private PrioritizedRunnableImpl(int count, int priority) {
            this.count = count;
            this.priority = priority;
        }

        public void run() {
            System.out.println(priority + "----->" + count);
        }

        public int getPriority() {
            return priority;
        }
    }
}
