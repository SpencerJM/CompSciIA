import java.util.ArrayList;

public class LatexHelper {
    
    public static Expr toExpr(String input){
        /*
        This method converts a Latex expression into an "Expr" expression.
        The order of operations are as follows:
        1. If there is addition, runs toExpr on the addends and returns a Sum object
        2. Otherwise, divides input into expressions:
            a. identifies any "sub-expressions" and runs toExpr on them before creating the Expr object
            b. checks to see if the expression has an exponent
        3. if only one expression is found, returns that Expr object. Otherwise, returns a product of all identified expressions

        This method assumes that characters are not used to show multiplication or division (x, *, /)
        */

        int layer = 0; //tracks whether at first layer (0) or inside brackets/parentheces (1+)

        
        //checking for addition starts here
        int lastPlus = -1;
        ArrayList<Expr> addends = new ArrayList<>(); //if addition is found on layer zero, this will be filled with addend expressions and later converted to Sum object
        boolean lastMinus = false;
        for(int i = 0; i < input.length(); i++){
            String c = input.substring(i, i+1);
            

            if(c.equals("(") || c.equals("{") || c.equals("[")){
                layer++;
                continue;
            }
            if(c.equals(")") || c.equals("}") || c.equals("]")){
                layer--;
                continue;
            }

            if(c.equals("+") && layer == 0){
                if(lastMinus){
                    addends.add(toExpr(input.substring(lastPlus, i)));
                } else {
                    addends.add(toExpr(input.substring(lastPlus+1, i)));
                }
                lastPlus = i;
                lastMinus = false;
            } else if(c.equals("-") && layer == 0 && i != 0){
                if(lastMinus){
                    addends.add(toExpr(input.substring(lastPlus, i)));
                } else {
                    addends.add(toExpr(input.substring(lastPlus+1, i)));
                }
                lastPlus = i;
                lastMinus = true;
            }
        }
        if(!addends.isEmpty()){
            if(lastMinus){
                addends.add(toExpr(input.substring(lastPlus)));
            } else {
                addends.add(toExpr(input.substring(lastPlus + 1)));
            }
            return new Sum(addends);
        }
        //checking for addition ends here

        //checking for multiplication starts here
        ArrayList<Expr> factors = new ArrayList<>();
        for(int i = 0; i < input.length(); i++){

            Expr factor;

            String c = input.substring(i, i+1);
            String fwd = input.substring(i);

            if(c.equals("\\")){ //accounts for special expressions beginning with backslash: pi, sqrt, frac, trig (may need to add int later)

                if(fwd.startsWith("\\pi")){ //pi
                    factor = new Constant("\\pi");
                    if(fwd.contains("^") && fwd.indexOf("^") == 3){
                        Expr exponent = toExpr(fwd.substring(fwd.indexOf("{"), closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                        factors.add(new Power(factor, exponent));
                        i += closeIndex(fwd, Brackets.CURLY_BRACKETS);
                    } else {
                        factors.add(factor);
                        i += 2;
                    }
                }
                else if(fwd.startsWith("\\sqrt")) { //root
                    if(fwd.contains("[") && fwd.indexOf("[") < fwd.indexOf("{")){ //checks if it is a non-square root (square breackets before argument in curly brackets)
                        Expr root = toExpr(fwd.substring(fwd.indexOf("[")+1, closeIndex(fwd, Brackets.SQUARE_BRACKETS))); //converts root into expression
                        Expr base = toExpr(fwd.substring(fwd.indexOf("{")+1, closeIndex(fwd, Brackets.CURLY_BRACKETS))); //converts base value into expression
                        factors.add(new Power(base, root, true)); //creates power object w/ isRoot set to true
                    }
                    else {
                        Expr base = toExpr(fwd.substring(fwd.indexOf("{")+1, closeIndex(fwd, Brackets.CURLY_BRACKETS))); //converts base value into expression
                        factors.add(new Power(base, new Constant(2.0), true)); //creates power object w/ isRoot set to true
                    }
                    i += closeIndex(fwd, Brackets.CURLY_BRACKETS); // moves index up to the closing of the square root, index will be beginning of next factor once incremented
                }
                else if(fwd.startsWith("\\frac")){
                    Expr numerator = toExpr(fwd.substring(fwd.indexOf("{")+1, closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                    Expr denominator = toExpr(fwd.substring(closeIndex(fwd, Brackets.CURLY_BRACKETS)+2, closeIndex(fwd, Brackets.CURLY_BRACKETS, 2)));
                    factors.add(new Fraction(numerator, denominator));
                    i += closeIndex(fwd, Brackets.CURLY_BRACKETS, 2); // moves index up to the closing of the fraction, index will be beginning of next factor once incremented
                }
                else { //trig functions
                    Expr arg = toExpr(fwd.substring(fwd.indexOf("(")+1, closeIndex(fwd, Brackets.PARENTHECES))); //creates expression for function's argument
                    switch(fwd.substring(1, Math.min(fwd.indexOf("("), fwd.indexOf("^")))){ //determines function type and creates appropriate object
                        case "sin" -> factor = new TrigFunc(Trig.SIN, arg);
                        case "cos" -> factor = new TrigFunc(Trig.COS, arg);
                        case "tan" -> factor = new TrigFunc(Trig.TAN, arg);
                        case "arcsin" -> factor = new TrigFunc(Trig.ASIN, arg);
                        case "arccos" -> factor = new TrigFunc(Trig.ACOS, arg);
                        case "arctan" -> factor = new TrigFunc(Trig.ATAN, arg);
                        case "sec" -> factor = new TrigFunc(Trig.SEC, arg);
                        case "csc" -> factor = new TrigFunc(Trig.CSC, arg);
                        case "cot" -> factor = new TrigFunc(Trig.COT, arg);
                        default -> factor = null;
                    }

                    if(fwd.contains("^") && fwd.indexOf("^") < fwd.indexOf("(")){ //trig functions are special in their syntax and thus do not go through the standard check for exponents.
                        Expr exponent = toExpr(fwd.substring(fwd.indexOf("{")+1, closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                        factors.add(new Power(factor, exponent));
                    } else {
                        factors.add(factor);
                    }
                    i += closeIndex(fwd, Brackets.PARENTHECES); // moves index up to the closing of the function, index will be beginning of next factor once incremented
                } //end of function checker

                if(endConstantIndex(fwd) != 0){
                    factor = new Constant(Double.parseDouble(fwd.substring(0, endConstantIndex(fwd))));
                    if(fwd.substring(endConstantIndex(fwd), endConstantIndex(fwd)+1).equals("^")){
                        Expr exponent = toExpr(fwd.substring(fwd.indexOf("{"), closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                        factors.add(new Power(factor, exponent));
                        i += closeIndex(fwd, Brackets.CURLY_BRACKETS);
                    } else {
                        factors.add(factor);
                        i += endConstantIndex(fwd) - 1;
                    }
                } else { //variables and defined constants w/o backslash
                    switch(c){
                        case "e" -> {
                            factor = new Constant("e");
                            if(fwd.contains("^") && fwd.indexOf("^") == 1){
                                Expr exponent = toExpr(fwd.substring(fwd.indexOf("{"), closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                                factors.add(new Power(factor, exponent));
                                i += closeIndex(fwd, Brackets.CURLY_BRACKETS);
                            } else {
                                factors.add(factor);
                                //index (i) increments by zero in this case
                            }
                        }
                        case "d" -> { //for things like "dx"
                            factor = new Variable(fwd.substring(0, 2));
                            if(fwd.contains("^") && fwd.indexOf("^") == 2){
                                Expr exponent = toExpr(fwd.substring(fwd.indexOf("^"), closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                                factors.add(new Power(factor, exponent));
                                i += closeIndex(fwd, Brackets.CURLY_BRACKETS);
                            } else {
                                factors.add(factor);
                                i += 1;
                            }
                        }
                        default -> { //all variables are single letters
                            factor = new Variable(c);
                            if(fwd.contains("^") && fwd.indexOf("^") == 1){
                                Expr exponent = toExpr(fwd.substring(fwd.indexOf("{"), closeIndex(fwd, Brackets.CURLY_BRACKETS)));
                                factors.add(new Power(factor, exponent));
                                i += closeIndex(fwd, Brackets.CURLY_BRACKETS);
                            } else {
                                factors.add(factor);
                                //index (i) increments by zero in this case
                            }
                        }
                    }
                }


                
            } 
        } 
        

        //throws an error if there are no factors, returns single expression if only one found, returns product if multiple expressions
        switch(factors.size()){
            case 0 -> {throw new RuntimeException("No expressions found by LatexHelper.toExpr");}
            case 1 -> {return factors.get(0);} //VSC is only satisfied when this is in curly brackets. no clue why
            default -> {return new Product(factors);}
        }
    }

    private static int closeIndex(String input, Brackets type){
        String close;

        int layer = 0;

        switch(type){
            case Brackets.PARENTHECES -> close = ")";
            case Brackets.CURLY_BRACKETS -> close = "}";
            case Brackets.SQUARE_BRACKETS -> close = "]";
            default -> close = ")";
        }

        for(int i = 0; i < input.length(); i++){
            String c = input.substring(i, i+1);
            
            switch(c){
                case "(", "[", "{" -> layer++;
                case ")", "]", "}" -> layer--;
                default -> {continue;} //VSC is only satisfied when this is in curly brackets. no clue why
            }

            if(layer == 0 && c.equals(close)){
                return i;
            }
        }
        return -1;
    }

    private static int closeIndex(String input, Brackets type, int num){
        String close;

        int layer = 0;
        int count = 0;

        switch(type){
            case Brackets.PARENTHECES -> close = ")";
            case Brackets.CURLY_BRACKETS -> close = "}";
            case Brackets.SQUARE_BRACKETS -> close = "]";
            default -> close = ")";
        }

        for(int i = 0; i < input.length(); i++){
            String c = input.substring(i, i+1);
            
            switch(c){
                case "(", "[", "{" -> layer++;
                case ")", "]", "}" -> layer--;
                default -> {continue;} //VSC is only satisfied when this is in curly brackets. no clue why
            }

            if(layer == 0 && c.equals(close)){
                count++;
                if(count == num){
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("UnnecessaryContinue") //to make both the reader and code happy
    private static int endConstantIndex(String input){ //this method is terrible but its the only non-painful way I could think of
        for(int i = 0; i < input.length(); i++){
            switch(input.substring(i, i+1)){
                case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "." -> {continue;}
                default -> {return i;}
            }
        }
        return 0;
    }
}



enum Brackets{
    PARENTHECES,
    SQUARE_BRACKETS,
    CURLY_BRACKETS
}