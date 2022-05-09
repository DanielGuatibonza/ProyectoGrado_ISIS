package senscript;

import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_SAVE_VALIDATION extends Command {
	
	public Command_SAVE_VALIDATION(SensorNode sensor) {
		this.sensor = sensor ;
	}
	
	@Override
	public double execute() {
		System.out.println("Entró command save validation");
		ManejadorBlockchain.blockchains.get(sensor.getId()).recibirConfirmacion();
		return 0;
	}

}
