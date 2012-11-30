_spray-solr_ is a simple Solr DSL for [Scala] and [Spray].

### Installation

There is no repo for spray-solr, yet.
So just build it yourself from the sources here.
If you want to build it as part of another [Scala] project add:

	lazy val spraySolr = RootProject( uri("git://github.com/Bathtor/spray-solr.git") )

to your Build.scala and have your project dependOn(spraySolr).

### Usage

#### Setup

Create SolrService actor:
 	
 	val solrService = system.actorOf(
 		props = Props[SolrService],
 		name = "solr-service");

Don't forget that you need to have a spray-client running at `context.actorFor("../http-client")` (relative to the context of the SolrService).

#### Query

Create query "http://localhost:8983/solr/core1/select?q=test&wt=json&rows=10" with:

	val solrQuery = Solr("localhost", 8983, "core1").q("test").rows(10)()

or if you have com.typesafe.config.Config config file with something like

	test.solr {
		ip = "localhost"
		port = 8983
		core = "/solr/core1"
	}

and an akka.actor.ActorSystem with that config in scope:

	val solrQuery = Solr("test.solr").q("test").rows(10)()

Then send the request off to the SolrService instance (let's say we have it in scope in '''solrService''') and remember the the future (if doing ask):

	val solrResponseFuture = solrService ? solrQuery

Now say you expect the response from Solr to have two fields test1 and test2 with string values and you want those end up in a nice case class like:

	case class SolrTestResult(val test1: String, val test2: String)

You need to get a spray-json formatter for SolrTestResult into scope.
You can find out how to do that in the [SprayJson] documentation.

One you have that you can get the list of results:

	olrResponseFuture onSuccess {
		case solrResponse: SolrResults => {
			val testResults: List[SolrTestResult] = solrResponse.as[SolrTestResult]
			// do something with the results
		}
		case SolrError => {
			// handle the error
		}
	}

### API Documentation
You can find the scaladoc for _spray-solr_ here:
<http://bathtor.github.com/spray-solr/api>

### License

_spray-solr_ is licensed under [APL 2.0].

	[APL 2.0]: http://www.apache.org/licenses/LICENSE-2.0
	[Scala]: http://www.scala-lang.org/
	[Spray]: http://spray.io/
	[SprayJson]: http://github.com/spray/spray-json