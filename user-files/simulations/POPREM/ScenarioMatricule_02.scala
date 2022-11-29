package poprem
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps



class ScenarioMatricule_02 extends  Simulation{

  private val  host : String =System.getProperty("urlCible", "https://poprem.pp.ecom-internal.gl.rocks")
  private val  VersionAppli: String  = System.getProperty("VersionApp", "Vxx.xx.xx")
  private val  TpsMonteEnCharge: Int = System.getProperty("tpsMonte", "2").toInt
  private val  DureeMax : Int = System.getProperty("dureeMax", "5").toInt + TpsMonteEnCharge
  private val  LeCoeff : Int = System.getProperty("coeff", "1").toInt
  private val NbrIter: Int = System.getProperty("nbIter", "10").toInt
  private val  nbVu : Int = 1 * LeCoeff




  val FichierPath: String = System.getProperty("dataDir", "/gatling-poprem/user-files/resources/data")
  val FichierDataMatricule: String = "matricule.csv"
  val FichierDataTempory: String = "temporality.csv"
  val FichierDataSalesUnitld: String = "salesUnitId.csv"
  val FichierDataorgLevelCode: String = "orgLevelCode.csv"

  val matricule = csv(FichierPath + FichierDataMatricule).circular.eager

  val tempory = csv(FichierPath + FichierDataTempory).circular.eager

  val salesUnitId = csv(FichierPath + FichierDataSalesUnitld).circular.eager

  val orgLevelCode = csv(FichierPath + FichierDataorgLevelCode).circular.eager


  val httpProtocol = http.baseUrl(host)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("TESTS-DE-PERF")


  def Autentication() =
  {
    exec(http("POST")
      .post("https://ssotest.interne.galerieslafayette.com/auth/realms/GL-ENTREPRISE/protocol/openid-connect/token")
      .formParam("client_id", "poprem-pp-front")
      // .formParam("client_secret", "${client_secret}")
      .formParam("username", "p_portapve")
      .formParam("password", "gl224434")
      .formParam("grant_type", "password")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("access_token")))
  }

  def getparameters(): ChainBuilder = {
    repeat(NbrIter)
    {
      exec(http("Get_parameters")
        .get("/api/parameters")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200)))
    }
  }

  def Get_employees_matricules(): ChainBuilder = {
    repeat(NbrIter)
    {
      exec(http("Get_employees_matriculates")
        .get("/api/employees/${matricule}")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200)))
    }
  }
    def Get_employees_matricules_peers():ChainBuilder  = {
      repeat(NbrIter)
      {
        exec(http("Get_employees_matricules_peers")
          .get("/api/employees/${matricule}/peers")
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer ${access_token}")
          .check(status.is(200)))
      }

    }

  def Get_employees_salesUnitId(): ChainBuilder = {
    repeat(NbrIter)
    {
      exec(http("Get_employees_salesUnitId")
        .get("/api/performance/sales-units/${salesUnitId}/temporality/${temporality}")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200))
      )
    }

  }

  def Get_employees_orgLevelCode(): ChainBuilder = {
    exec(http("Get_employees_orgLevelCode")
      .get("/api/performance/teams/${orgLevelCode}/temporality/${temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${access_token}")
      .check(status.is(200)))
  }

  def Get_employees_today(): ChainBuilder = {
      exec(http("Get_employees_today")
        .get("/api/performance/employees/${matricule}/temporality/${temporality}")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${access_token}")
        .check(status.is(200))
      )
  }

def Get_scenario1_apppelfrequents(): ChainBuilder =
  {

    exec(http("Get_matricule_temporality")
      .get("/api/performance/employees/${matricule}/temporality/${temporality}")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${access_token}")
      .check(status.is(200)))
      .pause(5)

    exec(http("Get_matricule_today_scenario1")
      .get("/api/performance/employees/${matricule}/temporality/today")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${access_token}")
      .check(status.is(200)))
      .pause(5)

    exec(http("Get_matricule_yesterday_scenario1")
      .get("/api/performance/employees/${matricule}/temporality/yesterday")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${access_token}")
      .check(status.is(200)))
      .pause(5)

    exec(http("Get_matricule_week_scenario1")
      .get("/api/performance/employees/${matricule}/temporality/week")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${access_token}")
      .check(status.is(200)))
      .pause(5)

    exec(http("Get_matricule_month_scenario1")
      .get("/api/performance/employees/${matricule}/temporality/month")
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer ${access_token}")
      .check(status.is(200)))
  }


      exec {
        session =>
          println(session)
          session
      }



  val scenarioMatricule  = scenario("TEST_PERF_POPREM")
    .exec(Autentication())
    .pause(5)
        .feed(matricule)
        .feed(tempory)
        .feed(salesUnitId)
        .feed(orgLevelCode)
        .exec(getparameters())
         .pause(5)
         .exec(Get_employees_matricules())
         .pause(5)
         .exec(Get_employees_matricules_peers())
          .pause(5)
          .exec(Get_employees_today())
         .pause(5)
          .exec(Get_employees_salesUnitId())
         .pause(5)
         .exec(Get_employees_orgLevelCode())
       .pause(5)
    .exec(Get_scenario1_apppelfrequents())

  setUp(
    scenarioMatricule.inject(rampUsers(nbVu) during (TpsMonteEnCharge minutes)))
    .protocols(httpProtocol)
    .maxDuration(DureeMax minutes)

}
