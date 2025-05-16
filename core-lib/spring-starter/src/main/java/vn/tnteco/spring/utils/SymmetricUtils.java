package vn.tnteco.spring.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@UtilityClass
public class SymmetricUtils {
    private final String KEY_TYPE = "AES";

    private final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";

    private final int TAG_LENGTH = 128;
    private static final String IV = "HVCUJpJ6xix8lA2/lzRLxA==";


    @SneakyThrows
    public byte[] encrypt(byte[] inputBytes, byte[] secretKeyBytes, byte[] iv) {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, KEY_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(inputBytes);
    }

    @SneakyThrows
    public byte[] encrypt(byte[] inputBytes, byte[] secretKeyBytes) {
        return encrypt(inputBytes, secretKeyBytes, Base64.getDecoder().decode(IV));
    }

    @SneakyThrows
    public byte[] encryptGCM(byte[] inputBytes, byte[] secretKeyBytes, byte[] iv) {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, KEY_TYPE);
        AlgorithmParameterSpec algorithmParameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, algorithmParameterSpec);
        return cipher.doFinal(inputBytes);
    }

    @SneakyThrows
    public byte[] decrypt(byte[] cipherBytes, byte[] secretKeyBytes, byte[] iv) {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, KEY_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(cipherBytes);
    }

    @SneakyThrows
    public byte[] decrypt(byte[] cipherBytes, byte[] secretKeyBytes) {
        return decrypt(cipherBytes, secretKeyBytes, Base64.getDecoder().decode(IV));
    }

    @SneakyThrows
    public byte[] decryptGCM(byte[] cipherBytes, byte[] secretKeyBytes, byte[] iv) {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, KEY_TYPE);
        AlgorithmParameterSpec algorithmParameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameterSpec);
        return cipher.doFinal(cipherBytes);
    }

    public static void main(String[] args) throws Exception {
        //Generate a AES SecretKey
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(256, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        //Generate an IV (Initialization Vector)
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        //Create a Cipher object and initialize with the SecretKey and IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        //Encrypt some plaintext
        String plaintext = "11KiO83byJnz55PuH9AYMKx1OIh0Q";

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        //Print the resulting ciphertext and IV
        System.out.println("Secret key: " + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        System.out.println("IV: " + Base64.getEncoder().encodeToString(iv));
        System.out.println("Data: " + Base64.getEncoder().encodeToString(ciphertext));
    }
}
