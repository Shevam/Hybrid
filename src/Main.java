import java.io.File;

public class Main {
	public static final String SEQUENCE_FILE = "BorreliaFull_CompleteSequence.fasta";
	public static final int READ_SIZE = 200;
	public static final String GENERATED_READS_FILE = "generatedReads.fna";
	public static final int MINIMUM_OVERLAP_LENGTH = 10;
	
	public static final String READS_FILE_NAME = "generatedReads.fna";
	//Filenames: BorreliaFull_ExactReads.fna BorreliaReduced_ExactReads.fna generatedReads.fna 
	public static final String OUTPUT_FILE = "generatedContigs.fasta";
	
	public static void main(String args[]) 
	{
		long readGenerationStartTime = System.nanoTime();
		StaticMethods.generateReads(SEQUENCE_FILE, READ_SIZE, MINIMUM_OVERLAP_LENGTH, GENERATED_READS_FILE);
		long readGenerationEndTime = System.nanoTime();
		System.out.println("Time to generate reads(ms): " + (readGenerationEndTime - readGenerationStartTime) / 1000000);
		
		long graphConstructionStartTime = System.nanoTime();
		StaticMethods.constructGraph(new File(READS_FILE_NAME), MINIMUM_OVERLAP_LENGTH);
		long graphConstructionEndTime = System.nanoTime();
		System.out.println("Time to construct graph(ms): " + (graphConstructionEndTime - graphConstructionStartTime) / 1000000);
		
		long contigGenerationStartTime = System.nanoTime();
		StaticMethods.generateContigs(OUTPUT_FILE);
		long contigGenerationEndTime = System.nanoTime();
		System.out.println("Time to generate contigs(ms): " + (contigGenerationEndTime - contigGenerationStartTime) / 1000000);
		
		System.out.println("Total execution time(ms): " + (contigGenerationEndTime - readGenerationStartTime) / 1000000);
	}
}
