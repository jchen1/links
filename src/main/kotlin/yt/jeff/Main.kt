package yt.jeff

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullString
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.sql2o.Sql2o
import spark.Spark.*
import yt.jeff.links.Link
import yt.jeff.links.LinkDAO

fun main(args: Array<String>) {
    exception(Exception::class.java) { e, req, res -> e.printStackTrace() }

    val dbUsername = System.getenv("PG_USERNAME") ?: "links_user"
    val dbPassword = System.getenv("PG_PASSWORD") ?: "links_password"
    val dbName= System.getenv("PG_DBNAME") ?: "postgres"
    val linksPort = (System.getenv("LINKS_PORT") ?: "4567").toInt()
    val hostUrl = System.getenv("HOST_URL") ?: "http://localhost"

    Class.forName("org.postgresql.Driver")

    val sql2o = Sql2o("jdbc:postgresql://localhost:5432/" + dbName, dbUsername, dbPassword)
    val linkDAO = LinkDAO(sql2o)

    val gson = Gson()

    port(linksPort)

    get("/", fun(req, res): String {
        res.type("text/html")
        return "<html><head>  <title>test</title></head><body>  <form name='myForm' id='myForm'>    <p><label for='url'>URL:</label>      <input type='text' name='url' id='url'></p>    <input value='Submit' type='submit'>  </form>  <a id='shortlink' href='#'></a>  <script type='text/javascript'>    var form = document.getElementById('myForm');    form.addEventListener('submit', function(event) {      event.preventDefault();      var data = {        url: document.getElementById('url').value      };      var request = new XMLHttpRequest();      var url = 'http://links.jeff.yt/links';      request.open('POST', url, true);      request.setRequestHeader('Content-Type', 'application/json');      request.onreadystatechange = function() {        if (request.readyState === 4 && request.status === 200) {          var link = document.getElementById('shortlink');          link.setAttribute('href', request.responseText);          link.innerText = request.responseText;        }      };      request.send(JSON.stringify(data));    });  </script></body></html>"
    })

    post("/links", "application/json", fun(req, res): String {
        val body = gson.fromJson<JsonObject>(req.body())
        var url = body["url"].nullString

        if (url is String) {
            val id = linkDAO.insert(url)
            res.status(200)
            return hostUrl + (if (hostUrl.indexOf("localhost") == -1) "" else ":" + linksPort) + "/" + id
        }
        else {
            res.status(400)
            return "url is required"
        }
    })

    get("/:id", { req, res ->
        val id = req.params(":id").toLong()
        val link = linkDAO.getById(id)
        when (link) {
            is Link -> res.redirect(link.url)
            else -> res.status(404)
        }
    })
}

