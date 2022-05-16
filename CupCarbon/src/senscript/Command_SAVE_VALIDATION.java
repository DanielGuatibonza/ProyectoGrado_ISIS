package senscript;

import blockchain.Blockchain;
import blockchain.Bloque;
import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_SAVE_VALIDATION extends Command {

	/**
	 * ID de quien generó el bloque
	 */
	protected String arg1 = "" ;

	public Command_SAVE_VALIDATION(SensorNode sensor, String arg1) {
		this.sensor = sensor ;
		this.arg1 = arg1;
	}

	@Override
	public double execute() {
		int idEstacion = Integer.parseInt(sensor.getScript().getVariableValue(arg1));

		if (idEstacion == sensor.getId()) {
			Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
			Bloque ultimoBloque = blockchain.darBloques().get(blockchain.darBloques().size() - 1);
			System.out.println(blockchain.darIdEstacion() + " - Entró command save validation; Estado bloque: " + ultimoBloque.darEstado() 
			+ " Validaciones: " + ultimoBloque.darConfirmaciones() +" Hash: " + ultimoBloque.darHash() + " Bloques: " + blockchain.darBloques().size());
			if (ultimoBloque.darEstado().equals(Bloque.Estado.EN_ESPERA)){
				blockchain.recibirConfirmacion();
			}
		}
		return 0;
	}

}
