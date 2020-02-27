package keysearch;

import javax.crypto.*;
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
//    private static byte[] key;
//    private static String salt = "ssshhhhhhhhhhh!!!!";

    public static String encrypt(String strToEncrypt, byte[] secret)
    {
        try
        {

            SecretKeySpec sskey= new SecretKeySpec(secret, "AES");

            Cipher c = Cipher.getInstance("AES");

            c.init(Cipher.ENCRYPT_MODE, sskey);

            byte[] encrypted = c.doFinal(strToEncrypt.getBytes());
//            System.out.println("encrypted string: " + encrypted.toString());
            return Base64.getEncoder().encodeToString(encrypted);

        }
        catch (Exception e)
        {
//            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static void main(String args[]) {

        String originalString = "adlfhaksd";
        byte[] keyArray = SecureRandom.getSeed(32);

        System.out.println(keyArray[31]);

//        String secretKey = new String(keyArray, StandardCharsets.UTF_8);


        String encryptedString = encrypt(originalString, keyArray);
        System.out.println(encryptedString);
        keylsbs |= (keyArray[31] & 0xFF)<<8;

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
            System.out.println(trialkey[31]);

            String dynamicsecretKey= new String(trialkey, StandardCharsets.UTF_8);
            String dynamicdecryptedString = encrypt(originalString, trialkey);

            try {
                if (encryptedString.compareTo(dynamicdecryptedString)==0) {
                    System.out.println("found:"+dynamicdecryptedString+" lsbs:"+trialkey[31]);

                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;
                    System.out.println("Elapsed time::"+String.valueOf(elapsedTime)+" ms");
                    break;
                }
            }
            catch (NullPointerException e){

            }

        }


    }
}
