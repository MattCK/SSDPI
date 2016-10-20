/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package adshotrunner.techpreview;
import java.util.Random;
/**
 *
 * @author matt
 */
public class TextBoxDetails {
    public int x;
    public int y;
    public String Font;
    public String fontColor;
    public int fontSize;
    public String Text;

    public String BuildXMLTextBoxString()
    {
    //int convertedX = x * 12700;
    //int convertedY = y * 12700;
    int convertedX = x * 9525;
    int convertedY = y * 9525;
    
    //the text box size default size needs to be widened for very long text lengths
    int calculatedWidth = 5303520;
    if (Text.length() >= 70){
    	calculatedWidth = 8403120;
    }
    
    Random randomGenerator = new Random();
    int randoCalrisian = randomGenerator.nextInt();
    randoCalrisian = Math.abs(randoCalrisian);
    String TextBoxXML = 
    "<p:sp   xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" +
    //"<p:sp>" +
    "<p:nvSpPr>" +
    "<p:cNvPr id=\"" + Integer.toString(randoCalrisian) + "\" name=\"TextShape " + Integer.toString(randoCalrisian) + "\"/>" +
    "<p:cNvSpPr txBox=\""+ Integer.toString(1) + "\"/>" +//do I have to change this part?
    "<p:nvPr/>" +
    "</p:nvSpPr>" +
    "<p:spPr>" +
    "<a:xfrm>" +
    "<a:off x=\"" + Integer.toString(convertedX) + "\" y=\"" + Integer.toString(convertedY) + "\"/>" +
    "<a:ext cx=\"" + Integer.toString(calculatedWidth) + "\" cy=\"546680\"/>" +
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
    "<a:rPr lang=\"en-US\" sz=\"" + Integer.toString(fontSize) + "00\">" +
    "<a:solidFill>" +
    "<a:srgbClr val=\"" + fontColor + "\"/>" +
    "</a:solidFill>" +
    "<a:latin typeface=\"" + Font + "\"/>" +
    "</a:rPr>" +
    "<a:t>" + Text + "</a:t>" +
    "</a:r>" +
    "<a:endParaRPr/>" +
    "</a:p>" +
    "</p:txBody>" +
    "</p:sp>";

    return TextBoxXML;

    }

    public String Red = "FF0000";
    public String White = "FFFFFF";
    public String Black = "000000";
    public String Blue = "0000FF";
    public String Green = "00FF00";
    
}
