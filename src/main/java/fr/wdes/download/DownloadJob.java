package fr.wdes.download;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import fr.wdes.Launcher;
import fr.wdes.logger;


public class DownloadJob {
	protected final Queue<Downloadable> remainingFiles = new ConcurrentLinkedQueue<Downloadable>();
    protected final List<Downloadable> allFiles = Collections.synchronizedList(new ArrayList<Downloadable>());
    protected final List<Downloadable> failures = Collections.synchronizedList(new ArrayList<Downloadable>());
    protected final List<Downloadable> successful = Collections.synchronizedList(new ArrayList<Downloadable>());
    protected final List<ProgressContainer> progressContainers = Collections.synchronizedList(new ArrayList<ProgressContainer>());
    protected final DownloadListener listener;
    protected String name;
    protected final boolean ignoreFailures;
    protected final AtomicInteger remainingThreads = new AtomicInteger();
    protected boolean started;

    public DownloadJob(final String name, final boolean ignoreFailures, final DownloadListener listener) {
        this(name, ignoreFailures, listener, null);
    }

    public DownloadJob(final String name, final boolean ignoreFailures, final DownloadListener listener, final Collection<Downloadable> files) {
        this.name = name;
        this.ignoreFailures = ignoreFailures;
        this.listener = listener;
        if(files != null)
            addDownloadables(files);
    }

    public void addDownloadables(final Collection<Downloadable> downloadables) {
        if(started)
            throw new IllegalStateException("Cannot add to download job that has already started");

        allFiles.addAll(downloadables);
        remainingFiles.addAll(downloadables);

        for(final Downloadable downloadable : downloadables) {
            progressContainers.add(downloadable.getMonitor());
            if(downloadable.getExpectedSize() == 0L)
                downloadable.getMonitor().setTotal(5242880L);
            else
                downloadable.getMonitor().setTotal(downloadable.getExpectedSize());
            downloadable.getMonitor().setJob(this);
        }
    }

    public void addDownloadables(final Downloadable[] downloadables) {
        if(started)
            throw new IllegalStateException("Cannot add to download job that has already started");

        for(final Downloadable downloadable : downloadables) {
            allFiles.add(downloadable);
            remainingFiles.add(downloadable);
            progressContainers.add(downloadable.getMonitor());
            if(downloadable.getExpectedSize() == 0L)
                downloadable.getMonitor().setTotal(5242880L);
            else
                downloadable.getMonitor().setTotal(downloadable.getExpectedSize());
            downloadable.getMonitor().setJob(this);
        }
    }

    public int getFailures() {
        return failures.size();
    }

    public String getName() {
        return name;
    }

    public float getProgress() {
        float current = 0.0F;
        float total = 0.0F;

        synchronized(progressContainers) {
            for(final ProgressContainer progress : progressContainers) {
                total += progress.getTotal();
                current += progress.getCurrent();
     //Launcher.getInstance().getLauncherPanel().getProgressBar().setString("Téléchargement de(s) : "+getName());
            }
        }

        float result = -1.0F;
        if(total > 0.0F)
            result = current / total;
        return result;
    }

    public int getSuccessful() {
        return successful.size();
    }

    public boolean isComplete() {
        return started && remainingFiles.isEmpty() && remainingThreads.get() == 0;
    }

    public boolean isStarted() {
        return started;
    }

    private void popAndDownload() {
        Downloadable downloadable;
        while((downloadable = remainingFiles.poll()) != null)
            if(downloadable.getNumAttempts() > 5) {
                if(!ignoreFailures)
                    failures.add(downloadable);
                logger.warn("Gave up trying to download " + downloadable.getUrl() + " for job '" + name + "'");
            
            }
            else
                try {
                	if(downloadable.getNumAttempts()>0){this.name = this.name + "'... (essai " + downloadable.getNumAttempts() + ")";}
                	logger.info("Téléchargement de :" + downloadable.getUrl() + " Tâche : '" + this.name );
                	Launcher.getInstance().getLauncherPanel().getProgressBar().setString("Téléchargement de :" + downloadable.getName() + " =>'" + this.name );
                    final String result = downloadable.download();
                    successful.add(downloadable);
                    
                    logger.info("Téléchargé : " + downloadable.getUrl() + " Tâche : '" + name + "'" + ": " + result);
                }
                catch(final Throwable t) {
                	logger.warn("Echec " + downloadable.getUrl() + " Tâche : '" + name + "'", t);
                    remainingFiles.add(downloadable);
                }
        if(remainingThreads.decrementAndGet() <= 0)
            listener.onDownloadJobFinished(this);
    }

    public boolean shouldIgnoreFailures() {
        return ignoreFailures;
    }

    public void startDownloading(final ThreadPoolExecutor executorService) {
        if(started)
            throw new IllegalStateException("Cannot start download job that has already started");
        started = true;

        if(allFiles.isEmpty()) {
        	logger.info("Download job '" + name + "' skipped as there are no files to download");
            listener.onDownloadJobFinished(this);
        }
        else {
            final int threads = executorService.getMaximumPoolSize();
            remainingThreads.set(threads);
            
            logger.info("Download job '" + name + "' started (" + threads + " threads, " + allFiles.size() + " files)");
            for(int i = 0; i < threads; i++)
                executorService.submit(new Runnable() {
                    public void run() {
                        DownloadJob.this.popAndDownload();
                    }
                });
        }
    }

    public void updateProgress() {
        listener.onDownloadJobProgressChanged(this);
    }
}