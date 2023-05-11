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


    public ASTNodes.StatementList statementList;

    public Map<ASTNodes.Type,String> typeString;

    public Map<ASTNodes.Type,Class> typeClass;

    public Map<String, Object> constValues;

    private ClassWriter cw;

    byte[] bytes;


    ArrayList<Pair<String, ClassWriter>> structs = new ArrayList<>();


    public CodeGenerator(ASTNodes.StatementList statementList) {
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

        cw.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,"Main",null,"java/lang/Object",null);

        for(ASTNodes.Statement stt : statementList.statements){
            if(stt instanceof ASTNodes.ConstCreation){
                Object val = evaluateConstExpr(((ASTNodes.ConstCreation) stt).initExpr);
                System.out.println("Val is " + val);
                constValues.put(((ASTNodes.ConstCreation) stt).identifier, val );
                cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, ((ASTNodes.ConstCreation) stt).identifier,
                        typeString.get(((ASTNodes.ConstCreation) stt).type), null, val );
            }
        }

        /*MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC ,"main","([Ljava/lang/String;)V",null,null);
        mv.visitCode();

        mv.visitFieldInsn(Opcodes.GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
        mv.visitLdcInsn("hello");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream","println","(Ljava/lang/String;)V",false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();
        mv.visitMaxs(-1,-1);*/
        // TODO plein de trucs

        /*
        for (ASTNodes.Statement s : statementList.statements) {
            if (s instanceof ASTNodes.ConstCreation || s instanceof ASTNodes.ValCreation || s instanceof ASTNodes.VarCreation ||
                s instanceof ASTNodes.FunctionDef || s instanceof ASTNodes.Record || s instanceof ASTNodes.VarAssign) {
                generateStatement(s,null);
            }
        }*/

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,"main","([Ljava/lang/String;)V",null,null);

        mv.visitCode();

        mv.visitIntInsn(BIPUSH, 10);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitIntInsn(BIPUSH, 20);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitInsn(IMUL);
        mv.visitVarInsn(ISTORE, 3);
        mv.visitInsn(RETURN);

        mv.visitMaxs(3, 3);

        for (ASTNodes.Statement s : statementList.statements) {
            generateStatement(s,mv);
        }

        cw.visitEnd();



        // Write the bytes as a class file
        bytes = cw.toByteArray();  //concatWithArrayCopy(bytes,cw.toByteArray()); // cw.toByteArray();
        try (FileOutputStream stream = new FileOutputStream("Main.class")) {

            stream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*bytes = cw.toByteArray();
        ByteArrayClassLoader loader = new ByteArrayClassLoader();

        Class<?> test = loader.defineClass("MainClass",bytes);

        try {
            test.getMethod("main",String[].class).invoke(null,(Object) new String[0]);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }*/

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

    public void generateStatement(ASTNodes.Statement s,MethodVisitor mv) {
        if (s instanceof ASTNodes.ConstCreation) {
            generateConst((ASTNodes.ConstCreation) s);
        }
        else if (s instanceof ASTNodes.ValCreation) {
            generateVal((ASTNodes.ValCreation) s);
        } else if (s instanceof ASTNodes.VarCreation) {
            generateVar((ASTNodes.VarCreation) s);
        } else if (s instanceof ASTNodes.FunctionDef) {
            generateFunctionDef((ASTNodes.FunctionDef) s);
        } else if (s instanceof ASTNodes.Record) {
            generateRecord((ASTNodes.Record) s);
        }
    }

    public void generateRecord(ASTNodes.Record record) {

        // TODO later : I cant find a way to show in the "GeneratedClass" file the inner class

        ClassWriter new_cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        new_cw.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,record.identifier,null,"java/lang/Object",null);

    }


    public void generateFunctionDef(ASTNodes.FunctionDef f) {
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
        /*
        for (ASTNodes.Statement s : f.functionCode.statements) {
            generateStatement(s);
        }*/

    }

    public  void generateVar(ASTNodes.VarCreation creation) {
        Object initval = null;
        if (creation.varExpr instanceof ASTNodes.DirectValue)
            initval = generateExpression(creation.varExpr);

        String type = typeString.get(creation.type);
        Class<?> c = typeClass.get(creation.type);

        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,(c)initval).visitEnd();


        /*
        if (creation.type.type.equals("int")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "I",null,(int)initval).visitEnd();
        }
        if (creation.type.type.equals("real")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "F",null,(float)initval).visitEnd();
        }
        if (creation.type.type.equals("bool")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "Z",null,(boolean)initval).visitEnd();
        }
        if (creation.type.type.equals("string")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "Ljava/lang/String;",null,String.valueOf(initval)).visitEnd();
        } else {
            // TODO : creation is a record
        }*/
    }

    public void generateVal(ASTNodes.ValCreation creation) {
        Object initval = generateExpression(creation.valExpr);

        String type = typeString.get(creation.type);
        Class<?> c = typeClass.get(creation.type);

        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,(c)initval).visitEnd();



        /*
        if (creation.type.type.equals("int")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "I",null,(int)initval).visitEnd();
        }
        if (creation.type.type.equals("real")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "F",null,(float)initval).visitEnd();
        }
        if (creation.type.type.equals("bool")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "Z",null,(boolean)initval).visitEnd();
        }
        if (creation.type.type.equals("string")) {
            String type = "Ljava/lang/String;";
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,String.valueOf(initval)).visitEnd();
        } else {
            // TODO : creation is a record
        }*/
    }

    public void generateConst(ASTNodes.ConstCreation creation) {

        Object initval = generateExpression((ASTNodes.Expression) creation.initExpr);

        String type = typeString.get(creation.type);
        Class<?> c = typeClass.get(creation.type);

        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,(c)initval).visitEnd();


        /*

        if (creation.type.type.equals("int")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "I",null,(int)initval).visitEnd();
        }
        if (creation.type.type.equals("real")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "F",null,(float)initval).visitEnd();
        }
        if (creation.type.type.equals("bool")) {
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, "Z",null,(boolean)initval).visitEnd();
        }
        if (creation.type.type.equals("string")) {
            String type = "Ljava/lang/String;";
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, creation.identifier, type,null,String.valueOf(initval)).visitEnd();
        }*/


    }

    public Object generateExpression(ASTNodes.Expression e) {
        if (e == null) return null;

        if (e instanceof ASTNodes.DirectValue) {
            ASTNodes.DirectValue val = (ASTNodes.DirectValue) e;

            if (val.type.type.equals("int")) {
                return Integer.parseInt(val.value);
            } else if (val.type.type.equals("string")) {
                return val.value;
            } else if (val.type.type.equals("real")) {
                return Float.parseFloat(val.value);
            } else {
                return Boolean.parseBoolean(val.value);
            }
        }
        // TODO
        else if (e instanceof ASTNodes.MathExpr) {
            return generateMathExpr((ASTNodes.MathExpr) e);
        }
        else {
            return null;
        }
    }

    public Object generateMathExpr(ASTNodes.MathExpr expr) {
        if (expr instanceof ASTNodes.AddExpr) {
            ASTNodes.AddExpr addExpr = (ASTNodes.AddExpr) expr;


        }


        return null;
    }

    public static void main(String[] args) {
    }
}
