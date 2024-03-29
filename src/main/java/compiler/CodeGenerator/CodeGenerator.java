package compiler.CodeGenerator;

import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SemanticAnalyzerException;
//import org.checkerframework.checker.units.qual.A;
import org.objectweb.asm.*;
//import org.objectweb.asm.util.*;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


import compiler.parser.ASTNodes;


public class CodeGenerator implements Opcodes{

    public String containerName = "Main";

    public ASTNodes.StatementList statementList;

    public Map<ASTNodes.Type,String> typeString;

    public Map<ASTNodes.Type,Class> typeClass;

    public Map<String, Object> constValues;

    public ArrayList<Pair<String, ClassWriter>> records;

    private ClassWriter cw;

    // Main Method visitor
    private MethodVisitor mmv;

    // Current method visitor
    private MethodVisitor context;

    // Keeps track of idx and desc of local variables
    private SymbolIndexTable sit = new SymbolIndexTable();
    byte[] bytes;

    private Pair<Label, Label> currentScope;

    public Map<String, ArrayList<ASTNodes.Param>> functionTable;

    ArrayList<Pair<String, ClassWriter>> structs = new ArrayList<>();


    public CodeGenerator(ASTNodes.StatementList statementList) throws SemanticAnalyzerException {
        //System.out.println("LETSGO");
        this.statementList = statementList;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        this.typeClass = new HashMap<>();
        this.typeString = new HashMap<>();
        this.constValues = new HashMap<>();
        this.records = new ArrayList<>();

        this.functionTable = new SemanticAnalyzer(statementList).functionTable;

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

        //if(typeString.containsKey(t)) return Type.getType(typeString.get(t));
        String desc = "";
        for(int i = 0; i < t.arrayDims; i++) desc += "[";
        ASTNodes.Type nArr = new ASTNodes.Type(t.type, false);
        if(typeString.containsKey(nArr))
            return Type.getType(desc + typeString.get(nArr));
        // Is a struct
        return Type.getType( desc + "L" + containerName + "$" + t.type + ";");

    }

    public void generateCode(String compiledFileName){
        containerName = compiledFileName;
        records.add(new Pair<>(containerName, cw));
        cw.visit(V1_8,ACC_PUBLIC,containerName,null,"java/lang/Object",null);

        // Main method visitor
        mmv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);
        Label beginLbl = new Label(); Label endLbl = new Label();
        currentScope = new Pair(beginLbl, endLbl);
        mmv.visitCode();
        mmv.visitLabel(beginLbl);
        for(ASTNodes.Statement stt : statementList.statements){
            generateStatement(stt, mmv,null);
        }

        mmv.visitLabel(endLbl);
        mmv.visitInsn(RETURN);
        mmv.visitEnd();
        mmv.visitMaxs(-1, -1);
        //mmv.visitEnd();


        cw.visitEnd();

        PrintWriter pw = new PrintWriter(System.out);
        //CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), true, pw);

