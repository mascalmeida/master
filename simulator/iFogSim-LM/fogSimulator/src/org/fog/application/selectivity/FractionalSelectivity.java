package org.fog.application.selectivity;

import org.fog.utils.distribution.NormalDistribution;

import java.util.Random;

/**
 * Generates an output tuple for an incoming input tuple with a fixed probability
 * @author Harshit Gupta
 *
 */
public class FractionalSelectivity implements SelectivityModel{

	/**
	 * The fixed probability of output tuple creation per incoming input tuple
	 */
	double selectivity;
	int seed;
	public FractionalSelectivity(double selectivity, int seed){
		setSelectivity(selectivity);
		this.seed = seed;
	}

	public FractionalSelectivity(double selectivity){
		this(selectivity,-1);
	}

	public double getSelectivity() {
		return selectivity;
	}
	public void setSelectivity(double selectivity) {
		this.selectivity = selectivity;
	}

	@Override
	public boolean canSelect() {
		Random r = new Random();
		if(seed>0) {
			r.setSeed(seed);
		}
		if(r.nextDouble() < getSelectivity()) // if the probability condition is satisfied
			return true;
		return false;
	}

	@Override
	public double getMeanRate() {
		return getSelectivity(); // the average rate of tuple generation is the fixed probability value
	}

	@Override
	public double getMaxRate() {
		return getSelectivity(); // the maximum rate of tuple generation is the fixed probability value
	}

}
