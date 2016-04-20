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
}
