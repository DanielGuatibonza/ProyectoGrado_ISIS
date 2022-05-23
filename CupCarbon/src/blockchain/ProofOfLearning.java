package blockchain;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProofOfLearning implements ProofOfX {
	
	private JSONObject poleConf;
	
	public ProofOfLearning(Bloque pBloque, JSONObject pJsonObject) {
		poleConf = pJsonObject;
		System.out.println(poleConf);
	}
	
	public ProofOfLearning(String JSONObjectStr) {
		poleConf = (new JSONArray(JSONObjectStr)).getJSONObject(0);  
		JSONArray num_capas = poleConf.getJSONArray("num_capas");
	}

	@Override
	public String ejecutar() {
		// TODO Auto-generated method stub
		return "";
	}
	
	@Override
	public String toString () {
		return "Proof of Learning, parametros " + poleConf;
	}
}
