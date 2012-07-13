package ca.ubc.cs.beta.aclib.expectedimprovement;
import static ca.ubc.cs.beta.aclib.misc.math.ArrayMathOps.*;
/**
 * The ExpectedExponentialImprovement
 * 
 * Most of this code was a direct copy paste from a .c file in the MATLAB version
 * 
 * 
 * @author seramage
 *
 */
public strictfp class ExpectedExponentialImprovement implements ExpectedImprovementFunction {

	
	/* Approximation of Normal CDF from http://www.sitmo.com/doc/Calculating_the_Cumulative_Normal_Distribution */
	private double normcdf( double x)
	{
	     double b1 =  0.319381530;
	     double b2 = -0.356563782;
	     double b3 =  1.781477937;
	     double b4 = -1.821255978;
	     double b5 =  1.330274429;
	     double p  =  0.2316419;
	     double c  =  0.39894228;

	    if(x >= 0.0) {
	        double t = 1.0 / ( 1.0 + p * x );
	        return (1.0 - c * exp( -x * x / 2.0 ) * t * ( t *( t * ( t * ( t * b5 + b4 ) + b3 ) + b2 ) + b1 ));
	    } else {
	        double t = 1.0 / ( 1.0 - p * x );
	        return ( c * exp( -x * x / 2.0 ) * t * ( t *( t * ( t * ( t * b5 + b4 ) + b3 ) + b2 ) + b1 ));
	    }
	}

	/* Compute log of normal cumulative density function.
	 * Translated and shortened from Tom Minka's Matlab lightspeed 
	 * implementation by Frank Hutter.
	 * More accurate than log(normcdf(x)) when x is small.
	 * The following is a quick and dirty approximation to normcdfln:
	 * normcdfln(x) =approx -(log(1+exp(0.88-x))/1.5)^2 */
	private double normcdfln( double x){
	    double y, z, pi = 3.14159265358979323846264338327950288419716939937510;
	    if( x > -6.5 ){
	        return log( normcdf(x) );
	    }
	    z = pow(x, -2);
	/*    c = [-1 5/2 -37/3 353/4 -4081/5 55205/6 -854197/7];
	    y = z.*(c(1)+z.*(c(2)+z.*(c(3)+z.*(c(4)+z.*(c(5)+z.*(c(6)+z.*c(7)))))));*/
	    y = z*(-1+z*(5.0/2+z*(-37.0/3+z*(353.0/4+z*(-4081.0/5+z*(55205.0/6+z*-854197.0/7))))));
	    return y - 0.5*log(2*pi) - 0.5*x*x - log(-x);
	}


	/* Univariate Normal PDF */
	@SuppressWarnings("unused")
	private double normpdf( double x)
	{
	    double pi = 3.14159265358979323846264338327950288419716939937510;
	    return 1/sqrt(2*pi) * exp(-x*x/2);
	}
	
	private double[] log_exp_exponentiated_imp( double[] fmin_samples, double[] mus, double[] sigmas ){
	    int i,s;
	    double cdfln_1, cdfln_2, c, d;
	    /* Formula from .m file: 
	     *  c = f_min + normcdfln((f_min-mu(i))/sigma(i));
	     *  d = (sigma(i)^2/2 + mu(i)) + normcdfln((f_min-mu(i))/sigma(i) - sigma(i));*/
	    
	    int numSamples = fmin_samples.length;
	    int numMus = mus.length;
	    
	    double[] log_expEI = new double[mus.length];
	    if (numSamples > 1){
	    	throw new IllegalArgumentException("log_exp_exponentiated_imp not yet implemented for numSamples>1; can do that based on logsumexp trick.");
	     
	    }

	    for (i=0; i<numMus; i++){
	        log_expEI[i] = 0;        
	        
	        for (s=0; s<numSamples; s++){
	            cdfln_1 = normcdfln((fmin_samples[s]-mus[i])/sigmas[i]);
	            cdfln_2 = normcdfln((fmin_samples[s]-mus[i])/sigmas[i] - sigmas[i]);
	            c = fmin_samples[s] + cdfln_1;
	            d = (sigmas[i]*sigmas[i]/2 + mus[i]) + cdfln_2;
	            if (c<=d){
	/*                if (c < d-1e-6){
	                    printf("c=%lf, d=%lf\n", c, d);
	                    mexErrMsgTxt("Error -- due to approx. errors with normcdfln?");
	                } else {*/
	                    log_expEI[i] = d;
	/*                }*/
	            } else {
	                log_expEI[i] = d + log(exp(c-d)-1); /* for multiple samples, would collect these values in array, and then apply logsumexp */
	            }
	        }
	    }
	    
	  
	   
	    return log_expEI;
	}
	@Override
	public double[] computeNegativeExpectedImprovement(double f_min_samples,
			double[] predmean, double[] predvar) {
		
		if(predmean.length != predvar.length)
		{
			throw new IllegalArgumentException("Expected predmean and predvar to have the same length");
		}
		
		
		double log10 = Math.log(10.0);
		
		double[] fmin =  {log10*f_min_samples};
		double[] expImp = log_exp_exponentiated_imp(fmin, times(log10,predmean), times(log10,sqrt(predvar)));
		 //System.out.println(f_min_samples + "," + predmean[0] + "," + predvar[0] + "=> " + expImp[0]);
		
		for(int i=0; i < expImp.length; i++)
		{
			expImp[i] = -expImp[i];
		}
		
		return expImp;
		
	}

}
