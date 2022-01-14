package applications;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.lang.Math;


import org.pi4.locutil.io.TraceGenerator;
import org.pi4.locutil.trace.Parser;
import org.pi4.locutil.trace.TraceEntry;

import org.pi4.locutil.*;

/**
 * Example of how to use LocUtil
 * @author mikkelbk
 */

public class empirical_FP_NN {

	/**
	 * Execute example
	 * @param args
	 */
	public static void main(String[] args) {
		
		String offlinePath = "data/MU.1.5meters.offline.trace", onlinePath = "data/MU.1.5meters.online.trace";
		TraceEntry randomOnlineTrace = null;
		TraceEntry nearestNeighbour = null;
		LinkedList<Double> NN = new LinkedList<Double>();
		GeoPosition A = new GeoPosition(0.0,0.0,0.0);
		GeoPosition B = new GeoPosition(50.0,50.0,0.0);
		PositioningError tempError = new PositioningError(A,B);
		GeoPosition nearN = null;
		double aff = 0.0;
		
		
		//Construct parsers
		File offlineFile = new File(offlinePath);
		Parser offlineParser = new Parser(offlineFile);
		System.out.println("Offline File: " +  offlineFile.getAbsoluteFile());
		
		File onlineFile = new File(onlinePath);
		Parser onlineParser = new Parser(onlineFile);
		System.out.println("Online File: " + onlineFile.getAbsoluteFile());
		
		
		
		//Construct trace generator
		TraceGenerator tg;
		
		
		try {
			int offlineSize = 1;
			int onlineSize = 1;
			
			tg = new TraceGenerator(offlineParser, onlineParser,offlineSize,onlineSize);
			
			//Generate traces from parsed files
			tg.generate();
			
			//Iterate the trace generated from the offline file
			List<TraceEntry> offlineTrace = tg.getOffline();	
			List<TraceEntry> onlineTrace = tg.getOnline();	
			
			randomOnlineTrace = onlineTrace.get((int) (Math.random() * onlineTrace.size()) + 1 );
			System.out.println("randomOnlineTrace: " + randomOnlineTrace);
			
			for(TraceEntry entry: offlineTrace) {
				
				PositioningError positioningError = new PositioningError(entry.getGeoPosition(), randomOnlineTrace.getGeoPosition());
				NN.add(positioningError.getPositioningError());
				
				if(tempError != null && (positioningError.getPositioningError() <= tempError.getPositioningError()) ) {
					
					tempError = positioningError;
					nearN = entry.getGeoPosition();// + tempError.getPositioningError();
					aff = tempError.getPositioningError();
				}
				
				
				//Print out coordinates for the collection point and the number of signal strength samples
			//	System.out.println(entry.getGeoPosition().toString() + " - " + entry.getSignalStrengthSamples().size());				
			}
			System.out.println("nearN"+ nearN);
			System.out.println("aff: "+aff);
			
			PositioningError testFehler = new PositioningError(nearN, randomOnlineTrace.getGeoPosition());
			System.out.println("Testfehler: "+ testFehler);
			//Iterate the trace generated from the online file
			
			for(TraceEntry entry: onlineTrace) {
				//Print out coordinates for the collection point and the number of signal strength samples
			//	System.out.println(entry.getGeoPosition().toString() + " - " + entry.getSignalStrengthSamples().size());
			}
			NN.sort(null);
			
				System.out.println("NN"+NN);
			
			
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		System.out.println();
		
	}

}