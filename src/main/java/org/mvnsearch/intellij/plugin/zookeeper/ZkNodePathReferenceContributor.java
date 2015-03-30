package org.mvnsearch.intellij.plugin.zookeeper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.patterns.PsiMethodPattern;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.mvnsearch.intellij.plugin.zookeeper.vfs.ZkVirtualFileSystem;

import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.patterns.PsiJavaPatterns.literalExpression;
import static com.intellij.patterns.PsiJavaPatterns.psiExpression;
import static com.intellij.patterns.PsiJavaPatterns.psiMethod;
import static com.intellij.patterns.StandardPatterns.string;

/**
 * zookeeper node path reference contributor
 *
 * @author linux_china
 */
public class ZkNodePathReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        final StringPattern methodName = string().oneOf("forPath");
        final PsiMethodPattern method = psiMethod().withName(methodName);
        final PsiJavaElementPattern.Capture<PsiLiteralExpression> javaFile
                = literalExpression().and(psiExpression().methodCallParameter(0, method));
        registrar.registerReferenceProvider(javaFile, new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull ProcessingContext context) {
                return new FileReferenceSet(element) {
                    @Override
                    protected Collection<PsiFileSystemItem> getExtraContexts() {
                        Project project = element.getProject();
                        final ArrayList<PsiFileSystemItem> result = new ArrayList<PsiFileSystemItem>();
                        ZkVirtualFileSystem fileSystem = ZkProjectComponent.getInstance(project).getFileSystem();
                        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                        String innerText = ((PsiLiteralExpressionImpl) literalExpression).getInnerText();
                        if (innerText == null) {
                            innerText = "";
                        }
                        innerText = innerText.replace("IntellijIdeaRulezzz", "");
                        if (innerText.isEmpty()) {
                            innerText = "/";
                        }
                        final PsiManager psiManager = element.getManager();
                        VirtualFile rootDirectory = fileSystem.findFileByPath(innerText);
                        if (rootDirectory != null) {
                            if (rootDirectory.isDirectory()) {
                                result.add(psiManager.findDirectory(rootDirectory));
                            } else {
                                result.add(psiManager.findFile(rootDirectory));
                            }
                        }
                        return result;
                    }
                }.getAllReferences();
            }
        });
    }
}
