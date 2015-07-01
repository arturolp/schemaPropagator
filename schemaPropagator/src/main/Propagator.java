package main;

import java.util.ArrayList;

import tools.Util;
import dataset.DiscretizeWithARFF;
import dataset.DiscretizeWithMultipleARFF;
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
		System.out.println("Expected commands format: -inputSchema data/schema1.arff [data/schema2.arff] -inputRaw data/mydata.arff -output data/mydata-schema.arff");
		System.out.println("   -inputSchema data/schema1.arff [data/schema2.arff] \t data.arff or data.nam is the input file in NAM or ARFF format of the discretization schema. Multiple files are allowed, but only ARFF is supported");
		System.out.println("   -inputRaw data/mydata.arff \t the file to be discretized");
		System.out.println("   -output data/mydata-schema.arff \t The new name of the output. The extension can be ARFF or CAS. Default is ARFF");
	}

	public static void main(String args[]) {

		if((args.length < 6)){
			System.err.println("Insuficient number of arguments.");
			showInfo();
			System.exit(1);
		}


		ArrayList<String> inputSchemaFile = new ArrayList<String>(0);
		ArrayList<String> inputSchemaType = new ArrayList<String>(0);
		String inputRawFile = "";
		String outputFile = "";

		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-inputSchema")){
				if(i+1 < args.length){
					int j= i+1;
					while(j < args.length){
						if(args[j].startsWith("-")){
							break;
						}
						else{
							inputSchemaFile.add(args[j]);
							inputSchemaType.add(Util.getFileExtension(args[j]));
						}
						j++;
					}
				}
				else{
					System.err.println("-- No argumnet provided for -inputSchema");
					showInfo();
					System.exit(1);
				}
			}
			else if(args[i].equalsIgnoreCase("-inputRaw")){
				if(i+1 < args.length){
					inputRawFile = args[(1+i)];
				}
				else{
					System.err.println("-- No argumnet provided for -inputRaw");
					showInfo();
					System.exit(1);
				}
			}
			else if(args[i].equalsIgnoreCase("-output")){
				if(i+1 < args.length){
					outputFile = args[++i];
				}
				else{
					System.err.println("-- No argumnet provided for -output");
					showInfo();
					System.exit(1);
				}
			}

		}

		//For single input
		if(inputSchemaFile.size() == 1){
			if(inputSchemaType.get(0).equals("nam")){
				DiscretizeWithNAM prop = new DiscretizeWithNAM();
				prop.runner(inputSchemaFile.get(0), inputRawFile, outputFile);
			}
			else if(inputSchemaType.get(0).equals("arff")){
				System.out.println("Discretizing ARFF file...");
				DiscretizeWithARFF prop = new DiscretizeWithARFF();
				prop.runner(inputSchemaFile.get(0), inputRawFile, outputFile);
			}
		}
		
		//For multiple input
		if(inputSchemaFile.size() > 1){
			DiscretizeWithMultipleARFF prop = new DiscretizeWithMultipleARFF();
			prop.runner(inputSchemaFile, inputRawFile, outputFile);
		}
	}


}
