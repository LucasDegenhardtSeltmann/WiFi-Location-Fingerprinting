package applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import java.lang.Math;


import org.pi4.locutil.io.TraceGenerator;
import org.pi4.locutil.trace.Parser;
import org.pi4.locutil.trace.TraceEntry;
import org.pi4.locutil.trace.SignalStrengthSamples;

import org.pi4.locutil.*;

/**
 * Example of how to use LocUtil
 * @author mikkelbk
 */

public class empirical_FP_KNN {

	/**
	 * Execute example
	 * @param args
	 */
	public static void main(String[] args) {
		
		String offlinePath = "data/MU.1.5meters.offline.trace", onlinePath = "data/MU.1.5meters.online.trace";
		TraceEntry randomOnlineTrace = null;
		String [] apMac = { "00:14:BF:B1:7C:54",
							"00:16:B6:B7:5D:8F",
							"00:14:BF:B1:7C:57",
							"00:14:BF:B1:97:8D",
							"00:16:B6:B7:5D:9B",
							"00:14:6C:62:CA:A4",
							"00:14:BF:3B:C7:C6",
							"00:14:BF:B1:97:8A",
							"00:14:BF:B1:97:81",
							"00:16:B6:B7:5D:8C",
							"00:11:88:28:5E:E0" };
		
		//Construct parsers
		File offlineFile = new File(offlinePath);
		Parser offlineParser = new Parser(offlineFile);
		System.out.println("Offline File: " +  offlineFile.getAbsoluteFile());
		
		File onlineFile = new File(onlinePath);
		Parser onlineParser = new Parser(onlineFile);
		System.out.println("Online File: " + onlineFile.getAbsoluteFile());
		
		int k = Integer.parseInt(JOptionPane.showInputDialog("Zahl f�r k eingeben: "));
		
		
		//Construct trace generator
		TraceGenerator tg;
		
		
		try {
			int offlineSize = 110;
			int onlineSize = 110;
			
			tg = new TraceGenerator(offlineParser, onlineParser,offlineSize,onlineSize);
			
			//Generate traces from parsed files
			tg.generate();
			
			//Iterate the trace generated from the offline file
			List<TraceEntry> offlineTrace = tg.getOffline();	
			List<TraceEntry> onlineTrace = tg.getOnline();	
	
			randomOnlineTrace = onlineTrace.get((int) (Math.random() * onlineTrace.size()) + 1 );
			System.out.println("randomOnlineTrace: " + randomOnlineTrace);
			
			
			HashMap<GeoPosition, Double> estimatedPosition = new HashMap<GeoPosition, Double>();
			for(TraceEntry entry: offlineTrace) {
							
				double dist = 0;
				for(int i=0; i<apMac.length; i++) {
					
					//Wenn Offline-Fingerprint Signalmessung von MACAdresse besitzt
					if(entry.getSignalStrengthSamples().containsKey(MACAddress.parse(apMac[i]))) {
						double offlineTraceSS = entry.getSignalStrengthSamples().getAverageSignalStrength(MACAddress.parse(apMac[i]));
						
					//Wenn Offline-Fingerprint Signalmessung von MACAdresse besitzt und Online-Fingerprint Signalmessung von MACAdresse besitzt
						if(randomOnlineTrace.getSignalStrengthSamples().containsKey(MACAddress.parse(apMac[i]))) {
							double onlineTraceSS = randomOnlineTrace.getSignalStrengthSamples().getAverageSignalStrength(MACAddress.parse(apMac[i]));
				
								dist = dist+((onlineTraceSS-(offlineTraceSS))*(onlineTraceSS-(offlineTraceSS)));
						}
						
					//Wenn Offline-Fingerprint Signalmessung von MACAdresse besitzt	aber Online-Fingerprint keine Signalmessung zu MACAdresse besitzt
						else
						{
				//			dist = dist+((-90.0-(offlineTraceSS))*(-90.0-(offlineTraceSS)));
						}
					}
					
					//Wenn Offline-Fingerprint keine Signalmessung von MACAdresse besitzt, aber Online-Fingerprint Signalmessung von MACAdresse hat
					else if(randomOnlineTrace.getSignalStrengthSamples().containsKey(MACAddress.parse(apMac[i]))) {
						double onlineTraceSS = randomOnlineTrace.getSignalStrengthSamples().getAverageSignalStrength(MACAddress.parse(apMac[i]));
						
						dist = dist+((onlineTraceSS-(-90.0))*(onlineTraceSS-(-90.0)));
					}
					
					
				}
				//System.out.println(entry.getGeoPosition());
				dist = Math.sqrt(dist);
				//System.out.println(dist);
				estimatedPosition.put(entry.getGeoPosition(), dist);
			
		/*	for(SignalStrengthSamples s : samples) {
				String st = s.getSignalStrengthValues(MACAddress.parse("00:11:88:28:5E:E0")).toString();
				st = st.substring(1, st.length()-1);
				System.out.println("Gehts? "+st);
				}
				*/
			
			}
			
		//	estimatedPosition.entrySet().forEach(entry -> {
		//	    System.out.println(entry.getKey() + " " + entry.getValue());
		//	});
			
			nearestNeighbour(randomOnlineTrace, estimatedPosition, k);
				

			//write(randomOnlineTrace.getGeoPosition(), nearestNeighbour);
			//Iterate the trace generated from the online file
			
			//for(TraceEntry entry: onlineTrace) {
			//Print out coordinates for the collection point and the number of signal strength samples
			//	System.out.println(entry.getGeoPosition().toString() + " - " + entry.getSignalStrengthSamples().size());
			//}

			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		System.out.println();
		
	}
	
	public static void write(GeoPosition realPosition, GeoPosition estimatedPosition)
	{
		try {
			File file = new File("empirical_FP_KNN.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			else {
				System.out.println("File already exists");
			}
			
			
			FileWriter fw = new FileWriter(file, true);
			fw.write("RealPosition:"+realPosition);
			fw.write(";");
			fw.write("EstimatedPosition:"+estimatedPosition);
			fw.write(System.getProperty( "line.separator" ));
			fw.close();
		}
		catch(FileNotFoundException e)
	    {
	        System.out.println("File Not Found");
	     }
	    catch(IOException e)
	    {
	        System.out.println("something messed up");
	    }
	}
	
	public static void nearestNeighbour(TraceEntry realPosition, HashMap<GeoPosition, Double> estimatedPositionsMap, int k) {
	
		
		HashMap<GeoPosition, Double> kNN = new LinkedHashMap<GeoPosition, Double>();
		
		GeoPosition [] aEstimatedPositionsMap = new GeoPosition[estimatedPositionsMap.size()];
		double[] aEukDist = new double[estimatedPositionsMap.size()];
		
		int index = 0;
		for (HashMap.Entry<GeoPosition, Double> entry : estimatedPositionsMap.entrySet()) {
			aEstimatedPositionsMap[index] = entry.getKey();
			aEukDist[index] = entry.getValue();
		    index++;
		}
		/*for(int i=0;i<aEukDist.length;i++) {
			System.out.println(aEukDist[i]);
		}*/
		
		Arrays.sort(aEukDist);
		System.out.println();
		
	/*	for(int i=0;i<aEukDist.length;i++) {
			System.out.println(aEukDist[i]);
		}*/
		
		double x=0;
		double y=0;
		double z=0;
		
		System.out.println(aEukDist[0]);
		System.out.println(aEukDist[1]);
		System.out.println(aEukDist[2]);
		
		for(HashMap.Entry<GeoPosition, Double> entry : estimatedPositionsMap.entrySet()) {
			for(int i=0; i<k; i++) {
				if(entry.getValue() == aEukDist[i]) {
					kNN.put(entry.getKey(),entry.getValue());
					x=x+entry.getKey().getX();
					System.out.println(x);
					y=y+entry.getKey().getY();
					System.out.println(y);
					z=z+entry.getKey().getZ();
					System.out.println(z);
				}
			}
		}
		
		GeoPosition estimatedPosition = new GeoPosition((x)/k , (y)/k , (z)/k);
		
		System.out.println(estimatedPosition);
			
								
						
				
		for(HashMap.Entry<GeoPosition, Double> e : kNN.entrySet()) {
			
			System.out.println("NearestNeighbour: "+e.getKey()+" "+e.getValue());
		}
		
		write(realPosition.getGeoPosition(), estimatedPosition);
		
	}

}


