package site.forgus.plugins.apigeneratorplus.livetemplates;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author lmx 2021/9/11 11:15
 **/

public class MarkdownContext extends TemplateContextType {
    protected MarkdownContext() {
        super("MARKDOWN", "Markdown");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile psiFile, int i) {
        return psiFile.getName().endsWith(".md");
    }
}
