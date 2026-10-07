package com.edupixel.school

import com.edupixel.school.core.network.NetworkErrorParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException

class NetworkErrorParserTest {

    @Test
    fun testConnectExceptionHandling() {
        val error = NetworkErrorParser.parseException(ConnectException("Connection refused"))
        assertTrue(error.isNetworkError)
        assertTrue(error.message.contains("Cannot connect to EduPixel API"))
    }

    @Test
    fun testFastApi422ValidationParsing() {
        val jsonErrorBody = """
            {
              "detail": [
                {
                  "type": "missing",
                  "loc": ["body", "name"],
                  "msg": "Field required"
                }
              ]
            }
        """.trimIndent()

        val response = Response.error<Unit>(422, jsonErrorBody.toResponseBody("application/json".toMediaType()))
        val httpException = HttpException(response)

        val parsed = NetworkErrorParser.parseException(httpException)
        assertEquals(422, parsed.code)
        assertTrue(parsed.message.contains("Field required"))
        assertTrue(parsed.details.isNotEmpty())
    }

    @Test
    fun test404NotFoundParsing() {
        val response = Response.error<Unit>(404, "".toResponseBody("application/json".toMediaType()))
        val httpException = HttpException(response)

        val parsed = NetworkErrorParser.parseException(httpException)
        assertEquals(404, parsed.code)
        assertEquals("Requested item was not found.", parsed.message)
    }

    @Test
    fun test409ConflictParsing() {
        val response = Response.error<Unit>(409, "".toResponseBody("application/json".toMediaType()))
        val httpException = HttpException(response)

        val parsed = NetworkErrorParser.parseException(httpException)
        assertEquals(409, parsed.code)
        assertTrue(parsed.message.contains("conflict"))
    }
}
