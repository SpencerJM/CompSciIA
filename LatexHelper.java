import java.util.ArrayList;

public class LatexHelper {
    
    public static Expr toExpr(String input){
        /*
        This method converts a Latex expression into an "Expr" expression.
        The order of operations are as follows:
        1. If there is addition, runs toExpr on the addends and returns a Sum object
        2. If there is multiplication, divides the string into factors, runs toExpr on the factors, and returns a Product object
        3. If the input is a special function (trig, power, fraction), runs toExpr on their associated expressions and returns the appropriate object
        4. If the input is a variable, returns a Variable object. All variables are one character except for dx or similar
        5. If the input is a constant, returns a constant object. e and \pi are considered constants.

        This order assumes that there are no characters used to indicate multiplication (X, *)
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



    }
}
