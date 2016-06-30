/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Siemens AG and the thingweb community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.thingweb.gui.text;

import javax.swing.text.BadLocationException;

public class DoubleRangeDocumentFilter extends AbstractDocumentFilter {

		final Double minimum;
		final Double maximum;
		final boolean isExclusiveMinimum;
		final boolean isExclusiveMaximum;
		
		public DoubleRangeDocumentFilter(double minimum, double maximum) {
			this(minimum, maximum, false, false);
		}
		
		private DoubleRangeDocumentFilter(double minimum, double maximum, boolean isExclusiveMinimum, boolean isExclusiveMaximum) {
			super();
			this.minimum = minimum;
			this.maximum = maximum;
			this.isExclusiveMinimum = isExclusiveMinimum;
			this.isExclusiveMaximum = isExclusiveMaximum;
		}
		
		

		@Override
		Object checkInput(String proposedValue, int offset)
				throws BadLocationException {
			Double newValue = 0.0;
			if (proposedValue.length() > 0) {
				if(proposedValue.endsWith("d") || proposedValue.endsWith("D")) {
					throw new BadLocationException(proposedValue, offset);
				} else {
					try {
						newValue = new Double(proposedValue);
					} catch (NumberFormatException e) {
						try {
							// maybe 12. or 12E missing number
							String proposedValue2 = proposedValue + "0";
							newValue = new Double(proposedValue2);
						} catch (NumberFormatException e2) {
							throw new BadLocationException(proposedValue, offset);
						}
					}
				}
			}
			// TODO isExclusiveMinimum && isExclusiveMaximum
			
			if(minimum.compareTo(newValue) <= 0 && newValue.compareTo(maximum) <= 0) {
				return newValue;
			} else {
				throw new BadLocationException(proposedValue, offset);
			}
		}
	}