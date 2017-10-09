package de.upb.crc901.mascot.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimacsFormulaStream {
	private final static Logger l = LoggerFactory.getLogger(DimacsFormulaStream.class);
	private final Collection<Integer> props;
	private final int maxlength;
	private final boolean disjunctionsAllowed;
	private final boolean negationAllowed;
	private final Map<Integer,List<Set<Integer>>> clauses = new HashMap<>();
	private final Map<Integer, Set<Set<Set<Integer>>>> cache = new HashMap<>();
	
	
	private Map<Integer,Integer> positionInLevel = new HashMap<>();
	private Queue<Set<Set<Integer>>> queue;
	
	public DimacsFormulaStream(Collection<Integer> props, int maxlength, boolean disjunctionsAllowed, boolean negationAllowed) {
		super();
		this.props = props;
		this.maxlength = maxlength;
		this.disjunctionsAllowed = disjunctionsAllowed;
		this.negationAllowed = negationAllowed;
		
		/* create the formula of length 0 */
		Set<Set<Integer>> emptyCNF = new HashSet<>();
		Set<Set<Set<Integer>>> cnfsOfSizeZero = new HashSet<>();
		cnfsOfSizeZero.add(emptyCNF);
		cache.put(0, cnfsOfSizeZero);
	}
	public Collection<Integer> getProps() {
		return props;
	}
	public int getMaxlength() {
		return maxlength;
	}
	public boolean isDisjunctionsAllowed() {
		return disjunctionsAllowed;
	}
	public boolean isNegationAllowed() {
		return negationAllowed;
	}
	
	public DimacsCNF next() throws InterruptedException {
		return nextOfMaxLength(Integer.MAX_VALUE);
	}
	
	public DimacsCNF nextOfMaxLength(final int pMaxLength) throws InterruptedException {
		
		/* is an element in the queue */
		if (queue == null) {
			int level = getExplorationLevel() + 1;
			if (level > maxlength || level > pMaxLength)
				return null;
			computeAllUpToLength(level);
			List<Set<Set<Integer>>> list = new ArrayList<>(cache.get(level));
			l.info("CNFs of size {}: {}", level, list);
			if (list.isEmpty())
				return null;
			Collections.shuffle(list);
			queue.addAll(list);
		}
		
		/* get next element from queue, convert it into dimacs and return it */
		DimacsCNF nextOne = DimacsUtil.convertIntCNFToDimacsCNF(queue.peek());
		if (nextOne.getNumberOfLiterals() > pMaxLength)
			return null;
		queue.poll();
		
		/* if no more element is in the queue, remove the queue */
		if (queue.isEmpty())
			queue = null;
		return nextOne;
	}
	
	public DimacsCNF nextOfExactLength(final int pExactLength) throws InterruptedException {
		
		/* is an element in the queue */
		if (pExactLength > getExplorationLevel()) {
			if (pExactLength > maxlength)
				return null;
			computeAllUpToLength(pExactLength);
			List<Set<Set<Integer>>> list = new ArrayList<>(cache.get(pExactLength));
			l.info("CNFs of size {}: {}", pExactLength, list);
			if (list.isEmpty())
				return null;
			Collections.shuffle(list);
			queue.addAll(list);
		}
		
		/* get next element from queue, convert it into dimacs and return it */
		Set<Set<Integer>> cnf;
		int size;
		while (!queue.isEmpty()) {
			cnf = queue.poll();
			size = 0;
			for (Set<Integer> clause : cnf) {
				size += clause.size();
			}

			if (size != pExactLength)
				continue;
			return DimacsUtil.convertIntCNFToDimacsCNF(cnf);
		}
		return null;
	}
	
	private void computeAllUpToLength(int length) throws InterruptedException {
		
		for (int level = getExplorationLevel() + 1; level <= length; level++) {
			
			/* if no more element CAN or SHALL be generated */
			if (level > maxlength || level > length)
				return;
			
			/* first compute clauses of the desired size */
			if (level == 1 || disjunctionsAllowed)
				clauses.put(level, new ArrayList<>(DimacsUtil.getPossibleClausesOfSize(props, level, negationAllowed)));
			
			/* otherwise create cnfs of next step and insert them into the queue */
			l.info("Now compute CNFs of size {}.", level);
			cache.put(level, DimacsUtil.getPossibleCNFs(clauses, level, cache));
		}
	}
	
	public void reset() {
		reset(1);
	}
	
	public void reset(int length) {
		queue = new LinkedList<>();
		for (int i = length; i <= getExplorationLevel(); i++) {
			positionInLevel.put(i, 0);
			queue.addAll(cache.get(i));
		} 
	}
	
	/**
	 * Indicates the size up to which all possible cnfs have been generated.
	 * @return
	 */
	private int getExplorationLevel() {
		return cache.size() - 1;
	}
}
