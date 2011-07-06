package play

/**
* wraps around a java property
*/
class RichConfiguration(val conf: java.util.Properties) {
    def apply(key: String) = conf.getProperty(key)
    def apply(key: String, default: String) = conf.getProperty(key, default)
}

/**
* provides auto-escaping
*/
class WithEscape(val x: Any) {
    def escape = org.apache.commons.lang.StringEscapeUtils.escapeHtml(x.toString)
    def escapeHtml = escape
}

