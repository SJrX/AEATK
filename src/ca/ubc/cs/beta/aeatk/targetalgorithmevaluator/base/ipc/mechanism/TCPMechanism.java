package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.mechanism;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aeatk.misc.watch.AutoStartStopWatch;
import ca.ubc.cs.beta.aeatk.misc.watch.StopWatch;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.ResponseParser;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

public class TCPMechanism {


	private final Logger log = LoggerFactory.getLogger(getClass());
	public TCPMechanism() {
	
		
	}

	/**
	 * 
	 * @param rc
	 * @param execConfig
	 * @param port
	 * @param remoteAddr
	 * @param udpPacketSize
	 * @return
	 */
	public AlgorithmRun evaluateRun(RunConfig rc, String remoteHost, int remotePort) 
	{
		
		
		try {
			Socket clientSocket = new Socket(remoteHost, remotePort);
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			
			//outStream.println("Request:" + Arrays.deepToString(args) + " to: " + port);
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(rc.getProblemInstanceSeedPair().getInstance().getInstanceName());
			list.add(rc.getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation());
			list.add(String.valueOf(rc.getCutoffTime()));
			list.add(String.valueOf(Integer.MAX_VALUE));
			list.add(String.valueOf(rc.getProblemInstanceSeedPair().getSeed()));
			
			StringFormat f = StringFormat.NODB_SYNTAX;
			
			for(String key : rc.getParamConfiguration().getActiveParameters()  )
			{
				
				
				if(!f.getKeyValueSeperator().equals(" ") || !f.getGlue().equals(" "))
				{
					throw new IllegalStateException("Key Value seperator or glue is not a space, and this means the way we handle this logic won't work currently");
				}
				list.add(f.getPreKey() + key);
				list.add(f.getValueDelimeter() + rc.getParamConfiguration().get(key)  + f.getValueDelimeter());	
				
			}
			
			StringBuilder sb = new StringBuilder();
			for(String s : list)
			{
				if(s.matches(".*\\s+.*"))
				{
					sb.append("\""+s + "\"");
				} else
				{
					sb.append(s);
				}
				sb.append(" ");
			}
	
			byte[] sendData;
			try {
				sendData = sb.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
			
			sb.append("\n");
			
			PrintWriter bwrite = new PrintWriter(clientSocket.getOutputStream());
			
			StopWatch watch = new AutoStartStopWatch();
			
			bwrite.append(sb);
			
			bwrite.flush();
		
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String serverLine;
			
			try {
			while( (serverLine = in.readLine() ) != null)
			{
				return ResponseParser.processLine(serverLine, rc,  watch.time() / 1000.0);
			}
			} finally
			{
				clientSocket.close();
			}
			return new ExistingAlgorithmRun( rc, RunResult.CRASHED, 0, 0, 0, 0, "No response from server: " + remoteHost + ":" + remotePort);
		} catch (IOException e) {
			log.error("Error creating socket, trying connection again in 10 seconds",e);
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
				throw new TargetAlgorithmAbortException(e1);
			}
			
			return evaluateRun(rc,  remoteHost, remotePort);
		}
		
		/*
		try {
			
			
			
			
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			byte[] receiveData = new byte[udpPacketSize];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			StopWatch watch = new AutoStartStopWatch();
			clientSocket.send(sendPacket);
			
			clientSocket.receive(receivePacket);
			watch.stop();
			receiveData = receivePacket.getData();
			
			String response = new String(receiveData,"UTF-8");
		
			clientSocket.close();

			return ResponseParser.processLine(response, rc, execConfig, watch.time() / 1000.0);
			
		} catch (SocketException e1) {
			throw new TargetAlgorithmAbortException("TAE Aborted due to socket exception",e1);
		} catch(IOException e1)
		{
			throw new TargetAlgorithmAbortException("TAE Aborted due to IOException",e1);
		}
		*/
		
		
	}

}
