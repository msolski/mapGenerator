/* Biome
 *  b - boreal forest
 *  d - desert
 *  l - lake
 *  m - mountain
 *  o - ocean
 *  p - plains
 *  r - rainforest
 *  s - swamp
 *  t - tundra
 *  w - woods
 */
public class Biome {
	char type;
	int intensity;
	String name;
	
	public Biome(){
		this.type = 'u';
		intensity = 0;
	}
	
	public Biome(char type){
		this.type = type;
		intensity = 1;
		name = "unnamed";
	}
	
	public Biome(char type, int intensity){
		this.type = type;
		this.intensity = intensity;
		name = "unnamed";
	}
	
	//Identifiers
	public boolean isOcean(){
		return type == 'o';
	}
	public boolean isLake(){
		return type == 'l';
	}
	public boolean isIdentified(){
		return !(type == 'u');
	}

}
