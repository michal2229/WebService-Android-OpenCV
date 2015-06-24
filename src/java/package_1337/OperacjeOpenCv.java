/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacv.FrameGrabber;


/**
 *
 * @author Michał
 */
/*public class OperacjeOpenCv {
    public static void main(String[] args) throws IOException, FrameGrabber.Exception {
        out.println("Hello from OpenCV.");
        
        // todo: dorzucic do kodu .mkdirs, zeby nie bylo ze nie ma folderow i nie da sie utworzyc
        // wyniki zawsze w folderze users/nazwaUsera/nazwaProjektu/wyniki - ale users/nazwaUsera/nazwaProjektu bedzie podana, wiec tylko koncowym folderem nalezy sie martwic
        // todo: rozne poprawki zrobic opisane na kartkach
        BazaOperacji.operuj("robocze\\obrazki", "robocze\\wyniki", 2, 1);

        out.println("Bye from OpenCV.");
    }
}*/

public class OperacjeOpenCv {
    static int VID = 0; static int SEQ = 1; static int IMG = 2;
    static int STD = 0; static int STAB = 1; 
    static int iloscBledowOdczytu = 0;
    static int iloscBledowZapisu = 0;
    static int iloscBledowUsredniania = 0;
    static int iloscBledowStabilizowania = 0;
    static opencv_core.Mat bazaDoStabilizacji = null;
    static opencv_core.Mat bazaDoUsredniania = null;
    static opencv_features2d.KeyPoint keypoint1 = new opencv_features2d.KeyPoint();
    static opencv_core.Mat descriptors1 = new opencv_core.Mat();
    static opencv_features2d.FastFeatureDetector detectorFast = new opencv_features2d.FastFeatureDetector();
    static opencv_features2d.ORB detektorOrb = new opencv_features2d.ORB(5000, 1.4f, 8, 31, 0, 2, opencv_features2d.ORB.HARRIS_SCORE, 31); // opisac parametry
    static opencv_features2d.BFMatcher matcherBFM = new opencv_features2d.BFMatcher();
//	static opencv_features2d.FlannBasedMatcher matcherFBM = new opencv_features2d.FlannBasedMatcher();

public static void operuj(String sciezkaZrodlowa, String sciezkaDocelowa, int typWyjscia, int typStabilizacji) throws IOException, org.bytedeco.javacv.FrameGrabber.Exception {
    List<Path> listaPlikow = new ArrayList<>();
    Path nazwaFolderuZrodlowego = Paths.get(sciezkaZrodlowa);
    Path nazwaFolderuDocelowego = Paths.get(sciezkaDocelowa);
    opencv_highgui.CvCapture cvcapt = null;
    opencv_core.Mat baza = null;
    int iloscObrazkow = 0;

    // to bedzie zgadywane na podstawie zawartosci folderu
    int typWejscia = SEQ;

    Files.walk(nazwaFolderuZrodlowego).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
            listaPlikow.add(filePath); //out.println(filePath.toAbsolutePath().toString());
        }
    });

    if (( typWejscia == VID && listaPlikow.size() > 0) || (typWejscia == SEQ && listaPlikow.size() > 1))  {
        if (typWejscia == SEQ) {
        // to bedzie z ifem
            baza = opencv_highgui.imread(listaPlikow.get(listaPlikow.size()/5).toAbsolutePath().toString());
            iloscObrazkow = listaPlikow.size();
        }

        if (typWejscia == VID) { 
        // tu bedzie czytanie klatki bazowej z grabbera z ifem
            cvcapt = opencv_highgui.cvCreateFileCapture(listaPlikow.get(0).toAbsolutePath().toString());
//		        opencv_highgui.cvGrabFrame(cvcapt);
            iloscObrazkow = (int) opencv_highgui.cvGetCaptureProperty(cvcapt,opencv_highgui.CV_CAP_PROP_FRAME_COUNT ) - 1;        
            int iloscPominietych = iloscObrazkow/5;
            for (int i = 0; i < iloscPominietych; i++) opencv_highgui.cvQueryFrame(cvcapt);

            baza = new opencv_core.Mat(opencv_highgui.cvQueryFrame(cvcapt)).clone();

            cvcapt = opencv_highgui.cvCreateFileCapture(listaPlikow.get(0).toAbsolutePath().toString());


        }
//	        opencv_highgui.imwrite(nazwaFolderuDocelowego.toAbsolutePath().toString() + "\\_baza_" + System.currentTimeMillis() + "." + "jpg", baza);


        for (int i = 0; i < iloscObrazkow; i++) {
            out.println("\n\nprzetwarzanie " + i + " z " + iloscObrazkow);
            opencv_core.Mat obrazek = null;
            if (typWejscia == SEQ) {
                Path plik = listaPlikow.get(i);
                out.println("\n\n\n" + plik.getFileName().toString());
                obrazek = opencv_highgui.imread(plik.toAbsolutePath().toString());
//	        	 tu bedzie czytanie klatki z grabbera z ifem
            }
            if (typWejscia == VID) { 
                obrazek = new opencv_core.Mat(opencv_highgui.cvQueryFrame(cvcapt));
            }
            opencv_core.Mat obrazekPoStabilizacji = ustablilizuj(baza, obrazek.clone());
            if (obrazekPoStabilizacji != null) {
                usrednij(obrazekPoStabilizacji.clone(), i);
                // to bedzie z ifem
                // zapis klatki ustabilizowanej z filmu/sekwencji
//		        	opencv_highgui.imwrite(nazwaFolderuDocelowego.toAbsolutePath().toString() + "\\_wynik-stabilizacji_" + System.currentTimeMillis() + "." + "jpg", obrazekPoStabilizacji);
            }
             System.gc(); 
        }
        // to bedzie z ifem
        // zapis obrazka usrednionego
        opencv_highgui.imwrite(nazwaFolderuDocelowego.toAbsolutePath().toString() + "\\_wynik-usredniania_" + System.currentTimeMillis() + "." + "png", bazaDoUsredniania);
    } 
    else {
            out.println("Zbyt malo plikow w folderze '" + sciezkaZrodlowa + "'.");
    }
//        zapiszJakoObrazy("robocze\\filmiki", "robocze\\wydobyte", "png");
//        dopasujWiele("robocze\\wydobyte", "robocze\\dopasowane", "png");
//        usrednijWiele("robocze\\dopasowane", "robocze\\wyniki", "png");
    }
	
	
	
	
	
	
	
	
public static opencv_core.Mat usrednij(opencv_core.Mat im1, int i){
		int j = i + 1 - iloscBledowUsredniania;
		double k = 1.0/j;
		if (im1.channels() == 4) opencv_imgproc.cvtColor(im1, im1, opencv_imgproc.CV_BGRA2BGR);
		
        try {
        	if (bazaDoUsredniania == null) {
        		bazaDoUsredniania = new opencv_core.Mat();
        		new opencv_core.Mat(im1.clone()).convertTo(bazaDoUsredniania, opencv_core.CV_16UC3);
        		
        		
        		opencv_core.Mat macierzRazy257 = new opencv_core.Mat( bazaDoUsredniania.size(), opencv_core.CV_16UC3, new opencv_core.Scalar(257, 257, 257, 257) );
				opencv_core.cvMul(bazaDoUsredniania.asCvMat(), macierzRazy257.asCvMat(), bazaDoUsredniania.asCvMat());
     		
        	}
    		else { 
    			im1.convertTo(im1 , opencv_core.CV_16UC3); 
        		opencv_core.Mat macierzRazy257 = new opencv_core.Mat( im1.size(), opencv_core.CV_16UC3, new opencv_core.Scalar(257, 257, 257, 257) );
				opencv_core.cvMul(im1.asCvMat(), macierzRazy257.asCvMat(), im1.asCvMat());
				
    			opencv_core.cvAddWeighted(im1.asCvMat(), k, bazaDoUsredniania.asCvMat(), 1.0 - k, 0.0, bazaDoUsredniania.asCvMat());
    		}
        } 
        catch (Exception e) {
        	iloscBledowUsredniania++;
            out.println("Blad usredniania - cos nie tak z obrazami w argumentach metody");
        }
		
		return bazaDoUsredniania;
	};
	
	
	
	
