    package Main.Token.Lexer.Parser;

    import Main.ExceptionHandlers.*;
    import Main.Nodes.ASTNodes.*;
    import Main.Nodes.EvaluableNodes.*;
    import Main.Nodes.ExpressionNodes.ArithmeticExpressionNode;
    import Main.Nodes.ExpressionNodes.ComparisonExpressionNode;
    import Main.Nodes.ExpressionNodes.LogicalExpressionNode;
    import Main.Nodes.SymbolTable;
    import Main.Token.Token;

    import java.util.*;

    public class Parser {
        private final List<Token> tokens;
        private int currentTokenIndex;
        private final SymbolTable symbolTable = new SymbolTable();
        private int statementCount;
        private int errorCount;

        public Parser(List<Token> tokens) {
            this.tokens = tokens;
            currentTokenIndex = 0;
            errorCount = 0;
            statementCount = 0;
        }

        // call parse method to start parsing
        public ASTNode parse() throws BeginContainerMissingException, EndContainerMissingException, SyntaxErrorException {
            return program();
        }

        private boolean match(Token.Type expectedType) {
            if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getType() == expectedType) {
                if(currentTokenIndex + 1 == tokens.size() && tokens.get(currentTokenIndex).getType() != Token.Type.EndContainer) {
                    System.err.println("ERROR: Missing END CODE container.");
                    System.exit(1);
                }
                currentTokenIndex++;
                return true;
            }
            return false;
        }


        public int getCurrentTokenIndex() {
            return currentTokenIndex;
        }

        public int getStatementCount() {
            return statementCount;
        }



        // Program --> BEGIN CODE VariableDeclarations ExecutableCode END CODE
        private ASTNode program() throws BeginContainerMissingException, EndContainerMissingException, SyntaxErrorException {
            if(match(Token.Type.BeginContainer)) {
                statementCount++;
                ASTNode variableDeclarations = variableDeclarations();
                ASTNode executableCode = executableCode((VariableDeclarationsNode) variableDeclarations);
                if(match(Token.Type.EndContainer)) {
                    statementCount++;
                    if(tokens.size() > currentTokenIndex) {
                        throw new SyntaxErrorException("ERROR: Invalid token found after END CODE container.");
                    }
                    return new ProgramNode(variableDeclarations, executableCode, symbolTable);
                } else {
                    throw new EndContainerMissingException("ERROR: Missing END CODE container.");
                }
            } else {
                throw new BeginContainerMissingException("ERROR: Missing BEGIN CODE container");
            }
        }



        private ASTNode scanFunction(VariableDeclarationsNode declarations) {
            try {
                if (match(Token.Type.Scan)) {
                    if (match(Token.Type.Colon)) {
                        List<String> variableNames = new ArrayList<>();
                        if(match(Token.Type.Identifier)) {
                            variableNames.add(tokens.get(currentTokenIndex-1).getText());
                            while(match(Token.Type.Comma)) {
                                variableNames.add(tokens.get(currentTokenIndex).getText());
                            }
                        }
                        List<SingleVariableDeclaration> declarationStatements = declarations.getVariableDeclarations();
                        if(declarationStatements.isEmpty() && !variableNames.isEmpty()) {
                            throw new VariableDeclarationException("ERROR: Variables '" + variableNames + "' not declared.");
                        } else {
                            for(String varName : variableNames) {
                                if(declarations.getVariableDeclarations().stream()
                                        .flatMap(declaration -> declaration.getVariableNames().stream())
                                        .noneMatch(variableNode -> variableNode.getVariableName().equals(varName))) {
                                    throw new VariableDeclarationException("ERROR: Variable '" + varName + "' not declared.");
                                }
                            }
                        }

                        statementCount++;
                        return new ScannerNode(variableNames, declarations);
                    } else {
                        throw new SyntaxErrorException("ERROR: Expected colon (:) after SCAN keyword.");
                    }
                }
            } catch (SyntaxErrorException | VariableDeclarationException i ) {
                errorCount++;
                System.err.println(i.getMessage());
                System.exit(1);
            }
            return null;
        }

        public static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
                "BEGIN", "CODE", "END", "INT", "CHAR", "BOOL", "FLOAT", "DISPLAY",
                "THEN", "ELSE", "IF", "WHILE", "AND", "OR", "NOT", "TRUE", "FALSE", "SCAN"
        ));


        private boolean isReservedWord(String token) {
            return RESERVED_WORDS.contains(token);
        }

        // Method to check if a variable name is a reserved word
        private boolean isReservedVariable(String variableName) {
            return isReservedWord(variableName);
        }


        public int getErrorCount() {
            return errorCount;
        }




        // VariableDeclarations -> VariableDeclaration VariableDeclarations | ε
        private ASTNode variableDeclarations() {
            List<SingleVariableDeclaration> variables = new ArrayList<>();

            while (true) {
                SingleVariableDeclaration variable = (SingleVariableDeclaration) variableDeclaration();
                if (variable != null) {
                    statementCount++;
                    variables.add(variable);
                } else {
                    break;
                }
            }
            return new VariableDeclarationsNode(variables);
        }




        // VariableDeclaration -> DataType VariableList
        private ASTNode variableDeclaration() {
            String dataType = tokens.get(currentTokenIndex).getText();
            ASTNode variableDeclaration = null;
            try {
                if (!dataType.isEmpty() && DataType()) {
                    List<VariableNode> variableNodes = variableList();
                    if(variableNodes.isEmpty()) {
                        throw new VariableDeclarationException("ERROR: Found Data Type token but variable list is empty.");
                    }
                    variableDeclaration = new SingleVariableDeclaration(dataType, variableNodes);
                }
            } catch (VariableDeclarationException v) {
                errorCount++;
                System.err.println(v.getMessage());
                System.exit(1);
            }
            return variableDeclaration;
        }


        // VariableList -> VariableName VariableList'
        private List<VariableNode> variableList() throws VariableDeclarationException {
            List<VariableNode> variableNodes = new ArrayList<>();
            List<String> dominoInitializedVariables = new ArrayList<>();
            String variableName;

            while((variableName = variableName()) != null) {
                dominoInitializedVariables.add(variableName);
                Object initialValue = assignment();
                if(initialValue != null) {
                    while (initialValue.equals("continue")) {
                        variableName = tokens.get(currentTokenIndex - 1).getText();
                        dominoInitializedVariables.add(variableName);
                        initialValue = assignment();
                    }

                    for (String domino : dominoInitializedVariables) {
                        variableNodes.add(new VariableNode(domino, initialValue));
                    }

                    dominoInitializedVariables.clear();
                } else {
                    variableNodes.add(new VariableNode(variableName, null));
                }

                if (!match(Token.Type.Comma)) {
                    break;
                }

            }
            return variableNodes;
        }

        // DataType -> INT | CHAR | BOOL | FLOAT
        private boolean DataType() {
            return match(Token.Type.Int) || match(Token.Type.Char) || match(Token.Type.Bool) || match(Token.Type.Float);
        }

        // VariableName -> Identifier
        private String variableName() throws VariableDeclarationException {
            String variableName;
            if (match(Token.Type.Identifier)) {
                variableName = tokens.get(currentTokenIndex - 1).getText();
            } else if(isReservedVariable(tokens.get(currentTokenIndex).getText())) {
                throw new VariableDeclarationException("ERROR: Variable name '" + tokens.get(currentTokenIndex).getText() + "' is a reserved word.");
            } else {
                throw new VariableDeclarationException("ERROR: Invalid variable name format. It should start with a letter or an underscore only.");
            }
            return variableName;
        }


        // Assignment -> '=' Expression
        private Object assignment() {
            if (match(Token.Type.Assign)) {
                try {
                    Object assignedValue = value();

                    if(assignedValue != null) {
                        return assignedValue;
                    }

                    throw new VariableInitializationException("ERROR: Assignment operator found but " +
                            "value token is missing or is an invalid value type. Please check again.");
                } catch (VariableInitializationException | SyntaxErrorException v) {
                    System.err.println(v.getMessage());
                    System.exit(1);
                }
            }
            return null;
        }




        // Expression -> Num | NumFloat | CharLiteral | BooleanLiteral\
        private Object value() throws VariableInitializationException, SyntaxErrorException {
            if(isValidExpression()) {
                currentTokenIndex--;
                return expressionHandler();
            } else if(match(Token.Type.Negation)) {
                if (isValidExpression() ) {
                    currentTokenIndex-=2;
                    return expressionHandler();
                } else if(match(Token.Type.Identifier)) {
                    return new VariableNode(tokens.get(currentTokenIndex-1).getText(), -1);
                } else if (match(Token.Type.Num)) {
                    return -(Integer.parseInt(tokens.get(currentTokenIndex-1).getText()));
                } else if (match(Token.Type.NumFloat)) {
                    return -(Float.parseFloat(tokens.get(currentTokenIndex-1).getText()));
                }
            } else if (match(Token.Type.Num)) {
                return Integer.parseInt(tokens.get(currentTokenIndex - 1).getText());
            } else if (match(Token.Type.NumFloat)) {
                return Float.parseFloat(tokens.get(currentTokenIndex - 1).getText());
            } else if (match(Token.Type.CharLiteral)) {
                return tokens.get(currentTokenIndex - 1).getText().charAt(0);
            } else if (match(Token.Type.BooleanLiteral)) {
                return tokens.get(currentTokenIndex - 1).getText();
            } else if(match(Token.Type.Identifier)) {
                String variableName = tokens.get(currentTokenIndex-1).getText();
                if(match(Token.Type.Assign)) {
                    currentTokenIndex--;
                    return "continue";
                } else {
                    return new VariableNode(variableName, null);
                }
            }
            return null;
        }

        private boolean isValidExpression() {
            return ((tokens.get(currentTokenIndex + 1).getType() == Token.Type.Plus ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Minus ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Times ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Divide ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Modulo ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Less ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.LessEqual ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Greater ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.GreaterEqual ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Equals ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.NotEqual ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.And ||
                     tokens.get(currentTokenIndex + 1).getType() == Token.Type.Or) &&
                     (match(Token.Type.Num) || match(Token.Type.NumFloat) || match(Token.Type.Identifier))) ||
                     (match(Token.Type.Parentheses) && tokens.get(currentTokenIndex - 1).getText().equals("(")) ||
                    match(Token.Type.Not);
        }


    private ASTNode executableCode(VariableDeclarationsNode variableDeclarationsNode) {
        try {
            List<ASTNode> statements = new ArrayList<>();
            while(true) {
                if (match(Token.Type.Print)) {
                    DisplayNode displayNode = (DisplayNode) displayHandler(variableDeclarationsNode);
                    statements.add(displayNode);
                } else if (match(Token.Type.Identifier)) {
                    if (match(Token.Type.Assign)) {
                        currentTokenIndex -= 2;
                        statements.add(reinitializeVariable(variableDeclarationsNode));
                    }
                } else if (match(Token.Type.Scan)) {
                    currentTokenIndex--;
                    statements.add(scanFunction(variableDeclarationsNode));
                } else if (match(Token.Type.If)) {
                    currentTokenIndex--;
                    statements.add(conditionalStatements(variableDeclarationsNode));
                } else {
                    break;
                }
            }

            if(DataType()) {
                currentTokenIndex--;
                throw new SyntaxErrorException("ERROR: Variable declaration found after executable code.");
            }

            return new ExecutableCodeNode(statements);
        } catch (SyntaxErrorException | DisplayException | VariableInitializationException v) {
            errorCount++;
            System.err.println(v.getMessage());
            System.exit(1);
        }
        return null;
    }




    private ASTNode reinitializeVariable(VariableDeclarationsNode declarationStatements) {
        try {
            List<String> dominoInitializedVariables = new ArrayList<>();
            Object initialValue = null;
            String variableName;
            if((variableName = variableName()) != null) {
                dominoInitializedVariables.add(variableName);
                initialValue = assignment();
                if(initialValue != null) {
                    while (initialValue.equals("continue")) {
                        variableName = tokens.get(currentTokenIndex - 1).getText();
                        dominoInitializedVariables.add(variableName);
                        initialValue = assignment();
                    }
                } else {
                    throw new SyntaxErrorException("ERROR: Invalid variable reinitialization format. Expected assignment operator.");
                }
            }
            for(String domino : dominoInitializedVariables) {
                boolean variableDeclared = declarationStatements.getVariableDeclarations().stream()
                        .flatMap(declaration -> declaration.getVariableNames().stream())
                        .anyMatch(variableNode -> variableNode.getVariableName().equals(domino));

                if (!variableDeclared) {
                    throw new VariableInitializationException("ERROR: Variable '" + domino + "' not declared.");
                }

            }
            statementCount++;
            return new VariableReinitializedNode(dominoInitializedVariables, new LiteralNode(initialValue));
        } catch (Exception v) {
            errorCount++;
            System.err.println(v.getMessage());
            System.exit(1);
        }
        return null;
    }


    private ASTNode expressionHandler() throws SyntaxErrorException, VariableInitializationException {
        ASTNode exprNode = expr();
        if (exprNode != null) {
            return exprNode;
        } else {
            throw new SyntaxErrorException("ERROR: Invalid parsing of expression. Null value found.");
        }
    }
    

    private ASTNode displayHandler(VariableDeclarationsNode variableDeclarationsNode) throws DisplayException, SyntaxErrorException, VariableInitializationException {
        List<ASTNode> expressions = new ArrayList<>();
        if(match(Token.Type.Colon)) {
            while (true) {
                if(isValidExpression()) {
                    currentTokenIndex--;
                    expressions.add(expressionHandler());
                } else if(match(Token.Type.Negation)) {
                    if (isValidExpression() ) {
                        currentTokenIndex-=2;
                        expressions.add(expressionHandler());
                    } else if(match(Token.Type.Identifier)) {
                        expressions.add(new VariableNode(tokens.get(currentTokenIndex-1).getText(), -1));
                    } else if (match(Token.Type.Num)) {
                        expressions.add(new LiteralNode(-(Integer.parseInt(tokens.get(currentTokenIndex-1).getText()))));
                    } else if (match(Token.Type.NumFloat)) {
                        expressions.add(new LiteralNode(-(Float.parseFloat(tokens.get(currentTokenIndex-1).getText()))));
                    }
                } else if(match(Token.Type.Identifier)) {
                    expressions.add(new VariableNode(tokens.get(currentTokenIndex-1).getText(), null));
                } else if(match(Token.Type.Num) || match(Token.Type.NumFloat) ||
                    match(Token.Type.CharLiteral) || match(Token.Type.BooleanLiteral) ||
                    match(Token.Type.StringLiteral) || match(Token.Type.NewLine) ||
                    match(Token.Type.Escape) ){

                    LiteralNode literalNode = new LiteralNode(tokens.get(currentTokenIndex - 1).getText());
                    if(literalNode.getValue().equals("$")) {
                        literalNode = new LiteralNode("\n");
                    }
                    expressions.add(literalNode);

                } else {
                    throw new DisplayException("ERROR: Invalid display statement format near '" + tokens.get(currentTokenIndex).getText() + "' token");
                }

                if (match(Token.Type.Concat)) {
                    continue;
                } else {
                    if(tokens.get(currentTokenIndex).getType() == Token.Type.NewLine ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.Escape ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.Identifier ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.StringLiteral ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.CharLiteral ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.Num ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.NumFloat ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.BooleanLiteral) {
                            throw new DisplayException("ERROR: Expression found but concatenation token is missing.");
                    }
                    break;
                }
            }
        } else {
            throw new DisplayException("ERROR: Colon (:) token missing after DISPLAY keyword.");
        }
        statementCount++;
        return new DisplayNode(expressions);
    }


    private ASTNode expr() throws SyntaxErrorException, VariableInitializationException {
        ASTNode logicalAnd = logicalAnd();
        if (logicalAnd != null) {
            while (match(Token.Type.Or)) {
                Token.Type operator = tokens.get(currentTokenIndex - 1).getType();
                ASTNode rightOperand = logicalAnd();
                if (rightOperand != null) {
                    logicalAnd = new LogicalExpressionNode(logicalAnd, operator, rightOperand);
                } else {
                    throw new SyntaxErrorException("ERROR: " + tokens.get(currentTokenIndex).getType() + " " + tokens.get(currentTokenIndex).getText() + " is not a valid operand.");
                }
            }
        }
        return logicalAnd;
    }



    private ASTNode logicalAnd() throws SyntaxErrorException, VariableInitializationException {
        ASTNode comp = comp();
        if (comp != null) {
            while (match(Token.Type.And)) {
                Token.Type operator = tokens.get(currentTokenIndex - 1).getType();
                ASTNode rightOperand = comp();
                if (rightOperand != null) {
                    comp = new LogicalExpressionNode(comp, operator, rightOperand);
                } else {
                    throw new SyntaxErrorException("ERROR: " + tokens.get(currentTokenIndex).getType() + " " + tokens.get(currentTokenIndex).getText() + " is not a valid operand.");
                }
            }
        }
        return comp;
    }



    private ASTNode comp() throws SyntaxErrorException, VariableInitializationException {
        ASTNode arithmeticExpr = arithmeticExpr();
        if (arithmeticExpr != null) {
            while (match(Token.Type.Less) || match(Token.Type.LessEqual) || match(Token.Type.Greater) ||
                    match(Token.Type.GreaterEqual) || match(Token.Type.Equals) || match(Token.Type.NotEqual)) {
                Token.Type operator = tokens.get(currentTokenIndex - 1).getType();
                ASTNode rightOperand = arithmeticExpr();
                if (rightOperand != null) {
                    arithmeticExpr = new ComparisonExpressionNode(arithmeticExpr, operator, rightOperand);
                } else {
                    throw new SyntaxErrorException("ERROR: " + tokens.get(currentTokenIndex).getType() + " " + tokens.get(currentTokenIndex).getText() + " is not a valid operand.");
                }
            }
        }
        return arithmeticExpr;
    }


    private ASTNode arithmeticExpr() throws SyntaxErrorException, VariableInitializationException {
        ASTNode term = term();

        if (term != null) {
            while (match(Token.Type.Plus) || match(Token.Type.Minus)) {
                Token.Type operator = tokens.get(currentTokenIndex - 1).getType();
                ASTNode rightOperand = term();
                if (rightOperand != null) {
                    term = new ArithmeticExpressionNode(term, operator, rightOperand);
                } else {
                    throw new SyntaxErrorException("ERROR: " + tokens.get(currentTokenIndex).getType() + " " + tokens.get(currentTokenIndex).getText() + " is not a valid operand.");
                }
            }
        }
        return term;
    }

    private ASTNode term() throws SyntaxErrorException, VariableInitializationException {
        ASTNode factor = factor();
        if(factor != null) {
            while (match(Token.Type.Times) || match(Token.Type.Divide) || match(Token.Type.Modulo)) {
                Token.Type operator = tokens.get(currentTokenIndex - 1).getType();
                ASTNode rightOperand = factor();
                if (rightOperand != null) {
                    factor = new ArithmeticExpressionNode(factor, operator, rightOperand);
                } else {
                    throw new SyntaxErrorException("ERROR: " + tokens.get(currentTokenIndex).getType() + " " + tokens.get(currentTokenIndex).getText() + " is not a valid operand.");
                }
            }
        }

        return factor;
    }


    private ASTNode factor() throws SyntaxErrorException, VariableInitializationException {
        if (match(Token.Type.Negation)) {
            if(match(Token.Type.Identifier)) {
                return new VariableNode(tokens.get(currentTokenIndex-1).getText(), -1);
            } if (match(Token.Type.Num)) {
                return new LiteralNode(-(Integer.parseInt(tokens.get(currentTokenIndex - 1).getText())));
            } else if (match(Token.Type.NumFloat)) {
                return new LiteralNode(-(Float.parseFloat(tokens.get(currentTokenIndex - 1).getText())));
            }
            throw new SyntaxErrorException("ERROR: Negation token found but number token is missing.");
        } else if (match(Token.Type.Not)) {
            Token.Type type = tokens.get(currentTokenIndex - 1).getType();
            ASTNode operand = factor();
            if (operand != null) {
                return new LogicalExpressionNode(operand, type, null);
            } else {
                throw new SyntaxErrorException("ERROR: NOT operator found but operand to be complemented is missing.");
            }
        } else if (match(Token.Type.Num)) {
            return new LiteralNode(Integer.parseInt(tokens.get(currentTokenIndex - 1).getText()));
        } else if (match(Token.Type.Identifier)) {
            return new VariableNode(tokens.get(currentTokenIndex-1).getText(), null);
        } else if (match(Token.Type.NumFloat)) {
            return new LiteralNode(Float.parseFloat(tokens.get(currentTokenIndex - 1).getText()));
        } else if(match(Token.Type.BooleanLiteral)) {
            return new LiteralNode(tokens.get(currentTokenIndex - 1).getText());
        } else if (match(Token.Type.Parentheses) && tokens.get(currentTokenIndex - 1).getText().equals("(")) {
            ASTNode expression = expressionHandler();
            if (tokens.get(currentTokenIndex).getText().equals(")") && match(Token.Type.Parentheses)) {
                return expression;
            }
            throw new SyntaxErrorException("ERROR: Invalid expression format. Missing closing parenthesis.");
        }
        return null;
    }


        private ASTNode conditionalStatements(VariableDeclarationsNode variableDeclarationsNode) throws SyntaxErrorException, VariableInitializationException, DisplayException {
            List<ASTNode> ifStatements = new ArrayList<>();
            List<ExecutableCodeNode> elseIfBlocks = new ArrayList<>();
            List<ASTNode> elseStatements = new ArrayList<>();
            List<ASTNode> conditions = new ArrayList<>();
            if(match(Token.Type.If)) {
                conditions.add(parseCondition());
                beginIfBlock(ifStatements, variableDeclarationsNode);
            }
            while(match(Token.Type.IfElse)) {
                List<ASTNode> elseIfStatements = new ArrayList<>();
                conditions.add(parseCondition());
                beginIfBlock(elseIfStatements, variableDeclarationsNode);
                elseIfBlocks.add(new ExecutableCodeNode(elseIfStatements));
            }
            if(match(Token.Type.Else)) {
                beginIfBlock(elseStatements, variableDeclarationsNode);
            }
            return new ConditionalNode(conditions,new ExecutableCodeNode(ifStatements), elseIfBlocks, new ExecutableCodeNode(elseStatements));
        }

        private ASTNode parseCondition() throws SyntaxErrorException, VariableInitializationException {
            ASTNode condition = null;
            if(match(Token.Type.Parentheses) && tokens.get(currentTokenIndex-1).getText().equals("(")) {
                condition = expressionHandler();
                if (!match(Token.Type.Parentheses) || !tokens.get(currentTokenIndex - 1).getText().equals(")")) {
                    throw new SyntaxErrorException("ERROR: Missing closing parenthesis for IF condition.");
                }
            } else {
                throw new SyntaxErrorException("ERROR: Missing opening parenthesis for IF condition.");
            }
            return condition;
        }

        private void beginIfBlock(List<ASTNode> ifStatements, VariableDeclarationsNode variableDeclarationsNode) throws SyntaxErrorException, VariableInitializationException, DisplayException {
            if (match(Token.Type.BeginIf)) {
                while (!match(Token.Type.EndIf)) {
                    if (match(Token.Type.Print)) {
                        DisplayNode displayNode = (DisplayNode) displayHandler(variableDeclarationsNode);
                        ifStatements.add(displayNode);
                    } else if (match(Token.Type.Identifier)) {
                        if (match(Token.Type.Assign)) {
                            currentTokenIndex -= 2;
                            ifStatements.add(reinitializeVariable(variableDeclarationsNode));
                        }
                    } else if (match(Token.Type.Scan)) {
                        currentTokenIndex--;
                        ifStatements.add(scanFunction(variableDeclarationsNode));
                    } else if(match(Token.Type.If)) {
                        currentTokenIndex--;
                        ifStatements.add(conditionalStatements(variableDeclarationsNode));
                    } else {
                        throw new SyntaxErrorException("ERROR: Invalid statement inside IF block.");
                    }
                }
            } else {
                throw new SyntaxErrorException("ERROR: Missing BEGIN IF after IF condition.");
            }
        }
}
