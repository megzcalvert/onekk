package org.wheatgenetics.imageprocess;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import org.wheatgenetics.imageprocess.ImgProcess1KK;

public class Test1KK {
	
	public static void main(String[] args) {
	    System.out.println("Testing 1KK");

	    // run the ImgProcess code here...
	    
	    String inputFILE = "C:\\Users\\Trevor\\Desktop\\photoA.tif";
	    // Filter Image 
	    ImgProcess1KK imgP = new ImgProcess1KK(inputFILE, 1);
	    
	    imgP.writeProcessedImg("C:\\Users\\Trevor\\Desktop\\photoA_filter.tif");
	    
	    //System.out.println(String.format("Image size:" + image.size()));
	    
	    System.out.println("\nFINISHED!");

	}

}
