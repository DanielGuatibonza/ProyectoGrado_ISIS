package senscript;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

import crypto.ECC;


import device.SensorNode;
import simulation.WisenSimulation;

public class Command_DECIPHER extends Command {

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
	 * Variable donde se guarda el texto claro
	 */
	protected String arg4 = "" ;
	
	public Command_DECIPHER(SensorNode sensor, String arg1, String arg2, String arg3, String arg4) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;

	}

	@Override
	public synchronized double execute() {

		// Id del sensor con quien se comunica
		String x_str = sensor.getScript().getVariableValue(arg2);

		String[] tab = sensor.getScript().getVector(arg3);

		// Obtener llave simétrica
		String val = tab[Double.valueOf(x_str).intValue()];

		byte[] key =ECC.hexStringToByteArray(val);
		SecretKey llaveSimetricaServ = new SecretKeySpec(key, 0, key.length, "AES");
		try {
			
			// Descifrar posición
			byte[] textoClaro = ECC.descifrar(llaveSimetricaServ, ECC.hexStringToByteArray(sensor.getScript().getVariableValue(arg1)));
			
			String v = ECC.hexstringToString(ECC.bytesToHex(textoClaro)) ;
			sensor.getScript().addVariable(arg4, v);
			return 0;
			
		}
		catch (Exception e) {
			System.out.println("Entra al catch");
			sensor.getScript().addVariable(arg4, "ERR");
			return 0;
		}
	}

	@Override
	public String toString() {
		return "GET TIME";
	}

}