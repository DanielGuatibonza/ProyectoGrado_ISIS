package senscript;

import javax.crypto.SecretKey;

import arduino.Arduino;
import crypto.ECC;
import crypto.ECCKeys_Manager;
import device.MessageEventList;
import device.DeviceList;
import device.MultiChannels;
import device.SensorNode;
import radio_module.RadioPacketGenerator;
import simulation.SimulationInputs;
import simulation.WisenSimulation;
import utilities.UColor;

public class Command_SEND_CIPHER extends Command{

	protected boolean writtenInUART = false ;

	protected boolean channelFree = false ;

	protected String arg1 = "";
	protected String arg2 = "";

	protected String rRadioName = "";

	protected boolean checkForAckMessage = false;

	// ---------------------------------------------------------------------------------------------------------------------
	// Constructor 
	// ---------------------------------------------------------------------------------------------------------------------
	/**
	 * Comando para enviar un mensaje cifrado
	 * @param sensor
	 * @param arg1: Mensaje a enviar de forma cifrada
	 * @param arg2: Id destino
	 */
	public Command_SEND_CIPHER(SensorNode sensor, String arg1, String arg2) {
		this.sensor = sensor;
		this.arg1 = arg1;
		this.arg2 = arg2;
		writtenInUART = false ;
		channelFree = false ;
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// execute send
	// First we write in a UART (if writtenInUART=false)
	// Then the message will be sent by radio (if writtenInUART=true) 
	// Than we will wait for an ACK if this mode is activated (ie. SimulationInputs.ack=true)
	// No ACK for the broadcast sending : arg2.equals("*") && arg3.equals("")
	// ---------------------------------------------------------------------------------------------------------------------
	@Override
	public double execute() {
		boolean couleur = false;
		if(arg1.equals("!color")) couleur = true;

		if(sensor.getScript().getVariableValue(arg1)==null || (sensor.getScript().getVariableValue(arg2)==null && !arg2.equals("*"))) {
			return 0;
		}

		if (couleur) {
			Double v = Double.parseDouble(sensor.getScript().getVariableValue(arg2));
			int iv = v.intValue()%13;
			sensor.setRadioLinkColor(UColor.colorTab2[iv]);
			return 0;
		}

		String mensaje = sensor.getScript().getVariableValue(arg1);
		Integer idOrigen = sensor.getId();
		Integer idDestino = Integer.parseInt(sensor.getScript().getVariableValue(arg2));

		SecretKey llaveSimetricaServ = ECCKeys_Manager.darLlaveCompartida(idOrigen, idDestino);

		byte[] textoCifrado = ECC.cifrar(llaveSimetricaServ, mensaje);
		String textoCifradoStr = ECC.bytesToHex(textoCifrado) ;

		//System.out.println(message);
		int mensajeLong = textoCifradoStr.length();

		// In ACK mode (SimulationInputs.ack = true)
		// After sending a message (ackBlock = true), we verify if the ACK message is received (sensor.ackOk)
		// Otherwise, we re-send the message
		if(SimulationInputs.ack && checkForAckMessage) {
			//System.out.println("ACK BLOCK");
			if(sensor.isAckReceived()) {
				//System.out.println(WisenSimulation.time + " ACK RECEIVED");
				sensor.setAckReceived(false);
				sensor.setAckWaiting(false);
				sensor.setAttempts(0);
				writtenInUART = false;
				executing = false;
				checkForAckMessage = false;
				return 0 ;
			}
			else {
				//System.out.println("RESEND");
				sensor.incAttempts();
				writtenInUART = true;
			}
		}

		// First operation, we start by writing the message in the UART of the card
		if(!writtenInUART) {
			//System.out.println("UART");
			sensor.setMessageLost(false);
			sensor.setAckReceived(false);
			sensor.setAttempts(0);
			WisenSimulation.simLog.add("S" + sensor.getId() + " is writing the message : \"" + textoCifradoStr + "\" in its buffer.");
			writtenInUART = true ;
			executing = true;
			//double ratio = 1.0/(sensor.getUartDataRate());
			double ratio = 0.1E-20;
			return (ratio * (mensajeLong*8)) ;
		}


		// MAC Layer: listening to the channel and test if it is free
		if(writtenInUART && !channelFree) {
			double macProba = 1.0;
			if(SimulationInputs.macLayer) macProba = SimulationInputs.macProba;
			WisenSimulation.simLog.add("S" + sensor.getId() + " is listening to the channel");
			double isChannelFree = Math.random();
			if(isChannelFree <= macProba) {		// a chance of 90% to have a free channel
				WisenSimulation.simLog.add("S" + sensor.getId() + " the channel is free.");
				channelFree = true;	
			}
			else {
				double randomWaitTime = Math.random();  // 0 to 1000 milliseconds
				WisenSimulation.simLog.add("S" + sensor.getId() + " the channel is not free, wait for "+randomWaitTime+"ms.");
				//System.out.println("Channel not free: wait for "+randomWaitTime);
				return randomWaitTime;
			}
		}

		// ---------------------------------------------------------------------------------------------------------------------
		// Once the message is written in the UART, here we send it with ack mode
		// ---------------------------------------------------------------------------------------------------------------------
		if (channelFree && writtenInUART && SimulationInputs.ack && !sensor.isAckWaiting()) {
			//if (writtenInUART && SimulationInputs.ack) {
			if(sensor.getAttempts() < sensor.getNumberOfSends()) {
				WisenSimulation.simLog.add("S" + sensor.getId() + " starts sending the message : \"" + textoCifradoStr + "\".");
				//System.out.println(WisenSimulation.time+ " "+ sensor.getAttempts() + " SEND " + message);
				sensor.setAckWaiting(true);
				//System.out.println();				 
				sendOperation(textoCifradoStr);
				checkForAckMessage = true;

				writtenInUART = false; /////

				return sensor.getTimeToResend();
			}
			else {
				MessageEventList.numberOfLostMessages += sensor.getPl()/100.;
				MessageEventList.numberOfLostMessages_b += mensajeLong * (sensor.getPl()/100.);
				//System.out.println("MESSAGE LOST");
				sensor.setAckWaiting(false);
				checkForAckMessage = false;
				sensor.setAckReceived(false);
				writtenInUART = false;
				channelFree = false;
				executing = false;
				sensor.setAttempts(0);
				sensor.setMessageLost(true);
				return 0;
			}
		}

		return 0;
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// Generate Arduino Code for the SEND function
	// ---------------------------------------------------------------------------------------------------------------------
	//@Override
	//	public String getArduinoForm2() {
	//		String s1 = sensor.getScript().getVariableValue(arg1);
	//		String s2 = sensor.getScript().getVariableValue(arg2);
	//		String s3 = sensor.getScript().getVariableValue(arg3);
	//		System.out.println(arg1);
	//		System.out.println(arg2);
	//		System.out.println(arg3);
	//		System.out.println(s1);
	//		System.out.println(s2);
	//		System.out.println(s3);
	//		String s = XBeeToArduinoFrameGenerator.data16(s1, s2, s3);
	//		return s;
	//	}

	@Override
	public String getArduinoForm() {
		String s = "";

		if(arg1.charAt(0)=='$') {			
			s += "\tfor(int i=0; i<30; i++) {\n";				
			s += "\t\tsdata[i] = " + arg1.substring(1) + ".charAt(i);\n";
			s += "\t}\n";
		}
		else {
			for(int i=0; i<arg1.length();i++) {
				s += "\tsdata["+ i +"] = '" + arg1.charAt(i) + "';\n";
			}
			s +="\tfor(int i="+arg1.length()+"; i < 30; i++) {\n";
			s +="\t\tsdata[i] = ' ';\n\t}\n";
		}

		String [] info ;
		if (arg2.equals("") || arg2.equals("*")) {
			s += "\taddr = XBeeAddress64(0x0, 0xFFFF);\n";
		}
		else
			if (arg2.equals("0")) {
				info = getXBeeInfoById(arg2);
				s += "\taddr = XBeeAddress16(0x0, "+info[1]+");\n";
			} 
			else {
				info = getXBeeInfoById(arg2);
				s += "\taddr = XBeeAddress64("+info[2]+", "+info[3]+");\n";
			}
		s +=  "\ttx = Tx64Request(addr, sdata, sizeof(sdata));\n";
		s +=  "\txbee.send(tx);";
		return s;
	}

	public String [] getXBeeInfoById(String id) {
		String [] info = {"", "", "", ""};
		for(String [] s : Arduino.xbeeList) {
			if(s[0].equals(id)) {
				info[0] = s[1];
				info[1] = s[2];
				info[2] = s[3];
				info[3] = s[4];
			}
		}		
		//		for(int i=0; i<5; i++) {
		//			if(Arduino.xbeeList.get(i)[0].equals(id)) {
		//				info[0] = Arduino.xbeeList.get(i)[1];
		//				info[1] = Arduino.xbeeList.get(i)[2];
		//				info[2] = Arduino.xbeeList.get(i)[3];
		//				info[3] = Arduino.xbeeList.get(i)[4];
		//			}
		//		}
		return info;
	}

	// ---------------------------------------------------------------------------------------------------------------------
	// toString()
	// ---------------------------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		if(writtenInUART)
			return "SEND [BY RADIO]";	
		else 
			return "SEND [TO UART]"; 

	}

	// ---------------------------------------------------------------------------------------------------------------------
	// Operation of sending a message (prepare the packets according to the standard and the type of the command)
	// Calculate the energy consumption of the sending ooperation
	// ---------------------------------------------------------------------------------------------------------------------
	public void sendOperation(String message) {
		//String packet = RadioPacketGenerator.generate(sensor.getStandard());
		MessageEventList.numberOfSentMessages += 1;
		MessageEventList.numberOfSentMessages_b += message.length();

		sensor.consumeTx(RadioPacketGenerator.packetLengthInBits(message, 0, sensor.getStandard()));

		if (arg2.equals("*")) {
			WisenSimulation.simLog.add("S" + sensor.getId() + " has finished sending in a broadcast the message : \"" + message + "\" to the nodes: ");
			for (SensorNode rnode : sensor.getSensorNodeNeighbors()) {
				if (sensor.propagationDetect(rnode) && sensor.canCommunicateWith(rnode)) {
					MultiChannels.addPacketEvent(2, message, sensor, rnode);
				}
			}
		}
		else {
			String dest = arg2;
			dest = sensor.getScript().getVariableValue(dest);
			double destNodeId;	
			destNodeId = Double.valueOf(dest);
			SensorNode rnode = DeviceList.getSensorNodeById((int) destNodeId);
			if (rnode != null) {
				WisenSimulation.simLog.add("S" + sensor.getId() + " has finished sending the message : \"" + message + "\" to the node: ");
				if (sensor.propagationDetect(rnode) && sensor.canCommunicateWith(rnode)) {
					if(rRadioName.equals("") || rRadioName.equals(rnode.getCurrentRadioModule().getName())) 
						MultiChannels.addPacketEvent(0, message, sensor, rnode);
				}
				else
					WisenSimulation.simLog.add("S" + sensor.getId() + " and S" + destNodeId + "have not the same protocol!");
			}
			else 
				WisenSimulation.simLog.add("S" + sensor.getId() + " can not send the message : \"" + message + "\" to the non-existent node: "+destNodeId);
		}
	}

}