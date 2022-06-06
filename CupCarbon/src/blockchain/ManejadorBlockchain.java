package blockchain;

import java.util.HashMap;
import java.util.Map;

public class ManejadorBlockchain {

	public final static int NUM_NODOS = 8;
	public static Map<Integer, Blockchain> blockchains = new HashMap<Integer, Blockchain>(); 
	public static boolean ejecutarPoW = true;
	
	public static synchronized Blockchain darBlockchain(int idEstacion) {
		return blockchains.get(idEstacion);
	}
	
}
