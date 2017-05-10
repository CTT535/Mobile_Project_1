package hcmus.angtonyvincent.firebaseauthentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ang Tony Vincent on 4/13/2017.
 */

public class PatternDrawer extends View {

    /**
     * The number of rows and columns
     */
    public static final int LOCK_SIZE = 3;

    /**
     * The size of the pattern's matrix.
     */
    public static final int MATRIX_SIZE = LOCK_SIZE * LOCK_SIZE;

    private static final boolean PROFILE_DRAWING = false;

    /**
     * The time (millisecond) of the animation
     */
    private static final int MILLIS_PER_ANIMATION = 700;

    /**
     * The display's update
     */
    private static final float DRAG_THRESHHOLD = 0.0f;

    private final CellState[][] mCellStates;

    /**
     * The size of dots and paths
     */
    private final int mDotSize;
    private final int mDotSizeActivated;
    private final int mPathWidth;

    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();
    private final Rect mTmpInvalidateRect = new Rect();

    private boolean mDrawingProfilingStarted = false;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();

    private OnPatternListener mOnPatternListener;

    private ArrayList<Cell> mPattern = new ArrayList<>(MATRIX_SIZE);

    /**
     * The pattern drawer
     */
    private boolean[][] mPatternDrawLookup = new boolean[LOCK_SIZE][LOCK_SIZE];

    /**
     * The position of user's finger
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private long mAnimatingPeriodStart;

    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;

    private float mHitFactor = 0.6f;

    private float mSquareWidth;
    private float mSquareHeight;
    private int mRegularColor;
    private int mErrorColor;
    private int mSuccessColor;
    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;

    /**
     * Constructor
     *
     * @param context
     */
    public PatternDrawer(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PatternDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);

