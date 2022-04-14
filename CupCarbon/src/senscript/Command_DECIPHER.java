package senscript;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

import crypto.ECC;
import crypto.ECCKeys_Manager;
import device.SensorNode;
import simulation.WisenSimulation;

public class Command_DECIPHER extends Command {

	/**
	 * Mensaje a descifrar
	 */
	protected String arg1 = "" ;

	/**
	 *  ID del sensor con el que me quiero comunicar
	 */
	protected String arg2 = "" ;

	/**
	 * Variable donde se guarda el texto claro
	 */
	protected String arg3 = "" ;
	
	public Command_DECIPHER(SensorNode sensor, String arg1, String arg2, String arg3) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2 = arg2;
		this.arg3 = arg3;

	}

	@Override
	public synchronized double execute() {

		// Id del sensor con quien se comunica
		String idN = sensor.getScript().getVariableValue(arg2);


		// Obtener llave simétrica
		SecretKey llaveSimetricaServ = ECCKeys_Manager.darLlaveCompartida(this.sensor.getId(), Integer.parseInt(idN));
		try {
			
			byte[] textoClaro = ECC.descifrar(llaveSimetricaServ, ECC.hexStringToByteArray(sensor.getScript().getVariableValue(arg1)));
			
			String v = ECC.hexstringToString(ECC.bytesToHex(textoClaro)) ;
			sensor.getScript().addVariable(arg3, v);
			return 0;
			
		}
		catch (Exception e) {
			System.out.println("Entra al catch");
			sensor.getScript().addVariable(arg3, "ERR");
			return 0;
		}
	}

	@Override
	public String toString() {
		return "GET TIME";
	}

}