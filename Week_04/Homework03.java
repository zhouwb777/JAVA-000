package week03;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 一个简单的代码参考：
 */
public class Homework03 {
    
    public static void main(String[] args) {
        
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        
        int result = sum(); //这是得到的返回值
        
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);
         
        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
        
        // 然后退出main线程
    }
    
    private static int sum() {
        return fibo(36);
    }
    
    private static int fibo(int a) {
        if ( a < 2) 
            return 1;
        return fibo(a-1) + fibo(a-2);
    }

    /**
     * join()
     * */
    @Test
    public void test01() throws InterruptedException {
        AtomicInteger result = new AtomicInteger();
        Thread thread = new Thread(() -> {
            result.set(sum());
        });
        thread.start();
        thread.join();
        System.out.println("异步计算结果为：" + result.get());
    }

    /**
     * Synchronized
     * */
    @Test
    public void test02() throws InterruptedException {
        Object lock = new Object();
        AtomicInteger result = new AtomicInteger();
        new Thread(() -> {
            synchronized (lock) {
                result.set(sum());
                lock.notifyAll();
            }
        }).start();

        synchronized (lock) {
            if(result.get() == 0){
                lock.wait();
            }
            System.out.println("异步计算结果为：" + result.get());
        }
    }

    /**
     * Lock Condition
     * */
    @Test
    public void test03() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        AtomicInteger result = new AtomicInteger();
        new Thread(() -> {
            lock.lock();
            try {
                result.set(sum());
            }finally {
                condition.signalAll();
                lock.unlock();
            }
        }).start();

        lock.lock();
        if(result.get() == 0){
            condition.await();
        }
        System.out.println("异步计算结果为：" + result.get());
        lock.unlock();
    }

    /**
     * CountDownLatch
     * */
    @Test
    public void test04() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger();
        new Thread(() -> {
            result.set(sum());
            countDownLatch.countDown();
        }).start();

        countDownLatch.await();
        System.out.println("异步计算结果为：" + result.get());
    }

    /**
     * CyclicBarrier
     * */
    @Test
    public void test05() throws BrokenBarrierException, InterruptedException {
        AtomicInteger result = new AtomicInteger();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2,()->{
            System.out.println("异步计算结果为：" + result.get());
        });
        new Thread(() -> {
            result.set(sum());
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
        cyclicBarrier.await();
    }

    /**
     * Semaphore
     * */
    @Test
    public void test06() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        AtomicInteger result = new AtomicInteger();
        new Thread(() -> {
            result.set(sum());
            semaphore.release();
        }).start();

        semaphore.acquire();
        System.out.println("异步计算结果为：" + result.get());
    }


    /**
     * Future
     * */
    @Test
    public void test07() throws ExecutionException, InterruptedException {
        Callable<Integer> task = ()->sum();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> future = executorService.submit(task);
        // 确保  拿到result 并输出
        Integer result = future.get();
        System.out.println("异步计算结果为："+result);
    }

    /**
     * FutureTask
     * */
    @Test
    public void test08() throws ExecutionException, InterruptedException {
        Callable<Integer> task = ()->sum();
        FutureTask<Integer> futureTask = new FutureTask(task);
//        new Thread(futureTask).start();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(futureTask);
        // 确保  拿到result 并输出
        Integer result = futureTask.get();
        System.out.println("异步计算结果为："+result);
    }

    /**
     * CompletableFuture
     * */
    @Test
    public void test09() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(Homework03::sum);
        Integer result = completableFuture.get();
        System.out.println("异步计算结果为："+result);
    }

    /**
     * LockSupport
     * */
    @Test
    public void test10() {
        AtomicInteger sum = new AtomicInteger();
        Thread currentThread = Thread.currentThread();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1, () -> {
            // 如果先unpark，后park，也没有关系，
            // 会把下一次park的线程唤醒
            LockSupport.unpark(currentThread);
            System.out.println("异步计算结果为：" + sum.get());
        });

        new Thread(() -> {
            try {
                sum.set(sum());
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        LockSupport.park();
    }
}
