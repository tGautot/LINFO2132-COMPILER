package compiler.CodeGenerator;

import compiler.SemanticAnalyzer.SemanticAnalyzerException;
import org.checkerframework.checker.units.qual.A;
import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;




import compiler.parser.ASTNodes;


public class CodeGenerator<c> implements Opcodes{

    public String containerName = "Main";

    public ASTNodes.StatementList statementList;

    public Map<ASTNodes.Type,String> typeString;

    public Map<ASTNodes.Type,Class> typeClass;

    public Map<String, Object> constValues;

    private ClassWriter cw;

    // Main Method visitor
    private MethodVisitor mmv;

    // Current method visitor
    private MethodVisitor context;

    // Keeps track of idx and desc of local variables
    private SymbolIndexTable sit = new SymbolIndexTable();
    byte[] bytes;

    private Pair<Label, Label> currentScope;


    ArrayList<Pair<String, ClassWriter>> structs = new ArrayList<>();


    public CodeGenerator(ASTNodes.StatementList statementList) {
        System.out.println("LETSGO");
        this.statementList = statementList;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        this.typeClass = new HashMap<>();
        this.typeString = new HashMap<>();
        this.constValues = new HashMap<>();

        typeClass.put(new ASTNodes.Type("int",false), int.class);
        typeClass.put(new ASTNodes.Type("int",true), int[].class);
        typeClass.put(new ASTNodes.Type("real",false), float.class);
        typeClass.put(new ASTNodes.Type("real",true), float[].class);
        typeClass.put(new ASTNodes.Type("bool",false), boolean.class);
        typeClass.put(new ASTNodes.Type("bool",true), boolean[].class);
        typeClass.put(new ASTNodes.Type("string",false), String.class);
        typeClass.put(new ASTNodes.Type("string",true), String[].class);
        typeClass.put(new ASTNodes.Type("void",false), Void.class);

        typeString.put(new ASTNodes.Type("int",false),"I");
        typeString.put(new ASTNodes.Type("int",true),"[I");
        typeString.put(new ASTNodes.Type("real",false),"F");
        typeString.put(new ASTNodes.Type("real",true),"[F");
        typeString.put(new ASTNodes.Type("bool",false),"Z");
        typeString.put(new ASTNodes.Type("bool",true),"[Z");
        typeString.put(new ASTNodes.Type("string",false),"Ljava/lang/String;");
        typeString.put(new ASTNodes.Type("string",true),"[Ljava/lang/String;");
        typeString.put(new ASTNodes.Type("void",false), "V");

    }

    public org.objectweb.asm.Type typeToAsmType(ASTNodes.Type t){

        if(typeString.containsKey(t)) return Type.getType(typeString.get(t));
        // Is a struct
        return Type.getType("L" + containerName + "/" + t.type);

    }

    public void generateCode(){
        cw.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,containerName,null,"java/lang/Object",null);

        // Main method visitor
        mmv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
        Label beginLbl = new Label(); Label endLbl = new Label();
        currentScope = new Pair(beginLbl, endLbl);
        mmv.visitCode();
        mmv.visitLabel(beginLbl);
        for(ASTNodes.Statement stt : statementList.statements){
            generateStatement(stt, mmv);
        }
        mmv.visitLabel(endLbl);
        mmv.visitInsn(RETURN);
        mmv.visitEnd();
        mmv.visitMaxs(-1, -1);
        //mmv.visitEnd();


        cw.visitEnd();