public static opencv_core.Mat ustablilizuj(opencv_core.Mat baza, opencv_core.Mat obrazDoStabilizacji){
        try {
        	if (bazaDoStabilizacji == null) {
        		bazaDoStabilizacji = new opencv_core.Mat();
	        	opencv_imgproc.cvtColor(baza, bazaDoStabilizacji, opencv_imgproc.CV_BGR2BGRA);
		        detektorOrb.detect(bazaDoStabilizacji, keypoint1);
		        detektorOrb.compute( bazaDoStabilizacji, keypoint1, descriptors1); 
        	}
//        	opencv_highgui.imwrite("robocze\\debug"+ "\\_baza_" + System.currentTimeMillis() + "." + "jpg", bazaDoStabilizacji);
       	
        	
        	opencv_imgproc.cvtColor(obrazDoStabilizacji, obrazDoStabilizacji, opencv_imgproc.CV_BGR2BGRA);
        	
        	double max_dist = 0; double median_dist = 500; double min_dist = 1000;
//        	opencv_core.Mat debugMat1 = new opencv_core.Mat(obrazDoStabilizacji.clone());
        	opencv_core.Mat debugMat1 = new opencv_core.Mat(obrazDoStabilizacji.size(), opencv_core.CV_8UC4, new opencv_core.Scalar(0, 0, 0, 255));
	        
	        opencv_features2d.KeyPoint keypoint2 = new opencv_features2d.KeyPoint();
	        opencv_features2d.DMatch  matches = new opencv_features2d.DMatch();
	        List<opencv_features2d.DMatch> very_good_matches = new ArrayList<>();
	        int iloscPunktowDoLiczeniaH;
	        opencv_core.CvMat srcPoints;
	        opencv_core.CvMat dstPoints;
	        opencv_core.Mat H;
	        opencv_core.Mat result = new opencv_core.Mat(bazaDoStabilizacji.size(), opencv_core.CV_8UC4);;
	        
//	        opencv_core.cvAddWeighted(debugMat1.asIplImage(), 0.1, debugMat1.asIplImage(), 0.1, 0.0, debugMat1.asIplImage());
	        
	       
	        detektorOrb.detect(obrazDoStabilizacji,keypoint2);
	        
	        //out.println("keypoint1: " + keypoint1.capacity());
	        //out.println("keypoint2: " + keypoint2.capacity());
	        opencv_core.Mat descriptors2 = new opencv_core.Mat();
	        detektorOrb.compute( obrazDoStabilizacji, keypoint2, descriptors2 );

	        
	        matcherBFM.match( descriptors1, descriptors2, matches );
	        for( int i = 0; i < descriptors1.rows(); i++ ) {
	            double dist = matches.position(i).distance();
	            //out.println("dist: " + dist);
	            if( dist < min_dist ) min_dist = dist;
	            
	            if( dist > max_dist ) max_dist = dist;
	        }
	        median_dist =  matches.position(descriptors1.rows()/2).distance();
	        
	        opencv_core.putText( debugMat1,  "dist min: " + min_dist + ", median: " + median_dist + ", max: " + max_dist , new opencv_core.Point(15, 15+15*4), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
	        //out.println("dist min: " + min_dist + ", median: " + median_dist + ", max: " + max_dist);
	        
			int iloscCechPoFiltrowaniu = 0;
	        List<opencv_features2d.DMatch> good_matches = new ArrayList<>();
	        
	        for( int i = 0; i < descriptors1.rows(); i++ ) {
	            if( matches.position(i).distance()  <=  Math.min(median_dist, max_dist/2)  ) { 
	                good_matches.add(new opencv_features2d.DMatch(matches.position(i))); 
	                iloscCechPoFiltrowaniu++;
	            }
	        }
			iloscPunktowDoLiczeniaH = iloscCechPoFiltrowaniu*3/4;
			
	        for (int i = 0; i < iloscPunktowDoLiczeniaH; i++) {
	            int m = 0;
	            opencv_features2d.DMatch tempDm = new opencv_features2d.DMatch(0, 0, -1.0f);
	            for (int k = 0; k < good_matches.size(); k++) {
                	double x11 = keypoint1.position(good_matches.get(k).queryIdx()).pt().x();
                	double y11 = keypoint1.position(good_matches.get(k).queryIdx()).pt().y();
                	double x12 = keypoint2.position(good_matches.get(k).trainIdx()).pt().x();
                	double y12 = keypoint2.position(good_matches.get(k).trainIdx()).pt().y();
                	
                	double x21 = keypoint1.position(tempDm.queryIdx()).pt().x();
                	double y21 = keypoint1.position(tempDm.queryIdx()).pt().y();
                	double x22 = keypoint2.position(tempDm.trainIdx()).pt().x();
                	double y22 = keypoint2.position(tempDm.trainIdx()).pt().y();
                	if (
                			(
	                			Math.sqrt((x11 - x12)*(x11 - x12) + (y11 - y12)*(y11 - y12))
	                			< Math.sqrt((x21 - x22)*(x21 - x22) + (y21 - y22)*(y21 - y22)) 
                			) || tempDm.distance() < 0
                		) {
                		tempDm = new opencv_features2d.DMatch(good_matches.get(k)); m = k;
                    }
	            } 
	            very_good_matches.add(tempDm); good_matches.remove(m);
	                       
//	            opencv_core.line( debugMat1, 
//                		new opencv_core.Point((int) keypoint1.position(tempDm.queryIdx()).pt().x(),  (int) keypoint1.position(tempDm.queryIdx()).pt().y()),
//                		new opencv_core.Point((int) keypoint2.position(tempDm.trainIdx()).pt().x(), (int) keypoint2.position(tempDm.trainIdx()).pt().y()),
//                		new opencv_core.Scalar(0.0, 255.0*(1 - tempDm.distance()/median_dist), 255.0*(tempDm.distance()/median_dist), 0.0));
	            
	            opencv_core.arrowedLine( debugMat1, 
	            		new opencv_core.Point((int) keypoint2.position(tempDm.trainIdx()).pt().x(), (int) keypoint2.position(tempDm.trainIdx()).pt().y()),
                		new opencv_core.Point((int) keypoint1.position(tempDm.queryIdx()).pt().x(),  (int) keypoint1.position(tempDm.queryIdx()).pt().y()),
                		new opencv_core.Scalar(0.0, 255.0*(1 - tempDm.distance()/median_dist), 255.0*(tempDm.distance()/median_dist), 0.0), 
	            		1, 
	            		opencv_core.CV_AA, 
	            		0, 
	            		0.1);
	            
//            	opencv_core.putText(debugMat1, m + ": " +new Double((int)tempDm.distance()).toString(), 
//            			new opencv_core.Point((int) keypoint1.position(tempDm.queryIdx()).pt().x(),  (int) keypoint1.position(tempDm.queryIdx()).pt().y()),
//            			1, 0.6,
//            			new opencv_core.Scalar(0.0, 255.0*(1 - tempDm.distance()/median_dist), 255.0*(tempDm.distance()/median_dist), 0.0));
	        }
	        
	        srcPoints = opencv_core.CvMat.create(very_good_matches.size(), 2, opencv_core.CV_32F);
	        dstPoints = opencv_core.CvMat.create(very_good_matches.size(), 2, opencv_core.CV_32F);
	        
	        for (int i = 0; i< very_good_matches.size(); i++) {
	        	opencv_features2d.DMatch tempDm = very_good_matches.get(i);
	        	srcPoints.put(i, 0, keypoint1.position(tempDm.queryIdx()).pt().x());
	            srcPoints.put(i, 1, keypoint1.position(tempDm.queryIdx()).pt().y());
	            dstPoints.put(i, 0, keypoint2.position(tempDm.trainIdx()).pt().x());
	            dstPoints.put(i, 1, keypoint2.position(tempDm.trainIdx()).pt().y());
	        }
	        
	        //out.println("matches: " + descriptors1.rows());
	        opencv_core.putText( debugMat1,  "matches: " + descriptors1.rows(), new opencv_core.Point(15, 15+15*6), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
	        //out.println("good matches: " + iloscCechPoFiltrowaniu);
	        opencv_core.putText( debugMat1,  "good matches: " + iloscCechPoFiltrowaniu, new opencv_core.Point(15, 15+15*7), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
	        //out.println("very good matches: " + very_good_matches.size());
	        opencv_core.putText( debugMat1,  "very good matches: " + very_good_matches.size(), new opencv_core.Point(15, 15+15*8), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
	        
		    if (very_good_matches.size() >= 200) {    
		    	H = opencv_calib3d.findHomography(new opencv_core.Mat(dstPoints), new opencv_core.Mat(srcPoints), opencv_calib3d.CV_RANSAC, 3.0, new opencv_core.Mat() );
		        //out.println("H: " + H.asCvMat());
		        
		        result = new opencv_core.Mat(obrazDoStabilizacji.rows(),obrazDoStabilizacji.cols(), opencv_core.CV_8UC4);
		        //opencv_imgproc.cvtColor(obrazDoStabilizacji, obrazDoStabilizacji, opencv_imgproc.CV_BGR2BGRA);
				
				opencv_imgproc.warpPerspective(obrazDoStabilizacji, result,H, obrazDoStabilizacji.size(), opencv_imgproc.CV_INTER_LINEAR,opencv_imgproc.BORDER_TRANSPARENT, new opencv_core.Scalar(0,0,0, 255));
								
				opencv_core.Mat white = new opencv_core.Mat( bazaDoStabilizacji.rows(),bazaDoStabilizacji.cols(), opencv_core.CV_8UC4, new opencv_core.Scalar(255, 255, 255, 255) );		
				// alphaMask:
				opencv_core.Mat alphaMask = new opencv_core.Mat(bazaDoStabilizacji.size(), opencv_core.CV_8UC4);
				int iFromTo1[] = { 3,0, 3,1, 3,2, 3,3 };
				opencv_core.mixChannels(new opencv_core.MatVector(result, white), new opencv_core.MatVector(alphaMask, white), iFromTo1);
				opencv_core.cvAddWeighted(alphaMask.asCvMat(), 1.0/255, alphaMask.asCvMat(), 0, 0, alphaMask.asCvMat());
				int iFromTo2[] = { 3,3, 3,3, 3,3, 3,3 };
				opencv_core.mixChannels(new opencv_core.MatVector(white, white), new opencv_core.MatVector(alphaMask, white), iFromTo2);
//				opencv_highgui.imwrite("alphaMask.png", alphaMask);
				// alphaMaskInv:
				opencv_core.Mat alphaMaskInv = new opencv_core.Mat(bazaDoStabilizacji.size(), opencv_core.CV_8UC4);
				opencv_core.Mat ones = new opencv_core.Mat( bazaDoStabilizacji.rows(),bazaDoStabilizacji.cols(), opencv_core.CV_8UC4, new opencv_core.Scalar(1, 1, 1, 255) );
				opencv_core.cvSub(ones.asCvMat(), alphaMask.asCvMat(), alphaMaskInv.asCvMat());
				opencv_core.mixChannels(new opencv_core.MatVector(white, white), new opencv_core.MatVector(alphaMaskInv, white), iFromTo2);
//				opencv_highgui.imwrite("alphaMaskInv.png", alphaMaskInv);
				// base_:
				opencv_core.Mat base_ = new opencv_core.Mat(bazaDoStabilizacji.size(), opencv_core.CV_8UC4);
				opencv_core.cvMul(bazaDoStabilizacji.asCvMat(), alphaMaskInv.asCvMat(), base_.asCvMat());
//				opencv_highgui.imwrite("base_.png", base_);
				// img_:
				opencv_core.Mat img_ = new opencv_core.Mat(bazaDoStabilizacji.size(), opencv_core.CV_8UC4);
				opencv_core.cvMul(result.asCvMat(), alphaMask.asCvMat(), img_.asCvMat());
//				opencv_highgui.imwrite("img_.png", img_);
				// out:
				opencv_core.cvAddWeighted(img_.asCvMat(), 1.0,  base_.asCvMat(), 1.0, 0.0, result.asCvMat());
//				opencv_highgui.imwrite("out_.png", out_);
				
		        String H_String[] = H.asCvMat().toString().split("\n");
		        for (int i = 0; i < H_String.length; i++) opencv_core.putText( debugMat1,  H_String[i].replace(",", "      "), new opencv_core.Point(15, 15+15*i), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
		        opencv_core.cvAddWeighted(debugMat1.asIplImage(), 0.8, result.asIplImage(), 0.4, 0.0, debugMat1.asIplImage());
		        opencv_highgui.imwrite("robocze\\debug" + "\\_debugMat1_" + System.currentTimeMillis() + "." + "jpg", debugMat1);
		       
		        return result;
		    }
		    else {
	        	out.println("zbyt malo prawidlowych punktow wspolnych");
		        opencv_core.cvAddWeighted(debugMat1.asIplImage(), 0.8, result.asIplImage(), 0.4, 0.0, debugMat1.asIplImage());
	        	opencv_core.putText( debugMat1,  "brak macierzy transformacji", new opencv_core.Point(15, 15), 1, 1, new opencv_core.Scalar(64.0, 64.0, 255.0, 0.0));
		        opencv_highgui.imwrite("robocze\\debug" + "\\_debugMat1_" + System.currentTimeMillis() + "." + "jpg", debugMat1);
	        	iloscBledowStabilizowania++;
	        	return null;
	        }
        } catch (Exception e) {
        	iloscBledowStabilizowania++;
            out.println("Blad usredniania - cos nie tak z obrazami w argumentach metody");
        }
		
		return null;
	};





public static void usrednijWiele(String sciezkaZrodlowa, String sciezkaDocelowa, String rozszerzenie) throws IOException {
    List<Path> listaPlikow = new ArrayList<>();
    String nazwaFolderuZrodlowego = Paths.get(sciezkaZrodlowa).toAbsolutePath().toString();
    String nazwaFolderuDocelowego = Paths.get(sciezkaDocelowa).toAbsolutePath().toString();

    Files.walk(Paths.get(nazwaFolderuZrodlowego)).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
            listaPlikow.add(filePath); //out.println(filePath.toAbsolutePath().toString());
        }
    });


    //MatOfDouble obrazekWynikowy = new MatOfDouble(); out.println("Inicjalizowanie obrazekWynikowy za pomoca: " + listaObrazkow.get(0).toAbsolutePath().toString());
    opencv_core.IplImage obrazekWynikowy = new opencv_core.IplImage();// cvLoadImage(listaObrazkow.get(0).toAbsolutePath().toString());
    //out.println("bity obrazekWynikowy: " + obrazekWynikowy.depth());
    opencv_core.Mat obrazekWynikowy_ = new opencv_core.Mat();
    opencv_highgui.imread(listaPlikow.get(0).toAbsolutePath().toString(), opencv_highgui.IMREAD_UNCHANGED).convertTo(obrazekWynikowy_, opencv_core.CV_16UC4);
    // ogarnac to za pomoca mnozenia...

    obrazekWynikowy = obrazekWynikowy_.asIplImage();
    opencv_core.cvAddWeighted(obrazekWynikowy, 255.0, obrazekWynikowy, 0.0, 0.0, obrazekWynikowy);
    //out.println("bity obrazekWynikowy: " + obrazekWynikowy.depth());
    Dimension wymiaryObrazka = new Dimension(obrazekWynikowy.width(), obrazekWynikowy.height());

    out.println("wymiary obrazka: " + wymiaryObrazka.toString());



    int iloscObrazkow = listaPlikow.size(); out.println("ilosc obrazkow do przetworzenia: " + iloscObrazkow);

    if ((iloscObrazkow > 0) && (!wymiaryObrazka.equals(new Dimension(0, 0)))) {
        int iloscPrzetworzonych = 0;
        int iloscBledow = 0;

        //cvSaveImage(nazwaFolderuDocelowego + "\\_obrazekWynikowy.png", usredniajRekursywnie(listaObrazkow.toArray(new Path[iloscObrazkow]), cvLoadImage(listaObrazkow.get(0).toAbsolutePath().toString(),CV_32F), 0, iloscObrazkow-1));

        opencv_core.Mat obrazek_ = new opencv_core.Mat();
        opencv_core.IplImage obrazek = new opencv_core.IplImage();
        for (Path sciezkaObrazka : listaPlikow) {
            iloscPrzetworzonych++; out.println("\n\n" + sciezkaObrazka.getFileName().toString());

            //Highgui.imread(sciezkaObrazka.toAbsolutePath().toString()).convertTo(obrazek, CvType.CV_64FC3);
            //IplImage obrazek = cvLoadImage(sciezkaObrazka.toAbsolutePath().toString(), CV_32F);
            opencv_highgui.imread(sciezkaObrazka.toAbsolutePath().toString(), opencv_highgui.IMREAD_UNCHANGED).convertTo(obrazek_, opencv_core.CV_16UC4);
            obrazek = obrazek_.asIplImage();
         // ogarnac to za pomoca mnozenia...
            opencv_core.cvAddWeighted(obrazek, 255.0, obrazek, 0.0, 0.0, obrazek);


            try {



//                    out.println("bity obrazekWynikowy: " + obrazekWynikowy.depth());
//                out.println("kanaly obrazek: " + obrazek.nChannels());
//                    out.println("bity obrazek: " + obrazek.depth());
//                out.println("kanaly obrazekWynikowy: " + obrazekWynikowy.nChannels());
                    opencv_core.cvAddWeighted(obrazek, 1.0/(iloscPrzetworzonych), obrazekWynikowy, 1.0 - 1.0/(iloscPrzetworzonych), 0.0, obrazekWynikowy);

                //opencv_core.cvMin(obrazek, obrazekWynikowy, obrazekWynikowy);


            } catch (Exception e) {
                iloscBledow++;
                out.println("Blad...\n" + e.getLocalizedMessage());
            }

            out.println("przetworzonych: " + iloscPrzetworzonych + "/" + iloscObrazkow + ", bledow: " + iloscBledow);
            System.gc   (); // pomaga
            //cvReleaseImage(obrazek);
        }

        //double k = ((double) iloscPrzetworzonych)/(iloscPrzetworzonych - iloscBledow);
        //Core.multiply(obrazekWynikowy, new Scalar(k), obrazekWynikowy);
        //Highgui.imwrite(nazwaFolderu + "\\_obrazekWynikowy.png", obrazekWynikowy); 
        //cvtColor( obrazekWynikowy_, obrazekWynikowy_, CV_16U );
        opencv_highgui.cvSaveImage(nazwaFolderuDocelowego + "\\_obrazekWynikowy_" + iloscObrazkow + "o-" + iloscBledow + "e."+rozszerzenie, obrazekWynikowy);
    }


}

public static void dopasujWiele(String sciezkaZrodlowa, String sciezkaDocelowa, String rozszerzenie) throws IOException {
    List<Path> listaObrazkow = new ArrayList<>();
    String nazwaFolderuZrodlowego = Paths.get(sciezkaZrodlowa).toAbsolutePath().toString();
    String nazwaFolderuDocelowego = Paths.get(sciezkaDocelowa).toAbsolutePath().toString();

    Files.walk(Paths.get(nazwaFolderuZrodlowego)).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
            listaObrazkow.add(filePath); //out.println(filePath.toAbsolutePath().toString());
        }
    });

            // Ladowanie zdjec
            out.println("ladowanie zdjec");
            int nrZdjBazowego = listaObrazkow.size()/25;
            opencv_core.Mat image1_ = opencv_highgui.imread(listaObrazkow.get(nrZdjBazowego).toAbsolutePath().toString()); listaObrazkow.remove(nrZdjBazowego);

    Dimension wymiaryObrazka = new Dimension(image1_.cols(), image1_.rows());
    int iloscObrazkow = listaObrazkow.size(); out.println("ilosc obrazkow do przetworzenia: " + iloscObrazkow);

    if ((iloscObrazkow > 0) && (!wymiaryObrazka.equals(new Dimension(0, 0)))) {
        int iloscPrzetworzonych = 0;
        int iloscBledow = 0;

        opencv_imgproc.cvtColor(image1_, image1_, opencv_imgproc.CV_BGR2BGRA);
//            opencv_highgui.imwrite(nazwaFolderuDocelowego + "\\_wynik_" + "baza" + "." + rozszerzenie, image1_);


        //BRISK detektorBRISK = new BRISK();
        opencv_nonfree.SIFT detektorSift = new opencv_nonfree.SIFT(0, 3, 0.01, 40, 1.6);
            //MSER detektorMser = new MSER();
        opencv_nonfree.SIFT detektorSurf = new opencv_nonfree.SIFT(400);
        opencv_features2d.FastFeatureDetector detectorFast = new opencv_features2d.FastFeatureDetector();
        opencv_features2d.ORB detektorOrb = new opencv_features2d.ORB(1000, 1.3f, 8, 64, 0, 2, opencv_features2d.ORB.HARRIS_SCORE, 64);
//	        ORB(int nfeatures=500, 
//	        		float scaleFactor=1.2f, 
//	        		int nlevels=8, 
//	        		int edgeThreshold=31, 
//	        		int firstLevel=0, 
//	        		int WTA_K=2, 
//	        		int scoreType=ORB::HARRIS_SCORE, 
//	        		int patchSize=31, 
//	        		xxx int fastThreshold=20)

        opencv_features2d.BFMatcher matcherBFM = new opencv_features2d.BFMatcher();
        opencv_features2d.FlannBasedMatcher matcherFBM = new opencv_features2d.FlannBasedMatcher();

        opencv_features2d.KeyPoint keypoint1 = new opencv_features2d.KeyPoint();
        opencv_core.Mat descriptors1 = new opencv_core.Mat();
            opencv_nonfree.SIFT detektor = detektorSift;

            opencv_core.Mat image1 = new opencv_core.Mat(image1_.clone());

//	        cvtColor( image1, image1, CV_RGB2GRAY );
//	        opencv_imgproc.equalizeHist( image1, image1 );

            detektorOrb.detect(image1,keypoint1);
            detektorOrb.compute( image1, keypoint1, descriptors1 ); 

        for (Path sciezkaObrazka : listaObrazkow) {
            out.println("\n\n\n" + sciezkaObrazka.getFileName().toString());
            iloscPrzetworzonych++;

            opencv_core.Mat image2_ = opencv_highgui.imread(sciezkaObrazka.toAbsolutePath().toString());


                    opencv_core.Mat image2 = new opencv_core.Mat(image2_.clone());


//		        opencv_imgproc.bilateralFilter(image1, image1, 9, 150, 150);
//		        opencv_imgproc.bilateralFilter(image2, image2, 9, 150, 150);

//		        opencv_imgproc.blur(image1, image1, new Size(100));
//		        opencv_imgproc.blur(image2, image2, new Size(100));

                ////odcienie szarosci
                            //out.println("odcienie szarosci");
//		        cvtColor( image1, image1, CV_RGB2GRAY );
//		        cvtColor( image2, image2, CV_RGB2GRAY );



                    //fastNlMeansDenoising(Mat InputArray, Mat OutputArray, float h=3, int templateWindowSize=7, int searchWindowSize=21 );
//		        opencv_photo.fastNlMeansDenoising(image1, image1, 3, 7, 21 );
//		        opencv_photo.fastNlMeansDenoising(image1, image1, 3, 7, 21 );


                     //wyrownanie histogramu
                    //out.println("wyrownanie histogramu");
//		        opencv_imgproc.equalizeHist( image1, image1 );
//		        opencv_imgproc.equalizeHist( image2, image2 );



                    opencv_core.Mat debugMat1 = new opencv_core.Mat(image2_.clone());
//		        Mat debugMat2 = new Mat(image1_.clone());

                    double max_dist = 0; double median_dist = 500; double min_dist = 1000;
                    //double max_dist_ = 0; double min_dist_ = 1000;

                    opencv_core.cvAddWeighted(debugMat1.asIplImage(), 0.1, debugMat1.asIplImage(), 0.1, 0.0, debugMat1.asIplImage());
//		        cvAddWeighted(debugMat2.asIplImage(), 0.1, debugMat2.asIplImage(), 0.1, 0.0, debugMat2.asIplImage());

                            //// detekcja cech
                            //out.println("detekcja cech");

                    opencv_features2d.KeyPoint keypoint2 = new opencv_features2d.KeyPoint();
                    opencv_core.Mat descriptors2 = new opencv_core.Mat();

                    detektorOrb.detect(image2,keypoint2);

                    out.println("keypoint1: " + keypoint1.capacity());
                    out.println("keypoint2: " + keypoint2.capacity());

                    // wyglada dobrze


                            // obliczanie cech
                            //out.println("obliczanie cech");

                    detektorOrb.compute( image2, keypoint2, descriptors2 );

//		        out.println("descriptors1: " + descriptors1.rows());
//		        out.println("descriptors2: " + descriptors2.rows());

                            // dopasowywanie cech
                            //out.println("dopasowywanie cech");

                    opencv_features2d.DMatch  matches = new opencv_features2d.DMatch();
                    matcherBFM.match( descriptors1, descriptors2, matches );
                    //out.println("matches: " + matches.capacity());
                    // wyglada ok


                    //if (matches.capacity() > 100) {
                            // obliczanie min i max dist
                            //out.println("obliczanie min i max dist");

                    for( int i = 0; i < descriptors1.rows(); i++ ) {
                        double dist = matches.position(i).distance();
                        //out.println("dist: " + dist);
                        if( dist < min_dist ) min_dist = dist;

                        if( dist > max_dist ) max_dist = dist;
                    }
                    median_dist =  matches.position(descriptors1.rows()/2).distance();

                    opencv_core.putText( debugMat1,  "dist min: " + min_dist + ", median: " + median_dist + ", max: " + max_dist , new opencv_core.Point(15, 15+15*4), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
                    out.println("dist min: " + min_dist + ", median: " + median_dist + ", max: " + max_dist);
                    // wyglada dobrze


                            int iloscCechPoFiltrowaniu = 0;
                            // odfiltrowywanie najgorszych cech
                            //out.println("odfiltrowywanie najgorszych cech");
                    List<opencv_features2d.DMatch> good_matches = new ArrayList<>();
//		        DMatch goodDmatchArray[] = new DMatch[descriptors1.rows()];

//		        double [] point1_arr = new double[descriptors1.rows()*2];
//		        double [] point2_arr = new double[descriptors1.rows()*2];


                    for( int i = 0; i < descriptors1.rows(); i++ ) {
                        if( matches.position(i).distance()  <=  Math.min(median_dist, max_dist/2)  ) { 
                            good_matches.add(new opencv_features2d.DMatch(matches.position(i))); 
//		                goodDmatchArray[iloscCechPoFiltrowaniu] = new DMatch(matches.position(i));              
                            iloscCechPoFiltrowaniu++;

//		                out.println("dist: " + matches.position(i).distance() + "\n");

                        }
                    }

                    List<opencv_features2d.DMatch> very_good_matches = new ArrayList<>();

                            // sortowanie listy dopasowan rosnaco wzgledem odleglosci
                            //out.println("sortowanie listy dopasowan rosnaco wzgledem odleglosci");
                            int iloscPunktowDoLiczeniaH = iloscCechPoFiltrowaniu*3/4;
//		        DMatch pointWyniki_arr[] = new DMatch[iloscPunktowDoLiczeniaH];
                            opencv_features2d.DMatch pointWyniki_arr;


                    for (int i = 0; i < iloscPunktowDoLiczeniaH; i++) {
                        int m = 0;
                        //DMatch minDmatch = new DMatch();
                        pointWyniki_arr = new opencv_features2d.DMatch(0, 0, -1.0f);
                        for (int k = 0; k < good_matches.size(); k++) {
//		                if (good_matches.get(k) != null) {
                                    double x11 = keypoint1.position(good_matches.get(k).queryIdx()).pt().x();
                                    double y11 = keypoint1.position(good_matches.get(k).queryIdx()).pt().y();
                                    double x12 = keypoint2.position(good_matches.get(k).trainIdx()).pt().x();
                                    double y12 = keypoint2.position(good_matches.get(k).trainIdx()).pt().y();

                                    double x21 = keypoint1.position(pointWyniki_arr.queryIdx()).pt().x();
                                    double y21 = keypoint1.position(pointWyniki_arr.queryIdx()).pt().y();
                                    double x22 = keypoint2.position(pointWyniki_arr.trainIdx()).pt().x();
                                    double y22 = keypoint2.position(pointWyniki_arr.trainIdx()).pt().y();
//		                	if (goodDmatchArray[k].distance() < pointWyniki_arr.distance() ) {
                                    if (//dystans punktow w 
                                                    (Math.sqrt((x11 - x12)*(x11 - x12) + (y11 - y12)*(y11 - y12))
                                                    < 
                                                    Math.sqrt((x21 - x22)*(x21 - x22) + (y21 - y22)*(y21 - y22)) )
                                                    || pointWyniki_arr.distance() < 0
                                            ) {
                                    pointWyniki_arr = new opencv_features2d.DMatch(good_matches.get(k)); m = k;
//		                        //out.println("i " + i + " k " + k + " m " + m + " "+ pointWyniki_arr.distance());
                                }
//		                }
                        } 
                        very_good_matches.add(pointWyniki_arr); good_matches.remove(m);

                        opencv_features2d.DMatch dm = pointWyniki_arr;

                        //out.println("dist : k " + m + ", i " + i + ", d " + pointWyniki_arr.distance());
                        opencv_core.line( debugMat1, 
                                    new opencv_core.Point((int) keypoint1.position(dm.queryIdx()).pt().x(),  (int) keypoint1.position(dm.queryIdx()).pt().y()),
                                    new opencv_core.Point((int) keypoint2.position(dm.trainIdx()).pt().x(), (int) keypoint2.position(dm.trainIdx()).pt().y()),
                                    new opencv_core.Scalar(0.0, 255.0*(1 - dm.distance()/median_dist), 255.0*(dm.distance()/median_dist), 0.0));

                    opencv_core.putText(debugMat1, m + ": " +new Double((int)dm.distance()).toString(), 
                                    new opencv_core.Point((int) keypoint1.position(dm.queryIdx()).pt().x(),  (int) keypoint1.position(dm.queryIdx()).pt().y()),
                                    1, 0.6,
                                    new opencv_core.Scalar(0.0, 255.0*(1 - dm.distance()/median_dist), 255.0*(dm.distance()/median_dist), 0.0));
                    }

                    opencv_core.CvMat srcPoints = opencv_core.CvMat.create(very_good_matches.size(), 2, opencv_core.CV_32F);
                    opencv_core.CvMat dstPoints = opencv_core.CvMat.create(very_good_matches.size(), 2, opencv_core.CV_32F);

                    for (int i = 0; i< very_good_matches.size(); i++) {
                            opencv_features2d.DMatch dm = very_good_matches.get(i);
                            srcPoints.put(i, 0, keypoint1.position(dm.queryIdx()).pt().x());
                        srcPoints.put(i, 1, keypoint1.position(dm.queryIdx()).pt().y());
                        dstPoints.put(i, 0, keypoint2.position(dm.trainIdx()).pt().x());
                        dstPoints.put(i, 1, keypoint2.position(dm.trainIdx()).pt().y());
                    // CvArr* img, const char* text, CvPoint org, const CvFont* font, CvScalar color
                    }



                    //Mat H = new Mat(3,3);
                    //Mat H_affine = new Mat(3,3);
//		        IplImage H_sum = null;
//		        IplImage H_affine_sum = null;
                    //for (int c = 0; c<(iloscCechPoFiltrowaniu/4-1); c++) {
                                    // przygotowanie danych do obliczenia przeksztalcenia
//					out.println("przygotowanie danych do obliczenia przeksztalcenia, c: " + c);
//			        CvMat cvMat1 = CvMat.create(4, 2, CV_32F);
//			        CvMat cvMat2 = CvMat.create(4, 2, CV_32F);
//			        
//			        int c = 0;
//			        cvMat1.put(0,0,keypoint1.position(pointWyniki_arr[4*c+0].queryIdx()).pt().x()); cvMat1.put(0,1,keypoint1.position(pointWyniki_arr[4*c+0].queryIdx()).pt().y());
//			        cvMat1.put(1,0,keypoint1.position(pointWyniki_arr[4*c+1].queryIdx()).pt().x()); cvMat1.put(1,1,keypoint1.position(pointWyniki_arr[4*c+1].queryIdx()).pt().y());
//			        cvMat1.put(2,0,keypoint1.position(pointWyniki_arr[4*c+2].queryIdx()).pt().x()); cvMat1.put(2,1,keypoint1.position(pointWyniki_arr[4*c+2].queryIdx()).pt().y());
//			        cvMat1.put(3,0,keypoint1.position(pointWyniki_arr[4*c+3].queryIdx()).pt().x()); cvMat1.put(3,1,keypoint1.position(pointWyniki_arr[4*c+3].queryIdx()).pt().y());
//			
//			        cvMat2.put(0,0,keypoint2.position(pointWyniki_arr[4*c+0].trainIdx()).pt().x()); cvMat2.put(0,1,keypoint2.position(pointWyniki_arr[4*c+0].trainIdx()).pt().y());
//			        cvMat2.put(1,0,keypoint2.position(pointWyniki_arr[4*c+1].trainIdx()).pt().x()); cvMat2.put(1,1,keypoint2.position(pointWyniki_arr[4*c+1].trainIdx()).pt().y());
//			        cvMat2.put(2,0,keypoint2.position(pointWyniki_arr[4*c+2].trainIdx()).pt().x()); cvMat2.put(2,1,keypoint2.position(pointWyniki_arr[4*c+2].trainIdx()).pt().y());
//			        cvMat2.put(3,0,keypoint2.position(pointWyniki_arr[4*c+3].trainIdx()).pt().x()); cvMat2.put(3,1,keypoint2.position(pointWyniki_arr[4*c+3].trainIdx()).pt().y());
//			        Mat point1 = new Mat(cvMat1);
//			        Mat point2 = new Mat(cvMat2);
//			        out.println("point1: " + point1.asCvMat());
//			        out.println("point2: " + point2.asCvMat());
    //		        
    //		        out.println(" checkVector: point1 : " + point1.checkVector(2, CV_32F, true));
    //		        out.println(" checkVector: point2 : " + point2.checkVector(2, CV_32F, true));
                    out.println("matches: " + descriptors1.rows());
                    opencv_core.putText( debugMat1,  "matches: " + descriptors1.rows(), new opencv_core.Point(15, 15+15*6), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));

                    out.println("good matches: " + iloscCechPoFiltrowaniu);
                    opencv_core.putText( debugMat1,  "good matches: " + iloscCechPoFiltrowaniu, new opencv_core.Point(15, 15+15*7), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));

                    out.println("very good matches: " + very_good_matches.size());
                    opencv_core.putText( debugMat1,  "very good matches: " + very_good_matches.size(), new opencv_core.Point(15, 15+15*8), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));







