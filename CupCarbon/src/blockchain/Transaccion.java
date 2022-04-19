package blockchain;

import java.text.ParseException;
import java.util.Date;

public class Transaccion {
	
	private int idSensor;
	
	private Date timestamp;
	private int tiempoTranscurrido;
	
	private double latitud;
	private double longitud;
	
	private double pH;
	private double temperatura;
	private double terneza;
	private double mermaPorCoccion;
	private double colorL;
	private double colorA;
	private double colorB;
	
	public Transaccion (int pIDSensor, Date pTimestamp, int pTiempoTranscurrido, double pLatitud, double pLongitud, double pPH, double pTemperatura,
						double pTerneza, double pMermaPorCoccion, double pColorL, double pColorA, double pColorB) {
		idSensor = pIDSensor;
		timestamp = pTimestamp;
		tiempoTranscurrido = pTiempoTranscurrido;
		latitud = pLatitud;
		longitud = pLongitud;
		pH = pPH;
		temperatura = pTemperatura;
		terneza = pTerneza;
		mermaPorCoccion = pMermaPorCoccion;
		colorL = pColorL;
		colorA = pColorA;
		colorB = pColorB;
	}
	
	public Transaccion (String parametros) {
		String[] partes = parametros.split(" | ");
		idSensor = Integer.parseInt(partes[0].split(": ")[1]);
		
		try {
			timestamp = Bloque.FORMATO.parse(partes[1].split(": ")[1]);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		tiempoTranscurrido = Integer.parseInt(partes[2].split(": ")[1]);
		latitud = Double.parseDouble(partes[3].split(": ")[1]);
		longitud = Double.parseDouble(partes[4].split(": ")[1]);
		pH = Double.parseDouble(partes[5].split(": ")[1]);
		temperatura = Double.parseDouble(partes[6].split(": ")[1]);
		terneza = Double.parseDouble(partes[7].split(": ")[1]);
		mermaPorCoccion = Double.parseDouble(partes[8].split(": ")[1]);
		
		String colores = partes[9].split(": ")[1];
		colores = colores.substring(1, colores.length() - 1);
		String[] divisiones = colores.split(", ");
		colorL = Double.parseDouble(divisiones[0].split(" = ")[1]);
		colorA = Double.parseDouble(divisiones[1].split(" = ")[1]);
		colorB = Double.parseDouble(divisiones[2].split(" = ")[1]);
	}
	
	@Override
	public String toString () {
		return "ID Sensor: " + idSensor + " | Timestamp: " + Bloque.FORMATO.format(timestamp) + " | Tiempo transcurrido: " + tiempoTranscurrido + " segundos | Latitud: " + 
				latitud + " | Longitud: " + longitud + " | pH: " + pH + " | Temperatura: " + temperatura + " �C | Terneza: " + terneza + " | Merma por cocci�n: " + mermaPorCoccion + 
				" | Color: (L* = " + colorL + ", a* = " + colorA + ", b* = " + colorB + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaccion other = (Transaccion) obj;
		if (idSensor != other.idSensor)
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}
	
	
}