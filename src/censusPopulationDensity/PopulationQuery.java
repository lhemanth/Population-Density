package censusPopulationDensity;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
public class PopulationQuery {
	public static final int TOKENS_PER_LINE  = 7;
	public static final int POPULATION_INDEX = 4; 
	public static final int LATITUDE_INDEX   = 5;
	public static final int LONGITUDE_INDEX  = 6;
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static final int THRESHOLD = 4400;
	public static CensusData parse(String filename) {
		CensusData result = new CensusData();
		try(BufferedReader fileIn = new BufferedReader(new FileReader(filename))) {
            String oneLine = fileIn.readLine();
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
	public static void main(String[] args) throws IOException {
		String fileName = args[0];
		int x = Integer.valueOf(args[1]);
		int y = Integer.valueOf(args[2]);
		String version = args[3];
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
				CensusData censusData = PopulationQuery.parse(fileName);
				Pair<Integer, Float> result = processOutput(rows, columns, version, censusData,"UI");
				return result;
	}
}
