package blockchain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Hex;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Bloque {

	public final static SimpleDateFormat FORMATO = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static synchronized String formatDate(Date fecha) {
		return FORMATO.format(fecha);
	}
	public static synchronized Date parseDate(String fechaStr) {
		Date fecha = null;
		try {
			fecha =  FORMATO.parse(fechaStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return fecha;
	}

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
		try (InputStream is = FileUtils.openInputStream(new File("data/tareas.json")))
		{
			idEstacion = pIdEstacion;
			nonce = (new Random()).nextInt(1000);
			transacciones = new ArrayList<Transaccion> ();
			transaccionesStr = null;
			estado = Estado.ABIERTO;
			digest = MessageDigest.getInstance("SHA-256");
			hashAnterior = hash;
			timestamp = parseDate("1970-01-01 00:00:00");
			JSONTokener tokener = new JSONTokener(is);
			JSONArray listaTareas = new JSONArray(tokener);

			if (listaTareas.length() == 0){
				proof = new ProofOfWork(this);
			}
			else {
				proof = new ProofOfLearning(this, listaTareas.getJSONObject(0));
				timestamp = new Date();
			}
			
			System.out.println("BLOQUE CREADO: " + toString());
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
			timestamp = parseDate(partes[5].split("= ")[1]);
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
		return formatDate(timestamp);
	}

	public ArrayList<Transaccion> darTransacciones () {
		return transacciones;
	}
	
	public ProofOfX darProof() {
		return proof;
	}

	public void incrementarConfirmaciones () {
		confirmaciones++;
	}
	
	public void establecerEstado(Estado pEstado) {
		estado = pEstado;
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
	
	public void asignarHash() {
		byte[] encodedhash = digest.digest(toString().getBytes(StandardCharsets.UTF_8));
		hash = new String(Hex.encode(encodedhash));
	}

	// True si lo generó, False si lo recibió
	public boolean ejecutarPoW () {
		System.out.println(idEstacion + " - Ejecutando PoW");
		String respuesta = (String) proof.ejecutar();
		boolean ejecuto = true;
		if (respuesta == null) {
			ejecuto = false;
			System.out.println(idEstacion + " - Terminó PoW, pero no ganó");
		}
		else {
			hash = respuesta;
			estado = Estado.EN_ESPERA;
			System.out.println(idEstacion + " - Terminó PoW y ganó");
		}
		return ejecuto;
	}
	
	public String ejecutarPoLe() {
		String ruta = "models/autoencoder_" + idEstacion + ".bin";
		MultiLayerNetwork net = (MultiLayerNetwork) proof.ejecutar();
		try {
			net.save(new File(ruta));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(idEstacion + " MODELO GUARDADO " + ruta);
		return ruta;
	}
	
	public JSONObject darJSONObject() {
		JSONObject bloqueJson = new JSONObject();
		bloqueJson.put("tipoProof", proof.toString());
		bloqueJson.put("idEstacion", idEstacion);
		bloqueJson.put("hash", hash);
		bloqueJson.put("hashAnterior", hashAnterior);
		bloqueJson.put("timestamp", formatDate(timestamp));
		bloqueJson.put("merkleRoot", merkleRoot);
		bloqueJson.put("nonce", nonce);
		
		JSONArray arrayTransacciones = new JSONArray();
		JSONObject transaccionJson = null;
		for(Transaccion t: transacciones) {
			transaccionJson = t.darJSONObject();
			arrayTransacciones.put(transaccionJson);
		}
		bloqueJson.put("transacciones", arrayTransacciones);
		
		return bloqueJson;
	}

	@Override
	public String toString () {
		return "Transacciones= " + transaccionesStr + " % Merkle root= " + merkleRoot + " % Nonce= " + nonce + " % Proof= " + proof.toString() + " % ID Estacion= " + idEstacion + " % Timestamp= " + formatDate(timestamp) + " % Hash anterior= " + hashAnterior;  
	}
}
