package cupcarbon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import device.IoTMqttModule;
import solver.SolverProxyParams;

public class CupCarbonMain {
	public static void main(String[] args) {
		System.out.println("Welcome to CupCarbon Version "+CupCarbonVersion.VERSION);
		System.out.println("Session Generation ...");
		CupCarbon.cupcarbonSession = "cupcarbon_"+CupCarbon.generateCode(30);
		if(args.length>0) {
			SolverProxyParams.proxyset = "true";
			SolverProxyParams.host = args[0];
			SolverProxyParams.port = args[1];
			CupCarbon.setProxy();
		}
		
		File file_code = new File("mqtt_code.par");
		if(file_code.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file_code)));
				IoTMqttModule.com_real_node_topic = br.readLine();
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				FileOutputStream fos = new FileOutputStream("mqtt_code.par");
				String topic_code = "cupcarbon_sim/"+CupCarbon.generateCode(30);
				IoTMqttModule.com_real_node_topic = topic_code;
				PrintStream ps = new PrintStream(fos);
				ps.println(topic_code);
				ps.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		//CupCarbonServer server = new CupCarbonServer();
		//server.start();		
		
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					MapLayer.repaint();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();*/
		
		CupCarbon.runCupCarbon(args);
	}
}
