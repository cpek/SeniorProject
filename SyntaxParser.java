package edu.calpoly.halicej;

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
	
	private static StringBuilder outputSB;
	private static char currentChar = 0;
	
	public static String parseTypeDeclaration(BufferedReader br) {
		outputSB = new StringBuilder();
		outputSB.append("<pre>");
		
		if(parseClassOrInterfaceDeclaration(br) == null) {
			appendRest(br);
		}
		
		outputSB.append("</pre>");
		return outputSB.toString();
	}
	
	private static String parseClassOrInterfaceDeclaration(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(modifierList.contains(nextString)) {
				outputSB.append("<font color=purple>" + nextString + "</font>");
				nextString = getNextToken(br);
			}
			if(nextString.equals("class")) {
				outputSB.append("<font color=purple>" + nextString + "</font>");
				return parseNormalClassDeclaration(br);
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static String parseNormalClassDeclaration(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {			
			outputSB.append(nextString);
			
			if((nextString = getNextToken(br)) != null) {
				if(nextString.equals("{")) {
					outputSB.append(nextString);
					return parseClassBody(br);
				}
				else {
					outputSB.append("<font color=red>" + nextString + "</font>");
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static String parseClassBody(BufferedReader br) {
		String nextString;
		if((nextString = parseClassBodyDeclaration(br)) == null) {
			return null;
		}
		if(nextString.equals("}")) {
			outputSB.append(nextString);
			return nextString;
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
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
					outputSB.append("<font color=\"purple\">" + nextString + "</font>");
					nextString = parseMethodOrFieldDecl(br);
				}
				else if(coiHeaderList.contains(nextString)) {
					outputSB.append("<font color=\"purple\">" + nextString + "</font>");
					nextString = parseNormalClassDeclaration(br);
				}
				else {
					outputSB.append(nextString);
					nextString = parseConstructorDeclaratorRest(br);
				}
			} while(nextString != null && !nextString.equals("}"));
			
			return nextString;
		}
		else {
			return null;
		}
	}
	
	private static String parseModifiers(BufferedReader br, String nextString) {
		try {
			if(modifierList.contains(nextString)) {
				outputSB.append("<font color=\"purple\">" + nextString + "</font>");
				nextString = getNextToken(br);
			}
			if(nextString.equals("static")) {
				outputSB.append("<font color=\"purple\">" + nextString + "</font>");
				nextString = getNextToken(br);
			}
			if(nextString.equals("final")) {
				outputSB.append("<font color=\"purple\">" + nextString + "</font>");
				nextString = getNextToken(br);
			}
			return nextString;
		}
		catch(NoSuchElementException e) {
			outputSB.append("Incomplete member declaration\n");
			return null;
		}
	}
	
	private static String parseMethodOrFieldDecl(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			outputSB.append(nextString);
			return parseMethodOrFieldRest(br);
		}
		else {
			outputSB.append("Incomplete method or field declaration\n");
			return null;
		}
	}
	
	private static String parseMethodOrFieldRest(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(nextString.equals("(")) {
				outputSB.append(nextString);
				return parseMethodDeclaratorRest(br);
			}
			else {
				nextString = parseFieldDeclaratorsRest(br, nextString);
				if(nextString != null && nextString.equals(";")) {
					outputSB.append(nextString);
					return getNextToken(br);
				}
				else {
					outputSB.append("<font color=red>" + nextString + "</font>");
					return null;
				}
			}
		}
		else {
			return null;
		}
	}
	
	private static String parseMethodDeclaratorRest(BufferedReader br) {
		/*outputSB.append("Modifier: " + VariableInfo.getModifier() + "\n");
		outputSB.append("Type: " + VariableInfo.getType() + "\n");
		outputSB.append("Method name: " + VariableInfo.getName() + "\n\n");
		VariableInfo.clear();*/
		
		String nextString = parseFormalParameters(br);
		return parseStatementHandler(br, nextString);
	}
	
	private static String parseStatementHandler(BufferedReader br, String currString) {
		String nextString = currString;
		if(nextString != null && nextString.equals("{")) {
			outputSB.append(nextString);
			if((nextString = parseStatements(br)) != null && nextString.equals("}")) {
				outputSB.append(nextString);
				return getNextToken(br);
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
				return null;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseFormalParameters(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			if(nextString.equals(")") || 
			  ((nextString = parseFormalParameterDecls(br, nextString)) != null && 
			    nextString.equals(")"))) {
				outputSB.append(nextString);
				return getNextToken(br);
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
				return null;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseFormalParameterDecls(BufferedReader br, String currString) {
		String nextString = currString;
		if(basicTypeList.contains(nextString)) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			return parseFormalParameterDeclsRest(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseFormalParameterDeclsRest(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			outputSB.append(nextString);
			
			if((nextString = getNextToken(br)) == null) {
				return null;
			}
			
			if(nextString.equals("[")) {
				do {
					outputSB.append(nextString);
					nextString = handleCloseBracket(br);
				} while(nextString != null && nextString.equals("["));
			}
			
			if(nextString.equals(",")) {
				outputSB.append(nextString);
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
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseFieldDeclaratorsRest(BufferedReader br, String currString) {
		String nextString = parseVariableDeclaratorRest(br, currString);
		while(nextString != null && nextString.equals(",")) {
			outputSB.append(nextString);
			nextString = parseVariableDeclarator(br);
		}
		return nextString;
	}
	
	private static String parseVariableDeclarator(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null) {
			outputSB.append(nextString);
			if((nextString = getNextToken(br)) != null) {
				return parseVariableDeclaratorRest(br, nextString);
			}
			else {
				return null;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseVariableDeclaratorRest(BufferedReader br, String currString) {		
		String nextString = currString;
		if(nextString.equals("[")) {
			do {
				outputSB.append(nextString);
				nextString = handleCloseBracket(br);
			} while(nextString != null && nextString.equals("["));
		}
		
		if(nextString != null && nextString.equals("=")) {
			outputSB.append(nextString);
			nextString = parseVariableInitializer(br);
		}
		
		return nextString;
	}
	
	private static String handleCloseBracket(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("]")) {
			outputSB.append(nextString);
			return getNextToken(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleCloseBracket2(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && 
				Character.isDigit(nextString.charAt(0))) {
			outputSB.append(nextString);
			if((nextString = getNextToken(br)) != null && nextString.equals("]")) {
				outputSB.append(nextString);
				return getNextToken(br);
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
				return null;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseVariableInitializer(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) == null) {
			return null;
		}
		
		if(nextString.equals("{")) {
			outputSB.append(nextString);
			return parseArrayInitializer(br);
		}
		else {
			return parseExpression(br, nextString);
		}
	}
	
	private static String parseArrayInitializer(BufferedReader br) {
		String nextString = parseVariableInitializer(br);
		
		while(nextString.equals(",")) {
			outputSB.append(nextString);
			nextString = parseVariableInitializer(br);
		}
		
		if(nextString.equals("}")) {
			outputSB.append(nextString);
			return getNextToken(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseConstructorDeclaratorRest(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("(")) {
			outputSB.append(nextString);
			nextString = parseFormalParameters(br);
			return parseStatementHandler(br, nextString);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseStatements(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) == null) {
			return null;
		}
		
		if(nextString.equals("}")) {
			return nextString;
		}
		
		do {
			nextString = parseStatement(br, nextString);
		} while(nextString != null && (statementHeaderList.contains(nextString) || 
				basicTypeList.contains(nextString) ||
				prefixOpList.contains(nextString) ||
				Character.isLetter(nextString.charAt(0))));
		
		return nextString;
	}
	
	private static String parseStatement(BufferedReader br, String currString) {
		String nextString = currString;
		if(nextString.equals(";")) {
			outputSB.append(nextString);
			nextString = getNextToken(br);
		}
		else if(nextString.equals("if")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleIfStatement(br);
		}
		else if(nextString.equals("while")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleWhileStatement(br);
		}
		else if(nextString.equals("do")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleDoStatement(br);
		}
		else if(nextString.equals("for")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleForStatement(br);
		}
		else if(nextString.equals("break")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleBreakStatement(br);
		}
		else if(nextString.equals("continue")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleContinueStatement(br);
		}
		else if(nextString.equals("return")) {
			outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			nextString = handleReturnStatement(br);
		}
		else {
			nextString = handleStatementExpression(br, nextString);
		}
		return nextString;
	}
	
	private static String handleIfStatement(BufferedReader br) {
		String nextString;
		if((nextString = handleParExpression(br)) != null && nextString.equals(")")) {
			outputSB.append(nextString);
			if((nextString = parseStatementHandler(br, getNextToken(br))) != null && 
					nextString.equals("else")) {
				outputSB.append(nextString);
				return parseStatementHandler(br, getNextToken(br));
			}
			else {
				return nextString;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleParExpression(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("(")) {
			outputSB.append(nextString);
			return parseExpression(br, getNextToken(br));
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleWhileStatement(BufferedReader br) {
		String nextString;
		if((nextString = handleParExpression(br)) != null && nextString.equals(")")) {
			outputSB.append(nextString);
			return parseStatementHandler(br, getNextToken(br));
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleDoStatement(BufferedReader br) {
		String nextString;
		if((nextString = parseStatementHandler(br, getNextToken(br))) != null && 
				nextString.equals("while")) {
			outputSB.append(nextString);
			if((nextString = handleParExpression(br)) != null && nextString.equals(")")) {
				outputSB.append(nextString);
				if((nextString = getNextToken(br)) != null && nextString.equals(";")) {
					outputSB.append(nextString);
					return getNextToken(br);
				}
				else {
					outputSB.append("<font color=red>" + nextString + "</font>");
					return null;
				}
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
				return null;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleForStatement(BufferedReader br) {
		String nextString;
		if((nextString = handleForControl(br)) != null && nextString.equals(")")) {
			outputSB.append(nextString);
			return parseStatementHandler(br, getNextToken(br));
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleForControl(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals("(")) {
			outputSB.append(nextString);
			if((nextString = parseExpression(br, getNextToken(br))) != null && 
					nextString.equals(";")) {
				outputSB.append(nextString);
				if((nextString = parseExpression(br, getNextToken(br))) != null && 
						nextString.equals(";")) {
					outputSB.append(nextString);
					return parseExpression(br, getNextToken(br));
				}
				else {
					outputSB.append("<font color=red>" + nextString + "</font>");
					return null;
				}
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
				return null;
			}
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleBreakStatement(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals(";")) {
			outputSB.append(nextString);
			return getNextToken(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleContinueStatement(BufferedReader br) {
		String nextString;
		if((nextString = getNextToken(br)) != null && nextString.equals(";")) {
			outputSB.append(nextString);
			return getNextToken(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleReturnStatement(BufferedReader br) {
		String nextString;
		if((nextString = parseExpression(br, getNextToken(br))) != null && 
				nextString.equals(";")) {
			outputSB.append(nextString);
			return getNextToken(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String handleStatementExpression(BufferedReader br, String currString) {
		String nextString = currString;
		if((nextString = parseExpression(br, nextString)) != null && 
				nextString.equals(";")) {
			outputSB.append(nextString);
			return getNextToken(br);
		}
		else {
			outputSB.append("<font color=red>" + nextString + "</font>");
			return null;
		}
	}
	
	private static String parseExpression(BufferedReader br, String currString) {
		String nextString = currString;
		if((nextString = parseExpression2(br, nextString)) == null) {
			return null;
		}
		
		if(assignmentOpList.contains(nextString)) {
			outputSB.append(nextString);
			if((nextString = getNextToken(br)) == null) {
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
				outputSB.append(nextString);
				if((nextString = getNextToken(br)) == null) {
					return null;
				}
				nextString = parseExpression3(br, nextString);
			} while(nextString != null && infixOpList.contains(nextString));
			return nextString;
		}
		else if(nextString.equals("instanceof")) {
			outputSB.append(nextString);
			if((nextString = getNextToken(br)) != null && 
					basicTypeList.contains(nextString)) {
				outputSB.append("<font color=\"purple\">" + nextString + "</font>");
				return getNextToken(br);
			}
			else {
				outputSB.append("<font color=red>" + nextString + "</font>");
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
			if(basicTypeList.contains(nextString)) {
				outputSB.append("<font color=\"purple\">" + nextString + "</font>");
			}
			else {
				outputSB.append(nextString);
			}
			if((nextString = getNextToken(br)) == null) {
				return null;
			}
			return parseExpression3(br, nextString);
		}
		
		if(nextString != null && (Character.isLetter(nextString.charAt(0)) ||
				Character.isDigit(nextString.charAt(0)))) {
			outputSB.append(nextString);
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
				outputSB.append(nextString);
				nextString = handleCloseBracket2(br);
			} while(nextString != null && nextString.equals("["));
		}
		
		if(nextString.equals(".")) {
			do {
				outputSB.append(nextString);
				if((nextString = getNextToken(br)) != null && 
						(Character.isLetter(nextString.charAt(0)))) {
					outputSB.append(nextString);
					nextString = getNextToken(br);
				}
			} while(nextString != null && nextString.equals("."));
		}
		
		if(postfixOpList.contains(nextString)) {
			outputSB.append(nextString);
			nextString = getNextToken(br);
		}
		
		return nextString;
	}
	
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
			
			if(value == -1) {
				return null;
			}
			
			if(Character.isWhitespace(value)) {				
				try {
					do {
		         	ch = (char)value;
		         	if(ch == '\n') {
		         		outputSB.append("<br />");
		         	}
		         	else {
		         		outputSB.append((char)value);
		         	}
		         } while((value = br.read()) != -1 && Character.isWhitespace(value));
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
				currentChar = (char)value;
			}
      } catch (IOException e) {
	      e.printStackTrace();
      }
		
		return sb.toString();
	}
	
	private static void appendRest(BufferedReader br) {
		int value;
		char ch;
		try {
			outputSB.append(' ');
	      while((value = br.read()) != -1) {
	      	ch = (char)value;
	      	if(ch == '\n') {
	      		outputSB.append("<br />");
	      	}
	      	else {
	      		outputSB.append((char)value);
	      	}
	      }
      } catch (IOException e) {
	      e.printStackTrace();
      }
	}
}