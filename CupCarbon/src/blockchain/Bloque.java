package blockchain;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.util.encoders.Hex;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

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
	
	private ProofOfX proof;
	
	public Bloque(String cadena) {
		try (FileReader reader = new FileReader("data/tareas.json"))
	    {
			nonce = -(new Random()).nextInt(1000);
			hashAnterior = cadena;
			transacciones = new ArrayList<Transaccion> ();
			transaccionesStr = "";
			estado = Estado.ABIERTO;
			digest = MessageDigest.getInstance("SHA-256");
				
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(reader);
			JSONArray listaTareas = (JSONArray) obj;
			
			if (listaTareas.size() == 0){
				proof = new ProofOfWork(this);
			}
			else {
				proof = new ProofOfLearning();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int darConfirmaciones () {
		return confirmaciones;
	}
	
	public Estado darEstado () {
		return estado;
	}
	
	public String darHash () {
		return hash;
	}
	
	public String darHashAnterior () {
		return hashAnterior;
	}
	
	public int darNonce() {
		return nonce;
	}
	
	public ArrayList<Transaccion> darTransacciones () {
		return transacciones;
	}
	
	public void incrementarConfirmaciones () {
		confirmaciones++;
	}
	
	public void incrementarNonce () {
		nonce++;
	}
	
	public void cerrarBloque () {
		estado = Estado.CERRADO;
		timestamp = new Date ();
	}
	
	public void agregarTransaccion (Transaccion nueva) {
		transacciones.add(nueva);
		transaccionesStr += nueva.toString() + "\n";
		byte[] encodedhash = digest.digest(transaccionesStr.getBytes(StandardCharsets.UTF_8));
		merkleRoot = new String(Hex.encode(encodedhash));
	}
	
	public void ejecutar() {
		hash = proof.ejecutar();
		estado = Estado.EN_ESPERA;
	}
	
	public String toString () {
		return "Timestamp: " + Bloque.FORMATO.format(timestamp) + " | Transacciones: " + transaccionesStr + " | Merkle root: " + merkleRoot + " | Nonce: " + nonce + " | Hash anterior: " + hashAnterior;  
	}
}
