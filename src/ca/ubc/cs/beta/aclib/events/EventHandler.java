package ca.ubc.cs.beta.aclib.events;

public interface EventHandler<T extends AutomaticConfiguratorEvent> {

	public void handleEvent(T event);

}
