package com.rsb.vpnpoc.util;

import java.util.regex.Pattern;

public class DomainUtils {
    private static Pattern pDomainNameOnly1;
    private static Pattern pDomainNameOnly2;

    private static final String DOMAIN_NAME_PATTERN_CHK_1 = "^(?![0-9-])[A-Za-z0-9-]{1,63}(?<!-)$";
    private static final String DOMAIN_NAME_PATTERN_CHK_2 = "^((?![0-9-])[A-Za-z0-9-]{1,63}(?<!-)\\.)+(?![0-9-])[A-Za-z0-9-]{1,63}(?<!-)$";

    static {
        pDomainNameOnly1 = Pattern.compile(DOMAIN_NAME_PATTERN_CHK_1);
        pDomainNameOnly2 = Pattern.compile(DOMAIN_NAME_PATTERN_CHK_2);
    }

    public static boolean isValidDomainName(String domainName) {
        return (pDomainNameOnly1.matcher(domainName).find() || pDomainNameOnly2.matcher(domainName).find() || domainName.equals(" "));
    }
}
