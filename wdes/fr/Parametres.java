package wdes.fr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;


public class Parametres {
	private static String readInputStream(InputStream inputStream) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
	            inputStream, "UTF-8"));
	    String tmp;
	    StringBuilder sb = new StringBuilder();
	    while ((tmp = reader.readLine()) != null) {
	        sb.append(tmp).append("\n");
	    }
	    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
	        sb.setLength(sb.length() - 1);
	    }
	    reader.close();
	    return sb.toString();
	}
	public static String getparam(String name){

		JSONObject obj = null;
		try {
			obj = new JSONObject(readInputStream(Ressources.getResourceAsStream("/wdes/fr/ressources/config.json")));
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return (String) obj.getString(name);
	}
	

}