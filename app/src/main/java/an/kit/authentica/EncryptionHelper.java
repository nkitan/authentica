package an.kit.authentica;

import android.content.Context;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;


// import exceptions
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// import Crypto Libraries from Java Standard libraries
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// import util functions from Utils.java
import static an.kit.authentica.Utils.readFully;
import static an.kit.authentica.Utils.writeFully;

public class EncryptionHelper {
    private final static String ALGORITHM = "AES/GCM/PKCS5Padding";
    private final static int KEY_LENGTH = 16;
    private final static int IV_LENGTH = 12;
    
    /* 
        function to encrypt key with AES
        takes in secretkey, seed and plaintext
        returns AES encrypted string
    */
    public static byte[] encrypt(SecretKey secretKey, IvParameterSpec iv, byte[] plainText) 
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);   // create an instance of AES GCM with no padding
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv); // generate cipher using specified key and length     

        return cipher.doFinal(plainText);               // digest cipher and return
    }

    /* 
     function to decrypt AES key
     takes in secretkey and cipertext
     returns decrypted plaintext 
    */
    public static byte[] decrypt(SecretKey secretKey, IvParameterSpec iv, byte[] cipherText) 
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        return cipher.doFinal(cipherText);
    }

    /*  
        function to encrypt key with AES
        takes in secretkey and plaintext
        overloaded to work without a seed/initialization vector
        returns AES encrypted string
    */
    public static byte[] encrypt(SecretKey secretKey, byte[] plaintext) 
        throws NoSuchPaddingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        final byte[] iv = new byte[IV_LENGTH];  
        new SecureRandom().nextBytes(iv); // generate initialization vector / seed

        // pass to encrypt function defined earlier
        byte[] cipherText = encrypt(secretKey, new IvParameterSpec(iv), plaintext);

        // pad encrypted string(ciphertext) with the length of the IV
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

        // return padded string
        return combined;
    }

    /*  
       function to decrypt AES encrypted ciphertext without iv/seed
       overloads decrypt function to work without iv
       returns decrypted string as plaintext / string
    */
    public static byte[] decrypt(SecretKey secretKey, byte[] cipherText) 
        throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        // generate iv/seed
        byte[] iv = Arrays.copyOfRange(cipherText, 0, IV_LENGTH);
        byte[] cipher = Arrays.copyOfRange(cipherText, IV_LENGTH,cipherText.length);

        // call decrypt function previously defined
        return decrypt(secretKey,new IvParameterSpec(iv), cipher );
    }

    /*
    *  Funtion to generate random bytearray
    *
    */
    private static byte[] generateRandom(int length) {
        final byte[] raw = ByteBuffer.allocate(length).array();
        new SecureRandom().nextBytes(raw);

        return raw;
    }

    /* 
       Load our symmetric secret key.
       The symmetric secret key is stored securely on disk by wrapping
       it with a public/private key pair, possibly backed by hardware.
     */

    /* 
        function to load / generate AES key
    */

    public static SecretKey loadOrGenerateKeys(Context context, File keyFile)
            throws GeneralSecurityException, IOException {
        // create a constant / final wrapper
        final SecretKeyWrapper wrapper = new SecretKeyWrapper(context, "settings"); 

        // Generate secret key if none exists
        if (!keyFile.exists()) {
            final byte[] raw = generateRandom(KEY_LENGTH);


            final SecretKey key = new SecretKeySpec(raw, "AES");
            final byte[] wrapped = wrapper.wrap(key);


            writeFully(keyFile, wrapped);
        }

        // Even if we just generated the key, always read it back to ensure we
        // can read it successfully.
        final byte[] wrapped = readFully(keyFile);
        
        // return generated / loaded key from keyFile
        return wrapper.unwrap(wrapped);
    }
}
