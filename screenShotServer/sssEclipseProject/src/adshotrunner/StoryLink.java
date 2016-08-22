package adshotrunner;

public class StoryLink {
	
	public String href;
	public String className;
	public String style;
	public String text;
	public String title;
	public int xPosition;
	public int yPosition;
	public int width;
	public int height;
	public String id;
	public String name;
	public String onclick;
	
	public String scoreExplanationLog;
	
	public StoryLink() {
		
		scoreExplanationLog = "";
	}
	
	public void addScoreLog(String explanation){
		
		this.scoreExplanationLog += explanation + ",";
	}
}
