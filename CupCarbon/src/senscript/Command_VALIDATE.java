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
	 * Variable donde se almacenar� la respuesta
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
		String[] partes = bloque.split(" % ");
		String hashAnterior = partes[partes.length - 1].split("= ")[1];
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		
		while (!blockchain.darValidando()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		ArrayList<Bloque> bloques = blockchain.darBloques();
		MessageDigest digest;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
			String ultimoBloqueHash = "null";
			if (bloques.size() > 0) {
				ultimoBloqueHash = bloques.get(bloques.size() - 1).darHash();
			}
			if (hashAnterior.equals(ultimoBloqueHash)) {
				byte[] encodedhash = digest.digest(bloque.getBytes(StandardCharsets.UTF_8));
				String hashActual = new String(Hex.encode(encodedhash)); 
				System.out.println(sensor.getId() + " VALIDANDO " + hashActual);
				
				String ceros = ""; 
				for (int i = 0; i < ProofOfWork.DIFICULTAD; i++ ) {
					ceros += "0";
				}
				// Detener ejecuci�n del bloque y agregar el bloque recibido
				if (hashActual.startsWith(ceros)) {
					sensor.getScript().addVariable(arg3, sensor.getScript().getVariableValue(arg2));
					System.out.println(sensor.getId() + " VALIDATE - ReemplazarBloque ");
					blockchain.reemplazarBloque(bloque, hashAnterior, hashActual);	
					blockchain.establecerValidando(false);
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
		System.out.println(sensor.getId() + " ARG3 " + sensor.getScript().getVariableValue(arg3));
		return 0;
	}

}
