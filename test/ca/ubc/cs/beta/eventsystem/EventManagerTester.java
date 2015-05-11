package ca.ubc.cs.beta.eventsystem;



import static org.junit.Assert.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.ubc.cs.beta.aeatk.eventsystem.EventHandler;
import ca.ubc.cs.beta.aeatk.eventsystem.EventManager;
import ca.ubc.cs.beta.aeatk.eventsystem.events.AutomaticConfiguratorEvent;
import ca.ubc.cs.beta.aeatk.eventsystem.events.basic.EventHandlerRuntimeExceptionEvent;
import ca.ubc.cs.beta.aeatk.eventsystem.exceptions.EventManagerShutdownException;

public class EventManagerTester {

	private EventManager eventManager;
	
	@Before
	public void setUp()
	{
		this.eventManager = new EventManager();
		this.eventManager.registerHandler(EventHandlerRuntimeExceptionEvent.class, new EventHandler<EventHandlerRuntimeExceptionEvent>()
		{

			@Override
			public void handleEvent(EventHandlerRuntimeExceptionEvent event) {
				event.getRuntimeException().printStackTrace();
				
			}
			
		});
		
	}	
	
	@After
	public void tearDown()
	{
		if(!this.eventManager.isShutdown())
		{
			this.eventManager.shutdown();
		}
	}
	
	
	@Test
	/**
	 * Tests that events are fired
	 * @throws InterruptedException
	 */
	public void eventFires() throws InterruptedException
	{
		//Tests that events that are registered are sent to the correct handler
		TestHandler handler = new TestHandler();
		TestHandler handler2 = new TestHandler();
		eventManager.registerHandler(TestEvent.class, handler);
		eventManager.registerHandler(TestEvent2.class, handler2);
		
		TestEvent event = new TestEvent();
		eventManager.fireEvent(event);
		
		//Sleep because we will test flush in another method
		Thread.sleep(150);

		assertEquals("Expected objects to be == the same", handler.getLastEvent() ,  event);
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  null);
		

		TestEvent2 event2 = new TestEvent2();
		eventManager.fireEvent(event2);
		//Sleep because we will test flush in another method
		Thread.sleep(150);

