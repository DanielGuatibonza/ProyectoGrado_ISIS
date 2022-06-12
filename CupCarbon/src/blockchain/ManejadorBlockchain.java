package blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManejadorBlockchain {

	public final static int NUM_NODOS = 8;
	public static Map<Integer, Blockchain> blockchains = new HashMap<Integer, Blockchain>(); 
	public static boolean ejecutarPoW = true;

	public static boolean blockchainTamanioAceptable(int idEstacion) {
		boolean tamanioAceptable = true;
		int numBloquesEstacion = blockchains.get(idEstacion).darBloques().size();
		Integer[] ids = blockchains.keySet().toArray(new Integer[blockchains.size()]);
		for(int i=0; i<ids.length; i++) {
			if(numBloquesEstacion > blockchains.get(ids[i]).darBloques().size()) {
				tamanioAceptable = false;
				break;
			}
		}
		return tamanioAceptable;
	}
	
	public static boolean necesitaValidaciones(int idEstacion) {
		boolean necesita = true;
		ArrayList<Bloque> bloquesEstacion = blockchains.get(idEstacion).darBloques();
		if(bloquesEstacion.get(bloquesEstacion.size()-1).darConfirmaciones() > 4) {
			necesita = false;
		}
		return necesita;
	}
	
	public static int contador = 0;
	public static void printStatus() {
		contador++;
		if(contador >= 1000) {
			contador = 0;
			System.out.println("\n--------------");
			Integer[] ids = blockchains.keySet().toArray(new Integer[blockchains.size()]);
			for(int i=0; i<ids.length; i++) {
				ArrayList<Bloque> bloquesActual = blockchains.get(ids[i]).darBloques();
				String texto = "Estacion " + ids[i] + " tiene " + bloquesActual.size() + " bloques";
				if(bloquesActual.size() > 0) {
					texto += ": - Tipo: " + bloquesActual.get(bloquesActual.size()-1).darProof() + 
								" - Estado: " + bloquesActual.get(bloquesActual.size()-1).darEstado() + 
								" - Aceptable: " + blockchainTamanioAceptable(ids[i]); 
				}
				System.out.println(texto);
			}
		}
	}
}
