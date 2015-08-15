package wdes.fr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;




public class Mise_a_jour {
	  public static boolean pauseAskUpdate = false;
	public static boolean fatalError = false;
	public static String fatalErrorDescription = "Erreur Fatale";
	public static int percentage = 0;
	public static String subtaskMessage = "Launcher Wdes";
	public static String state_msg = "Initialisation du launcher";
	protected static int forceUpdate =0;
	public boolean shouldUpdate =false;
	public String [] ressources;
	public String [] versions;
	public String [] librairies;
	private int totalSizeDownload;
	private int currentSizeDownload;
   
	public final void run_maj() {

		System.setProperty("file.encoding","UTF-8");

    
	try {
		/*
		    String crypt = null;
            String decrypt = null;
		    byte[] keyValue = new byte[] { '4', '3', '1', '4', '2', '2', '2','2', '1', '4', '2','3', '2', '3', '0', '1' };
	     	String key_crypt = new String(keyValue, "UTF-8");
		   Console.log("Key : "+key_crypt);
		   crypt = new String(Secure.encrypt( "test",key_crypt ));
	       decrypt = new String(Secure.decrypt(crypt,key_crypt));
		   Console.log(crypt);
		   Console.log(decrypt);
	    */
		update_description();
		 Console.log("[UPDATE] Mise a jour en cours.");
		 Console.log("[UPDATE] Force update : "+forceUpdate+".");
	
			ressources = loadURLs(Parametres.getparam("URL_RESSOURCES_SERVER"),Parametres.getparam("URL_RESSOURCES"),"ressources");
			versions = loadURLs(Parametres.getparam("URL_VERSIONS_SERVER"),Parametres.getparam("URL_VERSIONS"),"versions");
			librairies = loadURLs(Parametres.getparam("URL_LIBRAIRIES_SERVER"),Parametres.getparam("URL_LIBRAIRIES"),"librairies");
			
			telecharger_jars(Parametres.getparam("URL_RESSOURCES"),ressources,"","ressources");
			telecharger_jars(Parametres.getparam("URL_VERSIONS"),versions,"versions","de la version");
			telecharger_jars(Parametres.getparam("URL_LIBRAIRIES"),librairies,"librairies","librairies");
			
					}
		 catch (Exception e) {
			e.printStackTrace();
		}
		 
	}
	  protected String getFileName(URL url) {
		    String fileName = url.getFile();
		    if (fileName.contains("?")) {
		      fileName = fileName.substring(0, fileName.indexOf("?"));
		    }
		    return fileName.substring(fileName.lastIndexOf('/') + 1);
		  }
	  protected InputStream getJarInputStream(String currentFile, final URLConnection urlconnection)
		      throws Exception
		    {
		      final InputStream[] is = new InputStream[1];
		  
		      for (int j = 0; (j < 3) && (is[0] == null); j++) {
		      Thread t = new Thread() {
		          public void run() {
		            try {
		              is[0] = urlconnection.getInputStream();
		            }
		            catch (IOException localIOException)
		            {
		            }
		          }
		        };
		       t.setName("JarInputStreamThread");
		       t.start();
		  
		       int iterationCount = 0;
		        while ((is[0] == null) && (iterationCount++ < 5)) {
		          try {
		           t.join(1000L);
		          }
		          catch (InterruptedException localInterruptedException)
		          {
		          }
		        }
		        if (is[0] != null) continue;
		        try {
		          t.interrupt();
		          t.join();
		        }
		        catch (InterruptedException localInterruptedException1)
		        {
		        }
		      }
		  
		  	if (is[0] == null) {
			throw new Exception("Impossible de télécharger " + urlconnection.toString());
		    } 
		  	return is[0];
		  }
	  protected void telecharger_jars(String SERVER_NAME,String[] jarlist,String REPLACE_NAME,String COMMENTAIRE)
		      throws Exception
		    {
		  
		  state_msg = "Téléchargement en cours des fichiers "+COMMENTAIRE+" ...";
		      update_description();
		      int[] fileSizes = new int[jarlist.length];
		      for (int i = 0; i < jarlist.length; i++) {
		     		percentage = i*100/jarlist.length;
		     		subtaskMessage = ("Listage des fichiers en cours... (" + i*100/jarlist.length +"%)");
		        Console.log("Url "+i+" : "+jarlist[i]);
		        URLConnection urlconnection = new URL(jarlist[i]).openConnection();
		        urlconnection.setDefaultUseCaches(false);
		        if ((urlconnection instanceof HttpURLConnection)) {
		          ((HttpURLConnection)urlconnection).setRequestMethod("HEAD");
		        }
		        fileSizes[i] = urlconnection.getContentLength();
		        totalSizeDownload += fileSizes[i];
		      
		      }
		      int initialPercentage = percentage = 10;
		      byte[] buffer = new byte[65536];
	          subtaskMessage = ("Installation en cours...");
	          
	     		
		      for (int i = 0; i < jarlist.length; i++)
		      {
		    	  
		        int unsuccessfulAttempts = 0;
		        int maxUnsuccessfulAttempts = 3;
		        boolean downloadFile = true;
		        while (downloadFile) {
		          downloadFile = false;
		          URLConnection urlconnection = new URL(jarlist[i]).openConnection(); 
		          if ((urlconnection instanceof HttpURLConnection)) {
		            urlconnection.setRequestProperty("Cache-Control", "no-cache");
		            urlconnection.connect();
		          }
		          String currentFile = getFileName(new URL (jarlist[i]));
		          InputStream inputstream = getJarInputStream(currentFile, urlconnection);
		          String name = jarlist[i].replace(SERVER_NAME, REPLACE_NAME);
		          FileOutputStream fos = new FileOutputStream(Util.getWorkingDirectory()+"/" + name); 
		          long downloadStartTime = System.currentTimeMillis();
		          int downloadedAmount = 0;
		          int fileSize = 0;
		          String downloadSpeedMessage = "";
		          int bufferSize;
		          while ((bufferSize = inputstream.read(buffer, 0, buffer.length)) != -1)
		          {
		        	 
		            fos.write(buffer, 0, bufferSize);
		            this.currentSizeDownload += bufferSize;
		            fileSize += bufferSize;
		            percentage = (initialPercentage + this.currentSizeDownload * 45 / this.totalSizeDownload);
		            subtaskMessage = ("Recu: " + currentFile + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%");
		           
		            downloadedAmount += bufferSize;
		            long timeLapse = System.currentTimeMillis() - downloadStartTime;  
		            if (timeLapse >= 1000L)
		            {
		              float downloadSpeed = downloadedAmount / (float)timeLapse;
		              downloadSpeed = (int)(downloadSpeed * 100.0F) / 100.0F;
		              downloadSpeedMessage = " -> " + downloadSpeed + " KB/sec";
		              downloadedAmount = 0;
		              downloadStartTime += 1000L;
		            }  
		            subtaskMessage += downloadSpeedMessage;
		          }
		          inputstream.close();
		          fos.close();
		          
		          Console.log("Réception en cours de : " + currentFile + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%");
		          subtaskMessage = ("Réception en cours de : " + currentFile + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%");
		          if ((!(urlconnection instanceof HttpURLConnection)) ||(fileSize == fileSizes[i]))
		            continue;
		          if (fileSizes[i] <= 0)
		          {
		            continue;
		          }
		          unsuccessfulAttempts++;
		          if (unsuccessfulAttempts < maxUnsuccessfulAttempts) {
		            downloadFile = true;
		            this.currentSizeDownload -= fileSize;
		          }
		          else {
		        	  subtaskMessage = "Echec du téléchargement " + currentFile;
		            throw new Exception("Echec du téléchargement " + currentFile);
		          }
		        }
		      subtaskMessage = "";
		    }
		    }

	public final String update_description() {
		   return state_msg;
	}
	  protected String[] loadURLs(String Server,String data,String name) throws Exception 
	  {
		  state_msg = "Téléchargement des informations...";
	     String jars = Util.HTTPS_POST(Server, "");
	     if (jars == null) {
	    	 fatalErrorOccured("Connection impossible a "+Parametres.getparam("SERVER_NAME")+"(by Wdes)",null);

	     }
	     else{
	    	 state_msg = "Informations récupérées !! ";
	    	 JSONObject obj = new JSONObject(jars);
	    	 JSONArray dossiers = obj.getJSONArray("dossiers");
	    	 state_msg = "Création des dossiers";
	    	 List<String> list =new ArrayList<String>();
		     	for (int i = 0; i < dossiers.length(); i++)
		     	{
		     		percentage = i*100/dossiers.length();
		     		subtaskMessage = ("Création des dossiers en cours... (" + i*100/dossiers.length() +"%)");
		     		new File(Util.getWorkingDirectory()+"/"+dossiers.getJSONObject(i).getString("nom")).mkdirs();
		     	}
	     	JSONArray arr = obj.getJSONArray(name);
	     	
	     	int totalurls = arr.length();
	     	
	     	state_msg = "Vérification des fichiers...";
	     	subtaskMessage = ("Vérification en cours... (0%)");
	     	for (int i = 0; i < arr.length(); i++)
	     	{
	     		percentage = i*100/totalurls;
	     		subtaskMessage = ("Vérification en cours... (" + i*100/totalurls +"%)");
	     		//String md5 = arr.getJSONObject(i).getString("md5");
	     		String nom = arr.getJSONObject(i).getString("nom");
	     		String dossier = arr.getJSONObject(i).getString("dossier");
	     		int lenght = arr.getJSONObject(i).getInt("length");
	     		if(!dossier.equals( "aucun")){nom = dossier+"/"+nom;}
	     		Console.log(Util.getWorkingDirectory()+"/"+nom);
	     		//String md5_file = MD5.getMD5Checksum(Util.getWorkingDirectory()+"/"+nom);
	     		File fichier  =new File(Util.getWorkingDirectory()+"/"+nom);
	     		int lenght_local   = (int)fichier.length();
	     		
	     		//Console.log("\n"+nom+" : "+lenght_local+" => "+lenght);
	     		//md5.equals(md5_file)
	     	    if( lenght == lenght_local && forceUpdate == 0 ){
	     	    	
	     	     //Console.log("[UPDATE][FALSE] Fichier : "+nom+" MD5 : "+md5_file+"");
	     	       Console.log("[UPDATE][FALSE] Fichier : "+nom+" LENGTH : "+lenght_local+"");
	     	    }
	     	    else if (forceUpdate ==1 ){
	     	    	list.add(new String(data+"/"+arr.getJSONObject(i).getString("nom")));
	     	    
	     	  //Console.log("[FROCE_UPDATE][TRUE] Fichier : "+nom+" MD5 FILE : "+md5_file+" MD5 SITE : "+md5);	
	     	    	Console.log("[FROCE_UPDATE][TRUE] Fichier : "+nom+" LENGTH FILE : "+lenght_local+" LENGTH SITE : "+lenght);	
	     	    }
	     	    //else if (md5 != md5_file){
	     	    else if (lenght != lenght_local){
	     	    	list.add(new String(data+"/"+arr.getJSONObject(i).getString("nom")));
	  	     	  //Console.log("[UPDATE][TRUE] Fichier : "+nom+" MD5 FILE : "+md5_file+" MD5 SITE : "+md5);	
	     	    	Console.log("[UPDATE][TRUE] Fichier : "+nom+" LENGTH FILE : "+lenght_local+" LENGTH SITE : "+lenght);
	     	    }
	     	}
	     	String [] jarlist = list.toArray(new String[list.size()]);
	     	Console.log("JAR_LIST : "+jarlist.length);

	     	Console.log(name+" trouvés : "+arr.length());
	     	return jarlist;
	     

	  }
		return null;
	}
	  protected void fatalErrorOccured(String error, Exception e) {
		    e.printStackTrace();
		    Mise_a_jour.fatalError = true;
		    Mise_a_jour.fatalErrorDescription = ("Erreur fatale (" + state_msg + "): " + error);
		    Console.log(Mise_a_jour.fatalErrorDescription);

		  }
}
