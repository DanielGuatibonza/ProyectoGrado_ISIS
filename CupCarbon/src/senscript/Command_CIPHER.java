package senscript;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import crypto.ECC;

import device.SensorNode;
import simulation.WisenSimulation;

public class Command_CIPHER extends Command {

	/*
	 * Mensaje a cifrar
	 */
	protected String arg1 = "" ;
	
	/*
	 *  ID del sensor con el que me quiero comunicar
	 */
	protected String arg2 = "" ;
	
	/*
	 * Vector donde se encuentran las llaves simétricas
	 */
	protected String arg3 = "" ;
	
	/*
	 * Variable donde se almacenará el texto cifrado
	 */
	protected String arg4 = "" ;

	public Command_CIPHER(SensorNode sensor, String arg1, String arg2, String arg3, String arg4) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2= arg2;
		this.arg3= arg3;
		this.arg4= arg4;
	}

	@Override
	public synchronized double execute() {
		
		String x_str = sensor.getScript().getVariableValue(arg2);
		String m = sensor.getScript().getVariableValue(arg1);
		String[] tab = sensor.getScript().getVector(arg3);
		
		// Llave simétrica
		String val = (String) tab[Double.valueOf(x_str).intValue()];
		
		byte[] key = ECC.hexStringToByteArray(val);
		SecretKey llaveSimetricaServ = new SecretKeySpec(key, 0, key.length, "AES");
		
		byte[] textoCifrado = ECC.cifrar(llaveSimetricaServ, m);
		String v = ECC.bytesToHex(textoCifrado) ;
		sensor.getScript().addVariable(arg4, v);
		return 0;
	}

	@Override
	public String toString() {
		return "GET TIME";
	}

}