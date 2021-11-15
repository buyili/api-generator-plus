package site.forgus.plugins.apigeneratorplus.action;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import com.intellij.openapi.diagnostic.Logger;
import site.forgus.plugins.apigeneratorplus.util.NotificationUtil;

public class TestNotificationAction extends AnAction {

    private Logger LOG = Logger.getInstance(TestNotificationAction.class);

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        // TODO: insert action logic here
        Project project = actionEvent.getProject();

        // 自定义链接状态改变时的逻辑，如：点击链接后，hyperlinkUpdate 方法会被调用
        // 也可以实现 NotificationListener.Adapter ，这个类默认处理点击事件
        //NotificationListener notificationListener = new NotificationListener() {
        //    @Override
        //    public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
        //        log.info("execute hyperlinkUpdate");
        //    }
        //};

        //NotificationGroup notificationGroup = new NotificationGroup("ApiGeneratorPlus.NotificationGroup",
        //        NotificationDisplayType.BALLOON, true, null, SdkIcons.Logo);

        NotificationGroup notificationGroup = NotificationUtil.notificationGroup;

        NotificationListener.Adapter notificationListener = new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                // e.getDescription() 的值就是标签 a 中的 href 属性值
                String message = "你点击了我" + e.getDescription() + "🎉🎉";
                LOG.info(message);
                System.out.println(message);
            }
        };
        String title = "我来组成标题👶";
        String subtitle = "我来组成副标题👨‍🎓";
        String content = "我来组成身体👨‍🦯。<a href=\"我的头\">来点我的头</a>.\n" +
                "<a href=\"来点我的左手\">我的左手</a> <a href=\"我的右手\">来点我的右手</a> <a href=\"我的肚腩\">来点我的肚腩</a>";
        Notification notification = notificationGroup.createNotification(title, subtitle,
                content, NotificationType.INFORMATION, notificationListener);
        Notifications.Bus.notify(notification, project);
    }
}
