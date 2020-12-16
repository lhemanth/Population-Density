package censusPopulationDensity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
public class SmarterAndParallel extends SimpleAndParallel{
	int[][] grid;
	public SmarterAndParallel(int x, int y, CensusData censusData, int threshold) {
		super(x, y, censusData, threshold);
        grid = new int[x][y];
	}
	public void findUSMap() {
		super.findUSMap();
		grid = forkJoinPool.invoke(new SmarterRecursiveAction(0, censusData.data_size));
		int i=0;
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
	@SuppressWarnings("serial")
    class SmarterRecursiveAction extends RecursiveTask<int[][]>{
        int high, low;
         SmarterRecursiveAction(int low, int high) {
            this.low  = low;
            this.high = high;
        }
        protected int[][] compute() {
        	if(high - low >  THRESHOLD) {
        		SmarterRecursiveAction left = new SmarterRecursiveAction(low, (high+low)/2);
            	SmarterRecursiveAction right = new SmarterRecursiveAction((high+low)/2, high);
                left.fork(); 
                int[][] gRight = right.compute();
                int[][] gLeft = left.join();
                 forkJoinPool.invoke(new AddGridsParallelly(0, length, gLeft, gRight));
                return gRight;
        	}
        	else{
                CensusGroup censusGroup;
                int row, col;
                int[][] grid = new int[length][breadth];
                float width = (usRectangle.right - usRectangle.left) / length;
        		float height = (usRectangle.top - usRectangle.bottom) / breadth;
                float gridLeft = usRectangle.left;
                float gridBottom = usRectangle.bottom;
                for (int i = low; i < high; i++) {
                	censusGroup = censusData.data[i];
                    col = (int) ((censusGroup.latitude - gridBottom) / height);
                    if (censusGroup.latitude >= (col + 1) * height + gridBottom)
                        col++;
                    col = (col == breadth ?  breadth - 1: col);
                    row = (int) ((censusGroup.longitude - gridLeft) / width);
                    if (censusGroup.longitude >= (row + 1) * width + gridLeft)
                        row++;
                    row = (row == length ? length - 1 : row);
                    grid[row][col] += censusGroup.population;
                }
                return grid;
            }
        }
    }

    class AddGridsParallelly extends RecursiveAction{
		private static final long serialVersionUID = 1L;
		int gridHigh, gridLow;
        int[][] left, right;
         AddGridsParallelly(int gridLow, int gridHigh, int[][] left, int[][] right) {
            this.gridLow  = gridLow;
            this.gridHigh = gridHigh;
            this.left = left;
            this.right = right;
        }
        protected void compute() {
        	
        	if(gridHigh-gridLow >  THRESHOLD) {
        		List<AddGridsParallelly> subtasks = new ArrayList<>();
                subtasks.add(new AddGridsParallelly(gridLow, (gridHigh+gridLow)/2, left, right));
                subtasks.add(new AddGridsParallelly((gridHigh+gridLow)/2, gridHigh, left, right));
                ForkJoinTask.invokeAll(subtasks);
        	}
        	else{
                for(int i = gridLow; i < gridHigh; i++) {
                    for(int j = 0; j < breadth; j++)
                        right[i][j] += left[i][j];
                }
            }
        }
    }
}
