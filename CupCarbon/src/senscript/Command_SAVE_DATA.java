package senscript;

import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_SAVE_DATA extends Command {

	/**
	 * Datos a guardar
	 */
	protected String arg1 = "" ;
	
	public Command_SAVE_DATA (SensorNode sensor, String arg1) {
		this.sensor = sensor;
		this.arg1 = arg1;
	}
	
	@Override
	public synchronized double execute() {
		
		String[] transacciones = arg1.split("&");
		
		for (String t: transacciones) {
			ManejadorBlockchain.blockchains.get(sensor.getId()).recibirTransaccion(t);
		}
		return 0;
	}

}
