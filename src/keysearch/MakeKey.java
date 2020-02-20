package keysearch;

import javax.crypto.BadPaddingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class MakeKey {
    static int keylsbs, maxcounter;
    public static void main(String args[]) {
        byte[] keyArray = SecureRandom.getSeed(32);
//        final String secretKey = keyArray.toString();

        String secretKey = new String(keyArray, StandardCharsets.UTF_8);

        String originalString = "howtodoinjava.com";
        String encryptedString = AES.encrypt(originalString, secretKey);

        for(int i=29;i<32;i++){
            Array.setByte(keyArray, i,  (byte) 0);
        }

        byte[] trialkey = new byte[32];
        System.arraycopy(keyArray,0,trialkey,0,32);

        maxcounter = (1 <<24)-1;
        // Try every possible combination of low-order key bits.
        for(int counter = 0; counter < maxcounter; ++counter) {
            // Fill in low-order key bits.
            // Try the key.

            int lsbs = keylsbs | counter;
            trialkey[29] = (byte) (lsbs >>> 16);
            trialkey[30] = (byte) (lsbs >>> 8);
            trialkey[31] = (byte) (lsbs);

            String dynamicsecretKey= new String(trialkey, StandardCharsets.UTF_8);
            String dynamicdecryptedString = AES.decrypt(encryptedString, dynamicsecretKey);
            try {
                if (originalString.compareTo(dynamicdecryptedString)==0) {
                    System.out.println("found");
                    break;
                }
            }
            catch (NullPointerException e){

            }
        }

    }
}
