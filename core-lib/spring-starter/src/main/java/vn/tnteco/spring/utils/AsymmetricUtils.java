package vn.tnteco.spring.utils;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AsymmetricUtils {
    private static final String KEY_TYPE = "RSA";

    private static final String CIPHER_TYPE = "RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING";

    @SneakyThrows
    public byte[] encrypt(String publicKeyStr, byte[] dataBytes) {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        return encrypt(publicKeyBytes, dataBytes);
    }

    @SneakyThrows
    public byte[] encrypt(byte[] publicKeyBytes, byte[] dataBytes) {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(dataBytes);
    }

    public byte[] decrypt(String privateKeyStr, byte[] dataBytes) {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        return decrypt(privateKeyBytes, dataBytes);
    }

    @SneakyThrows
    public byte[] decrypt(byte[] privateKeyBytes, byte[] dataBytes) {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(dataBytes);
    }

}
