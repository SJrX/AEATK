package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc;


import ca.ubc.cs.beta.aeatk.misc.jcommander.validator.ValidPortValidator;
import ca.ubc.cs.beta.aeatk.misc.jcommander.validator.ValidServerPortValidator;
import ca.ubc.cs.beta.aeatk.misc.options.OptionLevel;
import ca.ubc.cs.beta.aeatk.misc.options.UsageTextField;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.encoding.CallStringEncodingMechanism;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.encoding.EncodingMechanism;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.encoding.JavaSerializationEncodingMechanism;

import com.beust.jcommander.Parameter;

@UsageTextField(title="Inter-Process Communication Target Algorithm Evaluator Options", description="This Target Algorithm Evaluator hands the requests off to another process. The current encoding mechanism is the same as on the command line, except that we do not specify the algo executable field. The current mechanism can only execute one request to the server at a time. A small code change would be required to handle the more general case, so please contact the developers if this is required. ", level=OptionLevel.ADVANCED)
public class IPCTargetAlgorithmEvaluatorOptions extends AbstractOptions {

	
	
	@Parameter(names="--ipc-report-persistent", description="Whether the TAE should be treated as persistent, loosely a TAE is persistent if we could ask it for the same request later and it wouldn't have to redo the work from scratch.")
	public boolean persistent;
	
	@Parameter(names="--ipc-mechanism", description="Mechanism to use for IPC")
	public IPCMechanism ipcMechanism = IPCMechanism.UDP;
	
	@Parameter(names="--ipc-encoding", description="How the message is encoded")
	public EncodingMechanismOptions encodingMechanism = EncodingMechanismOptions.CALL_STRING;
	
	@Parameter(names="--ipc-remote-host", description="Remote Host for some kinds of IPC mechanisms")
	public String remoteHost = "127.0.0.1";
	
	@Parameter(names="--ipc-remote-port", description="Remote Port for some kinds of IPC mechanisms", validateWith=ValidPortValidator.class)
	public int remotePort = 5050;
	
	@Parameter(names="--ipc-udp-packetsize", description="Remote Port for some kinds of IPC mechanisms", validateWith=ValidPortValidator.class)
	public int udpPacketSize = 4096;

	@Parameter(names="--ipc-local-port", description="Local server port for some kinds of IPC mechanisms (if 0, this will be automatically allocated by the operating system)", validateWith=ValidServerPortValidator.class)
	public int localPort = 0;
	
	enum IPCMechanism 
	{
		UDP,
		TCP,
		REVERSE_TCP
	}
	
	
	enum EncodingMechanismOptions
	{
		CALL_STRING(CallStringEncodingMechanism.class),
		JAVA_SERIALIZATION(JavaSerializationEncodingMechanism.class);
		
		private Class<?> cls;
		EncodingMechanismOptions(Class<?> cls)
		{
			this.cls = cls;
		}
		
		public EncodingMechanism getEncoder()
		{
			
			try {
				return (EncodingMechanism) cls.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException("Couldn't create new instance of serializer (" + this.name() + ")",e);
			}
		}
	}
	
	private static final long serialVersionUID = -7900348544680161087L;

}
