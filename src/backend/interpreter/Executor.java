package antlr4.backend.interpreter;

import antlr4.*;
import intermediate.symtab.*;

public class Executor extends Pcl4BaseVisitor<Object>
{

    private Symtab symtab = new Symtab();
	
    public Object visitProgram(Pcl4Parser.ProgramContext ctx)
    {
        System.out.println("Visiting program");
        return visit(ctx.block());
    }
    
    public Object visitStatement(Pcl4Parser.StatementContext ctx)
    {
        System.out.print("Line " + ctx.getStart().getLine() + ": ");
        return visitChildren(ctx);
    }
    
    public Object visitStatementList(Pcl4Parser.StatementListContext ctx)
    {
        System.out.println("Visiting statement list");

        for (Pcl4Parser.StatementContext stmtCtx : ctx.statement())
        {
            visit(stmtCtx);
        }
        
        return null;
    }
    
    public Object visitCompoundStatement(Pcl4Parser.CompoundStatementContext ctx)
    {
        System.out.println("Vsiting compound statement");        
        return visit(ctx.statementList());
    }
    
    
    
    public Object visitAssignmentStatement(Pcl4Parser.AssignmentStatementContext ctx)
    {
        System.out.println("Visiting assignment statement");
        String variableName = ctx.lhs().variable().getText();
        //visit(ctx.lhs());
        Object value = visit(ctx.rhs());
        double entryval;
        if(value.getClass().getName().equals("java.lang.Integer")) {
        	//Isn't the most elegant solution, but gets the job done
        	entryval = ((Integer)value).intValue();
        }
        else {
        	entryval = (Double) value;
        }
        SymtabEntry variableID = symtab.enter(variableName);
        variableID.setValue(entryval);
        System.out.println("Will assign value " +  value +
                           " to variable " + variableName);
        return null;
    }
    
    
    public Object visitRepeatStatement(Pcl4Parser.RepeatStatementContext ctx)
    {
       System.out.println("Visiting REPEAT statement");
    	Pcl4Parser.StatementListContext list = ctx.statementList();
    	Object b;
    	
    	do {
    		visit(list);
    		b = visit(ctx.expression());
    	} while (b.equals(false));
    	
        return null;
    }
    
    public Object visitWhileStatement(Pcl4Parser.WhileStatementContext ctx) 
    {
    	System.out.println("Visiting WHILE statement");
    	Pcl4Parser.StatementListContext list = ctx.statementList();
    	Object b = false;

    	while (b.equals(false)) {
    		
    		visit(list);
    		b = visit(ctx.expression());
    	}

    	return null;   
    }
   
    public Object visitIfStatement(Pcl4Parser.IfStatementContext ctx) {
    	System.out.println("Visiting IF statement");
    	boolean testCondition = (Boolean)visit(ctx.expression());
    	if(testCondition) {
    		visit(ctx.getChild(3));
    	}
    	else if (ctx.getChildCount() > 4) {
    		visit(ctx.getChild(5));
    	}
    	return null;
    }
    
    public Object visitForStatement(Pcl4Parser.ForStatementContext ctx) {
    	System.out.println("Visiting FOR statement");
    	int i = (int)visitExpression(ctx.expression(0));
    	boolean too = false;
    	if(ctx.TO()!=null) too = true;
    	if(too) {
    		boolean done = false;
    		while(!done) {
    			visitStatement(ctx.statement());
    			i++;
    			done = i > (int)visitExpression(ctx.expression(1)); 
    		}
    	}
    	else {
    		boolean done = false;
    		while(!done) {
    			visitStatement(ctx.statement());
    			i--;
    			done = i < (int)visitExpression(ctx.expression(1));
    		}
    	}
    	return null;
    }
    
    public Object visitCaseStatement(Pcl4Parser.CaseStatementContext ctx) {
    	System.out.println("Visiting CASE statement");
    	//evaluate expression
    	int value = (int) visitExpression(ctx.expression());
    	//Loop over constants list to look for value match
    	for(int i = 0; i < ctx.constantList().size(); i++) {
    		//Match?
    		if(value == (int) visitConstantList(ctx.constantList(i)))
    		{
    			visitStatement(ctx.statement(i));
    			break;
    		}
    	}
    	return null; 
    }
    
