package ca.ubc.cs.beta.aeatk.algorithmrunresult;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 *
 * This class replaces the {@link RunStatus} class, and essentially merges SAT and UNSAT, into success. It is also more strict about aliases.
 *
 */
public enum RunExecutionStatus {

    SUCCESS(),
    TIMEOUT(),
    CRASHED(),
    ABORT(),
    RUNNING(),
    KILLED();

    RunExecutionStatus()
    {

    }



}
