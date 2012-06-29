package solver;
import java.util.ArrayList;

public class Cell {

	public static int UNKNOWN = 1;
	public static int ROOM = 2;
	public static int WALL = 3;

	public int value;
	private int type;
	public Table parent;
	public int row;
	public int column;
	public Room roomContainer;
	public ArrayList<Cell> wallContainer;
	private boolean enclosed;
	public ReasonCode reasonCode;
	public Cell next;
	public ArrayList<Room> potentialOwners;
	
	public Cell(Table table, int row, int col)
	{
		parent = table;
		this.row = row;
		this.column = col;
		type = UNKNOWN;
		value = 0;
		roomContainer = null;
		enclosed=false;
		reasonCode=ReasonCode.UNKNOWN;
		potentialOwners=new ArrayList<Room>();
	}

	public Cell north()
	{
		return parent.cellAt(row-1,column);
	}
	public Cell east()
	{
		return parent.cellAt(row,column+1);
	}
	public Cell south()
	{
		return parent.cellAt(row+1,column);
	}
	public Cell west()
	{
		return parent.cellAt(row,column-1);
	}

	public int type() { return type; }
	
	public String typeStr()
	{
		return (type==UNKNOWN?"UNKNOWN":(type==ROOM?"ROOM":"WALL"));
	}
	
	public int mark(int newType, ReasonCode newReasonCode)
	{
		int count=(type()==UNKNOWN) ? 1 : 0;
		if (type()==UNKNOWN  &&  type()!=newType)
		{	
			this.type=newType;
			this.reasonCode=newReasonCode;
			//System.out.println("Cell "+text()+"marked itself as " + typeStr()+
			//		" due to "+newReasonCode.text);
			
			if (newType==WALL)
				parent.theWall.add(this);
			
			if (newType!=UNKNOWN)
				parent.unknown.remove(this);
		}
		return count;
	}

	public ArrayList<Cell> surroundingCells()
	{
		ArrayList<Cell> options = new ArrayList<Cell>();
		if (north()!=null) options.add (north());
		if (east()!=null)  options.add (east());
		if (south()!=null) options.add (south());
		if (west()!=null)  options.add (west());
		return options;
	}
	
	public ArrayList<Cell> availableSurroundingCells(int typeToMatch)
	{
		// iterate on the surrounding cells for this one
		// if any are available or of the specified type then return them
		ArrayList<Cell> available=new ArrayList<Cell>();
		for (Cell cell: surroundingCells())
			if (cell.type()==Cell.UNKNOWN || cell.type()==typeToMatch)
				available.add(cell);
		return available;
	}

	public int distance(Cell primary)
	{
		// this is VERY simple and needs to really walk the route to check
		// for obstacles...
		
		if (primary!=null)
		{
			int horiz = java.lang.Math.abs(primary.column-column);
			int vert  = java.lang.Math.abs(primary.row-row);
			
			return horiz+vert+1;
		}
		return 0;
	}

	public String text() 
	{
		return "("+row+", "+column+")" + (enclosed?"* ":" ");
	}
	
	public String longText()
	{
		return text()+typeStr()+" in "+(roomContainer==null?"null ":roomContainer.text()+" ");
	}

	public int markWallBetweenAdjacentRooms() 
	{
		//System.out.println("Checking room adjacency around cell "+text()+"contained by "
		//		+roomContainer.text());
		
		int cellsChanged=0;
		if (roomContainer==null)
			return cellsChanged;
		
		Cell first = north();
		if (first!=null)
		{
			Cell second = first.north();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}

			second = first.east();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}

			second = first.west();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}		
		}
	
		first = east();
		if (first!=null)
		{
			Cell second = first.east();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}
			
			second = first.north();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}

			second = first.south();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}
		
		}
	
		first = south();
		if (first!=null)
		{
			Cell second = first.south();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}

			second = first.east();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}

			second = first.west();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}
		
		}
	
		first = west();
		if (first!=null)
		{
			Cell second = first.west();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}
			
			second = first.north();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}

			second = first.south();
			if (second !=null)
			{
				if (second.type()==ROOM && second.roomContainer!=null && second.roomContainer!=roomContainer)
					cellsChanged += first.mark(WALL,ReasonCode.SEPARATE_ADJACENT_ROOMS);
			}
		}
		return cellsChanged;
	}
	
	public void setEnclosed(boolean val)
	{
		//System.out.println (text()+" marks itself as enclosed.");
		enclosed=val;
	}
	
	public boolean enclosed() { return enclosed; }
}