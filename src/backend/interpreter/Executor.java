package backend.interpreter;

import antlr4.*;
import intermediate.symtab.SymtabEntry;

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
        System.out.println("Visiting compound statement");        
        return visit(ctx.statementList());
    }
    
    public Object visitAssignmentStatement(Pcl4Parser.AssignmentStatementContext ctx)
    {
        System.out.println("Visiting assignment statement");
        
        String variableName = ctx.lhs().variable().getText();
        visit(ctx.lhs());
        Object value = visit(ctx.rhs());
        
        
        System.out.println("Will assign value " + value +
                           " to variable " + variableName);
        
        SymtabEntry varId = new SymtabEntry(variableName);
        varId.setValue(value); 
        
        
        return null;
    }
    
    public Object visitRepeatStatement(Pcl4Parser.RepeatStatementContext ctx)
    {
        System.out.println("Visiting REPEAT statement");
        return null;
    }
    
    public Object visitWhileStatement(Pcl4Parser.WhileStatementContext ctx) 
    {
    	System.out.println("Visiting WHILE statement");
    		Object value = visit(ctx.expression());
    		System.out.println(value); 
    	
    	return null;   
    }
    
    public Object visitForStatement(Pcl4Parser.ForStatementContext ctx) {
    	System.out.println("Visiting FOR statement");
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
    
    
    public Object visitExpression(Pcl4Parser.ExpressionContext ctx)
    {
        System.out.println("Visiting expression");
        return visitChildren(ctx);
    }
    
    //TODO : return variable value
    public Object visitVariable(Pcl4Parser.VariableContext ctx)
    {
        System.out.print("Visiting variable ");
        String variableName = ctx.getText();
        Object value = ctx.IDENTIFIER();
        
        System.out.println(value);
        return null;
    }
    

    public Object visitIntegerConstant(Pcl4Parser.IntegerConstantContext ctx) {
    	Object value = visit(ctx.INTEGER()); 
    	return value;
    }
    
    public Object visitNumber(Pcl4Parser.NumberContext ctx)
    {
        System.out.println("Visiting number: got value ");
        String text = ctx.unsignedNumber().integerConstant()
                                          .INTEGER().getText();
        Integer value = Integer.valueOf(text);
        
        return value;
    }
    
    public Object visitRealConstant(Pcl4Parser.RealConstantContext ctx) {
    	Object value = visit(ctx.REAL());
    	return value; 
    }
}
