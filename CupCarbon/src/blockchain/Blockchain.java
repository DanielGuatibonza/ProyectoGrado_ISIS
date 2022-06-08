package blockchain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import blockchain.Bloque.Estado;
import device.SensorNode;

public class Blockchain extends Thread {

	private SensorNode estacion;
	private ArrayList<Bloque> bloques;
	private ArrayList<Transaccion> transaccionesTemporales;
	private JSONArray jsonArray;
	private boolean validando;
	private Map<Integer, String> rutasModelos;

	public Blockchain (SensorNode pEstacion) {
		estacion = pEstacion;
		validando = false;
		bloques = new ArrayList<Bloque>();
		bloques.add(new Bloque("null", estacion.getId()));
		transaccionesTemporales = new ArrayList<Transaccion>();
		rutasModelos = new HashMap<Integer, String>();

		estacion.getScript().addVariable("bloqueNuevo", "");
		estacion.getScript().addVariable("reenviarTransaccion", "false");
		estacion.getScript().addVariable("timestampUltimo", "");
		estacion.getScript().addVariable("hashUltimo", "");
		estacion.getScript().addVariable("rutaModelo", "");
		
		try (FileWriter file = new FileWriter("data/blockchain-" + estacion.getId() + ".json")) {
			jsonArray = new JSONArray();
			file.write(jsonArray.toString()); 
			file.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run () {
		while (true) {
			if(!validando) {
				Bloque bloqueActual = bloques.get(bloques.size() - 1);
				if(bloqueActual.darProof() instanceof ProofOfWork) {
					if (bloqueActual.darEstado().equals(Estado.ABIERTO)) {
						boolean bloqueGenerado = bloqueActual.ejecutarPoW();
						if (bloqueGenerado) {
							ManejadorBlockchain.ejecutarPoW = false;
							estacion.getScript().addVariable("bloqueNuevo", bloqueActual.toString());
						} else {
							validando = true;
							bloques.remove(bloques.size() - 1);								
						}
					}
					else if (bloqueActual.darEstado().equals(Estado.EN_ESPERA)) {
						if (bloqueActual.darConfirmaciones() > ManejadorBlockchain.NUM_NODOS / 2) {
							bloqueActual.cerrarBloque();
							bloqueActual.establecerTimestamp(new Date());
							estacion.getScript().addVariable("timestampUltimo", bloqueActual.darTimestamp());
							estacion.getScript().addVariable("hashUltimo", bloqueActual.darHash());

							crearNuevoBloque(bloqueActual.darHash());
							ManejadorBlockchain.ejecutarPoW = true;
							agregarBloqueAJSON(bloqueActual);
						}
						else {
							esperarMillis(100);
						}
					}
				}
				else {
					if (bloqueActual.darEstado().equals(Estado.ABIERTO)) {
						String rutaModelo = bloqueActual.ejecutarPoLe();
						ManejadorModelos.elegidosMejorModelo.put(estacion.getId(), 0);

						estacion.getScript().addVariable("rutaModelo", rutaModelo);

						DataSetIterator datasetIterator = null;
						int numModelosRevisados = 0;
						int idMejorModelo = 0;
						double mejorMSE = Double.MAX_VALUE;
						while (numModelosRevisados < ManejadorBlockchain.NUM_NODOS - 1) {
							if (bloqueActual.darEstado().equals(Estado.ABIERTO) && bloqueActual.darTransacciones().size() > 0) {
								bloqueActual.establecerEstado(Estado.EN_ESPERA);
								crearCSVTemporal(bloqueActual);
							}
							if (rutasModelos.keySet().size() > 0 && bloqueActual.darEstado().equals(Estado.EN_ESPERA)) {
								System.out.println(estacion.getId() + "ANTES: Entra en espera " + rutasModelos.keySet().size());
								Set<Integer> llavesRutasModelos = rutasModelos.keySet();
								for(int idActual : llavesRutasModelos) {
									datasetIterator = ProofOfLearning.readCSVDataset("data/CSVs Temporales/bloque_actual_" + estacion.getId() + ".csv");
									String rutaActual = rutasModelos.get(idActual);
									System.out.println(" Quién soy: " + estacion.getId() + " - Quién valido: " + idActual);
									double mseActual = Blockchain.revisarModelo(rutaActual, datasetIterator);
									if(mseActual < mejorMSE) {
										mejorMSE = mseActual;
										idMejorModelo = idActual;
									}
									numModelosRevisados++;
								}
								rutasModelos = new HashMap<Integer, String>();
							} else {
								esperarMillis(100);
							}
						}
						System.out.println( estacion.getId() + " Salien2 primer while revisión");

						ManejadorModelos.elegidosMejorModelo.put(idMejorModelo, ManejadorModelos.elegidosMejorModelo.get(idMejorModelo) + 1);
						while(ManejadorModelos.numVotaciones() < ManejadorBlockchain.NUM_NODOS) {
							esperarMillis(100);
						}

						System.out.println( estacion.getId() + " Salien2 segundo while votaciones");
						int mejorID = ManejadorModelos.darMejorID();
						if(mejorID == estacion.getId()) {
							System.out.println(" MEJOR: " + mejorID);
							datasetIterator = ProofOfLearning.readCSVDataset("data/CSVs Temporales/bloque_actual_" + estacion.getId() + ".csv");
							guardarMejorModelo(mejorID, datasetIterator);
							
							bloqueActual.cerrarBloque();
							bloqueActual.establecerTimestamp(new Date());

							estacion.getScript().addVariable("bloqueNuevo", bloqueActual.toString());	
							//estacion.getScript().addVariable("timestampUltimo", bloqueActual.darTimestamp());
							//estacion.getScript().addVariable("hashUltimo", bloqueActual.darHash());

							eliminarTarea();
							crearNuevoBloque(bloqueActual.darHash());
							agregarBloqueAJSON(bloqueActual);							
						}
						else {
							bloques.remove(bloques.size() - 1);			
							validando = true;
						}
					}
				}
			}
			else {
				esperarMillis(100);
			}
		}
	}

	public int darIdEstacion () {
		return estacion.getId();
	}

	public ArrayList<Bloque> darBloques () {
		return bloques;
	}

	public boolean darValidando() {
		return validando;
	}

	public void establecerValidando (boolean pValidando) {
		validando = pValidando;
	}

	public void recibirConfirmacion () {
		bloques.get(bloques.size() - 1).incrementarConfirmaciones();
	}

	public synchronized void guardarRuta(int idEstacion, String rutaModelo) {
		rutasModelos.put(idEstacion, rutaModelo);
	}

	public void reemplazarBloque (String bloqueStr, String hashAnterior, String hashUltimo) {
		Bloque nuevoBloque = new Bloque (bloqueStr, hashAnterior);
		nuevoBloque.establecerHash(hashUltimo);
		bloques.add(nuevoBloque);
		crearNuevoBloque(hashUltimo);
	}

	public void crearNuevoBloque(String hashAnterior) {
		Bloque nuevoUltimoBloque = new Bloque (hashAnterior, estacion.getId());
		for (Transaccion t: transaccionesTemporales) {
			nuevoUltimoBloque.agregarTransaccion(t);
		}
		transaccionesTemporales = new ArrayList<Transaccion>();
		bloques.add(nuevoUltimoBloque);
	}

	public void establecerTimestamp(Date timestamp, String hashUltimo) {
		Bloque actual = null;
		for (int i = bloques.size() - 1; i >= 0; i-- ) {
			actual = bloques.get(i);
			if (actual.darEstado().equals(Bloque.Estado.CERRADO) && actual.darHash().equals(hashUltimo)) {
				actual.establecerTimestamp(timestamp);
				agregarBloqueAJSON(actual);
				break;
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
			Bloque ultimoBloque = null;
			if (bloques.size() > 0 )
			{
				ultimoBloque = bloques.get(bloques.size()-1);
				if (ultimoBloque.darEstado().equals(Estado.ABIERTO)) {
					ultimoBloque.agregarTransaccion(temporal);
				}
				else {
					transaccionesTemporales.add(temporal);
				}
			}
			else {
				transaccionesTemporales.add(temporal);
			}
			estacion.getScript().addVariable("reenviarTransaccion", "true");
		}
	}

	public synchronized static double revisarModelo(String rutaModelo, DataSetIterator datasetIterator) {
		double mse = Double.MAX_VALUE;
		try {
			File archivo = new File(rutaModelo);
			System.out.println(" - Ruta modelo en revisar: " + rutaModelo + " - Next? " + datasetIterator.hasNext());
			MultiLayerNetwork net = MultiLayerNetwork.load(archivo, false);
			RegressionEvaluation eval = net.evaluateRegression(datasetIterator);
			mse = eval.averageMeanSquaredError();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mse;
	}

	public void guardarMejorModelo(int idMejorModelo, DataSetIterator datasetIterator) {
		String ruta = "models/autoencoder_"+idMejorModelo + ".bin";
		try(FileWriter file = new FileWriter("models/best_model/stats.txt")) {
			MultiLayerNetwork net = MultiLayerNetwork.load(new File(ruta), false);
			try {
				net.save(new File("models/best_model/autoencoder_"+idMejorModelo + ".bin"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			RegressionEvaluation eval = net.evaluateRegression(datasetIterator);
			file.write(eval.stats()); 
			file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void agregarBloqueAJSON(Bloque bloque) {
		try (FileWriter file = new FileWriter("data/blockchain-" + estacion.getId() + ".json")) {
			JSONObject bloqueJson = bloque.darJSONObject();
			jsonArray.put(bloqueJson);
			file.write(jsonArray.toString()); 
			file.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
		try (FileWriter file = new FileWriter("data/CSVs Entrenamiento/entrenamiento_" + estacion.getId() + ".csv", true)) {
			ArrayList<Transaccion> transacciones = bloque.darTransacciones();
			Transaccion transaccionActual;
			for(int i=0; i<transacciones.size(); i++) {
				transaccionActual = transacciones.get(i);
				file.write(transaccionActual.darTemperatura() + ";" +
						transaccionActual.darPH() + ";" +
						transaccionActual.darTerneza() + ";" +
						transaccionActual.darMermaPorCoccion() + ";" +
						transaccionActual.darColorL() + ";" +
						transaccionActual.darColorA() + ";" +
						transaccionActual.darColorB() + ";" +
						transaccionActual.darTemperatura() + ";" +
						transaccionActual.darPH() + ";" +
						transaccionActual.darTerneza() + ";" +
						transaccionActual.darMermaPorCoccion() + ";" +
						transaccionActual.darColorL() + ";" +
						transaccionActual.darColorA() + ";" +
						transaccionActual.darColorB() + "\n");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public void crearCSVTemporal(Bloque bloqueActual) {
		try (FileWriter file = new FileWriter("data/CSVs Temporales/bloque_actual_" + estacion.getId() + ".csv", false)) {
			ArrayList<Transaccion> transacciones = bloqueActual.darTransacciones();
			Transaccion transaccionActual;
			file.write("temperatura;pH;terneza;mermaPorCoccion;colorL;colorA;colorB\n");
			for(int i=0; i<transacciones.size(); i++) {
				transaccionActual = transacciones.get(i);
				file.write(transaccionActual.darTemperatura() + ";" +
						transaccionActual.darPH() + ";" +
						transaccionActual.darTerneza() + ";" +
						transaccionActual.darMermaPorCoccion() + ";" +
						transaccionActual.darColorL() + ";" +
						transaccionActual.darColorA() + ";" +
						transaccionActual.darColorB() + ";" +
						transaccionActual.darTemperatura() + ";" +
						transaccionActual.darPH() + ";" +
						transaccionActual.darTerneza() + ";" +
						transaccionActual.darMermaPorCoccion() + ";" +
						transaccionActual.darColorL() + ";" +
						transaccionActual.darColorA() + ";" +
						transaccionActual.darColorB() + "\n");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void eliminarTarea() {
		JSONArray nuevaListaTareas = new JSONArray();
		try(InputStream is = FileUtils.openInputStream(new File("data/tareas.json")))
		{			
			JSONTokener tokener = new JSONTokener(is);
			JSONArray anteriorListaTareas = new JSONArray(tokener);
			for(int i=1; i<anteriorListaTareas.length(); i++) {
				nuevaListaTareas.put(anteriorListaTareas.getJSONObject(i));
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (FileWriter file = new FileWriter("data/tareas.json")) {
			file.write(nuevaListaTareas.toString()); 
			file.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void esperarMillis(int millis) {
		try {
			sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
