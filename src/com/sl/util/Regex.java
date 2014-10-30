package com.sl.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
	public static String find(String paramString1, String paramString2) {
		String str = "";
		if ((paramString1 != null) && (!paramString1.equals(""))) {
			Matcher localMatcher = Pattern.compile(paramString2).matcher(
					paramString1);
			if (localMatcher.find())
				str = localMatcher.group(0);
		}
		return str;
	}
}