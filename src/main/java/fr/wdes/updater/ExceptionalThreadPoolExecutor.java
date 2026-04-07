package fr.wdes.updater;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.wdes.logger;


public class ExceptionalThreadPoolExecutor extends ThreadPoolExecutor {
    public class ExceptionalFutureTask<T> extends FutureTask<T> {

        public ExceptionalFutureTask(final Callable<T> callable) {
            super(callable);
        }

        public ExceptionalFutureTask(final Runnable runnable, final T value) {
            super(runnable, value);
        }

      
        protected void done() {
            try {
                this.get();
            }
            catch(final Exception t) {
            	logger.warn("Unhandled exception in executor " + this, t);
            }
        }
    }

    public ExceptionalThreadPoolExecutor(final int threadCount) {
        super(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    }

    
    protected void afterExecute(final Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if(t == null && r instanceof Future)
            try {
               
				final Future<?> future = (Future<?>) r;
                if(future.isDone() || future.isCancelled())
                    future.get();
            }
            catch(final Exception ce) {
            	Thread.currentThread().interrupt();
            }
    }

    
    protected <T> RunnableFuture<T> newTaskFor(final Callable<T> callable) {
        return new ExceptionalFutureTask<T>(callable);
    }

  
    protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
        return new ExceptionalFutureTask<T>(runnable, value);
    }
}