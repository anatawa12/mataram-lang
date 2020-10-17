/* JFlex example: partial Java language lexer specification */
package com.anatawa12.mataram.parser;

import com.anatawa12.mataram.ast.Token;
import java.util.ArrayList;
import java.util.List;
import static com.anatawa12.mataram.parser.Tokens.*;

/**
 * This class is a simple example lexer.
 */
%%

%class JFlexLexer
%type LexicalToken
%unicode
%char

%{

  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  JFlexLexer(String in) {
    reset(in, 0, in.length(), YYINITIAL);
  }

  private int yychar;
  private LexicalTokensBuilder builder = new LexicalTokensBuilder();

  StringBuffer string = new StringBuffer();

  private LexicalToken token(TokenType type) {
    return token(type, yytext());
  }

  private LexicalToken token(TokenType type, CharSequence str) {
    return builder.build(type, yychar, str);
  }

  List<Integer> states = new ArrayList<>();

  private void pushState(int state) {
    states.add(yystate());
    yybegin(state);
  }

  private void popState() {
    yybegin(states.remove(states.size() - 1));
  }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {OneLineComment} | {MultiLineComment}

// Comment can be the last line of the file, without line terminator.
OneLineComment   = "//" [^\r\n]*
MultiLineComment = "/*" {CommentContent} "*"+ "/"
CommentContent   = ( [^*] | \*+ [^/*] )*

Identifier = [:letter:] ([:letter:]|[:digit:])*

HexDiget = [0-9a-fA-F]

EscapeSequence = \\[tnr\"`\\] | \\u{HexDiget}{4}
StringLiteralElement = [^\n\r\"\\]+ | {EscapeSequence}
StringLiteral = \" {StringLiteralElement}* \"

DecIntegerLiteral = [0-9]+ | 0x{HexDiget}+
IntegerLiteral = {DecIntegerLiteral}
NumberLiteral = [0-9]+ \. [0-9]+

%state INSIDE_QUOTE
%state INSIDE_QUOTE_AFTER_DOLLAR
%state INSIDE_QUOTE_AFTER_BRACE

%%

/* keywords */
<YYINITIAL> {
  /* identifiers */
  {Identifier}                   { return token(ID); }

  /* literals */
  {IntegerLiteral}               { return token(INTEGER_LITERAL); }
  {NumberLiteral}                { return token(NUMBER_LITERAL); }
  {StringLiteral}                { return token(STRING_LITERAL); }
  `                              { pushState(INSIDE_QUOTE); return token(QUOTE); }

  /* operators */
  "$"                            { return token(DOLLAR); }
  "@"                            { return token(AT_MARK); }
  ":"                            { return token(COLON); }
  ";"                            { return token(SEMI); }
  "("                            { return token(L_PARENTHESIS); }
  ")"                            { return token(R_PARENTHESIS); }
  "["                            { return token(L_BRACKET); }
  "]"                            { return token(R_BRACKET); }
  "{"                            { return token(L_BRACE); }
  "}"                            { return token(R_BRACE); }
  "=="                           { return token(EQUALS); }
  "!="                           { return token(NOT_EQUALS); }
  "<="                           { return token(LESS_OR_EQUALS); }
  ">="                           { return token(GRATER_OR_EQUALS); }
  "<"                            { return token(LESS_THAN); }
  ">"                            { return token(GRATER_THAN); }
  "="                            { return token(ASSIGN); }
  "."                            { return token(DOT); }
  "..."                          { return token(THREE_DOT); }
  "+"                            { return token(PLUS); }
  "-"                            { return token(MINUS); }
  "*"                            { return token(STAR); }
  "/"                            { return token(SLASH); }
  "%"                            { return token(MODULO); }
  "||"                           { return token(BOOL_OR); }
  "&&"                           { return token(BOOL_AND); }
  "++"                           { return token(INCREMENT); }
  "--"                           { return token(DECREMENT); }

  /* comments */
  {Comment}                      { return token(COMMENT); }

  /* whitespace */
  {WhiteSpace}+                  { return token(WHITE_SPACE); }
}

<INSIDE_QUOTE> {
  ([^$`\\]|{EscapeSequence})+    { return token(QUOTED_ELEMENT); }
  \$                             { yybegin(INSIDE_QUOTE_AFTER_DOLLAR); return token(DOLLAR); }
  `                              { popState();                         return token(QUOTE); }
}

<INSIDE_QUOTE_AFTER_DOLLAR> {
  \{                             { return token(L_BRACE); }
  {Identifier}                   { yybegin(INSIDE_QUOTE); return token(ID); }
  [^]                            { yybegin(INSIDE_QUOTE); yypushback(1); }
}

<INSIDE_QUOTE_AFTER_BRACE> {
  "..."                          { return token(THREE_DOT); }
  {Identifier}                   { return token(ID); }
  `                              { pushState(INSIDE_QUOTE); return token(QUOTE); }
  \}                             { yybegin(INSIDE_QUOTE);   return token(R_BRACE); }
}

/* error fallback */
[^]                              { throw new Error("Illegal character <"+
                                                    yytext()+">"); }
