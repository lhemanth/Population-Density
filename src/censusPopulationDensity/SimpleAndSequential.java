 package censusPopulationDensity;
public class SimpleAndSequential extends USRectangle{
	public SimpleAndSequential(int length, int breadth, CensusData censusData) {
		this.length = length;
		this.breadth = breadth;
		this.censusData = censusData;
	}
public void findUSMap() {
		int i=0;
		while(i<censusData.data_size) {
			CensusGroup censusGroup = censusData.data[i];
			if(censusGroup != null) {
				Rectangle temp = new Rectangle(censusGroup.longitude, censusGroup.longitude, censusGroup.latitude, censusGroup.latitude);
				if(usRectangle!=null)
					usRectangle = usRectangle.encompass(temp);
				else
					usRectangle = temp;
				overallPopulation += censusGroup.population;
			}
			i++;
		}
	}
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
			if (censusGroup.latitude >= queryBottom &&
                    (censusGroup.latitude < queryTop ||
                            queryTop >= usRectangle.top) && 
                            (censusGroup.longitude < queryRight || 
                                    queryRight >= usRectangle.right) &&  
                            censusGroup.longitude >= queryLeft) {
            		totalPopulation += censusGroup.population;
            }
		}
		return totalPopulation;	
	}
}
