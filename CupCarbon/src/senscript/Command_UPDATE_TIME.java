package senscript;

import blockchain.ManejadorSensores;
import device.SensorNode;

public class Command_UPDATE_TIME extends Command {

	/**
	 * Tiempo a sumar
	 */
	protected String arg1 = "" ;
	
	public Command_UPDATE_TIME (SensorNode sensor, String arg1) {
		this.sensor = sensor;
		this.arg1 = arg1;
	}
	
	@Override
	public synchronized double execute() {
		
		Integer tiempoAnterior = ManejadorSensores.tiemposSensores.get(sensor.getId());
		if(tiempoAnterior == null) {
			tiempoAnterior = 0;
		}
		ManejadorSensores.tiemposSensores.put(sensor.getId(), tiempoAnterior + Integer.parseInt(sensor.getScript().getVariableValue(arg1)));
		return 0;
	}

}
