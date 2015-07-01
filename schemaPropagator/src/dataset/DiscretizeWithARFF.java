package dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class DiscretizeWithARFF {

	private String fileInputScheme = "";
	private String fileInputRaw = "";
	private String fileOutput = "";
	ArrayList<Double[]> attCutoffs = new ArrayList<Double[]>();
	ArrayList<String[]> attBins = new ArrayList<String[]>();
	ArrayList<String> attLabels = new ArrayList<String>();
	Instances dataSchema;
	Instances dataRaw;
	Instances outData;
	
	String classColName = "";
	List<String> classLabels = new ArrayList<String>();


	private void readInputSchema(){

		//Read File
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileInputScheme));
			dataSchema = new Instances(reader);
			reader.close();

			//Set class
			dataSchema.setClassIndex(dataSchema.numAttributes()-1);


			//Get Labels and Cutoffs
			for(int i = 0; i < dataSchema.numAttributes()-1; i++){
				//Labels
				attLabels.add(dataSchema.attribute(i).name());

				//Cutoffs
				String[] cutString = new String[dataSchema.attribute(i).numValues()];
				Double[] cutDouble = new Double[dataSchema.attribute(i).numValues()-1];

				for(int j=0; j < dataSchema.attribute(i).numValues(); j++){
					cutString[j] = dataSchema.attribute(i).value(j);
					if(j < dataSchema.attribute(i).numValues()-1){
						if(hasCutoff(cutString[j])){
							cutDouble[j] = getCutoff(cutString[j]);
						}
					}
				}
				attCutoffs.add(cutDouble);
				attBins.add(cutString);
			}


			//tools.Tools.print(attLabels);

		} catch (IOException e) {
			System.out.println("Unable to read scheme file");
			e.printStackTrace();
		}

	}

	private void readInputRaw(){


		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileInputRaw));
			dataRaw = new Instances(reader);
			reader.close();

			//Set class
			dataRaw.setClassIndex(dataRaw.numAttributes()-1);

		} catch (IOException e) {
			System.out.println("Unable to read raw file");
			e.printStackTrace();
		}
	}


	private void writeARFF() {


		BufferedWriter writer;
		try {
			writer = new BufferedWriter(
					new FileWriter(fileOutput));
			writer.write(outData.toString());
			writer.newLine();
			writer.flush();
			writer.close();

		} catch (IOException e) {
			System.out.println("--File not written");
			e.printStackTrace();
		}


		System.out.println("[Done]");

	}


	private Boolean hasCutoff(String line){
		Boolean c=false;
		String rep = line.replace("'","");
		rep = rep.replace("\\","");
		rep = rep.replace("]","");
		String[] bits = rep.split("-");
		String lastOne = bits[bits.length-1];

		try{
			Double.parseDouble(lastOne);
			c = true;
		}
		catch(NumberFormatException nfe){
			c = false;
		}


		return c;
	}


	private Double getCutoff(String line){
		Double c;
		String rep = line.replace("'","");
		rep = rep.replace("\\","");
		rep = rep.replace("]","");
		rep = rep.replace("(","");
		String[] bits = rep.split("-");
		String lastOne = bits[bits.length-1];

		if(bits.length == 4){
			lastOne = "-"+lastOne;
		}

		c=Double.parseDouble(lastOne);

		//System.out.println(rep+" >> "+c+" >> "+bits.length);
		return c;
	}


	private int getDiscreteValue(double value, int index){

		int disc = 0;



		for(int i = 0; i < attCutoffs.get(index).length; i++){
			//System.out.println(attCutoffs.get(index)[i]);
			if(value < attCutoffs.get(index)[i]){
				break;
			}
			else{
				disc++;
			}
		}
		return disc;
	}

	private void removeAndDiscretize(){ 

		//Empty set of instances with the new Scheme
		initializeOutData();

		int smallCount = 0;
		int largeCount = 0;
		int totalInst = dataRaw.numInstances();


		//Select attributes from Raw
		/*
		String[] rawAttributes = new String[dataRaw.numAttributes()];
		for(int i = 0; i < dataRaw.numAttributes(); i++){
			rawAttributes[i] = dataRaw.attribute(i).name();
		}

		//Select the attribute index from Raw
		int[] rawIndexes = new int[attLabels.size()];
		for(int i = 0; i < attLabels.size(); i++){
			rawIndexes[i] = Arrays.asList(rawAttributes).indexOf(attLabels.get(i));
			System.out.println(attLabels.get(i)+" is in "+ rawIndexes[i]);
		}
		*/


		//Select only the chosen attributes and discretize
		//Select only the chosen attributes and discretize
				for(int rawInstanceIndex = 0; rawInstanceIndex < dataRaw.numInstances(); rawInstanceIndex++){
					Instance newInst = new DenseInstance(outData.numAttributes());

					for(int outAttIndex = 0; outAttIndex < outData.numAttributes(); outAttIndex++){

						String outDataName = outData.attribute(outAttIndex).name();
						int rawAttIndex = dataRaw.attribute(outDataName).index();
						//String rawAttName = dataRaw.attribute(outDataName).name();


						//System.out.println(dataRaw.attribute(rawAttIndex).name() + " "+ dataRaw.attribute(rawAttIndex).isNumeric());

						//If it is numeric, then discretize
						if(dataRaw.attribute(rawAttIndex).isNumeric()){

							// Set instance's values for the attributes
							//System.out.println(dataRaw.instance(rawInstanceIndex).value(rawAttIndex) + " ==? " + Double.isNaN(dataRaw.instance(rawInstanceIndex).value(rawAttIndex)));
							if(!Double.isNaN(dataRaw.instance(rawInstanceIndex).value(rawAttIndex))){
								double rawValue = dataRaw.instance(rawInstanceIndex).value(rawAttIndex);
								int discBinIndex = getDiscreteValue(rawValue, outAttIndex);
								String rawDisc = attBins.get(outAttIndex)[discBinIndex];
								
								//System.out.println( rawAttName+ "[" + rawValue + "]:"+discBinIndex+ "-> "+ rawDisc);
								newInst.setValue(outData.attribute(outAttIndex), rawDisc);
							}
						}
						//if it is not numeric, save the value as is. 
						else{
							String value = dataRaw.instance(rawInstanceIndex).stringValue(rawAttIndex);

							//System.out.print("("+i+", "+j+"): ");
							//System.out.print(dataScheme.attribute(dataRaw.attribute(rawindex).name()));
							//System.out.print(", "+value+", "+containsValue(value, dataScheme.attribute(dataRaw.attribute(rawindex).name())));
							//int index = dataSchema.get(0).attribute(dataRaw.attribute(rawAttIndex).name()).indexOfValue(value);
							//System.out.println(", "+value+", "+ index);
							//if(index > -1){
								newInst.setValue(outData.attribute(outAttIndex), value);
								//outData.setValue(dataSchema.get(0).attribute(dataRaw.attribute(rawAttIndex).name()), value);
							//}
						}

					}//for ends, looping the attributes

					//add class value
					//newInst.setValue(outData.attribute(classColName), dataRaw.instance(rawInstanceIndex).value(dataRaw.classAttribute()));
					//outData.setValue(dataScheme.classAttribute(), dataRaw.instance(rawInstanceIndex).value(dataRaw.classAttribute()));

					outData.add(newInst);





					System.out.print(".");
					smallCount++;
					largeCount++;
					if (largeCount == 10){
						System.out.println("["+((int) smallCount*100/totalInst)+"%]");
						largeCount = 0;
					}

				}//for ends, looping the instances
				System.out.println("[100%]");

	}
	
	private void initializeOutData() {
		
		//add variables from first schema
		ArrayList<Attribute> attValues = new ArrayList<Attribute>();
		
		// add common variables
		for(int j = 0; j < attLabels.size(); j++){
			//System.out.println(attLabels.get(j));
			String attributeName = attLabels.get(j);
			
			List<String> attributeValues = new ArrayList<String>();
			for(int k = 0; k < attBins.get(j).length; k++){
				attributeValues.add(attBins.get(j)[k]);
			}
			
			attValues.add(new Attribute(attributeName, attributeValues));
		}
		
		//add class variable
		int classIndexInFirstSchema = dataSchema.numAttributes()-1;
		//System.out.println("classIndex: "+classIndexInFirstSchema);
		classColName = dataSchema.attribute(classIndexInFirstSchema).name();
		for(int h = 0; h < dataSchema.attribute(classIndexInFirstSchema).numValues(); h++){
			String value = dataSchema.attribute(classIndexInFirstSchema).value(h);
			System.out.println(h+": "+value);
			classLabels.add(value);
		}
		attValues.add(new Attribute(classColName, classLabels));
		
		//initialize outData
		outData = new Instances("merged",attValues, 0);
		
		
		
		
		
		System.out.println("size of out: "+outData.numAttributes());
	}

	/*	private boolean containsValue(String value, Attribute attribute) {
		Boolean c = false;
		for(int k = 0; k < attribute.numValues(); k++){
			//System.out.println("<<"+attribute.value(k));
			if(attribute.value(k) == value){
				c = true;
			}
		}

		System.out.println(value + " in "+attribute.toString() + " == "+c);
		return c;
	}*/

	public void runner(String inputSchemeFile, String inputRawFile, String outputFile) {
		this.fileInputScheme = inputSchemeFile;
		this.fileInputRaw = inputRawFile;


		readInputSchema();
		readInputRaw();

		System.out.println("Transferring ARFF-Scheme to ARFF-Raw file...");
		removeAndDiscretize();

		this.fileOutput = outputFile;
		writeARFF();

	}

}
