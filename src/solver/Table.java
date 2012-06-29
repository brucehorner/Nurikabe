package solver;
import java.util.ArrayList;

public class Table
{
	public String name;
	public int width;
	public int height;
	public Cell[][] cells;
	public ArrayList<Room> rooms;
	public int totalRoomSize = 0;
	public ArrayList<Cell> unknown;
	public Wall theWall;
	public ArrayList<Cell> orphans;
	
	public Table (int w, int h)
	{
		rooms = null;
		width = w;
		height = h;
		cells = new Cell[h][w];	// rows, then columns
		unknown = new ArrayList<Cell>();
		theWall = new Wall(this);
		orphans = new ArrayList<Cell>();
		for (int row=1;row<=h;row++)
		{
			for (int col=1;col<=w;col++)
			{
				cells[row-1][col-1] = new Cell(this,row,col);
				unknown.add (cells[row-1][col-1]);
			}
		}
	}


	public boolean solve()
	{
		int verbose=0;
		boolean solved = false;
		TablePrinter printer = new TablePrinterHTML(); 
	  
		// first pre-process and surround all "1" rooms with a wall
		for (Room room : rooms)
		{
			if (room.size==1)
			{
				Cell primary = cellAt (room.row,room.column);
				ArrayList<Cell> neighbours = primary.surroundingCells();
				for (Cell cell: neighbours)
					cell.mark(Cell.WALL,ReasonCode.ROOM_OF_1);
				primary.setEnclosed(true);
			}
		}
	
		// 1. Now look for nearby room cells that cannot be in the same room
		// and mark the cell in between as a wall cell
		// this is a special case of keeping a wall between known rooms
		for (Room room: rooms)
			room.primary.markWallBetweenAdjacentRooms();		
		
		// 2. Mark unreachable cells
		Networker unreachableFinder=new Networker();
		unreachableFinder.markUnreachable(this);
		
		// repeat
		String prevStage = null;
		for (int j=0; j<20; j++)
		{
			String stageBase=new Integer(j+1).toString();
			String stage=stageBase+"a";
			String nextStage=stageBase+"b";
			
			if (verbose>0) System.out.println ("\n ====================== LOOP "+stage+" ========================");
			int changed = expandRooms();
			NurikabeFileWriter.writeFile (name, "html", stage, printer.print(this,prevStage,nextStage));
			prevStage=stage;
			stage=nextStage;
			nextStage=stageBase+"c";

			if (verbose>0) System.out.println ("\n ====================== LOOP "+stage+" ========================");
			changed += theWall.expand(false);
			NurikabeFileWriter.writeFile (name, "html", stage, printer.print(this,prevStage,nextStage));
			prevStage=stage;
			stage=nextStage;
			nextStage=stageBase+"d";

			if (verbose>0) System.out.println ("\n ====================== LOOP "+stage+" ========================");
			changed += checkBlockofFour();			
			NurikabeFileWriter.writeFile (name, "html", stage, printer.print(this,prevStage,nextStage));
			prevStage=stage;
			stage=nextStage;
			nextStage=stageBase+"e";

			if (verbose>0) System.out.println ("\n ====================== LOOP "+stage+" ========================");
			changed += adoptOrphans(0);
			NurikabeFileWriter.writeFile (name, "html", stage, printer.print(this,prevStage,nextStage));
			prevStage=stage;
			stage=nextStage;
			nextStage=stageBase+"f";

			if (complete())
			{
				//System.out.println ("\n ^^^  WE'RE DONE!  ^^^");
				solved=true;
				break;
			}
			
			// if we didn't do anything, then try an unreachable search
			// this is expensive so only do as last resort
			if (changed==0)
			{
				if (verbose>0) System.out.println ("\n ====================== LOOP "+stage+" ========================");
				changed += unreachableFinder.markUnreachable(this);				
			}
			NurikabeFileWriter.writeFile (name, "html", stage, printer.print(this,prevStage,nextStage));
			prevStage=stage;
			stage=nextStage;
			String nextStageBase=new Integer(j+2).toString();
			nextStage=nextStageBase+"a";

			if (complete())
			{
				//System.out.println ("\n ^^^  WE'RE DONE!  ^^^");
				solved=true;
				break;
			}

			// if still no changes then force a wall expansion
			if (changed==0)
			{
				if (verbose>0) System.out.println ("\n ====================== LOOP "+stage+" ========================");
				changed+= theWall.expand(true);

				// if STILL nothing, do a room expansion via walking
				if (changed==0)
					unreachableFinder.roomExpand(this);
			
			}
			NurikabeFileWriter.writeFile (name, "html", stage, printer.print(this,prevStage,nextStage));
			prevStage=stage;

			if (complete())
			{
				//System.out.println ("\n ^^^  WE'RE DONE!  ^^^");
				solved=true;
				break;
			}

			// if still no changes then abandon
			if (changed==0)
			{
				if (verbose>1) System.out.println("Breaking early after "+(j+1)+" loops since nothing else to do.");
				break;
			}			
			
		}
		
		if (verbose>1) printStats();
		return solved;
	}
	
		
	public boolean markCell (int row, int column, int type, ReasonCode reasonCode)
	{
		Cell cell = cellAt(row,column);
		if (cell!=null)
			return markCell(cell,type,reasonCode);
		return false;
	}
	
