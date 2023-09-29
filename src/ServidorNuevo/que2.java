package ServidorNuevo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class que2 {
    static KeyPair par= FirmaDigital.generarparKeys();
    public static String enviarMensajeSimetrico(String mensaje, SecretKey clave, PrivateKey yo){
        try {
            String laMensajeada=FirmaDigital.cifrarSimetrica(mensaje, clave);
            return laMensajeada +"¬"+ FirmaDigital.encriptarYHashear(mensaje,yo);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
    public static String recibirMensajeSimetrico(String mensaje, PublicKey publico, SecretKey clave){
        try {
            String[] ambosMensajes=mensaje.split("¬");
            String mensaje1=FirmaDigital.descifrarSimetrica(ambosMensajes[0], clave);
            String hash=FirmaDigital.descifradoPublic(ambosMensajes[1], publico);
            String mensaje1Hasheado=FirmaDigital.hashear(mensaje1);
            if(hash.equals(mensaje1Hasheado)){
                return mensaje1;
            }
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        return "NO SE PUEDE CONFIRMAR EL ORIGEN DEL MENSAJERO. POR LO TANTO, NO VA A RECIBIR EL MENSAJE GRACIAs";
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
    }
}
