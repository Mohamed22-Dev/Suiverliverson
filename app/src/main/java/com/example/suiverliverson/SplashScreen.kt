package com.example.suiverliverson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.AppBackground
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Purple
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

// تحسين الخوارزمية لزيادة الوضوح الحاد للحواف
fun makeBlackBackgroundTransparent(context: Context, resId: Int): ImageBitmap? {
    return try {
        val original = BitmapFactory.decodeResource(context.resources, resId) ?: return null
        val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
        val width = mutable.width
        val height = mutable.height
        val pixels = IntArray(width * height)
        mutable.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            if (r < 32 && g < 32 && b < 32) {
                pixels[i] = 0x00000000
            }
        }
        mutable.setPixels(pixels, 0, width, 0, 0, width, height)
        mutable.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LiversonLogo(iconSize: Dp = 150.dp, textSize: TextUnit = 50.sp) {
    val context = LocalContext.current
    val transparentLogo = remember { makeBlackBackgroundTransparent(context, R.drawable.logo) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (transparentLogo != null) {
            Image(
                bitmap = transparentLogo,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(Modifier.height(2.dp)) // تقريب الشعار ليكون مباشرة فوق نص الاسم
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = White)) { append("Liver") }
                withStyle(SpanStyle(color = Red)) { append("son") }
            },
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1800))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            for (i in 0..9) {
                val y = h * i / 9f
                drawLine(
                    Red.copy(alpha = 0.05f),
                    Offset(0f, y),
                    Offset(w, y - 160f),
                    strokeWidth = 2f
                )
            }
            for (i in 0..5) {
                val x = w * i / 5f
                drawLine(
                    Red.copy(alpha = 0.04f),
                    Offset(x, 0f),
                    Offset(x + 120f, h),
                    strokeWidth = 2f
                )
            }

            val wave = Path().apply {
                moveTo(0f, h * 0.90f)
                cubicTo(w * 0.30f, h * 0.80f, w * 0.55f, h * 0.97f, w, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                wave,
                Brush.horizontalGradient(listOf(Color(0xFF005B66), Purple))
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiversonLogo(iconSize = 160.dp, textSize = 50.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = LocalStrings.current.slogan.replace(". ", ".\n"),
                color = LightGray,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .width(110.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Red.copy(alpha = 0.25f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(110.dp * progress.value)
                    .background(Red)
            )
        }
    }
}
