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

/**
 * Constructs a Solr query.
 * 
 * Method names correspond to Solr url parameters.
 * See [[http://wiki.apache.org/solr/SolrQuerySyntax]] for Solr usage.
 */
class Solr(val host: String, val port: Int, val core: String) {
	
	private var query = "*:*";
	private var fields = "";
	private val wt = "json";
	private var start = 0;
	private var rows = 5;
	
	/**
	 * Gives a data-import url instead of a select url.
	 */
	def dataimport: SolrImport = new SolrImport(host, port, core)
	
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
	
	/**
	 * Constructs query that can be send to SolrService
	 */
	def apply(): SolrQuery = {
		val url = constructQuery();
		return SolrQuery(host, port, url);
	}
	
	private def constructQuery(): String = {
		val b: StringBuilder = new StringBuilder();
		b ++= core;
		b ++= "/select?";
		b ++= "q="+URLEncoder.encode(query, "UTF-8");
		b ++= "&wt="+URLEncoder.encode(wt, "UTF-8");
		b ++= "&fl="+URLEncoder.encode(fields, "UTF-8");
		b ++= "&start="+start;
		b ++= "&rows="+rows;
		
		return b.result;
	}
}

/**
 * Constructs a Solr dataimport query.
 * Allows to decide between full-import and delta-import
 * and specify values for clean, commit and optimise flags.
 */
class SolrImport(val host: String, val port: Int, val core: String) {
	private var deltaImport = false
	private var cleanFlag = true;
	private var commitFlag = true;
	private var optimiseFlag = false;
	
	private val wt = "json";
	
	def full: SolrImport = {
		deltaImport = false;
		return this;
	}
	
	def delta: SolrImport = {
		deltaImport = true;
		return this;
	}
	
	def clean(flag: Boolean): SolrImport = {
		cleanFlag = flag;
		return this;
	}	
	def clean: SolrImport = clean(true);
	def noClean: SolrImport = clean(false);
	
	def commit(flag: Boolean): SolrImport = {
		commitFlag = flag;
		return this;
	}
	def commit: SolrImport = commit(true);
	def noCommit: SolrImport = commit(false);
	
	def optimise(flag: Boolean): SolrImport = {
		optimiseFlag = flag;
		return this;
	}
	def optimise: SolrImport = optimise(true);
	def noOptimise: SolrImport = optimise(false);
	// For American nerds -.-
	def optimize: SolrImport = optimise(true);
	def noOptimize: SolrImport = optimise(false);
	
	/**
	 * Constructs query that can be send to SolrService
	 */
	def apply(): SolrQuery = {
		val url = constructQuery();
		return SolrQuery(host, port, url);
	}
	
	private def constructQuery(): String = {
		val b: StringBuilder = new StringBuilder();
		b ++= core;
		b ++= "/dataimport?";
		b ++= "command=" + (if(deltaImport) "delta-import" else "full-import");
		b ++= "&clean="+cleanFlag;
		b ++= "&commit="+commitFlag;
		b ++= "&optimize="+optimiseFlag; //remember that Solr has AE spelling
		b ++= "&wt="+URLEncoder.encode(wt, "UTF-8");
		
		return b.result;
	}
}

/**
 * Factory for Solr query builders
 */
object Solr {
	
	/**
	 * Wraps the constructor of [[spray.solr.Solr]]
	 */
	def apply(host: String, port: Int, core: String): Solr = {
		return new Solr(host, port, core);
	}
	
	/**
	 * Fetches data for building a [[spray.solr.Solr]] instance 
	 * from the provided typesafe config file.
	 * 
	 * Values <path>.ip, <path>.port and <path>.core must be present.
	 * 
	 * @param path the path inside the config file in standard '.'-Form (e.g. "spray.solr.host")
	 * @param config the config file to look up values in
	 */
	def apply(path: String, config: Config): Solr = {
		val ip = config.getString(path+".ip");
		val port = config.getInt(path+".port");
		val core = config.getString(path+".core");
		return apply(ip, port, core);
	}
	
	/**
	 * Acquires the config file for {@code Solr(path, config)} from the 
	 * implicit ActorSystem as in {@code system.settings.config}
	 */
	def apply(path: String)(implicit system: ActorSystem): Solr = {
		return apply(path, system.settings.config);
	}
}