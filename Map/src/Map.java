//Generally for testing for now
public class Map {
	
	/* WHAT I FINISHED RECENTLY: Started city generation
	 * WHAT I WANT TO DO: Finish planning out city generation, since I'm winging it right now
	 *                    Figure out the borders of biomes
	 *                    Assign names to biomes
	 *                    Make it so that cold biomes don't go too far south/vice versa
	 */
	public static void main(String[] args){
		//These numbers must be divisible by 4
		//Smallest seems to be 12 and biggest seems to be 28
		//But 20*24 seems to be a good size
		Block[][] map = new Block[20][24];
		Cell[][] cells;
		
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				map[i][j] = new Block();
			}
		}
		
		cells = Generator.generate(map);
		
		view_altitude(map);
		test_view(map);
		view_biome(cells);
		view_intensity(cells);
	}
	
	private static void view_altitude(Block map[][]){
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				System.out.format("%3d", map[i][j].altitude);
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	private static void view_biome(Cell map[][]){
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				System.out.format("%3c", map[i][j].biome.type);
			}
			System.out.println("");
		}
		
		System.out.println("");
	}
	
	private static void view_intensity(Cell map[][]){
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				System.out.format("%3d", map[i][j].biome.intensity);
			}
			System.out.println("");
		}
	}
	
	private static void test_view(Block map[][]){
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				
				//Cities
				if(map[i][j].hasCity()){
					if(map[i][j].city.type == 'c') System.out.format("@ ");
					else if (map[i][j].city.type == 't') System.out.format("* ");
					else if (map[i][j].city.type == 'v') System.out.format(". "); //maybe not this one
					continue;
				}
				
				//Mountains
				if (map[i][j].altitude > Generator.PEAK_LEVEL){ 
					System.out.format("n ");
					continue;
				} else if (map[i][j].altitude >= Generator.MONT_LEVEL){
					System.out.format("^ ");
					continue;
				}
				
				//Water
				if(map[i][j].isLake()){
					System.out.format("= ");
					continue;
				}
				
				if(map[i][j].hasRiver()){
					System.out.format("~ ");
					continue;
				}
				
				
				else if(map[i][j].isOcean()){
					System.out.format("w ");
					continue;
				}
				
				//Biome
				if(map[i][j].parent.biome.type == 't'){
					System.out.format("- ");
					continue;
				} else if(map[i][j].parent.biome.type == 'd'){
					System.out.format(". ");
					continue;
				} else if(map[i][j].parent.biome.type == 's'){
					System.out.format("# ");
					continue;
				} else if(map[i][j].parent.biome.type == 'w'){
					System.out.format("t ");
					continue;
				} else if(map[i][j].parent.biome.type == 'b'){
					System.out.format("l ");
					continue;
				} else if(map[i][j].parent.biome.type == 'r'){
					System.out.format("T ");
					continue;
				}
				
				
				//Other
				if (map[i][j].altitude <= Generator.SEA_LEVEL) System.out.format("w ");
				else System.out.format("  ");
			}
			System.out.println("");
		}
		System.out.println("");
	}
}
