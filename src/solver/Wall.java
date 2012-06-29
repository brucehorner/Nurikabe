package solver;
import java.util.ArrayList;
public class Wall
{
	
	private class Package
	{
		public Cell expand;
		public Cell enclosed;
		public Package (Cell expand,Cell enclosed) {this.expand=expand; this.enclosed=enclosed;}
	}
	
	private ArrayList<Cell> fullSet;
	private ArrayList<ArrayList<Cell>> wallFragments;
	private int maxSize;
	private Table parent;
	
	public Wall(Table parent)
	{
		this.parent=parent;
		maxSize = 0;
		fullSet = new ArrayList<Cell>();
		wallFragments = new ArrayList<ArrayList<Cell>>();
	}

	public void setMaxSize(int maxSize)	{ this.maxSize = maxSize; }
	public int maxSize() { return maxSize; }
	public ArrayList<Cell> fullSet() { return fullSet; }
	public int currentSize() { return fullSet.size(); }
		
	public boolean done()
	{
		boolean done=false;
		
		//System.out.println ("Testing table for done: currentWallSize is "+fullSet.size() + " out of max "
		//		+ maxSize);
		
		if (wallFragments.size()==1)
			done = (maxSize == wallFragments.get(0).size());
		return done;
	}
	
	public void add (Cell cell)
	{
		// make sure we don't already know about this wall cell
		if (cell!=null && !fullSet.contains(cell))
		{
			fullSet.add(cell);
			//System.out.print(" > added new wall at " + cell.text());
			
			// are there any adjacent wall segments? if so join this 
			ArrayList<Cell> neighbours = cell.availableSurroundingCells(Cell.WALL);
			//System.out.println("which has "+neighbours.size()+" neighbours.");
			boolean merged = false;
			
			for (Cell trial: neighbours)
			{
				if (trial.type()==Cell.WALL)
				{
					ArrayList<Cell> fragment = trial.wallContainer;
					if (fragment==null)
					{
						System.out.println ("*** ERR null wall fragment for existing cell");
						continue;
					}
					
					//System.out.println (" > new wall cell at "+cell.text()+
					//		"can be joined to existing wall at "+trial.text()
					//		+ "which is part of a fragment of size "+fragment.size());

					// if this cell hasn't been merged, do it now
					if (!merged)
					{
						fragment.add(cell);
						merged = true;
						cell.wallContainer=fragment;
						//System.out.println (" >> Cell "+cell.text()+"is now part of "+printFragment(fragment));
					}
					else // if so and there are more walls, combine them
					{
						// if this new add actually touches more than one segment
						// join them by moving cells from fragment to the one containing the cell
						// and throw away 'old' one
						if (merge(fragment,cell.wallContainer))
							wallFragments.remove(fragment);
					}
				}
			}
			
			// if there were no matches, create a new list for this fragment and track it
			if (!merged)
			{
				ArrayList<Cell> list = new ArrayList<Cell>();
				list.add(cell);
				wallFragments.add(list);
				cell.wallContainer=list;
				//System.out.println ("Created new Wall Fragment at "+cell.text());
			}
		}
	}

	// this will modify both lists
	// move all cells from source to target
	private boolean merge(ArrayList<Cell> source, ArrayList<Cell> target)
	{
		boolean didTheMerge=false;
		if (source==target)
		{
			//System.out.println ("Ignoring request to merge two sides of identical wall.");
		}
		else
		{			
			for (Cell sourceCell: source)
			{
				target.add(sourceCell);
				sourceCell.wallContainer=target;
			}
			
			source.clear();
			didTheMerge=true;
		}
		
		return didTheMerge;
	}
	
	public void print ()
	{
		System.out.println ("There are " + wallFragments.size() + " wall fragments.");
		for (ArrayList<Cell> list: wallFragments)
		{
			System.out.print(" > ");
			for (Cell cell:list)
				System.out.print (cell.text());
			System.out.println("");
		}
	}
	
	public int expand(boolean beForceful)
	{
		if (done())
			return 0;
		int cellsChanged = 0;
		ArrayList<Cell> candidates = new ArrayList<Cell>();
		for (Cell wallCell: fullSet)
		{
			ArrayList<Cell> avail = wallCell.availableSurroundingCells(Cell.WALL);
			if (avail.size()==1)
			{
				// must expand wall since it's blocked
				Cell target=avail.get(0);
				if (target.type()==Cell.UNKNOWN)
				{
					//System.out.println ("expand(): add wall expansion candidate from " + wallCell.text()
					//	+ " to " + target.text());
					candidates.add(target);
				}
			}
			else
			{
				//System.out.print(wallCell.text()+"has "+avail.size()+" avail neighbours: ");
				//for (Cell temp:avail)
				//	System.out.print(temp.text());
				//System.out.println("");
			}
		}

		for (Cell target: candidates)
			cellsChanged += target.mark(Cell.WALL,ReasonCode.EXPAND_WALL);

		cellsChanged+=expandFragments(beForceful);
		cellsChanged+=lastWallMerge();
		
		return cellsChanged;
	}