        // Write the bytes as a class file
        for(Pair<String, ClassWriter> clazz : records) {
            bytes = clazz.b.toByteArray();
            try (FileOutputStream stream = new FileOutputStream(clazz.a + ".class")) {

                stream.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Object directValToVal(ASTNodes.DirectValue dv, ASTNodes.Type constType){
        if (constType != null && constType.type.equals("real") && dv.type.type.equals("int")) {
            return Float.parseFloat(dv.value);
        }
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
    public Object evaluateConstExpr(ASTNodes.Expression initExpr, ASTNodes.Type constType) {
        //System.out.println("Evaluating " + initExpr);
        if(initExpr instanceof ASTNodes.DirectValue){
            return directValToVal((ASTNodes.DirectValue) initExpr,constType);
        }
        if(initExpr instanceof ASTNodes.Identifier){
            return constValues.get(((ASTNodes.Identifier) initExpr).id);
        }
        else{
            if(initExpr instanceof ASTNodes.PrioExpr){
                return evaluateConstExpr( ((ASTNodes.PrioExpr) initExpr).expr,constType);
            }
            if(initExpr instanceof ASTNodes.AddExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.AddExpr) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.AddExpr) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) + ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) + ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() + ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) + ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.SubExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.SubExpr) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.SubExpr) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) - ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) - ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() - ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) - ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.MultExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.MultExpr) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.MultExpr) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) * ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) * ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() * ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) * ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.DivExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.DivExpr) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.DivExpr) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return ((Integer) o1) / ((Integer) o2);
                if(o1 instanceof Float && o2 instanceof Integer)   return ((Float) o1) / ((Integer) o2).floatValue();
                if(o1 instanceof Integer && o2 instanceof Float)   return ((Integer) o1).floatValue() / ((Float) o2);
                if(o1 instanceof Float && o2 instanceof Float)     return ((Float) o1) / ((Float) o2);
            }
            if(initExpr instanceof ASTNodes.ModExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.ModExpr) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.ModExpr) initExpr).expr2,constType);
                return ((Integer) o1) % ((Integer) o2);
            }
            if(initExpr instanceof ASTNodes.NegateExpr){
                Object o1 = evaluateConstExpr(((ASTNodes.NegateExpr) initExpr).expr,constType);
                if(o1 instanceof Integer) return -((Integer) o1);
                else return -((Float) o1);
            }
            if(initExpr instanceof ASTNodes.EqComp){
                Object o1 = evaluateConstExpr(((ASTNodes.EqComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.EqComp) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return (((Integer) o1) == ((Integer) o2));
                if(o1 instanceof Float && o2 instanceof Integer)   return (((Float) o1) == ((Integer) o2).floatValue());
                if(o1 instanceof Integer && o2 instanceof Float)   return (((Integer) o1).floatValue() == ((Float) o2));
                if(o1 instanceof Float && o2 instanceof Float)     return (((Float) o1) == ((Float) o2));
            }
            if(initExpr instanceof ASTNodes.NotEqComp){
                Object o1 = evaluateConstExpr(((ASTNodes.NotEqComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.NotEqComp) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return (((Integer) o1) != ((Integer) o2));
                if(o1 instanceof Float && o2 instanceof Integer)   return (((Float) o1) != ((Integer) o2).floatValue());
                if(o1 instanceof Integer && o2 instanceof Float)   return (((Integer) o1).floatValue() != ((Float) o2));
                if(o1 instanceof Float && o2 instanceof Float)     return (((Float) o1) != ((Float) o2));
            }
            if(initExpr instanceof ASTNodes.GrComp){
                Object o1 = evaluateConstExpr(((ASTNodes.GrComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.GrComp) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return (((Integer) o1) > ((Integer) o2));
                if(o1 instanceof Float && o2 instanceof Integer)   return (((Float) o1) > ((Integer) o2).floatValue());
                if(o1 instanceof Integer && o2 instanceof Float)   return (((Integer) o1).floatValue() > ((Float) o2));
                if(o1 instanceof Float && o2 instanceof Float)     return (((Float) o1) > ((Float) o2));
            }
            if(initExpr instanceof ASTNodes.SmComp){
                Object o1 = evaluateConstExpr(((ASTNodes.SmComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.SmComp) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return (((Integer) o1) < ((Integer) o2));
                if(o1 instanceof Float && o2 instanceof Integer)   return (((Float) o1) < ((Integer) o2).floatValue());
                if(o1 instanceof Integer && o2 instanceof Float)   return (((Integer) o1).floatValue() < ((Float) o2));
                if(o1 instanceof Float && o2 instanceof Float)     return (((Float) o1) < ((Float) o2));
            }
            if(initExpr instanceof ASTNodes.GrEqComp){
                Object o1 = evaluateConstExpr(((ASTNodes.GrEqComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.GrEqComp) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return (((Integer) o1) >= ((Integer) o2));
                if(o1 instanceof Float && o2 instanceof Integer)   return (((Float) o1) >= ((Integer) o2).floatValue());
                if(o1 instanceof Integer && o2 instanceof Float)   return (((Integer) o1).floatValue() >= ((Float) o2));
                if(o1 instanceof Float && o2 instanceof Float)     return (((Float) o1) >= ((Float) o2));
            }
            if(initExpr instanceof ASTNodes.SmEqComp){
                Object o1 = evaluateConstExpr(((ASTNodes.SmEqComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.SmEqComp) initExpr).expr2,constType);
                if(o1 instanceof Integer && o2 instanceof Integer) return (((Integer) o1) <= ((Integer) o2));
                if(o1 instanceof Float && o2 instanceof Integer)   return (((Float) o1) <= ((Integer) o2).floatValue());
                if(o1 instanceof Integer && o2 instanceof Float)   return (((Integer) o1).floatValue() <= ((Float) o2));
                if(o1 instanceof Float && o2 instanceof Float)     return (((Float) o1) <= ((Float) o2));
            }
            if(initExpr instanceof ASTNodes.AndComp){
                Object o1 = evaluateConstExpr(((ASTNodes.AndComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.AndComp) initExpr).expr2,constType);
                return (((Boolean) o1) && ((Boolean) o2)) ;
            }
            if(initExpr instanceof ASTNodes.OrComp){
                Object o1 = evaluateConstExpr(((ASTNodes.OrComp) initExpr).expr1,constType);
                Object o2 = evaluateConstExpr(((ASTNodes.OrComp) initExpr).expr2,constType);
                return (((Boolean) o1) || ((Boolean) o2));
            }
        }
        return null;
    }


    public void generateStatement(ASTNodes.Statement s, MethodVisitor mv, ASTNodes.Type fType){
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
            generateReturn((ASTNodes.ReturnExpr) s, mv,fType);
        } else if (s instanceof ASTNodes.FunctionCall){
            generateFuncCall((ASTNodes.FunctionCall) s, mv);
        } else if (s instanceof ASTNodes.IfCond){
            generateIfStmt((ASTNodes.IfCond) s, mv,fType);
        } else if (s instanceof ASTNodes.WhileLoop) {
            generateWhileLoop((ASTNodes.WhileLoop) s,mv,fType);
        } else if (s instanceof ASTNodes.ForLoop) {
            generateForLoop((ASTNodes.ForLoop) s,mv,fType);
        } else if (s instanceof ASTNodes.VarAssign){
            generateVarAssign((ASTNodes.VarAssign) s, mv);
        }
    }

    private void generateRefToValue(ASTNodes.RefToValue rtv, MethodVisitor mv, boolean toStore, boolean topLvl){
        //System.out.println("Generating RefToValue " + rtv);
        if(rtv instanceof ASTNodes.Identifier){
            generateIdentifier((ASTNodes.Identifier) rtv, mv, toStore && topLvl);
        } else if(rtv instanceof ASTNodes.ArrayAccess){
            ASTNodes.ArrayAccess aa = (ASTNodes.ArrayAccess) rtv;
            generateRefToValue(aa.ref, mv, toStore, false);
            generateExpression(aa.arrayIndex, mv);
            Type owner = typeToAsmType(aa.ref.exprType);
            if(!(topLvl && toStore)) {
                if(aa.ref.exprType.arrayDims > 1){
                    mv.visitInsn(AALOAD);
                } else {
                    switch (aa.ref.exprType.type) {
                        case "int":
                            mv.visitInsn(IALOAD);
                            break;
                        case "real":
                            mv.visitInsn(FALOAD);
                            break;
                        case "bool":
                            mv.visitInsn(BALOAD);
                            break;
                        default:
                            mv.visitInsn(AALOAD);
                            break;
                    }
                }
            }
        } else if(rtv instanceof ASTNodes.ObjectAccess){
            ASTNodes.ObjectAccess oa = (ASTNodes.ObjectAccess) rtv;
            generateRefToValue(oa.object, mv, toStore, false);
            Type owner = typeToAsmType(oa.object.exprType);
            Type currType = typeToAsmType(oa.exprType);
            if(!(topLvl && toStore))
                mv.visitFieldInsn(GETFIELD, owner.getInternalName(), oa.accessIdentifier, currType.getDescriptor());

        }
    }

    private void generateVarAssign(ASTNodes.VarAssign va, MethodVisitor mv){

        if(va.ref instanceof ASTNodes.ObjectAccess){
            generateRefToValue(va.ref, mv, true, true);
            ASTNodes.ObjectAccess oa = (ASTNodes.ObjectAccess) va.ref;
            Type owner = typeToAsmType(oa.object.exprType);
            Type currType = typeToAsmType(oa.exprType);
            generateExpression(va.value, mv);
            if (va.ref.exprType.type.equals("real") && va.value.exprType.type.equals("int")) {
                mv.visitInsn(I2F);
            }
            mv.visitFieldInsn(PUTFIELD, owner.getInternalName(), oa.accessIdentifier, currType.getDescriptor());
        }
        else if(va.ref instanceof ASTNodes.ArrayAccess){
            generateRefToValue(va.ref, mv, true, true);
            ASTNodes.ArrayAccess aa = (ASTNodes.ArrayAccess) va.ref;
            generateExpression(va.value, mv); // Val after index
            Type owner = typeToAsmType(aa.ref.exprType);
            // Following line doesnt work, always return AASTORE, dunno why
            // mv.visitInsn(owner.getOpcode(IASTORE));
            switch(aa.ref.exprType.type){
                case "int":
                    mv.visitInsn(IASTORE);
                    break;
                case "real":
                    if (va.value.exprType.type.equals("int") && aa.ref.exprType.type.equals("real") &&
                    !va.value.exprType.isArray){
                        mv.visitInsn(I2F);
                    }
                    mv.visitInsn(FASTORE);
                    break;
                case "bool":
                    mv.visitInsn(BASTORE);
                    break;
                default:
                    mv.visitInsn(AASTORE);
                    break;
            }
        }
        else {
            ASTNodes.Identifier idt = (ASTNodes.Identifier) va.ref;
            generateExpression(va.value, mv);
            if (idt.exprType.type.equals("real") && va.value.exprType.type.equals("int") && !va.value.exprType.isArray) {
                mv.visitInsn(I2F);
            }
            generateIdentifier(idt, mv, true);
        }
    }

    private void generateFuncCall(ASTNodes.FunctionCall s, MethodVisitor mv) {
        //System.out.println("Generating FuncCall");

        if(s.identifier.startsWith("write")){
            String funcName = "";
            String funcDesc = "";

            if(s.identifier.endsWith("eln")){ funcName = "println"; funcDesc = "(Ljava/lang/String;)V"; }
            if(s.identifier.endsWith("ite")){ funcName = "print"; funcDesc = "(Ljava/lang/String;)V"; }
            if(s.identifier.endsWith("Int")){ funcName = "println"; funcDesc = "(I)V"; }
            if(s.identifier.endsWith("Real")){ funcName = "println"; funcDesc = "(F)V"; }

            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");

            generateExpression(s.paramVals.get(0), mv);
            if (s.identifier.equals("writeReal") && s.paramVals.get(0).exprType.type.equals("int") && functionTable.get(s.identifier).get(0).type.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", funcName,
                    funcDesc, false);
            return;
        }
        if(s.identifier.startsWith("read")){
            String funcName = "";
            String funcDesc = "";

            if(s.identifier.endsWith("Int")){ funcName = "nextInt"; funcDesc = "()I"; }
            if(s.identifier.endsWith("Real")){ funcName = "nextFloat"; funcDesc = "()F"; }
            if(s.identifier.endsWith("String")){ funcName = "next"; funcDesc = "()Ljava/lang/String;"; }


            mv.visitTypeInsn(NEW, "java/util/Scanner");
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", funcName, funcDesc, false);



            return;
        }
        if (s.identifier.equals("not")) {
            generateExpression(s.paramVals.get(0),mv);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IXOR);
            return;
        }
        if (s.identifier.equals("chr")) {
            generateExpression(s.paramVals.get(0),mv);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(C)Ljava/lang/String;", false);
            return;
        }
        if (s.identifier.equals("floor")) {
            generateExpression(s.paramVals.get(0),mv);
            if (s.paramVals.get(0).exprType.type.equals("int") && functionTable.get(s.identifier).get(0).type.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            mv.visitInsn(F2I);

            return;
        }
        if (s.identifier.equals("len")) {
            generateExpression(s.paramVals.get(0),mv);
            if (s.paramVals.get(0).exprType.type.equals("string"))
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false); // Invoke the length() method
            else
                mv.visitInsn(ARRAYLENGTH);
            return;
        }
        try {
            Pair<Integer, String> p = sit.get(s.identifier);
            String idtfr = s.identifier;
            if(p.a == -2){
                // Func call is record ceration
                idtfr = containerName + "$" + idtfr;
                mv.visitTypeInsn(NEW, idtfr);
                mv.visitInsn(DUP);
            }

            for (int i = 0; i < s.paramVals.size(); i++) {
            //for(ASTNodes.Expression param : s.paramVals){
                ASTNodes.Expression param = s.paramVals.get(i);
                generateExpression(param, mv);
                if (param.exprType.type.equals("int") && functionTable.get(s.identifier).get(i).type.type.equals("real")) {
                    mv.visitInsn(I2F);
                }
            }
            Type idtType = Type.getType(p.b);

            //System.out.println("Calling function with type " + idtType.getDescriptor());
            if(p.a == -2){
                mv.visitMethodInsn(INVOKESPECIAL, idtfr, "<init>", p.b, false);
                return;
            }


            mv.visitMethodInsn(INVOKESTATIC, containerName, idtfr, p.b, false);
        } catch (SemanticAnalyzerException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateReturn(ASTNodes.ReturnExpr s, MethodVisitor mv, ASTNodes.Type fType)  {
        //System.out.println("Generating return");
        ASTNodes.Type returnType = s.expr.exprType;
        generateExpression(s.expr, mv);
        if (fType.type.equals("real") && s.expr.exprType.type.equals("int")) {
            mv.visitInsn(I2F);
            returnType = new ASTNodes.Type("real",false);
        }

        mv.visitInsn(typeToAsmType(returnType).getOpcode(IRETURN));
    }

    public void generateRecord(ASTNodes.Record record) {
        //System.out.println("Generating Record");

        ClassWriter new_cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String bn = containerName + "$" + record.identifier;
        new_cw.visit(V1_8,ACC_PUBLIC | ACC_STATIC,bn,null,"java/lang/Object",null);

        String constructorDesc = "";
        for(ASTNodes.RecordVar rv : record.recordVars){
            Type t = typeToAsmType(rv.type);
            String tDesc = t.getDescriptor();
            constructorDesc += tDesc;
            if(tDesc.contains("$")){
                // RecordVar is a struct
                new_cw.visitInnerClass(containerName + "$" + rv.type.type, containerName, rv.type.type, ACC_PUBLIC | ACC_STATIC);
            }
            new_cw.visitField(ACC_PUBLIC, rv.identifier, t.getDescriptor(), null, null );
        }
        constructorDesc = "(" + constructorDesc + ")V";
        try {
            sit.add(record.identifier, -2, constructorDesc);
        } catch (SemanticAnalyzerException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("Constructor description: " + constructorDesc);
        MethodVisitor init = new_cw.visitMethod(ACC_PUBLIC, "<init>", constructorDesc, null, null);
        init.visitCode();
        init.visitVarInsn(ALOAD, 0); // this
        init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        int i = 1;
        for (ASTNodes.RecordVar rv: record.recordVars) {
            init.visitVarInsn(ALOAD, 0);
            Type t = typeToAsmType(rv.type);
            init.visitVarInsn(t.getOpcode(ILOAD), i);
            i += t.getSize();
            init.visitFieldInsn(PUTFIELD, bn, rv.identifier, typeToAsmType(rv.type).getDescriptor());

        }
        init.visitInsn(RETURN);

        init.visitEnd();
        init.visitMaxs(-1,-1);

        new_cw.visitEnd();
        new_cw.visitNestHost(containerName);
        new_cw.visitInnerClass(bn, containerName, record.identifier, ACC_PUBLIC | ACC_STATIC);
        cw.visitInnerClass(bn,containerName, record.identifier, ACC_PUBLIC | ACC_STATIC);
        cw.visitNestMember(bn);
        records.add(new Pair<>(bn, new_cw));

    }


    public void generateFunctionDef(ASTNodes.FunctionDef f) {
        //System.out.println("Generating FunctionDef");
        String params = "(";
        for (ASTNodes.Param p : f.paramList) {
            params += typeToAsmType(p.type).getDescriptor();
        }
        params += ")";
        params += typeToAsmType(f.returnType).getDescriptor();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC, f.identifier,params,null,null);

        // give the right names to the parameters
        for (int i = 0; i < f.paramList.size(); i++) {
            mv.visitParameter(f.paramList.get(i).identifier, i);
        }

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
            generateStatement(s, mv,f.returnType);
        }
        if(f.returnType.type.equals("void")){
            mv.visitInsn(RETURN);
        }
        mv.visitEnd();
        //int nxtId = sit.nxtAvailableIndex();
        mv.visitMaxs(-1, -1);

        sit = sit.prevTable;
    }


    public  void generateVar(ASTNodes.VarCreation creation, MethodVisitor mv){
        //System.out.println("Generating Var creation");
        /*if (creation.type.type.equals("real") && creation.varExpr.exprType.type.equals("int") && !creation.type.isArray) {
            creation.varExpr.exprType = new ASTNodes.Type("real",false);
        }*/
        Type varType = typeToAsmType(creation.type);
        String desc = varType.getDescriptor();

        if(sit.prevTable == null){ // Is top level context

            // Currently in global code, use fields not local var
            cw.visitField(ACC_PUBLIC | ACC_STATIC, creation.identifier, desc,null,null).visitEnd();
            generateExpression(creation.varExpr, mv);
            if (creation.varExpr != null) {
                if (creation.type.type.equals("real") && creation.varExpr.exprType.type.equals("int") &&
                !creation.varExpr.exprType.isArray) {
                    mv.visitInsn(I2F);
                }
                mv.visitFieldInsn(PUTSTATIC, containerName, creation.identifier, desc);
            }

            try {
                sit.add(creation.identifier, -1, desc);
            } catch (SemanticAnalyzerException e) {
                throw new RuntimeException(e);
            }

        } else {
            generateExpression(creation.varExpr, mv);
            Integer idx = null;
            try {
                idx = sit.add(creation.identifier, desc);
            } catch (SemanticAnalyzerException e) {
                throw new RuntimeException(e);
            }
            if (creation.varExpr != null) {
                if (creation.type.type.equals("real") && creation.varExpr.exprType.type.equals("int") && !creation.type.isArray) {
                    mv.visitInsn(I2F);
                }

                mv.visitVarInsn(varType.getOpcode(ISTORE), idx);
            }
        }

    }

    public void generateVal(ASTNodes.ValCreation creation, MethodVisitor mv){
        //System.out.println("Generating val creation");
        // No difference between val and var after semantic analysis, just re-use code
        ASTNodes.VarCreation equiv = new ASTNodes.VarCreation();
        equiv.varExpr = creation.valExpr;
        equiv.type = creation.type;
        equiv.identifier = creation.identifier;
        generateVar(equiv, mv);
    }

    public void generateConst(ASTNodes.ConstCreation creation) {
        //System.out.println("Generating const");

        Object val = evaluateConstExpr(creation.initExpr,creation.type);
        constValues.put(creation.identifier, val );
        cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, creation.identifier,
                typeString.get(creation.type), null, val ).visitEnd();



    }

    public void generateIfStmt(ASTNodes.IfCond stt, MethodVisitor mv, ASTNodes.Type fType){
        //System.out.println("Generating IfStmt");
        Label elseLabel = new Label();
        Label endLabel = new Label();
        boolean hasElse = stt.elseCodeBlock != null;
        generateExpression(stt.condition, mv);
        mv.visitJumpInsn(IFEQ, hasElse ? elseLabel : endLabel);
        sit = new SymbolIndexTable(sit);
        stt.codeBlock.statements.forEach(s->generateStatement(s, mv,fType));
        sit = sit.prevTable;
        if (hasElse) {
            mv.visitJumpInsn(GOTO, endLabel);
            mv.visitLabel(elseLabel);
            sit = new SymbolIndexTable(sit);
            stt.elseCodeBlock.statements.forEach(s->generateStatement(s, mv,fType));
            sit = sit.prevTable;
        }
        mv.visitLabel(endLabel);
    }

    public void generateWhileLoop(ASTNodes.WhileLoop stt, MethodVisitor mv, ASTNodes.Type fType){
        //System.out.println("Generating while loop");
        Label startLabel = new Label();
        Label endLabel = new Label();

        mv.visitLabel(startLabel);
        generateExpression(stt.condition,mv);
        mv.visitJumpInsn(IFEQ, endLabel);

        sit = new SymbolIndexTable(sit);
        stt.codeBlock.statements.forEach(s->generateStatement(s, mv,fType));
        sit = sit.prevTable;

        mv.visitJumpInsn(GOTO,startLabel);
        mv.visitLabel(endLabel);
    }

    public void generateForLoop(ASTNodes.ForLoop stt, MethodVisitor mv, ASTNodes.Type fType)   {
        //System.out.println("Generating for loop");
        Label startLabel = new Label();
        Label endLabel = new Label();

        Pair<Integer, String> idx;
        try {
            idx = sit.get(((ASTNodes.Identifier)stt.loopVal).id);

        } catch (SemanticAnalyzerException e) {
            throw new RuntimeException(e);
        }

        ASTNodes.VarAssign assign = new ASTNodes.VarAssign();
        assign.ref = stt.loopVal;
        assign.value = stt.initValExpr;
        generateVarAssign(assign,mv);

        mv.visitLabel(startLabel);

        if (idx.a != -1) {
            mv.visitVarInsn(ILOAD, idx.a);
        } else {
            mv.visitFieldInsn(GETSTATIC,containerName, ((ASTNodes.Identifier)stt.loopVal).id, "I");
        }
        generateExpression(stt.endValExpr, mv);
        mv.visitJumpInsn(IF_ICMPGE,endLabel);

        // loop body
        sit = new SymbolIndexTable(sit);
        stt.codeBlock.statements.forEach(s->generateStatement(s, mv,fType));
        sit = sit.prevTable;

        // increment
        if (idx.a != -1) {
            mv.visitVarInsn(ILOAD, idx.a);
            generateExpression(stt.increment, mv);
            mv.visitInsn(IADD);
            mv.visitVarInsn(ISTORE, idx.a);
        }
        else {
            mv.visitFieldInsn(GETSTATIC,containerName,((ASTNodes.Identifier)stt.loopVal).id,"I");
            generateExpression(stt.increment,mv);
            mv.visitInsn(IADD);
            mv.visitFieldInsn(PUTSTATIC,containerName, ((ASTNodes.Identifier)stt.loopVal).id, "I");
        }


        mv.visitJumpInsn(GOTO,startLabel);
        mv.visitLabel(endLabel);

    }

    public void generateExpression(ASTNodes.Expression e, MethodVisitor mv)  {

        //System.out.println("Generating Expression " + e);
        if (e == null) return;
        if (e instanceof ASTNodes.DirectValue) {
            ASTNodes.DirectValue val = (ASTNodes.DirectValue) e;
            mv.visitLdcInsn(directValToVal(val,null));
        } else if (e instanceof ASTNodes.RefToValue) {
            ASTNodes.RefToValue rtv = ((ASTNodes.RefToValue) e);
            generateRefToValue(rtv, mv, false, true); // Cannot store from expression
        } else if (e instanceof ASTNodes.MathExpr) {
            generateMathExpr((ASTNodes.MathExpr) e, mv);
        } else if (e instanceof ASTNodes.Comparison){
            generateComparison((ASTNodes.Comparison) e, mv);
        } else if (e instanceof ASTNodes.ArrayCreation){
            generateArrayCreation((ASTNodes.ArrayCreation) e, mv);
        } else if (e instanceof ASTNodes.ObjectCreation){
            // No need
        } else if (e instanceof ASTNodes.FunctionCall){
            generateFuncCall((ASTNodes.FunctionCall) e, mv);
        } else {
            System.out.println("--- Unexpected expression type " + e);
        }
        return;
    }


    private void generateArrayCreation(ASTNodes.ArrayCreation e, MethodVisitor mv) {
        //System.out.println("Generating array creation");
        for(ASTNodes.Expression asExp : e.arraySizes) {
            generateExpression(asExp, mv);
        }
        if(e.type.arrayDims == 1) {
            if (e.type.type.equals("int")) {
                mv.visitIntInsn(NEWARRAY, T_INT);
                return;
            } else if (e.type.type.equals("real")) {
                mv.visitIntInsn(NEWARRAY, T_FLOAT);
                return;
            } else if (e.type.type.equals("bool")) {
                // Maybe problem with comparison since there bool are ints
                mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
                return;
            } else {
                // Is a object, String or other

                mv.visitTypeInsn(ANEWARRAY, typeToAsmType(new ASTNodes.Type(e.type.type, false)).getInternalName());
            }
        } else {
            //System.out.println("Creating N-Dim-Array");
            mv.visitMultiANewArrayInsn(typeToAsmType(e.type).getDescriptor(), e.type.arrayDims);
        }
    }

    public void generateIdentifier(ASTNodes.Identifier idt, MethodVisitor mv, boolean toStore){
        //System.out.println("Generating Identifier (toStore: " + toStore + ")");
        try {
            if(!sit.contain(idt.id)){
                //System.out.println("Identifier " + idt.id + " not in SymbolIndexTable");
                if(constValues.containsKey(idt.id)){
                    assert(!toStore);
                    mv.visitLdcInsn(constValues.get(idt.id));
                    //return;
                } else {
                    Pair<Integer, String> pair = sit.get(idt.id);
                    int lid = pair.a; String desc = pair.b;
                    mv.visitFieldInsn(GETSTATIC, containerName, idt.id, desc);
                }
                return;
            }
            Pair<Integer, String> pair = sit.get(idt.id);
            int lid = pair.a; String desc = pair.b;
            if(lid == -1){ // Not local var but field
                mv.visitFieldInsn(toStore ? PUTSTATIC: GETSTATIC, containerName, idt.id, desc);
            }
            else {
                mv.visitVarInsn(Type.getType(desc).getOpcode(toStore ? ISTORE : ILOAD), lid);
            }
        } catch (SemanticAnalyzerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void generateMathExpr(ASTNodes.MathExpr expr, MethodVisitor mv) {
        if (expr instanceof ASTNodes.AddExpr) {
            ASTNodes.AddExpr addExpr = (ASTNodes.AddExpr) expr;
            generateExpression(addExpr.expr1, mv);
            if (addExpr.expr1.exprType.type.equals("int") && addExpr.expr2.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            generateExpression(addExpr.expr2, mv);
            if (addExpr.expr2.exprType.type.equals("int") && addExpr.expr1.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            int oc;
            if (addExpr.expr1.exprType.type.equals("int"))
                oc = typeToAsmType(addExpr.expr2.exprType).getOpcode(IADD);
            else
                oc = typeToAsmType(addExpr.expr1.exprType).getOpcode(IADD);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.SubExpr) {
            ASTNodes.SubExpr subExpr = (ASTNodes.SubExpr) expr;
            generateExpression(subExpr.expr1, mv);
            if (subExpr.expr1.exprType.type.equals("int") && subExpr.expr2.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            generateExpression(subExpr.expr2, mv);
            if (subExpr.expr2.exprType.type.equals("int") && subExpr.expr1.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            int oc;
            if (subExpr.expr1.exprType.type.equals("int"))
                oc = typeToAsmType(subExpr.expr2.exprType).getOpcode(ISUB);
            else
                oc = typeToAsmType(subExpr.expr1.exprType).getOpcode(ISUB);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.MultExpr) {
            ASTNodes.MultExpr multExpr = (ASTNodes.MultExpr) expr;
            generateExpression(multExpr.expr1, mv);
            if (multExpr.expr1.exprType.type.equals("int") && multExpr.expr2.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            generateExpression(multExpr.expr2, mv);
            if (multExpr.expr2.exprType.type.equals("int") && multExpr.expr1.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            int oc;
            if (multExpr.expr1.exprType.type.equals("int"))
                oc = typeToAsmType(multExpr.expr2.exprType).getOpcode(IMUL);
            else
                oc = typeToAsmType(multExpr.expr1.exprType).getOpcode(IMUL);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.DivExpr) {
            ASTNodes.DivExpr divExpr = (ASTNodes.DivExpr) expr;
            generateExpression(divExpr.expr1, mv);
            if (divExpr.expr1.exprType.type.equals("int") && divExpr.expr2.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            generateExpression(divExpr.expr2, mv);
            if (divExpr.expr2.exprType.type.equals("int") && divExpr.expr1.exprType.type.equals("real")) {
                mv.visitInsn(I2F);
            }
            int oc;
            if (divExpr.expr1.exprType.type.equals("int"))
                oc = typeToAsmType(divExpr.expr2.exprType).getOpcode(IDIV);
            else
                oc = typeToAsmType(divExpr.expr1.exprType).getOpcode(IDIV);
            mv.visitInsn(oc);
        }
        else if (expr instanceof ASTNodes.ModExpr) {
            ASTNodes.ModExpr modExpr = (ASTNodes.ModExpr) expr;
            generateExpression(modExpr.expr1, mv);
            generateExpression(modExpr.expr2, mv);
            mv.visitInsn(IREM);
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
        //System.out.println("Generating comparison");
        generateExpression(comp.expr1, mv);
        if (comp.expr1.exprType.type.equals("int") && comp.expr2.exprType.type.equals("real")) {
            comp.expr1.exprType = new ASTNodes.Type("real",false);
            mv.visitInsn(I2F);
        }
        generateExpression(comp.expr2, mv);
        if (comp.expr2.exprType.type.equals("int") && comp.expr1.exprType.type.equals("real")) {
            comp.expr2.exprType = new ASTNodes.Type("real",false);
            mv.visitInsn(I2F);
        }

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
