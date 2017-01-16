package adshotrunner.powerpoint;

import java.util.Random;

class TemplateCreator {

	final private static int POINTSPERPIXEL = 9525;	//Microsoft defined points per pixel in a PowerPoint
	
	public static String background() {
	    return "<p:bg xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" +
	    			"<p:bgPr>" + 
	    				"<a:blipFill dpi=\"0\" rotWithShape=\"1\">" +
		    				"<a:blip r:embed=\"${rID}\" cstate=\"print\">" +
		    					"<a:lum/>" +
		    				"</a:blip><a:srcRect/>" + 
		    				"<a:stretch><a:fillRect/></a:stretch></a:blipFill><a:effectLst/>" +
		    		"</p:bgPr>" +
		    	"</p:bg>";
	}
	
	public static String picture(String relationshipID, int xPosition, int yPosition, int width, int height) {
		
		//Convert the position and dimensions into their point equivalents
	    int xPositionPoint = xPosition * POINTSPERPIXEL;
	    int yPositionPoint = yPosition * POINTSPERPIXEL;
	    int widthPoint = width * POINTSPERPIXEL;
	    int heightPoint = height * POINTSPERPIXEL;
	    
	    //Generate a random ID for the picture
	    int pictureID = Math.abs(new Random().nextInt());
		
	    //Return the picture XML
	    return			
	            "<p:pic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"> "
	              + "<p:nvPicPr>"
	                + "<p:cNvPr id=\"" + pictureID + "\" name=\"Picture" + pictureID + "\" descr=\"Description" + pictureID + "\"/>"
	                + "<p:cNvPicPr>"
	                  + "<a:picLocks noChangeAspect=\"1\"/>"
	                + "</p:cNvPicPr>"
	                + "<p:nvPr/>"
	              + "</p:nvPicPr>"
	              + "<p:blipFill>"
	                + "<a:blip r:embed=\"" + relationshipID + "\" cstate=\"print\"/>"
	                + "<a:stretch>"
	                  + "<a:fillRect/>"
	                + "</a:stretch>"
	              + "</p:blipFill>"
	              + "<p:spPr>"
	                + "<a:xfrm>"
	                  + "<a:off x=\"" + xPositionPoint + "\" y=\"" + yPositionPoint + "\"/>"
	                  + "<a:ext cx=\"" + widthPoint + "\" cy=\"" + heightPoint + "\"/>"
	                + "</a:xfrm>"
	                + "<a:prstGeom prst=\"rect\">"
	                  + "<a:avLst/>"
	                + "</a:prstGeom>"
	              + "</p:spPr>"
	            + "</p:pic>";		
	}
	
	public static String textBox(String text, int xPosition, int yPosition, int width,
								 String fontType, int fontSize, String fontColor) {
		
		//Convert the position and dimensions into their point equivalents
	    int xPositionPoint = xPosition * POINTSPERPIXEL;
	    int yPositionPoint = yPosition * POINTSPERPIXEL;
	    int widthPoint = width * POINTSPERPIXEL;
	    
	    //Generate a random ID for the text
	    int textID = Math.abs(new Random().nextInt());
	    
	    //Return the textbox XML
	    return
			"<p:sp xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" +
				"<p:nvSpPr>" +
					"<p:cNvPr id=\"" + textID + "\" name=\"TextShape " + textID + "\"/>" +
					"<p:cNvSpPr txBox=\"1\"/>" +
					"<p:nvPr/>" +
				"</p:nvSpPr>" +
				"<p:spPr>" +
					"<a:xfrm>" +
					"<a:off x=\"" + xPositionPoint + "\" y=\"" + yPositionPoint + "\"/>" +
					"<a:ext cx=\"" + widthPoint + "\" cy=\"546680\"/>" +
					"</a:xfrm>" +
					"<a:prstGeom prst=\"rect\">" +
					"<a:avLst/>" +
					"</a:prstGeom>" +
				"</p:spPr>" +
				"<p:txBody>" +
					"<a:bodyPr wrap=\"none\" lIns=\"90000\" tIns=\"45000\" rIns=\"90000\" bIns=\"45000\">" +
					"<a:noAutofit/>" +
					"</a:bodyPr>"+
					"<a:p>" +
					"<a:r>" +
					"<a:rPr lang=\"en-US\" sz=\"" + fontSize + "00\">" +
					"<a:solidFill>" +
					"<a:srgbClr val=\"" + fontColor + "\"/>" +
					"</a:solidFill>" +
					"<a:latin typeface=\"" + fontType + "\"/>" +
					"</a:rPr>" +
					"<a:t>" + text + "</a:t>" +
					"</a:r>" +
					"<a:endParaRPr/>" +
					"</a:p>" +
				"</p:txBody>" +
			"</p:sp>";
	}
}
