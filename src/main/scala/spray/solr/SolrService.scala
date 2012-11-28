/*
 * Copyright (C) 2012 Lars Kroll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.solr

import akka.actor.Actor
import spray.can.client.{ HttpDialog, HttpClient }
import spray.http._
//import scala.concurrent._
import scala.util.{Success, Failure}
import akka.event.Logging
import akka.actor.ActorLogging
import spray.json._
import spray.util._
import akka.pattern.ask

abstract class SolrResponse[T];
case class SolrReply[T](responseHeader: ResponseHeader, response: Response[T]) extends SolrResponse[T]
case class ResponseHeader(status: Int, QTime: Int, params: Map[String, String])
case class Response[T](numFound: Int, start: Int, docs: List[T])
case class EmptyReply[T](error: String) extends SolrResponse[T]

object SolrJsonProtocol extends DefaultJsonProtocol {
	implicit def responseFormat[A: JsonFormat] = jsonFormat3(Response[A]);
	implicit val headerFormat = jsonFormat3(ResponseHeader);
	implicit def solrFormat[A: JsonFormat] = jsonFormat2(SolrReply.apply[A]);
}

abstract class SolrServiceResponse {
	def as[T]()(implicit format: JsonFormat[T]): List[T];
}

case class SolrQuery(host: String, port: Int, content: String)
case class SolrResults(response: HttpEntity) extends SolrServiceResponse {
	import SolrJsonProtocol._
	
	override def as[T]()(implicit format: JsonFormat[T]): List[T] = {

		//println("Solr got SolrService response: " + response.asString);
		val jsonString = response.asString;
		val json = JsonParser(jsonString);
		val result = json.convertTo[SolrReply[T]];
		return result.response.docs;
	}
}
case object SolrError extends SolrServiceResponse {
	override def as[T]()(implicit format: JsonFormat[T]): List[T] = {
		return List.empty[T];
	}
}

class SolrService extends Actor with ActorLogging {

	val httpClient = context.actorFor("../http-client");

	def receive = {
		case SolrQuery(host, port, content) => {
			//println("Sending HTTP request to Solr...");
			val responseFuture = HttpDialog(httpClient, host, port)
				.send(HttpRequest(uri = content))
				.end;
			val replyTo = sender;
			responseFuture onComplete {
				case Success(response) => {
					//println("Got HTTP response:" + response);
					replyTo ! SolrResults(response.entity);
				}
				case Failure(error) => {
					log.error("Could not get response for {} due to {}", host + content, error);
					replyTo ! SolrError;
				}
			}
		}
	}
}