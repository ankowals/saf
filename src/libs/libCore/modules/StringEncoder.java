package libs.libCore.modules;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class StringEncoder {

    public static String encrypt(String strClearText,String strKey){
        try {
            SecretKeySpec skeyspec = new SecretKeySpec(strKey.getBytes(), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
            byte[] encrypted = cipher.doFinal(strClearText.getBytes("UTF-8"));

            return new String(Base64.getEncoder().encode(encrypted)); //encrypted data shall be encoded to avoid special characters in output string
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e){
            Log.error(e.getMessage());
        }

        return null;
    }

    public static String decrypt(String strEncrypted,String strKey){
        try {
            SecretKeySpec skeyspec = new SecretKeySpec(strKey.getBytes(),"Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, skeyspec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(strEncrypted));

            return new String(decrypted, "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e){
            Log.error(e.getMessage());
        }

        return null;
    }

}