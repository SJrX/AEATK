package ca.ubc.cs.beta.aclib.algorithmrun.kill;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class StatusVariableKillHandler implements KillHandler {

	private volatile boolean isKilled = false;
	@Override
	public void kill() {
		isKilled = true;
		
	}

	@Override
	public boolean isKilled() {
		return isKilled;
		
	}

}
