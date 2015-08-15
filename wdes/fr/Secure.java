package wdes.fr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.digest.DigestUtils;


import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
public class Secure {

	static Cipher getCipher(int mode, String password) throws Exception {

	    Random random = new Random(43287234L);
	    byte[] salt = new byte[8];
	    random.nextBytes(salt);
	    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
		    SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		    Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		    cipher.init(mode, pbeKey, pbeParamSpec);
		    return cipher;
		  }
	
	 public static String encrypt(String message, String key){
		    try {
		      if (message==null || key==null ) return null;

		      char[] keys=key.toCharArray();
		      char[] mesg=message.toCharArray();
		 

		      int ml=mesg.length;
		      int kl=keys.length;
		      char[] newmsg=new char[ml];

		      for (int i=0; i<ml; i++){
		        newmsg[i]=(char)(mesg[i]^keys[i%kl]);
		      }
		      mesg=null; 
		      keys=null;
		      String temp = new String(newmsg);
		      return new String(new BASE64Encoder().encodeBuffer(temp.getBytes()));
		    }
		    catch ( Exception e ) {
		      return null;
		    }  
		  }


		  public static String decrypt(String message, String key){
		    try {
		      if (message==null || key==null ) return null;
		      BASE64Decoder decoder = new BASE64Decoder();
		      char[] keys=key.toCharArray();
		      message = new String(decoder.decodeBuffer(message));
		      char[] mesg=message.toCharArray();

		      int ml=mesg.length;
		      int kl=keys.length;
		      char[] newmsg=new char[ml];

		      for (int i=0; i<ml; i++){
		        newmsg[i]=(char)(mesg[i]^keys[i%kl]);
		      }
		      mesg=null; keys=null;
		      return new String(newmsg);
		    }
		    catch ( Exception e ) {
		      return null;
		    }  
		  }
		  
    static String sha1(String input) throws NoSuchAlgorithmException {
  		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
  		byte[] result = mDigest.digest(input.getBytes());
  		StringBuffer sb = new StringBuffer();
  		for (int i = 0; i < result.length; i++) {
  			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
  					.substring(1));
  		}
  		return sb.toString();

  	}
    public static String md5(String passe) 
    {
        return DigestUtils.md5Hex(passe);
     }
}
class MD5 {
	static String convertByteArrayToHexString(byte[] arrayBytes) {
	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < arrayBytes.length; i++) {
	        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
	                .substring(1));
	    }
	    return stringBuffer.toString();
	}
	public static byte[] createChecksum(String filename) throws Exception {
		File f = new File(filename);
		if(f.exists() && !f.isDirectory()) {  
	       InputStream fis =  new FileInputStream(filename);

	       byte[] buffer = new byte[1024];
	       MessageDigest complete = MessageDigest.getInstance("MD5");
	       int numRead;

	       do {
	           numRead = fis.read(buffer);
	           if (numRead > 0) {
	               complete.update(buffer, 0, numRead);
	           }
	       } while (numRead != -1);

	       fis.close();
	       return complete.digest();
		}
		return "".getBytes();
	   }

	   // see this How-to for a faster way to convert
	   // a byte array to a HEX string
	   public static String getMD5Checksum(String filename) throws Exception {
	       byte[] b = createChecksum(filename);
	       String result = "";

	       for (int i=0; i < b.length; i++) {
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	       }
	       return result;
	   }
	   public static byte[] convertSteamToByteArray(InputStream stream, long size) throws IOException {

		    // check to ensure that file size is not larger than Integer.MAX_VALUE.
		    if (size > Integer.MAX_VALUE) {
		        return new byte[0];
		    }

		    byte[] buffer = new byte[(int)size];
		    ByteArrayOutputStream os = new ByteArrayOutputStream();

		    int line = 0;
		    // read bytes from stream, and store them in buffer
		    while ((line = stream.read(buffer)) != -1) {
		        // Writes bytes from byte array (buffer) into output stream.
		        os.write(buffer, 0, line);
		    }
		    stream.close();
		    os.flush();
		    os.close();
		    return os.toByteArray();
		}
}
