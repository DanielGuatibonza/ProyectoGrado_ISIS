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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Bloque {

	public final static SimpleDateFormat FORMATO = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

	private int idEstacion;
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


	public Bloque(String hash, int pIdEstacion) {
		try (FileReader reader = new FileReader("data/tareas.json"))
		{
			idEstacion = pIdEstacion;
			nonce = (new Random()).nextInt(1000);
			transacciones = new ArrayList<Transaccion> ();
			transaccionesStr = null;
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
		try {
			transacciones = new ArrayList<Transaccion> ();
			estado = Estado.CERRADO;
			digest = MessageDigest.getInstance("SHA-256");
			String[] partes = cadena.split(" % ");
			transaccionesStr = partes[0].split("= ")[1];
			String[] pTransacciones = transaccionesStr.split("&");
			for (int i = 0; i < pTransacciones.length; i++) {
				if(!pTransacciones[i].equals("")) {
					transacciones.add(new Transaccion(pTransacciones[i]));	
				}
			}
			merkleRoot = partes[1].split("= ")[1];
			nonce = Integer.parseInt(partes[2].split("= ")[1]);
			
			if (partes[3].split("= ")[1].startsWith("Proof of Work")) {
				proof = new ProofOfWork(this);
			}
			else {
				proof = new ProofOfLearning();
			}
			
			idEstacion = Integer.parseInt(partes[4].split("= ")[1]);
			hashAnterior = partes[partes.length - 1].split("= ")[1];
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public int darIDEstacion () {
		return idEstacion;
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
	
	public String darTimestamp () {
		return FORMATO.format(timestamp);
	}

	public ArrayList<Transaccion> darTransacciones () {
		return transacciones;
	}

	public void incrementarConfirmaciones () {
		confirmaciones++;
	}

	public void establecerTimestamp (Date pTimestamp) {
		timestamp = pTimestamp;
	}
	
	public void establecerHash (String pHash) {
		hash = pHash;
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
		if(transaccionesStr == null) {
			transaccionesStr = nueva.toString() + "&";
		} else {
			transaccionesStr += nueva.toString() + "&";
		}
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
	
	public JSONObject darJSONObject() {
		JSONObject bloqueJson = new JSONObject();
		bloqueJson.put("idEstacion", idEstacion);
		bloqueJson.put("hash", hash);
		bloqueJson.put("hashAnterior", hashAnterior);
		bloqueJson.put("timestamp", timestamp.toString());
		bloqueJson.put("merkleRoot", merkleRoot);
		bloqueJson.put("nonce", nonce);
		
		JSONArray arrayTransacciones = new JSONArray();
		JSONObject transaccionJson = null;
		for(Transaccion t: transacciones) {
			transaccionJson = t.darJSONObject();
			arrayTransacciones.add(transaccionJson);
		}
		bloqueJson.put("transacciones", arrayTransacciones);
		
		return bloqueJson;
	}

	@Override
	public String toString () {
		return "Transacciones= " + transaccionesStr + " % Merkle root= " + merkleRoot + " % Nonce= " + nonce + " % Proof= " + proof.toString() + " % ID Estacion= " + idEstacion + " % Hash anterior= " + hashAnterior;  
	}
}
