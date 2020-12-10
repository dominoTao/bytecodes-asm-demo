package core.bytecode;


import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.*;

/**
 * ASM 操作字节码
 */
public class Demo {
    /**
     * 生成的字节码文件
     */
    public static final String NEW_FILE_PATH = "D:\\Demo2.class";
    /**
     * 引用的字节码文件
     */
    public static final String OLD_FILE_PATH = "D:\\Constant.class";
    public static void main1(String[] args) throws IOException {
        byte[] bytes = file2ByteArray(OLD_FILE_PATH);
        ClassReader cr = new ClassReader(bytes);
        // 不会自动计算栈和局部变量表的大小
//        ClassWriter cw = new ClassWriter(0);
        // ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES 会自动计算栈和表的大小  但是需要Maxs触发
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM7, cw){
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                System.out.println("field : " + name);
                // 删除字段
                if ("abc".equals(name)) {
                    return null;
                }
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                System.out.println("method : " + name);
                // 删除方法
                if ("run".equals(name)) {
                    return null;
                }

                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"foo".equals(name)) {
                    return methodVisitor;
                }
                Label startLable = new Label();
                return new AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {
                    @Override
                    protected void onMethodEnter() {// 进入方法
                        super.onMethodEnter();
                        // 打印正常信息
//                        methodVisitor.visitFieldInsn(GETSTATIC,"java/lang/System", "out", "Ljava/io/PrintStream;");
//                        methodVisitor.visitLdcInsn("enter"+name);
//                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

                        methodVisitor.visitLabel(startLable);
                        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                        methodVisitor.visitLdcInsn("enter" + name);
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintlStream", "println", "(Ljava/lang/String;)V", false);

                    }

                    @Override
                    protected void onMethodExit(int opcode) {// 退出方法
                        super.onMethodExit(opcode);
                        // 打印错误信息
//                        methodVisitor.visitFieldInsn(GETSTATIC,"java/lang/System", "out", "Ljava/io/PrintStream;");
//                        if (opcode == Opcodes.ATHROW) { // 异常退出
//                            methodVisitor.visitLdcInsn("err exit "+name);
//                        }else {
//                            methodVisitor.visitLdcInsn("normal exit " + name);
//                        }
//                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

                        //处理正常返回场景
                        if (opcode != ATHROW) {
                            finallyBlock(opcode);
                        }
                    }

                    @Override
                    public void visitMaxs(int maxStack, int maxLocals) {
                        // 生成异常表
                        Label endLable = new Label();
                        methodVisitor.visitTryCatchBlock(startLable, endLable, endLable, null);
                        methodVisitor.visitLabel(endLable);
                        // 生成异常处理代码块
                        finallyBlock(ATHROW);
                        methodVisitor.visitInsn(ATHROW);
                        super.visitMaxs(maxStack, maxLocals);
                    }

                    private void finallyBlock(int opcode){
                        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                        if (opcode == Opcodes.ATHROW) {
                            methodVisitor.visitLdcInsn("err exit " + name);
                        }else {
                            methodVisitor.visitLdcInsn("normal exit " + name);
                        }
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    }

                };
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
//                MethodVisitor myfunction = cv.visitMethod(Opcodes.ACC_PUBLIC, "myfunction", "(ILjava/lang/String;)V", null, null);
//                if (myfunction != null) {
//                    myfunction.visitEnd();
//                }
//                FieldVisitor myfield = cv.visitField(Opcodes.ACC_PUBLIC, "myfield", "Ljava/lang/String;", null, null);
//                if (myfield != null) {
//                    myfield.visitEnd();
//                }
                /**
                 * 修改方法 可以先将原来的方法删除，再新增一个方法（如果方法体简单）
                 */
                MethodVisitor run = cv.visitMethod(Opcodes.ACC_PRIVATE, "run", "(I)I", null, null);
                run.visitCode();
                run.visitVarInsn(Opcodes.ILOAD, 1);
                run.visitIntInsn(Opcodes.BIPUSH, 100);
                run.visitInsn(Opcodes.IADD);
                run.visitInsn(Opcodes.IRETURN);

                // 手动指定：：增加操作数栈大小和局部变量大小
//                run.visitMaxs(2, 2);
                // 自动指定：：触发ClassWriter中的两个参数
                run.visitMaxs(0, 0);
                run.visitEnd();


            }
        };
        cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        byteArray2File(cw.toByteArray(), NEW_FILE_PATH);

    }
    public static byte[] file2ByteArray(String filepath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filepath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = fileInputStream.read(bytes)) != -1) {
            byteArrayOutputStream.write(bytes, 0, len);
        }
        byte[] bytes1 = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        fileInputStream.close();
        return bytes1;
    }

    public static void byteArray2File(byte[] bytes, String filepath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(filepath));
        fileOutputStream.write(bytes);
        fileOutputStream.flush();
        fileOutputStream.close();

    }
}
