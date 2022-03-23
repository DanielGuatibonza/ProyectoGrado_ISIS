package senscript;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import crypto.ECC;
import crypto.PK;
import device.SensorNode;
import simulation.WisenSimulation;

public class Command_RSAE extends Command {

	protected String arg1,arg2 = "" ;
	
	public Command_RSAE(SensorNode sensor, String arg1,String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2= arg2;
	}

	@Override
	public synchronized double execute() {
		
		String x_str = sensor.getScript().getVariableValue(arg2);
		String m = sensor.getScript().getVariableValue(arg1);
		String[] tab = sensor.getScript().getVector("autos");
		
		String val = (String) tab[Double.valueOf(x_str).intValue()];
		byte[] key = PK.hexStringToByteArray(val);
		PublicKey publicKey;
		try {
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
			String textoCifrado= PK.Encrypt(m, publicKey);
			sensor.getScript().addVariable("cifrado", textoCifrado);
			return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} 
		
		
	}

	@Override
	public String toString() {
		return "GET TIME";
	}

}