	public boolean markCell (Cell cell, int type, ReasonCode reasonCode)
	{
		if ( cell!=null )
		{
			cell.mark(type,reasonCode);
			
			if (type==Cell.WALL)
				theWall.add(cell);
			
			if (type!=Cell.UNKNOWN)
				unknown.remove(cell);
			
			return true;
		}

		return false;
	}

	public void addRoom (Room room)
	{
		if (room==null)
			return;
		
		if (rooms==null)
			rooms = new ArrayList<Room>();
		rooms.add(room);

		room.initialize(cellAt(room.row,room.column));
	}

	public void preProcess()
	{
		totalRoomSize = 0;
		for (Room room : rooms) {
			totalRoomSize += room.size;
		}

		int totalWallSize = (height*width) - totalRoomSize;
		theWall.setMaxSize(totalWallSize);
	}

	public boolean complete()
	{
		return theWall.done() && roomsComplete();
	}
	
	public Cell cellAt(int row, int column)
	{
		if ( (row>0 && row <= height) && (column>0 && column <= width) )
			return cells[row-1][column-1];

		return null;
	}
	
	public Cell unknownNeighbourIntersect (Cell a, Cell b)
	{
		if ( a!=null && b!=null )
		{
			if (a==b)
				return null; //a;
			else
			{
				ArrayList<Cell> setA = a.surroundingCells();				
				ArrayList<Cell> setB = b.surroundingCells();
				/**
				for (Cell testA: setA)
					System.out.println (" a> " + testA.text());
				for (Cell testB: setB)
					System.out.println (" b> " + testB.text());
				**/
				ArrayList<Cell> intersect = new ArrayList<Cell>();
				
				for (Cell aa: setA)
					for (Cell bb: setB)
						if (aa==bb)
						{
							//System.out.println(" >> found intersection at " + aa.text());
							if (aa.type()==Cell.UNKNOWN)
								intersect.add(aa);
						}
			
				if (intersect.size()==1)
					return intersect.get(0);
			}
		}
		return null;
	}
	
	private boolean roomsComplete()
	{
		boolean allFull=true;
		for (Room room: rooms)
			if (room.isFull()==false)
			{
				allFull=false;
				break;
			}
		return allFull;
	}
	
	public int addOrphanRoomCell(Cell newOrphan, ReasonCode reason)
	{
		//System.out.println("Adding new Orphan "+newOrphan.text());
		int changed = newOrphan.mark(Cell.ROOM, reason);
		orphans.add(newOrphan);
		return changed;
	}

