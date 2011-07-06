package play.db.jpa

import javax.persistence._
import java.lang.annotation.Annotation
import play.data.validation.Validation

/**
* this class wraps around the the basic JPA model implementation.
* it was really needed due to the differences on how java and scala are handling fluid APIs
* this is made available via type alias
*/
@MappedSuperclass
@deprecated
private[jpa] class ScalaModel extends JPABase {

    /**
     * holds entity managers
     */
    def em() = JPA.em()

    /**
     * refreshes current instance
     * @return current type
     */
    def refresh(): this.type = {
        em().refresh(this)
        this
    }

    /**
     * merges current instance
     * @return current type
     */
    def merge(): this.type = {
        em().merge(this)
        this
    }

    /**
     * saves current instance
     * @return current type
     */
     def save(): this.type = {
         _save()
         this
     }

    /**
     * deletes current instance
     * @return current type
     */
    def delete(): this.type = {
        _delete()
        this
    }

    /**
     * edit current instance, this is mainly used by CRUD. Apps are usually using save.
     * @param name name
     * @param params parameters
     * @return current type
     */
    def edit(name: String, params: java.util.Map[String,Array[String]]): this.type = {
        GenericModel.edit(this, name, params, Array[Annotation]())
        this
    }

    /**
     * valides before saving
     * @return true if validation and saving were sucessfull, otherwise returns false
     */
    def validateAndSave(): Boolean = {
        if (Validation.current().valid(this).ok) {
            _save()
            true
        } else {
            false
        }
    }

    @Id @GeneratedValue var id:Long = _

    def getId():Long = id

}
