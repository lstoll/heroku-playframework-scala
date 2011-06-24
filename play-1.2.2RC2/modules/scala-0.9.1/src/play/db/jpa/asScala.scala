package play.db.jpa

import play.data.validation.Validation

import scala.collection.mutable

/**
 * Provides Query functionality for java models
 * in the form of asScala[Model].<query methods>
 * or on an instance method like new User("sdfd").asScala[User].save()
 **/
object asScala {

    private var queries = new mutable.HashMap[String, QueryHolder[_]]

    private def memoize[T <: JPABase](key: String): QueryHolder[T] = {
        println("key:" + key)
        if (!queries.contains(key)) {
            queries += (key) -> (new QueryHolder[T])
        }
        queries(key).asInstanceOf[QueryHolder[T]]
    }
  
    /**
     * Provides chainable methods for java models
     */
    class JavaModelWrapper(underlying: JPABase) {
        
        class Holder[T <: JPABase] {
          
            def save(): T = {
                underlying._save()
                underlying.asInstanceOf[T]
            }

            def delete(): T = {
                underlying._delete()
                underlying.asInstanceOf[T]
            }

            def refresh(): T = {
                JPA.em().refresh(underlying)
                underlying.asInstanceOf[T]
            }

            def merge(): T = {
                JPA.em().merge(underlying)
                underlying.asInstanceOf[T]
            }

            def validateAndSave(): Boolean = {
                if (Validation.current().valid(this).ok) {
                    underlying._save()
                    true
                } else {
                    false
                }
            }
        }
        
        def asScala[T <: JPABase] = new Holder[T]
    }

    class QueryHolder[T <: JPABase] extends QueryOn[T]

    implicit def enrichJavaModel(underlying: JPABase) = new JavaModelWrapper(underlying)

    def apply[T <: JPABase](implicit m: scala.reflect.Manifest[T]): QueryHolder[T] = memoize[T](m.erasure.toString)

    def asList[T](jlist: java.util.List[T]): List[T] = {

        import scala.collection.mutable.ListBuffer

        val buffer = ListBuffer[T]()
        
        for (e <- jlist.toArray) {
            buffer += e.asInstanceOf[T]
        }
        buffer.toList
    }
  
}

