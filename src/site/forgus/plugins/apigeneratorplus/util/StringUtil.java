package site.forgus.plugins.apigeneratorplus.util;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocLink;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag;
import com.intellij.psi.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtParameter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lmx 2020/11/12 17:05
 */

public class StringUtil {

    private static int nameNumber = 0;

    public static String getName() {
        nameNumber = nameNumber + 1;
        return "Unnameed (" + nameNumber + ")";
    }

    @NotNull
    @Contract(pure = true)
    public static String getPackageName(@NotNull String fqName) {
        return getPackageName(fqName, '.');
    }

    @NotNull
    @Contract(pure = true)
    public static String getPackageName(@NotNull String fqName, char separator) {
        int lastPointIdx = fqName.lastIndexOf(separator);
        if (lastPointIdx >= 0) {
            return fqName.substring(0, lastPointIdx);
        }
        return "";
    }

    public static void showPsiNameValuePair(PsiNameValuePair psiNameValuePair) {
        System.out.println(psiNameValuePair.getName());
        System.out.println(psiNameValuePair.getValue());
        System.out.println(psiNameValuePair.getValue().getText());
    }

    public static void showPsiMethod(PsiMethod psiMethod) {
//        Gson gson = new Gson();
        System.out.println(psiMethod.getName());
        System.out.println("PsiAnnotation annotation----------------");
        for (PsiAnnotation annotation : psiMethod.getModifierList().getAnnotations()) {
            //结果示例 @RequestMapping(value = "/test", method = RequestMethod.GET)
            System.out.println(annotation.getText());
        }
        System.out.println("PsiAnnotation annotation----------------end");
        System.out.println("PsiParameter parameter----------------");
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            //结果示例： QueryDto queryDto
            System.out.println(parameter.getText());
            PsiModifierList modifierList = parameter.getModifierList();
            if (modifierList != null) {
                for (PsiAnnotation annotation : modifierList.getAnnotations()) {
                    System.out.println("annotation text:                " + annotation.getText());
                }
            }
        }
        System.out.println("PsiParameter annotation----------------end");
    }

    public static void showKtParameter(KtParameter ktParameter) {
        System.out.println("KtParameter");

        System.out.println("ktParameter.getText()");
        System.out.println(ktParameter.getText());
        System.out.println();

        System.out.println("ktParameter.getTypeReference().getText()");
        System.out.println(ktParameter.getTypeReference().getText());
        System.out.println();

        System.out.println("ktParameter.getText()");
        System.out.println(ktParameter.getText());
        System.out.println();

    }


    public static void testTag(PsiDocTag tag) {

        String replace = tag.getText().replace("@res_body", "");
        String desc = tag.getText().replace("@res_body", "").replaceAll("\n *\\*", "\n");
        String text = tag.getText();
        PsiDocTagValue valueElement = tag.getValueElement();
        if (valueElement != null) {
            String text1 = valueElement.getText();
            System.out.println();
        }
        PsiElement[] dataElements = tag.getDataElements();
        for (PsiElement dataElement : dataElements) {
            String text1 = dataElement.getText();
            System.out.println();
        }
    }

    public static void testTag(KDocTag kDocTag) {
        String name = kDocTag.getName();
        String subjectName = kDocTag.getSubjectName();
        KDocLink subjectLink = kDocTag.getSubjectLink();
        if (subjectLink != null) {
            String linkText = subjectLink.getLinkText();
            System.out.println();
        }
        String content = kDocTag.getContent();
    }

    public static void testDoc(PsiDocComment psiDocComment) {
        if (psiDocComment != null) {
            String text1 = psiDocComment.getText();
//            String regex = "\\\\\\*\\*\n( *?)\\*";
            String regex = "/\\*\\*\\n( *?)\\*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text1);
            if (matcher.find()) {
                String group = matcher.group(1);
                System.out.println(group);
            }
            PsiElement[] descriptionElements = psiDocComment.getDescriptionElements();
            String str = "";
            for (PsiElement descriptionElement : descriptionElements) {
                String text = descriptionElement.getText();
                boolean notBlank = StringUtils.isNotBlank(text);
                str = str + text;
                System.out.println();
            }
            PsiDocTag docTag = psiDocComment.findTagByName("description");
            if (null != docTag) {
                String text = docTag.getText();
                System.out.println(text);
            }
            PsiDocTag docTag1 = psiDocComment.findTagByName("Description");
            if (null != docTag1) {
                String text = docTag1.getText();
                System.out.println(text);
            }
        }
    }

    public static void testDoc(KDoc kDoc) {
        if (kDoc != null) {
            String content = kDoc.getDefaultSection().getContent();
            String name = kDoc.getDefaultSection().getName();
            String subjectName = kDoc.getDefaultSection().getSubjectName();
            KDocTag description = kDoc.getDefaultSection().findTagByName("description");
            if (description != null) {
                String descriptionContent = description.getContent();
                System.out.println();
            }
            KDocTag description1 = kDoc.getDefaultSection().findTagByName("Description");
            if (description1 != null) {
                String descriptionContent = description1.getContent();
                System.out.println();
            }
            System.out.println();
        }
    }
}
