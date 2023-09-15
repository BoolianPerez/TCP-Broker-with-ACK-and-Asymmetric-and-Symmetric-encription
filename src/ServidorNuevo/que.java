package ServidorNuevo;

/*eolo*/
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class que{

    public static void comparar(String hola, String chau){
        if(hola.equals(chau)){
            System.out.println("son iguales");
        }
        else {
            System.out.println("no son iguales");
        }
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        KeyPair perlo=FirmaDigital.generarparKeys();
        String menjae="ACK";
        System.out.println(FirmaDigital.getPublicKey(perlo).toString());
        byte[] h=FirmaDigital.convertirKeyABytes(FirmaDigital.getPublicKey(perlo));
        System.out.println(h);
        String ig=FirmaDigital.encodearABase64(h);
        System.out.println(ig);
        byte[] ho=FirmaDigital.desencodearDeBase64(ig);
        System.out.println(ho);
        PublicKey key1=FirmaDigital.convertirBytesAKey(ho);
        System.out.println(key1.toString());
        if(FirmaDigital.getPublicKey(perlo).equals(key1)){
            System.out.println("Hola");
        }
        else{
            throw new NullPointerException();
        }



    }
}
