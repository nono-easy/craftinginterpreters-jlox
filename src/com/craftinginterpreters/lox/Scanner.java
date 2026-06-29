package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * 把源码字符串切割成一串 Token 。
 */
class Scanner {
    // 完整源码文本
    private final String source;
    // Token 列表
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    // 关键字表
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // 处于下一个原始文本的最开始位置
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // 单字符 token
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;

            // 可能是一个或者两个字段的token
            // match()：看当前未消费字符，如果符合，就移动 current
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '/':
                if (match('/')) {
                    // 行注释 直接吃掉整行
                    // peek() 看当前未消费字符，不移动 current
                    while (peek() != '\n' && !isAtEnd()) {
                        // 直接吃当前字符并移动 current
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // 忽略空格、回车、Tab
                break;

            case '\n':
                line++;
                break;

            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    // 吃掉当前字符，读取并移动
    private char advance() {
        return source.charAt(current++);
    }

    // addToken(TokenType type, Object literal) 简化版
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // 把“刚刚扫过的这一段源码”正式记录成 token
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // 看下一个字符是不是 expected；如果是，移动 current 指针位置
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    // 看下个字符，但是不移动 current 指针位置
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    // 判断是否为数字
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // 处理字符串
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // 处理数字
    private void number() {
        while (isDigit(peek()))
            advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    // 往后看第二个字符
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    // 处理标识符
    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }

    // 判断是否为字母
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // 判断是否为字母或数字
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}