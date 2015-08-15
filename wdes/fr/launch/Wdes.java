package wdes.fr.launch;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;





import wdes.fr.Connection;
import wdes.fr.Mise_a_jour;
import wdes.fr.Parametres;
import wdes.fr.Ressources;
import wdes.fr.Util;


public abstract class Wdes  {


	public static Applet launchGame() throws IOException, NoSuchAlgorithmException {
		
		try{
			HttpURLConnection urlConn = 
				(HttpURLConnection)(new URL(Parametres.getparam("URl_INTERNET")).openConnection());
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			while((reader.readLine()) != null)
			{

		username = Connection.userName.getText();
		
			}
			reader.close();
		} catch (Exception e) {
			username ="Joueur";
			
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(Ressources.getResourceAsStream("/wdes/fr/ressources/start.args")));
		

		String read;
		while((read = br.readLine()) != null) {
		    sb.append(read);
		    read =br.readLine();

		}
		File appdata = Util.getWorkingDirectory(Parametres.getparam("APPDATA"));
		Console.log("Username : " + username+"\n");
		Console.log("Garder le log : " + Wdes.keeplog +"\n");
		Console.log("Laisser le launcher ouvert : " + Wdes.noclose +"\n");
		Console.log("Démarrage dans " + appdata+"\n");
		final JPL processLauncher = new JPL(Os.getJavaDir(),new String[0]);
		processLauncher.directory(appdata);

		final Os os = Os.getCurrentPlatform();
		if (os.equals(Os.OSX)){
		processLauncher.addCommands(new String[] {"-Xdock:icon="+ new File(appdata, "icons/minecraft.icns").getAbsolutePath(),"-Xdock:name=" + Parametres.getparam("SERVER_NAME") });
		}
		else if (os.equals(Os.WINDOWS)){
		processLauncher.addCommands(new String[] { "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump" });
		}
		else if (os.equals(Os.LINUX)){
			processLauncher.addCommands(new String[] { "-XX:+UseFastAccessorMethods" });
			processLauncher.addCommands(new String[] { "-XX:+AggressiveOpts" });
			processLauncher.addCommands(new String[] { "-XX:+DisableExplicitGC" });
			processLauncher.addCommands(new String[] { "-XX:+UseAdaptiveGCBoundary" });
			processLauncher.addCommands(new String[] { "-XX:MaxGCPauseMillis=500" });
			processLauncher.addCommands(new String[] { "-XX:SurvivorRatio=16" });
			processLauncher.addCommands(new String[] { "-XX:+UseParallelGC" });
			processLauncher.addCommands(new String[] { "-XX:UseSSE=3" });
		}
		processLauncher.addCommands(new String[] { "-Xmx1G" });
		processLauncher.addCommands(new String[] { "-Djava.library.path="+appdata+ "/natives/"});
		processLauncher.addCommands(new String[] { "-cp",sb.toString().replace("{URL}", appdata.toString())});

		//"net.minecraft.client.main.Main"
		processLauncher.addCommands(new String[] { "net.minecraft.client.main.Main" });
		processLauncher.addCommands(new String[] { "--username",username});
		processLauncher.addCommands(new String[] { "--version",Parametres.getparam("VERSION")});
		processLauncher.addCommands(new String[] { "--userProperties","{\"twitch_access_token\":[]}"});
		processLauncher.addCommands(new String[] { "--userType","mojang"});
		//processLauncher.addCommands(new String[] { "--assetIndex", "1.8.1" });
		
		processLauncher.addCommands(new String[] { "--uuid", new UUID(0L, 0L).toString()});
		processLauncher.addCommands(new String[] { "--assetsDir",appdata+"/assets/"});
		processLauncher.addCommands(new String[] { "--gameDir",appdata+"/"});
		//processLauncher.addCommands(new String[] { "--tweakClass","cpw.mods.fml.common.launcher.FMLTweaker"});
		final List<String> parts = processLauncher.getFullCommands();
		final StringBuilder full = new StringBuilder();
		boolean first = true;

		for (final String part : parts) {
			if (!first)
				full.append(" ");
			full.append(part);
			first = false;
		}
		
		Console.log("Lancé " + full.toString()+"\n");
		
		processLauncher.start();
		Mise_a_jour.state_msg = "Lancement du jeu";
		

		Mise_a_jour.percentage = 100;
		Mise_a_jour.subtaskMessage = "Launcher 1.5.2 adapté par williamdes pour la 1.7.2,1.7.10,1.8.1";
		if(noclose != 1 | Mise_a_jour.pauseAskUpdate | Mise_a_jour.fatalError ){
		System.exit(0);
		}
		return null;
	}

	public static String username = "Joueur";
	public static int noclose = 0;
	public static int keeplog = 0;
	}




