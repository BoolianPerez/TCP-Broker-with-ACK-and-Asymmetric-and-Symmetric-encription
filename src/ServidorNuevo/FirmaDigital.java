package ServidorNuevo;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class FirmaDigital {
    private static KeyPair par;

    public FirmaDigital(){
        par=generarparKeys();
    }
    public static KeyPair generarparKeys(){
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }
    public static String cifradoPublic(String mensaje, PublicKey publico) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publico);
        byte[] secretMessageBytes = mensaje.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        return encodearABase64(encryptedMessageBytes);
    }
    public static String cifradoPrivate(String mensaje, PrivateKey privado) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, privado);
        byte[] secretMessageBytes = mensaje.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        return encodearABase64(encryptedMessageBytes);
    }
    public static String descifradoPrivate(String cifrado, PrivateKey privado) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privado);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(cifrado));
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        return decryptedMessage;
    }
    public static String descifradoPublic(String cifrado, PublicKey publico) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, publico);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(cifrado));
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        return decryptedMessage;
    }

    public static String encodearABase64(byte[] mensaje){
        String encodedMessage = Base64.getEncoder().encodeToString(mensaje);
        return encodedMessage;
    }


    public static String encriptarYHashear(String mensaje, PrivateKey privado) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hashbytes = digest.digest(
                mensaje.getBytes(StandardCharsets.UTF_8));
        String shaHex = bytesToHex(hashbytes);
        return cifradoPrivate(shaHex,privado);
    }
    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }



    public static String bytesAString(byte[] bytes){
        String mensaje= new String(bytes, StandardCharsets.UTF_8);
        return mensaje;
    }
    public static String hashear(String mensaje){
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        final byte[] hashbytes = digest.digest(
                mensaje.getBytes(StandardCharsets.UTF_8));
        String sha3Hex = bytesToHex(hashbytes);
        return sha3Hex;
    }

    public static PublicKey base64APublica(String publica64){
        try{
           KeyFactory keyFactory = KeyFactory.getInstance("RSA");
           return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publica64)));
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    public static String publicaABase64(PublicKey publica){
        return Base64.getEncoder().encodeToString(publica.getEncoded());
    }

}
