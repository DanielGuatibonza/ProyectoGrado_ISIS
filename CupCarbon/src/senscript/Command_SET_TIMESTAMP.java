package senscript;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import blockchain.Blockchain;
import blockchain.Bloque;
import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_SET_TIMESTAMP extends Command {

	/**
	 * Timestamp a enviar
	 */
	protected String arg1 = "" ;
		
	/**
	 * Hash el bloque
	 */
	protected String arg2 = "" ;
	
	public Command_SET_TIMESTAMP (SensorNode sensor, String arg1, String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
	
	@Override
	public synchronized double execute() {
		
		String hashBloque = sensor.getScript().getVariableValue(arg2);
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		Date timestamp = null;
		try {
			timestamp = Bloque.FORMATO.parse(sensor.getScript().getVariableValue(arg1));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println("Set timestamp " + hashBloque);
		blockchain.establecerTimestamp(timestamp, hashBloque);
		return 0;
	}

}
