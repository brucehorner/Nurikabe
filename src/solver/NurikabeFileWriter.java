package solver;
import java.io.*;

public class NurikabeFileWriter
{
	public static void writeFile (String name, String type, String subtype, String contents)
	{
		String outputFileName = name + ".";
		if (subtype!=null)
			outputFileName += (subtype+".");
		outputFileName += type;

		try
		{
    		FileWriter fstream = new FileWriter(outputFileName);
    	    BufferedWriter out = new BufferedWriter(fstream);
    		out.write(contents);
   			out.close();
    	}
    	catch (Exception e)
    	{
			System.err.println("Error: " + e.getMessage());
    	}
	}
}