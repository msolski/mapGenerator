import java.util.Random;
import java.lang.Math;

//All this stuff doesn't need to be static, but too lazy to change
public class Generator {
	
	private final static int MAX = 20; //Maximum altitude
	private final static int MIN = 1; //Minimum altitude
	public final static int SEA_LEVEL = 1; //Altitude of sea level;
	public final static int MONT_LEVEL = 15; //Altitude of which blocks are hills;
	public final static int PEAK_LEVEL = (MAX - MONT_LEVEL)/2 + MONT_LEVEL; //Altitude of which blocks are mountains
	
	/* Dictates which edge contains the ocean border
	 * -1 = undefined
	 * 0 = bottom
	 * 1 = right
	 */
	// ^ Can probably use enum for this, but whatever
	//Maybe even boolean
	private static int ocean_border = -1;
	
	public static Cell[][] generate(Block map[][]){
		
		System.out.println("Generating altitudes...");
		Generator.gen_altitude(map);
		
		Cell[][] cells = Cell.cell_creator(map);
		
		System.out.println("Assigning oceans and lakes...");
		Generator.gen_lakes_oceans(cells);
		
		System.out.println("Assigning mountain range...");
		Generator.gen_mountains(cells);
		
		System.out.println("Creating rivers...");
		//It's nice to have 2 rivers
		Generator.gen_rivers(map);
		Generator.gen_rivers(map);
		
		System.out.println("Assigning biomes...");
		Generator.gen_biomes(cells);
		
		System.out.println("Done!");
		return cells;
	}
	//==Cities=============================================================================================================================
	/* Generate Cities
	 * Assigns cities to nodes
	 * Rules:
	 *   Cities (castle towns) are on rivers, rarely on lakes, not anywhere else
	 *   Towns are on lakes, rarely on rivers, not anywhere else
	 *   Villages (hamlets) are anywhere else
	 *   Settlements less likely to be in extreme biomes
	 *   Settlements can't be on water
	 */
	public static void gen_cities(Block map[][]){
		
	}
	
