package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.InvalidKeySpecException;
import model.User;
import org.apache.commons.codec.binary.Hex;

public final class Hashing {


  // TODO: You should add a salt and make this secure:FIX
  public static String md5WithSalt(String password, User user) {
    //Getting the method getCreatedTime from model user to use as salt
    long salt = user.getCreatedTime();
    String hashedPassword = password + salt;
    return md5(hashedPassword);
  }
  public static String md5(String rawString) {

    try {

      // We load the hashing algorithm we wish to use.
      MessageDigest md = MessageDigest.getInstance("MD5");

      // We convert to byte array
      byte[] byteArray = md.digest(rawString.getBytes());

      // Initialize a string buffer
      StringBuffer sb = new StringBuffer();

      // Run through byteArray one element at a time and append the value to our stringBuffer
      for (int i = 0; i < byteArray.length; ++i) {
        sb.append(Integer.toHexString((byteArray[i] & 0xFF) | 0x100).substring(1, 3));
      }

      //Convert back to a single string and return
      return sb.toString();

    } catch (java.security.NoSuchAlgorithmException e) {

      //If somethings breaks
      System.out.println("Could not hash string");
    }

    return null;
  }

  // TODO: You should add a salt and make this secure: FIX
  public static String shaWithSalt(String password){
    String salt = Config.getSALT();
    String hashedPassword = password + salt;
    return sha(hashedPassword);
  }
  public static String sha(String rawString) {

    try {
      // We load the hashing algorithm we wish to use.
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // We convert to byte array
      byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

      // We create the hashed string
      String sha256hex = Hex.encodeHexString(hash);

      // And return the string
      return sha256hex;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return rawString;
  }

  public static void main(String[] args) {

    System.out.println(hashPassword("caroline"));
  }

  //This method contains the functionality of PBKDF2
  //The method takes four input variables
  public static String hashPassword( final String password) {

    String salt = "pumpkin_spice";


    //The password is converted into a char array before it is passed
    char[] passwordChars = password.toCharArray();

    byte[] saltBytes = salt.getBytes();
   //Number of iterations can be adjusted to adjust the speed of the algorithm
    int iterations = 10000;
    //Required output length of the hashed function
    int keyLength = 512;

    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
      PBEKeySpec spec = new PBEKeySpec( passwordChars, saltBytes, iterations, keyLength );
      SecretKey key = skf.generateSecret( spec );

      byte[] res = key.getEncoded( );

      return Hex.encodeHexString(res);

    } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
      throw new RuntimeException( e );
    }
  }
}
