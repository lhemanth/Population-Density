 package censusPopulationDensity;
public class SimpleAndSequential extends USRectangle{
	
	public SimpleAndSequential(int length, int breadth, CensusData censusData) {
		this.length = length;
		this.breadth = breadth;
		this.censusData = censusData;
	}
	//process the data to find the four corners of the U.S. rectangle using a sequential O(n) algorithm where n is the number of census-block-groups.
	public void findUSMap() {
		int i=0;
		while(i<censusData.data_size) {
			//Read Census Group information from Census Data
			CensusGroup censusGroup = censusData.data[i];
			if(censusGroup != null) {
				Rectangle temp = new Rectangle(censusGroup.longitude, censusGroup.longitude, censusGroup.latitude, censusGroup.latitude);
				if(usRectangle!=null)
					usRectangle = usRectangle.encompass(temp);//Update west, south, north and east coordinates based on new census group
				else
					usRectangle = temp;//Add first Rectangle data
				overallPopulation += censusGroup.population;
			}
			i++;
		}
	}
	
	//for each query do another sequential O(n) traversal to answer the query (determining for each census-block-group whether or not it is 
	//in the query rectangle)
	public long calculatePopulationBasedOnInput() {
		long totalPopulation = 0;
		
		float rectangleWidth = (usRectangle.right - usRectangle.left) / length;
		float rectangleHeight = (usRectangle.top - usRectangle.bottom) / breadth;
		
		float queryLeft = (usRectangle.left + (userQuery.left - 1) * (rectangleWidth));
		float queryRight = (usRectangle.left + (userQuery.right) * (rectangleWidth));
		float queryTop = (usRectangle.bottom + (userQuery.top) * (rectangleHeight));
		float queryBottom = (usRectangle.bottom + (userQuery.bottom - 1) * (rectangleHeight));
		
		for (int i = 0; i < censusData.data_size; i++) {
			CensusGroup censusGroup = censusData.data[i];
			
            // For each census group, see if latitude and longitude falls in above query ranges, and if yes, then add to the population 
            if (censusGroup.latitude >= queryBottom &&
                    (censusGroup.latitude < queryTop ||
                            queryTop >= usRectangle.top) && //In the unlikely case that a census-block-group falls exactly on the border
                            (censusGroup.longitude < queryRight || //of more than one grid position, tie-break by assigning it to the North and/or East.
                                    queryRight >= usRectangle.right) &&  
                            censusGroup.longitude >= queryLeft) {
            		totalPopulation += censusGroup.population;
            }
		}
		return totalPopulation;	
	}
}
