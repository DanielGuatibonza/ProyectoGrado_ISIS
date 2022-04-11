package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.util.encoders.Hex;

public class Bloque {

	public final static SimpleDateFormat FORMATO = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
	
	private String hash;
	private String hashAnterior;
	private Date timestamp;
	private ArrayList<Transaccion> transacciones;
	private String transaccionesStr;	
	private String merkleRoot;
	private int nonce;
	
	private int confirmaciones;
	private Estado estado;
	
	public enum Estado {
		ABIERTO,
		EN_ESPERA,
		CERRADO
	}
	
	private MessageDigest digest;
	
	private Blockchain blockchain;
	
	public Bloque(Blockchain pBlockchain, String pHashAnterior) {
		blockchain = pBlockchain;
		nonce = -(new Random()).nextInt(1000);
		hashAnterior = pHashAnterior;
		transacciones = new ArrayList<Transaccion> ();
		transaccionesStr = "";
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Transaccion> darTransacciones () {
		return transacciones;
	}
	
	public int darNonce() {
		return nonce;
	}
	
	public void incrementarNonce () {
		nonce ++;
	}
	
	public void cerrarBloque (String hashFinal) {
		hash = hashFinal;
		estado = Estado.CERRADO;
	}
	
	public void establecerTimestamp () {
		
	}

	public void agregarTransaccion (Transaccion nueva) {
		transacciones.add(nueva);
		transaccionesStr += nueva.toString() + "\n";
		byte[] encodedhash = digest.digest(transaccionesStr.getBytes(StandardCharsets.UTF_8));
		merkleRoot = new String(Hex.encode(encodedhash));
	}
	
	public String toString () {
		return "Hash: " + hash + " | Transacciones: " + transaccionesStr + " | Merkle root: " + merkleRoot + " | Nonce: " + nonce + " | Hash anterior: " + hashAnterior;  
	}
}
