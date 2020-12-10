package core.bytecode.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
// 只处理类全限定名为 core.bytecode.processor.Data 的注解
@SupportedAnnotationTypes("core.bytecode.processor.Data")
// 最高支持java8编译出的类文件
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DataAnnoProcessor extends AbstractProcessor {
    // 语法树元素的基类
    private JavacTrees javacTrees;
    // 封装了创建语法树节点的方法
    private TreeMaker treeMaker;
    // 提供了访问标识符的方法
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        javacTrees = JavacTrees.instance(processingEnv);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

    // process 方法用来修改抽象语法树AST
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Data.class);
        for (Element element : set) {
            JCTree tree = javacTrees.getTree(element);
            tree.accept(new TreeTranslator(){
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    jcClassDecl.defs.stream()
                            .filter(t -> t.getKind().equals(Tree.Kind.VARIABLE))
                            .map(t -> (JCTree.JCVariableDecl)t)
                            .forEach(t -> {
                                jcClassDecl.defs = jcClassDecl.defs.prepend(genGetterMethod(t));
                                jcClassDecl.defs = jcClassDecl.defs.prepend(genGetterMethod(t));
                            });
                    super.visitClassDef(jcClassDecl);
                }
            });
        }
        return true;
    }
    private Name getMethodName(Name name) {
        String fieldName = name.toString();
        return names.fromString("get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,name.length()));
    }
    private Name setMethodName(Name name){
        String fieldName = name.toString();
        return names.fromString("set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,name.length()));

    }
    private JCTree.JCMethodDecl genGetterMethod(JCTree.JCVariableDecl jcVariableDecl) {
        JCTree.JCReturn jcReturn = treeMaker.Return(
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())
        );
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<JCTree.JCStatement>().append(jcReturn);

        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        Name getMethodName = getMethodName(jcVariableDecl.getName());// 方法名
        JCTree.JCExpression returnMethodType = jcVariableDecl.vartype; // 返回值类型
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());// 方法体
        List<JCTree.JCTypeParameter> methodGenericParamList = List.nil();// 泛型参数列表
        List<JCTree.JCVariableDecl> parameterList = List.nil();// 参数列表
        List<JCTree.JCExpression> thrownCauseList = List.nil();// 异常抛出列表
        return treeMaker.MethodDef(modifiers, getMethodName, returnMethodType, methodGenericParamList, parameterList, thrownCauseList, body, null);
    }

    private JCTree.JCMethodDecl genSetterMethod(JCTree.JCVariableDecl jcVariableDecl) {
        JCTree.JCExpressionStatement statement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString("this")),
                                jcVariableDecl.getName()            // lhs
                        ),
                        treeMaker.Ident(jcVariableDecl.getName()) // rhs
                )
        );

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<JCTree.JCStatement>().append(statement);
        // set 方法参数
        JCTree.JCVariableDecl param = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER, List.nil()),   //访问修饰符
                jcVariableDecl.name,                                //变量名
                jcVariableDecl.vartype,                             //变量类型
                null                                    //变量初始值
        );
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        Name setMethodName = setMethodName(jcVariableDecl.getName());
        JCTree.JCExpression returnMethodType = treeMaker.Type(new Type.JCVoidType());
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        List<JCTree.JCTypeParameter> methodGenericParamList = List.nil();
        List<JCTree.JCVariableDecl> parameterList = List.nil();
        List<JCTree.JCExpression> thrownCauseList = List.nil();
        return treeMaker.MethodDef(modifiers, setMethodName, returnMethodType, methodGenericParamList, parameterList, thrownCauseList, body, null);
    }
}
