package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Literal;

/**
 * 把 AST 树打印成字符串
 * Expr 的访问者
 */
class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    // 二元表达式
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    // 括号表达式
    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    // 字面量
    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    // 一元表达式
    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    // 递归打印
    // 树：(-123) * (45.67)
    // 打印：(* (- 123) (group 45.67))
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        // 在这里 new 出 a == b == c 的左结合树，然后：
        Literal a = new Expr.Literal("a");
        Literal b = new Expr.Literal("b");
        Literal c = new Expr.Literal("c");

        Token euqalEqual1 = new Token(TokenType.EQUAL_EQUAL, "==", null, 1);
        Token euqalEqual2 = new Token(TokenType.EQUAL_EQUAL, "==", null, 1);

        // a == b == c
        Expr expr = new Expr.Binary(
                new Expr.Binary(
                        a,
                        euqalEqual1,
                        b),
                euqalEqual2,
                c);

        System.out.println(new AstPrinter().print(expr));
        // (== (== a b) c)
    }
}
