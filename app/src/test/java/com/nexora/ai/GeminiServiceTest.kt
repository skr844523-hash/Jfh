package com.nexora.ai

import com.nexora.ai.data.api.model.GeminiRequest
import com.nexora.ai.data.api.model.GeminiResponse
import com.nexora.ai.data.api.model.Candidate
import com.nexora.ai.data.api.model.Content
import com.nexora.ai.data.api.model.Part
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GeminiServiceTest {

    @Test
    fun testGeminiRequest_fromPrompt() {
        val prompt = "Hello NEXORA"
        val systemInstruction = "You are NEXORA AI"
        val request = GeminiRequest.fromPrompt(
            prompt = prompt,
            systemInstruction = systemInstruction
        )

        assertEquals(1, request.contents.size)
        assertEquals("user", request.contents[0].role)
        assertEquals("Hello NEXORA", request.contents[0].parts[0].text)
        assertNotNull(request.systemInstruction)
        assertEquals("system", request.systemInstruction?.role)
        assertEquals("You are NEXORA AI", request.systemInstruction?.parts?.get(0)?.text)
    }

    @Test
    fun testGeminiResponse_firstTextOrNull() {
        val response = GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = Content(
                        role = "model",
                        parts = listOf(
                            Part(text = "Hello! "),
                            Part(text = "I am NEXORA.")
                        )
                    )
                )
            )
        )

        assertEquals("Hello! I am NEXORA.", response.firstTextOrNull())
    }
}
