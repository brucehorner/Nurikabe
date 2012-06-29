package solver;

import java.util.ArrayList;

public class Networker 
{
	// Walk around the various parts of the given table and mark cells that
	// can't be reached
	public int markUnreachable(Table table) 
	{
		if (table==null)
			return 0;
		
		int cellsChanged = dumbVersion(table);
		//if (cellsChanged==0)
		//	cellsChanged = smartVersion(table);
		return cellsChanged; 
	}

	public int roomExpand(Table table)
	{
		System.out.println ("Networker.roomExpand");
		
		table.clearPotentialOwners();
		
		int cellsChanged=0;
		if (table!=null)
		{
			for (Room room:table.rooms)
			{
				if (room.isFull())
					continue;
				
				System.out.println(">look at "+room.text());				
				
				// start at room root, walk away following unknown cells and like room cells
				ArrayList<Cell> path = walkPath(room,room.primary,room.size,1,null);
				System.out.println(">full list of available cells:");
				for (Cell cell:path)
					System.out.print(cell.text());
				System.out.println("");
				
				if (path.size()==room.size)
				{
					System.out.println (" *** Can fill the room ***");
					for (Cell cell:path)
					{
						if (!room.cells().contains(cell))
							room.expand(cell);
					}
					
				}
				
			}
		}
		
		return cellsChanged;
	}
	
		
	private ArrayList<Cell> walkPath(Room room, Cell start, int size, int depth, ArrayList<Cell> knownCells)
	{
		String prefix = " "; for (int x=0;x<depth;x++) prefix+= " ";
		System.out.println(prefix+"Networker.walkPath, cell="+start.text()+"depth="+depth);
		
		// don't go further than the full size of the room
		if (depth>size)
			return null;
		
		// start at room root, walk away following unknown cells and like room cells
		// stop walking when hit a wall, or someone else's room
		// must avoid visiting cells we've already visited
		// check neighbours for this cell, if it looks good then add it and move on down
		
		ArrayList<Cell> list = new ArrayList<Cell>();
		ArrayList<Cell> neighbours=start.availableSurroundingCells(Cell.ROOM);
		ArrayList<Cell> extensionPoints = new ArrayList<Cell>();
		boolean extend=true;
		for (Cell neighbour:neighbours)
		{
			//System.out.print(prefix+" > neighbour "+neighbour.text());
			if (knownCells!=null  && knownCells.contains(neighbour))
			{
				//System.out.println ("already known");
				continue;
			}
			
			if (neighbour.type()==Cell.ROOM)
			{
				if (neighbour.roomContainer==null)
				{
					// orphans are possibles, mark them as potentially being one of ours
					//System.out.println(prefix+" > an orphan room");
					extensionPoints.add(neighbour);
					if (!neighbour.potentialOwners.contains(room))
						neighbour.potentialOwners.add(room);
				}
				else if (neighbour.roomContainer!=room)
				{
					// stop when hitting someone else's room
					//System.out.println(" > someone else's room ***, remove cell "+start.text());
					extend=false;
					break;
				}
				else
				{
					//System.out.println(" > a cell in our room");
					extensionPoints.add(neighbour);
				}
				
			} // if ROOM
			else
			{
				//System.out.println (neighbour.typeStr());
				if (neighbour.type()==Cell.UNKNOWN)
					extensionPoints.add(neighbour);
			}
			
		} // for each neighbour

		
		if (extend && extensionPoints.size()>0)
		{
			if (!list.contains(start))
			{
				//System.out.println(prefix+"> adding to list cell "+start.text());
				list.add(start);
			}
			
			if (depth<size)
			{
				for (Cell extension:extensionPoints)
				{
					ArrayList<Cell> childlist=walkPath(room, extension, size, depth+1, list);
					if (childlist!=null)
						for (Cell cell:childlist)
							if (!list.contains(cell))
									list.add(cell);					
				}
			}
		}
		
		
		return list;
	}

	
	
	private int smartVersion(Table table)
	{
		// this version will walk the paths
		int cellsChanged = 0;
		ArrayList<Cell> toBeMarked = new ArrayList<Cell>();
		//System.out.println ("Smart version: Checking "+table.unknown.size()+" cells for reachability...");
		for (Cell candidate:table.unknown)
		{
			for (Room room:table.rooms)
			{
				int remainingLength=room.size-room.cells().size();
				if (remainingLength==0)
					continue;
				
				for (Cell roomCell:room.cells())
				{
					if (roomCell.enclosed())
						continue;
					
					ArrayList<Cell> availableList=roomCell.availableSurroundingCells(Cell.ROOM);
					//System.out.print("Walking from "+roomCell.text());
					//for (Cell next:availableList)
					//	System.out.print(next.text());
					//System.out.println("");
					
				}
			}
			
		}
		
		
		return cellsChanged;
	}

	private int dumbVersion(Table table) 
	{
		int cellsChanged = 0;
		ArrayList<Cell> toBeMarked = new ArrayList<Cell>();
		//System.out.println ("Dumb Version: Checking "+table.unknown.size()+" cells for reachability...");
		for (Cell candidate: table.unknown)
		{
			//System.out.println (" > cell at "+candidate.column+", "+candidate.row);
			boolean reached = false;
			for (Room room : table.rooms)
			{
				//System.out.println ("   > room at "+room.primary.column+", "+room.primary.row);
				int distance = candidate.distance(room.primary);
				if (distance<=room.size)
				{
					reached=true;
					//System.out.println ("   > *** Reachable by room "+room);
					break;
				}
			} 
			
			// if no room can reach it, mark it 
			if (reached==false)
			{
				//System.out.println (" > UNREACHABLE");
				toBeMarked.add(candidate);
			}
		}
		
		for (Cell target: toBeMarked)
			cellsChanged += target.mark(Cell.WALL,ReasonCode.UNREACHABLE);
	
		return cellsChanged;
	}

}
