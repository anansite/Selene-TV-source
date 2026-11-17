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

private data class BannerMovieMeta(
    val tag: String,
    val genre: String,
    val overview: String
)

private val MOVIE_META_MAP = mapOf(
    "野兽之心" to BannerMovieMeta(
        tag = "2026 豆瓣爆款",
        genre = "剧情 / 爱情",
        overview = "面对现实的残酷与欲望的诱惑，一对相依为命的边缘恋人展开了扣人心弦的救赎博弈。"
    ),
    "八仙！" to BannerMovieMeta(
        tag = "2026 爆笑喜剧",
        genre = "喜剧 / 动画 / 奇幻 / 冒险",
        overview = "八仙在成仙与搞事业之间玩转人间。面对泼天富贵与神妖阻挠，八个身份各异的凡人展开爆笑逆袭。"
    ),
    "沙丘2" to BannerMovieMeta(
        tag = "IMAX 史诗巨制",
        genre = "科幻 / 冒险 / 动作",
        overview = "保罗·厄崔迪携手契妮与弗雷曼人，踏上复仇与拯救厄拉科斯的壮丽史诗之旅，宇宙宿命在此决裂。"
    ),
    "周处除三害" to BannerMovieMeta(
        tag = "高分动作爽片",
        genre = "动作 / 犯罪 / 剧情",
        overview = "通缉犯陈桂林得知自己身患绝症后，决心查出前两名通缉犯并除掉他们借此扬名，展开亡命追凶。"
    ),
    "流浪地球2" to BannerMovieMeta(
        tag = "硬核科幻里程碑",
        genre = "科幻 / 动作 / 灾难",
        overview = "太阳急速老化面临毁灭，人类启动万座行星发动机奔向新家园。在数字生命危机与太空电梯浩劫面前，全人类挺身决战。"
    ),
    "另一个妈妈" to BannerMovieMeta(
        tag = "高能惊悚力作",
        genre = "悬疑 / 惊悚 / 剧情",
        overview = "母亲意外发现收养的女儿暗藏不可告人的秘密，平静生活瞬间演变为一场惊心动魄的心理拉锯战。"
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
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .height(215.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isFocused) 16.dp else 4.dp,
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
 * 现代流光悬浮微卡（Modern Ambient Floating Card）：
 * 1. 黄金比例尺寸（215dp 紧凑大片画幅，告别傻大笨粗，释放屏幕纵向视野）；
 * 2. 右侧【电影明信片·立体悬浮微卡】：100% 完整原画 2:3 胶片卡，带细腻圆角、微光边框与景深投影，艺术感极强；
 * 3. 告别死黑【流体环境光溢出】：海报原色大底微透弥散扩散，配合平滑横向暗调渐变，整张卡片通透灵动；
 * 4. 左侧【紧凑黄金信息栈】：红标胶囊 + 金星评分 + 22sp粗体片名 + 2行看点精要 + 精巧指示胶囊条，浑然一体。
 */
@Composable
private fun BannerItemView(
    video: VideoInfo,
    isFocused: Boolean,
    currentPage: Int,
    pageCount: Int
) {
    val meta = MOVIE_META_MAP[video.title]
        ?: BannerMovieMeta(tag = "🔥 热门推荐", genre = "精选佳作", overview = "")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        // 1. 底层流体环境图（海报局部放大弥散，色彩自然溢出，告别枯燥死黑）
        CoverImage(
            rawUrl = video.cover,
            contentDescription = null,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.32f
                    scaleX = 1.25f
                    scaleY = 1.25f
                }
        )

        // 2. 现代流光暗调双层横向蒙版（保护左侧文字可读性，右侧透出环境光晕）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFA121214),
                            0.42f to Color(0xEE121214),
                            0.70f to Color(0x99121214),
                            1.0f to Color(0x40121214)
                        )
                    )
                )
        )

        // 3. 右侧【电影明信片·立体悬浮微卡】（100% 完整原画海报，永不裁切，典藏质感）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 22.dp, top = 12.dp, bottom = 12.dp)
                .fillMaxHeight()
                .aspectRatio(2f / 3f)
                .shadow(
                    elevation = 12.dp,
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

        // 4. 左侧【紧凑黄金信息栈】（徽标 + 评分 + 标题 + 剧情精要 + 底部指示条）
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.66f)
                .padding(start = 28.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // 元数据行：标签 + 评分 + 年份与题材
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 红色渐变小胶囊
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE50914), Color(0xFFFF4141))
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = meta.tag,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 金星评分
                val rate = video.rate
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
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 年份与分类
                if (video.year.isNotEmpty()) {
                    Text(
                        text = "${video.year} · ${meta.genre}",
                        color = Color(0xAAFFFFFF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 大片主标题（22sp 粗黑体，紧凑有力）
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 28.sp
            )

            // 精练看点剧情概述（2 行收敛排版）
            if (meta.overview.isNotEmpty()) {
                Text(
                    text = meta.overview,
                    color = Color(0xBBFFFFFF),
                    fontSize = 11.5.sp,
                    lineHeight = 16.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 5. 底部纤细指示条（紧贴文字左对齐，与内容融为一体）
            if (pageCount > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { index ->
                        val isCurrent = index == currentPage
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(if (isCurrent) 18.dp else 5.dp)
                                .clip(RoundedCornerShape(1.5.dp))
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
