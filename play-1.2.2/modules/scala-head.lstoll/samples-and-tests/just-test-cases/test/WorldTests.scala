import play._
import play.test._

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._


class WorldTests extends UnitFlatSpec with ShouldMatchers {

    // Load the World (check test/world.sql)
    Fixtures.deleteDatabase
    Fixtures.executeSQL(Play.getFile("test/world.sql"))

    import play.db.anorm._

    it should "Execute SQL requests" in {

        SQL("Select 1").execute() should be (true)

        SQL("delete from City where id = 99").executeUpdate() should be (1)
        evaluating {
            SQL("delete from Country where id = 99").executeUpdate()
        } should produce [java.sql.SQLException]

        SQL(
            """
                select * from Country c
                join CountryLanguage l on l.CountryCode = c.Code
                where c.code = 'FRA';
            """
        )

        SQL(
            """
                select * from Country c
                join CountryLanguage l on l.CountryCode = c.Code
                where c.code = {countryCode};
            """
        ).on("countryCode" -> "FRA")

        SQL(
            """
                select * from Country c
                join CountryLanguage l on l.CountryCode = c.Code
                where c.code = {countryCode};
            """
        ).onParams("FRA")

    }

    it should "Retrieve data using the Stream API" in {

        val selectCountries = SQL("Select * from Country")

        val countries = selectCountries().map(row =>
            row[String]("code") -> row[String]("name")
        ).toList

        countries.size should be (239)
        countries(72) should be ("FRA" -> "France")

        val firstRow = SQL("Select count(*) as c from Country").apply().head

        firstRow[Long]("c") should be (239)

    }

    it should "Use Pattern Matching" in {

        case class SmallCountry(name:String)
        case class BigCountry(name:String)
        case class France()

        val countries = SQL("Select name,population from Country")().collect {
            case Row("France", _) => France()
            case Row(name:String, pop:Int) if(pop > 1000000) => BigCountry(name)
            case Row(name:String, _) => SmallCountry(name)
        }

        countries(0) should be (SmallCountry("Aruba"))
        countries(50) should be (BigCountry("Costa Rica"))
        countries(72) should be (France())

        countries.size should be (239)

    }

    it should "Deal with Nullable columns" in {

        val results = SQL("Select name,indepYear from Country")().collect {
            case Row(name:String, Some(year:Short)) => name -> year
        }

        results.size should be (192)
        results(0) should be ("Afghanistan" -> 1919)

        val error = evaluating {
            SQL("Select name,indepYear from Country")().map { row =>
                row[String]("name") -> row[Short]("indepYear")
            }
        } should produce [RuntimeException]

        error.getMessage should equal ("UnexpectedNullableFound(COUNTRY.INDEPYEAR)")

        val all = SQL("Select name,indepYear from Country")().map { row =>
            row[String]("name") -> row[Option[Short]]("indepYear")
        }

        all.size should be (239)
        all(0) should be ("Aruba" -> None)
        all(110) should be ("Kazakstan" -> Some(1991))

    }

    import play.db.anorm.SqlParser._

