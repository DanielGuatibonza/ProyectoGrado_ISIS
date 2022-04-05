package senscript;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import crypto.ECC;
import crypto.ECCKeys_Manager;
import device.SensorNode;
import simulation.WisenSimulation;

public class Command_RECV_DECIPHER extends Command {

	protected String arg1 = ""; 
	protected String arg2 = "";
	
	/**
	 * Comando para recibir un mensaje cifrado
	 * @param sensor
	 * @param arg1: Variable donde almacenar el texto descifrado
	 * @param arg2: Id remitente
	 */
	public Command_RECV_DECIPHER(SensorNode sensor, String arg1, String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	@Override
	public double execute() {
		WisenSimulation.simLog.add("S" + sensor.getId() + " READ");
		String mensajeCifrado = sensor.readMessage(arg1);
		Integer idReceptor = sensor.getId();
		Integer idRemitente = Integer.parseInt(sensor.getScript().getVariableValue(arg2));
		SecretKey llaveSimetricaServ = ECCKeys_Manager.darLlaveCompartida(idReceptor, idRemitente);
		try {
			
			// Descifrar posición
			byte[] textoClaro = ECC.descifrar(llaveSimetricaServ, ECC.hexStringToByteArray(mensajeCifrado));
			
			String textoClaroStr = ECC.hexstringToString(ECC.bytesToHex(textoClaro)) ;
			sensor.getScript().addVariable(arg1, textoClaroStr);
			return 0;
			
		}
		catch (Exception e) {
			System.out.println("Entra al catch");
			sensor.getScript().addVariable(arg1, "ERR");
			return 0;
		}
	}

	@Override
	public String getArduinoForm() {
		String s = "";
		s += "\txbee.getResponse().getRx64Response(rx);\n";
		s += "\trdata = rx.getData();\n";
		s += "\tString "+arg1+" = \"\" ;\n";
		
		s += "\tfor(int i=0; i<20; i++) {\n";
		s += "\t\t" + arg1 + " += (char) rdata[i];\n";
		s += "\t}\n";
		
		return s;
	}

	@Override
	public String toString() {
		return "READ";
	}

	@Override
	public String finishMessage() {
		return ("S" + sensor.getId() + " has finished reading.");
	}
	
}
