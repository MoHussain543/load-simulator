package com.mohammed.loadsimulator.engine;

public record SingleRequestOutcome(boolean success, double responseTimeMs) {

	public static SingleRequestOutcome failure(double responseTimeMs) {
		return new SingleRequestOutcome(false, responseTimeMs);
	}
}
