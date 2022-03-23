package crypto;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Security;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.ECGenParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.math.ec.ECPoint;

public class ECC
{

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

	public static byte [] savePublicKey (PublicKey key) 
	{
		//return key.getEncoded();
		try {
			ECPublicKey eckey = (ECPublicKey)key;
			return eckey.getQ().getEncoded(true);
		}catch (Exception e) {
			return null;
		}
	}

	public static PublicKey loadPublicKey (byte [] data) throws Exception
	{
		/*KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		return kf.generatePublic(new X509EncodedKeySpec(data));*/

		ECParameterSpec params = ECNamedCurveTable.getParameterSpec("secp256r1");
		ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
		KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		return kf.generatePublic(pubKey);
	}

	public static byte [] savePrivateKey (PrivateKey key) 
	{
		//return key.getEncoded();
		try {
			ECPrivateKey eckey = (ECPrivateKey)key;
			return eckey.getD().toByteArray();
		}catch (Exception e) {
			return null;
		}
	}

	public static PrivateKey loadPrivateKey (byte [] data) throws Exception
	{
		//KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		//return kf.generatePrivate(new PKCS8EncodedKeySpec(data));

		ECParameterSpec params = ECNamedCurveTable.getParameterSpec("secp256r1");
		ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(data), params);
		KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		return kf.generatePrivate(prvkey);
	}

	public static byte[] doECDH (byte[] dataPrv, byte[] dataPub) 
	{
		try {
			KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
			ka.init(loadPrivateKey(dataPrv));
			ka.doPhase(loadPublicKey(dataPub), true);
			byte [] secret = ka.generateSecret();
			return secret;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Key[] generarLlaves() {

		try {

			Security.addProvider(new BouncyCastleProvider());

			KeyPairGenerator kpgen = KeyPairGenerator.getInstance("ECDH", "BC");
			kpgen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
			KeyPair pairA = kpgen.generateKeyPair();
			Key[] llaves= {pairA.getPrivate(),pairA.getPublic()};
			
			File myObj = new File("docs/llavesECC.txt");
			if (myObj.createNewFile()) {
				FileWriter myWriter = new FileWriter("docs/llavesECC.txt");
			      myWriter.write("Llave pública ECC: "+pairA.getPublic().getEncoded().length+"bytes");
			      myWriter.write("Llave privada ECC: "+pairA.getPrivate().getEncoded().length+"bytes");
			      myWriter.close();
			} else {
				
			}
			return llaves;
		}
		catch (Exception e) {
			return null;
		}
	}



	public static byte[] cifrar(SecretKey llave, String texto )
	{

		byte[] textoCifrado;
		try
		{

			Cipher cifrador = Cipher.getInstance("AES/ECB/PKCS5Padding");
			byte[] textoClaro=texto.getBytes();
			cifrador.init(1, llave);
			textoCifrado= cifrador.doFinal(textoClaro);

			return textoCifrado;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			return null;
		}
	}
	public static byte[] descifrar(SecretKey llave, byte[] texto)
	{
		byte[] textoClaro;
		try
		{

			Cipher cifrador = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro= cifrador.doFinal(texto);


		}
		catch(Exception e)
		{

			return "Llave erronea".getBytes();
		}
		return textoClaro;
	}

}