	public String printCellList(ArrayList<Cell> list)
	{
		StringBuffer buf=new StringBuffer();
		for (Cell cell:list)
			buf.append(cell.text());
		return buf.toString();
	}
	
	public void printStats()
	{
		System.out.println ("Table has " + this.height + " rows and "
				+ this.width + " columns.  There are " + this.rooms.size()
				+ " rooms, with wall size of " + theWall.maxSize()
				+ ".  Currently known wall size is " + theWall.currentSize()
				+ ", and there are " + this.unknown.size() + " unknown cells.");
	
		for (Room room: rooms)
			System.out.println(room.text());

		System.out.print ("There are "+orphans.size()+" orphans: ");
		System.out.println(printCellList(orphans));
		
		theWall.print();
	}



	private int checkBlockofFour() 
	{
		int cellsChanged = 0;
		ArrayList<Cell> wall = theWall.fullSet();
		ArrayList<Cell> candidates = new ArrayList<Cell>();
		for (Cell wallCell: wall)
		{
			if ( wallCell.south()!=null && wallCell.west()!=null)
			{
				if (wallCell.south().type()==Cell.WALL &&
					wallCell.west().type()==Cell.WALL)
					candidates.add(wallCell.south().west());
			}
	
			if ( wallCell.south()!=null && wallCell.east()!=null)
			{
				if (wallCell.south().type()==Cell.WALL &&
					wallCell.east().type()==Cell.WALL)
					candidates.add(wallCell.south().east());
			}
	
			if ( wallCell.north()!=null && wallCell.west()!=null)
			{
				if (wallCell.north().type()==Cell.WALL &&
					wallCell.west().type()==Cell.WALL)
					candidates.add(wallCell.north().west());
			}
	
			if ( wallCell.north()!=null && wallCell.east()!=null)
			{
				if (wallCell.north().type()==Cell.WALL &&
					wallCell.east().type()==Cell.WALL)
					candidates.add(wallCell.north().east());
			}
			
		}		
		
		// now mark all the found cells as rooms, which are initially orphans
		for (Cell target: candidates)	
		{
			if (target.type()==Cell.ROOM)
				continue;
			cellsChanged += addOrphanRoomCell(target,ReasonCode.AVOID_BLOCK_OF_4);
		}
		
		return cellsChanged;
	}


