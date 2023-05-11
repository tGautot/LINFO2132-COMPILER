package compiler.CodeGenerator;

import org.checkerframework.checker.units.qual.A;
import org.objectweb.asm.*;

import java.io.FileOutputStream;
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

    byte[] bytes;


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

        mmv.visitCode();
        context = mmv;
        for(ASTNodes.Statement stt : statementList.statements){
            generateStatement(stt, mmv);
        }

        mmv.visitMaxs(1, 1);
        //mmv.visitEnd();


        cw.visitEnd();



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
            params += typeString.get(p.type);
        }
        params += ")";
        params += typeString.get(f.returnType);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, f.identifier,params,null,null);

        // give the right names to the parameters
        for (int i = 0; i < f.paramList.size(); i++) {
            mv.visitParameter(f.paramList.get(i).identifier, i);
        }
        mv.visitCode();

        // TODO : look at each statement of the function code and call the right generation function

        for (ASTNodes.Statement s : f.functionCode.statements) {
            generateStatement(s, mv);
        }
        mv.visitEnd();

    }

    public  void generateVar(ASTNodes.VarCreation creation, MethodVisitor mv){
        /*Object initval = null;
        if (creation.varExpr instanceof ASTNodes.DirectValue)
            initval = generateExpression(creation.varExpr, mv);

        String type = typeString.get(creation.type);
        Class<?> c = typeClass.get(creation.type);

        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,(c)initval).visitEnd();
*/
    }

    public void generateVal(ASTNodes.ValCreation creation, MethodVisitor mv){
        /*Object initval = generateExpression(creation.valExpr, mv);

        String type = typeString.get(creation.type);
        Class<?> c = typeClass.get(creation.type);

        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,(c)initval).visitEnd();*/
    }

    public void generateConst(ASTNodes.ConstCreation creation) {
        System.out.println("Generating const");

        Object val = evaluateConstExpr(creation.initExpr);
        constValues.put(creation.identifier, val );
        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, creation.identifier,
                typeString.get(creation.type), null, val ).visitEnd();



    }

    public void genereateIfStmt(ASTNodes.IfCond stt, MethodVisitor mv){
        System.out.println("Generating IfStmt");
        Label elseLabel = new Label();
        Label endLabel = new Label();
        boolean hasElse = stt.elseCodeBlock != null;
        // TODO generate condition
        mv.visitJumpInsn(IFEQ, hasElse ? elseLabel : endLabel);
        stt.codeBlock.statements.forEach(s->generateStatement(s, mv));
        if (hasElse) {
            mv.visitJumpInsn(GOTO, endLabel);
            mv.visitLabel(elseLabel);
            stt.elseCodeBlock.statements.forEach(s->generateStatement(s, mv));
        }
        mv.visitLabel(endLabel);
    }

    public void generateExpression(ASTNodes.Expression e, MethodVisitor mv)  {
        System.out.println("Generating Expression");
        if (e == null) return;
        if (e instanceof ASTNodes.DirectValue) {
            ASTNodes.DirectValue val = (ASTNodes.DirectValue) e;
            mv.visitLdcInsn(directValToVal(val));

        } else if (e instanceof ASTNodes.Comparison) {
            return;
        } else if (e instanceof ASTNodes.MathExpr) {
            generateMathExpr((ASTNodes.MathExpr) e, mv);
        }
        return;
    }

    public void generateMathExpr(ASTNodes.MathExpr expr, MethodVisitor mv) {
        if (expr instanceof ASTNodes.AddExpr) {
            ASTNodes.AddExpr addExpr = (ASTNodes.AddExpr) expr;
            generateExpression(addExpr.expr1, mv);
            generateExpression(addExpr.expr2, mv);
            if(addExpr.expr1.exprType.type.equals("int")){
                mv.visitInsn(Opcodes.IADD);
            } else {
                mv.visitInsn(Opcodes.FADD);
            }
        }
        else if (expr instanceof ASTNodes.SubExpr) {
            ASTNodes.SubExpr subExpr = (ASTNodes.SubExpr) expr;
            generateExpression(subExpr.expr1, mv);
            generateExpression(subExpr.expr2, mv);
            if(subExpr.expr1.exprType.type.equals("int")){
                mv.visitInsn(Opcodes.ISUB);
            } else {
                mv.visitInsn(Opcodes.FSUB);
            }
        }
        else if (expr instanceof ASTNodes.MultExpr) {
            ASTNodes.MultExpr multExpr = (ASTNodes.MultExpr) expr;
            generateExpression(multExpr.expr1, mv);
            generateExpression(multExpr.expr2, mv);
            if(multExpr.expr1.exprType.type.equals("int")){
                mv.visitInsn(Opcodes.IMUL);
            } else {
                mv.visitInsn(Opcodes.FMUL);
            }
        }
        else if (expr instanceof ASTNodes.DivExpr) {
            ASTNodes.DivExpr divExpr = (ASTNodes.DivExpr) expr;
            generateExpression(divExpr.expr1, mv);
            generateExpression(divExpr.expr2, mv);
            if(divExpr.expr1.exprType.type.equals("int")){
                mv.visitInsn(Opcodes.IDIV);
            } else {
                mv.visitInsn(Opcodes.FDIV);
            }
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
            if(negExpr.expr.exprType.type.equals("int")){
                mv.visitInsn(Opcodes.INEG);
            } else {
                mv.visitInsn(Opcodes.DNEG);
            }
        }
        else {
            System.out.println("!!! Unknown math expression " + expr);
        }

    }

    public static void main(String[] args) {
    }
}
