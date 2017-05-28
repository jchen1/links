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

    post("/links", "application/json", fun(req, res): String {
        val body = gson.fromJson<JsonObject>(req.body())
        var url = body["url"].nullString

        if (url is String) {
            val id = linkDAO.insert(url)
            res.status(200)
            return hostUrl + (if (linksPort == 80) "" else ":" + linksPort) + "/" + id
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

