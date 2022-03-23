package crypto;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

public class PK {


	public PK()
	{

	}
	public static KeyPair generateKey() {
		try {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair par=  keyGen.generateKeyPair();
        File myObj = new File("docs/llavesRSA.txt");
		if (myObj.createNewFile()) {
			FileWriter myWriter = new FileWriter("docs/llavesRSA.txt");
		      myWriter.write("Llave pública RSA: "+par.getPublic().getEncoded().length+"bytes");
		      myWriter.write("Llave privada RSA: "+par.getPrivate().getEncoded().length+"bytes");
		      myWriter.close();
		} else {
			
		}
        return par;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static String Encrypt(String plain, PublicKey publicKeyO)  {
		try {
			String encrypted;
			byte[] encryptedBytes;      


			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKeyO);
			encryptedBytes = cipher.doFinal(plain.getBytes());

			encrypted = bytesToHex(encryptedBytes);
			return encrypted;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String Decrypt(String result, PrivateKey privateKey)  {
		try {
			byte[] decryptedBytes;

			String decrypted;

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			decryptedBytes = cipher.doFinal(hexStringToByteArray(result));
			decrypted = new String(decryptedBytes);
			return decrypted;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] hexStringToByteArray(String s) {
		byte[] cadena =DatatypeConverter.parseHexBinary(s);

		return cadena;
	}
	public static String hexstringToString(String hexString) {
		try {
		byte[] bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(hexString);
		return new String(bytes, "UTF-8");
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static void main(String[] args) {
		KeyPair llaves1 = generateKey();
		PublicKey p1=llaves1.getPublic();
		PrivateKey priv1=llaves1.getPrivate();
		KeyPair llaves2 = generateKey();
		PublicKey p2=llaves2.getPublic();
		PrivateKey priv2=llaves2.getPrivate();
		String t1="soy texto 1";
		String t2="soy texto 2";
		String pep1;
		String pep2;
		System.out.println("primero");
		System.out.println("Texto 1 cifrado con p2: "+ (pep1=Encrypt(t1, p2)));
		System.out.println("segundo");
		System.out.println("Texto 2 cifrado con p1: "+(pep2=Encrypt(t2,p1)));
		System.out.println("tercero");
		System.out.println("Texto 1 descifrado con priv2: "+Decrypt(pep1, priv2));
		System.out.println("cuarto");
		System.out.println("Texto 2 descifrado con priv1: "+Decrypt(pep2, priv1));
		
		
		
		
	}
}
