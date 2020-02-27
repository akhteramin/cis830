package keysearch;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Scanner;


public class MakeKey {
    static int keylsbs, maxcounter;
    private static byte[] key;
    private static String salt = "ssshhhhhhhhhhh!!!!";

    public static String encrypt(String strToEncrypt, String secret)
    {
        try
        {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try
        {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e) {
//            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static void main(String args[]) {
        Scanner stringObj = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter String:");

        String originalString = stringObj.nextLine();

        byte[] keyArray = SecureRandom.getSeed(32);
//        final String secretKey = keyArray.toString();
        for(int i=31;i<32;i++){
            keyArray[31] =  (byte) Math.sqrt(keyArray[31]*keyArray[31]);
        }
        keyArray[31] = 127;


        String secretKey = new String(keyArray, StandardCharsets.UTF_8);


        String encryptedString = encrypt(originalString, secretKey);

        for(int i=31;i<32;i++){
            Array.setByte(keyArray, i,  (byte) 0);
        }

        byte[] trialkey = new byte[32];
        System.arraycopy(keyArray,0,trialkey,0,32);


        long start = System.currentTimeMillis();
        maxcounter = (1 <<8)-1;

        // Try every possible combination of low-order key bits.
        for(int counter = 0; counter < maxcounter; ++counter) {
            // Fill in low-order key bits.
            // Try the key.

            int lsbs = keylsbs | counter;
//            trialkey[29] = (byte) (lsbs >>> 16);
//            trialkey[30] = (byte) (lsbs >>> 8);
            trialkey[31] = (byte) (lsbs);

//            String s2 = String.format("%8s", Integer.toBinaryString(trialkey[30] & 0xFF)).replace(' ', '0');
//            System.out.println(s2);
//            String s3 = String.format("%8s", Integer.toBinaryString(trialkey[31] & 0xFF)).replace(' ', '0');
//            System.out.println(s3);
//            System.out.println("\n");


            String dynamicsecretKey= new String(trialkey, StandardCharsets.UTF_8);
            String dynamicdecryptedString = decrypt(encryptedString, dynamicsecretKey);

            try {
                if (originalString.compareTo(dynamicdecryptedString)==0) {
                    System.out.println("found:"+dynamicdecryptedString+" lsbs:"+lsbs);

                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;
                    System.out.println("Elapsed time::"+String.valueOf(elapsedTime)+" ms");
//                    break;
                }
            }
            catch (NullPointerException e){

            }

        }


    }
}
