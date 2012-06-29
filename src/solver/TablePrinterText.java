package solver;

public class TablePrinterText implements TablePrinter {
	
	@Override
	public String print (Table table, String prev, String next)
	{
		return print(table);
	}
	
	@Override
	public String print(Table table) 
	{
		StringBuffer buf = new StringBuffer ();

		for (int row=0; row<table.height; row++)
		{
			for ( int col=0;col<table.width; col++)
			{
				Cell cell = table.cells[row][col];
				
				if (cell.type()==Cell.ROOM)
				{
					if (cell.value==0)
						buf.append(".");
					else
						buf.append(cell.value);
				}
				else if (cell.type()==Cell.WALL)
					buf.append("x");
				else
					buf.append("?");

			}
			buf.append("\n");
		}

		return buf.toString();
	}
}

