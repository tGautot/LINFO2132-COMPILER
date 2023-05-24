#! /bin/bash

# this script allows you to compile and execute a piece of code of the new language
# run this script with as :
# * first argument the name of the source code file (it should be in the "compiler" folder) you would like to compile and execute
# * second argument the name of the compiled code
# here is an example : bash script.sh simple_code_copie.txt MAIN

javac -cp ".:./compiler/asm.jar" -sourcepath . compiler/Compiler.java compiler/CodeGenerator/*.java compiler/Lexer/*.java compiler/parser/*.java compiler/SemanticAnalyzer/*.java compiler/Logger/*.java

java -cp ".:./compiler/asm.jar" compiler.Compiler "compiler/$1" $2

java $2

