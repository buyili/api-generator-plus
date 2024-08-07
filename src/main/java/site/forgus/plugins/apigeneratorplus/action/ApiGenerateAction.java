package site.forgus.plugins.apigeneratorplus.action;

import com.google.common.base.Strings;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import site.forgus.plugins.apigeneratorplus.serverurl.YApiDefineServerUrlDialog;
import site.forgus.plugins.apigeneratorplus.util.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.psi.*;
import site.forgus.plugins.apigeneratorplus.config.ApiGeneratorConfig;
import site.forgus.plugins.apigeneratorplus.config.ChooseYApiProjectDialog;
import site.forgus.plugins.apigeneratorplus.config.YApiProjectConfigInfo;
import site.forgus.plugins.apigeneratorplus.constant.TypeEnum;
import site.forgus.plugins.apigeneratorplus.constant.WebAnnotation;
import site.forgus.plugins.apigeneratorplus.curl.Assert;
import site.forgus.plugins.apigeneratorplus.curl.CurlUtils;
import site.forgus.plugins.apigeneratorplus.exception.BizException;
import site.forgus.plugins.apigeneratorplus.http.MediaType;
import site.forgus.plugins.apigeneratorplus.icons.SdkIcons;
import site.forgus.plugins.apigeneratorplus.normal.FieldInfo;
import site.forgus.plugins.apigeneratorplus.normal.MethodInfo;
import site.forgus.plugins.apigeneratorplus.store.GlobalVariable;
import site.forgus.plugins.apigeneratorplus.util.*;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestBodyTypeEnum;
import site.forgus.plugins.apigeneratorplus.yapi.enums.RequestMethodEnum;
import site.forgus.plugins.apigeneratorplus.yapi.enums.YApiInterfaceStatusEnum;
import site.forgus.plugins.apigeneratorplus.yapi.model.*;
import site.forgus.plugins.apigeneratorplus.yapi.sdk.YApiSdk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

