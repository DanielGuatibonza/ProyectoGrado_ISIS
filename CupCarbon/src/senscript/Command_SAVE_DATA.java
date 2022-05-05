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
		System.out.println("SAVE DATA ID:" + sensor.getId());
		String mensaje = sensor.getScript().getVariableValue(arg1);
		String[] transacciones = mensaje.split("&");
		System.out.println("TAMAÑO TRANSACCIONES: " + transacciones.length);
		for (String t: transacciones) {
			ManejadorBlockchain.blockchains.get(sensor.getId()).recibirTransaccion(t);
		}
		return 0;
	}

}
