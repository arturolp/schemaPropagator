package dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import tools.Tools;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;


public class DiscretizeWithARFF {

	private String fileInputScheme = "";
	private String fileInputRaw = "";
	private String fileOutput = "";
	ArrayList<Double[]> attCutoffs = new ArrayList<Double[]>();
	ArrayList<String[]> attBins = new ArrayList<String[]>();
	ArrayList<String> attLabels = new ArrayList<String>();
	Instances dataScheme;
	Instances dataRaw;
	Instances outData;
	String classColName;
	String[] classLabels;


	private void readInputScheme(){

		//Read File
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileInputScheme));
			dataScheme = new Instances(reader);
			reader.close();

			//Set class
			dataScheme.setClassIndex(dataScheme.numAttributes()-1);


			//Get Labels and Cutoffs
			for(int i = 0; i < dataScheme.numAttributes()-1; i++){
				//Labels
				attLabels.add(dataScheme.attribute(i).name());

				//Cutoffs
				String[] cutString = new String[dataScheme.attribute(i).numValues()];
				Double[] cutDouble = new Double[dataScheme.attribute(i).numValues()-1];

				for(int j=0; j < dataScheme.attribute(i).numValues(); j++){
					cutString[j] = dataScheme.attribute(i).value(j);
					if(j < dataScheme.attribute(i).numValues()-1){
						if(hasCutoff(cutString[j])){
							cutDouble[j] = getCutoff(cutString[j]);
						}
					}
				}
				attCutoffs.add(cutDouble);
				attBins.add(cutString);
			}

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
		String[] bits = rep.split("-");
		String lastOne = bits[bits.length-1];

		c=Double.parseDouble(lastOne);

		return c;
	}


	private int getDiscreteValue(double value, int index){

		int disc = 0;

		for(int i = 0; i < attCutoffs.get(index).length; i++){
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
		outData = new Instances(dataScheme, 0);

		int smallCount = 0;
		int largeCount = 0;
		int totalInst = dataRaw.numInstances();

		for(int i = 0; i < dataRaw.numInstances(); i++){

			// Create empty instance 
			Instance inst = new Instance(dataScheme.numAttributes());

			for(int j = 0; j < dataRaw.numAttributes(); j++){

				if(attLabels.contains(dataRaw.attribute(j).name())){

					if(dataRaw.attribute(j).isNumeric()){

						// Set instance's values for the attributes
						int index = dataScheme.attribute(dataRaw.attribute(j).name()).index();
						int valueIndex = getDiscreteValue(dataRaw.instance(i).value(j), index);
						//System.out.println(dataScheme.attribute(dataRaw.attribute(j).name()) + ">"+attBins.get(index)[valueIndex]);
						inst.setValue(dataScheme.attribute(dataRaw.attribute(j).name()), attBins.get(index)[valueIndex]);
					}
					else{
						String value = dataRaw.instance(i).stringValue(j);

						System.out.print("("+i+", "+j+"): ");
						System.out.print(dataScheme.attribute(dataRaw.attribute(j).name()));
						//System.out.print(", "+value+", "+containsValue(value, dataScheme.attribute(dataRaw.attribute(j).name())));
						int index = dataScheme.attribute(dataRaw.attribute(j).name()).indexOfValue(value);
						System.out.println(", "+value+", "+ index);
						if(index > -1){
							inst.setValue(dataScheme.attribute(dataRaw.attribute(j).name()), value);
						}
					}
				}
			}

			//add class
			inst.setValue(dataScheme.classAttribute(), dataRaw.instance(i).value(dataRaw.classAttribute()));

			outData.add(inst);

			System.out.print(".");
			smallCount++;
			largeCount++;
			if (largeCount == 10){
				System.out.println("["+((int) smallCount*100/totalInst)+"%]");
				largeCount = 0;
			}

		}
		System.out.println("[100%]");

	}

	private boolean containsValue(String value, Attribute attribute) {
		Boolean c = false;
		for(int k = 0; k < attribute.numValues(); k++){
			//System.out.println("<<"+attribute.value(k));
			if(attribute.value(k) == value){
				c = true;
			}
		}
		
		System.out.println(value + " in "+attribute.toString() + " == "+c);
		return c;
	}

	public void runner(String inputSchemeFile, String inputRawFile, String outputPath, String outputName) {
		this.fileInputScheme = inputSchemeFile;
		this.fileInputRaw = inputRawFile;
		String fileARFF = outputPath+"/"+outputName;


		readInputScheme();
		readInputRaw();

		System.out.println("Transferring ARFF-Scheme to ARFF-Raw file...");
		removeAndDiscretize();

		this.fileOutput = fileARFF;
		writeARFF();

	}

}
