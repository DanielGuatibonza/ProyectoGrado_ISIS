package blockchain;

import java.util.HashMap;
import java.util.Map;

public class ManejadorModelos {
	
	public static Map<Integer, Integer> elegidosMejorModelo = new HashMap<Integer, Integer>();
	
	public static int numVotaciones() {
		int contador = 0;
		for(int idActual: elegidosMejorModelo.keySet()) {
			contador += elegidosMejorModelo.get(idActual);
		}
		return contador;
	}
	
	public static int darMejorID() {
		int mejorId = 0;
		int mejorConteo = 0;
		for(int idActual: elegidosMejorModelo.keySet()) {
			int conteoActual = elegidosMejorModelo.get(idActual);
			if(mejorConteo < conteoActual) {
				mejorConteo = conteoActual;
				mejorId = idActual;
			}
		}
		return mejorId;
	}
}
