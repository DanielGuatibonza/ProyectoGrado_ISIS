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
		String bloque = sensor.getScript().getVariableValue(arg1);
		//System.out.println("ARG " + arg1 + " - " + bloque);
		String[] partes = bloque.split(" % ");
		String hashAnterior = partes[partes.length - 1].split("= ")[1];
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		ArrayList<Bloque> bloques = blockchain.darBloques();
		MessageDigest digest;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
			String ultimoBloqueHashAnterior = bloques.get(bloques.size() - 1).darHashAnterior();
			if(ultimoBloqueHashAnterior == null) {
				ultimoBloqueHashAnterior = "null";
			}
			if (hashAnterior.equals(ultimoBloqueHashAnterior)) {
				System.out.println("ENTRÉ");
				byte[] encodedhash = digest.digest(bloque.getBytes(StandardCharsets.UTF_8));
				String hashActual = new String(Hex.encode(encodedhash)); 
				System.out.println(sensor.getId() + " VALIDO " + hashActual);
				
				String ceros = ""; 
				for (int i = 0; i < ProofOfWork.DIFICULTAD; i++ ) {
					ceros += "0";
				}
				// Detener ejecución del bloque y agregar el bloque recibido
				if (hashActual.startsWith(ceros)) {
					sensor.getScript().addVariable(arg3, sensor.getScript().getVariableValue(arg2));
					blockchain.reemplazarBloque(bloque, hashAnterior, hashActual);	
				}
				else {
					sensor.getScript().addVariable(arg3, "invalido");
				}
			}
			else {
				sensor.getScript().addVariable(arg3, "invalido");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(sensor.getId() + "ARG3 " + sensor.getScript().getVariableValue(arg3));
		return 0;
	}

}
