package senscript;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import crypto.ECC;
import crypto.PK;
import device.SensorNode;
import simulation.WisenSimulation;

public class Command_RSAD extends Command {

	protected String arg1,arg2 = "" ;

	public Command_RSAD(SensorNode sensor, String arg1,String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
		this.arg2=arg2;
	}

	@Override
	public synchronized double execute() {


		String val = sensor.getScript().getVariableValue("$priv");
		byte[] key =  PK.hexStringToByteArray(val);
		try {
			
			PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
			String textoClaro=PK.Decrypt(sensor.getScript().getVariableValue(arg1), privateKey);
			
			sensor.getScript().addVariable("claro", textoClaro);
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