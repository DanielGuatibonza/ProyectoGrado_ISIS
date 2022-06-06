package senscript;

import blockchain.Blockchain;
import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_SAVE_ROUTE extends Command {

	/**
	 * ID de quien generó el modelo
	 */
	protected String arg1 = "" ;
	
	/**
	 * Ruta del modelo
	 */
	protected String arg2 = "" ;
	
	public Command_SAVE_ROUTE(SensorNode sensor, String arg1, String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
	
	@Override
	public synchronized double execute() {
		int idEstacion = Integer.parseInt(sensor.getScript().getVariableValue(arg1));
		String ruta = sensor.getScript().getVariableValue(arg2);
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		blockchain.guardarRuta(idEstacion, ruta);
		return 0;
	}

}
