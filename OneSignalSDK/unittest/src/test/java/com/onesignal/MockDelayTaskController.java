package com.onesignal;

import org.jetbrains.annotations.NotNull;

public class MockDelayTaskController extends OSDelayTaskController {
    private int mockedRandomValue = 0;
    private long timeDifference = 0L;
    private boolean runOnSameThread = true;
    private String threadName = null;

    public MockDelayTaskController(OSLogger logger) {
        super(logger);
    }

    protected int getRandomNumber() {
        return mockedRandomValue;
    }

    public void delayTaskByRandom(@NotNull Runnable runnable) {
        if (runOnSameThread) {
            runnable.run();
        } else {
            long calledTime = System.currentTimeMillis();
            Runnable customRunnable = () -> {
                threadName = Thread.currentThread().getName();
                timeDifference =  System.currentTimeMillis() - calledTime;
                runnable.run();
            };
            super.delayTaskByRandom(customRunnable);
        }
    }

    public void setMockedRandomValue(int mockedRandomValue) {
        this.mockedRandomValue = mockedRandomValue;
    }

    public void setRunOnSameThread(boolean runOnSameThread) {
        this.runOnSameThread = runOnSameThread;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getTimeDifference() {
        return timeDifference;
    }
}
