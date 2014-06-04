import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class SyntaxParser {
	private static List<String> modifierList = Arrays.asList(new String[] {
			"public", "protected", "private", "static", "abstract", "final"});
	private static List<String> basicTypeList = Arrays.asList(new String[] {
			"byte", "short", "char", "int", "long", "float", "double", "boolean"});
	private static List<String> coiHeaderList = Arrays.asList(new String[] {
			"class", "enum", "interface"});
	private static List<String> statementHeaderList = Arrays.asList(new String[] {
			",", "if", "while", "do", "for", "break", "continue", "return"});
	private static List<String> prefixOpList = Arrays.asList(new String[] {
			"++", "--", "!", "~", "+", "-"});
	private static List<String> infixOpList = Arrays.asList(new String[] {
			"||", "&&", "|", "^", "&", "==", "!=", "<", ">", "<=", ">=", "<<", ">>",
			">>>", "+", "-", "*", "/", "%"});
	private static List<String> postfixOpList = Arrays.asList(new String[] {
			"++", "--"});
	private static List<String> assignmentOpList = Arrays.asList(new String[] {
			"=", "+=", "-=", "*=", "/=", "&=", "|=", "^=", "%=", "<<=", ">>=", ">>>="});
	private static List<Character> spCharList = Arrays.asList(new Character[] {
			'[', ']', '(', ')', '{', '}', ';', ','});
	private static List<Character> spCharList2 = Arrays.asList(new Character[] {
			'+', '-', '*', '/', '%', '<', '>', '!', '^', '=', '|', '&'});
	
	private static class VariableInfo {
		private static String modifier = "";
		private static String type = "";
		private static String name = "";
		private static StringBuilder valueSB = new StringBuilder();
		
		private static void setModifier(String newModifier) {
			modifier = newModifier;
		}
		private static void setType(String newType) {
			type = newType;
		}
		private static void setName(String newName) {
			name = newName;
		}
		private static String getModifier() {
			return modifier;
		}
		private static String getType() {
			return type;
		}
		private static String getName() {
			return name;
		}
		private static void clear() {
			name = "";
			valueSB = new StringBuilder();
		}
	}
	
	private static StringBuilder outputSB;
	private static char currentChar = 0;
	
	public static String parseTypeDeclaration(BufferedReader br) {
		outputSB = new StringBuilder();
		parseClassOrInterfaceDeclaration(br);
		return outputSB.toString();
	}
	
	private static String parseClassOrInterfaceDeclaration(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(modifierList.contains(nextString)) {
				VariableInfo.setModifier(nextString);
				nextString = getNextToken(br);
			}
			if(nextString.equals("class")) {
				return parseNormalClassDeclaration(br);
			}
			else {
				appendOutputString("Invalid declaration keyword\n");
				return null;
			}
		}
		else {
			appendOutputString("Class or interface not found\n");
			return null;
		}
	}
	
	private static String parseNormalClassDeclaration(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			VariableInfo.setName(nextString);
			
			appendOutputString("Modifier: " + VariableInfo.getModifier() + "\n");
			appendOutputString("Class Name: " + VariableInfo.getName() + "\n\n");
			VariableInfo.clear();
			
			
			if((nextString = getNextToken(br)) != null) {
				if(nextString.equals("{")) {
					return parseClassBody(br);
				}
				else {
					appendOutputString("Missing \'{\'\n");
					return null;
				}
			}
			else {
				appendOutputString("Class body not found\n");
				return null;
			}
		}
		else {
			appendOutputString("Class name not found\n");
			return null;
		}
	}
	
	private static String parseClassBody(BufferedReader br) {
		String nextString = parseClassBodyDeclaration(br);
		if(nextString != null && nextString.equals("}")) {
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \'}\'\n");
			return null;
		}
	}
	
	private static String parseClassBodyDeclaration(BufferedReader br) {
		return parseMemberDecl(br);
	}
	
	private static String parseMemberDecl(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(nextString.equals("}")) {
				return nextString;
			}
			
			do {
				nextString = parseModifiers(br, nextString);
				if(basicTypeList.contains(nextString)) {
					VariableInfo.setType(nextString);
					nextString = parseMethodOrFieldDecl(br);
				}
				else if(coiHeaderList.contains(nextString)) {
					nextString = parseNormalClassDeclaration(br);
				}
				else {
					VariableInfo.setName(nextString);
					nextString = parseConstructorDeclaratorRest(br);
				}
			} while(nextString != null && !nextString.equals("}"));
			
			return nextString;
		}
		else {
			appendOutputString("Missing \'}\'\n");
			return null;
		}
	}
	
	private static String parseModifiers(BufferedReader br, String nextString) {
		try {
			if(modifierList.contains(nextString)) {
				VariableInfo.setModifier(nextString);
				nextString = getNextToken(br);
			}
			else {
				VariableInfo.setModifier("private");
			}
			if(nextString.equals("static")) {
				nextString = getNextToken(br);
			}
			if(nextString.equals("final")) {
				nextString = getNextToken(br);
			}
			return nextString;
		}
		catch(NoSuchElementException e) {
			appendOutputString("Incomplete member declaration\n");
			return null;
		}
	}
	
	private static String parseMethodOrFieldDecl(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			VariableInfo.setName(nextString);
			return parseMethodOrFieldRest(br);
		}
		else {
			appendOutputString("Incomplete method or field declaration\n");
			return null;
		}
	}
	
	private static String parseMethodOrFieldRest(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(nextString.equals("(")) {
				return parseMethodDeclaratorRest(br);
			}
			else {
				nextString = parseFieldDeclaratorsRest(br, nextString);
				if(nextString != null && nextString.equals(";")) {
					return getNextToken(br);
				}
				else {
					appendOutputString("Missing \';\'\n");
					return null;
				}
			}
		}
		else {
			return null;
		}
	}
	
	private static String parseMethodDeclaratorRest(BufferedReader br) {
		appendOutputString("Modifier: " + VariableInfo.getModifier() + "\n");
		appendOutputString("Type: " + VariableInfo.getType() + "\n");
		appendOutputString("Method name: " + VariableInfo.getName() + "\n\n");
		VariableInfo.clear();
		
		String nextString = parseFormalParameters(br);
		return parseStatementHandler(br, nextString);
	}
	
	private static String parseStatementHandler(BufferedReader br, String currString) {
		String nextString = currString;
		if(nextString != null && nextString.equals("{")) {
			if((nextString = parseStatements(br)) != null && nextString.equals("}")) {
				return getNextToken(br);
			}
			else {
				appendOutputString("Missing \'}\'\n");
				return null;
			}
		}
		else {
			appendOutputString("Missing \'{\'\n");
			return null;
		}
	}
	
	private static String parseFormalParameters(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(nextString.equals(")") || 
			  ((nextString = parseFormalParameterDecls(br, nextString)) != null && 
			    nextString.equals(")"))) {
				return getNextToken(br);
			}
			else {
				appendOutputString("Missing \')\'\n");
				return null;
			}
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String parseFormalParameterDecls(BufferedReader br, String currString) {
		String nextString = currString;
		if(basicTypeList.contains(nextString)) {
			VariableInfo.setType(nextString);
			return parseFormalParameterDeclsRest(br);
		}
		else {
			appendOutputString("Parameter type not found\n");
			return null;
		}
	}
	
	private static String parseFormalParameterDeclsRest(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			VariableInfo.setName(nextString);
			
			if((nextString = getNextToken(br)) == null) {
				return null;
			}
			
			if(nextString.equals("[")) {
				do {
					nextString = handleCloseBracket(br);
				} while(nextString != null && nextString.equals("["));
				VariableInfo.setType(VariableInfo.getType() + " array");
			}
			
			appendOutputString("Type: " + VariableInfo.getType() + "\n");
			appendOutputString("Parameter variable: " + VariableInfo.getName() + "\n\n");
			VariableInfo.clear();
			
			if(nextString.equals(",")) {
				if((nextString = getNextToken(br)) == null) {
					return null;
				}
				else {
					return parseFormalParameterDecls(br, nextString);
				}
			}
			else {
				return nextString;
			}
		}
		else {
			appendOutputString("Variable not found\n");
			return null;
		}
	}
	
	private static String parseFieldDeclaratorsRest(BufferedReader br, String currString) {
		String nextString = parseVariableDeclaratorRest(br, currString);
		while(nextString != null && nextString.equals(",")) {
			nextString = parseVariableDeclarator(br);
		}
		return nextString;
	}
	
	private static String parseVariableDeclarator(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			VariableInfo.setName(nextString);
			if((nextString = getNextToken(br)) != null) {
				return parseVariableDeclaratorRest(br, nextString);
			}
			else {
				return null;
			}
		}
		else {
			appendOutputString("Variable identifier not found\n");
			return null;
		}
	}
	
	private static String parseVariableDeclaratorRest(BufferedReader br, String currString) {
		boolean isArray = false;
		
		String nextString = currString;
		if(nextString.equals("[")) {
			do {
				nextString = handleCloseBracket(br);
			} while(nextString != null && nextString.equals("["));
			
			isArray = true;
		}
		
		if(nextString != null && nextString.equals("=")) {
			nextString = parseVariableInitializer(br);
		}
		
		appendOutputString("Modifier: " + VariableInfo.getModifier() + "\n");
		if(isArray) {
			appendOutputString("Type: " + VariableInfo.getType() + " array\n");
		}
		else {
			appendOutputString("Type: " + VariableInfo.getType() + "\n");
		}
		appendOutputString("Class variable: " + VariableInfo.getName() + "\n");
		appendOutputString("Value: " + VariableInfo.valueSB.toString() + "\n\n");
		VariableInfo.clear();
		
		return nextString;
	}
	
	private static String handleCloseBracket(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("]")) {
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \']\'\n");
			return null;
		}
	}
	
	private static String handleCloseBracket2(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && 
				Character.isDigit(nextString.charAt(0))) {
			if((nextString = getNextToken(br)) != null && nextString.equals("]")) {
				return getNextToken(br);
			}
			else {
				appendOutputString("Missing \']\'\n");
				return null;
			}
		}
		else {
			appendOutputString("Missing array index\n");
			return null;
		}
	}
	
	private static String parseVariableInitializer(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) == null) {
			appendOutputString("Literal not found");
			return null;
		}
		
		if(nextString.equals("{")) {
			return parseArrayInitializer(br);
		}
		else {
			return parseExpression(br, nextString);
		}
	}
	
	private static String parseArrayInitializer(BufferedReader br) {
		VariableInfo.valueSB.append("{");
		String nextString = parseVariableInitializer(br);
		
		while(nextString.equals(",")) {
			VariableInfo.valueSB.append(", ");
			nextString = parseVariableInitializer(br);
		}
		
		if(nextString.equals("}")) {
			VariableInfo.valueSB.append("}");
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \'}\'\n");
			return null;
		}
	}
	
	private static String parseConstructorDeclaratorRest(BufferedReader br) {
		appendOutputString("Constructor name: " + VariableInfo.getName() + "\n\n");
		VariableInfo.clear();
		
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("(")) {
			nextString = parseFormalParameters(br);
			return parseStatementHandler(br, nextString);
		}
		else {
			appendOutputString("Missing \'(\'\n");
			return null;
		}
	}
	
	private static String parseStatements(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) == null) {
			appendOutputString("Block statement not found");
			return null;
		}
		
		do {
			nextString = parseStatement(br, nextString);
		} while(nextString != null && (statementHeaderList.contains(nextString) || 
				basicTypeList.contains(nextString) ||
				prefixOpList.contains(nextString) ||
				Character.isAlphabetic(nextString.charAt(0))));
		
		return nextString;
	}
	
	private static String parseStatement(BufferedReader br, String currString) {
		String nextString = currString;
		switch(nextString) {
			case ";":
				nextString = getNextToken(br);
				break;
			case "if":
				nextString = handleIfStatement(br);
				break;
			case "while":
				nextString = handleWhileStatement(br);
				break;
			case "do":
				nextString = handleDoStatement(br);
				break;
			case "for":
				nextString = handleForStatement(br);
				break;
			case "break":
				nextString = handleBreakStatement(br);
				break;
			case "continue":
				nextString = handleContinueStatement(br);
				break;
			case "return":
				nextString = handleReturnStatement(br);
				break;
			default:
				nextString = handleStatementExpression(br, nextString);
		}
		return nextString;
	}
	
	private static String handleIfStatement(BufferedReader br) {
		String nextString;
		if((nextString = handleParExpression(br)) != null && nextString.equals(")")) {
			if((nextString = parseStatementHandler(br, getNextToken(br))) != null && 
					nextString.equals("else")) {
				return parseStatementHandler(br, getNextToken(br));
			}
			else {
				return nextString;
			}
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String handleParExpression(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("(")) {
			return parseExpression(br, getNextToken(br));
		}
		else {
			appendOutputString("Missing '('\n");
			return null;
		}
	}
	
	private static String handleWhileStatement(BufferedReader br) {
		String nextString;
		if((nextString = handleParExpression(br)) != null && nextString.equals(")")) {
			return parseStatementHandler(br, getNextToken(br));
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String handleDoStatement(BufferedReader br) {
		String nextString;
		if((nextString = parseStatementHandler(br, getNextToken(br))) != null && 
				nextString.equals("while")) {
			if((nextString = handleParExpression(br)) != null && nextString.equals(")")) {
				if((nextString = getNextToken(br)) != null && nextString.equals(";")) {
					return getNextToken(br);
				}
				else {
					appendOutputString("Missing \';\'\n");
					return null;
				}
			}
			else {
				appendOutputString("Missing \')\'\n");
				return null;
			}
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String handleForStatement(BufferedReader br) {
		String nextString;
		if((nextString = handleForControl(br)) != null && nextString.equals(")")) {
			return parseStatementHandler(br, getNextToken(br));
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String handleForControl(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("(")) {
			if((nextString = parseExpression(br, getNextToken(br))) != null && 
					nextString.equals(";")) {
				if((nextString = parseExpression(br, getNextToken(br))) != null && 
						nextString.equals(";")) {
					return parseExpression(br, getNextToken(br));
				}
				else {
					appendOutputString("Missing \';\'\n");
					return null;
				}
			}
			else {
				appendOutputString("Missing \';\'\n");
				return null;
			}
		}
		else {
			appendOutputString("Missing \'(\'\n");
			return null;
		}
	}
	
	private static String handleBreakStatement(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals(";")) {
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String handleContinueStatement(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals(";")) {
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String handleReturnStatement(BufferedReader br) {
		String nextString;
		if((nextString = parseExpression(br, getNextToken(br))) != null && 
				nextString.equals(";")) {
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String handleStatementExpression(BufferedReader br, String currString) {
		String nextString = currString;
		if((nextString = parseExpression(br, nextString)) != null && 
				nextString.equals(";")) {
			return getNextToken(br);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String parseExpression(BufferedReader br, String currString) {
		String nextString = currString;
		if((nextString = parseExpression2(br, nextString)) == null) {
			return null;
		}
		
		if(assignmentOpList.contains(nextString)) {
			if((nextString = getNextToken(br)) == null) {
				appendOutputString("Expression not found after assignment operator\n");
				return null;
			}
			return parseExpression2(br, nextString);
		}
		else {
			return nextString;
		}
	}
	
	private static String parseExpression2(BufferedReader br, String currString) {
		String nextString = currString;
		if((nextString = parseExpression3(br, nextString)) == null) {
			return null;
		}
		
		if(infixOpList.contains(nextString)) {
			do {
				if((nextString = getNextToken(br)) == null) {
					return null;
				}
				nextString = parseExpression3(br, nextString);
			} while(nextString != null && infixOpList.contains(nextString));
			return nextString;
		}
		else if(nextString.equals("instanceof")) {
			if((nextString = getNextToken(br)) != null && 
					basicTypeList.contains(nextString)) {
				return getNextToken(br);
			}
			else {
				appendOutputString("Type not found after instanceof\n");
				return null;
			}
		}
		else {
			return nextString;
		}
	}
	
	private static String parseExpression3(BufferedReader br, String currString) {
		String nextString = currString;
		if(prefixOpList.contains(nextString) || basicTypeList.contains(nextString)) {
			if((nextString = getNextToken(br)) == null) {
				return null;
			}
			return parseExpression3(br, nextString);
		}
		
		if(nextString != null && (Character.isAlphabetic(nextString.charAt(0)) ||
				Character.isDigit(nextString.charAt(0)))) {
			VariableInfo.valueSB.append(nextString);
			return handlePostExpression3(br);
		}
		else {
			return nextString;
		}
	}
	
	private static String handlePostExpression3(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) == null) {
			return null;
		}
		
		if(nextString.equals("[")) {
			do {
				nextString = handleCloseBracket2(br);
			} while(nextString != null && nextString.equals("["));
		}
		
		if(nextString.equals(".")) {
			do {
				if((nextString = getNextToken(br)) != null && 
						(Character.isAlphabetic(nextString.charAt(0)))) {
					VariableInfo.valueSB.append(nextString);
					nextString = getNextToken(br);
				}
			} while(nextString != null && nextString.equals("."));
		}
		
		if(postfixOpList.contains(nextString)) {
			nextString = getNextToken(br);
		}
		
		return nextString;
	}
	
	/*private static String getNextToken(BufferedReader br) {
		if(sc.hasNext()) {
			String nextString = sc.next();
			System.out.println(nextString);
			return nextString;
		}
		else {
			return null;
		}
	}*/
	
	private static String getNextToken(BufferedReader br) {
		StringBuilder sb = new StringBuilder();
		
		try {
			char ch;
			int value;
			
			if(currentChar != 0) {
				value = (int)currentChar;
				ch = currentChar;
				currentChar = 0;
			}
			else {
				value = br.read();
			}
			
			if(Character.isWhitespace(value)) {
				try {
		         while((value = br.read()) != -1 && 
		         		Character.isWhitespace(value)) {
		         	;
		         }
	         } catch (IOException e) {
		         e.printStackTrace();
	         }
			}
			ch = (char)value;
			
			if(spCharList.contains(ch)) {
				sb.append(ch);
			}
			else if(spCharList2.contains(ch)) {
				sb.append(ch);
				while((value = br.read()) != -1) {
					ch = (char)value;
					if(!spCharList2.contains(ch)) {
						currentChar = ch;
						break;
					}
					else {
						sb.append(ch);
					}
				}
			}
			else {
				sb.append(ch);
				while((value = br.read()) != -1 &&
						!Character.isWhitespace(value)) {
					ch = (char)value;
					if(spCharList.contains(ch) || spCharList2.contains(ch)) {
						currentChar = ch;
						break;
					}
					else {
						sb.append(ch);
					}
				}
			}
      } catch (IOException e) {
	      e.printStackTrace();
      }
		
		return sb.toString();
	}
	
	private static void appendOutputString(String s) {
		outputSB.append(s);
	}
}
