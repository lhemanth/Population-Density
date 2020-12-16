package censusPopulationDensity;

public class SmarterAndSequential extends USRectangle {
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
        int i=0;
		while(i<censusData.data_size) {
			censusGroup = censusData.data[i];
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
        float rectangleWidth = (usRectangle.right - usRectangle.left) / length;
		float rectangleHeight = (usRectangle.top - usRectangle.bottom) / breadth;
        float gridLeft = usRectangle.left;
        float gridBottom = usRectangle.bottom;
		int row, col;

        for (i = 0; i < censusData.data_size; i++) {
            censusGroup = censusData.data[i];
            col = (int) ((censusGroup.latitude - gridBottom) / rectangleHeight);
            if (censusGroup.latitude >= (col + 1) * rectangleHeight + gridBottom)
                col++;
            col = (col == breadth ?  breadth - 1: col);
            row = (int) ((censusGroup.longitude - gridLeft) / rectangleWidth);
            if (censusGroup.longitude >= (row + 1) * rectangleWidth + gridLeft)
                row++;
            row = (row == length ? length - 1 : row);
            grid[row][col] += censusGroup.population;

        }
        for(int j=grid[0].length-1;j>=0;j--)
		{
			for(i=0;i<grid.length;i++)
			{
				grid[i][j]+=((i>0)?grid[i-1][j]:0)+((j<grid[0].length-1)?grid[i][j+1]:0)-((i>0&&j<grid[0].length-1)?grid[i-1][j+1]:0);
			}
		}
  	}
public long calculatePopulationBasedOnInput() {
		long totalPopulation = 0;
		
		for(int i=(int)(userQuery.left-1);i<(int)(userQuery.right);i++)
		{
			for(int j=(int)(userQuery.bottom-1);j<(int)(userQuery.top);j++)
			{
				totalPopulation+=grid[i][j]+((i>0&&j<breadth-1)?grid[i-1][j+1]:0)-((i>0)?grid[i-1][j]:0)-((j<breadth-1)?grid[i][j+1]:0);
			}
		}		
		return totalPopulation;
	}
}
