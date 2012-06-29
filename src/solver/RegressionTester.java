package solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RegressionTester
{
	ArrayList<String> filenames;
	private static TablePrinter plainPrinter = new TablePrinterText();
	
	public RegressionTester(String filename)
	{
		if (filename!=null)
		{
			filenames = new ArrayList<String>();
			try
			{			
				System.out.println("Reading file " + filename + "...");
				
			    BufferedReader in = new BufferedReader(new FileReader(filename));
			    String str;
			    while ((str = in.readLine()) != null)
			    {
			    	if (!str.startsWith("#") && str.length()>0)
			    	{
			    		System.out.println (str);
			    		filenames.add(str);
			    	}
			    }
			    in.close();
			    
			} catch (IOException e)
			{
				System.err.println (e);
			}
			
			System.out.println("... completed.\n");		
		
		}
	}

	public void go()
	{
		int numSolved=0;
		for (String name: filenames)
		{
			Table table = NurikabeFileReader.readFile (name);
		    if (table!=null)
		    {
		    	long start = System.currentTimeMillis();
				table.preProcess();
				boolean solved = table.solve();
				long duration=System.currentTimeMillis()-start;
				
				System.out.println((solved?"Solved ":"Could not solve ")+name+" in duration "+duration+"ms.");
				if (solved)
				{
					NurikabeFileWriter.writeFile(name, "text", null, plainPrinter.print(table));
					if (compare(name,"text","master"))
						numSolved++;
				}
		    }
			
		}
		
		System.out.println ("Solved " + numSolved + " out of "+filenames.size()+" puzzles.");
	
		
	}

	private boolean compare(String filename, String type, String masterType)
	{
		boolean matches=false;
		
		try
		{	
			// read the puzzle output into a buffer
			String testFname = filename + "." + type;
		    BufferedReader testfile = new BufferedReader(new FileReader(testFname));
		    StringBuffer testBuf=new StringBuffer();
		    String str;
		    while ((str=testfile.readLine()) != null)
		    	testBuf.append(str);
		    testfile.close();
 
		    // see if we can find a master, if not create one, if we can the compare them
		    String masterFname = filename+ "." + masterType;
		    try
		    {
			    BufferedReader masterfile =new BufferedReader(new FileReader(masterFname));
			    StringBuffer masterBuf=new StringBuffer();
			    while ((str=masterfile.readLine()) != null)
			    	masterBuf.append(str);
			    masterfile.close();

			    matches = testBuf.toString().equals(masterBuf.toString());
			    if (!matches)
			    	System.err.println(" > " +testFname+" does not match its master content from "+masterFname);
			    
		    }
		    catch (IOException x)
		    {
	    		FileWriter fstream = new FileWriter(masterFname);
	    	    BufferedWriter out = new BufferedWriter(fstream);
	    		out.write(testBuf.toString());
	   			out.close();
	   			System.err.println (" > created new master file" + masterFname);
	   			matches=true; // may as well be optimistic
		    }
		}
		catch (IOException e)
		{
			System.err.println (e);
		}
		
		return matches;
	}
}
