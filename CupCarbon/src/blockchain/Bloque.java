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

	"Transacciones: " + transaccionesStr + " | Merkle root: " + merkleRoot + " | Nonce: " + nonce + " | Hash anterior: " + hashAnterior;  				

	public Bloque(String hash) {
		try (FileReader reader = new FileReader("data/tareas.json"))
		{
			nonce = -(new Random()).nextInt(1000);
			transacciones = new ArrayList<Transaccion> ();
			transaccionesStr = "";
			estado = Estado.ABIERTO;
			digest = MessageDigest.getInstance("SHA-256");
			hashAnterior = hash;
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

	public Bloque(String cadena, String hash) {
		transacciones = new ArrayList<Transaccion> ();
		transaccionesStr = "";
		estado = Estado.CERRADO;
		digest = MessageDigest.getInstance("SHA-256");
		String[] partes = cadena.split(" % ");
		
		String transaccionesStr = partes[0].split(": ")[1];
		String[] transacciones = transaccionesStr.split("&");
		for (int i = 0; i < transacciones.length; i++) {
			
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
	
	public void establecerTimestamp (String pTimestamp) {
		timestamp = FORMATO.parse(pTimestamp);
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
		transaccionesStr += nueva.toString() + "&";
		byte[] encodedhash = digest.digest(transaccionesStr.getBytes(StandardCharsets.UTF_8));
		merkleRoot = new String(Hex.encode(encodedhash));
	}

	// True si lo generó, False si lo recibió
	public boolean ejecutar () {
		String respuesta = proof.ejecutar();
		boolean ejecuto = true;
		if (respuesta == null) {
			ejecuto = false;
		}
		else {
			hash = respuesta;
			estado = Estado.EN_ESPERA;
		}
		return ejecuto;
	}

	public void detenerEjecucion () {
		proof.detenerEjecucion();
	}

	public String toString () {
		return "Transacciones: " + transaccionesStr + " % Merkle root: " + merkleRoot + " % Nonce: " + nonce + " % Hash anterior: " + hashAnterior;  
	}
}
