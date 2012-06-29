package solver;

public class Solver
{
	private static TablePrinter printer = new TablePrinterHTML(); 
	private static TablePrinter plainPrinter = new TablePrinterText();
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int numFiles = 0;
		for (String s: args)
		{
			if ("-regression".equals(s))
			{
				String fname = "regression.txt";
				RegressionTester tester = new RegressionTester(fname);
				tester.go();
			}
			else
			{
				Table table = solver.NurikabeFileReader.readFile (s);
			    if (table!=null)
			    {
			    	long start = System.currentTimeMillis();
					table.preProcess();
	
					boolean solved = table.solve();
					System.out.println((solved?"Solved ":"Could not solve ")+s+" in duration "+(System.currentTimeMillis()-start)+"ms.");
					solver.NurikabeFileWriter.writeFile (s, "html", null, printer.print(table));
					if (solved)
						solver.NurikabeFileWriter.writeFile(s, "text", null, plainPrinter.print(table));
					numFiles++;
			    }
		    }
		}

		if (numFiles>0)
			System.out.println ("Completed for " + numFiles + " individual puzzles.");
	}
}
