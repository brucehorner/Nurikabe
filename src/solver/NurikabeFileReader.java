package solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NurikabeFileReader {

	public static Table readFile (String filename)
	{
	    Table table = null;
		try {			
			//System.out.println("Reading file " + filename + "...");
			
		    BufferedReader in = new BufferedReader(new FileReader(filename));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	if (!str.startsWith("#") && str.length()>0)
		    	{
		    		if (table==null)
		    		{
		    			table = NurikabeFileReader.createTable (str);
		    			table.name=filename;
		    		}
		    		else
		    		{
			    		NurikabeFileReader.createRoom (table,str);
		    		}
		    	}
		    }
		    in.close();
	
		    
		} catch (IOException e)
		{
			System.err.println (e);
		}
		
		//System.out.println("... completed.\n");		
		return table;
		
	}

	public static Table createTable (String str)
	{
		if (str!=null)
		{
			String[] values = str.split(",");
			if (values.length==2)
			{
				int width = Integer.parseInt(values[0]);
				int height= Integer.parseInt(values[1]);
				
				return new Table (width,height);				
			}						
		}
	
		return null;		
	}

	public static Room createRoom (Table table,String str)
	{
		if (str!=null)
		{
			String[] values = str.split(",");
			if (values.length==3)
			{
				int size = Integer.parseInt(values[0]);
				int row  = Integer.parseInt(values[1]);
				int column  = Integer.parseInt(values[2]);
				
				return new Room (table, row, column, size);				
			}						
		}
	
		return null;		
	}

}
