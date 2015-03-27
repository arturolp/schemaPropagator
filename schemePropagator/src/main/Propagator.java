package main;

import tools.Util;
import dataset.DiscretizeWithARFF;
import dataset.DiscretizeWithNAM;

/**
 *
 * @author Arturo Lopez Pineda
 * 
 * email: arl68@pitt.edu
 * 
 */

public class Propagator {

	public static void showInfo(){
		System.out.println("Expected commands format: -inputScheme data.nam -inputRaw data.arff -outputNAME newName.arff [-output Desktop/data/]");
		System.out.println("   -inputScheme data.nam \t data.arff or data.nam is the input file in NAM or ARFF format of the discretization scheme");
		System.out.println("   -inputRaw data.arff \t the file to be discretized");
		System.out.println("   -outputName newName.arff \t The new name of the output. The extension can be ARFF or CAS. Default is ARFF");
		System.out.println("   -output Desktop/results/ \t The output Path where the new CAS or ARFF is going to be created. Default is the same as inputRawFile");
	}

	public static void main(String args[]) {

		if((args.length != 2) && (args.length != 4) && (args.length != 6) && (args.length != 8) ){
			System.err.println("Incorrect number of arguments.");
			showInfo();
			System.exit(1);
		}


		String inputSchemeFile = "";
		String inputSchemeType = "";
		String inputRawFile = "";
		String outputPath = "";
		String outputName = "";

		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-inputScheme")){
				inputSchemeFile = args[(1+i)];
				inputSchemeType = Util.getFileExtension(inputSchemeFile);
			}
			else if(args[i].equalsIgnoreCase("-inputRaw")){
				inputRawFile = args[(1+i)];
			}
			else if(args[i].equalsIgnoreCase("-output")){
				outputPath = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-outputName")){
				outputName = args[++i];
			}

		}


		
		if(outputPath.equals("")){
			outputPath = Util.fileNameStemAndSuffix(inputRawFile, "/")[0];
		}

		if(inputSchemeType.equals("nam")){
			DiscretizeWithNAM prop = new DiscretizeWithNAM();
			prop.runner(inputSchemeFile, inputRawFile, outputPath, outputName);
		}
		else if(inputSchemeType.equals("arff")){
			DiscretizeWithARFF prop = new DiscretizeWithARFF();
			prop.runner(inputSchemeFile, inputRawFile, outputPath, outputName);
		}
	}


}
