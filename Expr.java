
abstract class Expr {
    //abstract String toLatex();
    abstract boolean isConstant();
}

class Constant extends Expr {
    public double value;

    public Constant(double value){ //initalizes constant with its value
        this.value = value;
    }

    @Override
    public boolean isConstant(){ //Constants are constants
        return true;
    }
}

class Product extends Expr {
    public Expr[] factors;

    public Product(Expr[] factors){ //initializes product object with array of factor expressions
        this.factors = factors;
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
}

class Variable extends Expr {
    public String name;

    public Variable(String name){ //initializes variable with its name
        this.name = name;
    }

    @Override
    public boolean isConstant(){ //variables are not constants
        return false;
    }
}

class Power extends Expr {
    public Expr base;
    public Expr exponent;

    public Power(Expr base, Expr exponent){ //initializes power with base expression and exponent expression
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public boolean isConstant(){ // power is a constant if both base and exponents are constants
        return (base instanceof Constant && exponent instanceof Constant);
    }
}

class Sum extends Expr {
    public Expr[] addends;

    public Sum(Expr[] addends){ //initializes sum with array of addend expressions
        this.addends = addends;
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
}

class TrigFunc extends Expr {
    public Trig func;
    public Expr arg;

    public TrigFunc(Trig func, Expr arg){ //initializes trig function with function type and argument
        this.func = func;
        this.arg = arg;
    }

    @Override
    public boolean isConstant(){ // trig function is a constant if the argument is a constant
        return (arg instanceof Constant);
    }
}

enum Trig { //enum with trig functions and stuff
    SIN,
    COS,
    TAN,
    ASIN,
    ACOS,
    ATAN,
    SEC,
    CSC,
    COT
}
