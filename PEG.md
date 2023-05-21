# PEG とは

- [Qiita - Parsing Expression Grammar(PEG)の使い方](https://qiita.com/SenK/items/8655e7eb2dcb0649832b)
- [Wikipedia - Parsing Expression Grammar](https://ja.wikipedia.org/wiki/Parsing_Expression_Grammar)

# Toys の PEG

```
// Program は TopLevel 定義が0個以上？
program <- topLevelDefinition*;

// lines は line が一つ以上？
lines <- line+;

// 「TopLevel」の定義
topLevelDefinition <-
    globalVariableDefinition / functionDefinition;  // これ or かな？

// 関数定義の文法
functionDefinition <-
    "define" identifier
    "(" (identifier ("," identifier)*)? ")" // 多分、関数引数の定義だと思う
    blockExpression;

// グローバル変数定義の文法
globalVarialbeDefinition <-
    "global" identifier "=" expression; // global という予約語の後定義か
    
// 「行(line)」の定義
line <- println / whileExpression / ifExpression 
                / assignment / expressionLine 
                / blockExpression

// println 式
println <- "println" "(" expressiopn ")";

// if 式
ifExpression <- "if" "(" expression ")" line ("else" line )?;   // これだけで足りてるのだろうか。いまいち確信がない。 ? は Optionalか

// while 式
whileExpression <- "while" "(" expression ")" line;

// block 式
blockExpression <- "{" line "}";

// assignment 式
assignment <- identifier "=" expression ";";

// 「行」式
expressionLine <- expression ";";

// 「評価式」定義
expression <- comparative;

// 比較式
comparative <- additive (
    ("<" / ">" / "<=" / ">="/ "==" / "!=") additive
)*;

// 加減式定義
additive <- multitive(
    ("+" / "-") multitive   // 加減算より掛け算が優先されるからかな
)*;

// 乗除式定義
multitive <- primary (
    ("*" / "/") primary
)*;

// 優先度定義か？
primary <- "(" expression ")"
            / integer
            / functionCall
            / labbeledCall  // 未定義
            / arrayLiteral  // 未定義
            / boolLiteral   // 未定義
            / identifier;

// 関数呼び出し定義
funcationCall <- identifier "("
        ( expression ("," expression)*)?
")";

// なんていえばいいんだこれ
identifier <- IDENT;
 ```