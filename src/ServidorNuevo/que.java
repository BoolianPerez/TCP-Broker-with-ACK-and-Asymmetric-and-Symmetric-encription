package ServidorNuevo;

/*eolo*/
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class que{
    static KeyPair par=FirmaDigital.generarparKeys();
    public static void comparar(String hola, String chau){
        if(hola.equals(chau)){
            System.out.println("son iguales");
        }
        else {
            System.out.println("no son iguales");
        }
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

        String n="hol";
        String h=Base64.getEncoder().encodeToString(n.getBytes());

        byte[] no=Base64.getDecoder().decode(h);
        String ho=FirmaDigital.bytesAString(no);
        if(n.equals(ho)){
            System.out.println("f");
        }


    }
}
