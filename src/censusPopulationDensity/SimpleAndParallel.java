package censusPopulationDensity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SimpleAndParallel extends USRectangle{

	ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
	final int THRESHOLD;
	
	public SimpleAndParallel(int length, int breadth, CensusData censusData, int THRESHOLD) {
		this.length = length;
		this.breadth = breadth;
		this.censusData = censusData;
		this.THRESHOLD = THRESHOLD;
	}
	
	public void findUSMap() {
        if (censusData.data_size == 0)
            return;

        Pair<Rectangle,Long> result = forkJoinPool.invoke(new CustomRecursiveAction(0, censusData.data_size));
        usRectangle = result.getElementA();
        overallPopulation = result.getElementB();
	}
	
	class CustomRecursiveAction extends RecursiveTask<Pair<Rectangle,Long>> {
		private static final long serialVersionUID = 1L;

		int high, low;

		CustomRecursiveAction(int low, int high) {
            this.low  = low;
            this.high = high;
        }

        @Override
        protected Pair<Rectangle,Long> compute() {
        	if(high - low > THRESHOLD)
        	{
        		CustomRecursiveAction leftHalf = new CustomRecursiveAction(low,(high+low)/2);
        		CustomRecursiveAction rightHalf = new CustomRecursiveAction((high+low)/2, high);
        		
        		leftHalf.fork();
        		Pair<Rectangle,Long> leftRes = rightHalf.compute();
        		Pair<Rectangle,Long> rightRes = leftHalf.join();
                
        		return new Pair<Rectangle,Long>(rightRes.getElementA().encompass(leftRes.getElementA()),rightRes.getElementB()+leftRes.getElementB());
        	}
        	else
        	{
        		int i = low;
        		Rectangle rec = null;
                long pop = 0;
                do {
                	
        			CensusGroup censusGroup = censusData.data[i];
        			if(censusGroup != null) {
        				Rectangle temp = new Rectangle(censusGroup.longitude, censusGroup.longitude, censusGroup.latitude, censusGroup.latitude);
        				if(rec!=null)
        					rec = rec.encompass(temp);
        				else
        					rec = temp;
        				pop += censusGroup.population;
        			}
        			i++;
        		}while(i<high);
                Pair<Rectangle,Long> pair = new Pair<>(rec,pop);
                return pair;
        	}
        }

    }
	
	public long calculatePopulationBasedOnInput() {
		double queryLeft = (usRectangle.left + (userQuery.left - 1) * (usRectangle.right - usRectangle.left) / length);
		double queryRight = (usRectangle.left + (userQuery.right) * (usRectangle.right - usRectangle.left) / length);
		double queryTop = (usRectangle.bottom + (userQuery.top) * (usRectangle.top - usRectangle.bottom) / breadth);
		double queryBottom = (usRectangle.bottom + (userQuery.bottom - 1) * (usRectangle.top - usRectangle.bottom) / breadth);
		QueryRecursiveTask queryRectangle = new QueryRecursiveTask(0, censusData.data_size, queryLeft, queryRight, queryTop, queryBottom);
		return (long) forkJoinPool.invoke(queryRectangle);
	}
	
	class QueryRecursiveTask extends RecursiveTask<Integer> {
		private static final long serialVersionUID = 1L;

		int high, low;
        
        double queryLeft, queryRight, queryTop, queryBottom;

        QueryRecursiveTask(int low, int high, double queryLeft, double queryRight, double queryTop, double queryBottom) {
            this.low  = low;
            this.high = high;
            this.queryLeft = queryLeft;
            this.queryRight = queryRight;
            this.queryTop = queryTop;
            this.queryBottom = queryBottom;
        }

        @Override
        protected Integer compute() {
        	if(high - low >  THRESHOLD) {
        		List<QueryRecursiveTask> subtasks = new ArrayList<>();
        		//Create subtasks to divide original task to 2 subtasks
                subtasks.add(new QueryRecursiveTask(low, (high+low)/2,queryLeft,queryRight,queryTop,queryBottom));
                subtasks.add(new QueryRecursiveTask((high+low)/2, high,queryLeft,queryRight,queryTop,queryBottom));
         
                return ForkJoinTask.invokeAll(subtasks)
                        .stream()
                        .mapToInt(ForkJoinTask::join)
                        .sum();
        	}
        	else{
                int partPopulation = 0;

                for (int i = low; i < high; i++) {
        			CensusGroup censusGroup = censusData.data[i];
        			
                    // If census-block-group falls exactly on the border of more than one grid position, tie-break by assigning it to the North and/or East
                    if (censusGroup.latitude >= queryBottom &&
                            (censusGroup.latitude < queryTop ||
                                    (queryTop - usRectangle.top) >= 0) &&
                                    (censusGroup.longitude < queryRight ||
                                            (queryRight - usRectangle.right) >= 0) && 
                                    censusGroup.longitude >= queryLeft) {
                    		//As Census group lies in queried area, sum up the population
                    	partPopulation += censusGroup.population;
                    }
        		}

                return partPopulation;
            }
        }
    }
}
