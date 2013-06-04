package ca.ubc.cs.beta.aclib.eventsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.eventsystem.events.AutomaticConfiguratorEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.EventHandlerRuntimeExceptionEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.EventManagerShutdownEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.internal.FlushEvent;
import ca.ubc.cs.beta.aclib.eventsystem.exceptions.EventFlushDeadLockException;
import ca.ubc.cs.beta.aclib.eventsystem.exceptions.EventManagerShutdownException;

/**
 * Event Manager class
 * 
 * Can be used as a Mediator to insulate different aspects of your program from each other.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class EventManager {

	
	
	private final ConcurrentHashMap<Class<? extends AutomaticConfiguratorEvent>, List<EventHandler<?>>> handlerMap = new ConcurrentHashMap<Class<? extends AutomaticConfiguratorEvent>, List<EventHandler<?>>>();
	
	private transient Logger log = LoggerFactory.getLogger(EventManager.class);
	
	private ArrayBlockingQueue<Runnable> asyncRuns = new ArrayBlockingQueue<Runnable>(1024); 
	
	private final EventManagementThread eventDispatchThread;
	
	private final boolean shutdown = false;
	
	public EventManager()
	{
	
		
		this.registerHandler(FlushEvent.class, new EventHandler<FlushEvent>() {

			@Override
			public void handleEvent(FlushEvent event) {
				event.releaseSemaphore();
			}
			
		});
		
		EventManagementThread t = new EventManagementThread(asyncRuns);
		eventDispatchThread = t;
		t.start();		
		
	}
	
	
	/**
	 * Registers a handler 
	 * @param event - The class of event to register
	 * @param handler - handler to invoke
	 */
	public synchronized void registerHandler(Class< ? extends AutomaticConfiguratorEvent> eventClass, EventHandler<?> handler)
	{
		checkForShutdown();
		
		handlerMap.putIfAbsent(eventClass, new ArrayList<EventHandler<?>>());
		List<EventHandler<?>> handlers = handlerMap.get(eventClass);
		handlers.add(handler);
	}
	
	AtomicReference<EventFlushDeadLockException> deadLockException = new AtomicReference<EventFlushDeadLockException>();
	
	/**
	 * Fires an event
	 * @param event
	 * @throws EventManagerShutdownException - if the event manager was previous shutdown 
	 */
	public synchronized void fireEvent(AutomaticConfiguratorEvent event)
	{
	
		checkForShutdown();
		
		
		EventFlushDeadLockException exp = deadLockException.get();
		if(exp != null)
		{
			throw new IllegalStateException("Deadlock has previously occurred, event manager is unavailable", deadLockException.get());
		}
		
		
		log.trace("Event requested for dispatch {}",event.getClass().getSimpleName());
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
					} catch(RuntimeException t)
					{
						
						Object[] args = { handler2, event2, t};
						log.warn("Event Handler {} while processing event: {}, threw Exception {}",args);
						
						if(!(event2 instanceof EventHandlerRuntimeExceptionEvent))
						{
							EventManager.this.fireEvent(new EventHandlerRuntimeExceptionEvent(t, event2));
							
							return;
						} 
						
						log.error("Event Handler threw exception while we were processing the {} event, not notifying anything else", event2.getClass());
					}
				}
			};
			
			try {
				this.asyncRuns.put(run);
			} catch (InterruptedException e) {

				Thread.currentThread().interrupt();
			}
			
		}
		
		
	}

	/**
	 * Ensures that all previous events have completed
	 */
	public void flush() {
		
		  
		checkForDeadLock();
		
		Semaphore wait = new Semaphore(0);
		
		this.fireEvent(new FlushEvent( wait));
		try {
			wait.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public synchronized boolean isShutdown()
	{
		return shutdown;
	}
	
	/**
	 * This method checks to see if an event is calling flush() as a side effect
	 */
	private void checkForDeadLock()
	{

		if(Thread.currentThread().equals(eventDispatchThread))
		{
			EventFlushDeadLockException e = new EventFlushDeadLockException();
			this.deadLockException.set(e);
			
			log.error("Deadlock detected ", e);
			System.out.flush();
			System.err.flush();
			
			e.printStackTrace();
			System.out.flush();
			System.err.flush();
			
			throw e;
		}
	}
	
	private synchronized void checkForShutdown()
	{
		if(shutdown)
		{
			throw new EventManagerShutdownException();
		}
	}
	
	
	static class EventManagementThread extends Thread
	{ 
		private transient Logger log = LoggerFactory.getLogger(EventManager.class);
		private BlockingQueue<Runnable> asyncRuns;
		
		private Semaphore threadDone = new Semaphore(0);
		
		private int highLoadQueueWarningDisplay = 128;
		public EventManagementThread(BlockingQueue<Runnable> asyncRuns)
		{
			this.asyncRuns = asyncRuns;
			setName("Event Manager Dispatch Thread");
			setDaemon(true);
		}
	
		@Override
		public void run()
		{
			try {
			while(true)
			{
				try {
					
					if(Thread.interrupted())
					{
						Thread.currentThread().interrupt();
						return;
					}
					asyncRuns.take().run();
					
					if(asyncRuns.size() > highLoadQueueWarningDisplay)
					{
						highLoadQueueWarningDisplay *= 2;
						log.warn("Processing Events has {} elements currently waiting, next warning at {} ", asyncRuns.size(), highLoadQueueWarningDisplay);
					}
					
				} catch(RuntimeException e)
				{
					log.error("Unexpected Exception occured", e);
					
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			} finally
			{
				threadDone.release();
			}
		}
		
		public void waitForCompletion()
		{
			try {
				try {
					threadDone.acquire();
				} catch(InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return;
				}
				
			} finally
			{
				threadDone.release();
			}
		}

	};
	
	public synchronized void shutdown()
	{
		this.fireEvent(new EventManagerShutdownEvent());
		this.flush();
		//Clean up eventDispatcherThread
		eventDispatchThread.interrupt();
		eventDispatchThread.waitForCompletion();	
		
	}
}
