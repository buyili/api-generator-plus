package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.exception.BizException;

/**
 * reference:
 * 使用 Fetch: https://developer.mozilla.org/zh-CN/docs/Web/API/Fetch_API/Using_Fetch
 * WorkerOrGlobalScope.fetch(): https://developer.mozilla.org/zh-CN/docs/Web/API/WindowOrWorkerGlobalScope/fetch
 */
public class CopyAsFetchAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        try {
            CurlUtils curlUtils = new CurlUtils();
            curlUtils.copyAsFetch(actionEvent);
        } catch (BizException exception) {
            exception.printStackTrace();
        }
    }

}
