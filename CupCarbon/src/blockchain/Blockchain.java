package blockchain;

import java.util.ArrayList;
import java.util.Date;

import blockchain.Bloque.Estado;
import device.SensorNode;

public class Blockchain extends Thread {

	private SensorNode estacion;
	private ArrayList<Bloque> bloques;
	private ArrayList<Transaccion> transaccionesTemporales;

	public Blockchain (SensorNode pEstacion) {
		estacion = pEstacion;
		bloques = new ArrayList<Bloque>();
		bloques.add(new Bloque("", estacion.getId()));
		transaccionesTemporales = new ArrayList<Transaccion>();
		
		estacion.getScript().addVariable("bloqueNuevo", "");
		estacion.getScript().addVariable("reenviarTransaccion", "false");
		estacion.getScript().addVariable("timestampUltimo", "");
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
				if (bloqueActual.darConfirmaciones() > 4) {
					bloqueActual.cerrarBloque();
					bloqueActual.establecerTimestamp(new Date());
					estacion.getScript().addVariable("timestampUltimo", bloqueActual.darTimestamp());

					Bloque nuevoBloque = new Bloque (bloqueActual.darHash(), estacion.getId());
					
					for (Transaccion t: transaccionesTemporales) {
						nuevoBloque.agregarTransaccion(t);
					}
					transaccionesTemporales = new ArrayList<Transaccion>();
					bloques.add(nuevoBloque);
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
		for (Bloque b: bloques) {
			ArrayList<Transaccion> transacciones = b.darTransacciones();
			for (Transaccion t: transacciones) {
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
	}
}
