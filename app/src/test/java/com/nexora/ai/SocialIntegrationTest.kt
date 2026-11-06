package com.nexora.ai

import com.nexora.ai.integrations.SocialPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SocialIntegrationTest {

    @Test
    fun testSocialPlatforms() {
        assertEquals("واتساب", SocialPlatform.WHATSAPP.displayNameAr)
        assertEquals("فيسبوك", SocialPlatform.FACEBOOK.displayNameAr)
        assertEquals("انستغرام", SocialPlatform.INSTAGRAM.displayNameAr)
        assertEquals("تيليجرام", SocialPlatform.TELEGRAM.displayNameAr)

        assertEquals("com.whatsapp", SocialPlatform.WHATSAPP.packageName)
        assertEquals("com.facebook.katana", SocialPlatform.FACEBOOK.packageName)
        assertEquals("com.instagram.android", SocialPlatform.INSTAGRAM.packageName)
        assertEquals("org.telegram.messenger", SocialPlatform.TELEGRAM.packageName)
    }
}
