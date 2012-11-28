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

import spray.json._
import scala.concurrent._
import java.net.URLEncoder
import com.typesafe.config.Config
import akka.actor.ActorSystem


class Solr(val host: String, val port: Int, val db: String) {
	
	var query = "*:*";
	var fields = "";
	val wt = "json";
	var start = 0;
	var rows = 5;
	
	def q(str: String): Solr = {
		query = str;
		
		return this;
	}
	
	def fl(str: String): Solr = {
		fields = str;
		
		return this;
	}
	
	def start(i: Int): Solr = {
		start = i;
		
		return this;
	}
	
	def rows(i: Int): Solr = {
		rows = 5;
		
		return this;
	}
	
	//def apply()(implicit format: JsonFormat[String]) = apply[String];
	
	def apply(): SolrQuery = {
		val url = constructQuery();
		//println("Sending Query " + url + " to SolrService");
		return SolrQuery(host, port, url);
	}
	
	private def constructQuery(): String = {
		val b: StringBuilder = new StringBuilder();
		b ++= db;
		b ++= "/select?";
		b ++= "q="+URLEncoder.encode(query, "UTF-8");
		b ++= "&wt="+URLEncoder.encode(wt, "UTF-8");
		b ++= "&fl="+URLEncoder.encode(fields, "UTF-8");
		b ++= "&start="+start;
		b ++= "&rows="+rows;
		
		return b.result;
	}
}

object Solr {
	def apply(host: String, port: Int, collection: String): Solr = {
		return new Solr(host, port, collection);
	}
	
	def apply(path: String, config: Config): Solr = {
		val ip = config.getString(path+".ip");
		val port = config.getInt(path+".port");
		val collection = config.getString(path+".collection");
		return apply(ip, port, collection);
	}
	
	def apply(path: String)(implicit system: ActorSystem): Solr = {
		return apply(path, system.settings.config);
	}
}