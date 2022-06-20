package blockchain;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.encoders.Hex;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ProofOfWork implements ProofOfX {

	public static final int DIFICULTAD = 7;
	private Bloque bloque;
	//private boolean ejecutar;
	private MessageDigest digest;

	public ProofOfWork (Bloque pBloque) {
		bloque = pBloque;
		//ejecutar = true;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String ejecutar() {
		boolean nonceEncontrado = false;
		String hash = "";
		String ceros = ""; 
		for (int i = 0; i < DIFICULTAD; i++ ) {
			ceros += "0";
		}
		while (!nonceEncontrado && ManejadorBlockchain.ejecutarPoW) {
			if(bloque.darTransacciones().size() > 0) {
				byte[] encodedhash = digest.digest(bloque.toString().getBytes(StandardCharsets.UTF_8));
				hash = new String(Hex.encode(encodedhash));
				if (hash.startsWith(ceros)) {
					nonceEncontrado = true;
				}
				else {
					bloque.incrementarNonce();
				}
			}
			else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (!ManejadorBlockchain.ejecutarPoW) {
			hash = null;
		}
		return hash;
	}

	@Override
	public String toString () {
		return "Proof of Work, dificultad " + DIFICULTAD;
	}

}
