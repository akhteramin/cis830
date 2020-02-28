package keysearch;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.*;


public class MultiThreadMakeKey {
    static int keylsbs=0, maxcounter;


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
        int num =(int)Math.pow(2, pos);
        if ((valByte & num) == num ) {
            return 1;
        }
        else
            return 0;
    }



    public static void main(String args[]) {
//        System.out.println(args[0]);


        String originalString = args[0];
        int numOfBytes = Integer.parseInt(args[1]);


        byte[] keyArray = SecureRandom.getSeed(32);


//        String secretKey = new String(keyArray, StandardCharsets.UTF_8);


        String encryptedString = encrypt(originalString, keyArray);
        for(int i=0;i<(numOfBytes/8);i++){
            keylsbs |= ((keyArray[31-i] & 0xFF)<< 8*(i+1));
        }
        if(numOfBytes%8!=0){
            keylsbs |= ((keyArray[31-numOfBytes/8] & 0xFF)<< numOfBytes);
        }
        System.out.println(keyArray[30]+" "+keyArray[31]);

        String s4 = String.format("data:: %8s", Integer.toBinaryString(keyArray[30] & 0xFF)).replace(' ', '0');
        System.out.println(s4);
        String s5 = String.format("data::  %8s", Integer.toBinaryString(keyArray[31] & 0xFF)).replace(' ', '0');
        System.out.println(s5);

        for(int i=(32-(numOfBytes/8));i<32;i++){
            Array.setByte(keyArray, i,  (byte) 0);
        }
        if(numOfBytes%8!=0){
            byte temp=(byte) 255;
            keyArray[31-(numOfBytes/8)] &=  (byte)(temp << (numOfBytes%8));
        }

//        System.out.println(keyArray[31]);
        String s2 = String.format("%8s", Integer.toBinaryString(keyArray[30] & 0xFF)).replace(' ', '0');
        System.out.println(s2);
        String s3 = String.format("%8s", Integer.toBinaryString(keyArray[31] & 0xFF)).replace(' ', '0');
        System.out.println(s3);


        byte[] trialkey = new byte[32];
        System.arraycopy(keyArray,0,trialkey,0,32);


        long start = System.currentTimeMillis();

        maxcounter = (1 <<numOfBytes)-1;
        // Try every possible combination of low-order key bits.

        // omp parallel for threadNum(7)
        for(int counter = 0; counter < maxcounter; ++counter) {
            // Fill in low-order key bits.
            // Try the key.

            for (int i = 0; i < numOfBytes / 8; i++) {

                trialkey[31 - i] = (byte) (counter >>> i * 8);
            }
            if (numOfBytes % 8 != 0) {

                int number = numOfBytes % 8;
                for (int i = 0; i < number; i++) {
                    trialkey[31 - numOfBytes/8] = modifyBit(trialkey[31 - numOfBytes/8], i, checkbit((byte) (counter >>> (numOfBytes/8) * 8), i));
                }
            }

//            int lsbs = keylsbs | counter;
////            System.out.println(lsbs);
//
//            for(int indx=0;indx<numOfBytes/8;indx++){
//
//                trialkey[31-indx] = (byte) (lsbs >>> indx*8);
//            }
//            if (numOfBytes%8!=0) {
//                //System.out.println(String.format("%8s", Integer.toBinaryString(trialkey[indx] & 0xFF)).replace(' ', '0'));
//                //  System.out.println("Before " + trialkey[31]);
//                //System.out.println(String.format("%8s", Integer.toBinaryString((lsbs >>> indx*8) & 0xFF)).replace(' ', '0'));
//                for (int i=0;i<numOfBytes%8;i++) {
//                    trialkey[31-(numOfBytes/8)] =  modifyBit(trialkey[31-(numOfBytes/8)],i,checkbit((byte) (lsbs >>> (numOfBytes/8)*8),i));
//                    //System.out.println(checkbit(org,i));
//                }
//                //  System.out.println("after " + trialkey[31]);
//                //System.out.println(String.format("%8s", Integer.toBinaryString(trialkey[indx] & 0xFF)).replace(' ', '0'));
//            }




//            String dynamicsecretKey= new String(trialkey, StandardCharsets.UTF_8);
            String dynamicencryptedString = encrypt(originalString, trialkey);
//            System.out.println("Running:"+trialkey[30]+" "+trialkey[31]);


            try {
                if (encryptedString.compareTo(dynamicencryptedString)==0) {

                    System.out.println("found: "+dynamicencryptedString);
                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;
                    System.out.println("Elapsed time::"+String.valueOf(elapsedTime)+" ms");
                    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                    for (Thread thread : threadSet) { thread.interrupt();}
//                    threadSet.stream().findAny(t->t.getId()).ifPresent(Thread::interrupt);

                }
            }
            catch (NullPointerException e){

            }

        }


    }

}