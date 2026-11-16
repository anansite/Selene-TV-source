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

/**
 * 2024-2026 最新大片兜底精选池（全量 1080P 超清官方海报）
 */
private val DEFAULT_BANNER_MOVIES = listOf(
    VideoInfo(
        id = "36828393",
        title = "野兽之心",
        source = "douban",
        sourceName = "豆瓣",
        year = "2026",
        cover = "https://img3.doubanio.com/view/photo/l_ratio_poster/public/p2933185536.jpg",
        index = 0,
        totalEpisodes = 1,
        playTime = 0,
        totalTime = 0,
        saveTime = 0,
        searchTitle = "野兽之心",
        doubanId = "36828393",
        rate = "7.3"
    ),
    VideoInfo(
        id = "36882191",
        title = "八仙！",
        source = "douban",
        sourceName = "豆瓣",
        year = "2026",
        cover = "https://img3.doubanio.com/view/photo/l_ratio_poster/public/p2934074566.jpg",
        index = 0,
        totalEpisodes = 1,
        playTime = 0,
        totalTime = 0,
        saveTime = 0,
        searchTitle = "八仙！",
        doubanId = "36882191",
        rate = "8.2"
    ),
    VideoInfo(
        id = "35575567",
        title = "沙丘2",
        source = "douban",
        sourceName = "豆瓣",
        year = "2024",
        cover = "https://img3.doubanio.com/view/photo/l_ratio_poster/public/p2897429728.jpg",
        index = 0,
        totalEpisodes = 1,
        playTime = 0,
        totalTime = 0,
        saveTime = 0,
        searchTitle = "沙丘2",
        doubanId = "35575567",
        rate = "8.1"
    ),
    VideoInfo(
        id = "36151692",
        title = "周处除三害",
        source = "douban",
        sourceName = "豆瓣",
        year = "2023",
        cover = "https://img3.doubanio.com/view/photo/l_ratio_poster/public/p2905021483.jpg",
        index = 0,
        totalEpisodes = 1,
        playTime = 0,
        totalTime = 0,
        saveTime = 0,
        searchTitle = "周处除三害",
        doubanId = "36151692",
        rate = "8.1"
    ),
    VideoInfo(
        id = "35267208",
        title = "流浪地球2",
        source = "douban",
        sourceName = "豆瓣",
        year = "2023",
        cover = "https://img3.doubanio.com/view/photo/l_ratio_poster/public/p2886492513.jpg",
        index = 0,
        totalEpisodes = 1,
        playTime = 0,
        totalTime = 0,
        saveTime = 0,
        searchTitle = "流浪地球2",
        doubanId = "35267208",
        rate = "8.3"
    ),
    VideoInfo(
        id = "37317907",
        title = "另一个妈妈",
        source = "douban",
        sourceName = "豆瓣",
        year = "2026",
        cover = "https://img3.doubanio.com/view/photo/l_ratio_poster/public/p2935858295.jpg",
        index = 0,
        totalEpisodes = 1,
        playTime = 0,
        totalTime = 0,
        saveTime = 0,
        searchTitle = "另一个妈妈",
        doubanId = "37317907",
        rate = "6.5"
    )
)

/**
 * TV 端首页首屏巨幅轮播 Banner 组件
 *
 * @param videos 轮播影视列表（默认取豆瓣热门前几项）
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
        if (videos.isNotEmpty()) videos.take(6) else DEFAULT_BANNER_MOVIES
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
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .height(290.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isFocused) 20.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isFocused) Color(0x66FFFFFF) else Color.Black
            )
            .clip(RoundedCornerShape(16.dp))
            .border(
                border = BorderStroke(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) Color.White else Color(0x22FFFFFF)
                ),
                shape = RoundedCornerShape(16.dp)
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
                isFocused = isFocused
            )
        }

        // 底部右侧页码指示条
        if (bannerList.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 28.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(bannerList.size) { index ->
                    val isCurrent = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(if (isCurrent) 22.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) Color(0xFF27AE60) else Color(0x66FFFFFF)
                            )
                    )
                }
            }
        }
    }
}

/**
 * 单张巨幅 Banner 展示视图
 */
@Composable
private fun BannerItemView(
    video: VideoInfo,
    isFocused: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        // 1. 底层大图（居右裁剪）
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterEnd)
        ) {
            CoverImage(
                rawUrl = video.cover,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. 渐变遮罩层（左侧纯黑渐变到右侧透明，底部渐变防止与底栏冲突）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF101010),
                            0.45f to Color(0xF2101010),
                            0.70f to Color(0xAA101010),
                            1.0f to Color(0x33101010)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1.0f to Color(0xCC101010)
                        )
                    )
                )
        )

        // 3. 左侧内容排版
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.62f)
                .padding(start = 36.dp, top = 26.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // 顶部热播胶囊徽章
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE50914))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 热门推荐",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 评分
                val rate = video.rate
                if (!rate.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = rate,
                            color = Color(0xFFFFB800),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 标题
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.ellipsis,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 年份及辅助信息
            if (video.year.isNotEmpty()) {
                Text(
                    text = "${video.year} · 豆瓣热推精选",
                    color = Color(0xBBFFFFFF),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 播放操作提示按钮
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isFocused) Color(0xFF27AE60) else Color(0x33FFFFFF))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isFocused) "按 OK 键立即播放" else "立即播放",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