//			        CvMat cvMat1_affine = CvMat.create(3, 2, CV_32F);
//			        CvMat cvMat2_affine = CvMat.create(3, 2, CV_32F);
//			        
//			        cvMat1_affine.put(0,0,keypoint1.position(pointWyniki_arr[0].queryIdx()).pt().x()); cvMat1.put(0,1,keypoint1.position(pointWyniki_arr[0].queryIdx()).pt().y());
//			        cvMat1_affine.put(1,0,keypoint1.position(pointWyniki_arr[1].queryIdx()).pt().x()); cvMat1.put(1,1,keypoint1.position(pointWyniki_arr[1].queryIdx()).pt().y());
//			        cvMat1_affine.put(2,0,keypoint1.position(pointWyniki_arr[2].queryIdx()).pt().x()); cvMat1.put(2,1,keypoint1.position(pointWyniki_arr[2].queryIdx()).pt().y());
//			
//			        cvMat2_affine.put(0,0,keypoint2.position(pointWyniki_arr[0].trainIdx()).pt().x()); cvMat2.put(0,1,keypoint2.position(pointWyniki_arr[0].trainIdx()).pt().y());
//			        cvMat2_affine.put(1,0,keypoint2.position(pointWyniki_arr[1].trainIdx()).pt().x()); cvMat2.put(1,1,keypoint2.position(pointWyniki_arr[1].trainIdx()).pt().y());
//			        cvMat2_affine.put(2,0,keypoint2.position(pointWyniki_arr[2].trainIdx()).pt().x()); cvMat2.put(2,1,keypoint2.position(pointWyniki_arr[2].trainIdx()).pt().y());
//			        Mat point1_affine = new Mat(cvMat1_affine);
//			        Mat point2_affine = new Mat(cvMat2_affine);
//			        out.println("point1_affine: " + point1_affine.asCvMat());
//			        out.println("point2_affine: " + point2_affine.asCvMat());




                        if (very_good_matches.size() >= 50) {    
                            // liczenie przeksztalcenia
                                    //out.println("liczenie przeksztalcenia");

                                    //H = opencv_imgproc.getPerspectiveTransform( point2, point1);
                            opencv_core.Mat H = opencv_calib3d.findHomography(new opencv_core.Mat(dstPoints), new opencv_core.Mat(srcPoints), opencv_calib3d.CV_RANSAC, 3.0, new opencv_core.Mat() );
    //			        0 - a regular method using all the points
    //			        CV_RANSAC - RANSAC-based robust method
    //			        CV_LMEDS - Least-Median robust method
                            out.println("H: " + H.asCvMat());
    //			        if (H_sum == null) H_sum = new Mat(H).asIplImage();
                                    //else cvAddWeighted(H.asIplImage(), 1.0/(c+1), H_sum, 1.0 - 1.0/(c+1), 0.0, H_sum);

    //			        H_affine = opencv_imgproc.getAffineTransform( point2_affine, point1_affine );
    //			        if (H_affine_sum == null) H_affine_sum = new Mat(H_affine).asIplImage();
    //			        else cvAddWeighted(H_affine.asIplImage(), 1.0/(c+1), H_affine_sum, 1.0 - 1.0/(c+1), 0.0, H_affine_sum);




    //			        out.println("H_sum: " + H_sum.asCvMat());


            //		        out.println("H_affine: " + H_affine.asCvMat());

                            //}

                                    // przeksztalcanie obrazu wejsciowego
                                    //out.println("przeksztalcanie obrazu wejsciowego");

                            opencv_core.Mat result = new opencv_core.Mat(image2_.rows(),image2_.cols(), opencv_core.CV_8UC4);
                            //Mat result_ = new Mat(image1_.clone());
                            opencv_imgproc.cvtColor(image2_, image2_, opencv_imgproc.CV_BGR2BGRA);
                            //cvtColor(result_, result_, opencv_imgproc.CV_BGR2BGRA);
                                    //result.convertTo(result, opencv_core.CV_8UC4);

                                    //Mat newSrc = new Mat(image1_.rows(),image1_.cols(),opencv_core.CV_8UC4);

                                    //int iFromTo[] = { 0,0, 1,1, 2,2, 3,3 };




    //				Mat result_affine = new Mat();
                                    out.println("1. result.channels(): " + result.channels());
                            //opencv_imgproc.warpPerspective(image2_, result,H, image1_.size());
                                    opencv_imgproc.warpPerspective(image2_, result,H, image1_.size(), opencv_imgproc.CV_INTER_LINEAR,opencv_imgproc.BORDER_TRANSPARENT, new opencv_core.Scalar(0,0,0, 255));
    //		        opencv_imgproc.warpAffine(image2_, result_affine, H_affine, image1_.size() );
                                    //opencv_core.mixChannels(new MatVector(result, rgba), new MatVector(result_, rgba), iFromTo);
                                    out.println("2. result.channels(): " + result.channels());
                                    //result.copyTo(result_);




                                    opencv_core.Mat white = new opencv_core.Mat( image1_.rows(),image1_.cols(), opencv_core.CV_8UC4, new opencv_core.Scalar(255, 255, 255, 255) );


                                    // alphaMask
                                    opencv_core.Mat alphaMask = new opencv_core.Mat(image1_.size(), opencv_core.CV_8UC4);
                                    int iFromTo1[] = { 3,0, 3,1, 3,2, 3,3 };
                                    opencv_core.mixChannels(new opencv_core.MatVector(result, white), new opencv_core.MatVector(alphaMask, white), iFromTo1);
                                    opencv_core.cvAddWeighted(alphaMask.asCvMat(), 1.0/255, alphaMask.asCvMat(), 0, 0, alphaMask.asCvMat());
                                    int iFromTo2[] = { 3,3, 3,3, 3,3, 3,3 };
                                    opencv_core.mixChannels(new opencv_core.MatVector(white, white), new opencv_core.MatVector(alphaMask, white), iFromTo2);
//					opencv_highgui.imwrite("alphaMask.png", alphaMask);

                                    // alphaMaskInv
                                    opencv_core.Mat alphaMaskInv = new opencv_core.Mat(image1_.size(), opencv_core.CV_8UC4);
                                    opencv_core.Mat ones = new opencv_core.Mat( image1_.rows(),image1_.cols(), opencv_core.CV_8UC4, new opencv_core.Scalar(1, 1, 1, 255) );
                                    opencv_core.cvSub(ones.asCvMat(), alphaMask.asCvMat(), alphaMaskInv.asCvMat());
                                    opencv_core.mixChannels(new opencv_core.MatVector(white, white), new opencv_core.MatVector(alphaMaskInv, white), iFromTo2);
//					opencv_highgui.imwrite("alphaMaskInv.png", alphaMaskInv);


                                    // base_
                                    opencv_core.Mat base_ = new opencv_core.Mat(image1_.size(), opencv_core.CV_8UC4);
                                    opencv_core.cvMul(image1_.asCvMat(), alphaMaskInv.asCvMat(), base_.asCvMat());
//					opencv_highgui.imwrite("base_.png", base_);


                                    // img_ 
                                    opencv_core.Mat img_ = new opencv_core.Mat(image1_.size(), opencv_core.CV_8UC4);
                                    opencv_core.cvMul(result.asCvMat(), alphaMask.asCvMat(), img_.asCvMat());
//					opencv_highgui.imwrite("img_.png", img_);

                                    // out
                                    opencv_core.Mat out_ = new opencv_core.Mat(image1_.size(), opencv_core.CV_8UC4);
                                    opencv_core.cvAddWeighted(img_.asCvMat(), 1.0,  base_.asCvMat(), 1.0, 0.0, out_.asCvMat());
//					opencv_highgui.imwrite("out_.png", out_);


                                    result = out_;

                            opencv_highgui.imwrite(nazwaFolderuDocelowego + "\\_wynik_" + sciezkaObrazka.getFileName() + "_" + iloscObrazkow + "o-" + iloscBledow + "e."+rozszerzenie, result);
    //		        opencv_highgui.imwrite(nazwaFolderuDocelowego + "\\_wynik_affine_" + sciezkaObrazka.getFileName() + ".jpg", result_affine);

                            //Mat half = new Mat(result,new Rect(0,0,image2_.cols(),image2_.rows()));
                            //image2_.copyTo(half);
                            //opencv_highgui.imwrite("porownanie.jpg", half);

                            String H_String[] = H.asCvMat().toString().split("\n");

                            for (int i = 0; i < H_String.length; i++) opencv_core.putText( debugMat1,  H_String[i].replace(",", "      "), new opencv_core.Point(15, 15+15*i), 1, 1, new opencv_core.Scalar(255.0, 255.0, 255.0, 0.0));
                            opencv_highgui.imwrite("robocze\\debug" + "\\_debugMat1_" + sciezkaObrazka.getFileName() + "." + "jpg", debugMat1);
                            //opencv_highgui.imwrite("robocze\\debug" + "\\_debugMat2_" + sciezkaObrazka.getFileName() + ".jpg", debugMat2);

                        }
                        else {
                            out.println("zbyt malo prawidlowych punktow wspolnych");
                            opencv_core.putText( debugMat1,  "brak macierzy transformacji", new opencv_core.Point(15, 15), 1, 1, new opencv_core.Scalar(64.0, 64.0, 255.0, 0.0));
                            opencv_highgui.imwrite("robocze\\debug" + "\\_debugMat1_" + sciezkaObrazka.getFileName() + "." + "jpg", debugMat1);
                            iloscBledow++;
                    }

                    //opencv_highgui.imwrite("robocze\\debug" + "\\_debugMat2_" + sciezkaObrazka.getFileName() + ".jpg", debugMat2);
                    //}
                    //else {
                    //	out.println("zbyt malo punktow wspolnych");
                    //	iloscBledow++;
                    //}

        }

    }
}

