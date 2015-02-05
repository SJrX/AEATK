package ca.ubc.cs.beta.configspace;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ForbiddenOperators;

public class ForbiddenOperatorsTest {

	@Test
	public void testAndOrPrecendence()
	{	
		
		{
			ExpressionBuilder eb = new ExpressionBuilder("a || b && c || d");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 16; i++)
			{
			
				boolean a = ((i & 0x1) > 0);
				boolean b = ((i & 0x2) > 0);
				boolean c = ((i & 0x4) > 0);
				boolean d = ((i & 0x8) > 0);
			
				
				
				boolean expected = a || b && c || d;
				e.setVariable("a", a ? 1 : 0);
				e.setVariable("b", b ? 1 : 0);
				e.setVariable("c", c ? 1 : 0);
				e.setVariable("d", d ? 1 : 0);
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder("(a || b) && (c || d)");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 16; i++)
			{
			
				boolean a = ((i & 0x1) > 0);
				boolean b = ((i & 0x2) > 0);
				boolean c = ((i & 0x4) > 0);
				boolean d = ((i & 0x8) > 0);
			
				
				
				boolean expected = (a || b) && (c || d);
				e.setVariable("a", a ? 1 : 0);
				e.setVariable("b", b ? 1 : 0);
				e.setVariable("c", c ? 1 : 0);
				e.setVariable("d", d ? 1 : 0);
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder("a || b && (c || d)");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 16; i++)
			{
			
				boolean a = ((i & 0x1) > 0);
				boolean b = ((i & 0x2) > 0);
				boolean c = ((i & 0x4) > 0);
				boolean d = ((i & 0x8) > 0);
			
				
				
				boolean expected = a || b && (c || d);
				e.setVariable("a", a ? 1 : 0);
				e.setVariable("b", b ? 1 : 0);
				e.setVariable("c", c ? 1 : 0);
				e.setVariable("d", d ? 1 : 0);
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		{
			ExpressionBuilder eb = new ExpressionBuilder("(a || b) && c || d");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 16; i++)
			{
			
				boolean a = ((i & 0x1) > 0);
				boolean b = ((i & 0x2) > 0);
				boolean c = ((i & 0x4) > 0);
				boolean d = ((i & 0x8) > 0);
			
				
				
				boolean expected = (a || b) && c || d;
				e.setVariable("a", a ? 1 : 0);
				e.setVariable("b", b ? 1 : 0);
				e.setVariable("c", c ? 1 : 0);
				e.setVariable("d", d ? 1 : 0);
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
		
	}
	
	@Test
	public void testMishMashOfOperators()
	{
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" a > 2 != (b + 1) <= 3 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b");
			Expression e = eb.build();
			for(int i=0; i < 16; i++)
			{
			
				int a = ((i & 0b11));
				int b = ((i & 0b1100) >> 2);
			
			
				
				// true != true (false) && true || false
				//
				boolean expected =  a > 2 != (b + 1) <= 3 ;
				
				e.setVariable("a", a );
				e.setVariable("b", b );
				
				
	
				 System.out.println("(" + a +","+ b +")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" c <= 3 || d / 2 > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("c","d");
			Expression e = eb.build();
			for(int i=0; i < 16; i++)
			{
			
			
				int c = ((i & 0b11));
				int d = ((i & 0b1100)>> 2);
			
				
				// true != true (false) && true || false
				//
				boolean expected =  c <= 3 || d / 2 > 1 ;
			
				e.setVariable("c", c );
				e.setVariable("d", d );
	
				 System.out.println("(" + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" 0 || d > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("c","d");
			Expression e = eb.build();
			for(int i=0; i < 4; i++)
			{
			
			
				
				int d = ((i & 0b11));
			
				
				// true != true (false) && true || false
				//
				
			
				boolean expected = false || d  > 1 ;
			
			
				e.setVariable("d", d );
	
				 System.out.println("(" + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" 0 || d / 2 > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("c","d");
			Expression e = eb.build();
			for(int i=0; i < 4; i++)
			{
			
			
				
				double d = ((i & 0b11));
			
				
				// true != true (false) && true || false
				//
				
			
				boolean expected = false || d / 2 > 1 ;
			
			
				e.setVariable("d", d );
	
				 System.out.println("(" + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" 0 &&  1 || d / 2 > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("c","d");
			Expression e = eb.build();
			for(int i=0; i < 4; i++)
			{
			
			
				
				double d = ((i & 0b11));
			
				
				// true != true (false) && true || false
				//
				
			
				boolean expected = false && true || d / 2 > 1 ;
			
			
				e.setVariable("d", d );
	
				 System.out.println("(" + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
		
		
		
		
		
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" 0 &&  c <= 3 || d / 2 > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("c","d");
			Expression e = eb.build();
			for(int i=0; i < 256; i++)
			{
			
			
				int c = ((i & 0b11));
				double d = ((i & 0b1100)>> 2);
			
				
				// true != true (false) && true || false
				//
				
			
				boolean expected = false && c <= 3 || d / 2 > 1 ;
			
				e.setVariable("c", c );
				e.setVariable("d", d );
	
				 System.out.println("(" + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
		
		
		
		
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" a > 2 != (b + 1) <= 3 && c <= 3 || d / 2 > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 256; i++)
			{
			
				int a = ((i & 0b11));
				int b = ((i & 0b1100) >> 2);
				int c = ((i & 0b110000) >> 4);
				double d = ((i & 0b11000000)>> 6);
			
				
				// true != true (false) && true || false
				//
				boolean expected =  a > 2 != (b + 1) <= 3 && c <= 3 || d / 2 > 1 ;
				
				e.setVariable("a", a );
				e.setVariable("b", b );
				e.setVariable("c", c );
				e.setVariable("d", d );
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" (a > 2 != (b + 1) <= 3 ) && c <= 3 || d / 2 > 1 ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 256; i++)
			{
			
				int a = ((i & 0b11));
				int b = ((i & 0b1100) >> 2);
				int c = ((i & 0b110000) >> 4);
				double d = ((i & 0b11000000)>> 6);
			
				
				// true != true (false) && true || false
				//
				boolean expected = ( a > 2 != (b + 1) <= 3) && c <= 3 || d / 2 > 1 ;
				
				e.setVariable("a", a );
				e.setVariable("b", b );
				e.setVariable("c", c );
				e.setVariable("d", d );
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" (a > 2 != (b + 1) <= 3 ) && (c <= 3 || d / 2 > 1) ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 256; i++)
			{
			
				int a = ((i & 0b11));
				int b = ((i & 0b1100) >> 2);
				int c = ((i & 0b110000) >> 4);
				double d = ((i & 0b11000000)>> 6);
			
				
				// true != true (false) && true || false
				//
				boolean expected = ( a > 2 != (b + 1) <= 3) && ( c <= 3 || d / 2 > 1 ) ;
				
				e.setVariable("a", a );
				e.setVariable("b", b );
				e.setVariable("c", c );
				e.setVariable("d", d );
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		{
			ExpressionBuilder eb = new ExpressionBuilder(" a > 2 != (b + 1) <= 3 && (c <= 3 || d / 2 > 1) ");
			eb.operator(ForbiddenOperators.operators);
			eb.variables("a","b","c","d");
			Expression e = eb.build();
			for(int i=0; i < 256; i++)
			{
			
				int a = ((i & 0b11));
				int b = ((i & 0b1100) >> 2);
				int c = ((i & 0b110000) >> 4);
				double d = ((i & 0b11000000)>> 6);
			
				
		
				boolean expected =  a > 2 != (b + 1) <= 3 && ( c <= 3 || d / 2 > 1 ) ;
				
				e.setVariable("a", a );
				e.setVariable("b", b );
				e.setVariable("c", c );
				e.setVariable("d", d );
	
				 System.out.println("(" + a +","+ b +"," + c + "," + d + ")=>" + expected);
				 
				assertEquals("Expected result of expression (" + a +","+ b +"," + c + "," + d + ") didn't match",expected, e.evaluate() > 0);
				
			}
		}
		
		
		
	}
	
	@Test
	@Ignore
	public void exp4JThreadSafetyBug() throws InterruptedException, ExecutionException
	{
		//This test should fail
		StringBuilder sb = new StringBuilder();
		
		final int LIMIT = 1000000;
		for(int i=0; i < LIMIT -1; i++)
		{
			sb.append("x+");
		}
		
		sb.append("x");
		
		
		ExpressionBuilder eb = new ExpressionBuilder(sb.toString());
		eb.variable("x");
		Expression e = eb.build();
		
		e.setVariable("x", 1);
		
		
		ExecutorService execService = Executors.newCachedThreadPool();
		
		Future<Double> d = e.evaluateAsync(execService);
		e.setVariable("x", 2);
		
		Future<Double> d2 = e.evaluateAsync(execService);
		double resultOne = d.get();
		
		double resultTwo = d2.get();
		
		execService.shutdown();
		assertNotEquals("These result should not be equal", resultOne , resultTwo);
	}
}
