package main;

import tools.Util;
import dataset.DiscretizeWithARFF;
import dataset.DiscretizeWithNAM;

/**
 *
 * @author arturolp
 * 
 * email: arl68@pitt.edu
 * 
 */

public class Propagator {

	public static void showInfo(){
		System.out.println("Expected commands format: -inputSchema data/schema.arff -inputRaw data/mydata.arff -output data/mydata-schema.arff");
		System.out.println("   -inputSchema data/schema.arff \t data.arff or data.nam is the input file in NAM or ARFF format of the discretization scheme");
		System.out.println("   -inputRaw data/mydata.arff \t the file to be discretized");
		System.out.println("   -output data/mydata-schema.arff \t The new name of the output. The extension can be ARFF or CAS. Default is ARFF");
	}

	public static void main(String args[]) {

		if((args.length != 2) && (args.length != 4) && (args.length != 6) ){
			System.err.println("Incorrect number of arguments.");
			showInfo();
			System.exit(1);
		}


		String inputSchemeFile = "";
		String inputSchemeType = "";
		String inputRawFile = "";
		String outputFile = "";

		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-inputSchema")){
				inputSchemeFile = args[(1+i)];
				inputSchemeType = Util.getFileExtension(inputSchemeFile);
			}
			else if(args[i].equalsIgnoreCase("-inputRaw")){
				inputRawFile = args[(1+i)];
			}
			else if(args[i].equalsIgnoreCase("-output")){
				outputFile = args[++i];
			}

		}


		if(inputSchemeType.equals("nam")){
			DiscretizeWithNAM prop = new DiscretizeWithNAM();
			prop.runner(inputSchemeFile, inputRawFile, outputFile);
		}
		else if(inputSchemeType.equals("arff")){
			DiscretizeWithARFF prop = new DiscretizeWithARFF();
			prop.runner(inputSchemeFile, inputRawFile, outputFile);
		}
	}


}
