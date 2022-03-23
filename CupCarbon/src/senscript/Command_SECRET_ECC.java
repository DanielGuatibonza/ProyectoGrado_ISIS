package senscript;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import crypto.*;

import device.SensorNode;
import simulation.WisenSimulation;

public class Command_SECRET_ECC extends Command {

	protected String arg1 = "" ;
	protected String arg2 = "" ;
	protected String arg3 = "" ;

	public Command_SECRET_ECC(SensorNode sensor, String arg1, String arg2, String arg3) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2= arg2;
		this.arg3= arg3;

	}

	@Override
	public double execute() {
		byte[] publicKey = ECC.hexStringToByteArray(sensor.getScript().getVariableValue(arg1));

		if (publicKey !=null) {
			String privateKey =sensor.getScript().getVariableValue(arg2);
			byte[] secreto = ECC.doECDH(ECC.hexStringToByteArray(privateKey), publicKey);
			System.out.println("Tamaño llave simétrica: "+secreto.length);
			String v = ECC.bytesToHex(secreto) ;
			sensor.getScript().addVariable(arg3, v);
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