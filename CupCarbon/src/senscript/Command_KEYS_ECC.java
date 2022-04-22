package senscript;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import crypto.*;

import device.SensorNode;

public class Command_KEYS_ECC extends Command {

	protected String arg1 = "" ;
	protected String arg2 = "" ;

	public Command_KEYS_ECC(SensorNode sensor, String arg1, String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2= arg2;
	}

	@Override
	public double execute() {
		Key[] llaves= ECC.generarLlaves();
		if (llaves !=null) 
		{
			PrivateKey prk=(PrivateKey)llaves[0];
			PublicKey pk= (PublicKey)llaves[1];


			byte[] v = ECC.savePrivateKey(prk) ;
			byte[] v2= ECC.savePublicKey(pk);
			sensor.getScript().addVariable(arg1, ECC.bytesToHex(v));
			sensor.getScript().addVariable(arg2, ECC.bytesToHex(v2));
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