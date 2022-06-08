package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;


public class LoadModel {

	public static void main(String[] args) throws Exception {
		MultiLayerNetwork net = MultiLayerNetwork.load(new File("models/CSV Temporales/autoencoder_3.bin"), false);
//		DataSet ds = Main.readCSVDataset("data/testData.csv");
//
//        ArrayList<DataSet> DataSetList = new ArrayList<>();
//        DataSetList.add(ds);
//        
//        
//        NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();
//        preProcessor.fit(ds);
//        int nSamples = 50;
//        INDArray x = Nd4j.linspace(preProcessor.getMin().getInt(0), preProcessor.getMax().getInt(0), nSamples).reshape(nSamples, 1);
//        INDArray y = net.output(x);
//        DataSet modeloutput = new DataSet(x, y);
//        DataSetList.add(modeloutput);
//        
//		Main.plotDataset(DataSetList, 2);
		
		DataSetIterator datasetIterator = readCSVDataset("data/bloque_actual_1.csv");
		RegressionEvaluation eval = net.evaluateRegression(datasetIterator);
		double mse = eval.averageMeanSquaredError();
		System.out.println("MSE: " + mse);
	}

	public static DataSetIterator readCSVDataset(String filename) {
		DataSetIterator iter = null;
		try {
	        int batchSize = 4;
	        RecordReader rr = new CSVRecordReader(1, ";");
	        rr.initialize(new FileSplit(new File(filename)));
	        iter = new RecordReaderDataSetIterator(rr, batchSize, 7, 13, true);
		} catch(Exception e) {
			e.printStackTrace();
		}
        return iter;
    }
}