		assertEquals("Expected objects to be == the same", handler.getLastEvent() , event);
		System.out.println(event2);
		System.out.println(handler2.getLastEvent());
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  event2);
		
	}
	
	@Test
	/**
	 * Tests that all the required handlers are notified of an event
	 * @throws InterruptedException
	 */
	public void eventFiresMultipleHandlers() throws InterruptedException
	{
		//Tests that events that are registered are sent to the correct handler
		TestHandler handler = new TestHandler();
		TestHandler handler2 = new TestHandler();
		TestHandler handler3 = new TestHandler();
		eventManager.registerHandler(TestEvent.class, handler);
		eventManager.registerHandler(TestEvent.class, handler2);
		eventManager.registerHandler(TestEvent.class, handler3);
		
		TestEvent event = new TestEvent();
		eventManager.fireEvent(event);
		
		//Sleep because we will test flush in another method
		eventManager.flush();

		assertEquals("Expected objects to be == the same", handler.getLastEvent() ,  event);
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  event);
		assertEquals("Expected objects to be == the same", handler3.getLastEvent() ,  event);
		
	}
	
	
	
	@Test
	/**
	 * Tests that flush works by trying to power through as fast as possible and exploit a race condition
	 * @throws InterruptedException
	 */
	public void eventFiresFlushRaceConditionMode() throws InterruptedException
	{
		//Tests that events that are registered are sent to the correct handler
		TestHandler handler = new TestHandler();
		TestHandler handler2 = new TestHandler();
		eventManager.registerHandler(TestEvent.class, handler);
		eventManager.registerHandler(TestEvent2.class, handler2);
		
		
		TestEvent event = new TestEvent();
		
		eventManager.fireEvent(event);
		
		eventManager.flush();
		
			
		

		assertEquals("Expected objects to be == the same", handler.getLastEvent() ,  event);
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  null);
		

		TestEvent2 event2 = new TestEvent2();
		eventManager.fireEvent(event2);
		
		

		eventManager.flush();
		
		assertEquals("Expected objects to be == the same", handler.getLastEvent() , event);
		System.out.println(event2);
		System.out.println(handler2.getLastEvent());
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  event2);
		
	}
	
	
	
	@Test
	/***
	 * Tests that flush works even with long handlers by ensuring that the sleep handler which takes 500 ms seems to eat up that much
	 * runtime before flush returns
	 * @throws InterruptedException
	 */
	public void eventFiresFlushSleepMode() throws InterruptedException
	{
		//Tests that events that are registered are sent to the correct handler
		TestHandler handler = new TestHandler();
		TestHandler handler2 = new TestHandler();
		eventManager.registerHandler(TestEvent.class, handler);
		eventManager.registerHandler(TestEvent.class, new SleepHandler());
		eventManager.registerHandler(TestEvent2.class, handler2);
		eventManager.registerHandler(TestEvent2.class, new SleepHandler());
		
		TestEvent event = new TestEvent();
		long startTime = 0;
		long endTime = 0;
		
		startTime = System.currentTimeMillis();
		eventManager.fireEvent(event);
		
		eventManager.flush();
		
		endTime = System.currentTimeMillis(); 
		
		assertTrue("SleepTime should be greater than 500 ms", (endTime - startTime) > 490);
		

		assertEquals("Expected objects to be == the same", handler.getLastEvent() ,  event);
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  null);
		

		startTime = System.currentTimeMillis();
		TestEvent2 event2 = new TestEvent2();
		eventManager.fireEvent(event2);
		
		eventManager.flush();
		endTime = System.currentTimeMillis();
		
		System.out.println(endTime - startTime);
		System.out.println(endTime - startTime);
		System.out.println(endTime - startTime);
		System.out.println(endTime - startTime);
		System.out.println(endTime - startTime);
		
		assertTrue("SleepTime should be greater than 500 ms", (endTime - startTime) > 490);
		
		
		assertEquals("Expected objects to be == the same", handler.getLastEvent() , event);
		System.out.println(event2);
		System.out.println(handler2.getLastEvent());
		assertEquals("Expected objects to be == the same", handler2.getLastEvent() ,  event2);
		
	}
	
	
	@Test
	/**
	 * Tests that runtimeExceptions go to the registered handler
	 * @throws InterruptedException
	 */
	public void runtimeExceptionsCaught() throws InterruptedException
	{
		//Tests that events that are registered are sent to the correct handler
		
		BrokenHandler brokenHandler = new BrokenHandler();
		final Semaphore semi = new Semaphore(0);
		
		System.err.println("IGNORE ANY STACK TRACES ON CONSOLE UNTIL THIS TEST IS OVER, IF IT HANGS HERE THE TEST HAS FAILED");
		this.eventManager.registerHandler(EventHandlerRuntimeExceptionEvent.class, new EventHandler<EventHandlerRuntimeExceptionEvent>()
				{

					@Override
					public void handleEvent(EventHandlerRuntimeExceptionEvent event) {
						//event.getRuntimeException().printStackTrace();
						System.out.println("Got runtime exception event");
						semi.release();
					}
					
				});
				
		eventManager.registerHandler(TestEvent.class, brokenHandler);
		
		TestEvent event = new TestEvent();
		eventManager.fireEvent(event);
		
		eventManager.flush();
		semi.acquire();
		
		System.err.println("EVENT MANAGER STACK TRACES ARE NOW A PROBLEM AGAIN");
		
	}
	
	
	@Test
	public void testShutdown()
	{
		eventManager.shutdown();
		try {
		TestHandler handler = new TestHandler();
		eventManager.registerHandler(TestEvent.class, handler);
		} catch(EventManagerShutdownException e)
		{
			
		}

		try {
			//TestHandler handler = new TestHandler();
			eventManager.fireEvent(new AutomaticConfiguratorEvent()
			{
				
			});
		} catch(EventManagerShutdownException e)
		{
			
		}

		
	}
	class TestEvent extends AutomaticConfiguratorEvent
	{
		
	}
	
	class TestEvent2 extends AutomaticConfiguratorEvent
	{
		
	}
	
	static class TestEvent3 extends AutomaticConfiguratorEvent
	{
		
	}
	
	
	static class TestHandler implements EventHandler<AutomaticConfiguratorEvent>
	{

		AtomicReference<AutomaticConfiguratorEvent> ref = new AtomicReference<AutomaticConfiguratorEvent>();
		
		@Override
		public synchronized void handleEvent(AutomaticConfiguratorEvent event) {	
			System.out.println(this + " Got " + event);
			this.ref.set(event);
			
		}
		
		public synchronized AutomaticConfiguratorEvent getLastEvent()
		{
			return this.ref.get();
		}
		
	}
	
	class SleepHandler implements EventHandler<AutomaticConfiguratorEvent>
	{

		AtomicReference<AutomaticConfiguratorEvent> ref = new AtomicReference<AutomaticConfiguratorEvent>();
		
		@Override
		public synchronized void handleEvent(AutomaticConfiguratorEvent event) {	
			System.out.println(this + " Got " + event);
			this.ref.set(event);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			
		}
		
		public synchronized AutomaticConfiguratorEvent getLastEvent()
		{
			return this.ref.get();
		}
		
	}
	
	static class BrokenHandler implements EventHandler<AutomaticConfiguratorEvent>
	{

		AtomicReference<AutomaticConfiguratorEvent> ref = new AtomicReference<AutomaticConfiguratorEvent>();
		
		@Override
		public synchronized void handleEvent(AutomaticConfiguratorEvent event) {
			
			throw new IllegalStateException("I don't know what I want");
			
			
		}
		
		public synchronized AutomaticConfiguratorEvent getLastEvent()
		{
			return this.ref.get();
		}
		
	}
	
	
	
}
