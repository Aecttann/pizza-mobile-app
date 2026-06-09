package com.aectann.pizzamobileapp.ui.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.aectann.pizzamobileapp.data.model.Pizza
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.ui.catalog.components.BananaForScale
import com.aectann.pizzamobileapp.ui.catalog.components.OrderBar
import com.aectann.pizzamobileapp.ui.catalog.components.PizzaNavbar
import com.aectann.pizzamobileapp.ui.catalog.components.SizeSelector
import com.aectann.pizzamobileapp.ui.catalog.components.SizeSelectorCurveAnchors
import com.aectann.pizzamobileapp.ui.common.AppBackHandler
import com.aectann.pizzamobileapp.ui.theme.ColorAccent
import com.aectann.pizzamobileapp.ui.theme.ColorHighlight
import com.aectann.pizzamobileapp.ui.theme.ColorText
import com.aectann.pizzamobileapp.ui.theme.ColorWhite
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Layout values are expressed in the Figma reference frame (375dp wide) and scaled
// by widthScale = actualWidth / DESIGN_WIDTH to match the design on any device width.
private const val DESIGN_WIDTH = 375f

private val PIZZA_SIZE_S = 196.dp
private val PIZZA_SIZE_M = 244.dp
private val PIZZA_SIZE_L = 274.dp
private val SIDE_PIZZA_SIZE = 80.dp
private val ZOOM_ICON_SIZE = 88.dp
private val CAROUSEL_HEIGHT = 330.dp

private const val MAX_ZOOM = 5.2f
private const val ZOOM_PAN_MULTIPLIER = 2.5f
private const val PINCH_ZOOM_OUT_THRESHOLD = 1.08f
private const val ZOOM_ANIMATION_DURATION_MS = 950
private const val INITIAL_PIZZA_ID = "pepperoni-blast"
private const val CATALOG_BG_ENTER_DELAY_MS = 35L
private const val CATALOG_BG_ENTER_MS = 120
private const val CATALOG_CONTENT_ENTER_DELAY_MS = 95L
private const val CATALOG_CONTENT_ENTER_MS = 130

// Lower-resolution first paint, then the original resolution is swapped in.
private const val PIZZA_MEDIUM_PX = 700

private const val BG_SEAM_EDGE = 0.566f
private const val BG_SEAM_CENTER = 0.65f
private val BG_CURVE_SIDE_VISUAL_LIFT = 8.dp
private val INFO_PANEL_ZOOM_EXIT = 620.dp

private fun PizzaSize.pizzaImageSize(): Dp = when (this) {
    PizzaSize.S -> PIZZA_SIZE_S
    PizzaSize.M -> PIZZA_SIZE_M
    PizzaSize.L -> PIZZA_SIZE_L
}

// Two-tier loading for the same URL:
// - medium: a downsampled bitmap that paints quickly (used for the small side pizzas
//   and as the placeholder for the centered pizza);
// - full: the original resolution, kept crisp under up-to-MAX_ZOOM magnification.
// Distinct memory-cache keys let both coexist; the full request crossfades over the
// medium as soon as it is ready, at any interaction stage.
private fun pizzaImageRequest(context: PlatformContext, url: String, full: Boolean): ImageRequest =
    ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .apply {
            if (full) {
                size(Size.ORIGINAL)
                memoryCacheKey("$url@full")
                placeholderMemoryCacheKey("$url@medium")
            } else {
                size(PIZZA_MEDIUM_PX, PIZZA_MEDIUM_PX)
                memoryCacheKey("$url@medium")
            }
        }
        .build()

@Composable
fun PizzaCatalogScreen(
    initialPizzas: List<Pizza>? = null,
    viewModel: PizzaCatalogViewModel = viewModel { PizzaCatalogViewModel(initialPizzas = initialPizzas) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(ColorWhite)) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(CatalogTestTags.LOADING),
                    color = ColorAccent,
                )
            }
            state.error != null -> {
                Text(
                    text = "Failed to load pizzas.\nPlease try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .testTag(CatalogTestTags.ERROR),
                )
            }
            state.pizzas.isNotEmpty() -> {
                PizzaCarousel(
                    state = state,
                    onSizeSelected = viewModel::selectSize,
                    onIncrement = { viewModel.increment(it) },
                    onDecrement = { viewModel.decrement(it) },
                )
            }
        }
    }
}