	private int expandFragments(boolean beForceful) 
	{
		int cellsChanged=0;
		
		//System.out.println ("expandFragments(): testing expansion for "+wallFragments.size()+" fragments...");
		
		// look at each fragment, if it has only one way out, it must take it
		ArrayList<Package> trackingList = new ArrayList<Package>();
		boolean doneSpecialNeighbourExpansion=!beForceful;
		for (ArrayList<Cell> fragment: wallFragments)
		{
			//System.out.println ("Fragment: "+printFragment(fragment));
			ArrayList<Package> expansionPoints = getFragmentExpansionPoints(fragment);
			if (expansionPoints.size()==1)
			{
				Package expand=expansionPoints.get(0);
				//System.out.println(" > found one expansion at "+expand.expand.text());
				trackingList.add(expand);
			}
			else
			{	
				//System.out.println(" > cannot simply grow since found "+expansionPoints.size()+" expansion points");
				//for (Package x:expansionPoints)
				//	System.out.print(x.expand.text());
				//System.out.println("");
				
				
				// do this very carefully, so once we find one option for ALL fragments, block off this logic
				if (!doneSpecialNeighbourExpansion)
				{	
					//System.out.println (" > checking special neighbour expansion...");
					ArrayList<Package> rollingList=new ArrayList<Package>();
					for (Package option:expansionPoints)
					{
						Cell trial=option.expand;
						//System.out.println (" > option "+trial.text());
						ArrayList<Cell> neighbours=trial.availableSurroundingCells(Cell.WALL);
						neighbours.remove(option.enclosed);
						for (Cell test:neighbours)
							if (test.type()==Cell.WALL  &&  !rollingList.contains(option))
							{
								rollingList.add(option);
								//System.out.println (" > added "+option.expand.text()+"to rollingList");
							}
					}
					//System.out.println (" > rollingList has size = "+rollingList.size());
					if (rollingList.size()==1)
					{
						//System.out.println (" > found one option to link to another fragment at "
						//		+rollingList.get(0).expand.text());
						trackingList.add(rollingList.get(0));
						doneSpecialNeighbourExpansion=true;
					}
				}			
			}
		}	// for every wall fragment
		
		
		for (Package expand: trackingList)
		{
			cellsChanged += expand.expand.mark(Cell.WALL, ReasonCode.EXPAND_WALL);
			expand.enclosed.setEnclosed(true);
		} // for each expansion cell we found
		
		return cellsChanged;
	}

	private ArrayList<Package> getFragmentExpansionPoints(ArrayList<Cell> fragment)
	{
		return getFragmentExpansionPoints(fragment,Cell.UNKNOWN);
	}
	
	private ArrayList<Package> getFragmentExpansionPoints(ArrayList<Cell> fragment, int matchType)
	{
		// build up the full list of expansion options for this fragment by visiting each cell
		// and checking its neighbours for exit points
		//System.out.println("> fragment "+printFragment(fragment));
		ArrayList<Package> expansionPoints = new ArrayList<Package>();
		for (Cell cell: fragment)
		{
			ArrayList<Cell> neighbours = cell.availableSurroundingCells(matchType);
			//System.out.println (" > cell "+cell.text()+	"avail neighbours are "+printFragment(neighbours));
			for (Cell open:neighbours)
			{
				if (!expansionPoints.contains(open))
					expansionPoints.add(new Package(open,cell));
			}
		}	// for every cell
		return expansionPoints;
	}

	private int lastWallMerge()
	{
		int cellsChanged=0;
		if ( maxSize==currentSize()+1  &&  wallFragments.size()==2 )
		{
			//System.out.println("Last wall merge..");
			// find if there is a cell that would join the last two fragments
			Cell candidate = null;
			boolean located=false;
			ArrayList<ArrayList<Cell>> available = new ArrayList<ArrayList<Cell>>();

			// get the two lists of available expansion cells for each fragment
			for (int i=0;i<2;i++)
			{
				ArrayList<Cell> openlist = new ArrayList<Cell>();
				available.add(openlist);
				//System.out.println(" > fragment " + wallFragments.get(i));
				for (Cell cell: wallFragments.get(i))
				{
					ArrayList<Cell> neighbours = cell.availableSurroundingCells(Cell.UNKNOWN);
					if (neighbours!=null)
					{
						for (Cell open:neighbours)
						{
							//System.out.println ("   > available cell "+open.text());
							openlist.add(open);
						}
					}	
				}
			}
			
			//System.out.println("Available is "+available);
			
			// check intersection
			for (Cell cell:available.get(0))
			{
				if (available.get(1).contains(cell))
				{
					if (located==false)
					{		
						located=true;
						candidate=cell;
						//System.out.println(" > matched "+cell.text());
					}
					else
						located=false;
				}
			}
			
			
			if (located)
				cellsChanged += candidate.mark(Cell.WALL, ReasonCode.JOIN_WALL_FRAGMENTS);
		}
		
		return cellsChanged;
	}

	public String printFragment(ArrayList<Cell> fragment)
	{
		StringBuffer buf = new StringBuffer("[");
		for (Cell cell:fragment)
			buf.append(cell.text());
		buf.append("]");
		return buf.toString();
	}
	
}
