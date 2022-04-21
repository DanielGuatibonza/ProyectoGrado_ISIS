package senscript;

import java.text.ParseException;
import java.util.ArrayList;

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
	 * ID de quien generó el bloque
	 */
	protected String arg2 = "" ;
	
	public Command_SET_TIMESTAMP (SensorNode sensor, String arg1, String arg2) {
		this.sensor = sensor ;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
	
	@Override
	public synchronized double execute() {
		
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		ArrayList<Bloque> bloques = blockchain.darBloques();
		int idEstacion = Integer.parseInt(arg2);
		for (int i = bloques.size() - 1; i >= 0; i-- ) {
			Bloque actual = bloques.get(i);
			if (actual.darEstado().equals(Bloque.Estado.CERRADO) && actual.darIDEstacion() == idEstacion) {
				try {
					actual.establecerTimestamp(Bloque.FORMATO.parse(arg1));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return 0;
	}

}
