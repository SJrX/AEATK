package ca.ubc.cs.beta.aeatk.algorithmrunresult;

/**
 *
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
public enum Satisfiability {
    SATISFIABILE,
    UNSATISFIABLE,
    UNKNOWN;


    public static Satisfiability fromString(String s)
    {
        if(s == null)
        {
            return UNKNOWN;
        }
        switch(s.trim().toLowerCase())
        {
            case "y":
            case "yes":
            case "1":
            case "true":
            case "sat":
            case "satisfiable":
                return SATISFIABILE;
            case "n":
            case "no":
            case "0":
            case "false":
            case "unsat":
            case "unsatisfiable":
                return UNSATISFIABLE;
            case "?":
            case "unknown":
            case "timeout":
            case "null":
                return UNKNOWN;
        }

        throw new IllegalArgumentException("Not sure how to convert " + s + " into a satisfiability result please use \"SATISFIABLE\",\"UNSATISFIABLE\", \"UNKNOWN\"");
    }
}
