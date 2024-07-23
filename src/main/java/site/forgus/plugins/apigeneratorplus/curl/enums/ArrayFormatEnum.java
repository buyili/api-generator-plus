package site.forgus.plugins.apigeneratorplus.curl.enums;

import java.util.Arrays;

/**
 * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'indices' })
 * // 'a[0]=b&a[1]=c'
 * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'brackets' })
 * // 'a[]=b&a[]=c'
 * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'repeat' })
 * // 'a=b&a=c'
 * qs.stringify({ a: ['b', 'c'] }, { arrayFormat: 'comma' })
 * // 'a=b,c'
 */
public enum ArrayFormatEnum {

    indices,

    brackets,

    repeat,

    comma;

    public static String[] names(){
        return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
    }

}
