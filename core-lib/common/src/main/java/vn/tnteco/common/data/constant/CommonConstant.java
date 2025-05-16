package vn.tnteco.common.data.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstant {

    public static final Integer ZERO_VALUE = 0;
    public static final Integer ONE_VALUE = 1;
    public static final Integer TWO_VALUE = 2;
    public static final Integer THREE_VALUE = 3;
    public static final String EMPTY_STRING = "";
    public static final String TRUE_STRING = "true";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommonSymbol {

        public static final String SPACE = " ";

        public static final String DOT = ".";

        public static final String BACKSLASH = "\\";

        public static final String COMMA = ",";

        public static final String DASH = "-";

        public static final String SHIFT_DASH = "_";

        public static final String COLON = ":";

        public static final String FORWARD_SLASH = "/";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContentDisposition {

        public static final String ATTACHMENT = "attachment";

    }
}
