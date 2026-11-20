package org.moontechlab.selenetv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.moontechlab.selenetv.model.VideoInfo
import org.moontechlab.selenetv.model.DoubanMovieDetails
import org.moontechlab.selenetv.service.ApiResponse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.moontechlab.selenetv.service.DoubanService
import org.moontechlab.selenetv.util.getImageUrlSync

/**
 * TV 端首页首屏巨幅轮播 Banner 组件
 *
 * @param videos 轮播影视列表（最新且经可播放性校验的影片）
 * @param isLoading 加载状态
 * @param onVideoClick 点击触发播放/详情回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBannerCarousel(
    videos: List<VideoInfo>,
    isLoading: Boolean = false,
    onVideoClick: (VideoInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val bannerList = remember(videos) {
        if (videos.isNotEmpty()) videos.take(6) else emptyList()
    }

    if (bannerList.isEmpty()) {
        // 未加载完成或无数据时：展示优雅骨架展台，绝不展示硬编码假数据
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 6.dp)
                .height(330.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF121318))
                .border(
                    border = BorderStroke(0.8.dp, Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 32.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x22FFFFFF))
                )
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
                Box(
                    modifier = Modifier
                        .width(380.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x1FFFFFFF))
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { bannerList.size })
    val coroutineScope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }

    // 聚焦微动效缩放
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "banner_scale"
    )

    // 自动轮播：非聚焦状态下每 5 秒自动切到下一页
    LaunchedEffect(isFocused, pagerState.pageCount) {
        while (!isFocused && pagerState.pageCount > 1) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    val okKeyGate = remember { OkKeyClickGate() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 6.dp)
            .height(330.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isFocused) 18.dp else 4.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = if (isFocused) Color(0x55FFFFFF) else Color.Black
            )
            .clip(RoundedCornerShape(14.dp))
            .border(
                border = BorderStroke(
                    width = if (isFocused) 2.dp else 0.8.dp,
                    color = if (isFocused) Color(0xEEFFFFFF) else Color(0x1AFFFFFF)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused || focusState.hasFocus
            }
            .focusable()
            .onKeyEvent { keyEvent ->
                // 处理遥控器按键：左右循环切换轮播，OK键触发播放
                val nativeEvent = keyEvent.nativeKeyEvent
                if (okKeyGate.shouldConsume(nativeEvent.action, nativeEvent.keyCode, nativeEvent.repeatCount)) {
                    return@onKeyEvent true
                }

                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            val prevPage = if (pagerState.currentPage > 0) pagerState.currentPage - 1 else pagerState.pageCount - 1
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(prevPage)
                            }
                            true
                        }
                        Key.DirectionRight -> {
                            val nextPage = if (pagerState.currentPage < pagerState.pageCount - 1) pagerState.currentPage + 1 else 0
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(nextPage)
                            }
                            true
                        }
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            val currentItem = bannerList.getOrNull(pagerState.currentPage)
                            if (currentItem != null) {
                                onVideoClick(currentItem)
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val currentItem = bannerList.getOrNull(pagerState.currentPage)
                if (currentItem != null) {
                    onVideoClick(currentItem)
                }
            }
    ) {
        // HorizontalPager 承载各海报页
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val video = bannerList[pageIndex]
            BannerItemView(
                video = video,
                isFocused = isFocused,
                currentPage = pagerState.currentPage,
                pageCount = bannerList.size
            )
        }
    }
}

/**
 * 官方原生横版超清剧照 (Backdrop) + 悬浮胶片卡融合大片展台：
 * 1. 优先从豆瓣官方 Photos 异步拉取 16:9 / 2.35:1 超清横向电影剧照，大片构图完好无损展现；
 * 2. 动态拉取豆瓣详情 summary 填充剧情简介与题材，杜绝硬编码；
 * 3. 330dp 沉浸大画幅，大面积震撼的同时留出充裕的看点剧情排版空间。
 */
