/* *****************************************************************************
 *
 *
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package main;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Read a csv file. Fit and plot the data using Deeplearning4J.
 */
public class Main {

    public static boolean visualize = true;
    public static String dataLocalPath;


    public static void main(String[] args) throws Exception {
        String filename = new File(dataLocalPath, "data/testData.csv").getAbsolutePath();
        DataSet ds = readCSVDataset(filename);

        ArrayList<DataSet> DataSetList = new ArrayList<>();
        DataSetList.add(ds);

        plotDataset(DataSetList, 1); //Plot the data, make sure we have the right data.

        
        MultiLayerNetwork net = fitNN(ds);


        //Train the network on the full data set, and evaluate in periodically
//        DataSetIterator iterator = new ListDataSetIterator<>(DataSetList, 32);
//        int nEpochs = 50000;
//        for( int i=0; i<nEpochs; i++ ){
//            iterator.reset();
//            net.fit(iterator);
//        }
        // Get the min and max x values, using Nd4j
        NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();
        preProcessor.fit(ds);
        int nSamples = 50;
        INDArray x = Nd4j.linspace(preProcessor.getMin().getInt(0), preProcessor.getMax().getInt(0), nSamples).reshape(nSamples, 1);
        INDArray y = net.output(x);
        DataSet modeloutput = new DataSet(x, y);
        DataSetList.add(modeloutput);

        //plot on by default
        if (visualize) {
            plotDataset(DataSetList, 2);    //Plot data and model fit.
        }
    }

    /**
     * Fit a straight line using a neural network.
     *
     * @param ds The dataset to fit.
     * @return The network fitted to the data
     */
    private static MultiLayerNetwork fitStraightline(DataSet ds) {
        int seed = 12345;
        int nEpochs = 200;
        double learningRate = 0.00001;
        int numInputs = 1;
        int numOutputs = 1;

        //
        // Hook up one input to the one output.
        // The resulting model is a straight line.
        //
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .weightInit(WeightInit.XAVIER)
            .updater(new Nesterovs(learningRate, 0.9))
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numOutputs)
                .activation(Activation.IDENTITY)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .activation(Activation.IDENTITY)
                .nIn(numOutputs).nOut(numOutputs).build())
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        for (int i = 0; i < nEpochs; i++) {
            net.fit(ds);
        }

        return net;
    }
    
    private static MultiLayerNetwork fitNN(DataSet ds) {

        int seed = 12345;
        double learningRate = 0.00001;
        int numInput = 1;
        int numOutputs = 1;
        int nHidden1 = 20;
        int nHidden2 = 10;
        int nHidden3 = 5;
        MultiLayerConfiguration conf =new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden1)
                        .activation(Activation.RELU) //Change this to RELU and you will see the net learns very well very quickly
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(nHidden1).nOut(nHidden2)
                        .activation(Activation.RELU) //Change this to RELU and you will see the net learns very well very quickly
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(nHidden2).nOut(nHidden3)
                        .activation(Activation.RELU) //Change this to RELU and you will see the net learns very well very quickly
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden3).nOut(numOutputs).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));
        
        int nEpochs = 25000;
        for (int i = 0; i < nEpochs; i++) {
            net.fit(ds);
        }

        return net;
    }

    /**
     * Read a CSV file into a dataset.
     * <p>
     * Use the correct constructor:
     * DataSet ds = new RecordReaderDataSetIterator(rr,batchSize);
     * returns the data as follows:
     * ===========INPUT===================
     * [[12.89, 22.70],
     * [19.34, 20.47],
     * [16.94,  6.08],
     * [15.87,  8.42],
     * [10.71, 26.18]]
     * <p>
     * Which is not the way the framework likes its data.
     * <p>
     * This one:
     * RecordReaderDataSetIterator(rr,batchSize, 1, 1, true);
     * returns
     * ===========INPUT===================
     * [12.89, 19.34, 16.94, 15.87, 10.71]
     * =================OUTPUT==================
     * [22.70, 20.47,  6.08,  8.42, 26.18]
     * <p>
     * This can be used as is for regression.
     */
    private static DataSet readCSVDataset(String filename) throws IOException, InterruptedException {
        int batchSize = 4;
        RecordReader rr = new CSVRecordReader(1, ";");
        rr.initialize(new FileSplit(new File(filename)));

        DataSetIterator iter = new RecordReaderDataSetIterator(rr, batchSize, 1, 1, true);
        return iter.next();
    }

    /**
     * Generate an xy plot of the datasets provided.
     */
    private static void plotDataset(ArrayList<DataSet> DataSetList, int dscounter) {

        XYSeriesCollection c = new XYSeriesCollection();
        int contador = 0;

        for (DataSet ds : DataSetList) {
            INDArray features = ds.getFeatures();
            INDArray outputs = ds.getLabels();

            int nRows = features.rows();
            XYSeries series = new XYSeries("S" + dscounter + contador++);
            for (int i = 0; i < nRows; i++) {
                series.add(features.getDouble(i), outputs.getDouble(i));
            }

            c.addSeries(series);
        }

        String title = "title";
        String xAxisLabel = "xAxisLabel";
        String yAxisLabel = "yAxisLabel";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;
        //noinspection ConstantConditions
        JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, c, orientation, legend, tooltips, urls);
        JPanel panel = new ChartPanel(chart);

        JFrame f = new JFrame();
        f.add(panel);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setTitle("Training Data");

        f.setVisible(true);
    }
}