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
public class Tester2 {
    
    
    public static void main(String[] args) {
        Pptx4jTester pptx = new Pptx4jTester();
        pptx.CreatePresentation("16x9");
        pptx.addnewslide();
        pptx.setBackgroundOfCurrentSlide("V:\\Work\\Screenshot\\java\\img1.png");
        pptx.addnewslide();
        pptx.setBackgroundOfCurrentSlide("V:\\Work\\Screenshot\\java\\sc2.jpg");
        TextBoxDetails textBox1 = new TextBoxDetails();
        textBox1.Font = "Arial";
        textBox1.Text = "This is a test!";
        textBox1.fontColor = textBox1.Blue;
        textBox1.x = 50;
        textBox1.y = 320;
        textBox1.fontSize = 18;
        String box1XML = textBox1.BuildXMLTextBoxString();
        pptx.addTextBoxToCurrentSlide(box1XML);
        TextBoxDetails textBox2 = new TextBoxDetails();
        textBox2.Font = "Times New Roman";
        textBox2.Text = "This is also a test!";
        textBox2.fontColor = textBox1.White;
        textBox2.x = 50;
        textBox2.y = 220;
        textBox2.fontSize = 12;
        String box2XML = textBox2.BuildXMLTextBoxString();
        pptx.addTextBoxToCurrentSlide(box2XML);
        pptx.addnewslide();
        pptx.setBackgroundOfCurrentSlide("V:\\Work\\Screenshot\\java\\sc3.jpg");
        pptx.SaveFile("V:\\Work\\Screenshot\\java\\pptx4jTester\\pptxFiles\\pptx-picture11.pptx");
        
        
        
        
        
    }
    
}
