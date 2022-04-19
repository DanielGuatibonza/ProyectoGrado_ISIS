package senscript;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.bouncycastle.util.encoders.Hex;

import blockchain.Blockchain;
import blockchain.Bloque;
import blockchain.ManejadorBlockchain;
import blockchain.ProofOfWork;
import device.SensorNode;

public class Command_VALIDATE extends Command {

	/**
	 * Cadena representativa del bloque
	 */
	protected String arg1 = "" ;
		
	/**
	 * ID de a quien le valido el bloque
	 */
	protected String arg2 = "" ;
	
	/**
	 * Variable donde se almacenará la respuesta
	 */
	protected String arg3 = "" ;
	
	public Command_VALIDATE (SensorNode sensor, String arg1, String arg2, String arg3) {
		this.sensor = sensor ;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
	}
	
	@Override
	public synchronized double execute() {
		
		String[] partes = arg1.split(" % ");
		String hashAnterior = partes[partes.length - 1].split(": ")[1];
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		ArrayList<Bloque> bloques = blockchain.darBloques();
		MessageDigest digest;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
			if (hashAnterior.equals(bloques.get(bloques.size() - 1).darHashAnterior())) {
				byte[] encodedhash = digest.digest(arg1.getBytes(StandardCharsets.UTF_8));
				String hashActual = new String(Hex.encode(encodedhash)); 
				
				String ceros = ""; 
				for (int i = 0; i < ProofOfWork.DIFICULTAD; i++ ) {
					ceros += "0";
				}
				// TODO Detener ejecución del bloque y agregar el bloque recibido
				if (hashActual.startsWith(ceros)) {
					sensor.getScript().addVariable(arg3, arg2);
					blockchain.detenerProof();
					blockchain.reemplazarBloque(arg1);
					
				}
				else {
					sensor.getScript().addVariable(arg3, "");
				}
			}
			else {
				sensor.getScript().addVariable(arg3, "");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
