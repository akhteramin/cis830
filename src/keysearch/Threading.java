package keysearch;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

class thread implements Runnable {
    int start, end;
    byte[] trialkey;
    String encryptedString, originalString, tName;
    long startTime;
    int numOfBytes;

    thread(String name, int start, int end, byte[] trialkey, String encryptedString, String originalString,
               long time, int numOfBytes) {
        this.start = start;
        this.end = end;
        this.trialkey = trialkey;
        this.encryptedString = encryptedString;
        this.originalString = originalString;
        this.tName = name;
        this.startTime = time;
        this.numOfBytes = numOfBytes;
    }

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
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static byte modifyBit(byte n, int p, int b) {
        int mask = 1 << p;
        return (byte) ((n & ~mask) | ((b << p) & mask));
    }

    public static int checkbit(byte valByte, int pos) {
        int num = (int) Math.pow(2, pos);
        if ((valByte & num) == num) {
            return 1;
        } else
            return 0;
    }


    @Override
    public void run() {
        System.out.println("Thread " + tName + " Start " + start + " end " + end);
        // TODO Auto-generated method stub
        for (int counter = start; counter <= end; ++counter) {


            int indx = 0;
            for (indx = 0; indx < numOfBytes / 8; indx++) {

                trialkey[31 - indx] = (byte) (counter >>> indx * 8);
            }
            if (numOfBytes % 8 != 0) {

                int number = numOfBytes % 8;
                for (int i = 0; i < number; i++) {
                    trialkey[31 - indx] = modifyBit(trialkey[31 - indx], i, checkbit((byte) (counter >>> indx * 8), i));
                }
            }
            String dynamicencryptedString = encrypt(originalString, trialkey);

            try {
                if (encryptedString.compareTo(dynamicencryptedString) == 0) {

                    System.out.println("found: "+dynamicencryptedString+" "+trialkey[30]+" "+trialkey[31]);
                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;
                    System.out.println("Elapsed time::"+String.valueOf(elapsedTime)+" ms");
                    break;

                }
            } catch (NullPointerException e) {

            }

        }

    }
}
public class Threading {
    static int maxcounter;

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
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter text: ");
        String originalString = in.nextLine();

        System.out.println("Please enter number of bits you want to hide: ");
        int numOfBytes = in.nextInt();
        byte[] keyArray = SecureRandom.getSeed(32);
        String encryptedString = encrypt(originalString, keyArray);
        System.out.println("Cipher text with original Key: " + encryptedString);
        for (int i = (32 - (numOfBytes / 8)); i < 32; i++) {
            Array.setByte(keyArray, i, (byte) 0);
        }

        byte[] trialkey = new byte[32];
        System.arraycopy(keyArray, 0, trialkey, 0, 32);

        maxcounter = (1 << numOfBytes) - 1;

        long startTime = System.currentTimeMillis();
        int div = maxcounter / 10;
        for (int i = 1; i <= 10; i++) {
            Thread t1 = new Thread(new thread("thread" + Integer.toString(i), div * (i - 1), (div * i)-1, trialkey,
                    encryptedString, originalString, startTime, numOfBytes));
            t1.start();
        }
        if(maxcounter % 10!=0) {
            Thread t1 = new Thread(new thread("t" + Integer.toString(8), div * 7, maxcounter, trialkey, encryptedString,
                    originalString, startTime, numOfBytes));
            t1.start();
        }

    }
}
