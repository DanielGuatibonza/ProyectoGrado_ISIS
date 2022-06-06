package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;

public class LoadModel {

	public static void main(String[] args) throws Exception {
		MultiLayerNetwork net = MultiLayerNetwork.load(new File("models/prueba.bin"), false);
		DataSet ds = Main.readCSVDataset("data/testData.csv");

        ArrayList<DataSet> DataSetList = new ArrayList<>();
        DataSetList.add(ds);
        
        NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();
        preProcessor.fit(ds);
        int nSamples = 50;
        INDArray x = Nd4j.linspace(preProcessor.getMin().getInt(0), preProcessor.getMax().getInt(0), nSamples).reshape(nSamples, 1);
        INDArray y = net.output(x);
        DataSet modeloutput = new DataSet(x, y);
        DataSetList.add(modeloutput);
        
		Main.plotDataset(DataSetList, 2);
	}

}
