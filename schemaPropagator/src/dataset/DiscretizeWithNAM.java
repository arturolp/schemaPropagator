package dataset;

import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import tools.FileManager;


public class DiscretizeWithNAM {

	private String fileInputNAM = "";
	private String fileInputARFF = "";
	private String fileOutput = "";
	Instances data;
	Instances outData;
	ArrayList<Double[]> attCutoffs;
	ArrayList<String> attLabels;
	String classColName;
	String[] classLabels;

	private void updateOutput(String stream) {

		FileManager.write(fileOutput, stream);
		System.out.println(fileOutput + " [created]");
	}

	private void appendOutput(String stream) {

		FileManager.append(fileOutput, stream);
	}

	private void readInputARFF(){

		DataSource source;
		try {
			source = new DataSource(this.fileInputARFF);
			this.data = source.getDataSet();
			if (data.classIndex() == -1){
				data.setClassIndex(data.numAttributes() - 1);
			}

		} catch (Exception e) {
			System.out.println("==ERROR: ");
			e.printStackTrace();
		}

	}



	private void writeCAS() {
		System.out.println("Writing CAS file "+this.fileOutput+"...");

		//First Line: Num attributes
		updateOutput(""+outData.numAttributes());
		//appendOutput("" + (attLabels.size()+1)+"\n");

		//Second Line: Array of Num of states of each attribute
		appendOutput(" ");
		for(int i = 0; i < attLabels.size(); i++){
			int numStates = attCutoffs.get(i).length+1;
			appendOutput(numStates+ " ");
		}
		appendOutput(""+classLabels.length);
		appendOutput("\n");

		//Third Line: Num of instances
		appendOutput(""+outData.numInstances()+"\n");

		//Fourth Line: The dataset in nominal values
		for(int i = 0; i< outData.numInstances(); i++){
			String buf = "";
			for(int j = 0; j < outData.numAttributes(); j++){
				if(outData.classAttribute()!=outData.attribute(j)){
					//System.out.print(" "+ outData.instance(i).value(j));
					buf = buf + " "+getDiscreteValue(outData.instance(i).value(j),j);
				}
				else{ //for the class
					//System.out.print(" "+ outData.instance(i).value(j));
					buf = buf + " "+((int)outData.instance(i).value(j) +1);
				}
			}
			//System.out.println("");
			appendOutput(buf+"\n");
		}


		System.out.println("Done.");

	}

	private void readInputNAM(){

		String file = FileManager.read(fileInputNAM);
		String[] lines = file.split("\r?\n|\r");

		attLabels = new ArrayList<String>();
		attCutoffs = new ArrayList<Double[]>();

		int index=1;

		//Get Attributes
		for(int i = 0; i < Integer.parseInt(lines[0])-1; i++){

			attLabels.add(lines[index]);

			int offset = Integer.parseInt(lines[index+1]);
			Double[] elements = new Double[offset-1];
			for(int j = 0; j < offset-1; j++){
				elements[j] = getCutoff(lines[index+j+2]);
			}
			attCutoffs.add(elements);
			index = index+offset+2;
		}

		//Get Class
		classColName = lines[index];
		classLabels = new String[Integer.parseInt(lines[index+1])];
		int offset = Integer.parseInt(lines[index+1]);
		for(int j = 0; j < offset; j++){
			classLabels[j] = lines[index+j+2];
		}

	}

	private Double getCutoff(String line){
		Double c;
		String rep = line.replace("]","");
		String[] bits = rep.split("-");
		String lastOne = bits[bits.length-1];

		c=Double.parseDouble(lastOne);

		return c;
	}

	private void removeNotNAMattributes(){

		outData = new Instances(data);

		String removeList = "";

		for(int i = 0; i < outData.numAttributes(); i++){

			if(outData.classAttribute()!=outData.attribute(i)){
				if(!attLabels.contains(outData.attribute(i).name())){
					//Remove elements that are not in the scheme
					//outDatade.deleteAttributeAt(i);
					removeList = removeList + "," + (i+1);
				}
			}
		}
		System.out.println(" Removing attributes ");

		Remove remove = new Remove();
		remove.setAttributeIndices(removeList);

		try {
			remove.setInputFormat(data);
			outData = Filter.useFilter(data, remove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int getDiscreteValue(double value, int index){

		int disc = 1;

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


	public void runner(String inputNAM, String inputARFF, String outputFile) {
		this.fileInputNAM = inputNAM;
		this.fileInputARFF = inputARFF;


		readInputNAM();
		readInputARFF();

		System.out.println("Transferring NAM scheme to ARFF file...");
		removeNotNAMattributes();

		this.fileOutput = outputFile;
		writeCAS();

	}

}
