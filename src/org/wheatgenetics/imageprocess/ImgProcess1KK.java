package org.wheatgenetics.imageprocess;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.utils.*;
import org.wheatgenetics.imageprocess.ImgProcess1KK.Seed;

public class ImgProcess1KK {

	private String imageFILE; 
	private org.opencv.core.Mat image;
	private Scalar filterGreen[] = {new Scalar(0,0,0), new Scalar(60,255,255)};
	private Scalar filterBlue[] = {new Scalar(100,0,0), new Scalar(179,255,255)};
	
	private List<MatOfPoint> refContours = new ArrayList<MatOfPoint>();
	private double refDiam; 
	
	private double pixelSize = 0; // pixel size in mm
	
	private double expLWR; // expected seed length to width ratio
	private double minCirc; // expected minimum circularity of the seed
	private double minSize;
	private ArrayList<Seed> seedArray = new ArrayList<Seed>();
	
	public ImgProcess1KK(String inputFILE){
		System.out.println("WARNING: Reference diameter has not been set. \n" + "Ref Diameter: "+refDiam);
		imageFILE = inputFILE;
		
		this.initialize();
		this.processImage();
	}
	
	public ImgProcess1KK(String inputFILE, double refDiameter, double expLenWdthR, double minCircularity, double minSeedSize){
    	double start = System.currentTimeMillis();
    	
    	imageFILE = inputFILE;
		refDiam = refDiameter;
		expLWR = expLenWdthR;
		minCirc = minCircularity;
		minSize = minSeedSize;
		
		this.initialize();
		this.processImage();
    	
		double time = (System.currentTimeMillis() - start) / 1000 ;
    	System.out.println("\n Processed in : " + time + " sec"); 
	}
	
	
	private void initialize(){
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load the native library.
		
				
		image = Highgui.imread(imageFILE);
		System.out.println(String.format("Processing %s", imageFILE));
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);
	}
	
	public void processImage(){
		this.setReference();
		this.measureSeeds();
		this.countSeeds();
	}
	 
	
	/**
	 * Set reference circle diameter and pixel abs size
	 * <p>
	 * Image must be initialized and converted to HSV  
	 *
	 * @param  
	 * @see    
	 */	
	private void setReference(){
		System.out.println("Setting references...");
		Mat imgBLUE = this.filterBlue(image);
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    Imgproc.findContours(imgBLUE, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
	    
	    List<RotatedRect> minRect = new ArrayList<RotatedRect>();
	    List<RotatedRect> minEllipse = new ArrayList<RotatedRect>();
	    
	    for(int i=0; i<contours.size(); i++){
	    	MatOfPoint tmp = new MatOfPoint(contours.get(i).toArray());
	    	MatOfPoint2f tmp2f = new MatOfPoint2f(contours.get(i).toArray());
	    	
	    	if(tmp.toArray().length >10){
	    		RotatedRect rec = Imgproc.minAreaRect(tmp2f);
	    		RotatedRect elp = Imgproc.fitEllipse(tmp2f);
	    		double circ = 4*Math.PI*Imgproc.contourArea(tmp) / Math.pow(Imgproc.arcLength(tmp2f, true),2); // calculate circularity
	    		double h = rec.boundingRect().height;
	    		double w = rec.boundingRect().width;
	    		double h2w = Math.max(h,w)/Math.min(h,w);
	    		if (circ>0.85 & h2w<1.1){
	    			refContours.add(tmp);
	    		}
	    	}
	    }
	    
	    // find the average width and height // divide by reference circle size
	    double sum = 0;
	    for(int i=0; i<refContours.size(); i++){
	    	MatOfPoint2f ref2f = new MatOfPoint2f();
	    	refContours.get(i).convertTo(ref2f, CvType.CV_32FC2);
	    	RotatedRect rec = Imgproc.minAreaRect(ref2f);
	    	sum += rec.boundingRect().height + rec.boundingRect().width;
	    }
	    double avgRef = sum / refContours.size() / 2;
	    pixelSize = refDiam / avgRef ;  // TODO check if this is calculated correctly
	    System.out.println("Measured " + refContours.size() + " reference circles.");
	    System.out.println("REFERENCE PIXEL SIZE: " + pixelSize);
	}

	/**
	 * Count seeds.  Color filter is followed by erosion and watershed. 
	 * <p>
	 * Image must be initialized and converted to HSV  
	 *
	 * @param  
	 * @see    
	 */	
	private void countSeeds(){	
		System.out.println("Counting seeds...");
		/*Mat imgNG = this.filterGreen(image);
		Imgproc.erode(imgNG,imgNG,new Mat(),new Point(-1,-1),4);
		Mat imgNGdt = imgNG.clone();
		Imgproc.distanceTransform(imgNG, imgNGdt, 2, 3);
		Highgui.imwrite("/Users/jpoland/Dropbox/1KK/photoA_FILTER2.tif", imgNGdt);
	    
	    Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    Imgproc.findContours(imgNG, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
	    Mat markers = imgNG.clone();
	    markers.setTo(new Scalar(0,0,0));
	    Imgproc.drawContours(markers, contours, -1, new Scalar(255,255,255), 3);
		
	    //Imgproc.watershed(imgNG, markers);
	    //Imgproc.findContours(imgNG, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
	    */
		

        //Imgproc.threshold(threeChannel, threeChannel, 100, 255, Imgproc.THRESH_BINARY);
		
		Mat imgNG = this.filterGreen(image);
		
        //Mat fg = new Mat(imgNG.size(),CvType.CV_32FC1);
        //Imgproc.erode(imgNG,fg,new Mat(),new Point(-1,-1),2);

        //Mat bg = new Mat(imgNG.size(),CvType.CV_32FC1);
        //Imgproc.dilate(imgNG,bg,new Mat(),new Point(-1,-1),3);
        //Imgproc.threshold(bg,bg,1,128,Imgproc.THRESH_BINARY_INV);

        //Mat markers = new Mat(imgNG.size(),CvType.CV_32FC1, new Scalar(0));
        //Core.add(fg, bg, markers);

        //WatershedSegmenter segmenter = new WatershedSegmenter();
        //segmenter.setMarkers(markers);
        //Mat result = segmenter.process(mRgba);


        
        /*
        Mat markers = imgNG.clone();
        for(int i=0; i<25; i++){
        	Mat m2 = markers.clone();
        	Imgproc.erode(markers,m2,new Mat(),new Point(-1,-1),1);
        	markers = m2;
        	Core.multiply(markers, m2, m2);
        	Core.divide(255, m2, m2);
        	Core.subtract(imgNG, m2, imgNG);
        }
        */

        /*
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
	    Imgproc.findContours(markers, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
	    Mat mrk = new Mat(markers.size(),CvType.CV_32SC1, new Scalar(0));
	    for(int i=0; i<contours.size(); i++){
	    	Imgproc.drawContours(mrk, contours, i, new Scalar(i+1) , -1);
	    }
	    */
		
        //Mat markers = imgNG.clone();
        Imgproc.erode(imgNG, imgNG, new Mat(),new Point(-1,-1),2);
        
		Mat dt = new Mat();
		Imgproc.distanceTransform(imgNG, dt, Imgproc.CV_DIST_L2, 3); // Imgproc.CV_DIST_MASK_PRECISE
		Core.normalize(dt, dt, 0, 255, Core.NORM_MINMAX);
		Imgproc.threshold(dt, dt, 80, 255, Imgproc.THRESH_BINARY);
	    
		Mat img = Highgui.imread(imageFILE);
        Mat threeChannel = img.clone();
        Imgproc.cvtColor(threeChannel, threeChannel, Imgproc.COLOR_BGR2GRAY);
        
        //Imgproc.watershed(image, mrk);
		
        Highgui.imwrite("/Users/jpoland/Dropbox/1KK/photoB_watershed.tif", threeChannel);
        
        //List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    //Imgproc.findContours(imgNG, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
	    //System.out.println("NUMBER OF SEEDS: " + contours.size());
	    
	    
	    
	}
	
	
	
	/**
	 * Find seeds based on color filter and expected shape
	 * Image must be initialized and converted to HSV  
	 *
	 * @param  
	 * @see    
	 */	
	private void measureSeeds(){
		System.out.println("Measuring seeds...");
		Mat imgNG = this.filterGreen(image);
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    Imgproc.findContours(imgNG, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
	    
	    List<RotatedRect> minRect = new ArrayList<RotatedRect>();
	    List<RotatedRect> minEllipse = new ArrayList<RotatedRect>();
	    
	    for(int i=0; i<contours.size(); i++){
	    	MatOfPoint2f tmp = new MatOfPoint2f(contours.get(i).toArray());
	    	
	    	if(tmp.toArray().length >10){
	    		Seed s = new Seed(tmp);
	    		if(s.isCanonical){
	    			seedArray.add(s);
	    			
	    		}
	    	}
	    	
	    }
	    
	    System.out.println("NUMBER OF SEEDS MEASURED: " + seedArray.size());
	}

	
	/**
	 * Filters input image for blue
	 * <p>
	 * Scalar reference in HSB color space.  
	 *
	 * @param  img  input Mat to be filtered 
	 * @see    OpenCV Scalar
	 */
	private Mat filterBlue(Mat img){   
		//Threshold the image	
		Mat imgFilter = img.clone();
	    Core.inRange(img, filterBlue[0], filterBlue[1], imgFilter); 
		return imgFilter;
	}
	
	/**
	 * Filters input image for green
	 * <p>
	 * Scalar reference in HSB color space.  
	 *
	 * @param  img  input Mat to be filtered 
	 * @see    OpenCV Scalar
	 */
	private Mat filterGreen(Mat img){
	    //Threshold the image	
		Mat imageFilter = img.clone();
	    Core.inRange(img, filterGreen[0], filterGreen[1], imageFilter); 
		return imageFilter;
	}
	
	
	
	/**
	 * Sets the blue level filter for masking image.  
	 * <p>
	 * Scalar reference in HSB color space.  
	 *
	 * @param  low	three value Scalar giving HSB values for lower filter threshold
	 * @param  high three value Scalar giving HSB values for upper filter threshold
	 * @see    OpenCV Scalar
	 */
	public void setBlueFilter(Scalar low, Scalar high){
		this.filterBlue[0] = low;
		this.filterBlue[1] = high;
	}
	
	/**
	 * Sets the green level filter for masking image.  
	 * <p>
	 * Scalar reference in HSB color space.  
	 *
	 * @param  low	three value Scalar giving HSB values for lower filter threshold
	 * @param  high three value Scalar giving HSB values for upper filter threshold
	 * @see    OpenCV Scalar
	 */
	public void setGreenFilter(Scalar low, Scalar high){
		this.filterGreen[0] = low;
		this.filterGreen[1] = high;
	}
	
	/**
	 * Returns a false color image with the reference and seeds colored  
	 *
	 * @return    OpenCV Mat
	 */
	
	public Mat getProcessedImg(){
		Mat pImg = image.clone();
		Imgproc.cvtColor(pImg, pImg, Imgproc.COLOR_HSV2BGR);
		//pImg.setTo(new Scalar(255,255,255));
		Scalar blue = new Scalar(255, 0, 0);
		Scalar purple = new Scalar(255, 0, 155);
		Scalar green = new Scalar(0, 255, 0);
		Scalar red = new Scalar(50, 0, 255);
		Scalar orange = new Scalar(0, 50, 255);
		Scalar white = new Scalar(255, 255, 255);
		Scalar black = new Scalar(0, 0, 0);
		//Scalar color = new Scalar(Math.random()*255, Math.random()*255, Math.random()*255);
		
		Imgproc.drawContours(pImg, refContours, -1, purple, 3);
		
		
		List<MatOfPoint> seeds = new ArrayList<MatOfPoint>();
		for(int i=0; i<seedArray.size(); i++){
			seeds.add(seedArray.get(i).perm);
		}
		Imgproc.drawContours(pImg, seeds, -1, orange, 2);	

		
		//for(int i=0; i<seedArray.size(); i++){
		//	Core.ellipse(pImg, seedArray.get(i).elp, green, 3);
		//}
		
		// Draw width vector, length vector and CG point
		for(int i=0; i<seedArray.size(); i++){
			// draw width vector
			Core.line(pImg, seedArray.get(i).ptsW[0], seedArray.get(i).ptsW[1], blue, 2);
			
			// draw length vector
			Core.line(pImg, seedArray.get(i).ptsL[0], seedArray.get(i).ptsL[1], green, 2);
			
			// draw CG point
			Core.circle(pImg, seedArray.get(i).centgrav, 2, white, 2);
			
			// draw intLW point
			Core.circle(pImg, seedArray.get(i).intLW, 2, black, 2);
		}
		
		// Draw length vector
		for(int i=0; i<seedArray.size(); i++){
			
		}
		
    	return pImg;
	}
	
	/**
	 * Writes a false color image with the reference and seeds colored to the specified image path 
	 *
	 * @param String filename giving the full file path string of the file to write
	 * @see OpenCV Highgui.imwrite
	 */
	
	public void writeProcessedImg(String filename){
	    System.out.println(String.format("\nWriting %s", filename));
	    Highgui.imwrite(filename, this.getProcessedImg());
	    //Highgui.imwrite(filename, this.filterGreen(image));
	}
	
	
	public class Seed{
		private double length; 
		private double width; 
		private double circ;
		private double lwr; // length to width ratio
		private Point centgrav = new Point(); // center of gravity
		private Point intLW = new Point(); //intersection of lenght and width vector
		private double ds; // distance between centgrav and intLW;
		private double tolerance = 0.2;
		private boolean isCanonical = false;
		// TODO add factors for expected size and filter accordingly
		private MatOfPoint2f seedMat = new MatOfPoint2f();
		private MatOfPoint perm = new MatOfPoint();
		private RotatedRect rec = new RotatedRect();
		private RotatedRect elp = new RotatedRect();
		private Point[] ptsL = new Point[2];
		private Point[] ptsW = new Point[2];
		
	    /**
	     * Class to hold seed matrix array and descriptors
	     * 
	     * Seeds can be filtered from broken pieces, trash and other material by checking isCanonical.  
	     * isCanonical = 
	     *
	     * @param mat MatOfPoint2f giving the perimeter of the seed
	     * @param l2w expected length to width ratio of the seed
	     * @param c expected circularity of the seed
	     */
		
		public double getLength() {
			return length;
		}
		
		public double getWidth() {
			return width;
		}
		
		public double getCirc() {
			return circ;
		}
		
		public Seed(MatOfPoint2f mat){
			seedMat = mat;
			mat.convertTo(perm, CvType.CV_32S);
			
    		rec = Imgproc.minAreaRect(mat);
    		elp = Imgproc.fitEllipse(mat);
    		circ = 4*Math.PI*Imgproc.contourArea(mat) / Math.pow(Imgproc.arcLength(mat, true),2); // calculate circularity
    		//length = Math.max(rec.boundingRect().height, rec.boundingRect().width);
    		//width = Math.min(rec.boundingRect().height, rec.boundingRect().width); 
    		//lwr = length/width;
    		this.calcCG();
    		this.findMaxVec();
    		this.findIS();
    		
			if(this.checkCanonical()){
			//if(this.checkElp()){
				isCanonical = true;
			}
    		
		}
		
		/**
		 * Find the center of gravity by averaging all points in the perimeter   
		 * As described by Tanabata, T., T. Shibaya, K. Hori, K. Ebana and M. Yano (2012) "SmartGrain: high-throughput phenotyping software for measuring seed shape through image analysis." Plant physiology 160(4): 1871-1880.
		 * 
		 */
		private void calcCG(){	
			double sumX = 0;
			double sumY = 0;
			Point[] permArray = perm.toArray();
			for(int i=0; i<permArray.length; i++){
				sumX += permArray[i].x;
				sumY += permArray[i].y;
		
			}
			centgrav.x = Math.round(sumX/permArray.length);
			centgrav.y = Math.round(sumY/permArray.length);
		}
		
		/**
		 * Find the end-point coordinates of the maxium vector in the seed  
		 * As described by Tanabata, T., T. Shibaya, K. Hori, K. Ebana and M. Yano (2012) "SmartGrain: high-throughput phenotyping software for measuring seed shape through image analysis." Plant physiology 160(4): 1871-1880.
		 * 
		 */
		private void findMaxVec(){
			Point[] permArray = perm.toArray();
			for(int i=0; i<permArray.length; i++){
				for(int j=i; j<permArray.length; j++){
					Point p1 = permArray[i];
					Point p2 = permArray[j];
					double l = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
					if (l>length){
						length = l;
						ptsL[0]=p1;
						ptsL[1]=p2;
					}			
				}	
			}
			

			double slopeL = ((ptsL[1].y - ptsL[0].y) / (ptsL[1].x - ptsL[0].x));  // TODO not sure this works for infinite slope
			
			for(int i=0; i<permArray.length; i++){
				double d = 1;
				Point p1 = permArray[i];
				Point p2 = permArray[i];
				//double slp = 0;
				for(int j=0; j<permArray.length; j++){
					
					double s =  slopeL * ((p1.y - permArray[j].y) / (p1.x - permArray[j].x));  
					if (Math.abs(s+1)<d){
						d = Math.abs(s+1);  
						p2 = permArray[j];
					}	
				}	
				
				double w = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
				if(w>width){
					width = w;
					ptsW[0]=p1;
					ptsW[1]=p2;
				}
			}
			lwr = length / width;
		}
		
		/**
		 * Find the intersection of max length and max width vector  
		 * As described by Tanabata, T., T. Shibaya, K. Hori, K. Ebana and M. Yano (2012) "SmartGrain: high-throughput phenotyping software for measuring seed shape through image analysis." Plant physiology 160(4): 1871-1880.
		 * 
		 */
		private void findIS(){
			if(ptsW[0]==null){return;}; // exit method if width is null
			double S1 = 0.5*((ptsW[0].x - ptsW[1].x)*(ptsL[0].y - ptsW[1].y)-(ptsW[0].y - ptsW[1].y)*(ptsL[0].x - ptsW[1].x));
			double S2 = 0.5*((ptsW[0].x - ptsW[1].x)*(ptsW[1].y - ptsL[1].y)-(ptsW[0].y - ptsW[1].y)*(ptsW[1].x - ptsL[1].x));
			intLW.x = ptsL[0].x + S1*(ptsL[1].x -ptsL[0].x)/(S1+S2);
			intLW.y = ptsL[0].y + S1*(ptsL[1].y -ptsL[0].y)/(S1+S2); 
			
			ds = Math.sqrt(Math.pow(intLW.x - centgrav.x,2) + Math.pow(intLW.y - centgrav.y,2));
			
		}
		
		
		/**
		 * Returns the maximum vector length 
		 * As described by Tanabata, T., T. Shibaya, K. Hori, K. Ebana and M. Yano (2012) "SmartGrain: high-throughput phenotyping software for measuring seed shape through image analysis." Plant physiology 160(4): 1871-1880.
		 * @return  double giving the maximum vector length
		 */
		private double getMaxVec(){	
			return Math.sqrt(Math.pow(ptsL[0].x - ptsL[1].x, 2) + Math.pow(ptsL[0].y - ptsL[1].y, 2));	
		}
		
		/**
		 * Runs multiple checks to determine if the shape blob represents a canonical seed shape
		 *
		 */
		private boolean checkCanonical(){
			/*
			if(this.checkCirc() & this.checkElp() & this.length>minSize & this.checkL2W()){
				return true;
			}
			else{return false;}
			*/
			if(this.length<minSize){
				return false;
			}
			if(!this.checkCirc()){
				return false;
			}
			if(!this.checkElp()){
				return false;
			}
			else{return true;}
			
		}
		
		/**
		 * Checks and expected circularity value is within the expected circularity range  
		 *
		 * @param  c	double giving the circularity value to check
		 */
		private boolean checkCirc(){
			if(minCirc<circ){
				return true;
			}
			else{return false;}
		}
		
		/**
		 * Checks the object is roughly an eliptical shape.  Will filter blobs of two or more seeds
		 * 
		 * Not sure this is working correctly due to approximation formula for circumference of ellipse.
		 *
		 */
		private boolean checkElp(){
			
			double a = elp.boundingRect().height/2;
			double b = elp.boundingRect().width/2;
			double c = 2*Math.PI * Math.sqrt((Math.pow(a,2) + Math.pow(b,2))/ 2); // TODO this is the approximation formula for circumference of an ellipse - FIGURE OUT IF IT IS WORKING
			double p = Imgproc.arcLength(seedMat, true);
			//System.out.println(c + "\t" + p);
			
			if(p < 1.1*c){
				return true;
			}
			else{return false;}
		}
		
		
		/**
		 * Checks and expected length to width ratio is within a tolerance limit // DEFAULT SET TO 30%
		 *
		 * @param  l2w	double giving the expected value to check for length to width ratio
		 */
		private boolean checkL2W(){
			if(lwr>expLWR*(1-tolerance) & lwr<expLWR*(1+tolerance)){
				return true;
			}
			else{return false;}
		}
		
		public void setTolerance(double t){
			tolerance = t;
		}
		
		
	}


	public ArrayList<Seed> getList() {
		return seedArray;
	}
	
	
}
