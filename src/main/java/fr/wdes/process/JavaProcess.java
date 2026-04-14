package fr.wdes.process;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
@SuppressWarnings("unused")
public class JavaProcess {
    private static final int MAX_SYSOUT_LINES = 5;
    private final List<String> commands;
    private final Process process;
    private final LimitedCapacityList<String> sysOutLines = new LimitedCapacityList<String>(String.class, 5);
    private JavaProcessRunnable onExit;
    /**
     * Guards against {@link #onExit} being fired twice when the process
     * happens to exit between {@link ProcessMonitorThread} EOF-ing stdout and
     * the launcher calling {@link #safeSetExitRunnable}.
     */
    private final AtomicBoolean exitFired = new AtomicBoolean(false);
    private final ProcessMonitorThread monitor = new ProcessMonitorThread(this);

    public JavaProcess(final List<String> commands, final Process process) {
        this.commands = commands;
        this.process = process;

        monitor.start();
    }

    public int getExitCode() {
        try {
            return process.exitValue();
        }
        catch(final IllegalThreadStateException ex) {
            ex.fillInStackTrace();
            throw ex;
        }
    }

    public JavaProcessRunnable getExitRunnable() {
        return onExit;
    }

    public Process getRawProcess() {
        return process;
    }

    public String getStartupCommand() {
        return process.toString();
    }

    public List<String> getStartupCommands() {
        return commands;
    }

    public LimitedCapacityList<String> getSysOutLines() {
        return sysOutLines;
    }

    public boolean isRunning() {
        try {
            process.exitValue();
        }
        catch(final IllegalThreadStateException ex) {
            return true;
        }

        return false;
    }

    public void safeSetExitRunnable(final JavaProcessRunnable runnable) {
        setExitRunnable(runnable);

        if(!isRunning() && runnable != null && exitFired.compareAndSet(false, true))
            runnable.onJavaProcessEnded(this);
    }

    /**
     * Called from {@link ProcessMonitorThread} once the process has actually
     * exited. Returns false if a prior path already dispatched the runnable
     * (typically {@link #safeSetExitRunnable}), so the caller should skip.
     */
    boolean tryFireExit() {
        return exitFired.compareAndSet(false, true);
    }

    public void setExitRunnable(final JavaProcessRunnable runnable) {
        onExit = runnable;
    }

    public void stop() {
        process.destroy();
    }

    @Override
    public String toString() {
        return "JavaProcess[commands=" + commands + ", isRunning=" + isRunning() + "]";
    }
}