	private int expandRooms()
	{
		int cellsChanged=0;
		//System.out.println("Room expansion...");
		
		for (Room room : rooms)
		{
			if (!room.isFull())
			{
				//System.out.println ("Looking at Room at " + room.primary.text());
				ArrayList<Cell> availableCells = room.availableCells();
				if (availableCells.size()==1) // can we auto-expand ?
				{
					if (!room.primary.enclosed())
						room.primary.setEnclosed(true);
					Cell expansion = availableCells.get(0);
					//System.out.println ("   > expanding room into cell " + expansion.text());
					cellsChanged += room.expand (expansion);
					ArrayList<Cell> neighbours = expansion.surroundingCells();
					
					// was that the last cell in the room? if so, mark surrounding wall
					if (room.isFull())
					{
						if (!expansion.enclosed())
							expansion.setEnclosed(true);
						for (Cell edge: neighbours)
						{
							if (edge!=null && edge.type()==Cell.UNKNOWN)
								cellsChanged += edge.mark(Cell.WALL,ReasonCode.EXPAND_ROOM_ENCLOSE);
						}						
					}
					else 
					{
						// let's see if this new room cell is too close to another room
						cellsChanged += expansion.markWallBetweenAdjacentRooms();						
					}
				}
				else if (availableCells.size()==2)
				{
					// check for adjacent terminal wall
					if ( room.cells().size()==room.size-1)	// do we need just one more cell?
					{
						Cell a = availableCells.get(0);
						Cell b = availableCells.get(1);
						//System.out.println("Room "+room.text());
						//System.out.println("Two available cells, so check adjacent terminals for "+a.text()+", "+b.text());
						// if so , mark the shared adjacent cell as a wall
						// if both cells are in fact the same one, then it must be the last cell itself
						if (a==b)
						{
							//System.out.println ("Only one cell left, so mark as the room");
							cellsChanged+=room.expand(a);
						}
						else
						{
							Cell target = unknownNeighbourIntersect(a, b);
							if (target!=null  &&  target.type()==Cell.UNKNOWN)
							{
								cellsChanged+=target.mark(Cell.WALL,ReasonCode.UNKNOWN_NEIGHBOUR);
							}
						}
					}
				}				
			}
		}
	
		return cellsChanged;
	}

	
	private int adoptOrphans(int depth)
	{
		int cellsChanged=0;
		//System.out.println(depth+": adopting orphans...");
		ArrayList<Cell> expansionList = new ArrayList<Cell>();
		ArrayList<Cell> subtractionList = new ArrayList<Cell>();
		
		for (Cell orphan:orphans)
		{
			// something may have changed this cell's state during this loop
			// so to save us some time, if this is no longer and orphan move on
			if (orphan.roomContainer!=null)
				continue;
		
			//System.out.println("Orphan "+orphan.text());
			ArrayList<Cell> neighbours = orphan.availableSurroundingCells(Cell.ROOM);
			//System.out.println(" *** "+neighbours);
			if (orphan.next!=null)
				neighbours.remove(orphan.next);
			//System.out.println(" ****** "+neighbours);
			
			if (neighbours.size()==1)
			{
				Cell next = neighbours.get(0);
				if (next.type()==Cell.UNKNOWN)
				{
					//System.out.println(" > orphan "+orphan.text()+"can only go to "+next.text());
					expansionList.add(next);
					orphan.setEnclosed(true);
					next.next=orphan;	// chain these together
				}
			}
			for (Cell potential:neighbours)
			{
				//System.out.println(" > potential adoption at "+potential.text());
				Room room = potential.roomContainer;
				if (potential.type()==Cell.ROOM  &&  room!=null  
						&&  room.primary!=orphan
						&&  orphan.roomContainer!=potential.roomContainer)
				{
					//System.out.println (" > Adopting orphan "+orphan.text()+"to room at "+ room.primary.text());
					Cell adoptee=orphan;
					while (adoptee!=null)
					{
						//System.out.println("  >> expanding for "+adoptee.text());
						room.expand(adoptee);						
						cellsChanged+=adoptee.markWallBetweenAdjacentRooms();		
						subtractionList.add(adoptee);						
						adoptee=adoptee.next;	// walk the list
						//System.out.println("  >> adoptee moved down chain to "+
						//		(adoptee!=null?adoptee.text():null));
					}
				}
			}
		}
	
		for (Cell newOrphan:expansionList)
		{
			cellsChanged+=addOrphanRoomCell(newOrphan, ReasonCode.EXPAND_ORPHAN);
		}
		
		
		for (Cell matched:subtractionList)
		{
			//System.out.println ("removing "+matched.text()+"from orphan list");
			orphans.remove(matched);
			// if this is now full enclosed , mark it so
			ArrayList<Cell> neighbours=matched.availableSurroundingCells(Cell.UNKNOWN);
			if (neighbours.size()==0  && !matched.enclosed())
				matched.setEnclosed(true);
		}
	
		// let's check for any adoptions for the new cells
		if (expansionList.size()>0 && depth<10)
			cellsChanged+=adoptOrphans(depth+1);
		
		return cellsChanged;
	}


	public void clearPotentialOwners()
	{
		for (int row=1;row<=height;row++)
			for (int col=1;col<=width;col++)
				cells[row-1][col-1].potentialOwners.clear();
	}
	
}