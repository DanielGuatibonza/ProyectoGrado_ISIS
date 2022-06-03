package blockchain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;

public class ProofOfLearning implements ProofOfX {
	
	public static final int MAX_SECONDS = 300;
	
	private JSONObject poleConf;
	private MultiLayerNetwork net;
	private DataSet ds;
	
	public ProofOfLearning(Bloque pBloque, JSONObject pJsonObject) {
		poleConf = pJsonObject;
		System.out.println(poleConf);
	}
	
	public ProofOfLearning(String JSONObjectStr) {
		
		String filename = new File("data/entrenamiento.csv").getAbsolutePath();
        ArrayList<DataSet> DataSetList = new ArrayList<>();
        try {
        	DataSet ds = readCSVDataset(filename); 
            DataSetList.add(ds);
        } catch(Exception e) {
        	e.printStackTrace();
        }
        ds = DataSetList.get(0);
        
		poleConf = (new JSONArray(JSONObjectStr)).getJSONObject(0); 
		
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
		int numEntradas = 7;
		for(int i=1; i<=numCapas; i++) {
			JSONArray numNeuronasRange = poleConf.getJSONArray("num_neuronas_"+i);
			int minNumNeuronas = numNeuronasRange.getInt(0);
			int maxNumNeuronas = numNeuronasRange.getInt(1);
			int numSalidas = (new Random().nextInt(maxNumNeuronas-minNumNeuronas)) + minNumNeuronas;
			JSONArray activaciones = poleConf.getJSONArray("activacion_"+i);
			int indexActivacion = new Random().nextInt(activaciones.length());
			String activacionStr = activaciones.getString(indexActivacion);
			Activation activation = activacionStr.equals("relu") ? Activation.RELU : Activation.SIGMOID;
			listBuilder.layer(new DenseLayer.Builder().nIn(numEntradas).nOut(numSalidas)
		                .activation(activation)
		                .build());
		}
		net = new MultiLayerNetwork(listBuilder.build());
		net.init();
        net.setListeners(new ScoreIterationListener(1));
	}

	@Override
	public MultiLayerNetwork ejecutar() {
		long millisInicio = System.currentTimeMillis();
		net.fit(ds);
		long tiempoEpocaSegundos = (System.currentTimeMillis() - millisInicio) / 1000;
		int epocas = (int)(MAX_SECONDS / tiempoEpocaSegundos) - 1;
		for(int i=0; i<epocas; i++) {
			net.fit(ds);
		}
		return net;
	}
	
	private static DataSet readCSVDataset(String filename) throws IOException, InterruptedException {
        int batchSize = 4;
        RecordReader rr = new CSVRecordReader(1, ";");
        rr.initialize(new FileSplit(new File(filename)));
        DataSetIterator iter = new RecordReaderDataSetIterator(rr, batchSize, 7, 13, true);
        return iter.next();
    }
	
	@Override
	public String toString () {
		return "Proof of Learning, parametros " + poleConf;
	}
}
