/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package adshotrunner.techpreview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
/**
 *
 * @author matt
 */
import java.io.File;
import javax.xml.bind.JAXBException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

//import org.apache.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Log4jConfigurer;
import org.docx4j.Docx4jProperties;
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

public class PowerPointXMLGenerator {
    
    private PresentationMLPackage presentationPackage;
    private MainPresentationPart pp;
    private SlideLayoutPart layoutPart;
    private SlidePart currentSlide;
    private int currentSlideIDCounter;
    private int slideCounter;
    //private Object backGroundImage;
    private BinaryPartAbstractImage backgroundImage;

    private boolean debug = true;
    private void dbgmsg(String Msg)
    {
        if (this.debug)
        {
            System.out.println("Debug: " + Msg);
        }
    }
    //input of string which specifies slide size
    //acceptable inputs are "4x3" and "16x9"
    //if neither one of those is given 4x3 is the default
    public boolean CreatePresentation(String size)
    {
        boolean presentationCreated = false;
        Docx4jProperties.getProperties().setProperty("docx4j.Log4j.Configurator.disabled", "true");
        
        try
        {
            if(size == "16x9")
            {
                // Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
                //change slidesizeswell known . to whatever to set slide size
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
    
    public boolean setBackgroundOfCurrentSlide()
    {
        boolean slideCreated = false;
        try
        {
          Relationship rel = this.currentSlide.addTargetPart(this.backgroundImage);
          java.util.HashMap<String, String> mappingsBG = new java.util.HashMap<String, String>();
          mappingsBG.put("rID", rel.getId());
          Object BGImage = org.docx4j.XmlUtils.unmarshallFromTemplate(BACKGROUND,
                mappingsBG, Context.jcPML, CTBackground.class);
          this.currentSlide.getJaxbElement().getCSld().setBg((CTBackground) BGImage);
          slideCreated = true;
        }
        catch(Exception e3)
        {
            this.dbgmsg("Unable to set image background");
        }
        return slideCreated;
        
    }
    public boolean setBackgroundOfFirstSlide(String imagePath)
    {
        boolean slideCreated = false;
        try
        {
          File file = new File(imagePath);

          this.backgroundImage = BinaryPartAbstractImage
                .createImagePart(this.presentationPackage, this.currentSlide, file);
          Relationship rel = this.currentSlide.addTargetPart(this.backgroundImage);
          java.util.HashMap<String, String> mappingsBG = new java.util.HashMap<String, String>();
          mappingsBG.put("rID", rel.getId());
          Object BGImage = org.docx4j.XmlUtils.unmarshallFromTemplate(BACKGROUND,
                mappingsBG, Context.jcPML, CTBackground.class);
          this.currentSlide.getJaxbElement().getCSld().setBg((CTBackground) BGImage);

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
    // this expects a png as input
    public boolean addScreenShotToCurrentSlide(BufferedImage ScreenshotImg, int x, int y, int height, int width)
    {
        boolean screenShotAdded = false;
        
        //byte[] ScreenshotBytes = ((DataBufferByte) ScreenshotImg.getData().getDataBuffer()).getData();
        ByteArrayOutputStream ScreenshotBuffer = null;
        try {
        	ScreenshotBuffer = new ByteArrayOutputStream();
            try {
				ImageIO.write(ScreenshotImg, "png", ScreenshotBuffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } finally {
            try {
            	
            	ScreenshotBuffer.close();
            } catch (Exception e) {
            }
        }
        byte[] ScreenshotBytes = ScreenshotBuffer.toByteArray();

        try{
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(presentationPackage, currentSlide, ScreenshotBytes);
        this.dbgmsg("After creatingImagePart");
        PictureDetails picture1 = new PictureDetails();
        picture1.relationshipID = imagePart.getSourceRelationship().getId();
        picture1.x = x;
        picture1.y = y;
        picture1.width = width;
        picture1.height = height;
        String pictureStringXML = picture1.BuildXMLPictureString();
        this.dbgmsg("After buildingXML");
        Object picObject;
            picObject = org.docx4j.XmlUtils.unmarshalString(pictureStringXML, Context.jcPML, Pic.class);
        // Add p:pic to slide
        this.dbgmsg("After creating pic object");
	currentSlide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(picObject);
        
        }
        catch(Exception e)
        {
            this.dbgmsg("Failed to add image to slide");
            e.printStackTrace();
        }
        return screenShotAdded;
    }
    private static final String BACKGROUND="<p:bg xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\"><p:bgPr><a:blipFill dpi=\"0\" rotWithShape=\"1\"><a:blip r:embed=\"${rID}\" cstate=\"print\"><a:lum/></a:blip><a:srcRect/><a:stretch><a:fillRect/></a:stretch></a:blipFill><a:effectLst/></p:bgPr></p:bg>";
        
   
}
