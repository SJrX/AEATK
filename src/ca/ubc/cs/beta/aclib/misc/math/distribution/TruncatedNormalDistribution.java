package ca.ubc.cs.beta.aclib.misc.math.distribution;

import java.util.Random;

import net.sf.doodleproject.numerics4j.special.Erf;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Truncated Normal Distribution as defined in:
 * 
 * Bayesian Optimization With Censored Response Data
 * Frank Hutter, Holger Hoos, and Kevin Leyton-Brown
 * (http://www.cs.ubc.ca/~hutter/papers/11-NIPS-workshop-BO-with-censoring.pdf)
 * 
 * Not all methods are implemented
 * 
 * @author seramage
 */
public class TruncatedNormalDistribution extends AbstractRealDistribution {

	
	private static final long serialVersionUID = -901332698348589236L;
	private final double mu;
	private final double variance;
	private final double sigma;
	private final double kappa;
	private final NormalDistribution norm;
	private final Random random;
	
	/**
	 * Creates the Distribution
	 * @param mean 			mean of the distribution
	 * @param variance		variance of the distribution
	 * @param kappa			minimum value of the distribution
	 * @param rand			random object used for tie breaking
	 */
	public TruncatedNormalDistribution(double mean, double variance, double kappa, Random rand)
	{
		this.mu = mean;
		this.variance = variance;
		this.kappa = kappa;
		this.sigma = Math.sqrt(variance);
		this.norm = new NormalDistribution(mean, variance);
		this.random = rand;
		
	}
	
	@Override
	public double cumulativeProbability(double x) {

		throw new UnsupportedOperationException("Not Implemented Yet");
	}

	@Override
	public double density(double x) {
		if(x < kappa)
		{	
			return 0.0;
		} else
		{
			double numerator = (1 / sigma) * norm.density((x - mu)/sigma);
			double denominator = (1 - norm.cumulativeProbability((mu-kappa)/sigma));
			return numerator / denominator;
		}
	}

	@Override
	public double getNumericalMean() {
		return mu;
	}

	@Override
	public double getNumericalVariance() {
		return variance;
	}

	@Override
	public double getSupportLowerBound() {
		throw new UnsupportedOperationException("Not Implemented Yet");
	}

	@Override
	public double getSupportUpperBound() {
		throw new UnsupportedOperationException("Not Implemented Yet");
	}

	@Override
	public boolean isSupportConnected() {
		throw new UnsupportedOperationException("Not Implemented Yet");	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		throw new UnsupportedOperationException("Not Implemented Yet");
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		throw new UnsupportedOperationException("Not Implemented Yet");
	}

	public double erfinv(double x)
	{
		return Erf.inverseErf(x);
	}
	
	@Override
	public double sample()
	{
		/*
		 * Stolen from mtatlab code
		 * rand_draw_truncated_normal.m 
		 * 
		 *PHIl = normcdf((a-mu)/sigma);
		 * PHIr = normcdf((b-mu)/sigma);
		 * u is essentially the percentile we want
		 *	samples = mu + sigma*( sqrt(2)*erfinv(2*(PHIl+(PHIr-PHIl)*u)-1) );

		 */
		double u = random.nextDouble();
		double PHIl = norm.cumulativeProbability((kappa-mu)/sigma);
		double PHIr = norm.cumulativeProbability((Double.POSITIVE_INFINITY-mu)/sigma);
		double samples = mu + sigma*( Math.sqrt(2)*erfinv(2*(PHIl+(PHIr-PHIl)*u)-1) );
		
		return samples;
	}
	
	@Override
	public double probability(double arg0) {

		return 0;
	}

}
