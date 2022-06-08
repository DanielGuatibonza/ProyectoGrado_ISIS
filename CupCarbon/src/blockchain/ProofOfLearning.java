package blockchain;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import org.nd4j.common.config.ND4JSystemProperties;

public class ProofOfLearning implements ProofOfX {
	
	public static final int MAX_MILLISECONDS = 300000;
	
	private JSONObject poleConf;
	private MultiLayerNetwork net;
	private DataSet ds;
	private Bloque bloque;
	
	public ProofOfLearning(Bloque pBloque, JSONObject pJsonObject) {
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.toLevel("error"));
		bloque = pBloque;
		poleConf = pJsonObject;
		configurarRedNeuronal();
	}
	
	public ProofOfLearning(String JSONObjectStr) {
		poleConf = (new JSONArray(JSONObjectStr)).getJSONObject(0); 
		configurarRedNeuronal();
	}
	
	public void configurarRedNeuronal() {
		
		String filename = new File("data/CSVs Entrenamiento/entrenamiento_" + bloque.darIDEstacion() + ".csv").getAbsolutePath();
        ArrayList<DataSet> DataSetList = new ArrayList<>();
        try {
        	DataSet ds = readCSVDataset(filename).next(); 
            DataSetList.add(ds);
        } catch(Exception e) {
        	e.printStackTrace();
        }
        ds = DataSetList.get(0);
        
		JSONArray learningRateRange = poleConf.getJSONArray("learning_rate");
		double minLearningRate = learningRateRange.getDouble(0);
		double maxLearningRate = learningRateRange.getDouble(1);
		double learningRate = ((maxLearningRate - minLearningRate)*(new Random().nextDouble())) + minLearningRate;
		
		ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
	            .seed(12345)
	            .weightInit(WeightInit.XAVIER)
	            .updater(new Nesterovs(learningRate, 0.9))
	            .list();
		
		JSONArray numCapasRange = poleConf.getJSONArray("num_capas");
		int minNumCapas = numCapasRange.getInt(0);
		int maxNumCapas = numCapasRange.getInt(1);
		int numCapas =  (new Random().nextInt(maxNumCapas-minNumCapas)) + minNumCapas;
		ArrayList<Integer> salidasPorCapa = new ArrayList<Integer>();
		ArrayList<Activation> activacionesPorCapa = new ArrayList<Activation>();
		int numEntradas = 7;
		for(int i=1; i<=numCapas; i++) {
			JSONArray numNeuronasRange = poleConf.getJSONArray("num_neuronas_"+i);
			int minNumNeuronas = numNeuronasRange.getInt(0);
			int maxNumNeuronas = numNeuronasRange.getInt(1);
			int numSalidas = (new Random().nextInt(maxNumNeuronas-minNumNeuronas)) + minNumNeuronas;
			salidasPorCapa.add(numSalidas);
			JSONArray activaciones = poleConf.getJSONArray("activacion_"+i);
			int indexActivacion = new Random().nextInt(activaciones.length());
			String activacionStr = activaciones.getString(indexActivacion);
			Activation activation = activacionStr.equals("relu") ? Activation.RELU : Activation.SIGMOID;
			activacionesPorCapa.add(activation);
			listBuilder.layer(new DenseLayer.Builder().nIn(numEntradas).nOut(numSalidas)
		                .activation(activation)
		                .build());
			numEntradas = numSalidas;
		}
		for(int i=salidasPorCapa.size()-2; i>=0; i--) {
			int numSalidas = salidasPorCapa.get(i);
			Activation activation = activacionesPorCapa.get(i);
			listBuilder.layer(new DenseLayer.Builder().nIn(numEntradas).nOut(numSalidas)
	                .activation(activation)
	                .build());
			numEntradas = numSalidas;
		}
		listBuilder.layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .activation(Activation.IDENTITY)
                .nIn(numEntradas).nOut(7).build());
		net = new MultiLayerNetwork(listBuilder.build());
		net.init();
        net.setListeners(new ScoreIterationListener(1));
	}

	@Override
	public MultiLayerNetwork ejecutar() {
		long millisInicio = System.currentTimeMillis();
		net.fit(ds);
		long tiempoEpocaMilisegundos = (System.currentTimeMillis() - millisInicio);
		int epocas = 10000;//(int)(MAX_MILLISECONDS / tiempoEpocaMilisegundos) - 1;
		for(int i=0; i<epocas; i++) {
			net.fit(ds);
			Blockchain.esperarMillis(5);
		}
		return net;
	}
	
	public static DataSetIterator readCSVDataset(String filename) {
		DataSetIterator iter = null;
		try {
	        int batchSize = 4;
	        RecordReader rr = new CSVRecordReader(1, ";");
	        File archivo = new File(filename);
	        if (!archivo.exists()) {
	        	System.out.println("No existe " + filename);
	        }
	        rr.initialize(new FileSplit(archivo));
	        iter = new RecordReaderDataSetIterator(rr, batchSize, 7, 13, true);
		} catch(Exception e) {
			e.printStackTrace();
		}
        return iter;
    }
	
	@Override
	public String toString () {
		return "Proof of Learning, parametros " + poleConf;
	}
}
