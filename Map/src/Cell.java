import java.util.Random;

/* Cell
 * Made out of 2x2 blocks to help with data management
 * 	 altitude - average altitude of its blocks
 *   biome - biome of the block
 *   border - if the biome is bordering another
 */
public class Cell {
	Block blocks[][] = new Block[2][2];
	float altitude;
	Biome biome;
	
	//Takes a section of 2x2 blocks
	public Cell(Block block1, Block block2, Block block3, Block block4){
		blocks[0][0] = block1;
		blocks[0][1] = block2;
		blocks[1][0] = block3;
		blocks[1][1] = block4;
		
		block1.parent = this;
		block2.parent = this;
		block3.parent = this;
		block4.parent = this;
		
		average_alt();
		biome = new Biome();
	}
	
	//Averages out the altitudes of its blocks
	private void average_alt(){
		float total = blocks[0][0].altitude + blocks[0][1].altitude + blocks[1][0].altitude + blocks[1][1].altitude;
		altitude = total/4;
	}
	
	//turns the cell into an ocean
	public void oceanify(){
		biome = new Biome('o');
		blocks[0][0].ocean = true; 
		blocks[0][1].ocean = true; 
		blocks[1][0].ocean = true; 
		blocks[1][1].ocean = true; 
			
	}
	
	//turns the cell into a lake
	public void lakeify(){
		biome = new Biome('l');
		blocks[0][0].lake = true; 
		blocks[0][1].lake = true; 
		blocks[1][0].lake = true; 
		blocks[1][1].lake = true; 
		
		//Possibly make river false?
	}
	
	//adjust the altitude to a new value, above sea level
	public void adjust_altitude(int alt, int SEA_LEVEL){
		Random random = new Random();
		
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				blocks[i][j].altitude = alt - random.nextInt(2);
				
				if(blocks[i][j].altitude <= SEA_LEVEL){
					blocks[i][j].altitude = SEA_LEVEL + 1;
				}
				
			}
		}
		
		//Re-average the altitude
		average_alt();
	}
	
	//Creates a cell grid out of established blocks
	public static Cell[][] cell_creator(Block map[][]){
		Cell[][] cells = new Cell[map.length/2][map[0].length/2];
		
		for(int i=0;i<map.length;i+=2){
			for(int j=0;j<map[0].length;j+=2){
				cells[i/2][j/2] = new Cell(map[i][j], map[i][j+1], map[i+1][j], map[i+1][j+1]);
			}
		}
		
		return cells;
	}
	
}
