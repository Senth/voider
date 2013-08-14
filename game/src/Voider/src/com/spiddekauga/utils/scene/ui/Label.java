/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.StringBuilder;
import com.spiddekauga.voider.utils.Pools;

/** A text label, with optional word wrapping.
 * <p>
 * Unlike most scene2d.ui widgets, label can be scaled and rotated using the actor's scale, rotation, and origin. This only
 * affects drawing, other scene2d.ui widgets will still use the unscaled and unrotated bounds of the label. Note that a scaled or
 * rotated label causes a SpriteBatch flush when it is drawn, so should be used relatively sparingly.
 * <p>
 * The preferred size of the label is determined by the actual text bounds, unless {@link #setWrap(boolean) word wrap} is enabled.
 * @author Nathan Sweet
 * @author Matteus Magnusson <senth.wallace@gmail.com> Added doxygen and changed wrap behavior
 * */
public class Label extends Widget {

	/**
	 * Creates a label with the specified text
	 * @param text this will be displayed on the label
	 * @param skin the skin to use for the text
	 */
	public Label (CharSequence text, Skin skin) {
		this(text, skin.get(LabelStyle.class));
	}

	/**
	 * Creates a label with the specified text and specified style name
	 * @param text this will be displayed on the label
	 * @param skin skin to use for the text
	 * @param styleName name of the LabelStyle to use for the label
	 */
	public Label (CharSequence text, Skin skin, String styleName) {
		this(text, skin.get(styleName, LabelStyle.class));
	}

	/**
	 * Creates a label, using a {@link LabelStyle} that has a BitmapFont with
	 * the specified name from the skin and the specified color.
	 * @param text this will be displayed on the label
	 * @param skin skin to use for the text, uses default LabelStyle
	 * @param fontName the font to use for the label
	 * @param color color to use on the text
	 */
	public Label (CharSequence text, Skin skin, String fontName, Color color) {
		this(text, new LabelStyle(skin.getFont(fontName), color));
	}

