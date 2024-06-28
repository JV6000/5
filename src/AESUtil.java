import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String KEY_FILE = "aes.key";

    public static String encrypt(String input, String key) throws Exception {
        return Base64.getEncoder().encodeToString(doCrypto(Cipher.ENCRYPT_MODE, input.getBytes(), key));
    }

    public static String decrypt(String input, String key) throws Exception {
        return new String(doCrypto(Cipher.DECRYPT_MODE, Base64.getDecoder().decode(input), key));
    }

    private static byte[] doCrypto(int cipherMode, byte[] input, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(cipherMode, secretKey);
        return cipher.doFinal(input);
    }

    public static String generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static String getKey() throws Exception {
        if (Files.exists(Paths.get(KEY_FILE))) {
            return new String(Files.readAllBytes(Paths.get(KEY_FILE)));
        } else {
            String key = generateKey();
            Files.write(Paths.get(KEY_FILE), key.getBytes());
            return key;
        }
    }
}
