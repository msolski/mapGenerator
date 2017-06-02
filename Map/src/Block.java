/* Block
 * Smallest portion of data on the map
 * 	 altitude - height above sea level
 * 	 lake - if the block is part of a lake
 *   ocean - if the block is part of an ocean
 * 	 river - if there is a river in the block
 * 	 city - the city on the block, if any
 */
public class Block {
	int altitude;
	boolean lake;
	boolean ocean;
	boolean river;
	boolean border;
	Cell parent;
	City city;
	//topography map for details
	
	public Block(){
		this.altitude = 0;
		this.river = false;	
		this.ocean = false;
		this.river = false;
		border = false;
	}
	
	//Identifiers
	public boolean isLake(){
		return lake;
	}
	
	public boolean isOcean(){
		return ocean;
	}
	
	public boolean hasRiver(){
		return river;
	}
	
	public boolean hasCity(){
		return city != null;
	}
}
