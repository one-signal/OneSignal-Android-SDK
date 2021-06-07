package com.onesignal

import android.os.Build
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

open class OSDelayTaskController(private val logger: OSLogger) {
    private val maxDelay = 25
    private val minDelay = 0

    private var scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor

    init {
        scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1, JobThreadFactory())
    }

    protected open fun getRandomNumber(): Int {
        var randomNum = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            randomNum = ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1)
        }
        return randomNum
    }

    open fun delayTaskByRandom(runnable: Runnable) {
        val randomNum = getRandomNumber()

        logger.debug("OSDelayTaskController delaying task $randomNum second from thread: ${Thread.currentThread()}")
        scheduledThreadPoolExecutor.schedule(runnable, randomNum.toLong(), TimeUnit.SECONDS)
    }

    fun shutdownNow() {
        scheduledThreadPoolExecutor.shutdownNow()
    }

    private class JobThreadFactory : ThreadFactory {
        private val delayThreadName = "ONE_SIGNAL_DELAY"

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, delayThreadName)
        }
    }
}
