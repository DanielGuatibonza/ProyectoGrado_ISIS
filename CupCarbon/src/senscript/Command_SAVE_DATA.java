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
		String mensaje = sensor.getScript().getVariableValue(arg1);
		String[] transacciones = mensaje.split("&");
		for (String t: transacciones) {
			ManejadorBlockchain.blockchains.get(sensor.getId()).recibirTransaccion(t);
		}
		System.out.println(sensor.getId() + " RECIBI DATOS " + transacciones.length);
		return 0;
	}

}
