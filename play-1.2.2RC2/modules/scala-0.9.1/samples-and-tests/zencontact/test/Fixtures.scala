
class FreshDatabase {
    
    import play._
    import play.test._
    
    import models._
    
    Logger.info("Apply fixture: FreshDatabase")
    
    Fixtures.deleteDatabase()
    
    Yaml[List[Contact]]("test-data.yml").foreach {
        Contact.create(_)
    }
    
}