public class ApiGenerateAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(ApiGenerateAction.class);

    protected ApiGeneratorConfig config;

    private static final String SLASH = "/";

    private Editor editor;
    private Project project;
    private YApiProjectConfigInfo yApiProjectConfigInfo;

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        try {
            editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
            if (editor == null) {
                return;
            }
            PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            if (psiFile == null) {
                return;
            }
            project = editor.getProject();
            if (project == null) {
                return;
            }
            GlobalVariable.setProject(project);
            config = ServiceManager.getService(project, ApiGeneratorConfig.class);
            GlobalVariable.getInstance().setApiGeneratorConfig(config);

            boolean needRequestProjectId = false;
            if (StringUtils.isBlank(config.yApiServerUrl)) {
                needRequestProjectId = true;
                String serverUrl = Messages.showInputDialog("Input YApi Server Url", "YApi Server Url", Messages.getInformationIcon());
                if (StringUtils.isEmpty(serverUrl)) {
                    throw new BizException("YApi server url can not be empty.");
                }
                config.yApiServerUrl = serverUrl;
            }
            if (StringUtils.isBlank(config.projectToken) && (!config.isMultiModule || config.isUseDefaultToken)) {
                needRequestProjectId = true;
                String projectToken = Messages.showInputDialog("Input Project Token", "Project Token", Messages.getInformationIcon());
                if (StringUtils.isEmpty(projectToken)) {
                    throw new BizException("Project token can not be empty.");
                }
                config.projectToken = projectToken;
            }
            if (StringUtils.isBlank(config.projectId) || needRequestProjectId) {
                YApiProject projectInfo = YApiSdk.getProjectInfo(config.yApiServerUrl, config.projectToken);
                String projectId = projectInfo.get_id().toString();
                //if (StringUtils.isBlank(projectId)) {
                //    throw new BizException("Project id can not be empty");
                //}
                config.projectId = projectId;
            }

            PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
            Language language = referenceAt.getLanguage();
            if (language instanceof JavaLanguage) {
                PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
                if (selectedClass == null) {
                    NotificationUtil.errorNotify("this operate only support in class file", project);
                    return;
                }
                if (selectedClass.isInterface()) {
                    generateMarkdownForInterface(project, referenceAt, selectedClass);
                    return;
                }
                if (haveControllerAnnotation(selectedClass)) {
                    uploadApiToYApi(project, referenceAt, selectedClass);
                    return;
                }
                generateMarkdownForClass(project, selectedClass);
            } else if (language instanceof KotlinLanguage) {
                KtClass ktClass = PsiTreeUtil.getContextOfType(referenceAt, KtClass.class);
                Assert.notNull(ktClass);
                if (ktClass.isInterface()) {
                    generateMarkdownForInterface(project, referenceAt, ktClass);
                    return;
                }
                if (haveControllerAnnotation(ktClass)) {
                    uploadApiToYApi(project, referenceAt, ktClass);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof BizException) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            } else {
                throw e;
            }
        }
    }

    private void uploadApiToYApi(Project project, PsiElement referenceAt, PsiClass selectedClass) {
        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
            try {
                uploadSelectedMethodToYApi(project, selectedMethod);
            } catch (IOException e) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            }
            return;
        }
        try {
            uploadHttpMethodsToYApi(project, selectedClass);
        } catch (IOException e) {
            NotificationUtil.errorNotify(e.getMessage(), project);
        }
    }

    private void uploadApiToYApi(Project project, PsiElement referenceAt, KtClass selectedClass) {
        KtFunction selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, KtFunction.class);
        if (selectedMethod != null) {
            try {
                uploadSelectedMethodToYApi(project, selectedMethod);
            } catch (IOException e) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            }
            return;
        }
        try {
            uploadHttpMethodsToYApi(project, selectedClass);
        } catch (IOException e) {
            NotificationUtil.errorNotify(e.getMessage(), project);
        }
    }

    private void uploadHttpMethodsToYApi(Project project, PsiClass psiClass) throws IOException {
        if (!haveControllerAnnotation(psiClass)) {
            NotificationUtil.warnNotify("Upload api failed, reason:\n not REST api.", project);
            return;
        }
        if (StringUtils.isEmpty(config.yApiServerUrl)) {
            String serverUrl = Messages.showInputDialog("Input YApi Server Url", "YApi Server Url", Messages.getInformationIcon());
            if (StringUtils.isEmpty(serverUrl)) {
                NotificationUtil.warnNotify("YApi server url can not be empty.", project);
                return;
            }
            config.yApiServerUrl = serverUrl;
        }
        if (config.isUseDefaultToken && StringUtils.isBlank(config.projectToken)) {
            String projectToken = Messages.showInputDialog("Input Project Token", "Project Token", Messages.getInformationIcon());
            if (StringUtils.isEmpty(projectToken)) {
                NotificationUtil.warnNotify("Project token can not be empty.", project);
                return;
            }
            config.projectToken = projectToken;
        }
        if (StringUtils.isEmpty(config.projectId)) {
            YApiProject projectInfo = YApiSdk.getProjectInfo(config.yApiServerUrl, config.projectToken);
            String projectId = projectInfo != null && projectInfo.get_id() == null ? Messages.showInputDialog("Input Project Id", "Project Id", Messages.getInformationIcon()) : projectInfo.get_id().toString();
            if (StringUtils.isEmpty(projectId)) {
                NotificationUtil.warnNotify("Project id can not be empty.", project);
                return;
            }
            config.projectId = projectId;
        }
        PsiMethod[] methods = psiClass.getMethods();
        boolean uploadSuccess = false;
        for (PsiMethod method : methods) {
            if (hasMappingAnnotation(method)) {
                uploadSelectedMethodToYApi(project, method);
                uploadSuccess = true;
            }
        }
        if (uploadSuccess) {
            NotificationUtil.infoNotify("Upload api success.", project);
            return;
        }
        NotificationUtil.infoNotify("Upload api failed, reason:\n not REST api.", project);
    }

    private void uploadHttpMethodsToYApi(Project project, KtClass ktClass) throws IOException {
        if (!haveControllerAnnotation(ktClass)) {
            NotificationUtil.warnNotify("Upload api failed, reason:\n not REST api.", project);
            return;
        }
        if (StringUtils.isEmpty(config.yApiServerUrl)) {
            String serverUrl = Messages.showInputDialog("Input YApi Server Url", "YApi Server Url", Messages.getInformationIcon());
            if (StringUtils.isEmpty(serverUrl)) {
                NotificationUtil.warnNotify("YApi server url can not be empty.", project);
                return;
            }
            config.yApiServerUrl = serverUrl;
        }
        if (config.isUseDefaultToken && StringUtils.isBlank(config.projectToken)) {
            String projectToken = Messages.showInputDialog("Input Project Token", "Project Token", Messages.getInformationIcon());
            if (StringUtils.isEmpty(projectToken)) {
                NotificationUtil.warnNotify("Project token can not be empty.", project);
                return;
            }
            config.projectToken = projectToken;
        }
        if (StringUtils.isEmpty(config.projectId)) {
            YApiProject projectInfo = YApiSdk.getProjectInfo(config.yApiServerUrl, config.projectToken);
            String projectId = projectInfo != null && projectInfo.get_id() == null ? Messages.showInputDialog("Input Project Id", "Project Id", Messages.getInformationIcon()) : projectInfo.get_id().toString();
            if (StringUtils.isEmpty(projectId)) {
                NotificationUtil.warnNotify("Project id can not be empty.", project);
                return;
            }
            config.projectId = projectId;
        }

        boolean uploadSuccess = false;
        for (PsiElement psiElement : ktClass.getBody().getChildren()) {
            if (psiElement instanceof KtFunction) {
                KtFunction method = (KtFunction) psiElement;
                if (hasMappingAnnotation(method)) {
                    uploadSelectedMethodToYApi(project, method);
                    uploadSuccess = true;
                }
            }
        }
        if (uploadSuccess) {
            NotificationUtil.infoNotify("Upload api success.", project);
            return;
        }
        NotificationUtil.infoNotify("Upload api failed, reason:\n not REST api.", project);
    }

    private void generateMarkdownForInterface(Project project, PsiElement referenceAt, PsiClass selectedClass) {
        PsiMethod selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, PsiMethod.class);
        if (selectedMethod != null) {
            try {
                generateMarkdownForSelectedMethod(project, selectedMethod);
            } catch (IOException e) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            }
            return;
        }
        try {
            generateMarkdownsForAllMethods(project, selectedClass);
        } catch (IOException e) {
            NotificationUtil.errorNotify(e.getMessage(), project);
        }
    }

    private void generateMarkdownForInterface(Project project, PsiElement referenceAt, KtClass selectedClass) {
        KtFunction selectedMethod = PsiTreeUtil.getContextOfType(referenceAt, KtFunction.class);
        if (selectedMethod != null) {
            try {
                generateMarkdownForSelectedMethod(project, selectedMethod);
            } catch (IOException e) {
                NotificationUtil.errorNotify(e.getMessage(), project);
            }
            return;
        }
        try {
            generateMarkdownsForAllMethods(project, selectedClass);
        } catch (IOException e) {
            NotificationUtil.errorNotify(e.getMessage(), project);
        }
    }

    private void generateMarkdownForClass(Project project, PsiClass psiClass) {
        String dirPath = getDirPath(project);
        if (!mkDirectory(project, dirPath)) {
            return;
        }
        boolean generateSuccess = false;
        try {
            generateSuccess = generateDocForClass(project, psiClass, dirPath);
        } catch (IOException e) {
            NotificationUtil.errorNotify(e.getMessage(), project);
        }
        if (generateSuccess) {
            NotificationUtil.infoNotify("Generate Api doc success.", project);
        }
    }

    protected void generateMarkdownForSelectedMethod(Project project, PsiMethod selectedMethod) throws IOException {
        String dirPath = getDirPath(project);
        if (!mkDirectory(project, dirPath)) {
            return;
        }
        boolean generateSuccess = generateDocForMethod(project, selectedMethod, dirPath);
        if (generateSuccess) {
            NotificationUtil.infoNotify("Generate Api Plus doc success.", project);
        }
    }

    protected void generateMarkdownForSelectedMethod(Project project, KtFunction selectedMethod) throws IOException {
        String dirPath = getDirPath(project);
        if (!mkDirectory(project, dirPath)) {
            return;
        }
        boolean generateSuccess = generateDocForMethod(project, selectedMethod, dirPath);
        if (generateSuccess) {
            NotificationUtil.infoNotify("Generate Api Plus doc success.", project);
        }
    }

    protected void generateMarkdownsForAllMethods(Project project, PsiClass selectedClass) throws IOException {
        String dirPath = getDirPath(project);
        if (!mkDirectory(project, dirPath)) {
            return;
        }
        boolean generateSuccess = false;
        for (PsiMethod psiMethod : selectedClass.getMethods()) {
            if (generateDocForMethod(project, psiMethod, dirPath)) {
                generateSuccess = true;
            }
        }
        if (generateSuccess) {
            NotificationUtil.infoNotify("generate api doc success.", project);
        }
    }

    protected void generateMarkdownsForAllMethods(Project project, KtClass selectedClass) throws IOException {
        String dirPath = getDirPath(project);
        if (!mkDirectory(project, dirPath)) {
            return;
        }
        boolean generateSuccess = false;
        for (PsiElement psiElement : selectedClass.getBody().getChildren()) {
            KtFunction ktFunction = (KtFunction) psiElement;
            if (generateDocForMethod(project, ktFunction, dirPath)) {
                generateSuccess = true;
            }
        }
        if (generateSuccess) {
            NotificationUtil.infoNotify("generate api doc success.", project);
        }
    }

    private void uploadSelectedMethodToYApi(Project project, PsiMethod method) throws IOException {
        if (!hasMappingAnnotation(method)) {
            NotificationUtil.warnNotify("Upload api failed, reason:\n not REST api.", project);
            return;
        }
        yApiProjectConfigInfo = getProjectConfigInfo(method);
        uploadToYApi(project, method);
    }

    private void uploadSelectedMethodToYApi(Project project, KtFunction method) throws IOException {
        if (!hasMappingAnnotation(method)) {
            NotificationUtil.warnNotify("Upload api failed, reason:\n not REST api.", project);
            return;
        }
        yApiProjectConfigInfo = getProjectConfigInfo(method);
        uploadToYApi(project, method);
    }

    private void uploadToYApi(Project project, PsiMethod psiMethod) throws IOException {
        YApiInterface yApiInterface = buildYApiInterface(project, psiMethod);
        if (yApiInterface == null) {
            return;
        }
        YApiResponse yApiResponse = YApiSdk.saveInterface(config.yApiServerUrl, yApiInterface);
        if (yApiResponse.getErrcode() != 0) {
            NotificationUtil.errorNotify("Upload api failed, cause:" + yApiResponse.getErrmsg(), project);
            return;
        }
        NotificationUtil.infoNotify("Upload api success.", project);
    }

    private void uploadToYApi(Project project, KtFunction ktFunction) throws IOException {
        YApiInterface yApiInterface = buildYApiInterface(project, ktFunction);
        if (yApiInterface == null) {
            return;
        }
        YApiResponse yApiResponse = YApiSdk.saveInterface(config.yApiServerUrl, yApiInterface);
        if (yApiResponse.getErrcode() != 0) {
            NotificationUtil.errorNotify("Upload api failed, cause:" + yApiResponse.getErrmsg(), project);
            return;
        }
        NotificationUtil.infoNotify("Upload api success.", project);
    }

    private YApiInterface buildYApiInterface(Project project, PsiMethod psiMethod) throws IOException {
        MethodInfo methodInfo = new MethodInfo(psiMethod);
        return buildYApiInterface(project, methodInfo);
    }

    private YApiInterface buildYApiInterface(Project project, KtFunction ktFunction) throws IOException {
        MethodInfo methodInfo = new MethodInfo(ktFunction);
        return buildYApiInterface(project, methodInfo);
    }

    @Nullable
    private YApiInterface buildYApiInterface(Project project, MethodInfo methodInfo) throws IOException {
        if (!methodInfo.containControllerAnnotation()) {
            NotificationUtil.warnNotify("Invalid Class File!", project);
            return null;
        }
        YApiInterface yApiInterface = new YApiInterface();
        yApiInterface.setTag(StringUtil.string2Set(config.tag));
        yApiInterface.setStatus(config.apiDone ? YApiInterfaceStatusEnum.DONE.getValue() : YApiInterfaceStatusEnum.UNDONE.getValue());
        yApiInterface.setToken(config.projectToken);
        yApiInterface.setPath(PathUtil.pathResolve(methodInfo.getClassPath(), methodInfo.getMethodPath()));
        yApiInterface.setToken(yApiProjectConfigInfo.getToken());
        yApiInterface.setPath(PathUtil.pathResolve(yApiProjectConfigInfo.getBasePath(), yApiInterface.getPath()));

        RequestMethodEnum requestMethodEnum = methodInfo.getRequestMethod();
        yApiInterface.setMethod(requestMethodEnum.name());
        List<FieldInfo> requestFields = FieldUtil.filterChildrenFiled(methodInfo.getRequestFields(), config.filterFieldInfo);
        MediaType requestMediaType = methodInfo.getRequestMediaType();
        if (methodInfo.getParamStr().contains(WebAnnotation.RequestBody)) {
            yApiInterface.setReq_body_type(RequestBodyTypeEnum.JSON.getValue());
            yApiInterface.setReq_body_other(JsonUtil.buildJson5(getRequestBodyParam(requestFields)));
            yApiInterface.addReqYApiHeader(YApiHeader.json());
        } else if (MediaType.APPLICATION_FORM_URLENCODED == requestMediaType) {
            yApiInterface.setReq_body_type(RequestBodyTypeEnum.FORM.getValue());
            yApiInterface.setReq_body_form(listYApiForms(requestFields));
            yApiInterface.addReqYApiHeader(YApiHeader.form());
        } else if (MediaType.MULTIPART_FORM_DATA == requestMediaType) {
            yApiInterface.setReq_body_type(RequestBodyTypeEnum.FORM.getValue());
            yApiInterface.setReq_body_form(listYApiForms(requestFields));
            yApiInterface.addReqYApiHeader(YApiHeader.multipartFormData());
        }

        for (FieldInfo requestField : requestFields) {
            if (requestField.containRequestHeaderAnnotation()) {
                yApiInterface.addReqYApiHeader(new YApiHeader(requestField.getName(), "",
                        requestField.isRequire(), requestField.getDesc()));
            }
        }

        yApiInterface.setReq_query(listYApiQueries(requestFields, requestMethodEnum, requestMediaType));
        yApiInterface.setCatid(getCatId(methodInfo.getCatName(), yApiProjectConfigInfo));

        if (StringUtils.isNotBlank(methodInfo.getTitle())) {
            yApiInterface.setTitle(requestMethodEnum.name() + " " + methodInfo.getTitle());
        } else {
            yApiInterface.setTitle(requestMethodEnum.name() + " " + yApiInterface.getPath());
        }
        if (methodInfo.isReturnJSON()) {
            yApiInterface.setRes_body(JsonUtil.buildJson5(methodInfo.getResponse()));
        }
        yApiInterface.setReq_params(listYApiPathVariables(requestFields));
        yApiInterface.setDesc(Objects.nonNull(yApiInterface.getDesc()) ? yApiInterface.getDesc()
                : "<pre><code data-language=\"java\" class=\"java\">" + methodInfo.getDesc() + "</code> </pre>");

        if (config.ignoreResponse) {
            yApiInterface.setRes_body(null);
            yApiInterface.setRes_body_type(null);
            yApiInterface.setRes_body_is_json_schema(null);
        } else {
            YApiInterface docInterface = methodInfo.getYApiInterface();
            if (docInterface != null) {
                if (null != docInterface.getRes_body()) {
                    if ("ignore".equals(docInterface.getRes_body().trim())) {
                        yApiInterface.setRes_body(null);
                    } else {
                        yApiInterface.setRes_body(docInterface.getRes_body());
                    }
                }
                if (StringUtils.isNotBlank(docInterface.getRes_body_type())) {
                    if ("ignore".equals(docInterface.getRes_body_type().trim())) {
                        yApiInterface.setRes_body_type(null);
                    } else {
                        yApiInterface.setRes_body_type(docInterface.getRes_body_type());
                    }
                }
                if (null != docInterface.getRes_body_is_json_schema()) {
                    yApiInterface.setRes_body_is_json_schema(docInterface.getRes_body_is_json_schema());
                }
            }
        }

        return yApiInterface;
    }

    private String buildPath(PsiAnnotation classRequestMapping, PsiAnnotation methodMapping) {
        String classPath = getPathFromAnnotation(classRequestMapping);
        String methodPath = getPathFromAnnotation(methodMapping);
        return classPath + methodPath;
    }

    private FieldInfo getRequestBodyParam(List<FieldInfo> params) {
        if (params == null) {
            return null;
        }
        for (FieldInfo fieldInfo : params) {
            if (fieldInfo.containRequestBodyAnnotation()) {
                return fieldInfo;
            }
        }
        return null;
    }

    private boolean containRequestBodyAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RequestBody)) {
                return true;
            }
        }
        return false;
    }

    private boolean containResponseBodyAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.ResponseBody)) {
                return true;
            }
        }
        return false;
    }

    private boolean containRestControllerAnnotation(PsiAnnotation[] annotations) {
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.RestController)) {
                return true;
            }
        }
        return false;
    }

    private String getMethodDesc(PsiMethod psiMethod) {
        String methodDesc = psiMethod.getText().replace(Objects.nonNull(psiMethod.getBody()) ? psiMethod.getBody().getText() : "", "");
        if (!Strings.isNullOrEmpty(methodDesc)) {
            methodDesc = methodDesc.replace("<", "&lt;").replace(">", "&gt;");
        }
        return methodDesc;
    }

    private String getMethodDesc(KtFunction ktFunction) {
        KDoc docComment = ktFunction.getDocComment();
        if (docComment == null) {
            return "";
        }
        String methodDesc = docComment.getText();
        if (!Strings.isNullOrEmpty(methodDesc)) {
            methodDesc = methodDesc.replace("<", "&lt;").replace(">", "&gt;");
        }
        return methodDesc;
    }

    private List<YApiPathVariable> listYApiPathVariables(List<FieldInfo> requestFields) {
        List<YApiPathVariable> yApiPathVariables = new ArrayList<>();
        for (FieldInfo fieldInfo : requestFields) {
            List<PsiAnnotation> annotations = fieldInfo.getAnnotations();
            PsiAnnotation pathVariable = getPathVariableAnnotation(annotations);
            if (pathVariable != null) {
                YApiPathVariable yApiPathVariable = new YApiPathVariable();
                yApiPathVariable.setName(getPathVariableName(pathVariable, fieldInfo.getName()));
                yApiPathVariable.setDesc(fieldInfo.getDesc());
                yApiPathVariable.setExample(FieldUtil.getValue(fieldInfo).toString());
                yApiPathVariables.add(yApiPathVariable);
            }

            // Kotlin
            List<KtAnnotationEntry> ktAnnotationEntries = fieldInfo.getKtAnnotationEntries();
            KtAnnotationEntry ktAnnotationEntry = FieldUtil.findKtAnnotationByName(ktAnnotationEntries,
                    WebAnnotation.PathVariable);
            if (ktAnnotationEntry != null) {
                YApiPathVariable yApiPathVariable = new YApiPathVariable();
                yApiPathVariable.setName(getPathVariableName(ktAnnotationEntry, fieldInfo.getName()));
                yApiPathVariable.setDesc(fieldInfo.getDesc());
                yApiPathVariable.setExample(FieldUtil.getValue(fieldInfo).toString());
                yApiPathVariables.add(yApiPathVariable);
            }
        }
        return yApiPathVariables;
    }

    private String getPathVariableName(PsiAnnotation pathVariable, String fieldName) {
        PsiNameValuePair[] psiNameValuePairs = pathVariable.getParameterList().getAttributes();
        if (psiNameValuePairs.length > 0) {
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                String literalValue = psiNameValuePair.getLiteralValue();
                if (StringUtils.isEmpty(literalValue)) {
                    continue;
                }
                String name = psiNameValuePair.getName();
                if (name == null || "value".equals(name) || "name".equals(name)) {
                    return literalValue;
                }
            }
        }
        return fieldName;
    }

    private String getPathVariableName(KtAnnotationEntry ktAnnotationEntry, String fieldName) {
        KtValueArgumentList valueArgumentList = ktAnnotationEntry.getValueArgumentList();
        if (valueArgumentList != null) {
            for (KtValueArgument ktValueArgument : valueArgumentList.getArguments()) {
                if (ktValueArgument.getArgumentName() == null) {
                    return ktValueArgument.getText().replace("\"", "");
                }
                if ("value".equals(ktValueArgument.getArgumentName().getText())
                        || "name".equals(ktValueArgument.getArgumentName().getText())) {
                    KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
                    String text = null;
                    if (argumentExpression == null) {
                        return "";
                    }
                    if (argumentExpression instanceof KtCollectionLiteralExpression) {
                        KtCollectionLiteralExpression collectionLiteralExpression =
                                (KtCollectionLiteralExpression) argumentExpression;
                        List<KtExpression> innerExpressions = collectionLiteralExpression.getInnerExpressions();
                        if (CollectionUtils.isNotEmpty(innerExpressions)) {
                            text = innerExpressions.get(0).getText();
                        }
                    } else {
                        text = argumentExpression.getText();
                    }
                    return text == null ? "" : text.replace("\"", "");
                }
            }
        }
        return fieldName;
    }

    private PsiAnnotation getPathVariableAnnotation(List<PsiAnnotation> annotations) {
        return FieldUtil.findAnnotationByName(annotations, WebAnnotation.PathVariable);
    }


    private String getPathFromAnnotation(PsiAnnotation annotation) {
        if (annotation == null) {
            return "";
        }
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        if (psiNameValuePairs.length == 1 && psiNameValuePairs[0].getName() == null) {
            return appendSlash(psiNameValuePairs[0].getLiteralValue());
        }
        if (psiNameValuePairs.length >= 1) {
            for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
                if (psiNameValuePair.getName().equals("value") || psiNameValuePair.getName().equals("path")) {
                    return appendSlash(psiNameValuePair.getLiteralValue());
                }
            }
        }
        return "";
    }

    private String appendSlash(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        String p = path;
        if (!path.startsWith(SLASH)) {
            p = SLASH + path;
        }
        if (path.endsWith(SLASH)) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private String getDefaultCatName() {
        String defaultCat = config.defaultCat;
        return StringUtils.isEmpty(defaultCat) ? "api_generator_plus" : defaultCat;
    }

//    private String getClassCatName(PsiDocComment classDesc) {
//        if (classDesc == null) {
//            return "";
//        }
//        return DesUtil.getDescription(classDesc).split(" ")[0];
//    }
//
//    private String getClassCatName(KDoc classDesc) {
//        if (classDesc == null) {
//            return "";
//        }
//        return DesUtil.getDescription(classDesc).split(" ")[0];
//    }

    private String getCatId(Map<String, YApiCat> catNameMap, PsiDocComment classDesc) throws IOException {
        String defaultCatName = getDefaultCatName();
        String catName;
        if (config.autoCat) {
            String classCatName = DesUtil.getInterfaceCatName(classDesc);
            catName = StringUtils.isEmpty(classCatName) ? defaultCatName : classCatName;
        } else {
            catName = defaultCatName;
        }
        YApiCat apiCat = catNameMap.get(catName);
        if (apiCat != null) {
            return apiCat.get_id().toString();
        }
        YApiResponse<YApiCat> yApiResponse = YApiSdk.addCategory(config.yApiServerUrl, config.projectToken, config.projectId, catName);
        return yApiResponse.getData().get_id().toString();
    }

    private String getCatId(String catName, YApiProjectConfigInfo yApiProjectConfigInfo)
            throws IOException {
        Map<String, YApiCat> catNameMap = getCatNameMap(yApiProjectConfigInfo);
        String defaultCatName = getDefaultCatName();
        if (config.autoCat) {
            catName = StringUtils.isEmpty(catName) ? defaultCatName : catName;
        } else {
            catName = defaultCatName;
        }
        YApiCat apiCat = catNameMap.get(catName);
        if (apiCat != null) {
            return apiCat.get_id().toString();
        }
        YApiResponse<YApiCat> yApiResponse = YApiSdk.addCategory(config.yApiServerUrl,
                yApiProjectConfigInfo.getToken(), yApiProjectConfigInfo.getProjectId(), catName);
        return yApiResponse.getData().get_id().toString();
    }

    private String getCatId(Map<String, YApiCat> catNameMap, String catName, YApiProjectConfigInfo yApiProjectConfigInfo)
            throws IOException {
        String defaultCatName = getDefaultCatName();
        if (config.autoCat) {
            catName = StringUtils.isEmpty(catName) ? defaultCatName : catName;
        } else {
            catName = defaultCatName;
        }
        YApiCat apiCat = catNameMap.get(catName);
        if (apiCat != null) {
            return apiCat.get_id().toString();
        }
        YApiResponse<YApiCat> yApiResponse = YApiSdk.addCategory(config.yApiServerUrl,
                yApiProjectConfigInfo.getToken(), yApiProjectConfigInfo.getProjectId(), catName);
        return yApiResponse.getData().get_id().toString();
    }

    private String getCatId(Map<String, YApiCat> catNameMap, PsiDocComment classDesc, YApiProjectConfigInfo yApiProjectConfigInfo)
            throws IOException {
        String defaultCatName = getDefaultCatName();
        String catName;
        if (config.autoCat) {
            String classCatName = DesUtil.getInterfaceCatName(classDesc);
            catName = StringUtils.isEmpty(classCatName) ? defaultCatName : classCatName;
        } else {
            catName = defaultCatName;
        }
        YApiCat apiCat = catNameMap.get(catName);
        if (apiCat != null) {
            return apiCat.get_id().toString();
        }
        YApiResponse<YApiCat> yApiResponse = YApiSdk.addCategory(config.yApiServerUrl,
                yApiProjectConfigInfo.getToken(), yApiProjectConfigInfo.getProjectId(), catName);
        return yApiResponse.getData().get_id().toString();
    }

    private String getCatId(Map<String, YApiCat> catNameMap, KDoc classKDoc, YApiProjectConfigInfo yApiProjectConfigInfo)
            throws IOException {
        String defaultCatName = getDefaultCatName();
        String catName;
        if (config.autoCat) {
            String classCatName = DesUtil.getInterfaceCatName(classKDoc);
            catName = StringUtils.isEmpty(classCatName) ? defaultCatName : classCatName;
        } else {
            catName = defaultCatName;
        }
        YApiCat apiCat = catNameMap.get(catName);
        if (apiCat != null) {
            return apiCat.get_id().toString();
        }
        YApiResponse<YApiCat> yApiResponse = YApiSdk.addCategory(config.yApiServerUrl,
                yApiProjectConfigInfo.getToken(), yApiProjectConfigInfo.getProjectId(), catName);
        return yApiResponse.getData().get_id().toString();
    }

    private Map<String, YApiCat> getCatNameMap() throws IOException {
        List<YApiCat> yApiCats = YApiSdk.listCategories(config.yApiServerUrl, config.projectToken);
        Map<String, YApiCat> catNameMap = new HashMap<>();
        for (YApiCat cat : yApiCats) {
            catNameMap.put(cat.getName(), cat);
        }
        return catNameMap;
    }

    private Map<String, YApiCat> getCatNameMap(YApiProjectConfigInfo yApiProjectConfigInfo) throws IOException {
        List<YApiCat> yApiCats = YApiSdk.listCategories(config.yApiServerUrl, yApiProjectConfigInfo.getToken());
        Map<String, YApiCat> catNameMap = new HashMap<>();
        for (YApiCat cat : yApiCats) {
            catNameMap.put(cat.getName(), cat);
        }
        return catNameMap;
    }

    private List<YApiQuery> listYApiQueries(List<FieldInfo> requestFields, RequestMethodEnum requestMethodEnum,
                                            MediaType mediaType) {
        List<YApiQuery> queries = new ArrayList<>();
        if (MediaType.MULTIPART_FORM_DATA == mediaType || MediaType.APPLICATION_FORM_URLENCODED == mediaType) {
            return queries;
        }
        for (FieldInfo fieldInfo : requestFields) {
            if (notQuery(fieldInfo)) {
                continue;
            }
            if (TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
                queries.add(buildYApiQuery(fieldInfo));
            } else if (TypeEnum.OBJECT.equals(fieldInfo.getParamType())) {
                List<FieldInfo> children = fieldInfo.getChildren();
                for (FieldInfo info : children) {
                    queries.add(buildYApiQuery(info));
                }
            } else {
                YApiQuery apiQuery = buildYApiQuery(fieldInfo);
                apiQuery.setExample("1,1,1");
                queries.add(apiQuery);
            }
        }
        return queries;
    }

    private boolean notQuery(FieldInfo fieldInfo) {
        return fieldInfo.containPathVariableAnnotation() || fieldInfo.containRequestBodyAnnotation()
                || fieldInfo.containRequestAttributeAnnotation() || fieldInfo.containRequestHeaderAnnotation();
    }

    private YApiQuery buildYApiQuery(FieldInfo fieldInfo) {
        YApiQuery query = new YApiQuery();
        query.setName(fieldInfo.getName());
        query.setDesc(generateDesc(fieldInfo));
        Object value = FieldUtil.getValue(fieldInfo);
        if (value != null) {
            query.setExample(value.toString());
        }
        query.setRequired(convertRequired(fieldInfo.isRequire()));
        return query;
    }

    private String convertRequired(boolean required) {
        return required ? "1" : "0";
    }

    private String generateDesc(FieldInfo fieldInfo) {
        if (AssertUtils.isEmpty(fieldInfo.getRange()) || "N/A".equals(fieldInfo.getRange())) {
            return fieldInfo.getDesc();
        }
        if (AssertUtils.isEmpty(fieldInfo.getDesc())) {
            return "值域：" + fieldInfo.getRange();
        }
        return fieldInfo.getDesc() + "，值域：" + fieldInfo.getRange();
    }

    private List<YApiForm> listYApiForms(List<FieldInfo> requestFields) {
        List<YApiForm> yApiForms = new ArrayList<>();
        for (FieldInfo fieldInfo : requestFields) {
            if (fieldInfo.containPathVariableAnnotation() || fieldInfo.containRequestHeaderAnnotation()
                    || fieldInfo.containRequestAttributeAnnotation()) {
                continue;
            }
            if (TypeEnum.LITERAL.equals(fieldInfo.getParamType())) {
                yApiForms.add(buildYApiForm(fieldInfo));
            } else if (TypeEnum.OBJECT.equals(fieldInfo.getParamType())) {
                List<FieldInfo> children = fieldInfo.getChildren();
                for (FieldInfo info : children) {
                    yApiForms.add(buildYApiForm(info));
                }
            } else {
                YApiForm apiQuery = buildYApiForm(fieldInfo);
                apiQuery.setExample("1,1,1");
                yApiForms.add(apiQuery);
            }
        }
        return yApiForms;
    }

    private YApiForm buildYApiForm(FieldInfo fieldInfo) {
        YApiForm param = new YApiForm();
        param.setName(fieldInfo.getName());
        if (FieldUtil.isFileType(fieldInfo.getTypeText())) {
            param.setType("file");
        }
        param.setDesc(fieldInfo.getDesc());
        param.setExample(FieldUtil.getValue(fieldInfo).toString());
        param.setRequired(convertRequired(fieldInfo.isRequire()));
        return param;
    }

    private RequestMethodEnum getMethodFromAnnotation(PsiAnnotation methodMapping) {
        String text = methodMapping.getText();
        if (text.contains(WebAnnotation.RequestMapping)) {
            return extractMethodFromAttribute(methodMapping);
        }
        return extractMethodFromMappingText(text);
    }

    private RequestMethodEnum extractMethodFromMappingText(String text) {
        if (text.contains(WebAnnotation.GetMapping)) {
            return RequestMethodEnum.GET;
        }
        if (text.contains(WebAnnotation.PutMapping)) {
            return RequestMethodEnum.PUT;
        }
        if (text.contains(WebAnnotation.DeleteMapping)) {
            return RequestMethodEnum.DELETE;
        }
        if (text.contains(WebAnnotation.PatchMapping)) {
            return RequestMethodEnum.PATCH;
        }
        return RequestMethodEnum.POST;
    }

    private RequestMethodEnum extractMethodFromAttribute(PsiAnnotation annotation) {
        PsiNameValuePair[] psiNameValuePairs = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair psiNameValuePair : psiNameValuePairs) {
            if ("method".equals(psiNameValuePair.getName())) {
                PsiReference reference = psiNameValuePair.getValue().getReference();
                if (reference != null) {
                    return RequestMethodEnum.valueOf(reference.resolve().getText());
                }
                PsiElement[] children = psiNameValuePair.getValue().getChildren();
                for (PsiElement child : children) {
                    if (child instanceof PsiReference) {
                        return RequestMethodEnum.valueOf(((PsiReference) child).resolve().getText());
                    }
                }
            }
        }
        return RequestMethodEnum.POST;
    }

    private PsiAnnotation getMethodMapping(PsiMethod psiMethod) {
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            String text = annotation.getText();
            if (text.contains("Mapping")) {
                return annotation;
            }
        }
        return null;
    }

    private boolean hasMappingAnnotation(PsiMethod method) {
        PsiAnnotation[] annotations = method.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains("Mapping")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMappingAnnotation(KtFunction method) {
        for (KtAnnotationEntry annotation : method.getAnnotationEntries()) {
            if (annotation.getText().contains("Mapping")) {
                return true;
            }
        }
        return false;
    }

    private boolean haveControllerAnnotation(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getText().contains(WebAnnotation.Controller)) {
                return true;
            }
        }
        return false;
    }

    private boolean haveControllerAnnotation(KtClass ktClass) {
        for (KtAnnotationEntry annotationEntry : ktClass.getAnnotationEntries()) {
            if (annotationEntry.getText().contains(WebAnnotation.Controller)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(AnActionEvent event) {
        //perform action if and only if EDITOR != null
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        boolean enabled = false;
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (editor != null && null != psiFile
                && (psiFile.getFileType() instanceof JavaFileType || psiFile.getFileType() instanceof KotlinFileType)) {
            enabled = true;
        }
        event.getPresentation().setEnabledAndVisible(enabled);
        event.getPresentation().setIcon(SdkIcons.Logo);
    }

    private String getDirPath(Project project) {
        String dirPath = config.dirPath;
        if (StringUtils.isEmpty(dirPath)) {
            return project.getBasePath() + "/target/api_docs";
        }

        if (dirPath.endsWith(SLASH)) {
            return dirPath.substring(0, dirPath.lastIndexOf(SLASH));
        }
        return dirPath;
    }

    private boolean generateDocForClass(Project project, PsiClass psiClass, String dirPath) throws IOException {
        if (!mkDirectory(project, dirPath)) {
            return false;
        }
        String fileName = psiClass.getName();
        File apiDoc = new File(dirPath + SLASH + fileName + ".md");
        boolean notExist = apiDoc.createNewFile();
        if (!notExist) {
            if (!config.overwrite) {
                int choose = Messages.showOkCancelDialog(fileName + ".md already exists,do you want to overwrite it?", "Overwrite Warning!", "Yes", "No", Messages.getWarningIcon());
                if (Messages.CANCEL == choose) {
                    return false;
                }
            }
        }
        try (Writer md = new FileWriter(apiDoc)) {
            List<FieldInfo> fieldInfos = listFieldInfos(psiClass);
            md.write("## 示例\n");
            if (AssertUtils.isNotEmpty(fieldInfos)) {
                md.write("```json\n");
                md.write(JsonUtil.buildPrettyJson(fieldInfos) + "\n");
                md.write("```\n");
            }
            md.write("## 参数说明\n");
            if (AssertUtils.isNotEmpty(fieldInfos)) {
                writeParamTableHeader(md);
                for (FieldInfo fieldInfo : fieldInfos) {
                    writeFieldInfo(md, fieldInfo);
                }
            }
        }
        return true;
    }

    private void writeParamTableHeader(Writer md) throws IOException {
        md.write("名称|类型|必填|值域范围|描述/示例\n");
        md.write("---|---|---|---|---\n");
    }

    public List<FieldInfo> listFieldInfos(PsiClass psiClass) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (PsiField psiField : psiClass.getFields()) {
            if (StringUtil.string2Set(config.excludeFields).contains(psiField.getName())) {
                continue;
            }
            fieldInfos.add(new FieldInfo(psiClass.getProject(), psiField.getName(), psiField.getType(), DesUtil.getDescription(psiField.getDocComment()), psiField.getAnnotations()));
        }
        return fieldInfos;
    }

    private boolean generateDocForMethod(Project project, PsiMethod selectedMethod, String dirPath) throws IOException {
        if (!mkDirectory(project, dirPath)) {
            return false;
        }
        MethodInfo methodInfo = new MethodInfo(selectedMethod);
        String fileName = getFileName(methodInfo);
        File apiDoc = new File(dirPath + SLASH + fileName + ".md");
        boolean notExist = apiDoc.createNewFile();
        if (!notExist) {
            if (!config.overwrite) {
                int choose = Messages.showOkCancelDialog(fileName + ".md already exists,do you want to overwrite it?", "Overwrite Warning!", "Yes", "No", Messages.getWarningIcon());
                if (Messages.CANCEL == choose) {
                    return false;
                }
            }
        }
        Model pomModel = getPomModel(project);
        try (Writer md = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(apiDoc, true),
                StandardCharsets.UTF_8))) {
            md.write("## " + fileName + "\n");
            md.write("## 功能介绍\n");
            md.write(methodInfo.getDesc() + "\n");
            md.write("## Maven依赖\n");
            md.write("```xml\n");
            md.write("<dependency>\n");
            md.write("\t<groupId>" + pomModel.getGroupId() + "</groupId>\n");
            md.write("\t<artifactId>" + pomModel.getArtifactId() + "</artifactId>\n");
            md.write("\t<version>" + pomModel.getVersion() + "</version>\n");
            md.write("</dependency>\n");
            md.write("```\n");
            md.write("## 接口声明\n");
            md.write("```java\n");
            md.write("package " + methodInfo.getPackageName() + ";\n\n");
            md.write("public interface " + methodInfo.getClassName() + " {\n\n");
            md.write("\t" + methodInfo.getReturnStr() + " " + methodInfo.getMethodName() + methodInfo.getParamStr() + ";\n\n");
            md.write("}\n");
            md.write("```\n");
            md.write("## 请求参数\n");
            md.write("### 请求参数示例\n");
            if (AssertUtils.isNotEmpty(methodInfo.getRequestFields())) {
                md.write("```json\n");
                md.write(JsonUtil.buildPrettyJson(methodInfo.getRequestFields()) + "\n");
                md.write("```\n");
            }
            md.write("### 请求参数说明\n");
            if (AssertUtils.isNotEmpty(methodInfo.getRequestFields())) {
                writeParamTableHeader(md);
                for (FieldInfo fieldInfo : methodInfo.getRequestFields()) {
                    writeFieldInfo(md, fieldInfo);
                }
            }
            md.write("\n## 返回结果\n");
            md.write("### 返回结果示例\n");
            if (AssertUtils.isNotEmpty(methodInfo.getResponseFields())) {
                md.write("```json\n");
                md.write(JsonUtil.buildPrettyJson(methodInfo.getResponse()) + "\n");
                md.write("```\n");
            }
            md.write("### 返回结果说明\n");
            if (AssertUtils.isNotEmpty(methodInfo.getResponseFields())) {
                writeParamTableHeader(md);
                for (FieldInfo fieldInfo : methodInfo.getResponseFields()) {
                    writeFieldInfo(md, fieldInfo, "");
                }
            }
        }
        return true;
    }

    private boolean generateDocForMethod(Project project, KtFunction selectedMethod, String dirPath) throws IOException {
        if (!mkDirectory(project, dirPath)) {
            return false;
        }
        MethodInfo methodInfo = new MethodInfo(selectedMethod);
        String fileName = getFileName(methodInfo);
        File apiDoc = new File(dirPath + SLASH + fileName + ".md");
        boolean notExist = apiDoc.createNewFile();
        if (!notExist) {
            if (!config.overwrite) {
                int choose = Messages.showOkCancelDialog(fileName + ".md already exists,do you want to overwrite it?", "Overwrite Warning!", "Yes", "No", Messages.getWarningIcon());
                if (Messages.CANCEL == choose) {
                    return false;
                }
            }
        }
        Model pomModel = getPomModel(project);
        try (Writer md = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(apiDoc, true),
                StandardCharsets.UTF_8))) {
            md.write("## " + fileName + "\n");
            md.write("## 功能介绍\n");
            md.write(methodInfo.getDesc() + "\n");
            md.write("## Maven依赖\n");
            md.write("```xml\n");
            md.write("<dependency>\n");
            md.write("\t<groupId>" + pomModel.getGroupId() + "</groupId>\n");
            md.write("\t<artifactId>" + pomModel.getArtifactId() + "</artifactId>\n");
            md.write("\t<version>" + pomModel.getVersion() + "</version>\n");
            md.write("</dependency>\n");
            md.write("```\n");
            md.write("## 接口声明\n");
            md.write("```java\n");
            md.write("package " + methodInfo.getPackageName() + ";\n\n");
            md.write("public interface " + methodInfo.getClassName() + " {\n\n");
            md.write("\t" + methodInfo.getReturnStr() + " " + methodInfo.getMethodName() + methodInfo.getParamStr() + ";\n\n");
            md.write("}\n");
            md.write("```\n");
            md.write("## 请求参数\n");
            md.write("### 请求参数示例\n");
            if (AssertUtils.isNotEmpty(methodInfo.getRequestFields())) {
                md.write("```json\n");
                md.write(JsonUtil.buildPrettyJson(methodInfo.getRequestFields()) + "\n");
                md.write("```\n");
            }
            md.write("### 请求参数说明\n");
            if (AssertUtils.isNotEmpty(methodInfo.getRequestFields())) {
                writeParamTableHeader(md);
                for (FieldInfo fieldInfo : methodInfo.getRequestFields()) {
                    writeFieldInfo(md, fieldInfo);
                }
            }
            md.write("\n## 返回结果\n");
            md.write("### 返回结果示例\n");
            if (AssertUtils.isNotEmpty(methodInfo.getResponseFields())) {
                md.write("```json\n");
                md.write(JsonUtil.buildPrettyJson(methodInfo.getResponse()) + "\n");
                md.write("```\n");
            }
            md.write("### 返回结果说明\n");
            if (AssertUtils.isNotEmpty(methodInfo.getResponseFields())) {
                writeParamTableHeader(md);
                for (FieldInfo fieldInfo : methodInfo.getResponseFields()) {
                    writeFieldInfo(md, fieldInfo, "");
                }
            }
        }
        return true;
    }

    private boolean mkDirectory(Project project, String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                NotificationUtil.errorNotify("invalid directory path!", project);
                return false;
            }
        }
        return true;
    }

    private Model getPomModel(Project project) {
        PsiFile pomFile = FilenameIndex.getFilesByName(project, "pom.xml", GlobalSearchScope.projectScope(project))[0];
        String pomPath = pomFile.getContainingDirectory().getVirtualFile().getPath() + "/pom.xml";
        return readPom(pomPath);
    }

    private String getFileName(MethodInfo methodInfo) {
        if (!config.cnFileName) {
            return methodInfo.getMethodName();
        }
        if (StringUtils.isEmpty(methodInfo.getDesc()) || !methodInfo.getDesc().contains(" ")) {
            return methodInfo.getMethodName();
        }
        return methodInfo.getDesc().split(" ")[0];
    }

    private void writeFieldInfo(Writer writer, FieldInfo info) throws IOException {
        writer.write(buildFieldStr(info));
        if (info.hasChildren()) {
            for (FieldInfo fieldInfo : info.getChildren()) {
                writeFieldInfo(writer, fieldInfo, getPrefix());
            }
        }
    }

    private String buildFieldStr(FieldInfo info) {
        return getFieldName(info) + "|" + info.getTypeText() + "|" + getRequireStr(info.isRequire()) + "|" + getRange(info.getRange()) + "|" + info.getDesc() + "\n";
    }

    private String getFieldName(FieldInfo info) {
        if (info.hasChildren()) {
            return "**" + info.getName() + "**";
        }
        return info.getName();
    }

    private void writeFieldInfo(Writer writer, FieldInfo info, String prefix) throws IOException {
        writer.write(prefix + buildFieldStr(info));
        if (info.hasChildren()) {
            for (FieldInfo fieldInfo : info.getChildren()) {
                writeFieldInfo(writer, fieldInfo, getPrefix() + prefix);
            }
        }
    }

    private String getPrefix() {
        String prefix = config.prefix;
        if (" ".equals(prefix)) {
            return "&emsp";
        }
        return prefix;
    }

    private String getRequireStr(boolean isRequire) {
        return isRequire ? "Y" : "N";
    }

    private String getRange(String range) {
        return AssertUtils.isEmpty(range) ? "N/A" : range;
    }

    public Model readPom(String pom) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            return reader.read(new FileReader(pom));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SneakyThrows
    public YApiProjectConfigInfo getProjectConfigInfo(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        String qualifiedName = containingClass.getQualifiedName();
        return getProjectConfigInfo(qualifiedName);
    }

    @SneakyThrows
    public YApiProjectConfigInfo getProjectConfigInfo(KtFunction ktFunction) {
        String qualifiedName = KtUtil.getFqName(ktFunction);
        return getProjectConfigInfo(qualifiedName);
    }

    @SneakyThrows
    public YApiProjectConfigInfo getProjectConfigInfo(String packageName) {
        YApiProjectConfigInfo selectedConfig = null;
        if (config.isMultiModule) {
            selectedConfig = getProjectInfoFromStorage(packageName);
            // 选择包名对应的YApi项目
            if (selectedConfig != null) {
                if (AssertUtils.isEmpty(selectedConfig.getToken())) {
                    if (!config.isUseDefaultToken) {
                        String message = MessageFormat.format(
                                "根据{0}【{1}】匹配到的模块配置项【{2}】没有配置YApi token;是否使用默认YApi token?" +
                                        "\n如果不想每次都提醒，可以在设置里·勾选· Is Use Default Token"
                                , config.matchWithModuleName ? "模块名" : "包名"
                                , config.matchWithModuleName ? selectedConfig.getModuleName() : selectedConfig.getPackageName()
                                , selectedConfig.getName()
                        );
                        int resultIdx = Messages.showOkCancelDialog(message,
                                "提示", "Ok", "Cancel", Messages.getQuestionIcon());
                        if (Messages.CANCEL == resultIdx) {
                            throw new BizException("Cancel upload YApi api!");
                        }
                    }
                    selectedConfig.setToken(config.projectToken);
                    selectedConfig.setProjectId(config.projectId);
                } else {
                    /**
                     * 修复v1.0.13及之前版本Bug: 配置模块token获取到YApi项目信息后未将projectId存入字段:{@link site.forgus.plugins.apigeneratorplus.config.YApiProjectConfigInfo#projectId}
                     */
                    if (AssertUtils.isEmpty(selectedConfig.getProjectId())) {
                        YApiProject project = selectedConfig.getProject();
                        if (!(null != project && null != project.get_id())) {
                            throw new BizException(
                                    MessageFormat.format("未查询到模块token对应项目ID，请重新配置token。\n当前模块配置名称：【{0}】token:【{1}】", selectedConfig.getName(), selectedConfig.getToken())
                            );
                        }
                        selectedConfig.setProjectId(String.valueOf(project.get_id()));
                        selectedConfig.setBasePath(project.getBasepath());
                        // 将修复后的配置持久化保存到xml文件中
                        for (YApiProjectConfigInfo yApiProjectConfigInfo : config.yApiProjectConfigInfoList) {
                            if (yApiProjectConfigInfo.getId().equals(selectedConfig.getId())) {
                                yApiProjectConfigInfo.setProjectId(selectedConfig.getProjectId());
                                yApiProjectConfigInfo.setBasePath(selectedConfig.getBasePath());
                            }
                        }
                    }
                }
                Assert.isTrue(AssertUtils.isNotEmpty(selectedConfig.getToken()) && AssertUtils.isNotEmpty(selectedConfig.getProjectId()),
                        "token或token对应项目id为空，请尝试重新配置token。若重新配置后还是无法解决，请向作者反馈问题！ https://github.com/buyili/api-generator-plus/issues"
                );
                if (AssertUtils.isEmpty(selectedConfig.getBasePath())) {
                    selectedConfig.setBasePath("");
                }
                return selectedConfig;
            }
        }
        selectedConfig = new YApiProjectConfigInfo();
        selectedConfig.setToken(config.projectToken);
        selectedConfig.setProjectId(config.projectId);
        selectedConfig.setBasePath("");
        return selectedConfig;
    }

    /**
     * 根据包名或模块名获取YApi项目配置
     *
     * @param packageName 包名
     * @return
     */
    @Nullable
    private YApiProjectConfigInfo getProjectInfoFromStorage(String packageName) {
        if (CollectionUtils.isNotEmpty(config.yApiProjectConfigInfoList)) {
            Boolean matchWithModuleName = config.matchWithModuleName;
            if (matchWithModuleName) {
                Module module = CurlUtils.getModule(editor, project);
                if (module == null) {
                    throw new BizException("Failed to get module name");
                }
                String moduleName = module.getName();
                for (YApiProjectConfigInfo yApiProjectConfigInfo : config.yApiProjectConfigInfoList) {
                    String dbModuleName = yApiProjectConfigInfo.getModuleName();
                    if (moduleName.equals(dbModuleName)) {
                        // 返回配置的克隆对象，免得修改返回对象属性后更改了源配置
                        return yApiProjectConfigInfo.clone();
                    }
                }

                // 模块名没有匹配时，弹出选择框手动选择上传项目
                int exitCode = ChooseYApiProjectDialog.showDialog(config.yApiProjectConfigInfoList);
                if (exitCode == -1) {
                    throw new BizException("Cancel upload YApi api!");
                }
                // 返回配置的克隆对象，免得修改返回对象属性后更改了源配置
                return config.yApiProjectConfigInfoList.get(exitCode).clone();
            } else {
                for (YApiProjectConfigInfo yApiProjectConfigInfo : config.yApiProjectConfigInfoList) {
                    String tempPackageName = yApiProjectConfigInfo.getPackageName();
                    if (StringUtils.isNotBlank(tempPackageName) && StringUtils.isNotBlank(packageName)
                            && packageName.startsWith(tempPackageName)) {
                        return yApiProjectConfigInfo.clone();
                    }
                }
                throw new BizException("Matching configuration failed based on package name");
            }
        }
        throw new BizException("There is no multi-module project configuration");
    }
}
