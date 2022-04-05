package senscript;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import crypto.ECC;
import crypto.ECCKeys_Manager;
import device.SensorNode;
import simulation.WisenSimulation;

public class Command_CIPHER extends Command {

	/**
	 * Sensor
	 */
	protected SensorNode sensor;
	
	/**
	 * Mensaje a cifrar
	 */
	protected String arg1 = "" ;
	
	/**
	 *  ID del sensor con el que me quiero comunicar
	 */
	protected String arg2 = "" ;
	
	/**
	 * Variable donde se almacenará el texto cifrado
	 */
	protected String arg3 = "" ;

	public Command_CIPHER(SensorNode sensor, String arg1, String arg2, String arg3) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2= arg2;
		this.arg3= arg3;
	}

	@Override
	public synchronized double execute() {

		String m = sensor.getScript().getVariableValue(arg1);
		String idN = sensor.getScript().getVariableValue(arg2);
		
		// Llave simétrica
		SecretKey llaveSimetricaServ = ECCKeys_Manager.darLlaveCompartida(sensor.getId(), Integer.parseInt(idN));
		
		byte[] textoCifrado = ECC.cifrar(llaveSimetricaServ, m);
		String v = ECC.bytesToHex(textoCifrado) ;
		sensor.getScript().addVariable(arg3, v);
		return 0;
	}

	@Override
	public String toString() {
		return "GET TIME";
	}

}