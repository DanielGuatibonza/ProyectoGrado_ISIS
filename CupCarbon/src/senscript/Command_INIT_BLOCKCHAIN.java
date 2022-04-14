package senscript;

import blockchain.Blockchain;
import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_INIT_BLOCKCHAIN extends Command {

	public Command_INIT_BLOCKCHAIN(SensorNode sensor) {
		this.sensor = sensor;
	}
	
	@Override
	public synchronized double execute() {
		
		ManejadorBlockchain.blockchains.put(this.sensor.getId(), new Blockchain (sensor));
		return 0;
	}

}