    public Object visitWriteStatement(Pcl4Parser.WriteStatementContext ctx)
    {
        System.out.println("Visiting WRITE statement");
        return null;
    }
    
    public Object visitWritelnStatement(Pcl4Parser.WritelnStatementContext ctx)
    {
        System.out.println("Visiting WRITELN statement");
        return null;
    }
    
    //TODO
    public Object visitExpression(Pcl4Parser.ExpressionContext ctx)
    {
        if(ctx.getChildCount() == 1) {
        	return visitChildren(ctx);
        }
        //Binary Expression
        //Double lhs = (Double) 
        Object lefthander = visit(ctx.getChild(0));
        double lhs;
        if(lefthander.getClass().getAnnotatedInterfaces().equals("java.lang.Integer")) {
        	lhs = ((Integer)lefthander).intValue();
        }
        else {
        	lhs = (Double) lefthander;
        }
        
        Object righthander = visit(ctx.getChild(2));
        double rhs;
        if(righthander.getClass().getName().equals("java.lang.Integer")) {
        	//Isn't the most elegant solution, but gets the job done
        	rhs = ((Integer)righthander).intValue();
        }
        else {
        	rhs = (Double) righthander;
        }
        String op = ctx.relOp().getText();
        switch(op) {
        	case "="  : return lhs == rhs;
        	case "<>" : return lhs != rhs; 
        	case "<"  : return lhs < rhs; 
        	case "<=" : return lhs <= rhs; 
        	case ">"  : return lhs > rhs; 
        	case ">=" : return lhs >= rhs; 
        	default   : System.out.println("Error: Invalid Relational Operator");
        }
        return null;
    }
    
    //TODO
    public Object visitSimpleExpression(Pcl4Parser.SimpleExpressionContext ctx) {
    	//System.out.println("Visiting simple expression");
    	boolean neg = false;
    	if(ctx.sign() != null) {
    		neg = (ctx.sign().getText().equals("-")) ? true : false; 
    		ctx.children.remove(ctx.sign());
    	}
    	
    	Object val = visit(ctx.getChild(0));
		double value;
		if(val.getClass().getName().equals("java.lang.Integer")) {
			value = ((Integer)val).intValue(); //casts the int value as a double
		}
		else if(val.getClass().getName().equals("java.lang.Boolean")) {
			return val;
		}
		else
			value = (Double) val;
    	
    	if(ctx.getChildCount() > 1) {
    		String addOp = (String) ctx.getChild(1).getText();
    		if(addOp.equals("or")) {
    			boolean lhs = (Boolean) visit(ctx.getChild(0));
    			for(int i = 1; i < ctx.getChildCount(); i+=2) {
    				addOp = (String) ctx.getChild(i).getText();
    				if(!addOp.equals("or")) {
    					System.out.printf("ERROR Line %d: Invalid expression");
						return null;
    				}
    				boolean rhs = (boolean) visit(ctx.getChild(i+1));
    				lhs = lhs || rhs;
    			}
    			return lhs;
    		}
    		for(int i = 1; i < ctx.getChildCount(); i+=2) {
    			String op = (String) ctx.getChild(i).getText();
    			Object righthander = visit(ctx.getChild(i+1));
		        double rhs;
		        if(righthander.getClass().getName().equals("java.lang.Integer")) {
		        	//Isn't the most elegant solution, but gets the job done
		        	rhs = ((Integer)righthander).intValue();
		        }
		        else {
		        	rhs = (Double) righthander;
		        }
    			
    			switch (op) {
        			case "+" : value += rhs; break;
        			default  : value -= rhs; break;
        		}
    		}
    		return (neg) ? -1* value : value;
    	}
    	else {
    		if(neg) {
    			return -1*value;
    		}
    		return visit(ctx.getChild(0));
    	}
    }
    
