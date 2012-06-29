package solver;
import java.util.ArrayList;

public class Room {

	public int column;
	public int row;
	public int size;
	Cell primary;
	private ArrayList<Cell> cells;
	private Table parent;

	public Room (Table table, int row, int column, int size)
	{
		this.column = column;
		this.row = row;
		this.size = size;
		primary = null;
		cells = new ArrayList<Cell>();
		parent = table;
		if (table!=null) table.addRoom(this);
	}

	public void initialize(Cell starter)
	{
		if (starter==null)
			return;
		
		if (parent.markCell(this.row,this.column,Cell.ROOM,ReasonCode.INITIAL_ROOM))
		{
			primary = starter;
			primary.value = this.size;
			primary.roomContainer = this;
			cells.add(primary);
			primary.roomContainer = this;
		}
	}
	
	public int expand (Cell cell)
	{
		cells.add (cell);
		int count = cell.mark (Cell.ROOM,ReasonCode.EXPAND_ROOM);
		cell.roomContainer=this;
		//System.out.println("Cell "+cell.text()+"now part of room "+text());
		
		if (isFull())
			count+=encloseRoom();
		
		return count;
	}
	
	private int encloseRoom()
	{
		int cellsChanged=0;
		
		// walk down all the room's cells and make sure
		// all blanks are marked as walls
		for (Cell cell:cells)
		{
			ArrayList<Cell> neighbours=cell.availableSurroundingCells(Cell.UNKNOWN);
			for (Cell neighbour:neighbours)
			{
				//System.out.println (" > Should mark WALL for cell "+neighbour.text());
				cellsChanged+=neighbour.mark(Cell.WALL, ReasonCode.EXPAND_ROOM_ENCLOSE);
			}
		}
		
		return cellsChanged;
	}

	public boolean isFull ()
	{
		return cells==null?false:size==cells.size();
	}

	public String text()
	{
		String s = "Rm at "+ primary.text() +"of " + size + (isFull()?"*":"") 
			+ "=> ";
		
		for (Cell cell: cells)
			s += cell.text();
		
		return s;
	}
	
	public ArrayList<Cell> cells()
	{
		return cells;
	}
	
	public ArrayList<Cell> availableCells()
	{
		ArrayList<Cell> available=new ArrayList<Cell>();

		for (Cell cell: cells())
		{
			if (cell.enclosed())
				continue;
			
			//System.out.println (" > "+cell.longText());
			
			ArrayList<Cell> options = cell.surroundingCells();
			
			for (Cell option: options)
			{
				//System.out.print ("   > looking at cell " + option.text());
				// does this cell even exist, and if so is not already marked?
				if (option!=null && option.type()==Cell.UNKNOWN)
				{
					//System.out.println (" ... found expansion option at " + option.text());
					available.add(option);
				}
				//else
					//System.out.println (" ... not available.");
			}
		}	
		
		return available;
	}
}
