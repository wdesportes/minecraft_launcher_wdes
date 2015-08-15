package wdes.fr;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;






public class Util
{
  private static File workDir = null;

  public static File getWorkingDirectory() {
    if (workDir == null) workDir = getWorkingDirectory(Parametres.getparam("APPDATA"));
    return workDir;
  }

  public static File getWorkingDirectory(String applicationName) {
    String userHome = System.getProperty("user.home", ".");
    File workingDirectory;
    switch (getPlatform()) {
    case linux:
      workingDirectory = new File(userHome, '.' + applicationName + '/');
      break;
    case windows:
      String applicationData = System.getenv("APPDATA");
      if (applicationData != null) workingDirectory = new File(applicationData, "." + applicationName + '/'); else
        workingDirectory = new File(userHome, '.' + applicationName + '/');
      break;
    case macos:
      workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
      break;
    default:
      workingDirectory = new File(userHome, applicationName + '/');
    }
    if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) throw new RuntimeException("pas �t� cr�� : " + workingDirectory);
    return workingDirectory;
  }

  private static OS getPlatform() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) return OS.windows;
    if (osName.contains("mac")) return OS.macos;
    if (osName.contains("solaris")) return OS.solaris;
    if (osName.contains("sunos")) return OS.solaris;
    if (osName.contains("linux")) return OS.linux;
    if (osName.contains("unix")) return OS.linux;
    return OS.unknown;
  }

 
public static String HTTP_POST(String targetURL, String urlParameters)
  {
    HttpURLConnection connection = null;
    try
    {
      Console.log("POST HTTP : "+targetURL);
      URL url = new URL(targetURL);

      connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.connect();
      
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();

      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));

      StringBuffer response = new StringBuffer();
      String line;
      while ((line = rd.readLine()) != null)
      {
        response.append(line);
        response.append('\r');
      }
      rd.close();

      String str1 = response.toString();
      return str1;
    }
    catch (Exception e)
    {
     
      return null;
    }
    finally
    {
      if (connection != null)
        connection.disconnect();
    }
  }

public static String HTTPS_POST(String targetURL, String urlParameters) throws NoSuchAlgorithmException, KeyManagementException
{
	  HttpsURLConnection connection = null;
	TrustManager[] trustAllCerts = new TrustManager[] {
			   new X509TrustManager() {
			      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			        return null;
			      }

				@Override
				public void checkClientTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {
					
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {
					
				}

			   }
			};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {

					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

  try
  {
	  Console.log("POST HTTPS : "+targetURL);
    URL url = new URL(targetURL);
  	connection = (HttpsURLConnection)url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(true);

    connection.connect();
    
    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();
    wr.close();

    InputStream is = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

    StringBuffer response = new StringBuffer();
    String line;
    while ((line = rd.readLine()) != null)
    {
      response.append(line);
      response.append('\r');
    }
    rd.close();

    String str1 = response.toString();
    return str1;
  }
  catch (Exception e)
  {
   
    return null;
  }
  finally
  {
    if (connection != null)
      connection.disconnect();
  }
}
  public static boolean isEmpty(String str) {
    return (str == null) || (str.length() == 0);
  }

  public static void openLink(URI uri) {

	  Desktop d=Desktop.getDesktop();
	  try {
		d.browse(uri);
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  private static enum OS
  {
    linux, solaris, windows, macos, unknown;
  }
}
class Console {
public static void log(String text){
	System.out.println("[Wdes]"+text);
}
}