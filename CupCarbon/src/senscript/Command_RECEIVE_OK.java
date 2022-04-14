package senscript;

import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_RECEIVE_OK extends Command {

	/**
	 * ID del propietario del bloque
	 */
	protected String arg1 = "" ;
	
	public Command_RECEIVE_OK (SensorNode sensor, String arg1) {
		this.sensor = sensor ;
		this.arg1 = arg1 ;
	}
	
	@Override
	public synchronized double execute() {
		
		if (sensor.getId() == Integer.parseInt(arg1)) {
			ManejadorBlockchain.blockchains.get(sensor.getId()).recibirConfirmacion();
		}
		
		return 0;
	}

}
