package adshotrunner.shotter;

import java.util.Map;

public class InjecterResponse {
	
	public String outputLog;
	public Map<Integer, Coordinate> injectedCreatives;
	
	class Coordinate {
		int x;
		int y;
	}
	
}
