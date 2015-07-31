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
        final InputStreamReader reader = new InputStreamReader(process.getRawProcess().getInputStream());
        final BufferedReader buf = new BufferedReader(reader);
        String line = null;

        while(process.isRunning() )
            try {
                while((line = buf.readLine()) != null) {
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
                //Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally {
                try {
                    buf.close();
                }
                catch(final IOException ex) {
                    Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        final JavaProcessRunnable onExit = process.getExitRunnable();

        if(onExit != null)
            onExit.onJavaProcessEnded(process);
    }
}