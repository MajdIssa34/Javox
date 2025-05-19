# Javox 🛠️

**Javox** is a fully functional programming language I built from scratch using Java.  
It started as a journey through *Crafting Interpreters* — but quickly became my own project, with custom features and real-world functionality.

---

## ✨ Features

- ✅ Full **scanner**, **parser**, and **interpreter**
- ✅ Variable declarations and expressions
- ✅ Arithmetic and logical operations
- ✅ `if`, `else`, and `while` statements
- ✅ User-defined functions with parameters and return values
- ✅ String and number support, including mixed `+` operations
- ✅ **Multi-line comments** (`/* ... */`)
- ✅ New keyword: `read`
- ✅ Error handling with line numbers and clear messages

---

## 💻 Sample Code (statements.lox)

```lox
var x = 10;
var y = 5;
print "Sum: " + (x + y);

if (x > y) {
  print "x is greater";
} else {
  print "y is greater or equal";
}

fun multiply(a, b) {
  return a * b;
}

print "4 * 5 = " + multiply(4, 5);

var counter = 3;
while (counter > 0) {
  print "Countdown: " + counter;
  counter = counter - 1;
}
```

---

## ▶️ How to Run

### 1. Compile

```bash
javac javox/*.java
```

### 2. Run a `.lox` file

```bash
java javox.Javox examples/statements.lox
```

### 3. Or enter interactive mode

```bash
java javox.Javox
```

---

## 🧠 What I Learned

Building Javox gave me hands-on experience with how programming languages are built:

- How source code is **tokenized**, **parsed**, and **executed**
- How interpreters evaluate expressions and manage scope
- How to design clean error handling and extend language features
- Why compilers/interpreters are powerful tools to learn system-level thinking

---

## 📁 Project Structure

```
javox/
├── Javox.java         // Main runner (REPL + file execution)
├── Scanner.java       // Tokenizer (lexer)
├── Parser.java        // AST-based expression parser
├── Interpreter.java   // Visitor pattern evaluator
├── Token.java         // Token structure
├── TokenType.java     // All supported token types
├── Expr.java          // Expression interfaces
├── Stmt.java          // Statement interfaces
└── ... (more files)
```

---

## 📚 Inspired By

- [Crafting Interpreters](https://craftinginterpreters.com/) by Bob Nystrom  
- Java 21 — used to build everything from scratch

---

## 🤝 Feedback & Ideas Welcome

I'm actively exploring ways to improve and extend Javox.  
Let me know if you try it, break it, or want to help me grow it 🚀

---

### 🔗 License

This project is open for educational use and personal tinkering. Fork freely.
