import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MainDriver {	
	public static void main(String args[]) {
		try {
			File inFile = new File("src/input2.txt");
			File outFile = new File("src/output2.txt");
			
			Scanner sc = new Scanner(inFile);
			String outputString = SyntaxParser.parseTypeDeclaration(sc);
			
			FileWriter fw = new FileWriter(outFile);
			fw.write(outputString);
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
