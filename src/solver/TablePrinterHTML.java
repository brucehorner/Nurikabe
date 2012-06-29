package solver;

public class TablePrinterHTML implements TablePrinter
{	
	@Override
	public String print(Table table)
	{
		return print (table, null, null);
	}
	
	@Override
	public String print(Table table, String prev, String next)
	{
		StringBuffer buf = new StringBuffer ();
		buf.append(header());

		for (int row=0; row<table.height; row++)
		{
			buf.append("<tr>");
			for ( int col=0;col<table.width; col++)
			{
				Cell cell = table.cells[row][col];
				int value=0;
				buf.append ("<td class=\"");
				if (cell.type()==Cell.ROOM)
				{
					buf.append("room");
					value = cell.value;
				}
				else if (cell.type()==Cell.WALL)
					buf.append("wall");
				else
					buf.append("unknown");

				buf.append ("\" title=\"");
				buf.append(cell.reasonCode.text);
				buf.append ("\">");
				buf.append (value==0?"&bull;":value);
				buf.append (" </td>");
			}
			buf.append("</tr>\n");
		}

		buf.append(tail(table.name,prev,next));
		return buf.toString();
	}

	private String header()
	{
		return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
			+ "<html><head><title>Puzzle</title>" +
			  "<style>table, th, td "
			+ "{ font-family:\"Arial\"; border: 1px solid black; text-align: center;padding: 10px 15px 10px 15px; }\n"
			+ "td.wall { background-color:black; }	" +
			  "td.unknown { background-color:#C8C8C8; color:#C8C8C8; } "
			+ "td.room { background-color:white; font-weight:bold; }\n"
			+ "</style></head><body>\n"
			+ "<table><tr><td>"
			+ "<table cellspacing=\"0\" cellpadding=\"0\"> <tbody>\n";
	}

	private String tail(String basename, String prev, String next)
	{
		return "\n</tbody></table></td><td>"
			+ buttonZone(basename, prev, next)
			+ "</td></tr></table></body></html>";
	}
	
	private String buttonZone(String basename, String prev, String next)
	{
		StringBuffer buf = new StringBuffer();
		if (basename!=null)
		{
			if (prev!=null)
			{
				buf.append("<button style=\"width:65;height:65\" onClick=\"");
				buf.append(nextPrevString(basename,prev));
				buf.append("\"><b>Prev</b></button>");
			}
			
			if (next!=null)
			{
				buf.append("<button style=\"width:65;height:65\" onClick=\"");
				buf.append(nextPrevString(basename,next));
				buf.append("\"><b>Next</b></button>");
			}
		}
		
		return buf.toString();
	}
	
	private String nextPrevString(String basename, String target)
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append("window.location='");
		buf.append(basename);
		buf.append("."+target);
		buf.append(".html");
		buf.append("'");
		
		return buf.toString();
	}
}

