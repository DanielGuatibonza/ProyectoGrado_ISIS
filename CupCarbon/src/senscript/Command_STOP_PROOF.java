package senscript;

import blockchain.Blockchain;
import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_STOP_PROOF extends Command {

	public Command_STOP_PROOF(SensorNode sensor)
	{
		this.sensor = sensor;
	}
	
	@Override
	public double execute() {
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		blockchain.detenerProof();
		return 0;
	}

}
