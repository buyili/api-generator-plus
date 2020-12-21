package site.forgus.plugins.apigeneratorplus.util;

import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag;
import org.jetbrains.kotlin.psi.KtFunction;

import java.util.List;
import java.util.Objects;

/**
 * 描述工具, 用于获取各种注释
 */
public class DesUtil {


    /**
     * 去除字符串首尾出现的某个字符.
     *
     * @param source  源字符串.
     * @param element 需要去除的字符.
     * @return String.
     */
    private static String trimFirstAndLastChar(String source, char element) {
        boolean beginIndexFlag;
        boolean endIndexFlag;
        do {
            if (Strings.isNullOrEmpty(source.trim()) || source.equals(String.valueOf(element))) {
                source = "";
                break;
            }
            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;
            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();
            source = source.substring(beginIndex, endIndex);
            beginIndexFlag = (source.indexOf(element) == 0);
            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());
        } while (beginIndexFlag || endIndexFlag);
        return source;
    }

    /**
     * 获得描述
     *
     * @param psiMethodTarget the psi method target
     * @return the description
     */
    public static String getDescription(PsiMethod psiMethodTarget) {
        return getDescription(psiMethodTarget.getDocComment());
    }

    public static String getDescription(PsiDocComment psiDocComment) {
        if (psiDocComment != null) {
            PsiDocTag[] psiDocTags = psiDocComment.getTags();
            for (PsiDocTag psiDocTag : psiDocTags) {
                if (psiDocTag.getText().contains("@description") || psiDocTag.getText().contains("@Description") || psiDocTag.getText().toLowerCase().contains("description")) {
                    return trimFirstAndLastChar(psiDocTag.getText().replace("@description", "").replace("@Description", "").replace("Description", "").replace("<br>", "").replace(":", "").replace("*", "").replace("\n", " "), ' ');
                }
            }
            return trimFirstAndLastChar(
                    psiDocComment.getText().split("@")[0]
                            .replace("@description", "")
                            .replace("@Description", "")
                            .replace("Description", "")
                            .replace("<br>", "\n")
                            .replace(":", "")
                            .replace("*", "")
                            .replace("/", "")
                            .replace("\n", " ")
                            .replace("<p>", "\n")
                            .replace("</p>", "\n")
                            .replace("<li>", "\n")
                            .replace("</li>", "\n")
                            .replace("{", ""), ' '
            );
        }
        return null;
    }

    /**
     * 获得描述
     *
     * @param ktFunction the psi method target
     * @return the description
     */
    public static String getDescription(KtFunction ktFunction) {
        return getDescription(ktFunction.getDocComment());
    }

    public static String getDescription(KDoc kDoc) {
        if (kDoc != null) {
            List<KDocTag> descriptionTags = kDoc.getDefaultSection().findTagsByName("description");
            for (KDocTag descriptionTag : descriptionTags) {
                return trimFirstAndLastChar(descriptionTag.getText().replace("@description", "")
                        .replace("@Description", "").replace("Description", "")
                        .replace("<br>", "").replace(":", "").replace("*", "").replace("\n", " "), ' ');
            }
//            PsiDocTag[] psiDocTags = kDoc.getTags();
//            for (PsiDocTag psiDocTag : psiDocTags) {
//                if (psiDocTag.getText().contains("@description") || psiDocTag.getText().contains("@Description") || psiDocTag.getText().toLowerCase().contains("description")) {
//                    return trimFirstAndLastChar(psiDocTag.getText().replace("@description", "").replace("@Description", "").replace("Description", "").replace("<br>", "").replace(":", "").replace("*", "").replace("\n", " "), ' ');
//                }
//            }
            return trimFirstAndLastChar(
                    kDoc.getText().split("@")[0]
                            .replace("@description", "")
                            .replace("@Description", "")
                            .replace("Description", "")
                            .replace("<br>", "\n")
                            .replace(":", "")
                            .replace("*", "")
                            .replace("/", "")
                            .replace("\n", " ")
                            .replace("<p>", "\n")
                            .replace("</p>", "\n")
                            .replace("<li>", "\n")
                            .replace("</li>", "\n")
                            .replace("{", ""), ' '
            );
        }
        return null;
    }

    /**
     * 通过paramName 获得描述.
     *
     * @param psiMethodTarget the psi method target
     * @param paramName       the param name
     * @return the param desc
     */
    public static String getParamDesc(PsiMethod psiMethodTarget, String paramName) {
        if (psiMethodTarget.getDocComment() != null) {
            PsiDocTag[] psiDocTags = psiMethodTarget.getDocComment().getTags();
            for (PsiDocTag psiDocTag : psiDocTags) {
                if ((psiDocTag.getText().contains("@param") || psiDocTag.getText().contains("@Param")) && (!psiDocTag.getText().contains("[")) && psiDocTag.getText().contains(paramName)) {
                    return trimFirstAndLastChar(psiDocTag.getText().replace("@param", "").replace("@Param", "").replace(paramName, "").replace(":", "").replace("*", "").replace("\n", " "), ' ');
                }
            }
        }
        return "";
    }

    /**
     * 获得属性注释
     *
     * @param psiDocComment the psi doc comment
     * @return the filed desc
     */
    private static String getFiledDesc(PsiDocComment psiDocComment) {
        if (Objects.nonNull(psiDocComment)) {
            String fileText = psiDocComment.getText();
            if (!Strings.isNullOrEmpty(fileText)) {
                return trimFirstAndLastChar(fileText.replace("*", "").replace("/", "").replace(" ", "").replace("\n", ",").replace("\t", ""), ',').split("\\{@link")[0];
            }
        }
        return "";
    }

    /**
     * 获得link 备注
     *
     * @param remark  the remark
     * @param project the project
     * @param field   the field
     * @return the link remark
     */
    public static String getLinkRemark(String remark, Project project, PsiField field) {
        // 尝试获得@link 的常量定义
        if (Objects.isNull(field.getDocComment())) {
            return remark;
        }
        String[] linkString = field.getDocComment().getText().split("@link");
        if (linkString.length > 1) {
            //说明有link
            String linkAddress = linkString[1].split("}")[0].trim();
            PsiClass psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress, GlobalSearchScope.allScope(project));
            if (Objects.isNull(psiClassLink)) {
                //可能没有获得全路径，尝试获得全路径
                String[] importPaths = field.getParent().getContext() != null ? field.getParent().getContext().getText().split("import") : new String[0];
                if (importPaths.length > 1) {
                    for (String importPath : importPaths) {
                        if (importPath.contains(linkAddress.split("\\.")[0])) {
                            linkAddress = importPath.split(linkAddress.split("\\.")[0])[0] + linkAddress;
                            psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress.trim(), GlobalSearchScope.allScope(project));
                            break;
                        }
                    }
                }
                //如果小于等于一为不存在import，不做处理
            }
            if (Objects.nonNull(psiClassLink)) {
                //说明获得了link 的class
                PsiField[] linkFields = psiClassLink.getFields();
                if (linkFields.length > 0) {
                    remark += "," + psiClassLink.getName() + "[";
                    StringBuilder remarkBuilder = new StringBuilder(remark);
                    for (int i = 0; i < linkFields.length; i++) {
                        PsiField psiField = linkFields[i];
                        if (i > 0) {
                            remarkBuilder.append(",");
                        }
                        // 先获得名称
                        remarkBuilder.append(psiField.getName());
                        // 后获得value,通过= 来截取获得，第二个值，再截取;
                        String[] splitValue = psiField.getText().split("=");
                        if (splitValue.length > 1) {
                            String value = splitValue[1].split(";")[0];
                            remarkBuilder.append(":").append(value);
                        }
                        String filedValue = DesUtil.getFiledDesc(psiField.getDocComment());
                        if (!Strings.isNullOrEmpty(filedValue)) {
                            remarkBuilder.append("(").append(filedValue).append(")");
                        }
                    }
                    remark = remarkBuilder.toString();
                    remark += "]";
                }
            }
        }
        return remark;
    }
}
