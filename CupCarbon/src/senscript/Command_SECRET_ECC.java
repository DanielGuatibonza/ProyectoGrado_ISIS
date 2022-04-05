package senscript;

import crypto.*;

import device.SensorNode;

public class Command_SECRET_ECC extends Command {

	protected String arg1 = "";
	protected String arg2 = "";
	protected String arg3 = "";

	/**
	 * Genera llave simétrica basado en llave pública del otro (arg1) y la privada propia (arg2).
	 * @param sensor
	 * @param arg1: Id otro.
	 * @param arg2: Llave pública del otro.
	 * @param arg3: Llave privada propia.
	 */
	public Command_SECRET_ECC(SensorNode sensor, String arg1, String arg2, String arg3) {
		this.sensor = sensor;
		this.arg1 = arg1;
		this.arg2= arg2;
		this.arg3= arg3;
	}

	@Override
	public double execute() {
		byte[] llavePublicaOtro = ECC.hexStringToByteArray(sensor.getScript().getVariableValue(arg2));

		if (llavePublicaOtro !=null) {
			byte[] llavePrivadaPropia = ECC.hexStringToByteArray(sensor.getScript().getVariableValue(arg3));
			byte[] secreto = ECC.doECDH(llavePrivadaPropia, llavePublicaOtro);
			System.out.println("Tamaño llave simétrica: "+secreto.length);
			ECCKeys_Manager.agregarLlaveCompartida(sensor.getId(), Integer.parseInt(sensor.getScript().getVariableValue(arg1)), secreto);
			return 0;
		}
		else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "GET TIME";
	}

}