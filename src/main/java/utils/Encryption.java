package utils;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better.:FIX
      char[] key = Config.getEncryptionKey();


      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on?:FIX
      // An XOR gate implements an exclusive or. It performs a logical operation that takes two binary inputs and returns a single binary input.
      //The XOR gate returns true if one or the other output is true. If the inputs are alike it will return false.
      for (int i = 0; i < rawString.length(); i++)
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
      }

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }


}
