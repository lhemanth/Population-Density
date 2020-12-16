package censusPopulationDensity;
import java.io.BufferedReader;
import java.io.IOException;
public interface InputQuery {
	int parseInput(BufferedReader br) throws IOException;
	void findUSMap();
	Pair<Integer,Float> fetchPopulationBasedOnInput();
	long calculatePopulationBasedOnInput();
}
