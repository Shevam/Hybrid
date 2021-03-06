import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class StaticMethods
{
	public static String getSequence(String SEQUENCE_FILE)
	{
		try (Scanner fileIn = new Scanner(new File(SEQUENCE_FILE))) 
		{
			String currentLine = "";
			StringBuilder sequence = new StringBuilder();
			
			while (fileIn.hasNextLine())
			{
				currentLine = fileIn.nextLine();
				
				if (!currentLine.startsWith(">"))
					sequence.append(currentLine.trim());
			}
			return sequence.toString();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void generateReads(String SEQUENCE_FILE, int READ_SIZE, int MINIMUM_OVERLAP_LENGTH,String OUTPUT_FILE)
	{
		String sequence = getSequence(SEQUENCE_FILE);
		if(sequence!=null && !sequence.equals("") && sequence.length()>READ_SIZE)
		{
			try 
			{
				Formatter outfile = new Formatter(new File(OUTPUT_FILE));
				int sequenceSection=0, readCount=0;
				sequenceSection++;
				int aRandomNumber = new Random().nextInt(READ_SIZE - MINIMUM_OVERLAP_LENGTH);
				while(sequenceSection*READ_SIZE<sequence.length())
				{
					aRandomNumber = new Random().nextInt(READ_SIZE - MINIMUM_OVERLAP_LENGTH);
					
					if (((sequenceSection)*READ_SIZE)+aRandomNumber > sequence.length()){
						readCount++;
						String circularRead = sequence.substring(((sequenceSection-1)*READ_SIZE)+aRandomNumber);
						int remainingNoOfChars = READ_SIZE - circularRead.length();
						circularRead += sequence.substring(0, remainingNoOfChars);
						outfile.format(">r%d.1%n%s%n", readCount, circularRead);
						break;
					}
					else{
						readCount++;
						outfile.format(">r%d.1%n%s%n", readCount,sequence.substring(((sequenceSection-1)*READ_SIZE)+aRandomNumber, (sequenceSection*READ_SIZE)+aRandomNumber));
						readCount++;
						outfile.format(">r%d.1%n%s%n", readCount,sequence.substring((sequenceSection-1)*READ_SIZE, sequenceSection*READ_SIZE));
						sequenceSection++;
					}
				}
				
				System.out.println("Number of reads generated: "+readCount);
				outfile.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getOverlap(String startString, String endString, int minimumOverlapLength) {
		int endIndex = endString.length() - 1;
		while (endIndex >= minimumOverlapLength	&& !startString.endsWith(endString.substring(0, endIndex)))
			endIndex--;
		if(!startString.endsWith(endString.substring(0, endIndex)))
			return null;
		return endString.substring(0, endIndex);
	}
	
	public static String getSuperString(String startString, String endString)
    {
        String result = startString;
 
        int endIndex = endString.length() - 1;
        while(endIndex > 0 && !startString.endsWith(endString.substring(0, endIndex)))
            endIndex--;
 
        if(endIndex > 0)
            result += endString.substring(endIndex);
        else
            result += endString;
 
        return result;
    }
	
	public static void constructGraph(File readsFile, int minimumOverlapLength) 
	{
		try (Scanner fileIn = new Scanner(readsFile)) 
		{
			String currentLine = "";
			StringBuilder read = new StringBuilder();
			int readCount = 0;
			new OverlapGraph(minimumOverlapLength);
			
			while (fileIn.hasNextLine())
			{
				currentLine = fileIn.nextLine();

				if (currentLine.startsWith(">")) {
					if (!read.toString().equals("")) {
						OverlapGraph.getInstance().addNode(read.toString().toUpperCase());
						readCount++;
					}
					read = new StringBuilder();
				} 
				else
					read.append(currentLine.trim());
			}

			if (!read.toString().equals("")) {
				OverlapGraph.getInstance().addNode(read.toString().toUpperCase());
				readCount++;
			}

			System.out.println("Number of reads processed: " + readCount);
		}
		catch (FileNotFoundException e) {
			System.err.println("File not found: " + readsFile);
		}
	}
	
	public static void generateContigs(String OUTPUT_FILE) 
    {
		BufferedWriter writer;
		LinkedList<Node> contigNodeList;
		Node currentNode;
		int contigCount = 0;
		try
		{
			writer = new BufferedWriter(new FileWriter(new File(OUTPUT_FILE)));
			while (true)
			{
				contigNodeList = new LinkedList<Node>();
				currentNode = OverlapGraph.getInstance().getLeastIndegreeUnvisitedNode();
				if(currentNode == null)
					break;
				
		        while(true)
		        {
		        	contigNodeList.add(currentNode);
			        currentNode.setVisited(true);
			        
			        currentNode = OverlapGraph.getInstance().getNextNodeWithHighestOverlapLength(currentNode);
		        	if(currentNode == null)
		        		break;
		        }
		        contigCount++;
		        
		        if(!contigNodeList.isEmpty())
				{
		        	writer.write(">c" + contigCount + ".1_NodeCount_"+ contigNodeList.size() +"\n");
					writer.write(contigNodeList.getFirst().getRead());
					for(int i=1; i<contigNodeList.size(); i++)
						writer.write(contigNodeList.get(i).getRead().substring(OverlapGraph.getInstance().getOverlapLength(contigNodeList.get(i-1), contigNodeList.get(i))));
					
					writer.flush();
					writer.write("\n");
				} 
			}
			System.out.println("Number of contigs generated: " + contigCount);
			writer.close();
		}
		catch (FileNotFoundException e) {
			System.err.println("File not found: " + OUTPUT_FILE);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
}
