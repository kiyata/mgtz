package simulations.citation

import config.Configuration
import config.Configuration.{peakDuration, peakUsers, rampDownDuration}
import io.gatling.core.Predef.{exec, foreach, _}
import io.gatling.core.feeder.BatchableFeederBuilder
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import utils.Constants._
import scala.concurrent.duration._

class VotingTest extends Simulation {
  val programUsers: BatchableFeederBuilder[String] = csv("TestCoVoters2025.csv").circular
  val cookieId = "SESSbba4e82d2bad10f6f2efc841020b78f2"
  //val rampUpUsers: Int = Configuration.rampUpUsers
  val rampUpTime: Int = Configuration.rampUpDuration
  val concurrentUsers: Int = Configuration.peakUsers
  val duration: Int = Configuration.peakDuration
  val rampDownUsers: Int = Configuration.rampDownDuration
  val rampDownTime: Int = Configuration.rampDownDuration
  val citationsToAccept: List[Int] = List(1, 2, 5, 6)
  val citationsToReject: List[Int] = List(3, 4, 7, 8)
  val allCitations: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8)
  val citationIndices: Map[Int, Int] = Map(
    1 -> 2, 2 -> 3, 3 -> 4, 4 -> 5, 5 -> 7, 6 -> 8, 7 -> 9, 8 -> 10
  )
  val acceptButton = "hilited-button blue-button accept-button button js-form-submit form-submit"
  val headers: Map[String, String] = Map("cookie" -> "#{cookieId}=#{cookie}",
    "Content-Type" -> "application/json")

  val loginHeader: Map[String, String] = Map(
    "Content-Type" -> "application/x-www-form-urlencoded",
    "cookie" -> "_ga_Z3P8F9M7Q4=GS1.1.1740122183.1.1.1740122193.0.0.0; _ga=GA1.1.443345875.1740122183; _ga=GA1.4.443345875.1740122183; _gid=GA1.4.1969449847.1740122184; nmstat=a545196d-2c99-bee7-85ba-f49c48e7d10d"
  )

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(Configuration.url)
    .userAgentHeader(USER_AGENT)
    .inferHtmlResources()
    .silentResources

  val logout: ChainBuilder = exec(http("logout")
    .get("#{LogoutUrl}")
  )

  val login: ChainBuilder = tryMax(5)(
      exec(http("Go to login page")
      .get("#{LoginUrl}")
      .check(status.is(OK))
      .check(substring(DASHBOARD_TEXT).exists)
      .check(css("input[name='form_build_id']", "value").find.saveAs("formBuildId"))
      .check(css("input[name='form_id']", "value").find.saveAs("formId"))
    )
      .pause(PAUSE)
      .exec(http("submit credentials")
        .post(loginApi)
        .formParam("name", "#{UserName}")
        .formParam("pass", "#{Password}")
        .formParam("form_build_id", "#{formBuildId}")
        .formParam("form_id", "#{formId}")
        .formParam("op", OP_ID)
        .headers(loginHeader)
        .disableFollowRedirect
        .check(status.is(303))
      )
    .exec(http("check user logged in")
      .get("/user/#{UserId}")
      .queryParam("check_logged_in", 1)
      .check(status.is(OK))
      .check(substring(USER_INFO_TEXT).exists)
    )
  )
    .exitHereIfFailed


  val goToUserProgramPage: ChainBuilder = tryMax(5)(exec(http("Go to user program page")
    .get("/teks/admin/srp/v2/program/#{ProgramId}/voter/vote_number/0")
    .check(status.is(OK))
    .check(substring("#{Program}").exists)
    .check(substring("Program ID: #{ProgramId}").exists)
    .check(regex("/teks/admin/srp/program/\\d+/expectation/\\d+/correlation/\\d+/vote_number/0/all\\?type=narrative")
      .findAll.saveAs("correlationUrls"))
  )
  )
    .exitHereIfFailed

  def goToBreakoutPage(correlationUrl: String): ChainBuilder = tryMax(5)(exec(http("Go to breakout page")
    .get(correlationUrl)
    .check(css("input[name='form_build_id']", "value").findAll.saveAs("formBuildIds"))
    .check(css("input[name='form_token']", "value").findAll.saveAs("formTokens"))
    .check(css("input[name='form_id']", "value").findAll.saveAs("formIds"))
    .check(
      regex("/teks/admin/srp/reject_reason/program/\\d+/expectation/\\d+/correlation/\\d+/citation/\\d+")
        .findAll.saveAs("rejectUrls"))
    .check(regex("vote-status-(\\d+)").findAll.saveAs("citationNumbers"))
    .check(status.is(OK))
  )
  )
    .exitHereIfFailed


  def getParameters(citationSequence: Int): ChainBuilder = exec(
    exec { session =>
      val index: Int = citationIndices.getOrElse(citationSequence, 0)
      val formBuildId = session("formBuildIds").as[Seq[String]].apply(index)
      val formId = session("formIds").as[Seq[String]].apply(index)
      val formToken = session("formTokens").as[Seq[String]].apply(index)
      val citationNumber = session("citationNumbers").as[Seq[String]].apply(citationSequence - 1)
      val rejectUrl = session("rejectUrls").as[Seq[String]].apply(citationSequence - 1)
      session.set("formBuildId", formBuildId)
        .set("formId", formId)
        .set("formToken", formToken)
        .set("triggeringElementName", "accept_citation_" + citationNumber)
        .set("citationNumber", citationNumber)
        .set("citationIndex", citationSequence)
        .set("rejectUrl", rejectUrl)
    }
      .exitHereIfFailed
  )

  val voteAllCitations: ChainBuilder = exec(
    foreach("#{correlationUrls}", "correlationUrl", "correlationIndex") {
      exec(
        goToBreakoutPage("#{correlationUrl}")
          .exec(getParameters(1))
          .exec(acceptCitation())
          .exec(cancelVote())
          .exec(getParameters(2))
          .exec(acceptCitation())
          .exec(cancelVote())
          .exec(getParameters(3))
          .exec(rejectCitation())
          .exec(cancelVote())
          .exec(getParameters(4))
          .exec(rejectCitation())
          .exec(cancelVote())
          .exec(getParameters(5))
          .exec(acceptCitation())
          .exec(cancelVote())
          .exec(getParameters(6))
          .exec(acceptCitation())
          .exec(cancelVote())
          .exec(getParameters(7))
          .exec(rejectCitation())
          .exec(cancelVote())
          .exec(getParameters(8))
          .exec(rejectCitation())
          .exec(cancelVote())
      )
    }
  )

  def acceptCitation(): ChainBuilder = tryMax(5)(exec(
    http("Accept vote (citation # #{citationIndex})")
      .post("#{correlationUrl}")
      .queryParam("ajax_form", 1)
      .queryParam("_wrapper_format", "drupal_ajax")
      .formParam("form_build_id", "#{formBuildId}")
      .formParam("form_id", "#{formId}")
      .formParam("form_token", "#{formToken}")
      .formParam("_triggering_element_name", "#{triggeringElementName}")
      .formParam("_triggering_element_value", "Accept")
      .formParam("_drupal_ajax", 1)
      .formParam("ajax_page_state[theme]", "tea")
      .formParam("ajax_page_state[theme_token]", "")
      .formParam("ajax_page_state[libraries]", PAGE_STATE)
      .check(status.is(OK))
      .check(substring("Accepted").exists)

  ))
    .exitHereIfFailed


  def rejectCitation(): ChainBuilder = tryMax(5)(
    exec(http("Reject vote (citation # #{citationIndex})")
      .post("#{rejectUrl}?_wrapper_format=drupal_model")
      .formParam("js", true)
      .formParam("dialogOptions[width]", 600)
      .formParam("_drupal_ajax", 1)
      .formParam("ajax_page_state[theme]", "imra")
      .formParam("ajax_page_state[theme_token]", "")
      .formParam("ajax_page_state[libraries]", PAGE_STATE)
      .check(status.is(OK))
      .check(substring("Reason for rejection").exists)
      .check(regex("form_build_id\\u0022 value=\\u0022.{48}")
        .find
        .transform(string => string.replace("form_build_id\" value=\"",""))
        .saveAs("rejectionFormBuildId"))
      .check(regex("form_token\\u0022 value=\\u0022.{43}")
        .find
        .transform(string => string.replace("form_token\" value=\"",""))
        .saveAs("rejectionFormToken"))
    )
      .exitHereIfFailed

      .pause(PAUSE)
      .tryMax(5)(
      exec(http("Rejection reason (citation #{citationIndex})")
        .post("#{rejectUrl}")
        .queryParam("_wrapper_format", "drupal_model")
        .queryParam("ajax_form", 1)
        .queryParam("_wrapper_format", "drupal_ajax")
        .formParam("reason", "This is a sample rejection.")
        .formParam("form_build_id", "#{rejectionFormBuildId}")
        .formParam("form_id", "tea_teks_srp_srp_vote_rejection_reason")
        .formParam("form_token", "#{rejectionFormToken}")
        .formParam("_triggering_element_name", "op")
        .formParam("_triggering_element_value", "Record Rejection")
        .formParam("_drupal_ajax", 1)
        .formParam("ajax_page_state[theme]", "imra")
        .formParam("ajax_page_state[theme_token]", "")
        .formParam("ajax_page_state[libraries]", PAGE_STATE)
        .check(status.is(OK))
        .check(substring("Rejected").exists)
      )
  )
  )

  def cancelVote(): ChainBuilder = tryMax(5)(
    exec(http("Cancel vote (citation #{citationIndex})")
      .post("#{correlationUrl}")
      .queryParam("ajax_form", "1")
      .queryParam("_wrapper_format", "drupal_ajax")
      .formParam("form_build_id", "${formBuildId}")
      .formParam("form_id", "#{formId}")
      .formParam("form_token", "#{formToken}")
      .formParam("_triggering_element_name", "cancelCitation_#{citationNumber}")
      .formParam("_triggering_element_value", "Cancel Vote")
      .formParam("_drupal_ajax", 1)
      .formParam("ajax_page_state[theme]", "imra")
      .formParam("ajax_page_state[theme_token]", "")
      .formParam("ajax_page_state[libraries]", PAGE_STATE)
      .check(status.is(OK))
    )
  )

  val VotingScenario: ScenarioBuilder = scenario("SRP voting")
    .feed(programUsers)
    .exitBlockOnFail {
      exec(login)
    }
    .exec(goToUserProgramPage, voteAllCitations)

  setUp(
    VotingScenario.inject(
      rampConcurrentUsers(0).to(250) during 10.minutes,
      constantConcurrentUsers(250) during 20.minutes,
      rampConcurrentUsers(250) to 0 during 10.minutes,
    ).protocols(httpProtocol),
  ).assertions(
    global.responseTime.max.lt(60000),
    global.successfulRequests.percent.gt(80)
  )
}