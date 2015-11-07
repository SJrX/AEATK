package ca.ubc.cs.beta.aeatk.algorithmrunresult;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.factory.AlgorithmRunResultFactory;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator should order maps such that status, runtime, cost come first then regular arguments
 *
 *
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
public class AlgorithmRunResultKeyComparator implements Comparator<String>, Serializable {

    @Override
    public int compare(String firstArg, String secondArg) {

        boolean first = firstArg.equals(AlgorithmRunResultFactory.STATUS_KEY);
        boolean second = secondArg.equals(AlgorithmRunResultFactory.STATUS_KEY);

        if(first || second)
        {
            return getComparisonFirstTrueMeansFirst(first, second);
        }

        first = firstArg.equals(AlgorithmRunResultFactory.RUNTIME_KEY);
        second = secondArg.equals(AlgorithmRunResultFactory.RUNTIME_KEY);

        if(first || second)
        {
            return getComparisonFirstTrueMeansFirst(first, second);
        }

        first = firstArg.equals(AlgorithmRunResultFactory.COST_KEY);
        second = secondArg.equals(AlgorithmRunResultFactory.COST_KEY);

        if(first || second)
        {
            return getComparisonFirstTrueMeansFirst(first, second);
        }


        first = firstArg.startsWith("_");
        second = secondArg.startsWith("_");

        if(first || second)
        {
            return getComparisonFirstTrueMeansLast(first, second);
        }

        first = firstArg.startsWith("__");
        second = secondArg.startsWith("__");

        if(first || second)
        {
            return getComparisonFirstTrueMeansLast(first, second);
        }


        return firstArg.compareTo(secondArg);
    }

    private int getComparisonFirstTrueMeansLast(boolean first, boolean second) {
        if (first && second)
        {
            return 0;
        } else if (first)
        {
            return 1;
        } else
        {
            return -1;
        }
    }

    private int getComparisonFirstTrueMeansFirst(boolean first, boolean second) {
        if (first && second)
        {
            return 0;
        } else if (first)
        {
            return -1;
        } else
        {
            return 1;
        }
    }


    private static final Comparator<String> instance = new AlgorithmRunResultKeyComparator();

    public static Comparator<String> getInstance()
    {
        return instance;
    }

}
