package fr.wdes.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.wdes.Launcher;
import fr.wdes.logger;



public class ProcessMonitorThread extends Thread {
    private final JavaProcess process;
    public ProcessMonitorThread(final JavaProcess process) {
        this.process = process;
    }

    @Override
    public void run() {
        // The previous implementation closed the BufferedReader in a finally
        // *inside* the outer while loop, so on the second iteration we'd be
        // reading from an already-closed stream. Read the merged stdout/stderr
        // straight through until EOF, then clean up - this matches what
        // ProcessBuilder.redirectErrorStream(true) actually feeds us and means
        // we no longer drop crash dumps that arrive after the inner readLine
        // returned null once.
        final InputStreamReader reader = new InputStreamReader(process.getRawProcess().getInputStream());
        final BufferedReader buf = new BufferedReader(reader);
        try {
            String line;
            while ((line = buf.readLine()) != null) {
                logger.info("[Minecraft] " + line);
                if(line.contains("Setting user")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Joueur] Démarrage en cours ...");
                }
                else if(line.contains("LWJGL")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[LWJGL] Démarrage en cours ...");
                }
                else if(line.contains("Reloading ResourceManager")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Ressources] Redémarrage en cours ...");
                }
                else if(line.contains("Sound engine started")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Son] Démarré !!");
                }
                else if(line.contains("Created") && line.contains("textures")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Textures] Création en cours ...");
                }
                else if(line.contains("MCO Availability Checker")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Realms] Vérification ...");
                }
                else if(line.contains("Starting integrated minecraft server") || line.contains("Generating keypair")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Jeu] Bon jeu !!");
                }
                else if(line.contains("Stopping!")){
                    Launcher.getInstance().getLauncherPanel().progressBar.setText("[Jeu] Arrêt en cours ...");
                }

                process.getSysOutLines().add(line);
            }
        }
        catch(final IOException ex) {
            Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.WARNING, "Reading game stdout failed", ex);
        }
        finally {
            try {
                buf.close();
            }
            catch(final IOException ex) {
                Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Wait for the process itself to actually exit before notifying. EOF
        // on stdout can arrive before the process is reaped, and exitValue()
        // throws IllegalThreadStateException if it hasn't exited yet.
        try {
            process.getRawProcess().waitFor();
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        final JavaProcessRunnable onExit = process.getExitRunnable();

        // tryFireExit makes sure we don't double-dispatch when launchGame's
        // safeSetExitRunnable already raced us to it.
        if(onExit != null && process.tryFireExit())
            onExit.onJavaProcessEnded(process);
    }
}