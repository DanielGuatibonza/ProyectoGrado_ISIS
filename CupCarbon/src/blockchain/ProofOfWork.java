package blockchain;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.encoders.Hex;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ProofOfWork implements ProofOfX {

	private int dificultad;
	private Bloque bloque;
	private MessageDigest digest;
	
	public ProofOfWork (int pDificultad, Bloque pBloque) {
		dificultad = pDificultad;
		bloque = pBloque;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void ejecutar() {
		boolean nonceEncontrado = false;
		while (!nonceEncontrado) {
			byte[] encodedhash = digest.digest(bloque.toString().getBytes(StandardCharsets.UTF_8));
			String hash = new String(Hex.encode(encodedhash));
			String ceros = ""; 
			for (int i = 0; i < dificultad; i++ ) {
				ceros += "0";
			}
			if (hash.startsWith(ceros)) {
				System.out.println("Hash " + hash);
				nonceEncontrado = true;
			}
			else {
				bloque.incrementarNonce();
			}
		}
	}

}