@Composable
private fun BannerItemView(
    video: VideoInfo,
    isFocused: Boolean,
    currentPage: Int,
    pageCount: Int
) {
    val context = LocalContext.current
    var movieDetails by remember(video.id, video.doubanId) { mutableStateOf<DoubanMovieDetails?>(null) }
    var backdropUrl by remember(video.id) { mutableStateOf<String?>(null) }

    // 动态拉取超清横版剧照 + 官方剧情简介与详情
    LaunchedEffect(video.id, video.doubanId) {
        val dId = video.doubanId
        if (!dId.isNullOrBlank()) {
            launch {
                backdropUrl = DoubanService.getInstance(context).getDoubanBackdrop(dId)
            }
            launch {
                when (val resp = DoubanService.getInstance(context).getDoubanDetails(dId)) {
                    is ApiResponse.Success -> {
                        movieDetails = resp.data
                    }
                    else -> {}
                }
            }
        }
    }

    // 动态解析元数据与简介（彻底杜绝硬编码）
    val summaryText = movieDetails?.summary?.takeIf { it.isNotBlank() }
        ?: (video.searchTitle.takeIf { it.isNotBlank() && it != video.title } ?: "")

    val genreText = run {
        val genres = movieDetails?.genres?.filter { it.isNotBlank() }
        if (!genres.isNullOrEmpty()) {
            if (video.year.isNotEmpty()) "${video.year} · ${genres.joinToString(" / ")}" else genres.joinToString(" / ")
        } else if (video.year.isNotEmpty()) {
            "${video.year} · 电影"
        } else {
            "电影"
        }
    }

    val tagText = when {
        video.rate != null && (video.rate.toDoubleOrNull() ?: 0.0) >= 8.0 -> "🔥 豆瓣高分"
        video.year.isNotBlank() -> "${video.year} 最新"
        else -> "精选热映"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1014))
    ) {
        // 1. 底层大背景：优先使用官方原生 16:9 横版超清剧照；无剧照时平滑回退海报弥散环境光
        val displayImg = backdropUrl ?: video.cover
        if (displayImg.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrlSync(displayImg))
                    .size(coil.size.Size.ORIGINAL)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (backdropUrl != null) 0.98f else 0.40f
                        scaleX = if (backdropUrl != null) 1.0f else 1.20f
                        scaleY = if (backdropUrl != null) 1.0f else 1.20f
                    }
            )
        }

        // 2. 智能渐变遮罩：
        // - 有横版剧照时：采用自底向上深邃平滑渐变，上半部通透裸露超清大片画面，下半部平滑过渡保护文字
        // - 无剧照回退时：使用左侧水平渐变保护文字并突显右侧悬浮海报
        if (backdropUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.30f to Color.Transparent,
                                0.58f to Color(0x66090A0D),
                                0.78f to Color(0xCC090A0D),
                                1.0f to Color(0xFA090A0D)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF0F1014),
                                0.45f to Color(0xF20F1014),
                                0.75f to Color(0x990F1014),
                                1.0f to Color(0x220F1014)
                            )
                        )
                    )
            )
        }

        // 3. 右侧【电影明信片·立体悬浮微卡】（当未获取到横版剧照时，提供 100% 完整的海报原画保底）
        if (backdropUrl == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 28.dp, top = 20.dp, bottom = 20.dp)
                    .fillMaxHeight()
                    .aspectRatio(2f / 3f)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(8.dp),
                        spotColor = Color(0x99000000)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0x40FFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                CoverImage(
                    rawUrl = video.cover,
                    contentDescription = video.title,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 4. 【沉浸式信息排版】：大片标题 + 元数据副信息 + 剧情简介多行展示
        if (backdropUrl != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 第一行：两端对齐（微标签 + 评分 + 年份与题材，右侧指示器）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 红色微型胶囊
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE50914), Color(0xFFFF3B30))
                                    )
                                )
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = tagText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 金星评分
                        val rate = video.rate ?: movieDetails?.rate
                        if (!rate.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(13.5.dp)
                                )
                                Text(
                                    text = rate,
                                    color = Color(0xFFFFB800),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 细圆点分隔
                        Text(text = "•", color = Color(0x55FFFFFF), fontSize = 11.sp)

                        // 年份与题材
                        if (genreText.isNotEmpty()) {
                            Text(
                                text = genreText,
                                color = Color(0xB3FFFFFF),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 右侧纤细指示条
                    if (pageCount > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(pageCount) { index ->
                                val isCurrent = index == currentPage
                                Box(
                                    modifier = Modifier
                                        .height(3.5.dp)
                                        .width(if (isCurrent) 22.dp else 6.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isCurrent) Color.White else Color(0x44FFFFFF)
                                        )
                                )
                            }
                        }
                    }
                }

                // 第二行：大片主标题（26sp 粗体白字，气势磅礴）
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 32.sp
                )

                // 第三行：剧情看点简介（优雅 2 行排版，大屏电视清晰醒目）
                if (summaryText.isNotBlank()) {
                    Text(
                        text = summaryText,
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(top = 2.dp)
                    )
                }
            }
        } else {
            // 回退海报经典左侧排版（高度加高至 330dp，空间充裕）
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.64f)
                    .padding(start = 32.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 元数据行：标签 + 评分 + 年份与题材
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE50914), Color(0xFFFF4141))
                                )
                            )
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = tagText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val rate = video.rate ?: movieDetails?.rate
                    if (!rate.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = rate,
                                color = Color(0xFFFFB800),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (genreText.isNotEmpty()) {
                        Text(
                            text = genreText,
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 32.sp
                )

                if (summaryText.isNotBlank()) {
                    Text(
                        text = summaryText,
                        color = Color(0xCCFFFFFF),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (pageCount > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pageCount) { index ->
                            val isCurrent = index == currentPage
                            Box(
                                modifier = Modifier
                                    .height(3.5.dp)
                                    .width(if (isCurrent) 22.dp else 6.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isCurrent) Color.White else Color(0x44FFFFFF)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
