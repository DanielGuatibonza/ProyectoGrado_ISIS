package senscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import device.SensorNode;

public class Command_WAIT_TIME extends Command {

	
	public Command_WAIT_TIME (SensorNode sensor) {
		this.sensor = sensor;
	}
	
	@Override
	public synchronized double execute() {

		double tiempoEspera = sensor.getDriftTime();
		File archivo = new File("data/tiempos_espera.csv");
		
		try (BufferedReader br = new BufferedReader (new FileReader(archivo))) {
			String linea;
			while ((linea = br.readLine()) != null){
				String[] fila = linea.split(",");
				if (Integer.parseInt(fila[0]) == sensor.getId()) {
					tiempoEspera *= Integer.parseInt(fila[1]);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tiempoEspera;
	}

}
