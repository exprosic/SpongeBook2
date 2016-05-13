package com.example.exprosic.spongebook2.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.CountDownLatch;

/**
 * Created by exprosic on 4/17/2016.
 */
public class Sync {
    public static long newTimeStamp() {
        return System.currentTimeMillis();
    }

    public static class LoopRunnableThread extends Thread {
        private static class RunnableHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                msg.getCallback().run();
            }
        }

        private Handler mHandler;
        private CountDownLatch mHandlerPreparedLatch = new CountDownLatch(1);

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new RunnableHandler();
            mHandlerPreparedLatch.countDown();
            Looper.loop();
        }

        public void addTask(Runnable runnable) {
            while (mHandlerPreparedLatch.getCount() > 0)
                try {
                    mHandlerPreparedLatch.await();
                } catch (InterruptedException e) {
                    /* ignore */
                }
            mHandler.post(runnable);
        }
    }

    public static LoopRunnableThread newLoopRunnableThreadHandler() {
        LoopRunnableThread thread = new LoopRunnableThread();
        thread.start();
        return thread;
    }

    public static void awaitIgnoreInterrupt(CountLatch latch) {
        while (latch.getCount() > 0) {
            try {
                latch.await();
                break;
            } catch (InterruptedException e) {
                /* continue */
            }
        }
    }

    public static void awaitIgnoreInterrupt(CountDownLatch latch) {
        while (latch.getCount() > 0) {
            try {
                latch.await();
                break;
            } catch (InterruptedException e) {
                /* continue */
            }
        }
    }

    public static class CountLatch {
        private int mCount;
        private CountDownLatch latch;

        public CountLatch (int count) {
            mCount = count;
            latch = new CountDownLatch(count>0?1:0);
        }

        public void countUp() {
            addCount(1);
        }

        public void countDown() {
            addCount(-1);
        }

        public synchronized void addCount(int delta) {
            Debugging.myAssert(mCount>0, "addCount after being 0");
            mCount += delta;
            if (mCount == 0)
                latch.countDown();
        }

        public void await() throws InterruptedException {
            latch.await();
        }

        public int getCount() {
            return mCount;
        }
    }
}
