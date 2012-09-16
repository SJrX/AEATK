import java.io.IOException;
import java.util.Scanner;


public class Test {

	public static void main(String[] args)
	{
		try {
			Process proc = Runtime.getRuntime().exec("/home/ramage/testc/a.out");
			
			Scanner procIn = new Scanner(proc.getInputStream());
			
			
			while(procIn.hasNext())
			{
				System.out.println(procIn.nextLine());
			}
			
			System.out.println("==== ERR ====");
			
			procIn = new Scanner(proc.getErrorStream());
			
			
			while(procIn.hasNext())
			{
				System.out.println(procIn.nextLine());
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
