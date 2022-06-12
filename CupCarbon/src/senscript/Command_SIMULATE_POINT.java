package senscript;

import blockchain.ManejadorSensores;
import blockchain.Transaccion;
import device.SensorNode;

import java.util.Date;
import java.util.Random;

public class Command_SIMULATE_POINT extends Command {

	/**
	 * Nombre de la variable donde se almacenará el dato
	 */
	protected String arg1;

	public Command_SIMULATE_POINT (SensorNode sensor, String arg1) {
		this.sensor = sensor;
		this.arg1 = arg1;

	}

	@Override
	public synchronized double execute() {

		int tiempo = ManejadorSensores.tiemposSensores.get(sensor.getId());
		double temperatura, pH, terneza, mermaPorCoccion, colorL, colorA, colorB;		
		double[] posicion = sensor.getPosition();
		String transacciones = ""; 
		
		for (int i = 0; i < 3; i++)
		{
			// Latitud y longitud
			if (posicion[0] >= 4.645 && posicion[0] <= 4.646 && posicion[1] >= -74.0735 && posicion[1] <= -74.0725) {
				temperatura = generarValor(-16.5, 0.5);
			}
			else {
				temperatura = generarValor(1, 1);
			}

			// Rango de 4 días
			if (tiempo <= 345600) {
				pH = generarValor(5.75, 0.035);
				terneza = generarValor (2.7, 0.35);
				mermaPorCoccion = generarValor (19.7, 0.7);
				colorL = generarValor (49.18, 0.36);
				colorA = generarValor (2.43, 0.23);
				colorB = generarValor (0.45, 0.46);
			}
			// Rango de 90 días
			else if (tiempo > 345600 && tiempo <= 7.776 * Math.pow(10, 6)) {
				pH = generarValor(5.61, 0.04);
				terneza = generarValor (3.1, 0.35);
				mermaPorCoccion = generarValor (21, 0.87);
				colorL = generarValor (48.86, 0.57);
				colorA = generarValor (3.50, 0.26);
				colorB = generarValor (0.27, 0.35);
			}
			else {
				pH = generarValor(6.08, 0.035);
				terneza = generarValor (3.4, 0.38);
				mermaPorCoccion = generarValor (21.5, 0.83);
				colorL = generarValor (49.85, 0.5);
				colorA = generarValor (4.37, 0.21);
				colorB = generarValor (0.34, 0.48);
			}

			transacciones += (new Transaccion (sensor.getId(), new Date(), tiempo, posicion[0], posicion[1], pH, temperatura, terneza,
					mermaPorCoccion, colorL, colorA, colorB)).toString() + "&";
			
		}
		transacciones = transacciones.substring(0, transacciones.length()-1);
		sensor.getScript().addVariable(arg1, transacciones);
		return 0;
	}

	private double generarValor (double media, double desviacion)
	{
		Random random = new Random();
		return media + (random.nextGaussian() * Math.pow(desviacion, 2));
	}
}
