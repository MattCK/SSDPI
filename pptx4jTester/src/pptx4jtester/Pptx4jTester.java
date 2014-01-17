/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pptx4jtester;

/**
 *
 * @author matt
 */
import java.io.File;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.pptx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.*;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.relationships.Relationship;
import org.pptx4j.model.SlideSizesWellKnown;
import org.pptx4j.pml.CTBackground;
import org.pptx4j.pml.Pic;
import org.pptx4j.pml.Shape;
public class Pptx4jTester {
    
    private PresentationMLPackage presentationPackage;
    private MainPresentationPart pp;
    private SlideLayoutPart layoutPart;
    private SlidePart currentSlide;
    private int currentSlideIDCounter;
    private int slideCounter;

    private boolean debug = true;
    private void dbgmsg(String Msg)
    {
        if (this.debug)
        {
            System.out.println("Debug: " + Msg);
        }
    }
    //input of string which specifices slide size
    //acceptable inputs are "4x3" and "16x9"
    //if neither one of those is given 4x3 is the default
    public boolean CreatePresentation(String size)
    {
        boolean presentationCreated = false;
        
        try
        {
            if(size == "16x9")
            {
                // Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
                //changeslidesizeswell known . to whatever to set slide size
                this.presentationPackage = PresentationMLPackage.createPackage(SlideSizesWellKnown.SCREEN16x9,true);
            }
            else
            {
                this.presentationPackage = PresentationMLPackage.createPackage(SlideSizesWellKnown.SCREEN4x3,true);
            }
            // Need references to these parts to create a slide
            // Please note that these parts *already exist* - they are
            // created by createPackage() above.  See that method
            // for instruction on how to create and add a part.
            this.pp = (MainPresentationPart)this.presentationPackage.getParts().getParts().get(
                        new PartName("/ppt/presentation.xml"));
            this.layoutPart = (SlideLayoutPart)this.presentationPackage.getParts().getParts().get(
                        new PartName("/ppt/slideLayouts/slideLayout1.xml"));
            this.slideCounter = 1;
            
            presentationCreated = true;
        }
        catch(InvalidFormatException e)
        {
            this.dbgmsg("Unable to create presentation package");
        }
        
        
        return presentationCreated;
        
    }
    public boolean addnewslide()
    {
        try
        {
            this.currentSlide = this.presentationPackage.createSlidePart(pp,
            layoutPart, new PartName("/ppt/slides/slide" + Integer.toString(slideCounter) + ".xml"));
            this.slideCounter++;
        }
        catch(JAXBException | InvalidFormatException e)
        {
            
        }
        return true;
    }
    
    public boolean setBackgroundOfCurrentSlide(String imagePath)
    {
        boolean slideCreated = false;
        try
        {
          File file = new File(imagePath);

          BinaryPartAbstractImage imagePartBG = BinaryPartAbstractImage
                .createImagePart(this.presentationPackage, this.currentSlide, file);
          Relationship rel = this.currentSlide.addTargetPart(imagePartBG);
          java.util.HashMap<String, String> mappingsBG = new java.util.HashMap<String, String>();
          mappingsBG.put("rID", rel.getId());
          Object oBG = org.docx4j.XmlUtils.unmarshallFromTemplate(BACKGROUND,
                mappingsBG, Context.jcPML, CTBackground.class);

          this.currentSlide.getJaxbElement().getCSld().setBg((CTBackground) oBG);
          slideCreated = true;
        }
        catch(Exception e3)
        {
            this.dbgmsg("Unable to set image background");
        }
        return slideCreated;
        
    }
    
    public boolean SaveFile(String savePath)
    {
        boolean savedFile = false;
        try
        {
            this.presentationPackage.save(new java.io.File(savePath));
            savedFile = true;
        }
        catch(Docx4JException e4)
        {
            this.dbgmsg("Unable to save presentation file");
        }
        return savedFile;
    }
    //this XMLString is the full xml that gets inserted into the slide
    // it is most easily created with the TextBoxDetails Class and buildXMLTextBoxString function
    public boolean addTextBoxToCurrentSlide(String textBoxXML)
    {
        boolean textBoxAdded = false;
        try
        {
        // Create and add shape
        Shape sample = ((Shape)XmlUtils.unmarshalString(textBoxXML, Context.jcPML) );
        this.currentSlide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(sample);
        textBoxAdded = true;
        }
        catch(Exception e)
        {
            this.dbgmsg("Failed to add textbox");
        }
        return textBoxAdded;
    }
    public boolean addScreenShotToCurrentSlide(String filePath)
    {
        boolean screenShotAdded = false;
        
    }
    private static String BACKGROUND="<p:bg xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"><p:bgPr><a:blipFill dpi=\"0\" rotWithShape=\"1\"><a:blip r:embed=\"${rID}\" cstate=\"print\"><a:lum/></a:blip><a:srcRect/><a:stretch><a:fillRect/></a:stretch></a:blipFill><a:effectLst/></p:bgPr></p:bg>";
    private static final String SAMPLE_PICTURE = 			
          "<p:pic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"> "
            + "<p:nvPicPr>"
              + "<p:cNvPr id=\"${id1}\" name=\"${name}\" descr=\"${descr}\"/>"
              + "<p:cNvPicPr>"
                + "<a:picLocks noChangeAspect=\"1\"/>"
              + "</p:cNvPicPr>"
              + "<p:nvPr/>"
            + "</p:nvPicPr>"
            + "<p:blipFill>"
              + "<a:blip r:embed=\"${rEmbedId}\" cstate=\"print\"/>"
              + "<a:stretch>"
                + "<a:fillRect/>"
              + "</a:stretch>"
            + "</p:blipFill>"
            + "<p:spPr>"
              + "<a:xfrm>"
                + "<a:off x=\"${offx}\" y=\"${offy}\"/>"
                + "<a:ext cx=\"${extcx}\" cy=\"${extcy}\"/>"
              + "</a:xfrm>"
              + "<a:prstGeom prst=\"rect\">"
                + "<a:avLst/>"
              + "</a:prstGeom>"
            + "</p:spPr>"
          + "</p:pic>";
        private static String SAMPLE_SHAPE = 
        "<p:sp   xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">" +
        "<p:nvSpPr>" +
        "<p:cNvPr id=\"34\" name=\"TextShape 1\"/>" +
        "<p:cNvSpPr txBox=\"1\"/>" +
        "<p:nvPr/>" +
        "</p:nvSpPr>" +
        "<p:spPr>" +
        "<a:xfrm>" +
        "<a:off x=\"8890000\" y=\"4953000\"/>" +
        "<a:ext cx=\"5303520\" cy=\"346680\"/>" +
        "</a:xfrm>" +
        "<a:prstGeom prst=\"rect\">" +
        "<a:avLst/>" +
        "</a:prstGeom>" +
        "</p:spPr>" +
        "<p:txBody>" +
        "<a:bodyPr bIns=\"45000\" lIns=\"90000\" rIns=\"90000\" tIns=\"45000\" wrap=\"none\"/>" +
        "<a:p>" +
        "<a:r>" +
        "<a:rPr lang=\"en-US\" sz=\"3600\">" +
        "<a:solidFill>" +
	"<a:srgbClr val=\"dc2300\"/>" +
	"</a:solidFill>" +
	"<a:latin typeface=\"Blackoak Std\"/>" +
        "</a:rPr>" +
        "<a:t>TextBox1</a:t>" +
        "</a:r>" +
        "<a:endParaRPr/>" +
        "</a:p>" +
        "</p:txBody>" +
        "</p:sp>";
    

}