public static void zapiszJakoObrazy(String sciezkaZrodlowa, String sciezkaDocelowa, String rozszerzenie) throws IOException, FrameGrabber.Exception {
    List<Path> listaFilmikow = new ArrayList<>();
    String nazwaFolderuZrodlowego = Paths.get(sciezkaZrodlowa).toAbsolutePath().toString();
    String nazwaFolderuDocelowego = Paths.get(sciezkaDocelowa).toAbsolutePath().toString();

    Files.walk(Paths.get(nazwaFolderuZrodlowego)).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
            listaFilmikow.add(filePath); out.println(filePath.toAbsolutePath().toString());
        }
    });

    int iloscFilmikow = listaFilmikow.size(); out.println("ilosc filmikow do przetworzenia: " + iloscFilmikow);

    if ((iloscFilmikow > 0)) {
        int iloscPrzetworzonych = 0;
        int iloscBledow = 0;

        for (Path sciezkaFilmiku : listaFilmikow) {
            iloscPrzetworzonych++; out.println("przetwarzanie filmiku: " + sciezkaFilmiku.getFileName().toString());

            opencv_highgui.CvCapture cvcapt = opencv_highgui.cvCreateFileCapture(sciezkaFilmiku.toAbsolutePath().toString());
            opencv_highgui.cvGrabFrame(cvcapt);


            //try{
            //IplImage obrazek = null;
            DecimalFormat formatStr = new DecimalFormat("000000");
            int iloscKlatek = (int) opencv_highgui.cvGetCaptureProperty(cvcapt,opencv_highgui.CV_CAP_PROP_FRAME_COUNT );
            for (int i = 0; i<iloscKlatek-1; i++) {
                out.println("klatka: " + i + "/" + iloscKlatek);
                //obrazek = opencv_highgui.cvQueryFrame(cvcapt);
                opencv_highgui.cvSaveImage(nazwaFolderuDocelowego + "\\_" + sciezkaFilmiku.getFileName() + "_klatka_" + formatStr.format(i) + "."+rozszerzenie, opencv_highgui.cvQueryFrame(cvcapt));

            }
            //cvReleaseImage(obrazek);
//                } catch (Exception e) {
//                    iloscBledow++;
//                    out.println("Blad...");
//                }

            out.println("przetworzonych: " + iloscPrzetworzonych + "/" + iloscFilmikow + ", bledow: " + iloscBledow);
            System.gc   (); // pomaga

        }

    }





