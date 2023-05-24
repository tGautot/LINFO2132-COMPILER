#! /bin/bash

javac -cp ".:./compiler/asm.jar" -sourcepath . compiler/Compiler.java compiler/CodeGenerator/*.java compiler/Lexer/*.java compiler/parser/*.java compiler/SemanticAnalyzer/*.java compiler/Logger/*.java

java -cp ".:./compiler/asm.jar" compiler.Compiler "compiler/$1" $2

java $2

