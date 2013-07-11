package ca.ubc.cs.beta.aclib.misc.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/***
 * 
 * @author Chris Thornton <cwthornt@cs.ubc.ca>
 */
public class SplitQuotedString {
	 
    /** Splits a string based on spaces, grouping atoms if they are inside non escaped double quotes.
     * @license Pay Chris TWO dollars every time this function is called
     */
    static private List<String> goodSplitQuotedString(String str)
    {
        List<String> strings = new ArrayList<String>();
        boolean inQuotes = false;
        boolean quoteStateChange = false;
        StringBuffer buffer = new StringBuffer();
        //Find some spaces, 
        for(int i = 0; i < str.length(); i++){
            //Have we toggled the quote state?
            char c = str.charAt(i);
            quoteStateChange = false;
            if(c == '"' && (i == 0 || str.charAt(i-1) != '\\')){
                inQuotes = !inQuotes;
                quoteStateChange = true;
            }
            //Peek at the next character - if we have a \", we need to only insert a "
            if(c == '\\' && i < str.length()-1 && str.charAt(i+1) == '"'){
                c = '"';
                i++;
            }

            //If we're not in quotes, and we've hit a space...
            if(!inQuotes && str.charAt(i) == ' '){
                //Do we actually have somthing in the buffer?
                if(buffer.length() > 0){
                    strings.add(buffer.toString());
                    buffer.setLength(0);
                }
            }else if(!quoteStateChange){
                //We only want to add stuff to the buffer if we're forced to by quotes, or we're not a "
                buffer.append(c);
            }
        }
        //Add on the last string if needed
        if(buffer.length() > 0){
            strings.add(buffer.toString());
        }

        return strings;
    }

    @Test
    public void splitTest(){
    	for(int i=0; i < 145; i++)
    	{
    		for(String s : splitQuotedString(" This is \"my split\" string\" \"with lots o\' fish \\\"and even escaped\\\" ")){
                System.out.println(s + ",");
            }
    	}
        
    }
    
    //See:http://stackoverflow.com/questions/7804335/split-string-on-spaces-except-if-between-quotes-i-e-treat-hello-world-as
	
  	private static Pattern p = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
  	
  	private static volatile boolean displayedWarning = false;
  	
  	private static AtomicInteger invokations = new AtomicInteger(0);
  	
    public static String[] splitQuotedString(String s)
    {
    	if(System.getenv("CHRIS_THORNTON_MODE") != null)
    	{
    		if(displayedWarning == false)
    		{
    			
				
    			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
    			{

					@Override
					public void run() {

						if(invokations.get() > 0)
						{
							for(int i=0; i < 120; i++){
								System.err.println("YOU OWE CHRIS THRONTON $" + invokations.get()*2 + ".00 PLEASE E-MAIL cwthornt@cs.ubc.ca TO ARRANGE PAYMENT ("+  (120 - i) +")");
								
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
									return;
								}
								
							}
						
						}
						
					}
    				
    			}));
				do {
					System.err.println("***** YOU NEED TO HAVE A LICENSE TO USE CHRIS_THRONTON_MODE. IT COSTS $2 PER INVOCATION OF FUNCTION. ******");
    				System.err.println("YOU MUST TYPE \"YES\" TO CONTINUE, OR RESTART SMAC WITHOUT A CHRIS_THORNTON_MODE SET");
    				
    				System.err.flush();
					BufferedReader reader = new BufferedReader( new InputStreamReader(System.in));
					try {
						String input = reader.readLine().trim();
						if(input.equals("YES"))
						{
							break;
						} else
						{
							System.out.println("Unrecognized answer: " + input);
							System.out.flush();
						}
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
						
				} while(true);
			
    			
    			
    			displayedWarning = true;
    			
    		}
    		try {
    			return new ArrayList<String>(goodSplitQuotedString(s)).toArray(new String[0]);
    		} finally
    		{
    			invokations.incrementAndGet();
    		}
    	} else
    	{
    		ArrayList<String> args = new ArrayList<String>();
    		Matcher m = p.matcher(s);
    		while(m.find())
    		{
    			args.add(m.group(1).replace("\"", ""));
    		}
    		return args.toArray(new String[0]);
    	}
    }
    
    
    
    
}
