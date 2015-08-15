package wdes.fr.launch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import wdes.fr.Mise_a_jour;
import wdes.fr.Util;

public class JM extends Thread {
	public static JP process;
	
	public JM(final JP process) {
		JM.process = process;
	}

	
	public void run() {
		Mise_a_jour.state_msg = "Etat";
		final InputStreamReader reader = new InputStreamReader(process
				.getRawProcess().getInputStream());
		final BufferedReader buf = new BufferedReader(reader);
		String line = null;
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(Util.getWorkingDirectory()+"/"+"log.txt", true)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (process.isRunning())
			try {
				Mise_a_jour.subtaskMessage = "Minecraft est démarré !!";
				while ((line = buf.readLine()) != null) {
						Console.log("[CLIENT] > " + line);
							if(Wdes.keeplog == 1){
									out.println("[CLIENT] > " + line);
								}
								process.getSysOutLines().add(line);
							}
			} catch (IOException e) {
				e.printStackTrace();
			}
				out.close();
			

		final JPR onExit = process.getExitRunnable();
		
		Console.log("[CLIENT] > Code d'arrêt : " + process.getExitCode());
		if (onExit != null)
			onExit.onJavaProcessEnded(process);
		Mise_a_jour.subtaskMessage = "Minecraft est arrété : code " + process.getExitCode();
	}
}