@Composable
private fun PizzaCarousel(
    state: PizzaCatalogUiState,
    onSizeSelected: (PizzaSize) -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
) {
    var carouselRootLeft by remember { mutableStateOf(0f) }
    var carouselRootTop by remember { mutableStateOf(0f) }
    var sizeSelectorCurveAnchors by remember { mutableStateOf<SizeSelectorCurveAnchors?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val rootPosition = coordinates.positionInRoot()
                carouselRootLeft = rootPosition.x
                carouselRootTop = rootPosition.y
            },
    ) {
        val isLandscape = maxWidth > maxHeight
        val carouselWidth = if (isLandscape) maxWidth * 0.54f else maxWidth
        val widthScale = if (isLandscape) {
            (carouselWidth.value / DESIGN_WIDTH).coerceAtMost(1.05f)
        } else {
            maxWidth.value / DESIGN_WIDTH
        }
        val slotWidth = carouselWidth / 2f
        val sidePadding = (carouselWidth - slotWidth) / 2f
        val carouselHeight = CAROUSEL_HEIGHT * widthScale
        val scope = rememberCoroutineScope()
        val context = LocalPlatformContext.current
        val backgroundEnter = remember { Animatable(0f) }
        val contentEnter = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            coroutineScope {
                launch {
                    delay(CATALOG_BG_ENTER_DELAY_MS)
                    backgroundEnter.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(CATALOG_BG_ENTER_MS, easing = FastOutSlowInEasing),
                    )
                }
                launch {
                    delay(CATALOG_CONTENT_ENTER_DELAY_MS)
                    contentEnter.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(CATALOG_CONTENT_ENTER_MS, easing = FastOutSlowInEasing),
                    )
                }
            }
        }

        // Warm the medium tier for every pizza so the first paint is immediate and the
        // centered pizza already has a placeholder while its full resolution loads.
        LaunchedEffect(state.pizzas) {
            val loader = SingletonImageLoader.get(context)
            state.pizzas.forEach { loader.enqueue(pizzaImageRequest(context, it.imageUrl, full = false)) }
        }

        val initialIndex = remember(state.pizzas) {
            state.pizzas.indexOfFirst { it.id == INITIAL_PIZZA_ID }.coerceAtLeast(0)
        }
        val pagerState = rememberPagerState(initialPage = initialIndex) { state.pizzas.size }
        val currentPizza = state.pizzas.getOrNull(pagerState.currentPage)
        val zoomSpec = tween<Float>(
            durationMillis = ZOOM_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing,
        )
        val panSpec = tween<Float>(
            durationMillis = ZOOM_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing,
        )

        val zoom = remember { Animatable(1f) }
        val zoomPanX = remember { Animatable(0f) }
        val zoomPanY = remember { Animatable(0f) }
        var zoomMode by remember { mutableStateOf(false) }
        var zoomClosing by remember { mutableStateOf(false) }
        // Reset the zoom whenever the centered pizza changes.
        LaunchedEffect(pagerState.currentPage) {
            if (zoom.value != 1f) zoom.snapTo(1f)
            if (zoomPanX.value != 0f) zoomPanX.snapTo(0f)
            if (zoomPanY.value != 0f) zoomPanY.snapTo(0f)
            zoomMode = false
            zoomClosing = false
        }

        suspend fun closeZoom() {
            if (zoomClosing) return
            zoomClosing = true
            coroutineScope {
                launch { zoom.animateTo(1f, zoomSpec) }
                launch { zoomPanX.animateTo(0f, panSpec) }
                launch { zoomPanY.animateTo(0f, panSpec) }
            }
            zoomMode = false
            zoomClosing = false
        }

        // System back: zoom out first; only exit the app when not zoomed in.
        AppBackHandler(enabled = zoomMode) {
            scope.launch { closeZoom() }
        }

        fun startZoom() {
            zoomMode = true
            zoomClosing = false
            scope.launch {
                zoomPanX.snapTo(0f)
                zoomPanY.snapTo(0f)
                zoom.animateTo(MAX_ZOOM, zoomSpec)
            }
        }

        fun endZoom() {
            scope.launch { closeZoom() }
        }

        // Peach background: solid top with a curved bottom edge.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val progress = backgroundEnter.value
            val measuredCurveAnchors = sizeSelectorCurveAnchors?.takeUnless { isLandscape }
            val fallbackEdge = BG_SEAM_EDGE * h
            val fallbackControl = (2f * BG_SEAM_CENTER - BG_SEAM_EDGE) * h
            val targetControlX = measuredCurveAnchors
                ?.middleCenterX
                ?.minus(carouselRootLeft)
                ?.let { middleX -> 2f * middleX - w / 2f }
                ?: w / 2f
            val targetCenter = measuredCurveAnchors?.middleCenterY?.minus(carouselRootTop)
            val targetSide = measuredCurveAnchors?.sideCenterY?.minus(carouselRootTop)?.minus(BG_CURVE_SIDE_VISUAL_LIFT.toPx())
            val sideX = measuredCurveAnchors?.sideCenterX?.minus(carouselRootLeft)
            val sideT = if (sideX != null) {
                var low = 0f
                var high = 0.5f
                repeat(12) {
                    val mid = (low + high) / 2f
                    val xAtMid = 2f * (1f - mid) * mid * targetControlX + mid * mid * w
                    if (xAtMid < sideX) {
                        low = mid
                    } else {
                        high = mid
                    }
                }
                (low + high) / 2f
            } else {
                null
            }
            val sideWeight = sideT?.let { 2f * it * (1f - it) }
            val denominator = sideWeight?.let { 0.5f - it }
            val controlDelta = if (
                targetCenter != null &&
                targetSide != null &&
                denominator != null &&
                denominator > 0.001f
            ) {
                (targetCenter - targetSide) / denominator
            } else {
                null
            }
            val targetEdge = if (targetCenter != null && controlDelta != null) {
                targetCenter - controlDelta / 2f
            } else {
                fallbackEdge
            }
            val targetControl = if (targetCenter != null && controlDelta != null) {
                targetCenter + controlDelta / 2f
            } else {
                fallbackControl
            }
            val yEdge = lerp(h, targetEdge, progress)
            val yControl = lerp(h, targetControl, progress)
            val controlX = lerp(w / 2f, targetControlX, progress)
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, yEdge)
                quadraticTo(controlX, yControl, 0f, yEdge)
                close()
            }
            drawPath(path = path, color = ColorHighlight)
        }

        @Composable
        fun CarouselStage(modifier: Modifier = Modifier) {
            Box(
                modifier = modifier
                    .zIndex(if (zoomMode) 3f else 1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(slotWidth),
                    contentPadding = PaddingValues(horizontal = sidePadding),
                    userScrollEnabled = !zoomMode,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val pizza = state.pizzas[page]
                    val centerSize = state.selectedSize.pizzaImageSize() * widthScale
                    val sideScale = SIDE_PIZZA_SIZE.value / state.selectedSize.pizzaImageSize().value
                    val pageOffset =
                        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            .absoluteValue.coerceIn(0f, 1f)
                    val imageScale = lerp(1f, sideScale, pageOffset)
                    val isCenter = page == pagerState.currentPage

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f - pageOffset),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!zoomMode || !isCenter) {
                            AsyncImage(
                                model = pizzaImageRequest(context, pizza.imageUrl, full = isCenter),
                                contentDescription = pizza.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    // requiredSize keeps the image square regardless of the
                                    // narrower page width, so the circle clip stays a circle.
                                    .requiredSize(centerSize)
                                    .graphicsLayer {
                                        alpha = contentEnter.value
                                        scaleX = imageScale
                                        scaleY = imageScale
                                    }
                                    .shadow(elevation = 4.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (isCenter && pageOffset < 0.5f) {
                                            startZoom()
                                        } else {
                                            scope.launch { pagerState.animateScrollToPage(page) }
                                        }
                                    },
                            )
                        }

                        if (isCenter && !zoomMode) {
                            ZoomLoupeIcon(
                                onClick = { startZoom() },
                                modifier = Modifier
                                    .size(ZOOM_ICON_SIZE * widthScale)
                                    .graphicsLayer { alpha = contentEnter.value },
                            )
                        }
                    }
                }

                if (zoomMode && currentPizza != null) {
                    val centerSize = state.selectedSize.pizzaImageSize() * widthScale
                    AsyncImage(
                        model = pizzaImageRequest(context, currentPizza.imageUrl, full = true),
                        contentDescription = currentPizza.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .requiredSize(centerSize)
                            .graphicsLayer {
                                translationX = zoomPanX.value
                                translationY = zoomPanY.value
                                scaleX = zoom.value
                                scaleY = zoom.value
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { endZoom() })
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        zoomPanX.snapTo(zoomPanX.value + dragAmount.x * ZOOM_PAN_MULTIPLIER)
                                        zoomPanY.snapTo(zoomPanY.value + dragAmount.y * ZOOM_PAN_MULTIPLIER)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoomChange, _ ->
                                    scope.launch {
                                        zoomPanX.snapTo(zoomPanX.value + pan.x * ZOOM_PAN_MULTIPLIER)
                                        zoomPanY.snapTo(zoomPanY.value + pan.y * ZOOM_PAN_MULTIPLIER)
                                        val nextZoom = (zoom.value * zoomChange).coerceIn(1f, MAX_ZOOM * 1.5f)
                                        if (nextZoom <= PINCH_ZOOM_OUT_THRESHOLD) {
                                            closeZoom()
                                        } else {
                                            zoom.snapTo(nextZoom)
                                        }
                                    }
                                }
                            },
                    )
                }
            }
        }

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(carouselWidth)
                        .fillMaxHeight(),
                ) {
                    PizzaNavbar(
                        pizzaName = currentPizza?.name ?: "",
                        enterProgress = contentEnter.value,
                    )
                    CarouselStage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }

                if (currentPizza != null) {
                    PizzaInfoPanel(
                        pizza = currentPizza,
                        selectedSize = state.selectedSize,
                        quantity = state.quantityFor(currentPizza.id),
                        totalPrice = state.totalPriceFor(currentPizza.id),
                        onSizeSelected = { size -> onSizeSelected(size) },
                        onIncrement = { onIncrement(currentPizza.id) },
                        onDecrement = { onDecrement(currentPizza.id) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .navigationBarsPadding()
                            .padding(top = 16.dp)
                            .graphicsLayer {
                                val progress = ((zoom.value - 1f) / (MAX_ZOOM - 1f)).coerceIn(0f, 1f)
                                translationY = INFO_PANEL_ZOOM_EXIT.toPx() * progress
                            },
                        enterProgress = contentEnter.value,
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                PizzaNavbar(
                    pizzaName = currentPizza?.name ?: "",
                    enterProgress = contentEnter.value,
                )

                // Carousel region. Not clipped and raised above the navbar/info panel so the
                // zoomed pizza overflows on top of the other views without a background scrim.
                CarouselStage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(carouselHeight),
                )

                if (currentPizza != null) {
                    PizzaInfoPanel(
                        pizza = currentPizza,
                        selectedSize = state.selectedSize,
                        quantity = state.quantityFor(currentPizza.id),
                        totalPrice = state.totalPriceFor(currentPizza.id),
                        onSizeSelected = { size -> onSizeSelected(size) },
                        onIncrement = { onIncrement(currentPizza.id) },
                        onDecrement = { onDecrement(currentPizza.id) },
                        onSizeSelectorCurveAnchorsMeasured = { anchors ->
                            sizeSelectorCurveAnchors = anchors
                        },
                        modifier = Modifier.graphicsLayer {
                            val progress = ((zoom.value - 1f) / (MAX_ZOOM - 1f)).coerceIn(0f, 1f)
                            translationY = INFO_PANEL_ZOOM_EXIT.toPx() * progress
                        },
                        enterProgress = contentEnter.value,
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomLoupeIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Magnifier (loupe) outline matching the Figma zoom icon: thin ring + short handle.
        Canvas(modifier = Modifier.size(30.dp)) {
            val color = Color.White
            val strokeWidth = size.minDimension * 0.07f
            val ringRadius = size.minDimension * 0.28f
            val ringCenter = Offset(size.width * 0.42f, size.height * 0.42f)
            drawCircle(
                color = color,
                radius = ringRadius,
                center = ringCenter,
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = color,
                start = Offset(ringCenter.x + ringRadius * 0.72f, ringCenter.y + ringRadius * 0.72f),
                end = Offset(ringCenter.x + ringRadius * 1.7f, ringCenter.y + ringRadius * 1.7f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun PizzaInfoPanel(
    pizza: Pizza,
    selectedSize: PizzaSize,
    quantity: Int,
    totalPrice: Double,
    onSizeSelected: (PizzaSize) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    enterProgress: Float = 1f,
    onSizeSelectorCurveAnchorsMeasured: (SizeSelectorCurveAnchors) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enterProgress
                translationY = (1f - enterProgress) * 56.dp.toPx()
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))

        BananaForScale(modifier = Modifier.offset(y = 10.dp))

        Spacer(Modifier.height(0.dp))

        SizeSelector(
            selectedSize = selectedSize,
            onSizeSelected = onSizeSelected,
            onCurveAnchorsMeasured = onSizeSelectorCurveAnchorsMeasured,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = pizza.description,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
        )

        Spacer(Modifier.height(24.dp))

        OrderBar(
            quantity = quantity,
            totalPrice = totalPrice,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
        )

        Spacer(Modifier.height(32.dp))
    }
}
