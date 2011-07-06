/**
* stuffing a few Scala specific helpers into play namespace
*
*/

package object play {

    // -- CONFIGURATION

    def configuration = new RichConfiguration(play.Play.configuration)

    // - IMPLICITS
    implicit def withEscape(x: Any) = new WithEscape(x)

    implicit def domToXML(dom: org.w3c.dom.Document): scala.xml.Elem = {
        scala.xml.XML.loadString(play.libs.XML.serialize(dom))
    }

}
