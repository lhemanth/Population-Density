package censusPopulationDensity;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PopulationQuery {
	// next four constants are relevant to parsing
	public static final int TOKENS_PER_LINE  = 7;
	public static final int POPULATION_INDEX = 4; // zero-based indices
	public static final int LATITUDE_INDEX   = 5;
	public static final int LONGITUDE_INDEX  = 6;
	
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
    //As input contains 220,000 data points, we can have each bucket size as 4400 and do 50 parallel executions
	static final int THRESHOLD = 4400;
	
	// parse the input file into a large array held in a CensusData object
	public static CensusData parse(String filename) {
		CensusData result = new CensusData();
		
		try(BufferedReader fileIn = new BufferedReader(new FileReader(filename))) {
            
            // Skip the first line of the file
            // After that each line has 7 comma-separated numbers (see constants above)
            // We want to skip the first 4, the 5th is the population (an int)
            // and the 6th and 7th are latitude and longitude (floats)
            // If the population is 0, then the line has latitude and longitude of +.,-.
            // which cannot be parsed as floats, so that's a special case
            //   (we could fix this, but noisy data is a fact of life, more fun
            //    to process the real data as provided by the government)
            
            String oneLine = fileIn.readLine(); // skip the first line

            // read each subsequent line and add relevant data to a big array
            while ((oneLine = fileIn.readLine()) != null) {
                String[] tokens = oneLine.split(",");
                if(tokens.length != TOKENS_PER_LINE)
                	throw new NumberFormatException();
                int population = Integer.parseInt(tokens[POPULATION_INDEX]);
                if(population != 0)
                	result.add(population,
                			   Float.parseFloat(tokens[LATITUDE_INDEX]),
                		       Float.parseFloat(tokens[LONGITUDE_INDEX]));
            }

            fileIn.close();
        } catch(IOException ioe) {
            System.err.println("Error opening/reading/writing input or output file.");
            System.exit(1);
        } catch(NumberFormatException nfe) {
            System.err.println(nfe.toString());
            System.err.println("Error in file format");
            System.exit(1);
        }
        return result;
	}

	// argument 1: file name for input data: pass this to parse
	// argument 2: number of x-dimension buckets
	// argument 3: number of y-dimension buckets
	// argument 4: -v1, -v2, -v3, -v4, or -v5
	public static void main(String[] args) throws IOException {
		// FOR YOU
		String fileName = args[0];
		int x = Integer.valueOf(args[1]);
		int y = Integer.valueOf(args[2]);
		String version = args[3];
		
		//Parse Census data from file
		CensusData censusData = PopulationQuery.parse(fileName);
		
		processOutput(x, y, version, censusData,"Main");
		
		br.close();
	}

	private static Pair<Integer, Float> processOutput(int x, int y, String version, CensusData censusData, String from) throws IOException {
		Pair<Integer, Float> result = null;
		USRectangle usRectangle = null;
		switch(version){
		case "-v1":
			usRectangle = new SimpleAndSequential(x, y, censusData);
			break;
		case "-v2":
			usRectangle = new SimpleAndParallel(x, y, censusData, THRESHOLD);
			break;
		case "-v3":
			usRectangle = new SmarterAndSequential(x, y, censusData);
			break;
		case "-v4":
			usRectangle = new SmarterAndParallel(x, y, censusData, THRESHOLD);
			break;
		case "-v5":
			usRectangle = new SmarterAndLockBased(x, y, censusData, THRESHOLD);
			break;
		}
		
		long processStartTime;
		long processEndTime;
		int ret;
		processStartTime = System.nanoTime();
		usRectangle.findUSMap();
		processEndTime = System.nanoTime();
		System.out.println("Total Time to create US map rectangle: " + (processEndTime - processStartTime)+"ns");
		do {
			ret = usRectangle.parseInput(br);
			if(ret ==  1) {
				processStartTime = System.nanoTime();
				result = usRectangle.fetchPopulationBasedOnInput();
				processEndTime = System.nanoTime();
				System.out.println("Total Time to fetch population for query: " + (processEndTime - processStartTime)+"ns");
			}
		}while("Main".equals(from) && ret != 0);
		return result;
	}

	public static Pair<Integer, Float> singleInteraction(String fileName, int columns, int rows, String version, int w,
			int s, int e, int n) throws IOException {
		//Parse Census data from file
				CensusData censusData = PopulationQuery.parse(fileName);
				
				Pair<Integer, Float> result = processOutput(rows, columns, version, censusData,"UI");
				
		return result;
	}
}
