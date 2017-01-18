package adshotrunner.powerpoint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.relationships.Relationship;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.SlideSizesWellKnown;
import org.pptx4j.pml.CTBackground;
import org.pptx4j.pml.Pic;
import org.pptx4j.pml.Shape;

class PowerPoint {

	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	final private static SlideSizesWellKnown DEFAULTRATIO = SlideSizesWellKnown.SCREEN16x9;	//Slide ratio (16x9)

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Variables *********************************

	//**************************** Protected Static Variables *******************************


	//***************************** Private Static Variables ********************************

	//Prefix with underscore: _myVariable

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************


	//**************************** Protected Static Methods *********************************


	//***************************** Private Static Methods **********************************


	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Public Variables *************************************
	
	

	//******************************* Protected Variables ***********************************


	//******************************** Private Variables ************************************
	/**
	 * The overall PPTx package
	 */
	private PresentationMLPackage _package;
	
	/**
	 * The primary PPTx part that contains all the other child parts
	 */
	private MainPresentationPart _mainPart;
	
	/**
	 * The layout part is used to add new slides
	 */
	private SlideLayoutPart _layoutPart;
	
	/**
	 * Image file to use for the presentation background
	 */
	private File _backgroundImage;

	/**
	 * Background image part. Inserted into every slide. 
	 */
	private BinaryPartAbstractImage _backgroundImagePart;

	/**
	 * List of slides inside the PowerPoint in insertion order
	 */
	private ArrayList<SlidePart> _slides;
	
	


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	public PowerPoint(File backgroundImage) throws InvalidFormatException {
		
		//Create the new containing package that will hold the slides and all other parts 
		//Set the default ratio and flag that landscape should be used
        _package = PresentationMLPackage.createPackage(DEFAULTRATIO, true);
        
        //Get the main part and layout part, which are necessary to add a new slide
        _mainPart = (MainPresentationPart)_package.getParts().getParts().get(new PartName("/ppt/presentation.xml"));		
		_layoutPart = (SlideLayoutPart)_package.getParts().getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));

		//Store the background image
		_backgroundImage = backgroundImage;
		
		//Initialize the slides list
		_slides = new ArrayList<SlidePart>();
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	public SlidePart addNewSlide() throws Exception {
		
		//Get the index for the new slide part name
		int slideIndex = _slides.size() + 1;
		
		//Create the new slide part
		SlidePart slide = new SlidePart(new PartName("/ppt/slides/slide" + slideIndex + ".xml"));
		
		//Add the slide part to the presentation's main part. This adds it to the end of the presentation.
		_mainPart.addSlide(slide);
		
		//Initialize the contents of the slide and add the layout to it
		slide.setContents(SlidePart.createSld());		
		slide.addTargetPart(_layoutPart);
		
		//Set the background
		setSlideBackground(slide);
		
		//Add the slide to the list
		_slides.add(slide);
		
		//Return the slide part
		return slide;
	}
	
	public void addImageToSlide(SlidePart slide, BufferedImage image, 
								int xPosition, int yPosition, int width, int height) throws Exception {
		
		//Convert the image to a byte array
		ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", imageStream);
		byte[] convertedImage = imageStream.toByteArray();
		
		//Create the image part
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(_package, slide, convertedImage);
        String relationshipID = imagePart.getSourceRelationship().getId();
        
        //Get the picture XML template
        String pictureTemplate = TemplateCreator.picture(relationshipID, xPosition, yPosition, width, height);
		
		//Add the image to the slide
        Object pictureObject = org.docx4j.XmlUtils.unmarshalString(pictureTemplate, Context.jcPML, Pic.class);
        slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(pictureObject);
	}
	
	public void addTextToSlide(SlidePart slide, String text, int xPosition, int yPosition, int width,
							   String fontType, int fontSize, String fontColor) throws JAXBException {
		
        //Get the text box XML template
        String textBoxTemplate = TemplateCreator.textBox(text, xPosition, yPosition, width, fontType, fontSize, fontColor);
        
        //Add the text box to the slide
        Shape textElement = ((Shape)XmlUtils.unmarshalString(textBoxTemplate, Context.jcPML) );
        slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(textElement);

	}
	
	public void save(String filepath) throws Docx4JException {
		_package.save(new java.io.File(filepath));
	}
	
	//******************************** Protected Methods ************************************


	//********************************* Private Methods *************************************
	private void setSlideBackground(SlidePart slide) throws Exception {
		
		//Insert the background image into the presentation if it does not exist yet
		if (_backgroundImagePart == null) {
			
			//Turn the background image into an image part for the presentation
			_backgroundImagePart = BinaryPartAbstractImage
				.createImagePart(_package, slide, _backgroundImage);

		}
		
		//Create a relationship between the slide and the image
		Relationship imageSlideRelationship = slide.addTargetPart(_backgroundImagePart);
		
		//Create a properties map to specify the new relationship
		java.util.HashMap<String, String> relationshipProperty = new java.util.HashMap<String, String>();
		relationshipProperty.put("rID", imageSlideRelationship.getId());
		System.out.println(imageSlideRelationship.getId());

		//Add the background to the slide
		Object backgroundObject = org.docx4j.XmlUtils.unmarshallFromTemplate(TemplateCreator.background(), relationshipProperty, 
																			 Context.jcPML, CTBackground.class);
		System.out.println(backgroundObject);
		slide.getJaxbElement().getCSld().setBg((CTBackground) backgroundObject);
	}
	
	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
 

	//********************************* Protected Accessors *********************************


	//********************************* Private Accessors ***********************************

}
