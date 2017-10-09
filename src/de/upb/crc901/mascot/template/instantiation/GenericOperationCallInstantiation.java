package de.upb.crc901.mascot.template.instantiation;

import de.upb.crc901.configurationsetting.operation.OperationInvocation;
import de.upb.crc901.mascot.structure.GenericOperationCall;

public class GenericOperationCallInstantiation {

	private static int counter = 0;
	private int id = counter++;
	private GenericOperationCall genericOperationCall;
	private OperationInvocation operationCall;

	public GenericOperationCallInstantiation(final GenericOperationCall pGenericOperationCall,
			OperationInvocation pOperationCall) {
		super();
		this.genericOperationCall = pGenericOperationCall;
		this.operationCall = pOperationCall;
	}

	public int getId() {
		return id;
	}

	public GenericOperationCall getGenericOperationCall() {
		return genericOperationCall;
	}

	public OperationInvocation getOperationCall() {
		return operationCall;
	}

	@Override
	public String toString() {
		return "GenericOperationCallInstantiation [id=" + id + ", genericOperationCall=" + genericOperationCall
				+ ", operationCall=" + operationCall + "]";
	}
}
