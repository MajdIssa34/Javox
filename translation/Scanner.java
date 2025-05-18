package translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static translation.TokenType.*;


class Scanner {
    private final String source; // Source code to scan.
    private final List<Token> tokens = new ArrayList<>(); // List of scanned tokens.
    private static final Map<String, TokenType> keywords; // Map of keywords to token types.
    private int start = 0; // Start of the current lexeme.
    private int current = 0; // Current position in the source code.
    private int line = 1; // Current line in the source code.

    // Static block to initialize the keywords map.
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
        keywords.put("read", READ); // new read functionality
        keywords.put("rand", RAND); // new rand functionality
        keywords.put("loop", LOOP); // new loop function
        keywords.put("in", IN);
        keywords.put("printonly", PRINTONLY);

    }

    // Constructor to initialize the scanner with source code.
    Scanner(String source) {
        this.source = source;
    }

    // Main method to scan the source code into a list of tokens.
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // Start of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line)); // Add EOF token at the end.
        return tokens;
    }

    // Check if the scanner has reached the end of the source code.
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Advance to the next character in the source code.
    private char advance() {
        return source.charAt(current++);
    }

    // Add a token without a literal value.
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Add a token with a literal value.
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // Scan a single token from the source code.
    private void scanToken() {
        char c = advance();
        switch (c) {
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
            case '!':
                if (match('!')) {
                    addToken(RAND); // Add BANG_BANG for '!!'
                } else {
                    addToken(BANG);
                }
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                if (match('-')) {
                    addToken(READ); // Add LESS_MINUS for '<-' and read
                } else if (match('=')) {
                    addToken(LESS_EQUAL);
                } else {
                    addToken(LESS);
                }
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // Single-line comment.
                    while (peek(0) != '\n' && !isAtEnd())
                        advance();
                } else if (match('*')) {
                    // Multi-line comment.
                    multiLineComment();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
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
                    String unexpectedCharacter = String.valueOf(c);
                    Token errorToken = new Token(TokenType.STRING, unexpectedCharacter, null, line);

                    // Pass the Token to Javox.error
                    Lox.error(errorToken, "Unexpected character.");
                }
                break;
            case 'o':
                if (match('r')) {
                    addToken(OR);
                }
                break;
            case ':':
                if (isAlpha(peek(0))) {
                    symbol();
                } else {
                    Lox.error(currentToken(), "Invalid symbol name after ':'.");
                }
                break;
        }
    }

    // Scan an identifier or keyword.
    private void identifier() {
        while (isAlphaNumeric(peek(0)))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }

    // Match the current character against an expected character.
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    private void symbol() {
        while (isAlphaNumeric(peek(0)))
            advance();

        // Check if the symbol name is valid (not empty)
        if (start + 1 == current) { // +1 because start is at ':'
            Lox.error(currentToken(), "Symbol name cannot be empty.");
            return;
        }

        String text = source.substring(start + 1, current); // +1 to skip the colon
        addToken(TokenType.SYMBOL, text);
    }

    // Peek at the next character without advancing.
    private char peek(int distance) {
        if (current + distance >= source.length())
            return '\0';
        return source.charAt(current + distance);
    }

    // Scan a string literal.
    private void string() {
        while (peek(0) != '"' && !isAtEnd()) {
            if (peek(0) == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            Token errorToken = new Token(STRING, source.substring(start, current), null, line);
            Lox.error(errorToken, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // Check if a character is a digit.
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Scan a number literal.
    private void number() {
        while (isDigit(peek(0)))
            advance();

        // Look for a fractional part.
        if (peek(0) == '.' && isDigit(peek(1))) {
            // Consume the "."
            advance();
            while (isDigit(peek(0)))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || (c >= '0' && c <= '9');
    }

    // Scan a multi-line comment.
    private void multiLineComment() {
        while (!(peek(0) == '*' && peek(1) == '/') && !isAtEnd()) {
            if (peek(0) == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            // Create a Token to represent the error
            Token errorToken = new Token(TokenType.EOF, "", null, line);
            Lox.error(errorToken, "Unterminated multi-line comment.");
            return;
        }

        // Consume the closing */
        advance(); // consume '*'
        advance(); // consume '/'
    }

    // Helper function to return the current token
    private Token currentToken() {
        String lexeme = source.substring(start, current);
        return new Token(TokenType.SYMBOL, lexeme, null, line);
    }
}
