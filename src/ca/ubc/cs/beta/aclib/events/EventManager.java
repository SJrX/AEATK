package ca.ubc.cs.beta.aclib.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManager {

	private static UUID eventManagerUUID = UUID.randomUUID();
	
	private static final ConcurrentHashMap<Class<? extends AutomaticConfiguratorEvent>, List<EventHandler<?>>> handlerMap = new ConcurrentHashMap<Class<? extends AutomaticConfiguratorEvent>, List<EventHandler<?>>>();
	
	private static transient Logger log = LoggerFactory.getLogger(EventManager.class);
	
	private static ArrayBlockingQueue<Runnable> asyncRuns = new ArrayBlockingQueue<Runnable>(1024); 
	
	static{ 
		Thread t = new Thread() { 
			
			private boolean warningGeneratedForHighQueueLoad = false;
			@Override
			public void run()
			{
				try {
					asyncRuns.take().run();
					
					if(!warningGeneratedForHighQueueLoad && asyncRuns.size() > 100)
					{
						log.warn("Processing Events has over 100 elements currently waiting");
						warningGeneratedForHighQueueLoad = true;
					}
					
				} catch(RuntimeException e)
				{
					log.error("Unexpected Exception occured", e);
				} catch (InterruptedException e) {
					this.currentThread().interrupt();
				}
			}
			
			
			
			 
		};
		
		
		t.setDaemon(true);
		t.start();
		
		
		
	}
	
	public EventManager()
	{
		
	}
	
	public synchronized void registerHandler(Class< ? extends AutomaticConfiguratorEvent> event, EventHandler<?> handler )
	{
		
		
		List<EventHandler<?>> handlers =  handlerMap.putIfAbsent(event, new ArrayList<EventHandler<?>>());
		
		handlers.add(handler);
	}
	
	public synchronized UUID getUUID()
	{
		return eventManagerUUID;
	}
	
	
	
	public synchronized void fireEvent(AutomaticConfiguratorEvent event)
	{
		
		final List<EventHandler<?>> handlers =  handlerMap.putIfAbsent(event.getClass(), new ArrayList<EventHandler<?>>());
		
		
		for(EventHandler<?> handler : handlers)
		{
			

			final AutomaticConfiguratorEvent event2 = event;
			@SuppressWarnings("rawtypes")
			final EventHandler handler2 = handler;
			
			Runnable run = new Runnable()	{
				
				
				@SuppressWarnings("unchecked")
				public void run()
				{
					try { 
						handler2.handleEvent(event2);
					} catch(Throwable t)
					{
						log.error("Event Handler Threw Exception {}",t);
					}
				}
			};
			
			try {
				EventManager.asyncRuns.put(run);
			} catch (InterruptedException e) {

				Thread.currentThread().interrupt();
			}
			
		}
		
		
	}
}
