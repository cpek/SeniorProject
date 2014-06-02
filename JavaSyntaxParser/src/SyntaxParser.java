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
	
	public static String parseTypeDeclaration(Scanner sc) {
		outputSB = new StringBuilder();
		parseClassOrInterfaceDeclaration(sc);
		return outputSB.toString();
	}
	
	private static String parseClassOrInterfaceDeclaration(Scanner sc) {
		if(sc.hasNext()) {
			String nextString = sc.next();
			if(modifierList.contains(nextString)) {
				VariableInfo.setModifier(nextString);
				nextString = sc.next();
			}
			if(nextString.equals("class")) {
				return parseNormalClassDeclaration(sc);
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
	
	private static String parseNormalClassDeclaration(Scanner sc) {
		if(sc.hasNext()) {
			VariableInfo.setName(sc.next());
			
			appendOutputString("Modifier: " + VariableInfo.getModifier() + "\n");
			appendOutputString("Class Name: " + VariableInfo.getName() + "\n\n");
			VariableInfo.clear();
			
			
			if(sc.hasNext()) {
				String nextString = sc.next();
				if(nextString.equals("{")) {
					return parseClassBody(sc);
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
	
	private static String parseClassBody(Scanner sc) {
		String nextString = parseClassBodyDeclaration(sc);
		if(nextString != null && nextString.equals("}")) {
			return getNextString(sc);
		}
		else {
			appendOutputString("Missing \'}\'\n");
			return null;
		}
	}
	
	private static String parseClassBodyDeclaration(Scanner sc) {
		return parseMemberDecl(sc);
	}
	
	private static String parseMemberDecl(Scanner sc) {
		if(sc.hasNext()) {
			String nextString = sc.next();
			if(nextString.equals("}")) {
				return nextString;
			}
			
			do {
				nextString = parseModifiers(sc, nextString);
				if(basicTypeList.contains(nextString)) {
					VariableInfo.setType(nextString);
					nextString = parseMethodOrFieldDecl(sc);
				}
				else if(coiHeaderList.contains(nextString)) {
					nextString = parseNormalClassDeclaration(sc);
				}
				else {
					VariableInfo.setName(nextString);
					nextString = parseConstructorDeclaratorRest(sc);
				}
			} while(nextString != null && !nextString.equals("}"));
			
			return nextString;
		}
		else {
			appendOutputString("Missing \'}\'\n");
			return null;
		}
	}
	
	private static String parseModifiers(Scanner sc, String nextString) {
		try {
			if(modifierList.contains(nextString)) {
				VariableInfo.setModifier(nextString);
				nextString = sc.next();
			}
			else {
				VariableInfo.setModifier("private");
			}
			if(nextString.equals("static")) {
				nextString = sc.next();
			}
			if(nextString.equals("final")) {
				nextString = sc.next();
			}
			return nextString;
		}
		catch(NoSuchElementException e) {
			appendOutputString("Incomplete member declaration\n");
			return null;
		}
	}
	
	private static String parseMethodOrFieldDecl(Scanner sc) {
		if(sc.hasNext()) {
			VariableInfo.setName(sc.next());
			return parseMethodOrFieldRest(sc);
		}
		else {
			appendOutputString("Incomplete method or field declaration\n");
			return null;
		}
	}
	
	private static String parseMethodOrFieldRest(Scanner sc) {
		if(sc.hasNext()) {
			String nextString = sc.next();
			if(nextString.equals("(")) {
				return parseMethodDeclaratorRest(sc);
			}
			else {
				nextString = parseFieldDeclaratorsRest(sc, nextString);
				if(nextString != null && nextString.equals(";")) {
					return getNextString(sc);
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
	
	private static String parseMethodDeclaratorRest(Scanner sc) {
		appendOutputString("Modifier: " + VariableInfo.getModifier() + "\n");
		appendOutputString("Type: " + VariableInfo.getType() + "\n");
		appendOutputString("Method name: " + VariableInfo.getName() + "\n\n");
		VariableInfo.clear();
		
		String nextString = parseFormalParameters(sc);
		return parseStatementHandler(sc, nextString);
	}
	
	private static String parseStatementHandler(Scanner sc, String currString) {
		String nextString = currString;
		if(nextString != null && nextString.equals("{")) {
			if((nextString = parseStatements(sc)) != null && nextString.equals("}")) {
				return getNextString(sc);
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
	
	private static String getNextString(Scanner sc) {
		if(sc.hasNext()) {
			return sc.next();
		}
		else {
			return null;
		}
	}
	
	private static String parseFormalParameters(Scanner sc) {
		String nextString;
		if(sc.hasNext()) {
			nextString = sc.next();
			if(nextString.equals(")") || 
			  ((nextString = parseFormalParameterDecls(sc, nextString)) != null && 
			    nextString.equals(")"))) {
				return getNextString(sc);
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
	
	private static String parseFormalParameterDecls(Scanner sc, String currString) {
		String nextString = currString;
		if(basicTypeList.contains(nextString)) {
			VariableInfo.setType(nextString);
			return parseFormalParameterDeclsRest(sc);
		}
		else {
			appendOutputString("Parameter type not found\n");
			return null;
		}
	}
	
	private static String parseFormalParameterDeclsRest(Scanner sc) {
		if(sc.hasNext()) {
			VariableInfo.setName(sc.next());
			
			String nextString;
			if((nextString = getNextString(sc)) == null) {
				return null;
			}
			
			if(nextString.equals("[")) {
				do {
					nextString = parseCloseBracket(sc);
				} while(nextString != null && nextString.equals("["));
				VariableInfo.setType(VariableInfo.getType() + " array");
			}
			
			appendOutputString("Type: " + VariableInfo.getType() + "\n");
			appendOutputString("Parameter variable: " + VariableInfo.getName() + "\n\n");
			VariableInfo.clear();
			
			if(nextString.equals(",")) {
				if((nextString = getNextString(sc)) == null) {
					return null;
				}
				else {
					return parseFormalParameterDecls(sc, nextString);
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
	
	private static String parseFieldDeclaratorsRest(Scanner sc, String currString) {
		String nextString = parseVariableDeclaratorRest(sc, currString);
		while(nextString != null && nextString.equals(",")) {
			nextString = parseVariableDeclarator(sc);
		}
		return nextString;
	}
	
	private static String parseVariableDeclarator(Scanner sc) {
		if(sc.hasNext()) {
			VariableInfo.setName(sc.next());
			if(sc.hasNext()) {
				return parseVariableDeclaratorRest(sc, sc.next());
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
	
	private static String parseVariableDeclaratorRest(Scanner sc, String currString) {
		boolean isArray = false;
		
		String nextString = currString;
		if(nextString.equals("[")) {
			do {
				nextString = parseCloseBracket(sc);
			} while(nextString != null && nextString.equals("["));
			
			isArray = true;
		}
		
		if(nextString != null && nextString.equals("=")) {
			nextString = parseVariableInitializer(sc);
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
	
	private static String parseCloseBracket(Scanner sc) {
		if(sc.hasNext() && sc.next().equals("]")) {
			if(sc.hasNext()) {
				return sc.next();
			}
			else {
				return null;
			}
		}
		else {
			appendOutputString("Missing \']\'\n");
			return null;
		}
	}
	
	private static String parseVariableInitializer(Scanner sc) {
		String nextString;
		if((nextString = getNextString(sc)) == null) {
			appendOutputString("Literal not found");
			return null;
		}
		
		if(nextString.equals("{")) {
			return parseArrayInitializer(sc);
		}
		else {
			return parseExpression(sc, nextString);
		}
	}
	
	private static String parseArrayInitializer(Scanner sc) {
		VariableInfo.valueSB.append("{");
		String nextString = parseVariableInitializer(sc);
		
		while(nextString.equals(",")) {
			VariableInfo.valueSB.append(", ");
			nextString = parseVariableInitializer(sc);
		}
		
		if(nextString.equals("}")) {
			VariableInfo.valueSB.append("}");
			return getNextString(sc);
		}
		else {
			appendOutputString("Missing \'}\'\n");
			return null;
		}
	}
	
	private static String parseConstructorDeclaratorRest(Scanner sc) {
		appendOutputString("Constructor name: " + VariableInfo.getName() + "\n\n");
		VariableInfo.clear();
		
		String nextString;
		if((nextString = getNextString(sc)) != null && nextString.equals("(")) {
			nextString = parseFormalParameters(sc);
			return parseStatementHandler(sc, nextString);
		}
		else {
			appendOutputString("Missing \'(\'\n");
			return null;
		}
	}
	
	private static String parseStatements(Scanner sc) {
		String nextString;
		if((nextString = getNextString(sc)) == null) {
			appendOutputString("Block statement not found");
			return null;
		}
		
		do {
			nextString = parseStatement(sc, nextString);
		} while(statementHeaderList.contains(nextString));
		
		return nextString;
	}
	
	private static String parseStatement(Scanner sc, String currString) {
		String nextString = currString;
		switch(nextString) {
			case ";":
				nextString = getNextString(sc);
				break;
			case "if":
				nextString = handleIfStatement(sc);
				break;
			case "while":
				nextString = handleWhileStatement(sc);
				break;
			case "do":
				nextString = handleDoStatement(sc);
				break;
			case "for":
				nextString = handleForStatement(sc);
				break;
			case "break":
				nextString = handleBreakStatement(sc);
				break;
			case "continue":
				nextString = handleContinueStatement(sc);
				break;
			case "return":
				nextString = handleReturnStatement(sc);
				break;
			default:
				appendOutputString("Invalid statement\n" + nextString + "\n");
				return null;
		}
		return nextString;
	}
	
	private static String handleParExpression(Scanner sc) {
		String nextString;
		if((nextString = getNextString(sc)) != null && nextString.equals("(")) {
			return parseExpression(sc, getNextString(sc));
		}
		else {
			appendOutputString("Missing '('\n");
			return null;
		}
	}
	
	private static String handleIfStatement(Scanner sc) {
		String nextString;
		if((nextString = handleParExpression(sc)) != null && nextString.equals(")")) {
			if((nextString = parseStatementHandler(sc, getNextString(sc))) != null && 
					nextString.equals("else")) {
				return parseStatementHandler(sc, getNextString(sc));
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
	
	private static String handleWhileStatement(Scanner sc) {
		String nextString;
		if((nextString = handleParExpression(sc)) != null && nextString.equals(")")) {
			return parseStatementHandler(sc, getNextString(sc));
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String handleDoStatement(Scanner sc) {
		String nextString;
		if((nextString = parseStatementHandler(sc, getNextString(sc))) != null && 
				nextString.equals("while")) {
			if((nextString = handleParExpression(sc)) != null && nextString.equals(")")) {
				if((nextString = getNextString(sc)) != null && nextString.equals(";")) {
					return getNextString(sc);
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
	
	private static String handleForStatement(Scanner sc) {
		String nextString;
		if((nextString = handleForControl(sc)) != null && nextString.equals(")")) {
			return parseStatementHandler(sc, getNextString(sc));
		}
		else {
			appendOutputString("Missing \')\'\n");
			return null;
		}
	}
	
	private static String handleForControl(Scanner sc) {
		String nextString;
		if((nextString = getNextString(sc)) != null && nextString.equals("(")) {
			if((nextString = parseExpression(sc, getNextString(sc))) != null && 
					nextString.equals(";")) {
				if((nextString = parseExpression(sc, getNextString(sc))) != null && 
						nextString.equals(";")) {
					return parseExpression(sc, getNextString(sc));
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
	
	private static String handleBreakStatement(Scanner sc) {
		String nextString;
		if((nextString = getNextString(sc)) != null && nextString.equals(";")) {
			return getNextString(sc);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String handleContinueStatement(Scanner sc) {
		String nextString;
		if((nextString = getNextString(sc)) != null && nextString.equals(";")) {
			return getNextString(sc);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String handleReturnStatement(Scanner sc) {
		String nextString;
		if((nextString = parseExpression(sc, getNextString(sc))) != null && 
				nextString.equals(";")) {
			return getNextString(sc);
		}
		else {
			appendOutputString("Missing \';\'\n");
			return null;
		}
	}
	
	private static String parseExpression(Scanner sc, String currString) {
		String nextString = currString;
		if((nextString = parseExpression2(sc, nextString)) == null) {
			return null;
		}
		
		if(assignmentOpList.contains(nextString)) {
			if((nextString = getNextString(sc)) == null) {
				appendOutputString("Expression not found after assignment operator\n");
				return null;
			}
			return parseExpression2(sc, nextString);
		}
		else {
			return nextString;
		}
	}
	
	private static String parseExpression2(Scanner sc, String currString) {
		String nextString = currString;
		if((nextString = parseExpression3(sc, nextString)) == null) {
			return null;
		}
		
		if(infixOpList.contains(nextString)) {
			do {
				nextString = parseExpression3(sc, nextString);
			} while(nextString != null && infixOpList.contains(nextString));
			return nextString;
		}
		else if(nextString.equals("instanceof")) {
			if((nextString = getNextString(sc)) != null && 
					basicTypeList.contains(nextString)) {
				return getNextString(sc);
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
	
	private static String parseExpression3(Scanner sc, String currString) {
		String nextString = currString;
		if(prefixOpList.contains(nextString) || basicTypeList.contains(nextString)) {
			if((nextString = getNextString(sc)) == null) {
				return null;
			}
			return parseExpression3(sc, nextString);
		}
		
		VariableInfo.valueSB.append(nextString);
		if((nextString = getNextString(sc)) == null) {
			return null;
		}
		
		if(postfixOpList.contains(nextString)) {
			return getNextString(sc);
		}
		else {
			return nextString;
		}
	}
	
	private static void appendOutputString(String s) {
		outputSB.append(s);
	}
}
