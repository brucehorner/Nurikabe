package solver;

public class ReasonCode
{
	public int code;
	public String text;
	
	public ReasonCode (int code, String text)
	{
		this.code=code;
		this.text=text;
	}
	
	public static ReasonCode UNKNOWN = new ReasonCode (0,"UNKNOWN");
	public static ReasonCode ROOM_OF_1 = new ReasonCode (101,"Surrounding an initial room of size one");
	public static ReasonCode SEPARATE_ADJACENT_ROOMS = new ReasonCode (102, "Separate adjacent rooms");
	public static ReasonCode EXPAND_ROOM = new ReasonCode (103, "Expanding room");
	public static ReasonCode EXPAND_WALL = new ReasonCode (104, "Expanding wall");
	public static ReasonCode AVOID_BLOCK_OF_4 = new ReasonCode (105, "Avoid block of 4");
	public static ReasonCode UNREACHABLE = new ReasonCode (106, "Unreachable");
	public static ReasonCode UNKNOWN_NEIGHBOUR = new ReasonCode (107, "Unknown neighbour");
	public static ReasonCode INITIAL_ROOM = new ReasonCode (108, "Initial room");
	public static ReasonCode EXPAND_ROOM_ENCLOSE = new ReasonCode(109,"Enclosing an expanded room");
	public static ReasonCode JOIN_WALL_FRAGMENTS = new ReasonCode (110, "Joined two wall fragments");
	public static ReasonCode EXPAND_ORPHAN = new ReasonCode (111, "Expanding orphan room cell");			
	public static ReasonCode TRIAL = new ReasonCode(112, "Exploring options");
}
