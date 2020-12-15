package censusPopulationDensity;

public class SmarterAndSequential extends USRectangle {
	
	// create a grid of size x*y (use an array of arrays) where each element is an int that will hold the total population for that grid position
	int[][] grid;
	
    public SmarterAndSequential(int length, int breadth, CensusData data) {
		this.length = length;
		this.breadth = breadth;
		this.censusData = data;
        grid = new int[length][breadth];
    }
    
    
	public void findUSMap() {
        if (censusData.data_size == 0)
            return;

        CensusGroup censusGroup = null;
        //Form US Map and calculate overall population
        int i=0;
		while(i<censusData.data_size) {
			//Read Census Group information from Census Data
			censusGroup = censusData.data[i];
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
        
		float rectangleWidth = (usRectangle.right - usRectangle.left) / length;
		float rectangleHeight = (usRectangle.top - usRectangle.bottom) / breadth;
        
        float gridLeft = usRectangle.left;
        float gridBottom = usRectangle.bottom;

        int row, col;

        for (i = 0; i < censusData.data_size; i++) {
            censusGroup = censusData.data[i];
            col = (int) ((censusGroup.latitude - gridBottom) / rectangleHeight);
            if (censusGroup.latitude >= (col + 1) * rectangleHeight + gridBottom)//If it touches the border then make it inclusive
                col++;
            col = (col == breadth ?  breadth - 1: col);//check for edges
            row = (int) ((censusGroup.longitude - gridLeft) / rectangleWidth);
            if (censusGroup.longitude >= (row + 1) * rectangleWidth + gridLeft)//If it touches the border then make it inclusive
                row++;
            row = (row == length ? length - 1 : row);//Check for edges
            grid[row][col] += censusGroup.population;

        }
        
        //Modify the grid, so that grid element g stores the total population in the rectangle whose upper-left is the North-West corner of the country and the lower-right corner is g
        for(int j=grid[0].length-1;j>=0;j--)
		{
			for(i=0;i<grid.length;i++)
			{//add elements to top and left and subtract diagonal element
				grid[i][j]+=((i>0)?grid[i-1][j]:0)+((j<grid[0].length-1)?grid[i][j+1]:0)-((i>0&&j<grid[0].length-1)?grid[i-1][j+1]:0);
			}
		}
  	}
	
	public long calculatePopulationBasedOnInput() {
		long totalPopulation = 0;
		
		for(int i=(int)(userQuery.left-1);i<(int)(userQuery.right);i++)
		{
			for(int j=(int)(userQuery.bottom-1);j<(int)(userQuery.top);j++)
			{//subtract elements to top and left and add diagonal element
				totalPopulation+=grid[i][j]+((i>0&&j<breadth-1)?grid[i-1][j+1]:0)-((i>0)?grid[i-1][j]:0)-((j<breadth-1)?grid[i][j+1]:0);
			}
		}		
		return totalPopulation;
	}
}
