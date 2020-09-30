package antlr4.backend.interpreter;

import antlr4.*;
import intermediate.symtab.*;

public class Executor extends Pcl4BaseVisitor<Object>
{

    
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
        //System.out.println(visit(ctx.statementList()));        
        return visit(ctx.statementList());
    }
    
    
    
    public Object visitAssignmentStatement(Pcl4Parser.AssignmentStatementContext ctx)
    {
        System.out.println("Visiting assignment statement");
        String variableName = ctx.lhs().variable().getText();
        visit(ctx.lhs());
        Object value = visit(ctx.rhs());
        //assign(variableName, value);
        SymtabEntry variableID = new SymtabEntry(variableName);
        //variableID.setValue((Double)value);
        //System.out.println("Will assign value " +  value +
                           //" to variable " + variableName);
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
   
    //TODO
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
    
    //TODO
    public Object visitForStatement(Pcl4Parser.ForStatementContext ctx) {
    	System.out.println("Visiting FOR statement");
    	return null; 
    }
    
    //TODO
    public Object visitCaseStatement(Pcl4Parser.CaseStatementContext ctx) {
    	System.out.println("Visiting CASE statement");
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
        Double lhs = (Double) visit(ctx.getChild(0));
        Double rhs = (Double) visit(ctx.getChild(2));
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
    	System.out.println("Visiting simple expression");
    	boolean neg = false;
    	if(ctx.sign() != null) {
    		neg = (ctx.sign().getText().equals("=")) ? true : false; 
    		ctx.children.remove(ctx.sign());
    	}
    	
    	if(ctx.getChildCount() > 1) {
    		Double value = (Double) visit(ctx.getChild(0));
    		for(int i = 1; i < ctx.getChildCount(); i+=2) {
    			String op = (String) visit(ctx.getChild(i));
    			Double rhs = (Double) visit(ctx.getChild(i+1));
    			
    			switch (op) {
        			case "+" : value += rhs; break;
        			default  : value -= rhs; break;
        		}
    		}
    		return (neg) ? -1* value : value;
    	}
    	else {
    		return visit(ctx.getChild(0));
    	}
    }
    
    //TODO
    public Object visitTerm(Pcl4Parser.TermContext ctx) {
    	System.out.println("Visiting term");
		if(ctx.getChildCount() > 1) {
			Double value = (Double) visit(ctx.getChild(0));
			for(int i = 1; i < ctx.getChildCount(); i+=2) {
				String op = (String) visit(ctx.getChild(i));
				Double rhs = (Double) visit(ctx.getChild(i+1));
				switch(op) {
				 	case "/" : {
				 		if(rhs != 0.0)
				 			value /= rhs;
				 		else {
				 			System.out.println("Error: Division by Zero!");
				 			return null;
				 		}
				 	}
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
    	return null; 
    }
    
    //TODO : return variable value
    public Object visitVariable(Pcl4Parser.VariableContext ctx)
    {
        System.out.print("Visiting variable ");
        String variableName = ctx.getText();
        ctx.start.getLine();         
        
        //Object value = ctx.IDENTIFIER();
        System.out.println(variableName);
        
        
        return null;
    }
   
    


    public Object visitIntegerConstant(Pcl4Parser.IntegerConstantContext ctx) {
    	Integer value = Integer.valueOf(ctx.getText()); 
    	//System.out.print(value);
    	return value;
    }
    
    
    public Object visitNumber(Pcl4Parser.NumberContext ctx)
    {
        System.out.print("Visiting number: got value ");
        if(ctx.unsignedNumber().integerConstant() == null) {
        	Double value = (Double) visitRealConstant(ctx.unsignedNumber().realConstant());
        	System.out.println(value);
        	return value;
        }
        else {
        	Integer value = (Integer) visitIntegerConstant(ctx.unsignedNumber().integerConstant());
        	System.out.println(value);
        	return value;
        }
        
    }
    
    public Object visitRealConstant(Pcl4Parser.RealConstantContext ctx) {
    	Double value = Double.valueOf(ctx.getText());
    	//System.out.print(value);
    	return value; 
    }
}