        setClickable(true);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PatternDrawer);
        mRegularColor = typedArray.getColor(R.styleable.PatternDrawer_LOCK_COLOR, Color.WHITE);
        mErrorColor = typedArray.getColor(R.styleable.PatternDrawer_WRONG_COLOR, Color.RED);
        mSuccessColor = typedArray.getColor(R.styleable.PatternDrawer_CORRECT_COLOR, Color.GREEN);
        typedArray.recycle();


        mPathPaint.setColor(mRegularColor);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        mPathWidth = dpToPx(3); // Initiate path's size
        mPathPaint.setStrokeWidth(mPathWidth);
        mDotSize = dpToPx(10); // Initiate dot's size
        mDotSizeActivated = dpToPx(20); // Initiate dot's size when it is activated
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mCellStates = new CellState[LOCK_SIZE][LOCK_SIZE];
        for (int i = 0; i < LOCK_SIZE; i++) {
            for (int j = 0; j < LOCK_SIZE; j++) {
                mCellStates[i][j] = new CellState();
                mCellStates[i][j].size = mDotSize;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !isInEditMode()) {
            mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                    context, android.R.interpolator.fast_out_slow_in);
            mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                    context, android.R.interpolator.linear_out_slow_in);
        }
    }

    /**
     * Set the call back for pattern detection.
     *
     * @param onPatternListener The call back.
     */
    public void setOnPatternListener(OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    /**
     * Retrieves current pattern
     * @return current displaying pattern
     */
    @SuppressWarnings("unchecked")
    public List<Cell> getPattern() {
        return (List<Cell>) mPattern.clone();
    }

    /**
     * Set the display mode of the current pattern
     * @param displayMode The display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException(
                        "you must have a pattern to "
                                + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.column);
            mInProgressY = getCenterYForRow(first.row);
            clearPatternDrawLookup();
        }
        invalidate();
    }

    private String getSimplePattern(List<Cell> pattern) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Cell cell : pattern) {
            stringBuilder.append(getSipmleCellPosition(cell));
        }
        return stringBuilder.toString();
    }

    /**
     * Calculate cell's position to string value
     * @param cell
     * @return value
     */
    private String getSipmleCellPosition(Cell cell) {
        if (cell == null)
            return "";
        else {
            int i = cell.row * LOCK_SIZE + cell.column + 1;
            return "" + i;
        }
    }

    private void notifyCellAdded() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern, getSimplePattern(mPattern));
        }
    }

    private void notifyPatternStarted() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern, getSimplePattern(mPattern));
        }
    }

    private void notifyPatternCleared() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    /**
     * Reset all pattern state
     */
    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    /**
     * Clear the pattern lookup table
     */
    private void clearPatternDrawLookup() {
        for (int i = 0; i < LOCK_SIZE; i++) {
            for (int j = 0; j < LOCK_SIZE; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - getPaddingLeft() - getPaddingRight();
        mSquareWidth = width / (float) LOCK_SIZE;

        final int height = h - getPaddingTop() - getPaddingBottom();
        mSquareHeight = height / (float) LOCK_SIZE;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
        viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Determines whether the point is hit
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            // check for gaps in existing pattern
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dColumn = cell.column - lastCell.column;

                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.column;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null
                    && !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mEnableHapticFeedback) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
                    performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                    | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            return cell;
        }
        return null;
    }

    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.row][newCell.column] = true;
        mPattern.add(newCell);
        if (!mInStealthMode) {
            startCellActivatedAnimation(newCell);
        }
        notifyCellAdded();
    }

    private void startCellActivatedAnimation(Cell cell) {
        final CellState cellState = mCellStates[cell.row][cell.column];
        startSizeAnimation(mDotSize, mDotSizeActivated, 96,
                mLinearOutSlowInInterpolator, cellState, new Runnable() {

                    @Override
                    public void run() {
                        startSizeAnimation(mDotSizeActivated, mDotSize, 192,
                                mFastOutSlowInInterpolator, cellState, null);
                    }
                });
        startLineEndAnimation(cellState, mInProgressX, mInProgressY,
                getCenterXForColumn(cell.column), getCenterYForRow(cell.row));
    }

    private void startLineEndAnimation(final CellState state,
                                       final float startX, final float startY, final float targetX,
                                       final float targetY) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float t = (Float) animation.getAnimatedValue();
                        state.lineEndX = (1 - t) * startX + t * targetX;
                        state.lineEndY = (1 - t) * startY + t * targetY;
                        invalidate();
                    }

                });
        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                state.lineAnimator = null;
            }

        });
        valueAnimator.setInterpolator(mFastOutSlowInInterpolator);
        valueAnimator.setDuration(100);
        valueAnimator.start();
        state.lineAnimator = valueAnimator;
    }

    private void startSizeAnimation(float start, float end, long duration,
                                    Interpolator interpolator, final CellState state,
                                    final Runnable endRunnable) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        state.size = (Float) animation.getAnimatedValue();
                        invalidate();
                    }

                });
        if (endRunnable != null) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (endRunnable != null)
                        endRunnable.run();
                }

            });
        }
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }// startSizeAnimation()

    private Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }

    /**
     * Check the vertical coordinate
     * @param y The y coordinate
     * @return The row that y falls in, or -1 if it falls in no row.
     */
    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = getPaddingTop() + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < LOCK_SIZE; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check the honrizontal coordinate
     * @param x The x coordinate.
     * @return The column that x falls in, or -1 if it falls in no column.
     */
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = getPaddingLeft() + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < LOCK_SIZE; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (((AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE)).isTouchExplorationEnabled()) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    event.setAction(MotionEvent.ACTION_DOWN);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    event.setAction(MotionEvent.ACTION_UP);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                mPatternInProgress = false;
                resetPattern();
                notifyPatternCleared();

                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        final float radius = mPathWidth;
        final int historySize = event.getHistorySize();
        mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event
                    .getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event
                    .getY();
            Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = mPattern.size();
            if (hitCell != null && patternSize == 1) {
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            final float dx = Math.abs(x - mInProgressX);
            final float dy = Math.abs(y - mInProgressY);
            if (dx > DRAG_THRESHHOLD || dy > DRAG_THRESHHOLD) {
                invalidateNow = true;
            }

            if (mPatternInProgress && patternSize > 0) {
                final ArrayList<Cell> pattern = mPattern;
                final Cell lastCell = pattern.get(patternSize - 1);
                float lastCellCenterX = getCenterXForColumn(lastCell.column);
                float lastCellCenterY = getCenterYForRow(lastCell.row);
                float left = Math.min(lastCellCenterX, x) - radius;
                float right = Math.max(lastCellCenterX, x) + radius;
                float top = Math.min(lastCellCenterY, y) - radius;
                float bottom = Math.max(lastCellCenterY, y) + radius;

                if (hitCell != null) {
                    final float width = mSquareWidth * 0.5f;
                    final float height = mSquareHeight * 0.5f;
                    final float hitCellCenterX = getCenterXForColumn(hitCell.column);
                    final float hitCellCenterY = getCenterYForRow(hitCell.row);

                    left = Math.min(hitCellCenterX - width, left);
                    right = Math.max(hitCellCenterX + width, right);
                    top = Math.min(hitCellCenterY - height, top);
                    bottom = Math.max(hitCellCenterY + height, bottom);
                }

                mTmpInvalidateRect.union(Math.round(left), Math.round(top),
                        Math.round(right), Math.round(bottom));
            }
        }
        mInProgressX = event.getX();
        mInProgressY = event.getY();

        if (invalidateNow) {
            mInvalidate.union(mTmpInvalidateRect);
            invalidate(mInvalidate);
            mInvalidate.set(mTmpInvalidateRect);
        }
    }

    private void handleActionUp(MotionEvent event) {
        // report pattern detected
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            cancelLineAnimations();
            notifyPatternDetected();
            invalidate();
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void cancelLineAnimations() {
        for (int i = 0; i < LOCK_SIZE; i++) {
            for (int j = 0; j < LOCK_SIZE; j++) {
                CellState state = mCellStates[i][j];
                if (state.lineAnimator != null) {
                    state.lineAnimator.cancel();
                    state.lineEndX = Float.MIN_VALUE;
                    state.lineEndY = Float.MIN_VALUE;
                }
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.column);
            final float startY = getCenterYForRow(hitCell.row);

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int) (startX - widthOffset),
                    (int) (startY - heightOffset),
                    (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    private float getCenterXForColumn(int column) {
        return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f;
    }

    private float getCenterYForRow(int row) {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {

            // figure out what to draw

            final int oneCycle = (count + 1) * MILLIS_PER_ANIMATION;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart)
                    % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_ANIMATION;

            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.row][cell.column] = true;
            }

            // figure out in progress portion of ghosting line

            final boolean needToUpdateInProgressPoint = numCircles > 0
                    && numCircles < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_ANIMATION))
                        / MILLIS_PER_ANIMATION;

                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.column);
                final float centerY = getCenterYForRow(currentCell.row);

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle
                        * (getCenterXForColumn(nextCell.column) - centerX);
                final float dy = percentageOfNextCircle
                        * (getCenterYForRow(nextCell.row) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }

            invalidate();
        }

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        // draw the cells
        for (int i = 0; i < LOCK_SIZE; i++) {
            float centerY = getCenterYForRow(i);
            for (int j = 0; j < LOCK_SIZE; j++) {
                CellState cellState = mCellStates[i][j];
                float centerX = getCenterXForColumn(j);
                float size = cellState.size * cellState.scale;
                float translationY = cellState.translateY;
                drawRect(canvas, (int) centerX, (int) centerY + translationY,
                        size, drawLookup[i][j], cellState.alpha);
            }
        }

        // draw the paths
        final boolean drawPath = !mInStealthMode;

        if (drawPath) {
            mPathPaint.setColor(getCurrentColor(true /* partOfPattern */));

            boolean anyCircles = false;
            float lastX = 0f;
            float lastY = 0f;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);

                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if (i != 0) {
                    CellState state = mCellStates[cell.row][cell.column];
                    currentPath.rewind();
                    currentPath.moveTo(lastX, lastY);
                    if (state.lineEndX != Float.MIN_VALUE
                            && state.lineEndY != Float.MIN_VALUE) {
                        currentPath.lineTo(state.lineEndX, state.lineEndY);
                    } else {
                        currentPath.lineTo(centerX, centerY);
                    }
                    canvas.drawPath(currentPath, mPathPaint);
                }
                lastX = centerX;
                lastY = centerY;
            }

            // draw last in progress section
            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
                    && anyCircles) {
                currentPath.rewind();
                currentPath.moveTo(lastX, lastY);
                currentPath.lineTo(mInProgressX, mInProgressY);

                mPathPaint.setAlpha((int) (calculateLastSegmentAlpha(
                        mInProgressX, mInProgressY, lastX, lastY) * 255f));
                canvas.drawPath(currentPath, mPathPaint);
            }
        }
    }

    private float calculateLastSegmentAlpha(float x, float y, float lastX,
                                            float lastY) {
        float diffX = x - lastX;
        float diffY = y - lastY;
        float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        float frac = dist / mSquareWidth;
        return Math.min(1f, Math.max(0f, (frac - 0.3f) * 4f));
    }

    private int getCurrentColor(boolean partOfPattern) {
        if (!partOfPattern || mInStealthMode || mPatternInProgress) {
            // unselected cell
            return mRegularColor;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            // the pattern is wrong
            return mErrorColor;
        } else if (mPatternDisplayMode == DisplayMode.Correct
                || mPatternDisplayMode == DisplayMode.Animate) {
            return mSuccessColor;
        } else {
            throw new IllegalStateException("unknown display mode "
                    + mPatternDisplayMode);
        }
    }

    /**
     * @param partOfPattern Whether this circle is part of the pattern.
     */
    private void drawCircle(Canvas canvas, float centerX, float centerY,
                            float size, boolean partOfPattern, float alpha) {
        mPaint.setColor(getCurrentColor(partOfPattern));
        mPaint.setAlpha((int) (alpha * 255));
        canvas.drawCircle(centerX, centerY, size / 2, mPaint);
    }

    /**
     * @param partOfPattern Whether this rectangle is part of the pattern.
     */
    private void drawRect(Canvas canvas, float centerX, float centerY,
                          float size, boolean partOfPattern, float alpha) {
        mPaint.setColor(getCurrentColor(partOfPattern));
        mPaint.setAlpha((int) (alpha * 255));
        canvas.drawRect(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, mPaint);
    }

    /**
     * @param partOfPattern Whether this apple is part of the pattern.
     */
    private void drawCustomPoint(Canvas canvas, float centerX, float centerY,
                                 float size, boolean partOfPattern, float alpha) {
        mPaint.setColor(getCurrentColor(partOfPattern));
        mPaint.setAlpha((int) (alpha * 255)); // the default transparency level
        Bitmap apple = BitmapFactory.decodeResource(getResources(), R.drawable.apple);
        canvas.drawBitmap(apple, centerX - (apple.getWidth() / 2), centerY - (apple.getHeight() / 2), mPaint);
    }

    private int dpToPx(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Display the current pattern
     */
    public enum DisplayMode {

        /**
         * The pattern drawn is correct
         */
        Correct,

        /**
         * Animate the pattern
         */
        Animate,

        /**
         * The pattern is wrong
         */
        Wrong
    } // enum DisplayMode

    public static class Cell implements Parcelable {

        public static final Creator<Cell> CREATOR = new Creator<Cell>() {

            public Cell createFromParcel(Parcel in) {
                return new Cell(in);
            }

            public Cell[] newArray(int size) {
                return new Cell[size];
            }
        };
        static Cell[][] sCells = new Cell[LOCK_SIZE][LOCK_SIZE];

        static {
            for (int i = 0; i < LOCK_SIZE; i++) {
                for (int j = 0; j < LOCK_SIZE; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        public final int row, column;

        /**
         * @param row    number or row
         * @param column number of column
         */
        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        private Cell(Parcel in) {
            column = in.readInt();
            row = in.readInt();
        }

        /**
         * @param row    The row of the cell.
         * @param column The column of the cell.
         */
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        /**
         * Get a cell
         * @param id the cell ID.
         * @return the cell.
         */
        public static synchronized Cell of(int id) {
            return of(id / LOCK_SIZE, id % LOCK_SIZE);
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > LOCK_SIZE - 1) {
                throw new IllegalArgumentException("row must be in range 0-"
                        + (LOCK_SIZE - 1));
            }
            if (column < 0 || column > LOCK_SIZE - 1) {
                throw new IllegalArgumentException("column must be in range 0-"
                        + (LOCK_SIZE - 1));
            }
        }

        /**
         * Get the ID
         * @return the ID.
         */
        public int getId() {
            return row * LOCK_SIZE + column;
        }// getId()

        /**
         * @return Row and Column in String.
         */
        @Override
        public String toString() {
            return "(ROW=" + row + ",COL=" + column + ")";
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Cell)
                return column == ((Cell) object).column
                        && row == ((Cell) object).row;
            return super.equals(object);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(column);
            dest.writeInt(row);
        }

    } // class Cell

    public static abstract class OnPatternListener {

        /**
         * Start a new pattern
         */
        public void onPatternStart() {

        }

        /**
         * Delete the old pattern
         */
        public void onPatternCleared() {

        }

        /**
         * Add a cell to the pattern
         * @param pattern The pattern
         */
        public void onPatternCellAdded(List<Cell> pattern, String SimplePattern) {

        }

        /**
         * Detect a new pattern
         * @param pattern The pattern
         */
        public void onPatternDetected(List<Cell> pattern, String SimplePattern) {

        }
    } // class OnPatternListener

    public static class CellState {
        public float scale = 1.0f;
        public float translateY = 0.0f;
        public float alpha = 1.0f;
        public float size;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        public ValueAnimator lineAnimator;
    } // class CellState
}
