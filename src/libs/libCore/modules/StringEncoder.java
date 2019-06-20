package libs.libCore.modules;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class StringEncoder {

    private static final int KEY_SIZE = 128; //if bigger java.security.InvalidKeyException will be thrown, unless Java Cryptography Extension is configured
    private static final byte[] SALT = "jHLmjipa5u4wYOzzFf8Q".getBytes(); //does not need to be secret
    private static final int ITERATIONS = 1000;
    private static final char[] SECRET = "aOySBoeNyUmmakgBQk0o".toCharArray();

    private static SecretKeySpec generateSecretKeySpec(){
        try {
            /* Derive the key, given password and salt. */
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(SECRET, SALT, ITERATIONS, KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);

            return new SecretKeySpec(tmp.getEncoded(), "AES");

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            Log.error(e.getMessage());
        }

        return null;
    }

    public static String encrypt(String input){
        try {
            // CBC = Cipher Block chaining
            // PKCS5Padding Indicates that the keys are padded
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateSecretKeySpec());
            AlgorithmParameters params = cipher.getParameters();
            byte[] encrypted = cipher.doFinal(input.getBytes("UTF-8")); //encrypt input
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV(); //create init vector
            byte[] out = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(encrypted, 0, out, iv.length, encrypted.length);

            return new String(Base64.getEncoder().encode(out)); //encrypted data shall be encoded to avoid special characters in output string
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | InvalidParameterSpecException e){
            Log.error(e.getMessage());
        }

        return null;
    }

    public static String decrypt(String input){
        try {

            int keylen = KEY_SIZE / 8;
            byte[] iv = new byte[keylen];
            byte[] data = Base64.getDecoder().decode(input);
            System.arraycopy(Base64.getDecoder().decode(input), 0, iv, 0, keylen);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, generateSecretKeySpec(), new IvParameterSpec(iv));

            return new String(cipher.doFinal(data, keylen, data.length - keylen), "UTF-8");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e){
            Log.error(e.getMessage());
        }

        return null;
    }

}