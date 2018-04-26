package sourcecode.ast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import utils.Splitter;

public class FileParser
{
    private CompilationUnit cu = null;

    public static void main(String[] args) {}

    public FileParser(File file)
    {
        ASTCreator creator = new ASTCreator();
        creator.getFileContent(file);
        this.cu = creator.getCompilationUnit();
    }

    public int getLinesOfCode()
    {
        deleteNoNeededNode();
        String[] lines = this.cu.toString().split("\n");
        int len = 0;
        String[] arrayOfString1;
        int j = (arrayOfString1 = lines).length;
        for (int i = 0; i < j; i++)
        {
            String strLine = arrayOfString1[i];
            if (!strLine.trim().equals("")) {
                len++;
            }
        }
        return len;
    }

    public String[] getContent()
    {
        String[] tokensInSourceCode = Splitter.splitSourceCode(
                deleteNoNeededNode());
        StringBuffer sourceCodeContentBuffer = new StringBuffer();
        String[] arrayOfString1;
        int j = (arrayOfString1 = tokensInSourceCode).length;
        for (int i = 0; i < j; i++)
        {
            String token = arrayOfString1[i];
            sourceCodeContentBuffer.append(token + " ");
        }
        String content = sourceCodeContentBuffer.toString().toLowerCase();
        return content.split(" ");
    }

    public String[] getClassNameAndMethodName()
    {
        String content = (getAllClassName() + " " +
                getAllMethodName()).toLowerCase();
        return content.split(" ");
    }

    public String getPackageName()
    {
        return this.cu.getPackage() == null ? "" :
                this.cu.getPackage().getName().getFullyQualifiedName();
    }

    private String getAllMethodName()
    {
        ArrayList<String> methodNameList = new ArrayList();
        MethodDeclaration[] methodDecls;
        for (int i = 0; i < this.cu.types().size(); i++)
        {
            TypeDeclaration type = (TypeDeclaration)this.cu.types().get(i);
            methodDecls = type.getMethods();
            MethodDeclaration[] arrayOfMethodDeclaration1;
            int j = (arrayOfMethodDeclaration1 = methodDecls).length;
            for (int i = 0; i < j; i++)
            {
                MethodDeclaration methodDecl = arrayOfMethodDeclaration1[i];
                String methodName = methodDecl.getName()
                        .getFullyQualifiedName();
                methodNameList.add(methodName);
            }
        }
        String allMethodName = "";
        for (String methodName : methodNameList) {
            allMethodName = allMethodName + methodName + " ";
        }
        return allMethodName.trim();
    }

    private String getAllClassName()
    {
        ArrayList<String> classNameList = new ArrayList();
        String name;
        for (int i = 0; i < this.cu.types().size(); i++)
        {
            TypeDeclaration type = (TypeDeclaration)this.cu.types().get(i);
            name = type.getName().getFullyQualifiedName();
            classNameList.add(name);
        }
        String allClassName = "";
        for (String className : classNameList) {
            allClassName = allClassName + className + " ";
        }
        return allClassName.trim();
    }

    private String deleteNoNeededNode()
    {
        this.cu.accept(new ASTVisitor()
        {
            public boolean visit(AnnotationTypeDeclaration node)
            {
                if (node.isPackageMemberTypeDeclaration()) {
                    node.delete();
                }
                return super.visit(node);
            }
        });
        this.cu.accept(new ASTVisitor()
        {
            public boolean visit(PackageDeclaration node)
            {
                node.delete();
                return super.visit(node);
            }
        });
        this.cu.accept(new ASTVisitor()
        {
            public boolean visit(ImportDeclaration node)
            {
                node.delete();
                return super.visit(node);
            }
        });
        return this.cu.toString();
    }
}
