package senscript;

import blockchain.Blockchain;
import blockchain.Bloque;
import blockchain.ManejadorBlockchain;
import device.SensorNode;

public class Command_SAVE_VALIDATION extends Command {
	
	public Command_SAVE_VALIDATION(SensorNode sensor) {
		this.sensor = sensor ;
	}
	
	@Override
	public double execute() {
		Blockchain blockchain = ManejadorBlockchain.blockchains.get(sensor.getId());
		Bloque ultimoBloque = blockchain.darBloques().get(blockchain.darBloques().size() - 1);
		System.out.println(blockchain.darIdEstacion() + " - Entró command save validation; Estado bloque: " + ultimoBloque.darEstado() + " Validaciones: " + ultimoBloque.darConfirmaciones() +" Hash: " + ultimoBloque.darHash() + " Bloques: " + blockchain.darBloques().size());
		if (blockchain.darBloques().get(blockchain.darBloques().size() - 1).darEstado().equals(Bloque.Estado.EN_ESPERA)){
			blockchain.recibirConfirmacion();
		}
		return 0;
	}

}