//        
//        //MatOfDouble obrazekWynikowy = new MatOfDouble(); out.println("Inicjalizowanie obrazekWynikowy za pomoca: " + listaObrazkow.get(0).toAbsolutePath().toString());
//        IplImage obrazekWynikowy = cvLoadImage(listaObrazkow.get(0).toAbsolutePath().toString(),CV_32F);
//        Dimension wymiaryObrazka = new Dimension(obrazekWynikowy.width(), obrazekWynikowy.height());
//
//        out.println("wymiary obrazka: " + wymiaryObrazka.toString());
//        
//
//        
//        int iloscObrazkow = listaObrazkow.size(); out.println("ilosc obrazkow do przetworzenia: " + iloscObrazkow);
//        
//        if ((iloscObrazkow > 0) && (!wymiaryObrazka.equals(new Dimension(0, 0)))) {
//            int iloscPrzetworzonych = 0;
//            int iloscBledow = 0;
//            
//            for (Path sciezkaObrazka : listaObrazkow) {
//                iloscPrzetworzonych++; out.println("przetwarzanie obrazka: " + sciezkaObrazka.getFileName().toString());
//                
//                //Highgui.imread(sciezkaObrazka.toAbsolutePath().toString()).convertTo(obrazek, CvType.CV_64FC3);
//                IplImage obrazek = cvLoadImage(sciezkaObrazka.toAbsolutePath().toString(), CV_32F);
//                            
//                try {
//                    cvAddWeighted(obrazek, 1.0/(iloscPrzetworzonych), obrazekWynikowy, 1.0 - 1.0/(iloscPrzetworzonych), 0.0, obrazekWynikowy);
//                    opencv_core.cvAddWeighted(null, iloscBledow, null, iloscBledow, iloscBledow, null);
//                } catch (Exception e) {
//                    iloscBledow++;
//                    out.println("Blad...");
//                }
//                
//                out.println("przetworzonych: " + iloscPrzetworzonych + "/" + iloscObrazkow + ", bledow: " + iloscBledow);
//                System.gc   (); // pomaga
//                cvReleaseImage(obrazek);
//            }
//            
//            //double k = ((double) iloscPrzetworzonych)/(iloscPrzetworzonych - iloscBledow);
//            //Core.multiply(obrazekWynikowy, new Scalar(k), obrazekWynikowy);
//            //Highgui.imwrite(nazwaFolderu + "\\_obrazekWynikowy.png", obrazekWynikowy);   
//            cvSaveImage(nazwaFolderu + "\\_obrazekWynikowy.png", obrazekWynikowy);
//        }


}
}