    package Main.Token.Lexer.Parser;

    import Main.ExceptionHandlers.*;
    import Main.Nodes.*;
    import Main.Token.Token;

    import java.util.*;

    public class Parser {
        private final List<Token> tokens;
        private int currentTokenIndex;
        private final VariableInitializerNode variableInitializer = new VariableInitializerNode();
        private final Set<String> declaredVariables = new HashSet<>();
        private final List<ASTNode> statements = new ArrayList<>();

        private int errorCount;

        public Parser(List<Token> tokens) {
            this.tokens = tokens;
            currentTokenIndex = 0;
            errorCount = 0;
        }



        // call parse method to start parsing
        public ASTNode parse() throws BeginContainerMissingException, EndContainerMissingException, VariableInitializationException, DisplayException {
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



        // Program --> BEGIN CODE VariableDeclarations ExecutableCode END CODE
        private ASTNode program() throws BeginContainerMissingException, EndContainerMissingException, VariableInitializationException, DisplayException {
            if(match(Token.Type.BeginContainer)) {
                ASTNode variableDeclarations = variableDeclarations();
                ASTNode executableCode = executableCode((VariableDeclarationsNode) variableDeclarations);
                if(match(Token.Type.EndContainer)) {
                    return new ProgramNode(variableDeclarations, executableCode);
                } else {
                    throw new EndContainerMissingException("ERROR: Missing END CODE container.");
                }
            } else {
                throw new BeginContainerMissingException("ERROR: Missing BEGIN CODE container");
            }
        }



        private void scanFunction(VariableDeclarationsNode declarations) {
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
                            String[] userInputValues = getStrings(declarations.getVariableDeclarations(), variableNames);

                            for (int i = 0; i < variableNames.size(); i++) {
                                String variableName = variableNames.get(i);
                                String userInput = userInputValues[i].trim(); // Remove any leading/trailing whitespace
                                Object parsedValue = parseInput(userInput);
                                if (parsedValue != null) {
                                    LiteralNode valueNode = new LiteralNode(parsedValue);
                                    initializeVariable(variableName, valueNode);
                                    String dataType = getDataType(variableName, declarations.getVariableDeclarations());
                                    validateAssignmentType(dataType, variableName);
                                } else {
                                    throw new InputMismatchException("ERROR: No input provided for variable '" + variableName + "'.");
                                }
                            }

                        } else {
                            throw new SyntaxErrorException("ERROR: Expected colon (:) after SCAN keyword.");
                        }

                    }
                } catch (SyntaxErrorException | InputMismatchException | VariableInitializationException i ) {
                    errorCount++;
                    System.err.println(i.getMessage());
                    System.exit(1);
                }
        }

        private String[] getStrings(List<SingleVariableDeclaration> declarations, List<String> variableNames) {
            try {
                if(declarations.isEmpty() && !variableNames.isEmpty()) {
                    throw new VariableDeclarationException("ERROR: Variables '" + variableNames + "' not declared.");
                } else {
                    for(String varName : variableNames) {
                        if(declarations.stream().noneMatch(declaration -> declaration.getVariableNames().contains(varName))) {
                            throw new VariableDeclarationException("ERROR: Variable '" + varName + "' not declared.");
                        }
                    }
                }
            } catch (VariableDeclarationException v) {
                errorCount++;
                System.err.println(v.getMessage());
                System.exit(1);
            }


            Scanner scanner = new Scanner(System.in);

            // Prompt for input once, for all variables
            String inputLine = scanner.nextLine();
            String[] userInputValues = inputLine.split(",");

            try {
                if (userInputValues.length != variableNames.size()) {
                    throw new InputMismatchException("ERROR: The number of values provided does not match the number of variables.");
                }
            } catch (InputMismatchException i) {
                errorCount++;
                System.err.println(i.getMessage());
                System.exit(1);
            }
            return userInputValues;
        }

        private Object parseInput(String userInput) {
            // Try to parse user input to various data types
            if (userInput.matches("^\\d+$")) {
                return Integer.parseInt(userInput); // Integer
            } else if (userInput.matches("^\\d*\\.\\d+$")) {
                return Float.parseFloat(userInput); // Float
            } else if (userInput.equalsIgnoreCase("true") || userInput.equalsIgnoreCase("false")) {
                return userInput; // Boolean
            } else if (userInput.length() == 1) {
                return userInput.charAt(0); // Character
            }
            return null; // Invalid input
        }



        private String getDataType(String variableName, List<SingleVariableDeclaration> declarations) {
            try {
                for (SingleVariableDeclaration declaration : declarations) {
                    if (declaration.getVariableNames().contains(variableName)) {
                        return declaration.getDataType();
                    }
                }
                throw new VariableDeclarationException("ERROR: Variable '" + variableName + "' not declared.");
            } catch (VariableDeclarationException v) {
                errorCount++;
                System.err.println(v.getMessage());
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
                    List<String> variables = variableList();
                    for(String variable : variables) {

                        if (declaredVariables.contains(variable)) {
                            throw new VariableDeclarationException("ERROR: Variable '" + variable + "' is redeclared.");
                        }
                        validateAssignmentType(dataType, variable);
                        declaredVariables.add(variable);

                    }
                    if(variables.isEmpty()) {
                        throw new VariableDeclarationException("ERROR: Found Data Type token but variable list is empty.");
                    }
                    variableDeclaration = new SingleVariableDeclaration(dataType, variables);
                }
            } catch (VariableDeclarationException | VariableInitializationException v) {
                errorCount++;
                System.err.println(v.getMessage());
                System.exit(1);
            }
            return variableDeclaration;
        }



        // VariableList -> VariableName VariableList'
        private List<String> variableList() {
            List<String> variableNames = new ArrayList<>();
            List<String> dominoInitializedVariables = new ArrayList<>();
            try {
                String variableName = variableName();
                if (variableName != null) {
                /*
                    Check first for the first variable
                    Also include whether it is initialized
                    on declaration
                */
                    dominoInitializedVariables.add(variableName);
                    Object assignmentValue = assignment();
                    LiteralNode value;
                    if(assignmentValue != null) {
                        while(assignmentValue == "continue") {
                            dominoInitializedVariables.add(tokens.get(currentTokenIndex-1).getText());
                            variableNames.add(tokens.get(currentTokenIndex-1).getText());
                            assignmentValue = assignment();
                        }
                        value = new LiteralNode(assignmentValue);
                        for (String dominoInitializedVariable : dominoInitializedVariables) {
                            initializeVariable(dominoInitializedVariable, value);
                        }
                    }

                    /*
                     *  */
                    variableNames.add(variableName);
                    /*     */

                    while (match(Token.Type.Comma)) {
                        variableName = variableName();
                        if (variableName != null) {
                            assignmentValue = assignment();
                            if(assignmentValue != null) {
                                //                        System.out.println(variableName + "=" + assignmentValue);
                                value = new LiteralNode(assignmentValue);
                                initializeVariable(variableName, value);
                            }
                            variableNames.add(variableName);
                        } else {
                            throw new VariableDeclarationException("ERROR: Error parsing variable declarations. " +
                                    "Found a comma token without a succeeding variable declaration");
                        }

                    }


                } else {
                    // If variableName is null, it means no variable was declared.
                    // Check if it's because a reserved word was encountered.
                    if (match(Token.Type.Identifier) && isReservedVariable(tokens.get(currentTokenIndex - 1).getText())) {
                        throw new VariableDeclarationException("ERROR: Variable name '" + tokens.get(currentTokenIndex - 1).getText() + "' is a reserved word.");
                    }
                }
            } catch (VariableDeclarationException v) {
                errorCount++;
                System.err.println(v.getMessage());
                System.exit(1);
            }

            return variableNames;
        }



        // DataType -> INT | CHAR | BOOL | FLOAT
        private boolean DataType() {
            return match(Token.Type.Int) || match(Token.Type.Char) || match(Token.Type.Bool) || match(Token.Type.Float);
        }




        // VariableName -> Identifier
        private String variableName() throws VariableDeclarationException {
            String variableName = null;
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
                            "value token is missing. Please check again");
                } catch (VariableInitializationException | SyntaxErrorException v) {
                    errorCount++;
                    System.err.println(v.getMessage());
                    System.exit(1);
                }
            }
            return null;
        }




        // Expression -> Num | NumFloat | CharLiteral | BooleanLiteral
        private Object value() throws VariableInitializationException, SyntaxErrorException {
            String value = tokens.get(currentTokenIndex).getText();
            if(match(Token.Type.Identifier) ) {
                String variableName = tokens.get(currentTokenIndex - 1).getText();
                if(!match(Token.Type.Assign)) {
                    currentTokenIndex--;
                    ASTNode expr = expressionHandler();
                    if( expr instanceof ExpressionNode expressionNode) {
                        LiteralNode val = expressionNode.evaluateExpression();
                        return val.getValue();
                    } else if (variableInitializer.getValue(variableName) != null) {
                        return variableInitializer.getValue(variableName).getValue();
                    } else {
                        throw new VariableInitializationException("ERROR: Variable " + variableName + " has not been declared nor initialized.");
                    }
                } else {
                    currentTokenIndex--;
                    return "continue";
                }
            } else if (negationHandler()) {
                currentTokenIndex--;
                ASTNode expr = expressionHandler();
                if( expr instanceof ExpressionNode expressionNode) {
                    LiteralNode val = expressionNode.evaluateExpression();
                    return val.getValue();
                }
            } else if (match(Token.Type.Negation)) {
                if (negationHandler() ) {
                    currentTokenIndex-=2;
                    ASTNode expr = expressionHandler();
                    if( expr instanceof ExpressionNode expressionNode) {
                        LiteralNode val = expressionNode.evaluateExpression();
                        return val.getValue();
                    }
                } else if (match(Token.Type.Num)) {
                    return -(Integer.parseInt(tokens.get(currentTokenIndex-1).getText()));
                } else if (match(Token.Type.NumFloat)) {
                    return -(Float.parseFloat(tokens.get(currentTokenIndex-1).getText()));
                }
            } else if(match(Token.Type.Not)) {
                currentTokenIndex--;
                ASTNode expr = expressionHandler();
                if( expr instanceof ExpressionNode expressionNode) {
                    LiteralNode val = expressionNode.evaluateExpression();
                    return val.getValue();
                }
            } else if(match(Token.Type.Num)) {
                return Integer.parseInt(value);
            } else if(match(Token.Type.NumFloat)) {
                return Float.parseFloat(value);
            } else if(match(Token.Type.CharLiteral)) {
                return value.charAt(0);
            } else if (match(Token.Type.BooleanLiteral)) {
                if(!(tokens.get(currentTokenIndex).getType() == Token.Type.And ||
                        tokens.get(currentTokenIndex).getType() == Token.Type.Or)) {
                    return value;
                }
                currentTokenIndex--;
                ASTNode expr = expressionHandler();
                if( expr instanceof ExpressionNode expressionNode) {
                    LiteralNode val = expressionNode.evaluateExpression();
                    return val.getValue();
                }
            }
            return null;
        }


        private boolean negationHandler() {
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
                     (match(Token.Type.Num) || match(Token.Type.NumFloat))) ||
                     (match(Token.Type.Parentheses) && tokens.get(currentTokenIndex - 1).getText().equals("("));
        }

        private void validateAssignmentType(String dataType, String variable) throws VariableInitializationException {
            LiteralNode valueNode = variableInitializer.getValue(variable);
            if (valueNode != null) {
                Object assignedValue = valueNode.getValue();
                if (assignedValue != null) {
                    // Check if the assigned value matches the data type
                    switch (dataType) {
                        case "INT" -> {
                            try {
                                int intValue = Integer.parseInt(String.valueOf(assignedValue));
                                // The assigned value can be parsed as an int, so it matches the data type INT
                            } catch (NumberFormatException e) {
                                throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' is not a valid integer.");
                            }
                        }
                        case "CHAR" -> {
                            if (!(assignedValue instanceof Character)) {
                                throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' does not match data type CHAR.");
                            }
                        }
                        case "BOOL" -> {
                            if (!(assignedValue.equals("FALSE") || assignedValue.equals("TRUE"))) {
                                throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' does not match data type BOOL.");
                            }
                        }
                        case "FLOAT" -> {
                            try {
                                double floatValue = Double.parseDouble(String.valueOf(assignedValue));
                                // The assigned value can be parsed as a double, so it matches the data type FLOAT
                            } catch (NumberFormatException e) {
                                throw new VariableInitializationException("ERROR: Assigned value for variable '" + variable + "' is not a valid float.");
                            }
                        }
                        case null, default ->
                                throw new VariableInitializationException("ERROR: Unsupported data type '" + dataType + "'.");
                    }
                }
            }
        }






    // variable initializer
    private void initializeVariable(String varName, LiteralNode value) {
        variableInitializer.setValue(varName, value);
    }





    private ASTNode executableCode(VariableDeclarationsNode variableDeclarationsNode) {
        try {
            while(true) {
                if (match(Token.Type.Print)) {
                    DisplayNode displayNode = (DisplayNode) displayFunction();
                    System.out.print(displayNode.getOutput());
                } else if (match(Token.Type.Identifier)) {
                    if (match(Token.Type.Assign)) {
                        currentTokenIndex -= 2;
                        reinitializeVariable(variableDeclarationsNode);
                    }
                } else if (match(Token.Type.Scan)) {
                    currentTokenIndex--;
                    scanFunction(variableDeclarationsNode);
                } else {
                    break;
                }
            }

            if(DataType()) {
                currentTokenIndex--;
                throw new SyntaxErrorException("ERROR: Variable declaration found after executable code.");
            }
        } catch (VariableInitializationException | DisplayException | SyntaxErrorException v) {
            errorCount++;
            System.err.println(v.getMessage());
            System.exit(1);
        }


        return new ASTNode();
    }





    private void reinitializeVariable(VariableDeclarationsNode declarationStatements) {
        try {
            for(SingleVariableDeclaration declaration : declarationStatements.getVariableDeclarations() ) {
                String dataType = declaration.getDataType();
                List<String> dominoInitializedVariables = new ArrayList<>();
                while(match(Token.Type.Identifier)) {
                    String varName = tokens.get(currentTokenIndex-1).getText();
                    if(declaration.getVariableNames().contains(varName)) { //true
                        dominoInitializedVariables.add(varName); // x
                        Object assignedValue = assignment(); //
                        LiteralNode literalNode;
                        if(assignedValue != null) {
                            while(assignedValue == "continue") {
                                dominoInitializedVariables.add(tokens.get(currentTokenIndex-1).getText());
                                assignedValue = assignment();
                                literalNode = new LiteralNode(assignedValue);
                                for (String dominoInitializedVariable : dominoInitializedVariables) {
                                    initializeVariable(dominoInitializedVariable, literalNode);
                                }
                            }
                            literalNode = new LiteralNode(assignedValue);
                            initializeVariable(varName, literalNode);
                        }
                        validateAssignmentType(dataType, varName);
                    } else {
                        currentTokenIndex--;
                        break;
                    }

                }
            }
        } catch (VariableInitializationException v) {
            errorCount++;
            System.err.println(v.getMessage());
            System.exit(1);
        }

    }


    private ASTNode expressionHandler() throws SyntaxErrorException, VariableInitializationException {
        ASTNode exprNode = expr();
        if (exprNode != null) {
            return exprNode;
        } else {
            throw new SyntaxErrorException("ERROR: Invalid parsing of expression. Null value found.");
        }
    }
    


    private ASTNode displayFunction() throws DisplayException, VariableInitializationException, SyntaxErrorException {
        StringBuilder stringBuilder = new StringBuilder();
        if (match(Token.Type.Colon)) {
            while (true) {
                if (negationHandler()) {
                    currentTokenIndex--;
                    ASTNode expr = expressionHandler();
                    if( expr instanceof ExpressionNode expressionNode) {
                        LiteralNode val = expressionNode.evaluateExpression();
                        stringBuilder.append(val.getValue());
                    }
                } else if (match(Token.Type.Negation)) {
                    if (negationHandler()) {
                        currentTokenIndex-=2;
                        ASTNode expr = expressionHandler();
                        if( expr instanceof ExpressionNode expressionNode) {
                            LiteralNode val = expressionNode.evaluateExpression();
                            stringBuilder.append(val.getValue());
                        }
                    } else if (match(Token.Type.Num)) {
                        stringBuilder.append(-(Integer.parseInt(tokens.get(currentTokenIndex-1).getText())));
                    } else if (match(Token.Type.NumFloat)) {
                        stringBuilder.append(-(Float.parseFloat(tokens.get(currentTokenIndex-1).getText())));
                    }
                } else if(match(Token.Type.Not)) {
                    ASTNode expr = expressionHandler();
                    if( expr instanceof ExpressionNode expressionNode) {
                        LiteralNode val = expressionNode.evaluateExpression();
                        stringBuilder.append(val.getValue());
                    }
                } else if (match(Token.Type.Identifier)) {
                    // Check if the variable is initialized
                    String variableName = tokens.get(currentTokenIndex - 1).getText();
                    LiteralNode value = variableInitializer.getValue(variableName);
                    if(value == null) {
                        throw new VariableInitializationException("ERROR: Variable '" + variableName + "' is not initialized.");
                    }
                    currentTokenIndex--;
                    ASTNode expr = expressionHandler();
                    if( expr instanceof ExpressionNode expressionNode) {
                        value = expressionNode.evaluateExpression();
                    }
                    stringBuilder.append(value.getValue());
                } else if (match(Token.Type.Escape)) {
                    // Handle escape character
                    String escapeSequence = tokens.get(currentTokenIndex - 1).getText();
                    // Append the escape character to the output string
                    stringBuilder.append(escapeSequence);

                } else if (match(Token.Type.Num) || match(Token.Type.NumFloat) ||
                        match(Token.Type.CharLiteral) || match(Token.Type.BooleanLiteral) ||
                        match(Token.Type.StringLiteral)) {
                    stringBuilder.append(tokens.get(currentTokenIndex - 1).getText());
                    if (match(Token.Type.Concat)) {
                        continue;
                    } else {
                        break;
                    }
                } else if (match(Token.Type.NewLine)) {
                    stringBuilder.append(System.lineSeparator());
                    if (match(Token.Type.Concat)) {
                        continue;
                    } else {
                        break;
                    }
                } else {
                    throw new DisplayException("ERROR: Invalid display statement format.");
                }

                if (match(Token.Type.Concat)) {
                    continue;
                } else {
                    if(tokens.get(currentTokenIndex).getType() == Token.Type.NewLine ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.Identifier ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.StringLiteral ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.CharLiteral ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.Num ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.NumFloat ||
                            tokens.get(currentTokenIndex).getType() == Token.Type.BooleanLiteral) {
                        throw new DisplayException("ERROR: Invalid display statement format.");
                    }
                    break;
                }

            }
        }
        return new DisplayNode(stringBuilder.toString());
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
            if (match(Token.Type.Num)) {
                return new LiteralNode(-(Integer.parseInt(tokens.get(currentTokenIndex - 1).getText())));
            } else if (match(Token.Type.NumFloat)) {
                return new LiteralNode(-(Float.parseFloat(tokens.get(currentTokenIndex - 1).getText())));
            }
            throw new SyntaxErrorException("ERROR: Negation token found but no number token found.");
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
            if(variableInitializer.getInitializedVariables().containsKey(tokens.get(currentTokenIndex - 1).getText())) {
                return new LiteralNode(variableInitializer.getValue(tokens.get(currentTokenIndex - 1).getText()).getValue());
            }
            throw new SyntaxErrorException("ERROR: Variable '" + tokens.get(currentTokenIndex - 1).getText() + "' not initialized.");
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

}
