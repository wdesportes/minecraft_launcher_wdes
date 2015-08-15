package wdes.fr.launch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



class JP {
	private final List<String> commands;
	private final Process process;
	private final CL<String> sysOutLines = new CL<String>(
			String.class, 5);
	private JPR onExit;
	private final JM monitor = new JM(this);

	public JP(final List<String> commands, final Process process) {
		this.commands = commands;
		this.process = process;

		monitor.start();

	}

	public int getExitCode() {
		try {
			return process.exitValue();
		} catch (final IllegalThreadStateException ex) {
			ex.fillInStackTrace();
			throw ex;
		}
	}

	public JPR getExitRunnable() {
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

	public CL<String> getSysOutLines() {
		return sysOutLines;
	}

	public boolean isRunning() {
		try {
			process.exitValue();
		} catch (final IllegalThreadStateException ex) {
			return true;
		}

		return false;
	}

	public void safeSetExitRunnable(final JPR runnable) {
		setExitRunnable(runnable);

		if (!isRunning() && runnable != null)
			runnable.onJavaProcessEnded(this);
	}

	public void setExitRunnable(final JPR runnable) {
		onExit = runnable;
	}

	public void stop() {
		process.destroy();
	}

}
class JPL {
	private final String jvmPath;
	private final List<String> commands;
	private File directory;

	public JPL(String jvmPath, final String[] commands) {
		if (jvmPath == null) {
			Os.getCurrentPlatform();
			jvmPath = Os.getJavaDir();
		}
		this.jvmPath = jvmPath;
		this.commands = new ArrayList<String>(commands.length);
		addCommands(commands);
	}

	public void addCommands(final String[] commands) {
		this.commands.addAll(Arrays.asList(commands));
	}

	public void addSplitCommands(final String commands) {
		addCommands(commands.split(" "));
	}

	public JPL directory(final File directory) {
		this.directory = directory;

		return this;
	}

	public List<String> getCommands() {
		return commands;
	}

	public File getDirectory() {
		return directory;
	}

	public List<String> getFullCommands() {
		final List<String> result = new ArrayList<String>(commands);
		result.add(0, getJavaPath());
		return result;
	}

	protected String getJavaPath() {
		return jvmPath;
	}

	public JP start()  {
		final List<String> full = getFullCommands();
		try {
	 new JP(full, new ProcessBuilder(full).directory(directory).redirectErrorStream(true).start());
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		return null;
	}

}
abstract interface JPR {
	public abstract void onJavaProcessEnded(JP paramJavaProcess);
	
	
}
class CL<T> {
	private final T[] items;
	private final Class<? extends T> clazz;
	private final ReadWriteLock locks = new ReentrantReadWriteLock();
	private int size;
	private int head;

	
	@SuppressWarnings("unchecked")
	public CL(final Class<? extends T> clazz, final int maxSize) {
		this.clazz = clazz;
		items = (T[]) Array.newInstance(clazz, maxSize);
	}

	public T add(final T value) {
		locks.writeLock().lock();

		items[head] = value;
		head = (head + 1) % getMaxSize();
		if (size < getMaxSize())
			size += 1;

		locks.writeLock().unlock();
		return value;
	}


	@SuppressWarnings("unchecked")
	public T[] getItems() {
		final T[] result = (T[]) Array.newInstance(clazz, size);

		locks.readLock().lock();
		for (int i = 0; i < size; i++) {
			int pos = (head - size + i) % getMaxSize();
			if (pos < 0)
				pos += getMaxSize();
			result[i] = items[pos];
		}
		locks.readLock().unlock();

		return result;
	}

	public int getMaxSize() {
		locks.readLock().lock();
		final int result = items.length;
		locks.readLock().unlock();
		return result;
	}

	public int getSize() {
		locks.readLock().lock();
		final int result = size;
		locks.readLock().unlock();
		return result;
	}
}
class Console {
public static void log(String text){
System.out.println("[Wdes]"+text);
}
}