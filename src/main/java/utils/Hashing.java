package utils;

import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {

    private long salt;
    // TODO: You should add a salt and make this secure       :FIX

    //Salt attribut der benyttes til at sikre hashing ekstra udover brugeres password

    /**
     * Hashing med salt metoder, der tager @param password og sætter den sammen med salt værdien som
     * String hashedPassword. Hvorefter det bliver hashed gennem md5-metoden og tilsidst retunere den hashed
     * værdi af hashedPassword
     *
     * @param password
     * @return hashedPassword
     */

    public String HashWithSaltMd5WithTimestamp(String password, Long timeStamp) {

        String hashedPassword = timeStamp + password;

        hashedPassword = md5(hashedPassword);

        return hashedPassword;
    }

    public static String md5(String rawString) {
        try {

            // We load the hashing algoritm we wish to use.
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

    // TODO: You should add a salt and make this secure         :FIX, added in UserController in Token

    public String sha(String rawString) {
        try {
            // We load the hashing algoritm we wish to use.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // We convert to byte array
            byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

            // We create the hashed string
            String sha256hex = new String(Hex.encode(hash));

            // And return the string
            return sha256hex;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return rawString;
    }
}