	/**
	 * Creates a label, using a {@link LabelStyle} that has a BitmapFont with
	 * the specified name and the specified color from the skin.
	 * @param text this will be displayed on the label
	 * @param skin skin to use for the text, uses default LabelStyle
	 * @param fontName the font to use for the label
	 * @param colorName name of the color to use on the text
	 */
	public Label (CharSequence text, Skin skin, String fontName, String colorName) {
		this(text, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
	}

	/**
	 * Creates a label with the specified text using the labelStyle for the font and
	 * font color
	 * @param text this will be displayed on the label
	 * @param style label style to use for the label, this determines the font and its color
	 */
	public Label (CharSequence text, LabelStyle style) {
		if (text != null) {
			this.mText.append(text);
		}
		setStyle(style);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	/**
	 * Sets another style for the label
	 * @param style new label style for the label
	 */
	public void setStyle (LabelStyle style) {
		if (style == null) {
			throw new IllegalArgumentException("style cannot be null.");
		}
		if (style.font == null) {
			throw new IllegalArgumentException("Missing LabelStyle font.");
		}
		this.mStyle = style;
		mCache = new BitmapFontCache(style.font, style.font.usesIntegerPositions());
		invalidateHierarchy();
	}

	/**
	 * @return the label's style. Modifying the returned style may not have an
	 * effect until {@link #setStyle(LabelStyle)} is called.
	 */
	public LabelStyle getStyle () {
		return mStyle;
	}

	/**
	 * Sets a new text of the label, automatically resizes the label
	 * @param newText May be null.
	 */
	public void setText (CharSequence newText) {
		if (newText instanceof StringBuilder) {
			if (mText.equals(newText)) {
				return;
			}
			mText.setLength(0);
			mText.append((StringBuilder)newText);
		} else {
			if (newText == null) {
				newText = "";
			}
			if (textEquals(newText)) {
				return;
			}
			mText.setLength(0);
			mText.append(newText);
		}
		invalidateHierarchy();
		pack();
	}

	/**
	 * Checks if the other text is equal to the text inside this label
	 * @param other the other text to test with
	 * @return true if other and the text inside this label matches.
	 */
	private boolean textEquals (CharSequence other) {
		int length = mText.length;
		char[] chars = mText.chars;
		if (length != other.length()) {
			return false;
		}
		for (int i = 0; i < length; i++) {
			if (chars[i] != other.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return text inside this lable
	 */
	public CharSequence getText () {
		return mText;
	}

	@Override
	public void invalidate () {
		super.invalidate();
		mSizeInvalid = true;
	}

	/**
	 * Computes the size of the label
	 */
	private void computeSize () {
		mSizeInvalid = false;
		if (mWrap) {
			float width = getWidth();
			if (mStyle.background != null) {
				width -= mStyle.background.getLeftWidth() + mStyle.background.getRightWidth();
			}
			mBounds.set(mCache.getFont().getWrappedBounds(mText, width));
		}
		else {
			mBounds.set(mCache.getFont().getMultiLineBounds(mText));
		}
		mBounds.width *= mFontScaleX;
		mBounds.height *= mFontScaleY;
	}

	@Override
	public void layout () {
		if (mSizeInvalid) {
			computeSize();
		}

		if (mWrap) {
			float prefHeight = getPrefHeight();
			if (prefHeight != mLastPrefHeight) {
				mLastPrefHeight = prefHeight;
				invalidateHierarchy();
			}
		}

		BitmapFont font = mCache.getFont();
		float oldScaleX = font.getScaleX();
		float oldScaleY = font.getScaleY();
		if (mFontScaleX != 1 || mFontScaleY != 1) {
			font.setScale(mFontScaleX, mFontScaleY);
		}

		Drawable background = mStyle.background;
		float width = getWidth(), height = getHeight();
		float x = 0, y = 0;
		if (background != null) {
			x = background.getLeftWidth();
			y = background.getBottomHeight();
			width -= background.getLeftWidth() + background.getRightWidth();
			height -= background.getBottomHeight() + background.getTopHeight();
		}
		if ((mLabelAlign & Align.top) != 0) {
			y += mCache.getFont().isFlipped() ? 0 : height - mBounds.height;
			y += mStyle.font.getDescent();
		} else if ((mLabelAlign & Align.bottom) != 0) {
			y += mCache.getFont().isFlipped() ? height - mBounds.height : 0;
			y -= mStyle.font.getDescent();
		}
		else {
			y += (int)((height - mBounds.height) / 2);
		}
		if (!mCache.getFont().isFlipped()) {
			y += mBounds.height;
		}

		if ((mLabelAlign & Align.left) == 0) {
			if ((mLabelAlign & Align.right) != 0) {
				x += width - mBounds.width;
			}
			else {
				x += (int)((width - mBounds.width) / 2);
			}
		}

		if (mWrap) {
			mCache.setWrappedText(mText, x, y, mBounds.width, mLineAlign);
		}
		else {
			mCache.setMultiLineText(mText, x, y, mBounds.width, mLineAlign);
		}

		if (mFontScaleX != 1 || mFontScaleY != 1) {
			font.setScale(oldScaleX, oldScaleY);
		}
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
		Color color = getColor();
		if (mStyle.background != null) {
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			mStyle.background.draw(batch, getX(), getY(), getWidth(), getHeight());
		}
		Color tmpColor = Pools.color.obtain();
		mCache.setColor(mStyle.fontColor == null ? color : tmpColor.set(color).mul(mStyle.fontColor));
		Pools.color.free(tmpColor);
		mCache.setPosition(getX(), getY());
		mCache.draw(batch, color.a * parentAlpha);
	}

	@Override
	public float getPrefWidth () {
		if (mSizeInvalid) {
			computeSize();
		}
		float width = mBounds.width;
		Drawable background = mStyle.background;
		if (background != null) {
			width += background.getLeftWidth() + background.getRightWidth();
		}
		return width;
	}

	@Override
	public float getPrefHeight () {
		if (mSizeInvalid) {
			computeSize();
		}
		float height = mBounds.height - mStyle.font.getDescent() * 2;
		Drawable background = mStyle.background;
		if (background != null) {
			height += background.getTopHeight() + background.getBottomHeight();
		}
		return height;
	}

	/**
	 * @return bounds of the text
	 */
	public TextBounds getTextBounds () {
		if (mSizeInvalid) {
			computeSize();
		}
		return mBounds;
	}

	/**
	 * @param wrap If false, the text will only wrap where it contains newlines (\n).
	 * The preferred size of the label will be the text bounds.
	 * If true, the text will word wrap using the width of the label.
	 * The preferred width of the label return its current width, it is expected
	 * that the something external will set the width of the label. Default is false. */
	public void setWrap (boolean wrap) {
		this.mWrap = wrap;
		invalidateHierarchy();
	}

	/**
	 * @param wrapAlign Aligns each line of text horizontally and all the text vertically.
	 * @see Align
	 */
	public void setAlignment (int wrapAlign) {
		setAlignment(wrapAlign, wrapAlign);
	}

	/** @param labelAlign Aligns all the text with the label widget.
	 * @param lineAlign Aligns each line of text (left, right, or center).
	 * @see Align */
	public void setAlignment (int labelAlign, int lineAlign) {
		this.mLabelAlign = labelAlign;

		if ((lineAlign & Align.left) != 0) {
			this.mLineAlign = HAlignment.LEFT;
		}
		else if ((lineAlign & Align.right) != 0) {
			this.mLineAlign = HAlignment.RIGHT;
		}
		else {
			this.mLineAlign = HAlignment.CENTER;
		}

		invalidate();
	}

	/**
	 * Sets both x and y scale of the font
	 * @param fontScale scaling of the font
	 */
	public void setFontScale (float fontScale) {
		this.mFontScaleX = fontScale;
		this.mFontScaleY = fontScale;
		invalidateHierarchy();
	}

	/**
	 * Sets different font scaling for x and y
	 * @param fontScaleX scaling of the font
	 * @param fontScaleY scaling of the font
	 */
	public void setFontScale (float fontScaleX, float fontScaleY) {
		this.mFontScaleX = fontScaleX;
		this.mFontScaleY = fontScaleY;
		invalidateHierarchy();
	}

	/**
	 * @return current horizontal font scaling
	 */
	public float getFontScaleX () {
		return mFontScaleX;
	}

	/**
	 * Sets horizontal font scaling
	 * @param fontScaleX horizontal scaling
	 */
	public void setFontScaleX (float fontScaleX) {
		this.mFontScaleX = fontScaleX;
		invalidateHierarchy();
	}

	/**
	 * @return current vertical scaling
	 */
	public float getFontScaleY () {
		return mFontScaleY;
	}

	/**
	 * Current
	 * @param fontScaleY
	 */
	public void setFontScaleY (float fontScaleY) {
		this.mFontScaleY = fontScaleY;
		invalidateHierarchy();
	}

	/** The style for a label, see {@link Label}.
	 * @author Nathan Sweet */
	static public class LabelStyle {
		/** Font of the label */
		public BitmapFont font;
		/** Color, optional. */
		public Color fontColor;
		/** Background, optional. */
		public Drawable background;

		/**
		 * Default constructor
		 */
		public LabelStyle () {
		}

		/**
		 * Sets a font and a color to the label style
		 * @param font font to use for the style
		 * @param fontColor color of the font
		 */
		public LabelStyle (BitmapFont font, Color fontColor) {
			this.font = font;
			this.fontColor = fontColor;
		}

		/**
		 * Copies the style from another label style
		 * @param style other label style to copy values from
		 */
		public LabelStyle (LabelStyle style) {
			this.font = style.font;
			if (style.fontColor != null) {
				this.fontColor = new Color(style.fontColor);
			}
		}
	}

	/** Style for the label */
	private LabelStyle mStyle;
	/** Outer bounds */
	private final TextBounds mBounds = new TextBounds();
	/** The actual text to be displayed */
	private final StringBuilder mText = new StringBuilder();
	/** Bitmap fonts */
	private BitmapFontCache mCache;
	/** Label alignment */
	private int mLabelAlign = Align.left;
	/** Horizontal alignment */
	private HAlignment mLineAlign = HAlignment.LEFT;
	/** If the text shall be wrapped or not, if wrapped be sure to set the width manually */
	private boolean mWrap = false;
	/** Last preferred height */
	private float mLastPrefHeight;
	/** If the size needs to be recalculated */
	private boolean mSizeInvalid = true;
	/** Scaling of font */
	private float mFontScaleX = 1;
	/** Scaling of font */
	private float mFontScaleY = 1;
}