    it should "Use the Parser combinator API" in {

        SQL("select count(*) from Country").as(scalar[Long]) should be (239)

        val populations:List[String~Int] = {
            SQL("select * from Country").as( str("name") ~< int("population") * )
        }

        populations.size should be (239)
        populations(72) should be (new ~("France", 59225700))

        val populations2:List[String~Int] = {
            SQL("select * from Country").as('name.of[String]~<'population.of[Int]*)
        }

        populations2.size should be (239)
        populations2(72) should be (new ~("France", 59225700))

        val populations3:List[String~Int] = {
            SQL("select * from Country").as(
                get[String]("name") ~< get[Int]("population") *
            )
        }

        populations3.size should be (239)
        populations3(72) should be (new ~("France", 59225700))

        val error = evaluating {
            val populations4:String~Int = {
                SQL("select * from Country").as( str("name") ~< int("population") )
            }
        } should produce [RuntimeException]

        error.getMessage should be ("end of input expected")

        val population4:String~Int = {
            SQL("select * from Country").parse( str("name") ~< int("population") )
        }

        population4 should be (new ~("Aruba", 103000))

        case class SpokenLanguages(country:String, languages:Seq[String])

        val spokenLanguages = { countryCode:String =>
            SQL(
                """
                    select c.name, c.code, l.language from Country c
                    join CountryLanguage l on l.CountryCode = c.Code
                    where c.code = {code};
                """
            )
            .on("code" -> countryCode)
            .as(
                str("name") ~< spanM(by=str("code"), str("language")) ^^ {
                    case country~languages => SpokenLanguages(country, languages)
                } ?
            )
        }

        spokenLanguages("FRA") should be (Some(SpokenLanguages("France",List("Arabic", "French", "Italian", "Portuguese", "Spanish", "Turkish"))))

        case class SpokenLanguagesWithOfficial(
            country:String,
            officialLanguage: Option[String],
            otherLanguages:Seq[String]
        )

        val spokenLanguagesWithOfficial = { countryCode:String =>
            SQL(
                """
                    select * from Country c
                    join CountryLanguage l on l.CountryCode = c.Code
                    where c.code = 'FRA';
                """
            ).as(
                str("name") ~< spanM(
                    by=str("code"), str("language") ~< str("isOfficial")
                ) ^^ {
                    case country~languages =>
                        SpokenLanguagesWithOfficial(
                            country,
                            languages.collect { case lang~"T" => lang } headOption,
                            languages.collect { case lang~"F" => lang }
                        )
                } ?
            )
        }

        spokenLanguagesWithOfficial("FRA") should be (Some(SpokenLanguagesWithOfficial("France",Some("French"),List("Arabic", "Italian", "Portuguese", "Spanish", "Turkish"))))

    }

    it should "Add some Magic[T]" in {

        SQL("select * from Country").as(Country*).size should be (239)

        Country.count().single() should be (239)
        Country.count("population > 1000000").single() should be (154)
        Country.find().list().size should be (239)
        Country.find("population > 1000000").list().size should be (154)
        Country.find("code = {c}").on("c" -> "FRA").first() should be (Some(Country(Id("FRA"), "France", 59225700, Some("Jacques Chirac"))))

        Country.update(Country(Id("FRA"), "France", 59225700, Some("Nicolas S.")))

        Country.find("code = {c}").on("c" -> "FRA").first() should be (Some(Country(Id("FRA"), "France", 59225700, Some("Nicolas S."))))

        val Some(capital~country~languages) = SQL(
            """
                select * from Country c
                join CountryLanguage l on l.CountryCode = c.Code
                join City v on v.id = c.capital
                where c.code = {code}
            """
        ).on("code" -> "FRA").as( City ~< Country ~< Country.span(CountryLanguage*) ? )

        capital should be (City(Id(2974), "Paris"))
        country should be (Country(Id("FRA"), "France", 59225700, Some("Nicolas S.")))
        languages should be (List(CountryLanguage("Arabic","F"), CountryLanguage("French","T"), CountryLanguage("Italian","F"), CountryLanguage("Portuguese","F"), CountryLanguage("Spanish","F"), CountryLanguage("Turkish","F")))

        val officialLanguage = languages.collect {
                                   case CountryLanguage(lang, "T") => lang
                               }.headOption.getOrElse("No language?")

        officialLanguage should be ("French")
    }

}

import play.db.anorm._
import play.db.anorm.SqlParser._
import defaults._
object CountryLanguage extends Magic[CountryLanguage]
object City extends Magic[City]
object Country extends Magic[Country]

case class Country(code:Id[String], name:String, population:Int, headOfState:Option[String])
case class City(id:Pk[Int], name: String)
case class CountryLanguage(language:String, isOfficial:String)

