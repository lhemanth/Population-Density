package censusPopulationDensity;

import java.util.concurrent.locks.ReentrantLock;

public class SmarterAndLockBased extends SimpleAndParallel {
	// Store locked status corresponding to grid
	private final ReentrantLock[][] reentrantLocks;
	int[][] grid;

	public SmarterAndLockBased(int x, int y, CensusData data, int threshold) {
		super(x, y, data, threshold);
		grid = new int[x][y];
		reentrantLocks = new ReentrantLock[x][y];
		// Initialize Reentrant lock
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				reentrantLocks[i][j] = new ReentrantLock();
			}
		}
	}

	public void findUSMap() {
		super.findUSMap();

		SmarterRecursiveThread sp = new SmarterRecursiveThread(0, censusData.data_size);
		sp.run();

		int i = 0;
		// Modify the grid, so that grid element g stores the total population in the
		// rectangle whose upper-left is the North-West corner of the country and the
		// lower-right corner is g
		for (int j = grid[0].length - 1; j >= 0; j--) {
			for (i = 0; i < grid.length; i++) {// add elements to top and left and subtract diagonal element
				grid[i][j] += ((i > 0) ? grid[i - 1][j] : 0) + ((j < grid[0].length - 1) ? grid[i][j + 1] : 0)
						- ((i > 0 && j < grid[0].length - 1) ? grid[i - 1][j + 1] : 0);
			}
		}
	}

	public long findPopulationOfQueriedArea() {
		long totalPopulation = 0;

		for (int i = (int) (userQuery.left - 1); i < (int) (userQuery.right); i++) {
			for (int j = (int) (userQuery.bottom - 1); j < (int) (userQuery.top); j++) {// subtract elements to top and
																						// left and add diagonal element
				totalPopulation += grid[i][j] + ((i > 0 && j < breadth - 1) ? grid[i - 1][j + 1] : 0)
						- ((i > 0) ? grid[i - 1][j] : 0) - ((j < breadth - 1) ? grid[i][j + 1] : 0);
			}
		}
		return totalPopulation;
	}

	class SmarterRecursiveThread extends java.lang.Thread {
		int high, low;

		SmarterRecursiveThread(int low, int high) {
			this.low = low;
			this.high = high;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			
			if(high - low >  THRESHOLD) {
				SmarterRecursiveThread leftThread = new SmarterRecursiveThread(low,
						(high + low) / 2);
				SmarterRecursiveThread rightThread = new SmarterRecursiveThread(
						(high + low) / 2, high);

				leftThread.start(); // fork a thread and calls compute
				rightThread.run(); // call compute directly
				try {
					leftThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
			else{
				CensusGroup censusGroup;
				int row, col;

				float rectangleWidth = (usRectangle.right - usRectangle.left) / length;
				float rectangleHeight = (usRectangle.top - usRectangle.bottom) / breadth;
		        
		        float gridLeft = usRectangle.left;
		        float gridBottom = usRectangle.bottom;


		        for (int i = 0; i < censusData.data_size; i++) {
		            censusGroup = censusData.data[i];
		            col = (int) ((censusGroup.latitude - gridBottom) / rectangleHeight);
		            if (censusGroup.latitude >= (col + 1) * rectangleHeight + gridBottom)//If it touches the border then make it inclusive
		                col++;
		            col = (col == breadth ?  breadth - 1: col);//check for edges
		            row = (int) ((censusGroup.longitude - gridLeft) / rectangleWidth);
		            if (censusGroup.longitude >= (row + 1) * rectangleWidth + gridLeft)//If it touches the border then make it inclusive
		                row++;
		            row = (row == length ? length - 1 : row);//Check for edges

					reentrantLocks[row][col].lock();
					try {
						grid[row][col] += censusGroup.population;
					} finally {
						reentrantLocks[row][col].unlock();
					}

				}

			}

		}

	}
}
