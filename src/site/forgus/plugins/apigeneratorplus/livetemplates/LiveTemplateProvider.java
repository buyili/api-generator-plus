package site.forgus.plugins.apigeneratorplus.livetemplates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.Nullable;

/**
 * @author lmx 2021/9/11 11:10
 **/

public class LiveTemplateProvider implements DefaultLiveTemplatesProvider {
    @Override
    public String[] getDefaultLiveTemplateFiles() {
        // "liveTemplates/Markdown",
        return new String[]{"liveTemplates/Java"};
    }

    @Nullable
    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return new String[0];
    }
}