    //TODO
    public Object visitTerm(Pcl4Parser.TermContext ctx) {
    	//System.out.println("Visiting term");
		if(ctx.getChildCount() > 1) 
		{
			String op = (String) ctx.getChild(1).getText();
			if(op.equals("and")) {
				Boolean value = (Boolean) visit(ctx.getChild(0));
				for(int i = 1; i < ctx.getChildCount(); i+=2) {
					op = (String) ctx.getChild(i).getText();
					if(!op.equals("and")) {
						System.out.printf("ERROR Line %d: Invalid expression");
						return null;
					}
					Boolean rhs = (Boolean) visit(ctx.getChild(i+1));
					value = value && rhs;
				}
				return value;
			}
			
			//Double value = (Double) visit(ctx.getChild(0));
			Object entryval = visit(ctx.getChild(0));
	        double value;
	        if(entryval.getClass().getName().equals("java.lang.Integer")) {
	        	//Isn't the most elegant solution, but gets the job done
	        	value = ((Integer)entryval).intValue();
	        }
	        else {
	        	value = (Double) entryval;
	        }
			for(int i = 1; i < ctx.getChildCount(); i+=2) 
			{
				op = (String) ctx.getChild(i).getText();
				//Double rhs = (Double) visit(ctx.getChild(i+1)); Is basically what the code below does (int casting is a pain)
				Object righthander = visit(ctx.getChild(i+1));
		        double rhs;
		        if(righthander.getClass().getName().equals("java.lang.Integer")) {
		        	//Isn't the most elegant solution, but gets the job done
		        	rhs = ((Integer)righthander).intValue();
		        }
		        else {
		        	rhs = (Double) righthander;
		        }
				
		        switch(op) {
					
					case "mod" : value = value % rhs; break;
					
					case "div" : 
					{
						int something = (int) (value / rhs);
						value = (double) something;
					}
					break;
					
					case "/" : 
					{
				 		if(rhs != 0.0)
				 			value /= rhs;
				 		else 
				 		{
				 			System.out.printf("Error Line %d: Division by Zero!", ctx.getStart().getLine());
				 			return null;
				 		}
				 	} 
					break;
				 	
					default  : value *= rhs; 
				}
			}
			return value;
		}
		else 
			return visit(ctx.getChild(0));
    }
    
    public Object visitParenthesizedExpression(Pcl4Parser.ParenthesizedExpressionContext ctx) {
    	System.out.println("Visiting parenthesized expression");
    	return visit(ctx.expression()); 
    }
    
    public Object visitNotFactor(Pcl4Parser.NotFactorContext ctx) {
    	boolean b = (Boolean) visit(ctx.getChild(1));
    	return !b;
    }
    
    //TODO : return variable value
    public Object visitVariable(Pcl4Parser.VariableContext ctx)
    {
    	System.out.print("Visiting variable ");
        String variableName = ctx.getText();
        SymtabEntry entry = symtab.lookup(variableName);
        if(entry == null) {
        	System.out.printf("ERROR Line %d: Undeclared Variable\n", ctx.getStart().getLine());
        }
        double value = entry.getValue();
        //Object value = ctx.IDENTIFIER();
        System.out.printf("%s : %f\n", variableName, value);
        
        
        return value;
    }
   
    


    public Object visitIntegerConstant(Pcl4Parser.IntegerConstantContext ctx) {
    	Integer value = Integer.valueOf(ctx.getText()); 
    	//System.out.print(value);
    	return value;
    }
    
    
    public Object visitNumber(Pcl4Parser.NumberContext ctx)
    {
        boolean neg = (ctx.sign() != null) ? true : false;
        
    	System.out.print("Visiting number: got value ");
        if(ctx.unsignedNumber().integerConstant() == null) {
        	Double value = (Double) visitRealConstant(ctx.unsignedNumber().realConstant());
        	System.out.println(value);
        	return (neg) ? -1* value : value;
        }
        else {
        	Integer value = (Integer) visitIntegerConstant(ctx.unsignedNumber().integerConstant());
        	System.out.println(value);
        	return (neg) ? -1* value : value;
        }
        
    }
    
    public Object visitRealConstant(Pcl4Parser.RealConstantContext ctx) {
    	Double value = Double.valueOf(ctx.getText());
    	//System.out.print(value);
    	return value; 
    }
}