	//==Biomes=============================================================================================================================
	/* Generate Mountains
	 * Assigns mountain biomes
	 * Rules:
	 *   Uses peak_level for the high mountains
	 *   Uses mont_level for the rest of the range
	 *   Altitude determines the hieght of the mountains, and therefore the biome's intensity
	 */
	public static void gen_mountains(Cell map[][]){
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				if((int) map[i][j].altitude >= PEAK_LEVEL){
					map[i][j].biome = new Biome('m', 3);
				} else if((int) map[i][j].altitude < PEAK_LEVEL && (int) map[i][j].altitude > MONT_LEVEL) {
					map[i][j].biome = new Biome('m', 2);
				} else if((int) map[i][j].altitude == MONT_LEVEL && (int) map[i][j].altitude > MONT_LEVEL-2){
					map[i][j].biome = new Biome('m', 1);
				}
			}
		}
	}
	
	/* Generate Biomes
	 * Assigns the rest of the biomes
	 * Biomes:
	 *   b - boreal forest
	 *   d - desert
	 *   p - plains
	 *   r - rainforest
	 *   s - swamp
	 *   t - tundra
	 *   w - woods
	 * Rules:
	 *   whew lad
	 */
	public static void gen_biomes(Cell map[][]){
		//Only allowing one of each extreme biome
		boolean has_tundra = false, has_rainforest = false;
		int cold_choice, temp_choice, hot_choice;
		Random random = new Random();
		
		//Time to assign the middle of each biome
		//The middle of each biome is the peak of its intensity
		int counter = random.nextInt(map.length*2); //A biome will get placed every time this counter reaches 0
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				counter--;
				if(counter < 1 && !(map[i][j].biome.isIdentified())){
					//Cold biomes
					if(i<=map.length/4){
						//Cold biomes are harsh: tundra and boreal forest
						
						cold_choice = random.nextInt(3) + 1;
						
						if(cold_choice == 1 && !has_tundra){
							//Tundra!
							map[i][j].biome = new Biome('t',3);
							has_tundra = true;
						} else if (cold_choice ==2){
							//Snore-real forest
							map[i][j].biome = new Biome('b',3);
						} else {
							//Plains
							map[i][j].biome = new Biome('p',3);
						}
					}
					//Temperate biomes
					else if(i>map.length/4 && i<(map.length*3)/4){
						//Temperate biomes are moderate, like woods and plains
						//Low chance of rainforests and swamps
						temp_choice = random.nextInt(9) + 1;
						
						if(temp_choice == 2 && !has_rainforest){
							//Rainforest!
							map[i][j].biome = new Biome('r', 3);
							has_rainforest = true;
						} else if (temp_choice == 3){
							//Swamp
							map[i][j].biome = new Biome('s', 3);
						} else if (temp_choice > 3 && temp_choice < 7){
							//Woods
							map[i][j].biome = new Biome('w', 3);
						} else {
							//Plains
							map[i][j].biome = new Biome('p', 3);
						} 
					}
					//Hot biomes
					else if(i>=(map.length*3)/4){
						//Hot biomes have a higher chance of rainforests and deserts
						hot_choice = random.nextInt(11) + 1;
						
						if((hot_choice == 2 || hot_choice == 3) && !has_rainforest){
							//Rainforest!
							map[i][j].biome = new Biome('r', 3);
							has_rainforest = true;
						} else if (hot_choice == 4 || hot_choice == 5){
							//woods!
							map[i][j].biome = new Biome('w', 3);
						} else if(hot_choice > 5 && hot_choice < 9){
							//Desert!
							map[i][j].biome = new Biome('d', 3);
						} else if(hot_choice == 10){
							//Swamp
							map[i][j].biome = new Biome('s', 3);
						} else {
							//Plains
							map[i][j].biome = new Biome('p', 3);
						}
						
					}
					
					counter = random.nextInt(map.length-(map.length/2));
				}
			}
		}
		
		//Now that we have the core of each biome assigned, we can assign the surrounding areas
		//Keep going like this until every cell has a biome assigned to it
		//Edges be damned for now I guess
		int i = 0, j = 0, intensity, choice;
		boolean done = false;
		//Loop through the map looking for cells that have biomes
		//If a cell has a biome, pick a cell around it to give another part of the biome
		//Cells with an intensity of 1 have a lower chance of having a surrounding cell be affect
		while(!done){
			//If the cell has a biome that isn't a lake, ocean, or mountain
			if(map[i][j].biome.isIdentified() && !map[i][j].biome.isLake() && !map[i][j].biome.isOcean() && !(map[i][j].biome.type=='m')){
				
				//If the cell is at the top
				if(i==0){
					//Top left
					if(j==0){
						//Only 2 choices
						if(random.nextBoolean()){
							//To the right
							if(!map[i][j+1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else {
							//Below
							if(!map[i+1][j].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						}
					//Top right
					} else if(j == map[0].length-1){
						//Only 2 choices again
						if(random.nextBoolean()){
							//To the left
							if(!map[i][j-1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else {
							//Below
							if(!map[i+1][j].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						}
						
					//Somewhere between top right and top left
					} else {
						//This time, 3 choices
						choice = random.nextInt(3) + 1;
						
						if(choice == 1){
							//To the left
							if(!map[i][j-1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else if(choice == 2) {
							//Below
							if(!map[i+1][j].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else if(choice == 3){
							//To the right
							if(!map[i][j+1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						}
					}
				}
				//End of that bullshit
				
				//If the cell is at the bottom
				else if(i == map.length-1){
					//Bottom left
					if(j==0){
						//Only 2 choices
						if(random.nextBoolean()){
							//To the right
							if(!map[i][j+1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else {
							//Above
							if(!map[i-1][j].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						}
					//Bottom right
					} else if(j == map[0].length-1){
						//Only 2 choices again
						if(random.nextBoolean()){
							//To the left
							if(!map[i][j-1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else {
							//Above
							if(!map[i-1][j].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						}
						
					//Somewhere between bottom right and bottom left
					} else {
						//This time, 3 choices
						choice = random.nextInt(3) + 1;
						if(choice == 1){
							//To the left
							if(!map[i][j-1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else if(choice == 2) {
							//Above
							if(!map[i-1][j].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						} else if(choice == 3){
							//To the right
							if(!map[i][j+1].biome.isIdentified()){
								intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
								if(intensity > 0){
									map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
								} else {
									intensity = 1;
									if(random.nextBoolean()){
										map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
									}
								}
							}
						}
					}
				}
				//Whew, maybe should have picked another method
				//Now we've gotten corners and top and bottom edges out of the way
				
				//What if the cell is on the left edge? (excluding corners)
				if(j == 0 && i != 0 && i != map.length-1){
					//We have 3 choices
					choice = random.nextInt(3) + 1;
					
					if(choice == 1){
						//Above
						if(!map[i-1][j].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 2){
						//Below
						if(!map[i+1][j].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 3){
						//To the right
						if(!map[i][j+1].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					}
				}
				//What if the cell is on the right edge? (excluding corners)
				else if(j == map[0].length-1 && i != 0 && i != map.length-1){
					//We have 3 choices
					choice = random.nextInt(3) + 1;
					
					if(choice == 1){
						//Above
						if(!map[i-1][j].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 2){
						//Below
						if(!map[i+1][j].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 3){
						//To the left
						if(!map[i][j-1].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					}
				}
				//That one wasn't so bad
				
				//So now what if its anywhere not on the edges?
				if(i<map.length-1 && i>0 && j<map[0].length-1 && j>0){
					//Now we have 4 choices
					choice = random.nextInt(4) + 1;
					
					if(choice == 1){
						//Above
						if(!map[i-1][j].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i-1][j].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 2){
						//Below
						if(!map[i+1][j].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i+1][j].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 3){
						//To the left
						if(!map[i][j-1].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i][j-1].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					} else if(choice == 4){
						//To the right
						if(!map[i][j+1].biome.isIdentified()){
							intensity = map[i][j].biome.intensity - random.nextInt(2)-1;
							if(intensity > 0){
								map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
							} else {
								intensity = 1;
								if(random.nextBoolean()){
									map[i][j+1].biome = new Biome(map[i][j].biome.type, intensity);
								}
							}
						}
					}
				}
				//I think that's it
				
			//This is the bottom of the "if cell has a biome" statement
			}
			
			//Increment the array counters
			j++;
			if(j == map[0].length){
				i++;
				j = 0;
			}
			
			if(i == map.length){
				i = 0;
				j = 0;
			}
			
			//Check to see if every cell has a biome
			done = true;
			for(int k=0;k<map.length;k++){
				for(int l=0;l<map[0].length;l++){
					if(!map[k][l].biome.isIdentified()){
						done = false;
					}
				}
			}	
		}
	}
	
	//==Lakes=============================================================================================================================	
	/* Generate Lakes and Oceans
	 * Takes the cells of 4*1 non-ocean blocks and turns them into lakes or oceans
	 * Having random sea-level cells fucks with generation
	 * Rules:
	 *   If a 4*1 cell is surrounded by 3 or 4 cells of land, raise the altitude and turn it into a lake
	 *   If a 4*1 cell is surrounded by 2 or less cells of land, assign ocean biome
	 *   If a 4*1 cell is touching another ocean cell, assign ocean biome
	 * Parameters:
	 *   map - cell grid
	 */
	public static void gen_lakes_oceans(Cell map[][]){
		//To start out, we need some ocean cells to help determine what other cells are part of the ocean
		//According to the rules, some ocean cells might not be considered ocean cells
		
		//Since we've defined which edge has the ocean border, let's turn all the cells on that edge into ocean cells
		if(ocean_border == 0){
			//Ocean border is on the bottom
			for(int i=0;i<map[0].length;i++) map[map.length-1][i].oceanify();
		} else if(ocean_border == 1){
			//Ocean border is on the right
			for(int i=0;i<map.length;i++) map[i][map[0].length-1].oceanify();
		}
		
		//Now that we have some ocean reference, we can GET RIGHT INTO THE NEWS
		//For this, we're going to go in reverse
		//It's guaranteed that the bottom right cell is an ocean cell
		//We have to leave the edges out of the loop so checking words
		//We'll deal with those after the loop I guess
		//This way, everything will get a correct classification, hopefully
		for(int i=map.length-2;i>0;i--){
			for(int j=map[0].length-2;j>0;j--){
				
				//If the cell has already been classified, move on
				if(map[i][j].biome.isLake() || map[i][j].biome.isOcean()) continue;
				
				//The cell is below sea level
				if(map[i][j].altitude == SEA_LEVEL){
				
					//Cell to the right, above, to the left, or below an ocean?
					if(map[i-1][j].biome.isOcean() || map[i][j-1].biome.isOcean() || map[i+1][j].biome.isOcean() || map[i][j+1].biome.isOcean()){
						map[i][j].oceanify();
						continue;
					}
					
					//If not, let's count the number of land cells around it
					int land_count = 0;
					if(map[i-1][j].altitude > SEA_LEVEL) land_count++;
					if(map[i][j-1].altitude > SEA_LEVEL) land_count++;
					if(map[i+1][j].altitude > SEA_LEVEL) land_count++;
					if(map[i][j+1].altitude > SEA_LEVEL) land_count++;
					
					if(land_count > 2){
						//It's surrounded by too much land. Must be a lake.
						map[i][j].lakeify();
						
						//Make the altitude closer to its neighbours
						map[i][j].adjust_altitude((int) map[i-1][j-1].altitude, SEA_LEVEL);
					}
					
					//Testing shows that we don't have to do the whole land_count > 2 thing
				}
			}
		}
		
		//Now time to focus on the edges
		//Bottom-left to top-left
		for(int i=map.length-2;i>-1;i--){
			if(map[i][0].altitude == SEA_LEVEL){
				//Is the cell under it an ocean?
				if(map[i+1][0].biome.isOcean()){
					map[i][0].oceanify();
					continue;
				} else {
					map[i][0].lakeify();
					map[i][0].adjust_altitude((int) map[i][1].altitude, SEA_LEVEL);
				}
			}
		}
		
		//Bottom-right to top-right
		for(int i=map.length-2;i>-1;i--){
			if(map[i][map[0].length-1].altitude == SEA_LEVEL){
				//Is the cell under it an ocean?
				if(map[i+1][map[0].length-1].biome.isOcean()){
					map[i][map[0].length-1].oceanify();
					continue;
				} else {
					map[i][map[0].length-1].lakeify();
					map[i][map[0].length-1].adjust_altitude((int) map[i][map[0].length-2].altitude, SEA_LEVEL);
				}
			}
		}
		
		//Top-right to top-left
		for(int i=map[0].length-2;i>-1;i--){
			if(map[0][i].altitude == SEA_LEVEL){
				//Is the cell under it an ocean?
				if(map[0][i+1].biome.isOcean()){
					map[0][i].oceanify();
					continue;
				} else {
					map[0][i].lakeify();
					map[0][i].adjust_altitude((int) map[1][i].altitude, SEA_LEVEL);
				}
			}
		}
		
		//Bottom-right to bottom-left
		for(int i=map[0].length-2;i>-1;i--){
			if(map[map.length-1][i].altitude == SEA_LEVEL){
				//Is the cell under it an ocean?
				if(map[map.length-1][i+1].biome.isOcean()){
					map[map.length-1][i].oceanify();
					continue;
				} else {
					map[map.length-1][i].lakeify();
					map[map.length-1][i].adjust_altitude((int) map[i][map.length-2].altitude, SEA_LEVEL);
				}
			}
		}					
	}
	
	//==Rivers=============================================================================================================================	
	/* Generate Rivers
	 * Generates the info grid for the rivers in the map
	 * Rules:
	 *   River info grid is binary
	 *   A river starts at the highest point
	 *   The river then extends to the lowest block adjacent to it
	 *   The river ends at the edge of the map, sea level, or when every other adjacent block is higher altitude
	 * Parameters:
	 *   map - block grid
	 */
	public static void gen_rivers(Block map[][]){
		//Find the highest point
		int[] origin = {0,0};
		int highest = map[0][0].altitude, lowest = map[0][0].altitude;
		int i, j;
		
		for(i=0;i<map.length;i++){
			for(j=0;j<map[0].length;j++){
				if(map[i][j].altitude > highest) {
					highest = map[i][j].altitude;
					origin[0] = i;
					origin[1] = j;
				}
			}
		}
		
		//Spice the origin up a bit with some randomness
		Random random = new Random();
		
		if(random.nextInt(2) + 1 > 2){
			origin[0] = origin[0] + random.nextInt(2) + 1;
			origin[1] = origin[1] + random.nextInt(2) + 1;
		} else {
			origin[0] = origin[0] - random.nextInt(2) + 1;
			origin[1] = origin[1] - random.nextInt(2) + 1;
		}
		
		//Start the river
		map[origin[0]][origin[1]].river = true;
		
		//Extend the river to lower blocks
		i = origin[0]; j = origin[1];
		while(map[i][j].altitude != SEA_LEVEL && i < map.length-1 && i > -1 && j < map[0].length-1 && j > -1){
			
			//If the limits have been met, end
			if(i==0 || j==0){
				break;
			}
			
			//Find the lowest adjacent block
			lowest = map[i][j].altitude;
			for(int x=0;x<4;x++){
				if(map[i+1][j].altitude < lowest){
					lowest = map[i+1][j].altitude;
					origin[0] = i+1;
					origin[1] = j;
				} else if(map[i-1][j].altitude < lowest){
					lowest = map[i-1][j].altitude;
					origin[0] = i-1;
					origin[1] = j;
				} else if(map[i][j+1].altitude < lowest){
					lowest = map[i][j+1].altitude;
					origin[0] = i;
					origin[1] = j+1;
				} else if(map[i][j-1].altitude < lowest){
					lowest = map[i][j-1].altitude;
					origin[0] = i;
					origin[1] = j-1;
				}
			}
			
			//if the next blocks are equal, force right or down, depending on altitude
			if(map[i+1][j].altitude != 1 && map[i][j+1].altitude != 1 && lowest == map[i][j].altitude || map[origin[0]][origin[1]].river){
				if(map[i+1][j].altitude < map[i][j+1].altitude){
					origin[0] = i+1;
					//origin[1] = j;
				} else {
					//origin[0] = i;
					origin[1] = j+1;
				}
			}
			
			
			//Assign the next block the river status
			map[origin[0]][origin[1]].river = true;
			
			i = origin[0];
			j = origin[1];
		}
		
		//TODO: If too little riverage, run the generation again
		
	}
	
	//==Altitude=============================================================================================================================
	//***OLD FUNCTION***
	/* Generate altitude
	 * Generates the info grid for the altitude level of each block
	 * Rules:
	 *   A block is picked as the highest point of altitude
	 *   The further the block is from the altitude, the lower it will be
	 * Parameters:
	 *   map - block grid
	 */
	public static void gen_altitude(Block[][] map){
		Random random = new Random();
		
		//Generate the highest point on the map
		final int[] origin = {0,0};
		origin[0] = random.nextInt(10);
		origin[1] = random.nextInt(10);
		
		int distance;
		int value;
		
		map[origin[0]][origin[1]].altitude = MAX;
		
		//Generate dirty altitude
		for(int x=0;x<map.length;x++){
			for(int y=0;y<map[0].length;y++){
				distance = Math.abs(x-origin[0]) + Math.abs(y-origin[1]);
						
				value = MAX - distance * (random.nextInt(2)+1) - 1; //Generate altitude
						
				//Bias against water if not at edge, create flatlands
				if(distance > MAX/2 && distance < (MAX - MAX/4) && value < 4){
					value = random.nextInt(2)+2;
				}else if (distance > (MAX - MAX/4) && distance < (MAX - MAX/10) && value < 3){
					value = random.nextInt(2)+1;
				}
						
				if(value <= MIN){
					value = MIN;
				}
				map[x][y].altitude = value;
			}
		}
		
		//clean up some water/prime for biomes with cells
		clean_altitude(map);
		
		//Count how much water. If too much or too little, re-generate
		int water_count = 0;
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map[0].length;j++){
				if(map[i][j].altitude <= SEA_LEVEL) water_count++;
			}
		}
		
		try{
			//Over a third of map being water is unacceptable 
			if(water_count > (map.length*map[0].length)/3) gen_altitude(map);
			//If less than one fifth of the map is water, re-gen
			if(water_count < (map.length*map[0].length)/5) gen_altitude(map);
		} catch(StackOverflowError e){
			water_border(map);
		}
		//Border edges with water
		water_border(map);
	}
	
	//***OLD FUNCTION***
	/* Clean altitude
	 * Makes a basis for biomes by making special altitudes cleaner using large blocks (2x2 of blocks)
	 * Rules:
	 *   A block that is beside blocks with a much lower value, its value must be lowered
	 *      -> Average variance of -2 to 2 (or similar) must be kept
	 *   If a large block is mostly sea level, the entire large block will become sea level
	 *   Vice versa for land
	 *   If a land large block is adjacent to 2 water large blocks, it becomes half land, half water
	 * Parameters:
	 *   alt - info grid
	 */
	private static void clean_altitude(Block map[][]){
		int i, j;
		int variance;
		Random random = new Random();
		//*Fixing altitude variances loop*
		//Stop before bounds so no checks have to be made
		for(i=1;i<map.length-1;i++){
			for(j=1;j<map[0].length-1;j++){
				variance = 0;
				
				//Add up the difference among surrounding blocks
				variance += map[i][j].altitude - map[i-1][j].altitude;
				variance += map[i][j].altitude - map[i+1][j].altitude;
				variance += map[i][j].altitude - map[i][j-1].altitude;
				variance += map[i][j].altitude - map[i][j+1].altitude;
				variance /= 4; //Get the average
				variance = Math.abs(variance);
				
				if(variance > 2){
					map[i][j].altitude -= variance;
				}
			}
		}
		
		//*Fixing sea level loop*
		//Stop before bounds so no checks have to be made
		//The bottom corner will probably be water anyways
		for(i=0;i<map.length-1;i += 2){
			for(j=0;j<map[0].length-1;j +=2){
				
				//How much of the large block is water?
				int water_count = 0;
				
				if(map[i][j].altitude <= SEA_LEVEL) water_count++;
				if(map[i+1][j].altitude <= SEA_LEVEL) water_count++;
				if(map[i][j+1].altitude <= SEA_LEVEL) water_count++;
				if(map[i+1][j+1].altitude <= SEA_LEVEL) water_count++;
				
				//If there is no water in the large block, donchu worry bout it
				if(water_count == 0) continue;
				
				//If there is not a lot of water, raise altitude
				if(water_count <= 2) {
					map[i][j].altitude = SEA_LEVEL + (random.nextInt(1)+1); //Is this random?
					map[i+1][j].altitude = SEA_LEVEL + (random.nextInt(1)+1); //Is this random?
					map[i][j+1].altitude = SEA_LEVEL + (random.nextInt(1)+1); //Is this random?
					map[i+1][j+1].altitude = SEA_LEVEL + (random.nextInt(1)+1); //Is this random?
				}
				
				//If there is not a lot of land, lower altitude
				if(water_count > 2){
					map[i][j].altitude = SEA_LEVEL;
					map[i+1][j].altitude = SEA_LEVEL;
					map[i][j+1].altitude = SEA_LEVEL;
					map[i+1][j+1].altitude = SEA_LEVEL;
				}
			}
		}
	}
	
	//***OLD FUNCTION***
	/* Water Border
	 * Finds the edge of the map covered in water and makes it a full watery edge
	 * Uses large blocks
	 * Parameters:
	 *   map - block grid
	 */
	private static void water_border(Block map[][]){
		//Check to see which edge has the water border
		int bottom_count = 0;
		int right_count = 0;
		int i, j;
		
		for(i=0; i<map.length; i++){
			for(j=0; j<map[0].length; j++){
				
				//bottom?
				if(i+1 == map.length){
					if(map[i][j].altitude == SEA_LEVEL) bottom_count++;
				}
				//right?
				if(j+1 == map[0].length){
					if(map[i][j].altitude == SEA_LEVEL) right_count++;
				}
			}
		}
		
		//If right is to be the border
		if(right_count > bottom_count){
			ocean_border = 1;
			//Fill out the bottom
			j = map[0].length-2;
			for(i=0; i<map.length; i++){
				map[i][j].altitude = SEA_LEVEL;
				map[i][j+1].altitude = SEA_LEVEL;
			}
			
		//If bottom is to be the border
		} else {
			ocean_border = 0;
			i = map.length-2;
			for(j=0; j<map[0].length; j++){
				map[i][j].altitude = SEA_LEVEL;
				map[i+1][j].altitude = SEA_LEVEL;
			}
			
		}
	}
}
