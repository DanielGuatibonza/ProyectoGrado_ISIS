package blockchain;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import blockchain.Bloque.Estado;
import device.SensorNode;

public class Blockchain extends Thread {

	private SensorNode estacion;
	private ArrayList<Bloque> bloques;
	private ArrayList<Transaccion> transaccionesTemporales;
	private JSONArray jsonArray;

	public Blockchain (SensorNode pEstacion) {
		estacion = pEstacion;
		bloques = new ArrayList<Bloque>();
		bloques.add(new Bloque(null, estacion.getId()));
		transaccionesTemporales = new ArrayList<Transaccion>();
		
		estacion.getScript().addVariable("bloqueNuevo", "");
		estacion.getScript().addVariable("reenviarTransaccion", "false");
		estacion.getScript().addVariable("timestampUltimo", "");
		
		if(estacion.getId() == 1) {
			try (FileWriter file = new FileWriter("data/blockchain.json")) {
				jsonArray = new JSONArray();
	            file.write(jsonArray.toJSONString()); 
	            file.flush();
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}

	@Override
	public void run () {
		while (true) {
			Bloque bloqueActual = bloques.get(bloques.size() - 1);
			if (bloqueActual.darEstado().equals(Estado.ABIERTO)) {
				boolean bloqueGenerado = bloqueActual.ejecutar();
				if (bloqueGenerado) {
					estacion.getScript().addVariable("bloqueNuevo", bloqueActual.toString());
				} 				
			}
			else if (bloqueActual.darEstado().equals(Estado.EN_ESPERA)) {
				//System.out.println("RUN " + estacion.getId() + " - Confirmaciones: " + bloqueActual.darConfirmaciones());
				if (bloqueActual.darConfirmaciones() > 4) {
					System.out.println("RUN confirmaciones");
					bloqueActual.cerrarBloque();
					bloqueActual.establecerTimestamp(new Date());
					estacion.getScript().addVariable("timestampUltimo", bloqueActual.darTimestamp());

					Bloque nuevoBloque = new Bloque (bloqueActual.darHash(), estacion.getId());
					
					for (Transaccion t: transaccionesTemporales) {
						nuevoBloque.agregarTransaccion(t);
					}
					transaccionesTemporales = new ArrayList<Transaccion>();
					bloques.add(nuevoBloque);
					
					if(estacion.getId() == 1) {
						agregarBloqueAJSON(bloqueActual);
					}
				}
				else {
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void recibirTransaccion (String pTransaccion) {
		Transaccion temporal = new Transaccion (pTransaccion);
		boolean encontrada = false;
		for (int i=bloques.size()-1; i>=0; i--) {
			Bloque b = bloques.get(i);
			ArrayList<Transaccion> transacciones = b.darTransacciones();
			for (int j=transacciones.size()-1; j>=0; j--) {
				Transaccion t = transacciones.get(j);
				if (t.equals(temporal)) {
					encontrada = true;
					break;
				}
			}
			if (encontrada) {
				break;
			}
		}
		if (encontrada) {
			estacion.getScript().addVariable("reenviarTransaccion", "false");
		}
		else {
			Bloque ultimoBloque = bloques.get(bloques.size()-1);
			if (ultimoBloque.darEstado().equals(Estado.ABIERTO)) {
				ultimoBloque.agregarTransaccion(temporal);
			}
			else {
				transaccionesTemporales.add(temporal);
			}
			estacion.getScript().addVariable("reenviarTransaccion", "true");
		}
	}

	public ArrayList<Bloque> darBloques () {
		return bloques;
	}

	public void recibirConfirmacion () {
		System.out.println(estacion.getId() + " - Entró recibir confirmación. Bloque " + bloques.size() + " # transacciones: " + (bloques.get(0).darTransacciones().size()));
		bloques.get(bloques.size() - 1).incrementarConfirmaciones();
	}
	
	public void detenerProof () {
		Bloque ultimo = bloques.get(bloques.size() - 1);
		if (ultimo.darEstado().equals(Bloque.Estado.ABIERTO)) {
			ultimo.detenerEjecucion();
			bloques.remove(bloques.size() - 1);
		}
	}
	
	public void reemplazarBloque (String bloqueStr, String hashAnterior, String hashUltimo) {
		Bloque nuevoBloque = new Bloque (bloqueStr, hashAnterior);
		nuevoBloque.establecerHash(hashUltimo);
		bloques.add(nuevoBloque);
		bloques.add(new Bloque (hashUltimo, estacion.getId()));
		System.out.println("Reemplazar bloque");
	}
	
	public void establecerTimestamp(int idEstacion, Date timestamp) {
		Bloque actual = null;
		for (int i = bloques.size() - 1; i >= 0; i-- ) {
			actual = bloques.get(i);
			if (actual.darEstado().equals(Bloque.Estado.CERRADO) && actual.darIDEstacion() == idEstacion) {
				actual.establecerTimestamp(timestamp);
				break;
			}
		}
		if(estacion.getId() == 1) {
			agregarBloqueAJSON(actual);
		}	
	}
	
	public void agregarBloqueAJSON(Bloque bloque) {
		try (FileWriter file = new FileWriter("data/blockchain.json")) {
			JSONObject bloqueJson = bloque.darJSONObject();
			jsonArray.add(bloqueJson);
            file.write(jsonArray.toJSONString()); 
            file.flush();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
