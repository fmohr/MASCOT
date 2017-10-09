package de.upb.crc901.mascot.logic;

import java.util.Collection;
import java.util.HashSet;

import org.sat4j.core.VecInt;

@SuppressWarnings("serial")
public class DimacsCNF extends HashSet<VecInt> {
	
	public DimacsCNF() {
		super();
	}
	
	public DimacsCNF(Collection<VecInt> copy) {
		super(copy);
	}
	
	public int getNumberOfLiterals() {
		int sum = 0;
		for (VecInt clause : this) {
			sum += clause.size();
		}
		return sum;
	}
}
