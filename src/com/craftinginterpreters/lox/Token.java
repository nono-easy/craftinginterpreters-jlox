package com.craftinginterpreters.lox;

class Token {
    /* 类型 */
    final TokenType type;
    /* 源码里的原始文本 */
    final String lexeme;
    /* 真实值 */
    final Object literal;
    /* 行号 */
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}