package com.programmr.pneumoapp;

public class BreathCounts {
	int max=0;
	int min=0;
    double avg;
	public BreathCounts(int min,int max,double avg){
		this.min=min;
		this.max=max;
		this.avg=avg;
	}
	
	public int getMax(){
		return max;
	}
	
	public int getMin(){
		return min;
	}
	public double getAvg(){
		return avg;
	}
}
