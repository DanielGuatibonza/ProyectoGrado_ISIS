package blockchain;

import java.util.ArrayList;
import java.util.LinkedList;

public class Blockchain extends Thread {

	private int id;
	private LinkedList<Bloque> bloques;
	private boolean validarUltimoBloque;
	private boolean reenviarTransaccion;
	
	public Blockchain (int pID)
	{
		id = pID;
		validarUltimoBloque = false;
		reenviarTransaccion = false;
		bloques = new LinkedList<Bloque>();
		bloques.add(new Bloque(this, ""));
	}
	
	public void run () {
		
	}
	
	public void cambiarValidacion (boolean pValidar) {
		validarUltimoBloque = pValidar;
	}
	
	public boolean darValidar () {
		return validarUltimoBloque;
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
			reenviarTransaccion = false;
		}
		else {
			bloques.getLast().agregarTransaccion(temporal);
			reenviarTransaccion = true;
		}
	}
	
	public void recibirConfirmacion () {
		
	}
}
