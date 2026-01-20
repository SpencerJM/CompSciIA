import java.util.ArrayList;

public abstract class Expr {
    abstract String toLatex();
    abstract boolean isConstant();

    String name = null;
    Expr val = null;
    Double num = null;
}

class Constant extends Expr {
    private boolean isChar;

    public Constant(double value){ //initalizes constant with its value
        this.num = value;
        this.isChar = false;
    }
    public Constant(String name){ //constructs a constant represented by a character
        this.isChar = true;
        this.name = name;
        switch(name){
            case "\\pi" -> this.num = 3.14159;
            case "e" -> this.num = 2.71828;
        }
    }

    @Override
    public boolean isConstant(){ //Constants are constants
        return true;
    }

    @Override
    public String toLatex(){
        if(isChar){
            return name;
        } else {
            return "" + num;
        }
    }
}

class Product extends Expr {
    public ArrayList<Expr> factors;

    public Product(ArrayList<Expr> factors){ //initializes product object with array of factor expressions
        this.factors = factors;
    }
    public Product(Constant c, Expr ex){ //for easy creation of an expression multiplied by a constant
        this.factors = new ArrayList<>();
        factors.add(c);
        factors.add(ex);
    }

    @Override
    public boolean isConstant(){ //Checks to see if all factors are constants
        for(Expr factor : factors){
            if(factor instanceof Constant == false){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toLatex(){
        String out = "";
        for (Expr factor : factors){
            out += factor.toLatex();
        }
        return out;
    } 
}

class Variable extends Expr {

    public Variable(String name){ //initializes variable with its name
        this.name = name;
    }

    @Override
    public boolean isConstant(){ //variables are not constants
        return false;
    }

    @Override
    public String toLatex(){
        return name;
    }
}

class Power extends Expr {
    public Expr base;
    public Expr exponent;
    public boolean isRoot;

    public Power(Expr base, Expr exponent){ //initializes power with base expression and exponent expression
        this.base = base;
        this.exponent = exponent;
        this.isRoot = false;
    }

    public Power(Expr base, Expr root, boolean isRoot){
        this.base = base;
        this.exponent = root;
        this.isRoot = isRoot;
    }

    @Override
    public boolean isConstant(){ // power is a constant if both base and exponents are constants
        return (base instanceof Constant && exponent instanceof Constant);
    }

    @Override
    public String toLatex(){
        if(!isRoot){
            if(base instanceof TrigFunc){
                return (base.name + "^{" + exponent.toLatex() + "}(" + base.val.toLatex() + ")");
            }
            else if(base instanceof Constant || base instanceof Variable){
                return (base.toLatex() + "^{" + exponent.toLatex() + "}");
            }
            else {
                return ("(" + base.toLatex() + ")^{" + exponent.toLatex() + "}");
            }
        }
        else {
            if(exponent instanceof Constant && exponent.num == 2.0){
                return ("\\sqrt{" + base.toLatex() + "}");
            } else {
                return ("\\sqrt[" + exponent.toLatex() + "]{" + base.toLatex() + "}");
            }
        }
        
    }
}

class Sum extends Expr {
    public ArrayList<Expr> addends;

    public Sum(ArrayList<Expr> addends){ //initializes sum with array of addend expressions
        this.addends = addends;
        this.name = null;
    }

    @Override
    public boolean isConstant(){ //sum is constant if all addends are constants
        for(Expr addend : addends){
            if(addend instanceof Constant == false){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toLatex(){
        String out = addends.get(0).toLatex();
        for(int i = 1; i < addends.size(); i++){
            out += ("+" + addends.get(i).toLatex());
        }
        return out;
    }
}

class TrigFunc extends Expr {
    public Trig func;

    public TrigFunc(Trig func, Expr arg){ //initializes trig function with function type and argument
        this.func = func;
        this.val = arg;
        this.name = func.name;
    }

    @Override
    public boolean isConstant(){ // trig function is a constant if the argument is a constant
        return (val instanceof Constant);
    }

    @Override
    public String toLatex(){
        return (name + "(" + val.toLatex() + ")");
    }
}

class Fraction extends Expr {
    public Expr numerator;
    public Expr denominator;

    public Fraction(Expr numerator, Expr denominator){ //initializes fraction with numerator and denominator expressions
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public boolean isConstant(){
        return (numerator instanceof Constant && denominator instanceof Constant); //fraction is constant if both numerator and denominator are constants
    }

    @Override
    public String toLatex(){
        return ("\\frac{" + numerator.toLatex() + "}{" + denominator.toLatex() + "}");
    }
}

enum Trig { //enum with trig functions and stuff
    SIN("\\sin"),
    COS("\\cos"),
    TAN("\\tan"),
    ASIN("\\arcsin"),
    ACOS("\\arccos"),
    ATAN("\\arctan"),
    SEC("\\sec"),
    CSC("\\csc"),
    COT("\\cot");

    public String name;

    private Trig(String name){
        this.name = name;
    }
}
