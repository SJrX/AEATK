package ca.ubc.cs.beta.aclib.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

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
				while(true)
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
						Thread.currentThread().interrupt();
					}
				}
			}
			
			
			
			 
		};
		
		t.setName("Event Manager Dispatch Thread");
		t.setDaemon(true);
		t.start();
		
		
		
	}
	
	static {
		
		staticRegisterHandler(FlushEvent.class, new EventHandler<FlushEvent>() {

			@Override
			public void handleEvent(FlushEvent event) {
				event.releaseSemaphore();
			}
			
		});
	}
	public EventManager()
	{
		
	}
	
	private static void staticRegisterHandler(Class< ? extends AutomaticConfiguratorEvent> event, EventHandler<?> handler)
	{
		handlerMap.putIfAbsent(event, new ArrayList<EventHandler<?>>());
		List<EventHandler<?>> handlers = handlerMap.get(event);
		handlers.add(handler);
	}
	
	public synchronized void registerHandler(Class< ? extends AutomaticConfiguratorEvent> event, EventHandler<?> handler)
	{
		staticRegisterHandler(event, handler);
	}
	
	public synchronized UUID getUUID()
	{
		return eventManagerUUID;
	}
	
	
	
	public synchronized void fireEvent(AutomaticConfiguratorEvent event)
	{
		
		log.debug("Event requested for dispatch {}",event.getClass().getSimpleName());
		handlerMap.putIfAbsent(event.getClass(), new ArrayList<EventHandler<?>>());
		final List<EventHandler<?>> handlers = handlerMap.get(event.getClass());
		
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
						log.debug("Dispatching event {} ", event2.getClass().getSimpleName());
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

	public void flush() {
		
		Semaphore wait = new Semaphore(0);
		
		this.fireEvent(new FlushEvent(this.getUUID(), wait));
		try {
			wait.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	class FlushEvent extends AutomaticConfiguratorEvent
	{

		private final Semaphore semaphore;

		public FlushEvent(UUID uuid, Semaphore release) {
			super(uuid);
			this.semaphore = release;
		}
		
		public void releaseSemaphore()
		{
			this.semaphore.release();
		}
		
	}
}
