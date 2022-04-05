package crypto;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ECCKeys_Manager {
	private static Map<Integer, Map<Integer, SecretKey>> diccionarioLlavesCompartidas = new HashMap<Integer, Map<Integer, SecretKey>>();
	
	public static void agregarLlaveCompartida(Integer idOrigen, Integer idDestino, byte[] bytesLlave) {
		SecretKey newSecret = new SecretKeySpec(bytesLlave, 0, bytesLlave.length, "AES");
		if(diccionarioLlavesCompartidas.containsKey(idOrigen)) 
		{
			Map<Integer, SecretKey> existingIdSecrets = diccionarioLlavesCompartidas.get(idOrigen);
			existingIdSecrets.put(idDestino, newSecret);
			diccionarioLlavesCompartidas.put(idOrigen, existingIdSecrets);
		}
		else 
		{
			Map<Integer, SecretKey> existingIdSecrets = new HashMap<Integer, SecretKey>();
			existingIdSecrets.put(idDestino, newSecret);
			diccionarioLlavesCompartidas.put(idOrigen, existingIdSecrets);
		}
	}
	
	public static SecretKey darLlaveCompartida(Integer idOrigen, Integer idDestino) {
		SecretKey secret = null;
		if(diccionarioLlavesCompartidas.containsKey(idOrigen)) 
		{
			Map<Integer, SecretKey> existingIdSecrets = diccionarioLlavesCompartidas.get(idOrigen);
			if(existingIdSecrets.containsKey(idDestino)) {
				secret = existingIdSecrets.get(idDestino);
			}
		}
		return secret;
	}
}
