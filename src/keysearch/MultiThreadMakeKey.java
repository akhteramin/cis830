package keysearch;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;


public class MultiThreadMakeKey {
    static int keylsbs, maxcounter;
    private static byte[] key;
    private static String salt = "privatesalt";

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
        System.out.println(args[0]);

        String originalString = args[0];
        int numOfBytes = Integer.parseInt(args[1]);


        byte[] keyArray = SecureRandom.getSeed(32);
//        final String secretKey = keyArray.toString();

        String secretKey = new String(keyArray, StandardCharsets.UTF_8);


        String encryptedString = encrypt(originalString, secretKey);

        for(int i=(32-(numOfBytes/8));i<32;i++){
            Array.setByte(keyArray, i,  (byte) 0);
        }

        byte[] trialkey = new byte[32];
        System.arraycopy(keyArray,0,trialkey,0,32);


        long start = System.currentTimeMillis();
        maxcounter = (1 <<numOfBytes)-1;

        // Try every possible combination of low-order key bits.
        // omp parallel for
        for(int counter = 0; counter < maxcounter; ++counter) {
            // Fill in low-order key bits.
            // Try the key.

            int lsbs = keylsbs | counter;

            for(int indx=0;indx<numOfBytes/8;indx++){
                trialkey[31-indx] = (byte) (lsbs);
            }


//            String s2 = String.format("%8s", Integer.toBinaryString(trialkey[30] & 0xFF)).replace(' ', '0');
//            System.out.println(s2);
//            String s3 = String.format("%8s", Integer.toBinaryString(trialkey[31] & 0xFF)).replace(' ', '0');
//            System.out.println(s3);
//            System.out.println("\n");


            String dynamicsecretKey= new String(trialkey, StandardCharsets.UTF_8);
            String dynamicdecryptedString = decrypt(encryptedString, dynamicsecretKey);

            try {
                if (originalString.compareTo(dynamicdecryptedString)==0) {
                    System.out.println("found:"+dynamicdecryptedString);
                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;
                    System.out.println("Elapsed time::"+String.valueOf(elapsedTime)+" ms");
                }
            }
            catch (NullPointerException e){

            }

        }


    }

}