        PrintWriter pw = new PrintWriter(System.out);
        CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), true, pw);

        // Write the bytes as a class file
        bytes = cw.toByteArray();  //concatWithArrayCopy(bytes,cw.toByteArray()); // cw.toByteArray();
        try (FileOutputStream stream = new FileOutputStream("Main.class")) {

            stream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Object directValToVal(ASTNodes.DirectValue dv){
        if (dv.type.type.equals("int")) {
            return Integer.parseInt(dv.value);
        }
        else if (dv.type.type.equals("real")) {
            return Float.parseFloat(dv.value);
        }
        else if (dv.type.type.equals("bool")) {
            return Boolean.parseBoolean(dv.value);
        }
        else {
            return dv.value;
        }
    }
    public Object evaluateConstExpr(ASTNodes.Expression initExpr) {
        System.out.println("Evaluating " + initExpr);
        if(initExpr instanceof ASTNodes.DirectValue){
            return directValToVal((ASTNodes.DirectValue) initExpr);
        }
        if(initExpr instanceof ASTNodes.Identifier){
            return constValues.get(((ASTNodes.Identifier) initExpr).id);
        }
        else{
            if(initExpr instanceof ASTNodes.PrioExpr){
                return evaluateConstExpr( ((ASTNodes.PrioExpr) initExpr).expr);
            }
            if(initExpr instanceof ASTNodes.AddExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.AddExpr) initExpr).expr1);
                Object o2 = evaluateConstExpr(((ASTNodes.AddExpr) initExpr).expr2);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) + ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) + ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() + ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) + ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.SubExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.SubExpr) initExpr).expr1);
                Object o2 = evaluateConstExpr(((ASTNodes.SubExpr) initExpr).expr2);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) - ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) - ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() - ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) - ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.MultExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.MultExpr) initExpr).expr1);
                Object o2 = evaluateConstExpr(((ASTNodes.MultExpr) initExpr).expr2);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) * ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) * ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() * ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) * ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.DivExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.DivExpr) initExpr).expr1);
                Object o2 = evaluateConstExpr(((ASTNodes.DivExpr) initExpr).expr2);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) / ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) / ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() / ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) / ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.ModExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.ModExpr) initExpr).expr1);
                Object o2 = evaluateConstExpr(((ASTNodes.ModExpr) initExpr).expr2);
                return ((Integer) o1) % ((Integer) o2);
            }
            if(initExpr instanceof ASTNodes.NegateExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.NegateExpr) initExpr).expr);
                if(o1 instanceof Integer) return -((Integer) o1);
                else return -((Float) o1);
            }
        }
        // TODO evaluate comparison;
        return null;
    }

    static  byte[] concatWithArrayCopy(byte[] array1, byte[] array2) {
        byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public void generateStatement(ASTNodes.Statement s,MethodVisitor mv){
        if (s instanceof ASTNodes.ConstCreation) {
            generateConst((ASTNodes.ConstCreation) s);
        } else if (s instanceof ASTNodes.ValCreation) {
            generateVal((ASTNodes.ValCreation) s, mv);
        } else if (s instanceof ASTNodes.VarCreation) {
            generateVar((ASTNodes.VarCreation) s, mv);
        } else if (s instanceof ASTNodes.FunctionDef) {
            generateFunctionDef((ASTNodes.FunctionDef) s);
        } else if (s instanceof ASTNodes.Record) {
            generateRecord((ASTNodes.Record) s);
        } else if (s instanceof ASTNodes.ReturnExpr) {
            generateReturn((ASTNodes.ReturnExpr) s, mv);
        } else if (s instanceof ASTNodes.FunctionCall){
            generateFuncCall((ASTNodes.FunctionCall) s, mv);
        } else if (s instanceof ASTNodes.IfCond){
            generateIfStmt((ASTNodes.IfCond) s, mv);
        }
    }

    private void generateFuncCall(ASTNodes.FunctionCall s, MethodVisitor mv) {
        System.out.println("Generating FuncCall");

        if(s.identifier.startsWith("write")){
            String funcName = "";
            String funcDesc = "";

            if(s.identifier.endsWith( "ln")){ funcName = "println"; funcDesc = "(Ljava/lang/String;)V"; }
            if(s.identifier.endsWith("ite")){ funcName = "print"; funcDesc = "(Ljava/lang/String;)V"; }
            if(s.identifier.endsWith("Int")){ funcName = "println"; funcDesc = "(I)V"; }
            if(s.identifier.endsWith("Real")){ funcName = "println"; funcDesc = "(F)V"; }

            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");
            for(ASTNodes.Expression p : s.paramVals){
                generateExpression(p, mv);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", funcName,
                    funcDesc, false);
            return;
        }

        for(ASTNodes.Expression p : s.paramVals){
            generateExpression(p, mv);
        }
        try {
            mv.visitMethodInsn(INVOKESTATIC, containerName, s.identifier, sit.get(s.identifier).b, false);
        } catch (SemanticAnalyzerException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateReturn(ASTNodes.ReturnExpr s, MethodVisitor mv)  {
        System.out.println("Generating return");
        generateExpression(s.expr, mv);
        ASTNodes.Type returnType = s.expr.exprType;
        mv.visitInsn(typeToAsmType(returnType).getOpcode(IRETURN));
    }

    public void generateRecord(ASTNodes.Record record) {
        System.out.println("Generating Record");

        // TODO later : I cant find a way to show in the "GeneratedClass" file the inner class

        ClassWriter new_cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        new_cw.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,record.identifier,null,"java/lang/Object",null);

    }


    public void generateFunctionDef(ASTNodes.FunctionDef f) {
        System.out.println("Generating FunctionDef");
        String params = "(";
        for (ASTNodes.Param p : f.paramList) {
            params += typeToAsmType(p.type).getDescriptor();
        }
        params += ")";
        params += typeToAsmType(f.returnType).getDescriptor();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, f.identifier,params,null,null);
        try {
            sit.add(f.identifier, -1, params);
        } catch (SemanticAnalyzerException e) {
            throw new RuntimeException(e);
        }
        sit = new SymbolIndexTable(sit);
        // give the right names to the parameters
        for (int i = 0; i < f.paramList.size(); i++) {
            //mv.visitParameter(f.paramList.get(i).identifier, 0);
            try {
                ASTNodes.Param p = f.paramList.get(i);
                sit.add(p.identifier, i, typeToAsmType(p.type).getDescriptor()); // no +1 because 0 is not "this" because method is static
            } catch (SemanticAnalyzerException e) {
                throw new RuntimeException(e);
            }
        }

        mv.visitCode();
        for (ASTNodes.Statement s : f.functionCode.statements) {
            generateStatement(s, mv);
        }
        mv.visitEnd();
        //int nxtId = sit.nxtAvailableIndex();
        mv.visitMaxs(-1, -1);

        sit = sit.prevTable;
    }

    public  void generateVar(ASTNodes.VarCreation creation, MethodVisitor mv){
        System.out.println("Generating Var creation");
        Type varType = typeToAsmType(creation.type);
        String desc = varType.getDescriptor();

        if(mv == mmv){
            // Currently in global code, use fields not local var
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, desc,null,null).visitEnd();
            generateExpression(creation.varExpr, mv);
            mv.visitFieldInsn(PUTSTATIC, containerName, creation.identifier, desc);


        } else {
            generateExpression(creation.varExpr, mv);
            Integer idx = null;
            try {
                idx = sit.add(creation.identifier, desc);
            } catch (SemanticAnalyzerException e) {
                throw new RuntimeException(e);
            }
            mv.visitVarInsn(varType.getOpcode(ISTORE), idx);
        }

    }

    public void generateVal(ASTNodes.ValCreation creation, MethodVisitor mv){
        System.out.println("Generating val creation");
        // No difference between val and var after semantic analysis, just re-use code
        ASTNodes.VarCreation equiv = new ASTNodes.VarCreation();
        equiv.varExpr = creation.valExpr;
        equiv.type = creation.type;
        equiv.identifier = equiv.identifier;
        generateVar(equiv, mv);
    }

    public void generateConst(ASTNodes.ConstCreation creation) {
        System.out.println("Generating const");

        Object val = evaluateConstExpr(creation.initExpr);
        constValues.put(creation.identifier, val );
        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, creation.identifier,
                typeString.get(creation.type), null, val ).visitEnd();



    }

    public void generateIfStmt(ASTNodes.IfCond stt, MethodVisitor mv){
        System.out.println("Generating IfStmt");
        Label elseLabel = new Label();
        Label endLabel = new Label();
        boolean hasElse = stt.elseCodeBlock != null;
        generateExpression(stt.condition, mv);
        mv.visitJumpInsn(IFEQ, hasElse ? elseLabel : endLabel);
        sit = new SymbolIndexTable(sit);
        stt.codeBlock.statements.forEach(s->generateStatement(s, mv));
        sit = sit.prevTable;
        if (hasElse) {
            mv.visitJumpInsn(GOTO, endLabel);
            mv.visitLabel(elseLabel);
            sit = new SymbolIndexTable(sit);
            stt.elseCodeBlock.statements.forEach(s->generateStatement(s, mv));
            sit = sit.prevTable;
        }
        mv.visitLabel(endLabel);
    }

    public void generateExpression(ASTNodes.Expression e, MethodVisitor mv)  {

        /**
         * TODO Expr
         * [x] Direct Value
         * [x] Identifier
         * [ ] ArrayAcces
         * [ ] ObjectAccess
         * [x] MathExpr
         * [x] Comparison
         * [ ] ArrayCreation
         * [x] ObjectCreation -> as function call
         */

        System.out.println("Generating Expression " + e);
        if (e == null) return;
        if (e instanceof ASTNodes.DirectValue) {
            ASTNodes.DirectValue val = (ASTNodes.DirectValue) e;
            mv.visitLdcInsn(directValToVal(val));

        } else if (e instanceof ASTNodes.Identifier) {
            ASTNodes.Identifier idt = ((ASTNodes.Identifier) e);
            generateIdentifier(idt, mv);
        } else if (e instanceof ASTNodes.ArrayAccess) {
            // TODO
        } else if (e instanceof ASTNodes.ObjectAccess) {
            // TODO
        } else if (e instanceof ASTNodes.MathExpr) {
            generateMathExpr((ASTNodes.MathExpr) e, mv);
        } else if (e instanceof ASTNodes.Comparison){
            generateComparison((ASTNodes.Comparison) e, mv);
        } else if (e instanceof ASTNodes.ArrayCreation){
            // TODO
        } else if (e instanceof ASTNodes.ObjectCreation){
            // TODO
        } else if (e instanceof ASTNodes.FunctionCall){
            generateFuncCall((ASTNodes.FunctionCall) e, mv);
        } else {
            System.out.println("--- Unexpected expression type " + e);
        }



        return;
    }

    public void generateIdentifier(ASTNodes.Identifier idt, MethodVisitor mv){
        try {
            if(!sit.contain(idt.id)){
                if(constValues.containsKey(idt.id)){
                    mv.visitLdcInsn(constValues.get(idt.id));
                }
                return;
            }
            Pair<Integer, String> pair = sit.get(idt.id);
            int lid = pair.a; String desc = pair.b;
            if(lid == -1){ // Not local var but field
                mv.visitFieldInsn(GETFIELD, containerName, idt.id, desc);
            }
            else {
                mv.visitVarInsn(Type.getType(desc).getOpcode(ILOAD), lid);
            }
        } catch (SemanticAnalyzerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void generateMathExpr(ASTNodes.MathExpr expr, MethodVisitor mv) {
        if (expr instanceof ASTNodes.AddExpr) {
            ASTNodes.AddExpr addExpr = (ASTNodes.AddExpr) expr;
            generateExpression(addExpr.expr1, mv);
            generateExpression(addExpr.expr2, mv);
            int oc = typeToAsmType(addExpr.expr1.exprType).getOpcode(IADD);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.SubExpr) {
            ASTNodes.SubExpr subExpr = (ASTNodes.SubExpr) expr;
            generateExpression(subExpr.expr1, mv);
            generateExpression(subExpr.expr2, mv);
            int oc = typeToAsmType(subExpr.expr1.exprType).getOpcode(ISUB);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.MultExpr) {
            ASTNodes.MultExpr multExpr = (ASTNodes.MultExpr) expr;
            generateExpression(multExpr.expr1, mv);
            generateExpression(multExpr.expr2, mv);
            int oc = typeToAsmType(multExpr.expr1.exprType).getOpcode(IMUL);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.DivExpr) {
            ASTNodes.DivExpr divExpr = (ASTNodes.DivExpr) expr;
            generateExpression(divExpr.expr1, mv);
            generateExpression(divExpr.expr2, mv);
            int oc = typeToAsmType(divExpr.expr1.exprType).getOpcode(IDIV);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.ModExpr) {
            ASTNodes.ModExpr modExpr = (ASTNodes.ModExpr) expr;
            generateExpression(modExpr.expr1, mv);
            generateExpression(modExpr.expr2, mv);
            mv.visitInsn(Opcodes.IREM);
        }
        else if (expr instanceof ASTNodes.NegateExpr) {
            ASTNodes.NegateExpr negExpr = (ASTNodes.NegateExpr) expr;
            generateExpression(negExpr.expr, mv);
            int oc = typeToAsmType(negExpr.expr.exprType).getOpcode(INEG);
            mv.visitInsn(oc);
        }
        else {
            System.out.println("!!! Unknown math expression " + expr);
        }

    }

    public void generateComparison(ASTNodes.Comparison comp, MethodVisitor mv){
        System.out.println("Generating comparison");
        generateExpression(comp.expr1, mv);
        generateExpression(comp.expr2, mv);

        if (comp instanceof ASTNodes.AndComp){
            // Should put 1 if bot are 1
            mv.visitInsn(IAND);
            return;
        }
        if (comp instanceof ASTNodes.OrComp){
            mv.visitInsn(IOR);
            return;
        }
        boolean isInt = false;
        if(comp.expr1.exprType.type.equals("int") || comp.expr1.exprType.type.equals("bool")){
            isInt = true;
        } else if (comp.expr1.exprType.type.equals("real")){
            mv.visitInsn(FCMPG);
        }
        Label trueLbl = new Label();
        Label falselbl = new Label();
        Integer opcode = null;
        if (! Arrays.asList(new String[]{"int", "real", "bool"}).contains(comp.expr1.exprType.type)){
            if(comp instanceof ASTNodes.EqComp){
                opcode =  IF_ACMPEQ;
            } else if(comp instanceof ASTNodes.NotEqComp){
                opcode = IF_ACMPNE;
            } else {
                System.out.println("--- Invalid comparison type between objects of type " + comp.expr1.exprType.type);
            }
        } else {
            if (comp instanceof ASTNodes.EqComp) {
                opcode = isInt ? IF_ICMPEQ : IFEQ;
            } else if (comp instanceof ASTNodes.GrEqComp) {
                opcode = isInt ? IF_ICMPGE : IFGE;
            } else if (comp instanceof ASTNodes.GrComp) {
                opcode = isInt ? IF_ICMPGT : IFGT;
            } else if (comp instanceof ASTNodes.SmEqComp) {
                opcode = isInt ? IF_ICMPLE : IFLE;
            } else if (comp instanceof ASTNodes.SmComp) {
                opcode = isInt ? IF_ICMPLT : IFLT;
            } else if (comp instanceof ASTNodes.NotEqComp) {
                opcode = isInt ? IF_ICMPNE : IFNE;
            } else {
                System.out.println("--- Unexpected comparison " + comp);
            }
        }
        mv.visitJumpInsn(opcode, trueLbl);
        mv.visitInsn(ICONST_0); // Load FALSE
        mv.visitJumpInsn(GOTO, falselbl);
        mv.visitLabel(trueLbl);
        mv.visitInsn(ICONST_1); // Load TRUE
        mv.visitLabel(falselbl);
    }

    public static void main(String[] args) {
    }
}
