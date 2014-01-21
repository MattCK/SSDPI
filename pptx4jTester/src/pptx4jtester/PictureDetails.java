/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pptx4jtester;

import java.util.Random;

/**
 *
 * @author matt
 */



public class PictureDetails {
    public int x;
    public int y;
    public int height;
    public int width;
    public String relationshipID;

    public String BuildXMLPictureString()
    {
    int convertedX = x * 12700;
    int convertedY = y * 12700;
    int convertedHeight = height * 12700;
    int convertedWidth = width * 12700;
    Random randomGenerator = new Random();
    int randoCalrisian = randomGenerator.nextInt();

    String PictureXML = 			
          "<p:pic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"> "
            + "<p:nvPicPr>"
              + "<p:cNvPr id=\"" + Integer.toString(randoCalrisian) + "\" name=\"Picture" + Integer.toString(randoCalrisian) + "\" descr=\"Description" + Integer.toString(randoCalrisian) + "\"/>"
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
                + "<a:off x=\"" + Integer.toString(convertedX) + "\" y=\"" + Integer.toString(convertedY) + "\"/>"
                + "<a:ext cx=\"" + Integer.toString(convertedWidth) + "\" cy=\"" + Integer.toString(convertedHeight) + "\"/>"
              + "</a:xfrm>"
              + "<a:prstGeom prst=\"rect\">"
                + "<a:avLst/>"
              + "</a:prstGeom>"
            + "</p:spPr>"
          + "</p:pic>";

    return PictureXML;

    }
    
}
