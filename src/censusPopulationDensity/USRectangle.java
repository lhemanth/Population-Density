package censusPopulationDensity;
import java.io.BufferedReader;
import java.io.IOException;
public abstract class USRectangle implements InputQuery {
	int length,breadth;
	CensusData censusData;
	float left, bottom, right, top;
	Rectangle usRectangle, userQuery;
	long overallPopulation;
	public int parseInput(BufferedReader br) throws IOException {
		System.out.println("Please give west, south, east, north coordinates of your query rectangle: ");
		try {
			String s[]=br.readLine().split(" ");
			left = Integer.parseInt(s[0]);
			if(left < 1 || left > length) {
				System.out.println("Western coordinates should be between 1 and "+length);
				return 0;
			}

			bottom = Integer.parseInt(s[1]);
			if(bottom < 1 || bottom > breadth) {
				System.out.println("Southern coordinates should be between 1 and "+breadth);
				return 0;
			}

			right = Integer.parseInt(s[2]);
			if(right < left || right > length) {
				System.out.println("Eastern coordinates should be between "+left+" and "+length);
				return 0;
			}

			top = Integer.parseInt(s[3]);
			if(top < bottom || top > breadth) {
				System.out.println("Northern coordinates should be between "+bottom+" and "+breadth);
				return 0;
			}
			userQuery = new Rectangle(left, right, top, bottom);
		} catch (Exception e) {
			System.out.println("Exiting as only numeric values are accepted for the coordinates");
			return 0;
		}
		return 1;
	}
public abstract void findUSMap();
public Pair<Integer,Float> fetchPopulationBasedOnInput() {
		long popInArea = calculatePopulationBasedOnInput();
		System.out.println("population of rectangle: " + popInArea);
		float percent = (float)(popInArea * 100)/overallPopulation;
		System.out.printf("percent of total population: %.2f \n",percent);
		Pair<Integer,Float> pair = new Pair<Integer,Float>((int)popInArea, percent);
		return pair;
	}

	public abstract long calculatePopulationBasedOnInput();
	
}
